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

	public void customersInBrazil(HttpServletRequest request, HttpServletResponse response) {
		String json = "{ where: { country: 'Brazil' }, take: 5 }";
		executeQuery("Customers", json, response);
	}
	
	public void CustomersStartingWith(HttpServletRequest request, HttpServletResponse response) {
	    EntityQuery eq = this.extractEntityQuery(request);
	    String companyName = (String) eq.getParameters().get("companyName");
	    
	    Predicate newPred = new BinaryPredicate(Operator.StartsWith, "companyName", companyName);
	    // create a new EntityQuery object
	    eq = eq.where(newPred);
	    executeQuery(Customer.class, eq, response);
	    
	    
	}
	
/*	namedQuery.CustomersStartingWith = function(req, res, next) {
	    // start with client query and add an additional filter.
	  var companyName = req.query.companyName;
	  if (companyName == undefined) {
	    var err = { statusCode: 404, message: "'companyName must be provided'" };
	    next(err);
	  }
	  // need to use upper case because base query came from server
	  var pred = new breeze.Predicate("CompanyName", "startsWith", companyName);
	  var entityQuery = EntityQuery.fromUrl(req.url, "Customers").where(pred);
	  executeEntityQuery(entityQuery, null, res, next);
	};*/
	
}
