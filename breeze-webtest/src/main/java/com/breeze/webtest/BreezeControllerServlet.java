package com.breeze.webtest;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.SessionFactory;

import com.breeze.hib.QueryService;
import com.breeze.hib.SaveService;
import com.breeze.metadata.Metadata;
import com.breeze.query.EntityQuery;
import com.breeze.query.QueryResult;
import com.breeze.save.SaveResult;
import com.breeze.util.JsonGson;

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
                QueryResult qr = executeQuery(resourceName, entityQuery);
                writeResponse(response, qr.toJson());
            }

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

    public String getMetadata() {
        return _metadataJson;
    }

    protected EntityQuery extractEntityQuery(HttpServletRequest request) {
        String json = extractEntityQueryJson(request);
        return new EntityQuery(json);
    }

    protected String extractEntityQueryJson(HttpServletRequest request) {
        String qs = request.getQueryString();
        qs = (qs != null) ? URLDecoder.decode(qs) : null;
        Map<String, String[]> map = request.getParameterMap();
        String json = null;
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String parameterName = entry.getKey();
            String[] value = entry.getValue();
            
            if (qs.indexOf("&" + parameterName) == -1) {
                json = parameterName;
                // break;
            }
        }
        
//        // Alternate version - not as safe because queryString might have an '&'        
//        String json = (qs != null) ? URLDecoder.decode(qs) : null;
//        // Isolate other parameters from the query
//        // all other parameters will have the syntax 
//        // HACK
//        if (json != null && json.indexOf("&") >= 0) {
//            json = json.substring(0, json.indexOf("&"));
//        }
        
        return json;
    }
    
    

    protected void executeQuery(String resourceName, String json,
            HttpServletResponse response) {
        EntityQuery eq = new EntityQuery(json);
        QueryResult result = this._queryService.executeQuery(resourceName, eq);
        writeResponse(response, result.toJson());
    }

    protected void executeQuery(Class clazz, String json,
            HttpServletResponse response) {
        EntityQuery eq = new EntityQuery(json);
        QueryResult result = this._queryService.executeQuery(clazz, eq);
        writeResponse(response, result.toJson());
    }

    protected QueryResult executeQuery(String resourceName,
            EntityQuery entityQuery) {
        return this._queryService.executeQuery(resourceName, entityQuery);
    }

    protected QueryResult executeQuery(Class clazz, EntityQuery entityQuery) {
        return this._queryService.executeQuery(clazz, entityQuery);
    }

}
