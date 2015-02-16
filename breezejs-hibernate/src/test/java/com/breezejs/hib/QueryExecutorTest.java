package com.breezejs.hib;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import northwind.model.Customer;
import northwind.model.Employee;
import northwind.model.Location;
import northwind.model.Order;
import northwind.model.OrderDetail;
import northwind.model.Product;
import northwind.model.Supplier;

import com.breezejs.OdataParameters;
import com.breezejs.QueryResult;
import com.breezejs.query.EntityQuery;
import com.breezejs.util.JsonGson;

import junit.framework.TestCase;

public class QueryExecutorTest extends TestCase {

	private QueryExecutor _qe;

	protected void setUp() throws Exception {
		super.setUp();
		// then populate the database with test data...?
		_qe = new QueryExecutor(StaticConfigurator.getSessionFactory());
	}

	// TODO Need to add select support

	public void testSimple() {
		// String json = qs.queryToJson(Customer.class,
		// "?$top=5&$filter=country eq 'Brazil'");
		String json = "{ where: { country: 'Brazil' }, take: 5 }";
		QueryResult qr = _qe.executeQuery("Customers", json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() == 5);
		for (Object o : results) {
			Customer c = (Customer) o;
			assertTrue(c.getCountry().equals("Brazil"));
			assertTrue(c.getCompanyName() != null);
		}

	}

	public void testUsingResourceName() {
		// String json = qs.queryToJson(Order.class,
		// "?$top=5&$filter=shipCountry eq 'Brazil'");
		String json = "{ resourceName: 'Orders', take: 5, where: { shipCountry: 'Brazil' }}";
		QueryResult qr = _qe.executeQuery(new EntityQuery(json));
		Collection results = qr.getResults();

		assertTrue(results.size() == 5);
		for (Object o : results) {
			Order order = (Order) o;
			assertTrue(order.getShipCountry().equals("Brazil"));
		}

	}

	public void testOrderByNestedProp1() {
		// String json = qs.queryToJson(OrderDetail.class,
		// "?$filter=orderID lt 10258&$orderby=order/employee/lastName desc&$expand=order/employee");
		String json = "{ where: { orderID: { lt: 10258 }}, orderBy: 'order.employee.lastName desc', expand: 'order.employee' }";
		QueryResult qr = _qe.executeQuery("OrderDetails", json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		List<String> lastNames = new ArrayList<String>();
		List<String> lastNamesCpy = new ArrayList<String>();
		assertTrue(results.size() > 1);
		for (Object o : results) {
			OrderDetail od = (OrderDetail) o;
			assertTrue(od.getOrderID() < 10258);
			Order order = od.getOrder();
			assertTrue(order != null);
			Employee emp = order.getEmployee();
			String lastName = emp.getLastName();
			lastNames.add(lastName);
			lastNamesCpy.add(lastName);
		}

		Collections.sort(lastNames, Collections.reverseOrder());
		assertTrue(lastNames.equals(lastNamesCpy));
	}

	public void testExpandProduct() {
		// String json = qs.queryToJson(Customer.class,
		// "?$top=1&$filter=country eq 'Brazil'&$expand=orders/orderDetails/product");
		String json = "{ where: { country: 'Brazil' }, take: 1, expand: 'orders.orderDetails.product' }";
		QueryResult qr = _qe.executeQuery(Customer.class, json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() == 1);
		for (Object o : results) {
			Customer c = (Customer) o;
			assertTrue(c.getCountry().equals("Brazil"));
			Collection<Order> orders = c.getOrders();
			assertTrue(orders != null);
			for (Order order : orders) {
				Set<OrderDetail> ods = order.getOrderDetails();
				for (OrderDetail od : ods) {
					Product p = od.getProduct();
					assertTrue(p != null);
				}
			}
		}
	}

	public void testInlineCount() {
		// String json = qs.queryToJson(Customer.class,
		// "$top=3&$inlinecount=allpages");
		String json = "{ take: 3, inlineCountEnabled: true }";
		QueryResult qr = _qe.executeQuery(Customer.class, json);
		Collection results = qr.getResults();
		Long inlineCount = qr.getInlineCount();
		assertTrue(results.size() == 3);
		assertTrue(inlineCount > 3);
		String rJson = qr.toJson();
	}

	public void testComplexTypePropEqual() {
		// String json = qs.queryToJson(Supplier.class,
		// "?$filter=location/city eq 'New Orleans'");
		String json = "{ where: { location.city: 'New Orleans' }}";
		QueryResult qr = _qe.executeQuery(Supplier.class, json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() > 0);
		for (Object o : results) {
			Supplier s = (Supplier) o;
			Location loc = s.getLocation();
			String city = loc.getCity();
			assertTrue(city.equals("New Orleans"));
		}
	}

	public void testComplexTypePropNotEqual() {
		// String json = qs.queryToJson(Supplier.class,
		// "?$filter=location/city ne 'Tokyo'");
		String json = "{ where: { location.city: { ne: 'Tokyo' }}}";
		QueryResult qr = _qe.executeQuery(Supplier.class, json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() > 0);
		for (Object o : results) {
			Supplier s = (Supplier) o;
			Location loc = s.getLocation();
			String city = loc.getCity();
			assertTrue(!city.equals("Tokyo"));
		}
	}

	public void testGreaterThanDecimal() {
		// String json = qs.queryToJson(Product.class,
		// "?$filter=unitPrice gt 20.0");
		String json = "{ where: { unitPrice: { gt: 20.0 }}}";
		QueryResult qr = _qe.executeQuery(Product.class, json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() > 0);
		for (Object o : results) {
			Product p = (Product) o;
			BigDecimal price = p.getUnitPrice();
			assertTrue(price.doubleValue() > 20.0);
		}
	}

	public void testGreaterThanOrEqualDecimal() {
		// String json = qs.queryToJson(Product.class,
		// "?$filter=unitPrice ge 20.0");
		String json = "{ where: { unitPrice: { ge: 20.0 }}}";
		QueryResult qr = _qe.executeQuery(Product.class, json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() > 0);
		for (Object o : results) {
			Product p = (Product) o;
			BigDecimal price = p.getUnitPrice();
			assertTrue(price.doubleValue() >= 20.0);
		}
	}

	public void testLessThanDecimal() {
		// String json = qs.queryToJson(Product.class,
		// "?$filter=unitPrice lt 20.0");
		String json = "{ where: { unitPrice: { lt: 10.0 }}}";
		QueryResult qr = _qe.executeQuery(Product.class, json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() > 0);
		for (Object o : results) {
			Product p = (Product) o;
			BigDecimal price = p.getUnitPrice();
			assertTrue(price.doubleValue() < 10.0);
		}
	}

	public void testLessThanOrEqualDecimal() {
		// String json = qs.queryToJson(Product.class,
		// "?$filter=unitPrice le 20.0");
		String json = "{ where: { unitPrice: { le: 10.0 }}}";
		QueryResult qr = _qe.executeQuery(Product.class, json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() > 0);
		for (Object o : results) {
			Product p = (Product) o;
			BigDecimal price = p.getUnitPrice();
			assertTrue(price.doubleValue() <= 10.0);
		}
	}

	public void testEndsWith() {
		String json = "{ where: { productName: { endsWith: 'Sauce' }}}";
		QueryResult qr = _qe.executeQuery(Product.class, json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() > 0);
		for (Object o : results) {
			Product p = (Product) o;
			String productName = p.getProductName();
			assertTrue(productName.endsWith("Sauce"));
		}
	}

	public void testStartsWith() {
		String json = "{ where: { productName: { startsWith: 'Ch' }}}";
		QueryResult qr = _qe.executeQuery(Product.class, json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() > 0);
		for (Object o : results) {
			Product p = (Product) o;
			String productName = p.getProductName();
			assertTrue(productName.startsWith("Ch"));
		}
	}

	public void testContains() {
		String json = "{ where: { productName: { contains: 'ried' }}}"; // xxx
																		// Dried
																		// xxx,
																		// xxx
																		// Fried
																		// xxx
		QueryResult qr = _qe.executeQuery(Product.class, json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() > 2);
		for (Object o : results) {
			Product p = (Product) o;
			String productName = p.getProductName();
			assertTrue(productName.indexOf("ried") >= 0);
		}
	}

	public void testAndWithDecimal() {
		String json = "{ where: { unitPrice: { le: 10.0, gt: 3.5 }}}";
		QueryResult qr = _qe.executeQuery(Product.class, json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() > 0);
		for (Object o : results) {
			Product p = (Product) o;
			BigDecimal price = p.getUnitPrice();
			assertTrue(price.doubleValue() <= 10.0);
			assertTrue(price.doubleValue() > 3.5);
		}
	}

	public void testOrWithDecimal() {
		String json = "{ where: { or: [ { unitPrice: { ge: 200.0 }}, { unitPrice: { lt: 3.5 }}] }}";
		QueryResult qr = _qe.executeQuery(Product.class, json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() > 0);
		for (Object o : results) {
			Product p = (Product) o;
			BigDecimal price = p.getUnitPrice();
			assertTrue(price.doubleValue() >= 200.0
					|| price.doubleValue() < 3.5);
		}
	}

	public void testLogicalNot() {
		// String json = qs.queryToJson(Product.class,
		// "?$filter=not endswith(productName,'milk')");
		String json = "{ where: { not: { productName: { endsWith: 'milk' }}}}";
		QueryResult qr = _qe.executeQuery(Product.class, json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() > 0);
		for (Object o : results) {
			Product p = (Product) o;
			String productName = p.getProductName();
			assertTrue(!productName.endsWith("milk"));
		}
	}

	public void testNestedQuery() {
		// String json = qs.queryToJson(Product.class,
		// "?$filter=not endswith(productName,'milk')");
		String json = "{ where: { 'employee.lastName': { startsWith: 'D' }}}";
		QueryResult qr = _qe.executeQuery(Order.class, json);
		Collection results = qr.getResults();
		String rJson = qr.toJson();
		assertTrue(results.size() > 0);
		for (Object o : results) {
			Order order = (Order) o;
			Employee emp = order.getEmployee();
			String lastName = emp.getLastName();
			assertTrue(lastName.startsWith("D"));
		}
	}

}
