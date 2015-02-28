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
import com.breeze.webtest.ControllerServlet;
import com.breeze.query.QueryResult;

public class TestHarnessServlet extends ControllerServlet {
	
	@Override
	public void init() {
	}
	

	@Override
	protected void handleRequest(HttpServletRequest request, HttpServletResponse response) {

		String testCaseDir = "C:\\GitHub\\breeze.js\\test\\";
		String servletPath = request.getServletPath();
		 
		String pathInfo = request.getPathInfo();
		String fullFileName;
		if (servletPath.startsWith("/breezeTests")) {
			fullFileName = testCaseDir + "index.hibernate.html";
			response.setContentType("text/html");
		} else {
			String fileName = servletPath.substring(1);
			fullFileName = testCaseDir + fileName;
			if (fileName.endsWith(".js")) {
				response.setContentType("text/javascript");
			} else if (fileName.endsWith(".css")) {
				response.setContentType("text/css");
			}
		}

		
	    File srcFile = new File(fullFileName);
		
	    FileInputStream fileIn;
		try {
			fileIn = new FileInputStream(srcFile);
			ServletOutputStream out = response.getOutputStream();
	     
		    byte[] outputByte = new byte[4096];
		    int x;
		    //copy binary contect to output stream
		    while((x = fileIn.read(outputByte, 0, 4096)) != -1) 	    {
		    	out.write(outputByte, 0, x);
		    }
		    fileIn.close();
		    out.flush();
		    out.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			throw new RuntimeException("Unable to read: " + fullFileName);
		}

	}
	
}
