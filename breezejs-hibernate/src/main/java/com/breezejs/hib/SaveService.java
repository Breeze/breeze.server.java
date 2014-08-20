package com.breezejs.hib;

import javax.ws.rs.core.Response;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jboss.logging.Logger;

import com.breezejs.Metadata;
import com.breezejs.save.ContextProvider;
import com.breezejs.save.SaveResult;
import com.breezejs.util.Json;

/**
 * Class to receive JSON save bundles and save them to Hibernate
 * @author Steve
 *
 */
public class SaveService {

	public static final Logger log = Logger.getLogger(SaveService.class);
	private SessionFactory sessionFactory;
	private Metadata metadata;
	
	public SaveService(SessionFactory sessionFactory, Metadata metadata) {
		this.sessionFactory = sessionFactory;
		this.metadata = metadata;
	}
	
	/**
	 * Save the changes and return a response indicated the updated entities
	 * or errors
	 * @param source
	 * @return
	 */
	public Response saveChanges(String source) {
		log.debugv("saveChanges", "source={0}", source);
		Response response;
		Session session = sessionFactory.openSession();
		try {
			ContextProvider context = new HibernateContext(session, metadata);
			SaveResult sr = context.saveChanges(source);
			
			String json = Json.toJson(sr);
			log.debugv("saveChanges: SaveResult={0}", json);
			if (sr.hasErrors()) {
				response = Response.status(Response.Status.FORBIDDEN).entity(json).build(); 
			} else {
				response = Response.ok(json).build();
			}
		}
    	catch (Exception e) {
    		log.errorv(e, "saveChanges: source={0}", source);
    		String json = Json.toJson(e);
			response = Response.serverError().entity(json).build(); 
    	}
    	finally {
    		session.close();
    	}    	
		return response;
	}
	
}
