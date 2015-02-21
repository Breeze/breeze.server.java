package northwind.service;

import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import northwind.model.Customer;

import com.breeze.webtest.BreezeControllerServlet;
import com.breezejs.query.QueryResult;

public class NorthwindTestServlet extends BreezeControllerServlet {

	public void customersInBrazil(HttpServletRequest request, HttpServletResponse response) {
		String json = "{ where: { country: 'Brazil' }, take: 5 }";
		executeQuery("Customers", json, response);
	}
	
	
}
