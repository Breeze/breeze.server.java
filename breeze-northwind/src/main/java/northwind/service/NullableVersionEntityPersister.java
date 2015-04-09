package northwind.service;

import java.io.Serializable;

import org.hibernate.HibernateException;
import org.hibernate.cache.spi.access.EntityRegionAccessStrategy;
import org.hibernate.cache.spi.access.NaturalIdRegionAccessStrategy;
import org.hibernate.engine.spi.Mapping;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metamodel.binding.EntityBinding;
import org.hibernate.persister.entity.SingleTableEntityPersister;

/**
 * Allows use of null version column by modifying the SQL during updates and deletes.
 * Don't use this if you can make your version column not-null. 
 */
public class NullableVersionEntityPersister extends SingleTableEntityPersister {

    public NullableVersionEntityPersister(EntityBinding entityBinding, EntityRegionAccessStrategy cacheAccessStrategy,
            NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy, SessionFactoryImplementor factory, Mapping mapping)
            throws HibernateException {
        super(entityBinding, cacheAccessStrategy, naturalIdRegionAccessStrategy, factory, mapping);
    }

    public NullableVersionEntityPersister(PersistentClass persistentClass, EntityRegionAccessStrategy cacheAccessStrategy,
            NaturalIdRegionAccessStrategy naturalIdRegionAccessStrategy, SessionFactoryImplementor factory, Mapping mapping)
            throws HibernateException {
        super(persistentClass, cacheAccessStrategy, naturalIdRegionAccessStrategy, factory, mapping);
    }

    /**
     * Handle null oldVersion values by modifying the sql
     */
    @Override
    protected boolean update(Serializable id, Object[] fields, Object[] oldFields, Object rowId, boolean[] includeProperty, int j,
            Object oldVersion, Object object, String sql, SessionImplementor session) throws HibernateException {
        
        if (oldVersion == null) {
            sql = replaceVersionClause(sql);
        }
        
        return super.update(id, fields, oldFields, rowId, includeProperty, j, oldVersion, object, sql, session);
    }

    /**
     * Handle null version values by modifying the sql
     */
    @Override
    protected void delete(Serializable id, Object version, int j, Object object, String sql, SessionImplementor session,
            Object[] loadedState) throws HibernateException {

        if (version == null) {
            sql = replaceVersionClause(sql);
        }
        super.delete(id, version, j, object, sql, session, loadedState);
    }

    /**
     * Make the sql say "(rowVersion=? or rowVersion is null)" instead of "rowVersion=?"
     */
    protected String replaceVersionClause(String sql)
    {
        String vname = this.getVersionColumnName();
        int where = sql.toLowerCase().indexOf("where");
        String whereClause = sql.substring(where);
        String orig = vname + "=?";
        String repl = '(' + orig + " or " + vname + " is null)";
        whereClause = whereClause.replace(orig, repl);
        sql = sql.substring(0, where) + whereClause; 
        return sql;
    }
    
}
