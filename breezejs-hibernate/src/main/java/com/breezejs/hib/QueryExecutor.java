package com.breezejs.hib;

import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jboss.logging.Logger;

import com.breezejs.Metadata;
import com.breezejs.MetadataAdapter;
import com.breezejs.QueryResult;
import com.breezejs.metadata.IEntityType;
import com.breezejs.query.EntityQuery;
import com.breezejs.query.ExpandClause;
import com.breezejs.util.Reflect;

public class QueryExecutor {
	public static final Logger log = Logger.getLogger(QueryExecutor.class);
	private SessionFactory _sessionFactory;
	private MetadataAdapter _metadataAdapter;

	public QueryExecutor(SessionFactory sessionFactory) {
		this._sessionFactory = sessionFactory;
		
		MetadataBuilder mb = new MetadataBuilder(sessionFactory);
		Metadata metadata = mb.buildMetadata();
		this._metadataAdapter = new MetadataAdapter(metadata);
	}
	
	public QueryResult executeQuery(EntityQuery entityQuery) {
		String resourceName = entityQuery.getResourceName();
		if (resourceName == null) return null;
		IEntityType entityType = _metadataAdapter.getEntityTypeForResourceName(resourceName);
		return executeQuery(entityQuery, entityType);
	}
	
	
	public QueryResult executeQuery(String resourceName, String json) {
		EntityQuery entityQuery = new EntityQuery(json);
		IEntityType entityType = _metadataAdapter.getEntityTypeForResourceName(resourceName);
		return executeQuery(entityQuery, entityType);
	}
	
	public QueryResult executeQuery(Class<?> clazz, String json) {
		EntityQuery eq = new EntityQuery(json);
		IEntityType entityType = _metadataAdapter.getEntityTypeForClass(clazz);
		return executeQuery(eq, entityType);
	}

	/**
	 * Create and execute an EntityQuery using the given parameters
	 * @param clazz the entity class, e.g. Customer
	 * @param op OdataParameters representing the OData operations on the query
	 * @return the query results as JSON
	 */
	public QueryResult executeQuery(EntityQuery entityQuery, IEntityType entityType) {
		entityQuery.validate(entityType);

		Class<?> clazz = Reflect.lookupEntityType(entityType.getName());

		// log.debugv("executeQuery: class={0}, odataParameters={1}", entityQuery.getResourceName, op);
		QueryResult qr;
		Session session = _sessionFactory.openSession();
		try {
			session.beginTransaction();
			Criteria crit = session.createCriteria(clazz);
			CriteriaBuilder builder = new CriteriaBuilder();
	    	builder.updateCriteria(crit, entityQuery);
	    	// execute the query
	    	List result = crit.list();
	    	
			log.debugv("query: result size={0}", result.size());
			ExpandClause expandClause = entityQuery.getExpandClause();
			if (expandClause != null) {
				List<String> propertyPaths = expandClause.getPropertyPaths(); 
				String[] expands = propertyPaths.toArray(new String[propertyPaths.size()]);
				HibernateExpander.initializeList(result, expands);
			}
			
			if (entityQuery.isInlineCountEnabled()) {
				builder.applyInlineCount(crit);
				long countResult = (Long) crit.uniqueResult();
				log.debugv("queryn: inline count={0}", countResult);
				
				qr = new QueryResult(result, countResult);
			} else {
				qr = new QueryResult(result);
			}
				
			session.getTransaction().commit();
			return qr;
		}
    	catch (RuntimeException e) {
    		session.getTransaction().rollback();
    	    throw e; // or display error message
    	}
    	finally {
    		session.close();
    	}    	
	}
	
	
}
