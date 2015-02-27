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
import com.breezejs.query.EntityQuery;
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
        init((SessionFactory) ctx
                .getAttribute(AppContextListener.SESSIONFACTORY),
                (Metadata) ctx.getAttribute(AppContextListener.METADATA));
    }

    /** Create instance using provided sessionFactory and metadata. */
    private void init(SessionFactory sessionFactory, Metadata metadata) {
        System.out.println("BreezeTests: sessionFactory=" + sessionFactory
                + ", metadata=" + metadata);
        this._queryService = new QueryService(sessionFactory);
        this._saveService = new SaveService(sessionFactory, metadata);
        this._metadata = metadata;
        this._metadataJson = JsonGson.toJson(this._metadata, false);
    }

    @Override
    protected void handleRequest(HttpServletRequest request,
            HttpServletResponse response) {
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
                EntityQuery entityQuery = extractEntityQuery(request);
                executeQuery(resourceName, entityQuery, response);
            }

        } catch (ControllerException ex) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    ex.getMessage());
        } catch (Throwable ex) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    ex.getMessage());
        }
    }

    public void saveChanges(HttpServletRequest request,
            HttpServletResponse response) {
        String saveBundle = readPostData(request);
        SaveResult result = _saveService.saveChanges(saveBundle);
        String json = JsonGson.toJson(result);
        if (result.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        writeResponse(response, json);
    }

    protected String extractEntityQueryJson(HttpServletRequest request) {
        String qs = request.getQueryString();

        String json = (qs != null) ? URLDecoder.decode(qs) : null;
        // HACK
        if (json != null && json.indexOf("&") >= 0) {
            json = json.substring(0, json.indexOf("&"));
        }
        return json;
    }
    
    protected EntityQuery extractEntityQuery(HttpServletRequest request) {
        String json = extractEntityQueryJson(request);
        return new EntityQuery(json);
    }

    public String getMetadata() {
        return _metadataJson;
    }

    protected void executeQuery(String resourceName, String json,
            HttpServletResponse response) {
        EntityQuery eq = new EntityQuery(json);
        QueryResult result = this._queryService
                .executeQuery(resourceName, eq);
        writeResponse(response, result.toJson());
    }

    protected void executeQuery(Class clazz, String json,
            HttpServletResponse response) {
        EntityQuery eq = new EntityQuery(json);
        QueryResult result = this._queryService.executeQuery(clazz, eq);
        writeResponse(response, result.toJson());
    }
    
    protected void executeQuery(String resourceName, EntityQuery entityQuery,
            HttpServletResponse response) {
        QueryResult result = this._queryService
                .executeQuery(resourceName, entityQuery);
        writeResponse(response, result.toJson());
    }

    protected void executeQuery(Class clazz, EntityQuery entityQuery,          
            HttpServletResponse response) {
        QueryResult result = this._queryService
                .executeQuery(clazz, entityQuery);
        writeResponse(response, result.toJson());
    }
}
