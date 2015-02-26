package com.breezejs.hib;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
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

import com.breezejs.query.EntityQuery;
import com.breezejs.query.QueryResult;
import com.breezejs.util.JsonGson;

import junit.framework.TestCase;

public class QueryServiceTest extends TestCase {

    private QueryService _qe;

    // private SimpleDateFormat DATEFMT = new SimpleDateFormat("dd/MM/yyyy");

    protected void setUp() throws Exception {
        super.setUp();
        // then populate the database with test data...?
        _qe = new QueryService(StaticConfigurator.getSessionFactory());
    }

    // TODO: test boolean where - waiting on 'discontinued' field

    public void testEmptyQuery() {
        String json = "";
        QueryResult qr = _qe.executeQuery(Customer.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 5);
        for (Object o : results) {
            Customer c = (Customer) o;
        }
    }

    public void testNullQuery() {
        String json = null;
        QueryResult qr = _qe.executeQuery(Customer.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 5);
        for (Object o : results) {
            Customer c = (Customer) o;
        }
    }

    public void testSimpleWhereTake() {
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

    public void testOrderByNested3Deep() {
        // String json = qs.queryToJson(OrderDetail.class,
        // "?$filter=orderID lt 10258&$orderby=order/employee/lastName desc&$expand=order/employee");
        String json = "{ where: { orderID: { lt: 10258 }}, orderBy: ['order.employee.lastName desc'], expand: 'order.employee' }";
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

    public void testExpandNested3Deep() {
        // String json = qs.queryToJson(Customer.class,
        // "?$top=1&$filter=country eq 'Brazil'&$expand=orders/orderDetails/product");
        String json = "{ where: { country: 'Brazil' }, take: 1, expand: ['orders.orderDetails.product'] }";
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
        String json = "{ take: 3, inlineCount: true }";
        QueryResult qr = _qe.executeQuery(Customer.class, json);
        Collection results = qr.getResults();
        Long inlineCount = qr.getInlineCount();
        assertTrue(results.size() == 3);
        assertTrue(inlineCount > 3);
        String rJson = qr.toJson();
    }

    public void testInlineCountWithTake0() {
        String json = "{ take: 0, inlineCount: true }";
        QueryResult qr = _qe.executeQuery(Customer.class, json);
        Collection results = qr.getResults();
        Long inlineCount = qr.getInlineCount();
        assertTrue(results.size() == 0);
        assertTrue(inlineCount > 30);
        String rJson = qr.toJson();
    }

    public void testGreaterThanDateProps() {
        String json = "{ where: { birthDate: { lt: 'hireDate'}}}";
        QueryResult qr = _qe.executeQuery(Employee.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            Employee emp = (Employee) o;
            Date birthDt = emp.getBirthDate();
            Date hireDt = emp.getHireDate();
            assertTrue(birthDt.compareTo(hireDt) < 0);
        }
    }

    public void testContainsTwoProps() {
        String json = "{ where: { notes: { contains: 'firstName'}}}";
        QueryResult qr = _qe.executeQuery(Employee.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            Employee emp = (Employee) o;
            String notes = emp.getNotes();
            String firstName = emp.getFirstName();
            assertTrue(notes.indexOf(firstName) >= 0);
        }
    }

    public void testStartsWithTwoProps() {
        String json = "{ where: { notes: { startsWith: 'firstName'}}}";
        QueryResult qr = _qe.executeQuery(Employee.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            Employee emp = (Employee) o;
            String notes = emp.getNotes();
            String firstName = emp.getFirstName();
            assertTrue(notes.indexOf(firstName) == 0);
        }
    }

    public void testInString() {
        String[] countries = { "Austria", "Italy", "Norway" };
        String json = "{ where: { country: { in: ['Austria', 'Italy', 'Norway'] } } }";
        QueryResult qr = _qe.executeQuery(Customer.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            Customer cust = (Customer) o;
            String country = cust.getCountry();
            assertTrue(Arrays.asList(countries).contains(country));
        }
    }

    public void testInInt() {
        int[] empIds = { 1, 2, 4 };
        String json = "{ where: { reportsToEmployeeID: { in: [1 ,2 ,4] } } }";
        QueryResult qr = _qe.executeQuery(Employee.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            Employee emp = (Employee) o;
            int empId = emp.getReportsToEmployeeID();
            assertTrue(empId == 1 || empId == 2 || empId == 4);
            // assertTrue(Arrays.asList(empIds).contains(emp.getReportsToEmployeeID()));
        }
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

    public void testEqBoolean() {

        String json = "{ where: { isDiscontinued: true }}";
        QueryResult qr = _qe.executeQuery(Product.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            Product p = (Product) o;
            assertTrue(p.getIsDiscontinued());
        }
    }

    public void testGreaterThanDate() {
        // String json = qs.queryToJson(Product.class,
        // "?$filter=unitPrice gt 20.0");

        Date latestBirthDate = toDate(1956, 1, 1);
        String json = "{ where: { birthDate: { lt: '1956-01-01T00:00:00' }}}";
        QueryResult qr = _qe.executeQuery(Employee.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            Employee emp = (Employee) o;
            Date birthDt = emp.getBirthDate();
            assertTrue(birthDt.compareTo(latestBirthDate) < 0);
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
        // ...Dried/Fried...
        String json = "{ where: { productName: { contains: 'ried' }}}";

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

    public void testNotWithOr() {
        String json = "{ where: { not: { or: [ { companyName: { startsWith: 'B'}}, { city: { startsWith: 'L' }} ] } } }";
        QueryResult qr = _qe.executeQuery(Customer.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            Customer c = (Customer) o;
            assertTrue(!c.getCompanyName().startsWith("B"));
            assertTrue(!c.getCity().startsWith("L"));
        }
    }

    public void testNot() {
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

    public void testWhereNestedString() {
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

    public void testWhereNestedDate() {
        Date latestBirthDate = toDate(1956, 1, 1);
        String json = "{ where: { employee.birthDate: { gt: '1960-01-01T00:00:00' }}}";
        QueryResult qr = _qe.executeQuery(Order.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            Order order = (Order) o;
            Employee emp = order.getEmployee();
            Date birthDate = emp.getBirthDate();
            assertTrue(birthDate.compareTo(latestBirthDate) > 0);
        }
    }

    public void testWhereNestedWithAnd() {
        // String json = qs.queryToJson(Product.class,
        // "?$filter=not endswith(productName,'milk')");
        String json = "{ where: { freight: {gt: 100.0} , 'employee.lastName': { startsWith: 'D' }}}";
        QueryResult qr = _qe.executeQuery(Order.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            Order order = (Order) o;
            BigDecimal freight = order.getFreight();
            assertTrue(freight.doubleValue() > 100.0);
            Employee emp = order.getEmployee();
            String lastName = emp.getLastName();
            assertTrue(lastName.startsWith("D"));
        }
    }

    public void testWhereNestedSameAliasWithAnd() {
        //
        String json = "{ where: { 'product.productID': { gt: 11} , 'product.productName': { startsWith: 'Q' }}}";
        QueryResult qr = _qe.executeQuery(OrderDetail.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            OrderDetail od = (OrderDetail) o;

            Product p = od.getProduct();
            assertTrue(p.getProductName().startsWith("Q"));
            assertTrue(p.getProductID() > 11);
        }
    }

    public void testWhereNestedString3Deep() {
        // String json = qs.queryToJson(Product.class,
        // "?$filter=not endswith(productName,'milk')");
        String json = "{ where: { 'order.employee.lastName': { startsWith: 'D' }}}";
        QueryResult qr = _qe.executeQuery(OrderDetail.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            OrderDetail od = (OrderDetail) o;
            Order order = od.getOrder();
            Employee emp = order.getEmployee();
            String lastName = emp.getLastName();
            assertTrue(lastName.startsWith("D"));
        }
    }

    public void testWhereNestedInt() {
        // String json = qs.queryToJson(Product.class,
        // "?$filter=not endswith(productName,'milk')");
        String json = "{ where: { 'product.productID': 1}}";
        QueryResult qr = _qe.executeQuery(OrderDetail.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            OrderDetail od = (OrderDetail) o;
            Product p = od.getProduct();
            Integer id = p.getProductID();
            assertTrue(id == 1);
        }
    }

    public void testSingleCustomerClone() {

        String json = "{ take: 1 }";
        QueryResult qr = _qe.executeQuery(Customer.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() == 1);
        for (Object o : results) {
            Customer cust = (Customer) o;
            String custJson = JsonGson.toJson(cust, true);
            Map custMap = JsonGson.fromJson(custJson);
            Object clone = JsonGson.fromMap(Customer.class, custMap);
            Customer custClone = (Customer) clone;
            assertTrue(cust != custClone);
            assertTrue(cust.getCompanyName().equals(custClone.getCompanyName()));
            assertTrue(cust.getCustomerID().equals(custClone.getCustomerID()));
        }
    }

    public void testSelect() {

        String json = "{ where: { unitPrice: { gt: 20.0 }}, select: 'productName,unitPrice' }";
        QueryResult qr = _qe.executeQuery(Product.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            HashMap<String, Object> hm = (HashMap<String, Object>) o;
            String productName = (String) hm.get("productName");
            BigDecimal unitPrice = (BigDecimal) hm.get("unitPrice");
            assertTrue(unitPrice.doubleValue() > 20.0);
        }
    }

    public void testSelectNestedScalarNav() {

        String json = "{ where: { unitPrice: { gt: 20.0  } }, select: 'productName,supplier, supplier.companyName' }";
        QueryResult qr = _qe.executeQuery(Product.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            HashMap<String, Object> hm = (HashMap<String, Object>) o;
            String productName = (String) hm.get("productName");
            String companyName = (String) hm.get("supplier.companyName");
            Supplier supplier = (Supplier) hm.get("supplier");
            assertTrue(supplier.getCompanyName() != null);
            assertTrue(supplier.getCompanyName().equals(companyName));

        }
    }

    public void testSelectNestedNonScalarNav() {
        String json = "{ where: { companyName: { startsWith: 'B'  } }, select: 'companyName, orders' }";
        QueryResult qr = _qe.executeQuery(Customer.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            HashMap<String, Object> hm = (HashMap<String, Object>) o;
            String companyName = (String) hm.get("companyName");
            Collection<Order> orders = (Collection<Order>) hm.get("orders");
            assertTrue(orders.size() > 0);

        }
    }

    public void testSelectNestedData() {
        String json = "{ where: { unitPrice: { gt: 20.0 }}, select: 'productName,supplier.companyName,unitPrice' }";
        QueryResult qr = _qe.executeQuery(Product.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            HashMap<String, Object> hm = (HashMap<String, Object>) o;
            String productName = (String) hm.get("productName");
            assertTrue(productName.length() > 0);
            String supplierCompany = (String) hm.get("supplier.companyName");
            assertTrue(supplierCompany.length() > 0);
            assertTrue(productName.length() > 0);
            BigDecimal unitPrice = (BigDecimal) hm.get("unitPrice");
            assertTrue(unitPrice.doubleValue() > 20.0);
        }
    }

    public void testSelectNestedWithWhereNested() {
        String json = "{ where: { unitPrice: { gt: 20.0 }, 'supplier.companyName': { startsWith: 'S' }}, select: 'productName,supplier.companyName,unitPrice' }";
        QueryResult qr = _qe.executeQuery(Product.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            HashMap<String, Object> hm = (HashMap<String, Object>) o;
            String productName = (String) hm.get("productName");
            assertTrue(productName.length() > 0);
            String supplierCompany = (String) hm.get("supplier.companyName");
            assertTrue(supplierCompany.startsWith("S"));
            assertTrue(productName.length() > 0);
            BigDecimal unitPrice = (BigDecimal) hm.get("unitPrice");
            assertTrue(unitPrice.doubleValue() > 20.0);
        }
    }

    public void testAny() {
        String json = "{ where: { orders: { any: { freight: { gt: 950.0 } } } } }";
        QueryResult qr = _qe.executeQuery(Customer.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() < 5);
        for (Object o : results) {
            Customer c = (Customer) o;
        }
    }

    public void testAnyWithExpand() {
        String json = "{ where: { orders: { any: { freight: { gt: 950.0 } } } }, expand: 'orders' }";
        QueryResult qr = _qe.executeQuery(Customer.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() < 5);
        for (Object o : results) {
            Customer c = (Customer) o;
            Collection<Order> orders = c.getOrders();
            assertTrue(orders.size() > 0);
            boolean isOk = false;
            for (Order order : orders) {
                if (order.getFreight().doubleValue() > 950.0) {
                    isOk = true;
                }
            }
            assertTrue(isOk);
        }
    }

    public void testAndNotEqWithOrderByAndExpand() {
        // var p = Predicate.create("freight", ">", 100).and("customerID", "!=",
        // null);
        // var query = new breeze.EntityQuery()
        // .from("Orders")
        // .where(p)
        // .orderBy("orderID")
        // .expand("customer")
        // .take(1);
        String json = "{ where: { freight: { gt: 100.0 }, customerID: { ne: null }}, orderBy: ['orderID'], expand: ['customer'] }";
        QueryResult qr = _qe.executeQuery(Order.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            Order order = (Order) o;
            BigDecimal freight = order.getFreight();
            assertTrue(freight.doubleValue() > 100.0);
            assertTrue(order.getCustomerID() != null);
        }
    }

    //
    public void testNestedOrderByAndExpand() {
        // var q1 = EntityQuery.from("Products")
        // .expand("category")
        // .orderBy("category.categoryName, productName");
        String json = "{ orderBy: ['category.categoryName', 'productName'], expand: ['category'] }";
        QueryResult qr = _qe.executeQuery(Product.class, json);
        Collection results = qr.getResults();
        String rJson = qr.toJson();
        assertTrue(results.size() > 0);
        for (Object o : results) {
            Product product = (Product) o;
            // TODO:
        }
    }

    private Date toDate(int yr, int month, int day) {
        int y = yr - 1900;
        int m = month - 1;
        // wierd rules: yy - 1900, mm (0-11), dd (1-31)
        return new Date(y, m, day);
    }

}
