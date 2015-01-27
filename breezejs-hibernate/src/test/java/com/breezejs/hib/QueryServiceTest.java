package com.breezejs.hib;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import northwind.model.Customer;
import northwind.model.Order;
import northwind.model.Product;
import northwind.model.Supplier;

import com.breezejs.OdataParameters;

import junit.framework.TestCase;

public class QueryServiceTest extends TestCase {

	private QueryService qs;
	
	protected void setUp() throws Exception {
		super.setUp();
		// then populate the database with test data...?
		qs = new QueryService(StaticConfigurator.getSessionFactory());
	}
	
	public boolean hasValue(String json, String propertyName, String value) {
		String regex = propertyName + "[^\\w]{0,3}:[^\\w]{0,3}" + value;
		Matcher m = Pattern.compile(regex, Pattern.CANON_EQ).matcher(json);
		return m.find();
	}

	public void testQueryFilterCustomer() {
    	String json = qs.queryToJson(Customer.class, "?$top=5&$filter=country eq 'Brazil'");
    	assertTrue(json.indexOf("Customer") > 0);
    	assertTrue(json.indexOf("companyName") > 0);
    	assertTrue(json.indexOf("Comércio Mineiro") > 0);
    	assertTrue(json.indexOf("Hanari Carnes") > 0);
    	assertTrue(json.indexOf("Queen Cozinha") > 0);
    	assertTrue(json.indexOf("Que Delícia") > 0);
    	assertTrue(json.indexOf("Ricardo Adocicados") > 0);
    	assertTrue(json.indexOf("Brazil") > 0);
    	assertTrue(json.indexOf("France") < 0);
	}
	
	public void testQueryOrderByNestedProp1() {
    	String json = qs.queryToJson(Order.class, "?$filter=orderID lt 10258&$orderby=employee/lastName desc&$expand=employee");
		// String json = qs.queryToJson(Product.class, "?$orderby=Category/CategoryName desc,ProductName");
    	assertTrue(json.indexOf("Order") > 0);
    	assertTrue(json.indexOf("lastName") > 0);
	}
	
//	public void testQueryOrderByNestedProp2() {
//    	String json = qs.queryToJson(OrderDetail.class, "?$filter=orderID lt 10258&$orderby=order/employee/lastName desc&$expand=order/employee");
//		// String json = qs.queryToJson(Product.class, "?$orderby=Category/CategoryName desc,ProductName");
//    	assertTrue(json.indexOf("OrderDetail") > 0);
//    	assertTrue(json.indexOf("lastName") > 0);
//	}

//	public void testQueryFilterOrder() {
//    	String json = qs.queryToJson(Order.class, "?$top=5&$filter=shipCountry eq 'Brazil'");
//    	assertTrue(json.indexOf("Customer") > 0);
//    	assertTrue(json.indexOf("companyName") > 0);
//    	assertTrue(json.indexOf("Comércio Mineiro") > 0);
//    	assertTrue(json.indexOf("Hanari Carnes") > 0);
//    	assertTrue(json.indexOf("Queen Cozinha") > 0);
//    	assertTrue(json.indexOf("Que Delícia") > 0);
//    	assertTrue(json.indexOf("Ricardo Adocicados") > 0);
//    	assertTrue(json.indexOf("Brazil") > 0);
//    	assertTrue(json.indexOf("France") < 0);
//	}
	
	public void testQueryExpandProduct() {
    	String json = qs.queryToJson(Customer.class, "?$top=1&$filter=country eq 'Brazil'&$expand=orders/orderDetails/product");
    	assertTrue(hasValue(json, "companyName", "Comércio Mineiro"));
    	assertTrue(hasValue(json, "orderID", "11042"));
    	assertTrue(hasValue(json, "freight", "79.7"));
    	assertTrue(hasValue(json, "productID", "61"));
    	assertTrue(hasValue(json, "productName", "Gula Malacca"));
    	assertTrue(hasValue(json, "unitPrice", "28.5"));
    	assertTrue(json.indexOf("Customer") > 0);
    	assertTrue(json.indexOf("OrderDetail") > 0);
    	assertTrue(json.indexOf("Product") > 0);
    	assertTrue(json.indexOf("Brazil") > 0);
    	assertTrue(json.indexOf("France") < 0);
	}

	public void testQueryInlineCount() {
    	String json = qs.queryToJson(Customer.class, "$top=3&$inlinecount=allpages");
    	assertTrue(json.indexOf("Customer") > 0);
    	assertTrue(hasValue(json, "companyName", "Island Trading"));
    	assertTrue(hasValue(json, "InlineCount", "96"));
    	assertTrue(hasValue(json, "Results", "\\["));
	}
	
	
//	public void testQuerySelectCountryAndPostalCode() {
//    	String json = qs.queryToJson(Customer.class, "$top=3&$select=country,postalCode&$inlinecount=allpages");
//    	assertTrue(json.indexOf("Customer") < 0);
//    	assertTrue(json.indexOf("Country") > 0);
//    	assertTrue(json.indexOf("PostalCode") > 0);
//	}
	
	public void testQueryFilterOrderIdAndExpand() {
    	String json = qs.queryToJson(Order.class, "?$filter=orderID eq 10258&$expand=orderDetails/product/supplier");
    	System.out.println(json);
    	assertTrue(json.indexOf("Order") > 0);
    	assertTrue(hasValue(json, "orderID", "10258"));
    	assertTrue(hasValue(json, "productID", "32"));
    	assertTrue(hasValue(json, "supplierID", "14"));
    	assertTrue(hasValue(json, "contactName", "Elio Rossi"));
    	assertTrue(hasValue(json, "city", "Ravenna"));
	}

	public void testQueryToJsonCriteriaBooleanStringArray() {
//		fail("Not yet implemented");
	}

	public void testQueryHQL() {
    	QueryService qs = new QueryService(StaticConfigurator.getSessionFactory());
		String hqlQuery = "from Order where orderId in (10248, 10249, 10250)";
		String json = qs.queryToJson(hqlQuery);
    	assertTrue(json.indexOf("Order") > 0);
    	assertTrue(hasValue(json, "orderID", "10248"));
	}

	public void testEqual() {
    	String json = qs.queryToJson(Supplier.class, "?$filter=location/city eq 'New Orleans'");
    	assertTrue(json.indexOf("Supplier") > 0);
    	assertTrue(hasValue(json, "city", "New Orleans"));
	}
	public void testNotEqual() {
    	String json = qs.queryToJson(Supplier.class, "?$filter=location/city ne 'Tokyo'");
    	assertTrue(json.indexOf("Supplier") > 0);
    	assertTrue(hasValue(json, "city", "New Orleans"));
	}
//	public void testGreaterThan() {
//    	String json = qs.queryToJson(Product.class, "?$filter=unitPrice gt 20.0");
//    	assertTrue(json.indexOf("Product") > 0);
//	}
//	public void testGreaterThanOrEqual() {
//    	String json = qs.queryToJson(Product.class, "?$filter=unitPrice ge 10");
//    	assertTrue(json.indexOf("Product") > 0);
//	}
//	public void testLessThan() {
//    	String json = qs.queryToJson(Product.class, "?$filter=unitPrice lt 20");
//    	assertTrue(json.indexOf("Product") > 0);
//	}
//	public void testLessThanOrEqual() {
//    	String json = qs.queryToJson(Product.class, "?$filter=unitPrice le 100");
//    	assertTrue(json.indexOf("Product") > 0);
//	}
//	public void testLogicalAnd() {
//    	String json = qs.queryToJson(Product.class, "?$filter=unitPrice le 200 and unitPrice gt '3.5'");
//    	assertTrue(json.indexOf("Product") > 0);
//	}
//	public void testLogicalOr() {
//    	String json = qs.queryToJson(Product.class, "?$filter=unitPrice le '3.5' or unitPrice gt '200'");
//    	assertTrue(json.indexOf("Product") > 0);
//	}
//	public void testLogicalNot() {
//    	String json = qs.queryToJson(Product.class, "?$filter=not endswith(productName,'milk')");
//    	assertTrue(json.indexOf("Product") > 0);
//	}
	
//	qs.queryToJson(northwind.model.Customer.class, "?$top=5&$filter=country eq 'Brazil'&$expand=orders/orderDetails/product");
//	qs.queryToJson(northwind.model.Order.class, "?$filter=orderID eq 10258");
//	qs.queryToJson(northwind.model.Order.class, "?$filter=orderID eq 10258&$expand=orderDetails/product/supplier");
	
}
