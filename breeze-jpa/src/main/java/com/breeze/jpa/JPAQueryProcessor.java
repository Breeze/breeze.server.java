package com.breeze.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import com.breeze.metadata.IEntityType;
import com.breeze.metadata.Metadata;
import com.breeze.metadata.MetadataHelper;
import com.breeze.query.EntityQuery;
import com.breeze.query.QueryProcessor;
import com.breeze.query.QueryResult;

public class JPAQueryProcessor extends QueryProcessor {
    private EntityManagerFactory _emFactory;

    public JPAQueryProcessor(Metadata metadata, EntityManagerFactory emFactory) {
        super(metadata);
        _emFactory = emFactory;
    }

    @Override
    protected QueryResult executeQuery(IEntityType entityType, EntityQuery entityQuery) {

        entityQuery.validate(entityType);
        Class<?> clazz = MetadataHelper.lookupClass(entityType.getName());
        
        EntityManager em = _emFactory.createEntityManager();
        try {
            em.getTransaction().begin();
            
            CriteriaBuilder cb = em.getCriteriaBuilder();
            CriteriaQuery<?> cq = cb.createQuery(clazz);
            JPACriteriaBuilder.create(cq, cb, entityType, entityQuery);
//            Root<?> pet = cq.from(clazz);
//            pet.
//            cq.select(pet);
            TypedQuery<?> q = em.createQuery(cq);
            List<?> result = q.getResultList();       
            
            QueryResult qr;
            if (entityQuery.isInlineCountEnabled()) {
//                builder.applyInlineCount(crit);
                long countResult = 7; //(Long) crit.uniqueResult();
//                log.debugv("query: inline count={0}", countResult);

                qr = new QueryResult(result, countResult);
            } else {
                qr = new QueryResult(result);
            }
            em.getTransaction().commit();
            return qr;
        }
        catch (Exception ex) {
            em.getTransaction().rollback();
            throw ex;
        } finally {
            em.clear();
            em.close();
        }
    }

}
