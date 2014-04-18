package com.breezejs.hib;

import com.breezejs.OdataParameters;

import junit.framework.TestCase;

public class QueryServiceTest extends TestCase {

	protected void setUp() throws Exception {
		super.setUp();
		// then populate the database with test data...?
	}

	public void testQueryToJsonClassString() {
//    	QueryService qs = new QueryService(StaticConfigurator.getSessionFactory());
//    	String json = qs.queryToJson(Customer.class, "?$top=5&$filter=Country eq 'Brazil'");
//    	assertTrue(json.indexOf("Customer") > 0);
//    	assertTrue(json.indexOf("Brazil") > 0);
//		fail("Not yet implemented");
	}

	public void testQueryToJsonClassOdataParameters() {
//    	QueryService qs = new QueryService(StaticConfigurator.getSessionFactory());
//    	OdataParameters op = OdataParameters.parse("$top=3&$select=Country,PostalCode&$inlinecount=allpages");
//    	String json = qs.queryToJson(Customer.class, op);
//		fail("Not yet implemented");
	}

	public void testQueryToJsonCriteriaBooleanStringArray() {
//		fail("Not yet implemented");
	}

	public void testQueryToJsonString() {
//    	QueryService qs = new QueryService(StaticConfigurator.getSessionFactory());
//		String hqlQuery = "from Order where orderId in (10248, 10249, 10250)";
//		String json = qs.queryToJson(hqlQuery);
//    	assertTrue(json.indexOf("Order") > 0);
//    	assertTrue(json.indexOf("10248") > 0);
	}

//	qs.queryToJson(northwind.model.Customer.class, "?$top=5&$filter=country eq 'Brazil'&$expand=orders/orderDetails/product");
//	qs.queryToJson(northwind.model.Order.class, "?$filter=orderID eq 10258");
//	qs.queryToJson(northwind.model.Order.class, "?$filter=orderID eq 10258&$expand=orderDetails/product/supplier");
	
}
