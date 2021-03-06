package com.breeze.hib;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;


import org.hibernate.FlushMode;
import org.hibernate.JDBCException;
import org.hibernate.PropertyValueException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.metadata.ClassMetadata;

import com.breeze.metadata.DataType;
import com.breeze.metadata.Metadata;
import com.breeze.save.*;

public class HibernateSaveProcessor extends SaveProcessor {

    private SessionFactory _sessionFactory;
    private Session _session;
    private RelationshipFixer _fixer;
    private List<String> _possibleErrors = new ArrayList<String>();
   

    /**
     * @param metadata
     * @param sessionFactory 
     */
    public HibernateSaveProcessor(Metadata metadata, SessionFactory sessionFactory) {
        super(metadata);
        _sessionFactory = sessionFactory;
    }

    /**
     * Persist the changes to the entities in the SaveWorkState that is passed in.
     * This implements the abstract method in SaveProcessor.
     * After the completion of this method the saveWorkState.toSaveResult method may be called the will
     * return a SaveResult instance.
     * This method both persists the entities and creates the collection of KeyMappings, 
     * which map the temporary keys to their real generated keys.
     * Note that this method sets session.FlushMode = FlushMode.MANUAL, so manual flushes are required.
     * @param saveWorkState
     */
    @Override
    protected void saveChangesCore(SaveWorkState saveWorkState) {
        _session = _sessionFactory.openSession();
        _session.setFlushMode(FlushMode.MANUAL);
        Transaction tx = _session.getTransaction();
        boolean hasExistingTransaction = tx.isActive();
        if (!hasExistingTransaction)  tx.begin();
        try {
            // Relate entities in the saveMap to other entities, so Hibernate can save the FK values.
            _fixer = new RelationshipFixer(saveWorkState, _session);
            _fixer.fixupRelationships();
            // At this point all entities are hooked up but are not yet in the session.
            setSaveState(SaveState.AfterFixup);
            // Allow subclass to process entities before we save them
            saveWorkState.beforeSaveEntities();
            List<EntityInfo> saveOrder = _fixer.sortDependencies();
            processSaves(saveOrder);

            // At this point all entities are hooked up and in the session, and
            // all tempIds have been replaced with real ids.

            // Final chance to process entities before we save them - all entities 
            // have been added to the session.
            setSaveState(SaveState.BeforeCommit);
            saveWorkState.beforeCommit(_session);

            _session.flush();
            refreshFromSession(saveWorkState);
            if (!hasExistingTransaction) tx.commit();
            // so that serialization of saveResult doesn't have issues.
            _fixer.removeRelationships();
        } catch (EntityErrorsException eee) {
            if (tx.isActive()) tx.rollback();
            throw eee;
        } catch (PropertyValueException pve) {
            // Hibernate can throw this
            if (tx.isActive()) tx.rollback();
            EntityError entityError = new EntityError("PropertyValueException", 
                    pve.getEntityName(), null,
                    pve.getPropertyName(), pve.getMessage());
            throw new EntityErrorsException(entityError);
        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            String msg = "Save exception: ";
            if (ex instanceof JDBCException) {
                msg += "SQLException: " + ((JDBCException) ex).getSQLException().getMessage();
            } else {
                msg += ex.getMessage(); // most hibernate exceptions appear here
            }
            if (_possibleErrors.size() > 0) {
                msg += ". Possible errors: " + _possibleErrors.toString() + " ";
            }
            throw new RuntimeException(msg, ex);
        } finally {
            //          if (!hasExistingTransaction) tx.Dispose();
            _session.close();
        }

    }
    
    @Override
    public void processRelationships(EntityInfo entityInfo, boolean removeMode) {
        // _fixer will not be initialized until just before beforeSaveEntities is called
        // so it will not be available for beforeSaveEntity calls.
        SaveState saveState = getSaveState();
        if (saveState == SaveState.BeforeFixup) return;
        _fixer.processRelationships(entityInfo, removeMode);
        if (saveState == SaveState.BeforeCommit) {
            processEntity(entityInfo);
        }
    }
    
    // TODO: determine why this is different from getIdentifier commented out below. 
    // May have to do with this method only getting called for single part keys.
    @Override
    public Object getIdentifier(Object entity) {
        Object id = getClassMetadata(entity.getClass()).getIdentifier(entity, null);
        return id != null ? id : null;
    }


    /**
     * Persist the changes to the entities in the saveOrder.
     * @param saveOrder
     */
    protected void processSaves(List<EntityInfo> saveOrder) {
        for (EntityInfo entityInfo : saveOrder) {
            processEntity(entityInfo);
        }
    }

    /**
     * Add, update, or delete the entity according to its EntityState.
     * @param entityInfo
     * @param classMeta
     */
    protected void processEntity(EntityInfo entityInfo) {
        Object entity = entityInfo.entity;
        ClassMetadata classMeta = getClassMetadata(entity.getClass());
        EntityState state = entityInfo.entityState;

        // Restore the old value of the concurrency column so Hibernate will be able to save the entity
        if (classMeta.isVersioned()) {
            restoreOldVersionValue(entityInfo, classMeta);
        }

        if (state == EntityState.Modified) {
            _session.update(entity);
        } else if (state == EntityState.Added) {
            _session.save(entity);
        } else if (state == EntityState.Deleted) {
            _session.delete(entity);
        } else {
            // Ignore EntityState.Unchanged.  Too many problems using session.Lock or session.Merge
            //session.Lock(entity, LockMode.None);
        }
    }


    /**
     * Restore the old value of the concurrency column so Hibernate will save the entity.
     * Otherwise it will complain because Breeze has already changed the value.
     * @param entityInfo
     * @param classMeta
     */
    protected void restoreOldVersionValue(EntityInfo entityInfo, ClassMetadata classMeta) {
        if (entityInfo.originalValuesMap == null || entityInfo.originalValuesMap.size() == 0)
            return;
        int vcol = classMeta.getVersionProperty();
        String vname = classMeta.getPropertyNames()[vcol];
        if (entityInfo.originalValuesMap.containsKey(vname)) {
            Object oldVersion = entityInfo.originalValuesMap.get(vname);
            Object entity = entityInfo.entity;
            if (oldVersion == null) {
                _possibleErrors.add("Hibernate does not support 'null' version properties. " +
                        "Entity: " + entity + ", Property: " + vname);
            }
            Class versionClazz = classMeta.getPropertyTypes()[vcol].getReturnedClass();
            DataType dataType = DataType.fromClass(versionClazz);
            Object oldValue = DataType.coerceData(oldVersion, dataType);
            classMeta.setPropertyValue(entity, vname, oldValue);
        }
    }


    /**
     * Refresh the entities from the database.  This picks up changes due to triggers, etc.
     * and makes Hibernate update the foreign keys.
     * @param saveMap
     */
    protected void refreshFromSession(SaveWorkState saveWorkState) {
        for (Entry<Class, List<EntityInfo>> entry : saveWorkState.entrySet()) {
            for (EntityInfo entityInfo : entry.getValue()) {
                if (entityInfo.entityState == EntityState.Added || entityInfo.entityState == EntityState.Modified) {
                    _session.refresh(entityInfo.entity);
                }
            }
        }

    }
    
    // perf 
    private Class _classCached;
    private ClassMetadata _classMetadataCached;

    private ClassMetadata getClassMetadata(Class clazz) {
        // perf enhancement - this method gets called a lot in loops.
        if (clazz != _classCached) {
            _classCached = clazz;
            _classMetadataCached = _sessionFactory.getClassMetadata(clazz);
        }
        return _classMetadataCached;
    }
    
//  /**
//  * Get the identifier value for the entity.  If the entity does not have an
//  * identifier property, or natural identifiers defined, then the entity itself is returned.
//  * @param entity
//  * @param meta
//  * @return
//  */
// protected Object getIdentifier(Object entity) {
//     Class type = entity.getClass();
//     meta = getClassMetadata(type);
//
//     Type idType = meta.getIdentifierType();
//     if (idType != null) {
//         Serializable id = meta.getIdentifier(entity, null);
//         if (idType.isComponentType()) {
//             ComponentType compType = (ComponentType) idType;
//             return compType.getPropertyValues(id, EntityMode.POJO);
//         } else {
//             return id;
//         }
//     } else if (meta.hasNaturalIdentifier()) {
//         int[] idprops = meta.getNaturalIdentifierProperties();
//         Object[] values = meta.getPropertyValues(entity);
//         Object[] idvalues = new Object[idprops.length];
//         for (int i = 0; i < idprops.length; i++) {
//             idvalues[i] = values[idprops[i]];
//         }
//         return idvalues;
//     }
//     return entity;
// }

    
}
