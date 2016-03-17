package com.breeze.test;

import java.util.Collection;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import northwind.jpamodel.Customer;
import northwind.jpamodel.Employee;

import com.breeze.jpa.JPAMetadata;
import com.breeze.jpa.JPAQueryProcessor;
import com.breeze.metadata.Metadata;
import com.breeze.query.EntityQuery;
import com.breeze.query.QueryResult;
import com.breeze.util.BreezeTypeAdapterFactory;
import com.breeze.jpa.JPATypeAdapter;
import com.breeze.util.JsonGson;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import junit.framework.TestCase;

public class JPAQueryProcessorTest extends TestCase {

    static EntityManagerFactory _emf = Persistence.createEntityManagerFactory("northwind");
    JPAQueryProcessor _qe;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // then populate the database with test data...?
        Metadata metadata = new JPAMetadata(_emf);
        metadata.build();
        _qe = new JPAQueryProcessor(metadata, _emf);
    }
    
    public void testEmptyQuery() {
        String json = "";
        EntityQuery eq = new EntityQuery(json);
        QueryResult qr = _qe.executeQuery(Employee.class, eq);
        Collection results = qr.getResults();
        //String rJson = qr.toJson();
        GsonBuilder gsonBuilder = JsonGson.newGsonBuilder(false, false)
                .registerTypeAdapterFactory(new JPATypeAdapter.Factory(_emf.getPersistenceUnitUtil()))
                .registerTypeAdapterFactory(new BreezeTypeAdapterFactory());
        Gson gson = gsonBuilder.create();
        String rJson = gson.toJson(qr.getResults());
        System.out.println(rJson);
        assertTrue(results.size() > 5);
        for (Object o : results) {
            Employee c = (Employee) o;
        }
    }

//    public void testNullQuery() {
//        String json = null;
//        QueryResult qr = _qe.executeQuery(Customer.class, json);
//        Collection results = qr.getResults();
//        String rJson = qr.toJson();
//        assertTrue(results.size() > 5);
//        for (Object o : results) {
//            Customer c = (Customer) o;
//        }
//    }

}
