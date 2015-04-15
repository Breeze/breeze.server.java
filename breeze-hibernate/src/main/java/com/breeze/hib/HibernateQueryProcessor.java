package com.breeze.hib;

import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Hibernate;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.proxy.HibernateProxy;
import org.jboss.logging.Logger;

import com.breeze.metadata.IEntityType;
import com.breeze.metadata.Metadata;
import com.breeze.metadata.MetadataHelper;
import com.breeze.query.EntityQuery;
import com.breeze.query.ExpandClause;
import com.breeze.query.QueryProcessor;
import com.breeze.query.QueryResult;

public class HibernateQueryProcessor extends QueryProcessor {
    public static final Logger log = Logger.getLogger(HibernateQueryProcessor.class);
    private SessionFactory _sessionFactory;

    public HibernateQueryProcessor(Metadata metadata, SessionFactory sessionFactory) {
        super(metadata);
        _sessionFactory = sessionFactory;
    }

    public QueryResult executeQuery(IEntityType entityType, EntityQuery entityQuery) {
            
        entityQuery.validate(entityType);
        Class<?> clazz = MetadataHelper.lookupClass(entityType.getName());

        Session session = _sessionFactory.openSession();
        try {
            session.beginTransaction();
            
            Criteria crit = session.createCriteria(clazz, "root");
            CriteriaBuilder builder = CriteriaBuilder.create(crit, entityType, entityQuery);
            // execute the query
            List result = crit.list();

            log.debugv("query: result size={0}", result.size());
            ExpandClause expandClause = entityQuery.getExpandClause();
            if (expandClause != null) {
                List<String> expands = expandClause.getPropertyPaths();
                HibernateExpander.initializeList(result, expands);
            }

            // HACK:
            // Handles select's where at least one of the projected values is
            // itself a navigation property (either scalar or nonscalar).
            if (builder.containsNavPropertyProxy()
                    && entityQuery.getSelectClause() != null) {
                for (Object row : result) {
                    for (Object value : ((Map) row).values()) {
                        if (value instanceof HibernateProxy) {
                            Hibernate.initialize(value);
                        }
                    }
                }
            }
            
            QueryResult qr;
            if (entityQuery.isInlineCountEnabled()) {
                builder.applyInlineCount(crit);
                long countResult = (Long) crit.uniqueResult();
                log.debugv("query: inline count={0}", countResult);

                qr = new QueryResult(result, countResult);
            } else {
                qr = new QueryResult(result);
            }

            session.getTransaction().commit();
            return qr;
        } catch (RuntimeException e) {
            session.getTransaction().rollback();
            throw e; // or display error message
        } finally {
            session.close();
        }
    }

}
