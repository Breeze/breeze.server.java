package com.breeze.hib;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.jboss.logging.Logger;

import com.breeze.metadata.Metadata;
import com.breeze.save.ContextProvider;
import com.breeze.save.SaveResult;
import com.breeze.save.SaveWorkState;

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
	public SaveResult saveChanges(SaveWorkState saveWorkState) {
		// log.debugv("saveChanges", "source={0}", source);
		Session session = sessionFactory.openSession();
		try {
			ContextProvider context = new HibernateContext(session, metadata);
			SaveResult sr = context.saveChanges(saveWorkState);
			
			return sr;
		}
    	catch (Exception e) {
    		// log.errorv(e, "saveChanges: source={0}", source);
    		if (e instanceof RuntimeException) throw e;
    		throw new RuntimeException(e);
    	}
    	finally {
    		session.close();
    	}    	
	}
	
}
