package com.breeze.webserver;

import java.lang.reflect.Method;
import java.net.URLDecoder;
import java.util.Map;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.hibernate.SessionFactory;

import com.breeze.hib.HibernateSaveProcessor;
import com.breeze.hib.HibernateQueryProcessor;
import com.breeze.metadata.Metadata;
import com.breeze.query.EntityQuery;
import com.breeze.query.QueryProcessor;
import com.breeze.query.QueryResult;
import com.breeze.save.SaveProcessor;
import com.breeze.save.SaveResult;
import com.breeze.save.SaveWorkState;
import com.breeze.util.JsonGson;

public class BreezeControllerServlet extends ControllerServlet {
    private static final long serialVersionUID = 1L;

    protected HibernateQueryProcessor _queryService;
    protected SessionFactory _sessionFactory;
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
        _sessionFactory = sessionFactory;
        _metadata = metadata;
        _metadataJson = metadata.toJson();
        _sessionFactory = sessionFactory;
        
        
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
            if (method == null) {
                executeQuery(request, response);
            } else {
                dispatch(this, method, request, response);
            } 

        } catch (Throwable ex) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    ex.getMessage(), getStackTrace(ex));
        }
    }
    
    protected String getStackTrace(Throwable e) {
        java.io.StringWriter sw = new java.io.StringWriter();
        e.printStackTrace(new java.io.PrintWriter(sw));
        return sw.toString();
    }

    protected String getMetadata() {
        return _metadataJson;
    }

    protected void executeQuery(HttpServletRequest request,
            HttpServletResponse response) {
        String pathInfo = request.getPathInfo();
        String resourceName = pathInfo.substring(1);
        EntityQuery entityQuery = extractEntityQuery(request);
        QueryResult qr = executeQuery(resourceName, entityQuery);
        writeResponse(response, qr.toJson());
    }

    protected QueryResult executeQuery(String resourceName, String json) {
        EntityQuery eq = new EntityQuery(json);
        return createQueryProcessor().executeQuery(resourceName, eq);
    }

    protected QueryResult executeQuery(Class clazz, String json,
            HttpServletResponse response) {
        EntityQuery eq = new EntityQuery(json);
        return createQueryProcessor().executeQuery(clazz, eq);
    }

    protected QueryResult executeQuery(String resourceName,
            EntityQuery entityQuery) {
        return createQueryProcessor().executeQuery(resourceName, entityQuery);
    }

    protected QueryResult executeQuery(Class clazz, EntityQuery entityQuery) {
        return createQueryProcessor().executeQuery(clazz, entityQuery);
    }
    
    protected QueryProcessor createQueryProcessor() {
        return new HibernateQueryProcessor(_metadata, _sessionFactory);
    }

    protected void saveChanges(HttpServletRequest request,
            HttpServletResponse response) {
        Map saveBundle = extractSaveBundle(request);
        SaveWorkState sws = createSaveWorkState(saveBundle);
        SaveResult sr = saveChanges(sws);
        writeSaveResponse(response, sr);
    }
    
    // override this to provide default Before/After save Entities logic
    protected SaveWorkState createSaveWorkState(Map saveBundle) {
        return new SaveWorkState(saveBundle);
    }

    protected SaveResult saveChanges(SaveWorkState saveWorkState) {
        SaveProcessor processor = new HibernateSaveProcessor(_metadata, _sessionFactory);
        return processor.saveChanges(saveWorkState);
    }

    protected EntityQuery extractEntityQuery(HttpServletRequest request) {
        String qs = request.getQueryString();
        try {
            qs = (qs != null) ? URLDecoder.decode(qs, "UTF-8") : null;
        } catch (Exception e) {
            throw new RuntimeException("Unable to decode: " + qs, e);
        }
        Map<String, String[]> map = request.getParameterMap();
        String json = null;
        for (Map.Entry<String, String[]> entry : map.entrySet()) {
            String parameterName = entry.getKey();
            // String[] value = entry.getValue();

            if (qs.indexOf("&" + parameterName) == -1) {
                json = parameterName;
                // break;
            }
        }

        // // Alternate version - not as safe because queryString might have an
        // '&'
        // String json = (qs != null) ? URLDecoder.decode(qs) : null;
        // // Isolate other parameters from the query
        // // all other parameters will have the syntax
        // // HACK
        // if (json != null && json.indexOf("&") >= 0) {
        // json = json.substring(0, json.indexOf("&"));
        // }

        return new EntityQuery(json);

    }

    protected Map extractSaveBundle(HttpServletRequest request) {
        String saveBundleString = readPostData(request);

        Map saveBundle = JsonGson.fromJson(saveBundleString);
        return saveBundle;
    }
    
    protected void writeQueryResponse(HttpServletResponse response,
            QueryResult queryResult) {
        writeResponse(response, queryResult.toJson());
    }

    protected void writeSaveResponse(HttpServletResponse response,
            SaveResult saveResult) {
        String json = saveResult.toJson();
        if (saveResult.hasErrors()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        }
        writeResponse(response, json);
    }

}
