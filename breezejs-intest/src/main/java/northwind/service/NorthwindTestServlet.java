package northwind.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URLDecoder;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import northwind.model.Customer;

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

    public void CustomersStartingWithA(HttpServletRequest request,
            HttpServletResponse response) {
        Predicate newPred = new BinaryPredicate(Operator.StartsWith,
                "companyName", "A");
        EntityQuery eq = new EntityQuery().where(newPred);
        // or ...
        // EntityQuery eq = new
        // EntityQuery("{ companyName: { startsWith: 'A' }}");
        executeQuery(Customer.class, eq, response);
    };

    public void CustomersStartingWith(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        String companyName = (String) eq.getParameters().get("companyName");

        Predicate newPred = new BinaryPredicate(Operator.StartsWith,
                "companyName", companyName);
        // create a new EntityQuery object
        eq = eq.where(newPred);
        executeQuery(Customer.class, eq, response);
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
        executeQuery(Customer.class, eq, response);
    }

    public void CustomersAndOrders(HttpServletRequest request,
            HttpServletResponse response) {
        EntityQuery eq = this.extractEntityQuery(request);
        // create a new EntityQuery object
        eq = eq.expand("orders");
        executeQuery(Customer.class, eq, response);
    }
}
