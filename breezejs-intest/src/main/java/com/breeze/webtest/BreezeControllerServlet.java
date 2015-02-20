package com.breeze.webtest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.SessionFactory;

import com.breezejs.hib.QueryService;
import com.breezejs.hib.SaveService;
import com.breezejs.metadata.Metadata;
import com.breezejs.query.QueryResult;
import com.breezejs.save.SaveResult;
import com.breezejs.util.JsonGson;

public class BreezeControllerServlet extends ControllerServlet {
	private static final long serialVersionUID = 1L;
	
	protected QueryService queryService;
	protected SaveService saveService;
	protected Metadata metadata;
	protected static String metadataJson; 
	
	/** Create instance using the injected ServletContext */
	@Override
	public void init() {
		ServletContext ctx = getServletContext();
		init((SessionFactory) ctx.getAttribute(AppContextListener.SESSIONFACTORY),
				(Metadata) ctx.getAttribute(AppContextListener.METADATA));
	}

	/** Create instance using provided sessionFactory and metadata.  */
	private void init(SessionFactory sessionFactory, Metadata metadata) {
		System.out.println("BreezeTests: sessionFactory=" + sessionFactory + ", metadata=" + metadata);
    	this.queryService = new QueryService(sessionFactory);
    	this.saveService = new SaveService(sessionFactory, metadata);
    	this.metadata = metadata;
	}

	@Override
	protected void handleRequest(HttpServletRequest request, HttpServletResponse response) {
		String resourceName = super.getMethodName(request);
		String json = request.getQueryString();
		executeQuery(resourceName, json, response);
	}
	
	public String getMetadata() {
		if (metadataJson == null) {
			metadataJson = JsonGson.toJson(this.metadata, false);
		}
		return metadataJson;
	}
	
	public void saveChanges(HttpServletRequest request, HttpServletResponse response) {
		String saveBundle = readPostData(request);
		SaveResult result = saveService.saveChanges(saveBundle);
		String json = JsonGson.toJson(result);
		if (result.hasErrors()) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
		writeResponse(response, json);
	}
	
	protected void executeQuery(String resourceName, String json, HttpServletResponse response) {
		QueryResult result = this.queryService.executeQuery(resourceName, json);
		writeResponse(response, result.toJson());
	}
}
