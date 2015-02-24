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
import com.breezejs.query.QueryResult;

public class NorthwindTestServlet extends BreezeControllerServlet {

	public void customersInBrazil(HttpServletRequest request, HttpServletResponse response) {
		String json = "{ where: { country: 'Brazil' }, take: 5 }";
		executeQuery("Customers", json, response);
	}
	

	
}
