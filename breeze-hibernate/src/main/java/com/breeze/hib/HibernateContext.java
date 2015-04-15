package com.breeze.hib;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.hibernate.EntityMode;
import org.hibernate.FlushMode;
import org.hibernate.JDBCException;
import org.hibernate.PropertyValueException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

import com.breeze.metadata.DataType;
import com.breeze.metadata.Metadata;
import com.breeze.save.*;

public class HibernateContext extends ContextProvider {

    private Session _session;
    private SessionFactory _sessionFactory;

    private List<EntityError> _entityErrors = new ArrayList<EntityError>();
    // private Map<EntityInfo, KeyMapping> _entityKeyMapping = new HashMap<EntityInfo, KeyMapping>();
    private List<String> _possibleErrors = new ArrayList<String>();
    private RelationshipFixer _fixer;
    private Class _classCached;
    private ClassMetadata _classMetadataCached;

    /**
     * @param session Hibernate session to be used for saving
     * @param metadataMap metadata from MetadataBuilder
     */
    public HibernateContext(Session session, Metadata metadata) {
        super(metadata);
        this._session = session;
        this._sessionFactory = session.getSessionFactory();
    }

    /**
     * Persist the changes to the entities in the saveMap.
     * This implements the abstract method in ContextProvider.
     * Assigns saveWorkState.KeyMappings, which map the temporary keys to their real generated keys.
     * Note that this method sets session.FlushMode = FlushMode.MANUAL, so manual flushes are required.
     * @param saveWorkState
     * @throws Exception 
     */
    @Override
    protected void saveChangesCore(SaveWorkState saveWorkState) {

        _session.setFlushMode(FlushMode.MANUAL);
        Transaction tx = _session.getTransaction();
        boolean hasExistingTransaction = tx.isActive();
        if (!hasExistingTransaction)
            tx.begin();
        try {
            // Relate entities in the saveMap to other entities, so Hibernate can save the FK values.
            _fixer = new RelationshipFixer(saveWorkState, _session);
            _fixer.fixupRelationships();
            // Allow subclass to process entities before we save them
            saveWorkState.beforeSaveEntities();
            List<EntityInfo> saveOrder = _fixer.sortDependencies();
            processSaves(saveOrder);

            // saveContext = saveWorkState.beforeSessionPersist(saveContext, _session)

            _session.flush();
            refreshFromSession(saveWorkState);
            if (!hasExistingTransaction) {
                tx.commit();
            }
            _fixer.removeRelationships();
        } catch (PropertyValueException pve) {
            // Hibernate can throw this
            if (tx.isActive())        tx.rollback();
            _entityErrors.add(new EntityError("PropertyValueException", pve.getEntityName(), null,
                    pve.getPropertyName(), pve.getMessage()));
            saveWorkState.setEntityErrors(new EntityErrorsException(null, _entityErrors));
        } catch (Exception ex) {
            if (tx.isActive()) tx.rollback();
            if (ex instanceof EntityErrorsException) {
                throw ex;
            }
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
        }

    }
    
    @Override
    public void processRelationships(EntityInfo entityInfo, boolean removeMode) {
        // _fixer will not be initialized until just before beforeSaveEntities is called
        // so it will not be available for beforeSaveEntity calls.
        if (_fixer != null) {
            _fixer.processRelationships(entityInfo, removeMode);
        }
    }
    

    /**
     * Persist the changes to the entities in the saveOrder.
     * @param _saveMap
     */
    protected void processSaves(List<EntityInfo> saveOrder) {

        for (EntityInfo entityInfo : saveOrder) {
            Class entityType = entityInfo.entity.getClass();
            ClassMetadata classMeta = getClassMetadata(entityType);
            // addKeyMapping(entityInfo, entityType, classMeta);
            processEntity(entityInfo, classMeta);
        }
    }
    
    

    /**
     * Add, update, or delete the entity according to its EntityState.
     * @param entityInfo
     * @param classMeta
     */
    protected void processEntity(EntityInfo entityInfo, ClassMetadata classMeta) {
        Object entity = entityInfo.entity;
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

    // TODO: determine why this is different from getIdentifier below. Used by relationshipFixer
    @Override
    public Object getIdentifier(EntityInfo entityInfo) {
        Object entity = entityInfo.entity;
        Object id = getClassMetadata(entity.getClass()).getIdentifier(entity, null);
        return id != null ? id.toString() : null;
    }

    /**
     * Get the identifier value for the entity.  If the entity does not have an
     * identifier property, or natural identifiers defined, then the entity itself is returned.
     * @param entity
     * @param meta
     * @return
     */
    protected Object getIdentifier(Object entity, ClassMetadata meta) {
        Class type = entity.getClass();
        if (meta == null) {
            meta = getClassMetadata(type);
        }

        Type idType = meta.getIdentifierType();
        if (idType != null) {
            Serializable id = meta.getIdentifier(entity, null);
            if (idType.isComponentType()) {
                ComponentType compType = (ComponentType) idType;
                return compType.getPropertyValues(id, EntityMode.POJO);
            } else {
                return id;
            }
        } else if (meta.hasNaturalIdentifier()) {
            int[] idprops = meta.getNaturalIdentifierProperties();
            Object[] values = meta.getPropertyValues(entity);
            Object[] idvalues = new Object[idprops.length];
            for (int i = 0; i < idprops.length; i++) {
                idvalues[i] = values[idprops[i]];
            }
            return idvalues;
        }
        return entity;
    }

    /**
     * Get the identifier value for the entity as an object[].  This is needed for creating an EntityError.
     * @param entity
     * @param meta
     * @return
     */
    protected Object[] getIdentifierAsArray(Object entity, ClassMetadata meta) {
        Object value = getIdentifier(entity, meta);
        if (value.getClass().isArray()) {
            return (Object[]) value;
        } else {
            return new Object[] { value };
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

    private ClassMetadata getClassMetadata(Class clazz) {
        if (clazz != _classCached) {
            _classCached = clazz;
            _classMetadataCached = _sessionFactory.getClassMetadata(clazz);
        }
        return _classMetadataCached;
    }
}
