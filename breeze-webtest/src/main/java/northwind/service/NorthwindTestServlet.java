package northwind.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import northwind.model.Customer;
import northwind.model.Employee;
import northwind.model.Order;
import northwind.model.Product;

import com.breeze.webtest.BreezeControllerServlet;
import com.breeze.metadata.DataType;
import com.breeze.query.AndOrPredicate;
import com.breeze.query.BinaryPredicate;
import com.breeze.query.EntityQuery;
import com.breeze.query.Operator;
import com.breeze.query.Predicate;
import com.breeze.query.QueryResult;

public class NorthwindTestServlet extends BreezeControllerServlet {

    public void customersInBrazil(HttpServletRequest request,
            HttpServletResponse response) {
        String json = "{ where: { country: 'Brazil' }, take: 5 }";
        executeQuery("Customers", json, response);
    }
    

    
    public void CustomerFirstOrDefault(HttpServletRequest request,
            HttpServletResponse response) {
     // should return empty array
        Predicate newPred = new BinaryPredicate(Operator.StartsWith,
                "companyName", "blah");
        EntityQuery eq = new EntityQuery().where(newPred);
        QueryResult qr = executeQuery(Customer.class, eq);
        writeResponse(response, qr.toJson());
    };

    public void CustomersStartingWithA(HttpServletRequest request,
            HttpServletResponse response) {
        Predicate newPred = new BinaryPredicate(Operator.StartsWith,
                "companyName", "A");
        EntityQuery eq = new EntityQuery().where(newPred);
        // or ...
        // EntityQuery eq = new
        // EntityQuery("{ companyName: { startsWith: 'A' }}");
        QueryResult qr = executeQuery(Customer.class, eq);
        writeResponse(response, qr.toJson());
        
    };

    public void CustomersStartingWith(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        String companyName = (String) eq.getParameters().get("companyName");
        if (companyName != null) {
            Predicate newPred = new BinaryPredicate(Operator.StartsWith,
                    "companyName", companyName);
            // create a new EntityQuery object
            eq = eq.where(newPred);
        } else {
            if (!eq.getParameters().containsKey("companyName")) {
                writeError(response, 404, "'companyName' parameter should have been passed in");
                return;
            }
        }
        QueryResult qr = executeQuery(Customer.class, eq);
        writeResponse(response, qr.toJson());
    }

    public void CustomersOrderedStartingWith(HttpServletRequest request,
            HttpServletResponse response) {
        // start with client query and add an additional filter.
        EntityQuery eq = this.extractEntityQuery(request);
        String companyName = (String) eq.getParameters().get("companyName");

        Predicate newPred = new BinaryPredicate(Operator.StartsWith,
                "companyName", companyName);

        eq = eq.where(newPred).orderBy("companyName");
        QueryResult qr = executeQuery(Customer.class, eq);
        writeResponse(response, qr.toJson());
    }

    public void CustomersAndOrders(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        // create a new EntityQuery object
        eq = eq.expand("orders");
        QueryResult qr = executeQuery(Customer.class, eq);
        writeResponse(response, qr.toJson());
    }
    
    public void CustomerWithScalarResult(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        // create a new EntityQuery object
        eq = eq.take(1);
        QueryResult qr = executeQuery(Customer.class, eq);
        writeResponse(response, qr.toJson());
    }
    
    public void CustomersWithHttpError(HttpServletRequest request,
            HttpServletResponse response) {
        this.writeError(response, 404, "Unable to do something");
    }
    
    public void CustomersAsHRM(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        QueryResult qr = executeQuery(Customer.class, eq);
        writeResponse(response, qr.toJson());
    }
    
    public class CustomerWithBigOrders {
        public Customer customer;
        public List<Order> bigOrders;
        public CustomerWithBigOrders(Customer customer, List<Order> bigOrders) {
            this.customer = customer;
            this.bigOrders = bigOrders;
        }
    }
    
    public void CustomersWithBigOrders(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        String json = "{ orders: { any: { freight: { gt: 100 } } } }";
        eq = eq.where(json).expand("orders");
        QueryResult result = executeQuery(Customer.class, eq);
        QueryResult qr = executeQuery(Customer.class, eq);
        List<CustomerWithBigOrders> customersWithBigOrders = new ArrayList<CustomerWithBigOrders>();
        for (Object o : qr.getResults()) {
            Customer c = (Customer) o;
            List<Order> bigOrders = new ArrayList<Order>();
            for (Order order : c.getOrders()) {
                if (order.getFreight().doubleValue() > 100.0) {
                    bigOrders.add(order);
                }
            }
            if (bigOrders.size() > 0) {
                CustomerWithBigOrders cwbo = new CustomerWithBigOrders(c, bigOrders);
                customersWithBigOrders.add(cwbo);
            }
        }
        
        qr = new QueryResult(customersWithBigOrders);
        writeResponse(response, qr.toJson());
    }
    
    public class CustomersAndProductsBundle {
        public List<Customer> customers;
        public List<Product> products;
        public CustomersAndProductsBundle(List<Customer> customers, List<Product> products) {
            this.customers = customers;
            this.products = products;
        }
    }
    
    
    public void CustomersAndProducts(HttpServletRequest request,
            HttpServletResponse response) {
        
        EntityQuery eq = new EntityQuery();
        QueryResult cresult = executeQuery(Customer.class, new EntityQuery());
        QueryResult presult = executeQuery(Product.class, new EntityQuery());
        List<CustomersAndProductsBundle> list = new ArrayList<CustomersAndProductsBundle>();
        list.add(new CustomersAndProductsBundle((List<Customer>) (cresult.getResults()), (List<Product>) (presult.getResults())));
        
        QueryResult qr = new QueryResult(list);
        writeResponse(response, qr.toJson());
    }
  
    // AltCustomers will not be in the resourceName/entityType map;
    public void AltCustomers(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        QueryResult qr = this.executeQuery(Customer.class, eq);
        writeResponse(response, qr.toJson());
    }

    public void SearchCustomers(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        Map parameterMap = eq.getParameters();
        String companyName = (String) parameterMap.get("CompanyName");
        ArrayList contactNames = (ArrayList) parameterMap.get("ContactNames");
        String city = (String) parameterMap.get("City");
        boolean ok = companyName.length() > 0 && contactNames.size() > 1 && city.length() > 0;
        if (!ok) {
            writeError(response, 400, "Unable to properly parse parameters");
        }
        // just testing that qbe actually made it in not attempted to write qbe logic here
        // so just return first 3 customers.
        EntityQuery altQ = new EntityQuery().take(3);
        QueryResult qr = this.executeQuery(Customer.class, altQ);
        writeResponse(response, qr.toJson());
    }
    
    public void SearchCustomers2(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        Map parameterMap = eq.getParameters();
        ArrayList qbeList = (ArrayList) parameterMap.get("qbeList");
        for (Object qbeItem: qbeList) {
            Map qbeMap = (Map) qbeItem;
        
            String companyName = (String) qbeMap.get("CompanyName");
            ArrayList contactNames = (ArrayList) qbeMap.get("ContactNames");
            String city = (String) qbeMap.get("City");
            boolean ok = companyName.length() > 0 && contactNames.size() > 0 && city.length() > 0;
            if (!ok) {
                writeError(response, 400, "Unable to properly parse parameters");
                return;
            }
        }
        // just testing that qbe actually made it in not attempted to write qbe logic here
        // so just return first 3 customers.
        EntityQuery altQ = new EntityQuery().take(3);
        QueryResult qr = this.executeQuery(Customer.class, altQ);
        writeResponse(response, qr.toJson());
    }
    

    public void OrdersCountForCustomer(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        Map parameterMap = eq.getParameters();
        String companyName = (String) parameterMap.get("companyName");
        // EntityQuery eq2 = new EntityQuery();
        eq = eq.where( new BinaryPredicate(Operator.StartsWith, "companyName", companyName));
        eq = eq.expand("orders");
        eq = eq.take(1);
        QueryResult qr = this.executeQuery(Customer.class, eq);
        List<Customer> custResults = (List<Customer>) qr.getResults();
        List<Integer> results = new ArrayList<Integer>();
        if (custResults.size() > 0) {
            Customer cust = (Customer) qr.getResults().get(0);
            results.add(cust.getOrders().size());
        } else {
            results.add(0);
        }
        
        qr = new QueryResult(results);
        writeResponse(response, qr.toJson());
    }
    
    public void EnumerableEmployees(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        QueryResult qr = this.executeQuery(Employee.class, eq);
        writeResponse(response, qr.toJson());
    }
    
    public void EmployeesMultipleParams(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        Map parameterMap = eq.getParameters();
        Integer empId = ((Double) parameterMap.get("employeeID")).intValue();
        String city = (String) parameterMap.get("city");
        Predicate pred1 = new BinaryPredicate(Operator.Equals, "employeeID", empId);
        Predicate pred2 = new BinaryPredicate(Operator.Equals, "city", city);
        Predicate pred = new AndOrPredicate(Operator.And, pred1, pred2);
        EntityQuery eq2 = new EntityQuery().where(pred);
        QueryResult qr = this.executeQuery(Employee.class, eq2);
        writeResponse(response, qr.toJson());
    }

    public void CompanyNames(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = new EntityQuery().select("companyName");
        QueryResult qr = this.executeQuery(Customer.class, eq);
        writeResponse(response, qr.toJson());
    }
    
    public void CompanyNamesAndIds(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        eq = eq.select("companyName", "customerID");
        QueryResult qr = this.executeQuery(Customer.class, eq);
        writeResponse(response, qr.toJson());
    }
    
    public void CompanyNamesAndIdsAsDTO(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        eq = eq.select("companyName", "customerID");
        QueryResult qr = this.executeQuery(Customer.class, eq);
        writeResponse(response, qr.toJson());
    }

    public void CompanyInfoAndOrders(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        eq = eq.select("companyName", "customerID", "orders");
        QueryResult qr = this.executeQuery(Customer.class, eq);
        writeResponse(response, qr.toJson());
    }

    public void OrdersAndCustomers(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        eq = eq.expand("customer");
        QueryResult qr = this.executeQuery(Order.class, eq);
        writeResponse(response, qr.toJson());
    }

    public void SearchEmployees(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        Map parameterMap = eq.getParameters();
        ArrayList tmpEmpIds = (ArrayList) parameterMap.get("employeeIds");
        List<Integer> empIds = new ArrayList<Integer>();
        for ( Object val : tmpEmpIds) {
            empIds.add(((Double) val).intValue());
        }
        eq = eq.where(new BinaryPredicate(Operator.In, "employeeID", empIds));

        QueryResult qr = this.executeQuery(Employee.class, eq);
        writeResponse(response, qr.toJson());
    }

    public void EmployeesFilteredByCountryAndBirthdate(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        Map parameterMap = eq.getParameters();
        String tmpBirthDate = (String) parameterMap.get("birthDate");
        try {

            Date birthDate = new SimpleDateFormat("mm/dd/yy").parse(tmpBirthDate);
            Calendar cal = Calendar.getInstance();
            cal.setTime(birthDate);
            tmpBirthDate = javax.xml.bind.DatatypeConverter.printDateTime(cal);
            
        } catch (Exception e) {
            // ok - this just means that we should allow the breeze predicate to parse it.
        }
        // Date birthDate = (Date) DataType.coerceData(tmpBirthDate, DataType.DateTime);
        String country = (String) parameterMap.get("country");
        Predicate pred1 = new BinaryPredicate(Operator.GreaterThanOrEqual, "birthDate", tmpBirthDate);
        Predicate pred2 = new BinaryPredicate(Operator.Equals, "country", country);
        Predicate pred = new AndOrPredicate(Operator.And, pred1, pred2);
        eq = eq.where(pred);
        QueryResult qr = this.executeQuery(Employee.class, eq);
        writeResponse(response, qr.toJson());
    }



}
