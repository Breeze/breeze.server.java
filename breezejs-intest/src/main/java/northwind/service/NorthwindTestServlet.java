package northwind.service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.breeze.webtest.BreezeControllerServlet;

public class NorthwindTestServlet extends BreezeControllerServlet {

	protected void customersInBrazil(HttpServletRequest request, HttpServletResponse response) {
		String json = "{ where: { country: 'Brazil' }, take: 5 }";
		executeQuery("Customers", json, response);
	}
	
}
