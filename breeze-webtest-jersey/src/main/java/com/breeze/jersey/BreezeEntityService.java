package com.breeze.jersey;

import java.util.Map;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import northwind.model.Customer;

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
import com.breeze.webserver.AppContextListener;

public class BreezeEntityService {

    protected SessionFactory sessionFactory;
    protected Metadata metadata;
    private static String metadataJson;

    /** Create instance using the injected ServletContext */
    public BreezeEntityService(@Context ServletContext ctx) {
        this((SessionFactory) ctx.getAttribute(AppContextListener.SESSIONFACTORY),
                (Metadata) ctx.getAttribute(AppContextListener.METADATA));
    }

    /**
     * Create instance using provided sessionFactory and metadata. 
     */
    protected BreezeEntityService(SessionFactory sessionFactory, Metadata metadata) {
        System.out.println("BreezeEntityService: sessionFactory=" + sessionFactory + ", metadata=" + metadata);
        this.sessionFactory = sessionFactory;
        this.metadata = metadata;
    }

    @GET
    @Path("Metadata")
    public String getMetadata() {
        if (metadataJson == null) {
            metadataJson = JsonGson.toJson(this.metadata);
        }
        return metadataJson;
    }

    @POST
    @Path("SaveChanges")
    public Response saveChanges(String saveBundleJson) {
        Map saveBundle = JsonGson.fromJson(saveBundleJson);
        SaveWorkState sws = new SaveWorkState(saveBundle);
        SaveProcessor processor = new HibernateSaveProcessor(this.metadata, this.sessionFactory);
        SaveResult sr = processor.saveChanges(sws);

        String json = JsonGson.toJson(sr, true, true);
        Response response;
        if (sr.hasErrors()) {
            response = Response.status(Response.Status.FORBIDDEN).entity(json).build();
        } else {
            response = Response.ok(json).build();
        }

        return response;
    }

    protected String executeQuery(String resourceName, EntityQuery entityQuery) {
        QueryProcessor qp = new HibernateQueryProcessor(this.metadata, this.sessionFactory);
        QueryResult qr = qp.executeQuery(resourceName, entityQuery);
        return qr.toJson();
    }

    protected String executeQuery(Class clazz, EntityQuery entityQuery) {
        QueryProcessor qp = new HibernateQueryProcessor(this.metadata, this.sessionFactory);
        QueryResult qr = qp.executeQuery(clazz, entityQuery);
        return qr.toJson();
    }

    /**
     * Return the EntityQuery from the query string. Extracts the first thing that looks like JSON,
     * and passes that to the EntityQuery constructor.
     * 
     * @param uriInfo
     * @return
     */
    protected EntityQuery extractEntityQuery(UriInfo uriInfo) {
        MultivaluedMap<String, String> map = uriInfo.getQueryParameters();
        for (String key : map.keySet()) {
            String value = map.getFirst(key);
            if (key.startsWith("{") && (value == null || value.isEmpty())) {
                return new EntityQuery(key);
            }
            else if (value.startsWith("{")) {
                return new EntityQuery(value);
            }
        }
        return new EntityQuery();
    }

    /**
     * Handles standard entity queries
     * 
     * @param resourceName - Name of resources being queried, e.g. "Customers"
     * @param uriInfo - Gets the query string containing the EntityQuery JSON
     * @return JSON result
     */
    @GET
    @Path("{resourcename}")
    public String doQuery(@PathParam("resourcename") String resourceName, @Context UriInfo uriInfo) {
        EntityQuery entityQuery = extractEntityQuery(uriInfo);
        return executeQuery(resourceName, entityQuery);
    }

    @GET
    @Path("CustomersInBrazil")
    public String customersInBrazil(@Context UriInfo uriInfo) {
        EntityQuery entityQuery = extractEntityQuery(uriInfo);
        entityQuery = entityQuery.where("{ country: 'Brazil' }").take(5);
        return executeQuery(Customer.class, entityQuery);
    }
    
}
