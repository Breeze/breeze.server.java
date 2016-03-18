package com.breeze.test;

import java.util.Collection;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import northwind.jpamodel.*;

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
    Gson _gson;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // then populate the database with test data...?
        Metadata metadata = new JPAMetadata(_emf);
        metadata.build();
        _qe = new JPAQueryProcessor(metadata, _emf);
        
        GsonBuilder gsonBuilder = JsonGson.newGsonBuilder(false, false)
                .registerTypeAdapterFactory(new JPATypeAdapter.Factory(_emf.getPersistenceUnitUtil()))
                .registerTypeAdapterFactory(new BreezeTypeAdapterFactory());
        _gson = gsonBuilder.create();
    }
    
    private Collection runQuery(String resourceName, String query)
    {
        QueryResult qr = _qe.executeQuery(resourceName, query);
        Collection results = qr.getResults();
        String rJson = _gson.toJson(qr.getResults());
        System.out.println(rJson);
        return results;
    }
    
    public void testEmptyQuery() {
        String json = "";
        EntityQuery eq = new EntityQuery(json);
        QueryResult qr = _qe.executeQuery(Employee.class, eq);
        Collection results = qr.getResults();
        //String rJson = qr.toJson();
        String rJson = _gson.toJson(qr.getResults());
        System.out.println(rJson);
        assertTrue("results.size() > 5", results.size() > 5);
//        for (Object o : results) {
//            Employee c = (Employee) o;
//        }
    }

    public void testIsNull() {
        Collection results = runQuery("Customers", "{ where: { region: null }}");
        for (Object o : results) {
            assertNull(((Customer) o).getRegion());
        }
    }

    public void testNotNull() {
        Collection results = runQuery("Customers", "{ where: { region: { 'ne' : null }}}");
        for (Object o : results) {
            assertNotNull(((Customer) o).getRegion());
        }
    }
    
    public void testSimpleWhere() {
        Collection results = runQuery("Customers", "{ where: { country: 'Brazil' }}");
        assertTrue("results.size() > 5", results.size() > 5);
        for (Object o : results) {
            assertEquals("Brazil", ((Customer) o).getCountry());
        }
    }

    public void testSimpleWhereTake() {
        Collection results = runQuery("Customers", "{ where: { country: 'Brazil' }, take: 5 }");
        assertEquals(5, results.size());
        for (Object o : results) {
            assertEquals("Brazil", ((Customer) o).getCountry());
        }
    }

    public void testSkipTake() {
        Collection results = runQuery("Customers", "{ skip: 2, take: 3 }");
        assertEquals(3, results.size());
        for (Object o : results) {
            assertNotNull(((Customer) o).getCompanyName());
        }
    }

    public void testNotEquals() {
        Collection results = runQuery("Customers", "{ where: { country: { 'ne': 'Brazil' }}}");
        assertTrue("results.size() > 5", results.size() > 5);
        for (Object o : results) {
            assertFalse("Brazil", "Brazil".equals(((Customer) o).getCountry()));
        }
    }

    public void testGreaterThan() {
        Collection results = runQuery("Products", "{ where: { unitsInStock: { 'gt': 20 }}}");
        assertTrue("results.size() > 5", results.size() > 5);
        for (Object o : results) {
            assertTrue("c.getUnitsInStock() > 20", ((Product) o).getUnitsInStock() > 20);
        }
    }

    public void testGreaterThanOrEqual() {
        Collection results = runQuery("Products", "{ where: { unitsInStock: { 'ge': 20 }}}");
        assertTrue("results.size() > 5", results.size() > 5);
        for (Object o : results) {
            assertTrue("c.getUnitsInStock() >= 20", ((Product) o).getUnitsInStock() >= 20);
        }
    }
    
    public void testLessThan() {
        Collection results = runQuery("Products", "{ where: { unitsInStock: { 'lt': 20 }}}");
        assertTrue("results.size() > 5", results.size() > 5);
        for (Object o : results) {
            assertTrue("c.getUnitsInStock() < 20", ((Product) o).getUnitsInStock() < 20);
        }
    }

    public void testLessThanOrEqual() {
        Collection results = runQuery("Products", "{ where: { unitsInStock: { 'le': 20 }}}");
        assertTrue("results.size() > 5", results.size() > 5);
        for (Object o : results) {
            assertTrue("c.getUnitsInStock() <= 20", ((Product) o).getUnitsInStock() <= 20);
        }
    }

    public void testInNumber() {
        Collection results = runQuery("Products", "{ where: { unitsInStock: { 'in': [39, 17, 13] }}}");
        assertEquals(7, results.size());
        for (Object o : results) {
            int unitsInStock = ((Product) o).getUnitsInStock();
            assertTrue("unitsInStock in [39, 17, 13]", unitsInStock == 39 || unitsInStock == 17 || unitsInStock == 13);
        }
    }

    public void testInString() {
        Collection results = runQuery("Products", "{ where: { productName: { 'in': ['Ikura','Konbu','Tofu'] }}}");
        assertEquals(3, results.size());
        for (Object o : results) {
            String productName = ((Product) o).getProductName();
            assertTrue("productName", "['Ikura','Konbu','Tofu']".indexOf(productName) > 0);
        }
    }

    public void testStartsWith() {
        Collection results = runQuery("Customers", "{ where: { companyName: { 'startsWith': 'Fr' }}}");
        assertEquals(3, results.size());
        for (Object o : results) {
            String companyName = ((Customer) o).getCompanyName();
            assertTrue("companyName", companyName.startsWith("Fr"));
        }
    }

    public void testEndsWith() {
        Collection results = runQuery("Customers", "{ where: { companyName: { 'endsWith': 'os' }}}");
        assertEquals(5, results.size());
        for (Object o : results) {
            String companyName = ((Customer) o).getCompanyName();
            assertTrue("companyName", companyName.endsWith("os"));
        }
    }

    public void testContains() {
        Collection results = runQuery("Customers", "{ where: { companyName: { 'contains': 'rest' }}}");
        assertEquals(4, results.size());
        for (Object o : results) {
            String companyName = ((Customer) o).getCompanyName().toLowerCase();
            assertTrue("companyName", companyName.contains("rest"));
        }
    }
    
}
