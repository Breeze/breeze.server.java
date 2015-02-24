package com.breeze.webtest;

import java.lang.reflect.Method;
import java.net.URLDecoder;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.SessionFactory;

import com.breeze.webtest.ControllerServlet.ControllerException;
import com.breezejs.hib.QueryService;
import com.breezejs.hib.SaveService;
import com.breezejs.metadata.Metadata;
import com.breezejs.query.QueryResult;
import com.breezejs.save.SaveResult;
import com.breezejs.util.JsonGson;

public class BreezeControllerServlet extends ControllerServlet {
	private static final long serialVersionUID = 1L;
	
	protected QueryService _queryService;
	protected SaveService _saveService;
	protected Metadata _metadata;
	protected String _metadataJson; 
	
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
    	this._queryService = new QueryService(sessionFactory);
    	this._saveService = new SaveService(sessionFactory, metadata);
    	this._metadata = metadata;
    	this._metadataJson = JsonGson.toJson(this._metadata, false);
	}

	@Override
	protected void handleRequest(HttpServletRequest request, HttpServletResponse response) {
		try {
				
			String methodName = getMethodName(request);
  
			if (methodName.equals("Metadata")) {
				writeResponse(response, getMetadata());
				return;
			}
			if (methodName.equals("SaveChanges")) {
				saveChanges(request, response);
				return;
			}
			Method method = getMethod(this, methodName);
			if (method != null) {
				dispatch(this, method, request, response);
			} else {
				String pathInfo = request.getPathInfo();
				String resourceName = pathInfo.substring(1);
				String qs = request.getQueryString();
			
				String json = qs != null ? URLDecoder.decode(qs) : null;
				// HACK
				if (json.endsWith("&")) {
					json = json.substring(0, json.length()-1);
				}
				executeQuery(resourceName, json, response);				
			}
				
		} catch (ControllerException ex) {
			writeError(response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
		} catch (Throwable ex) {
			// sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
			
			writeError(response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
		}
	}
	
	public String getMetadata() {
		return _metadataJson;
	}
	
	public void saveChanges(HttpServletRequest request, HttpServletResponse response) {
		String saveBundle = readPostData(request);
		SaveResult result = _saveService.saveChanges(saveBundle);
		String json = JsonGson.toJson(result);
		if (result.hasErrors()) {
			response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		}
		writeResponse(response, json);
	}
	
	
	protected void executeQuery(String resourceName, String json, HttpServletResponse response) {
		QueryResult result = this._queryService.executeQuery(resourceName, json);
		writeResponse(response, result.toJson());
	}
	
	protected void executeQuery(Class clazz, String json, HttpServletResponse response) {
		QueryResult result = this._queryService.executeQuery(clazz, json);
		writeResponse(response, result.toJson());
	}
}
