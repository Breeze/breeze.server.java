package northwind.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import northwind.model.Customer;
import northwind.model.Order;
import northwind.model.Product;

import com.breeze.webtest.BreezeControllerServlet;
import com.breezejs.query.BinaryPredicate;
import com.breezejs.query.EntityQuery;
import com.breezejs.query.Operator;
import com.breezejs.query.Predicate;
import com.breezejs.query.QueryResult;

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

        Predicate newPred = new BinaryPredicate(Operator.StartsWith,
                "companyName", companyName);
        // create a new EntityQuery object
        eq = eq.where(newPred);
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
        // create a new EntityQuery object
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




}
