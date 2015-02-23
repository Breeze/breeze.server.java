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
	
//	public void breezeTest(HttpServletRequest request, HttpServletResponse response) {
//
//		String testCaseDir = "C:\\GitHub\\breeze.js\\test\\";
//		String fileName = testCaseDir + "index.hibernate.html";
////		try {
////			response.sendRedirect(fileName);
////		} catch (IOException e1) {
////			throw new RuntimeException("Unable to redirect to: " + fileName);
////		}
//		// response.setHeader("Content-Type", "text/html");
//		response.setContentType("text/html");
//	    // response.setHeader("Content-Disposition", "filename=\"hoge.txt\"");
//	    File srcFile = new File(fileName);
//	    // FileUtils.copyFile(srcFile, response.getOutputStream());
//		
//	    FileInputStream fileIn;
//		try {
//			fileIn = new FileInputStream(srcFile);
//			ServletOutputStream out = response.getOutputStream();
//	     
//		    byte[] outputByte = new byte[4096];
//		    //copy binary contect to output stream
//		    while(fileIn.read(outputByte, 0, 4096) != -1) 	    {
//		    	out.write(outputByte, 0, 4096);
//		    }
//		    fileIn.close();
//		    out.flush();
//		    out.close();
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			throw new RuntimeException("Unable to read: " + fileName);
//		}
//
//	}
	
}
