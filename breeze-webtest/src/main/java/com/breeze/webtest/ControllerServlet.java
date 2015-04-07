package com.breeze.webtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.breeze.util.JsonGson;

public abstract class ControllerServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		doGetOrPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		doGetOrPost(request, response);
	}

	protected void doGetOrPost(HttpServletRequest request, HttpServletResponse response) {
		request.setAttribute("servletPath", request.getServletPath());
		handleRequest(request, response);
	}

	protected void dispatch(Object target, Method m, HttpServletRequest request, HttpServletResponse response)
			throws ControllerException {
		try {
			m.invoke(target, new Object[] { request, response });
		} catch (IllegalAccessException ex) {
			throw new ControllerException("couldn't access method");
		} catch (InvocationTargetException ex) {
			Throwable targetEx = ex.getTargetException();
			throw new ControllerException(targetEx.getMessage());
		}
	}
	
	protected String readPostData(HttpServletRequest request) {
		StringBuffer sb = new StringBuffer();
		String line = null;
		try {
			BufferedReader reader = request.getReader();
			while ((line = reader.readLine()) != null)
				sb.append(line);
		} catch (Exception e) { 
			/*report an error*/
		}
		return sb.toString();
	}
		
	protected void writeResponse(HttpServletResponse response, String data) {
		try {
			response.setContentType("application/json;charset=UTF-8");
			response.getWriter().write(data);
		} catch (IOException e) {
			// TODO log this
			e.printStackTrace();
		}
	}

    protected void writeError(HttpServletResponse response, int status, String message) {
        writeError(response, status, message, null);
    }
	
	protected void writeError(HttpServletResponse response, int status, String message, String stack) {
		try {
			HttpErrorInfo err = new HttpErrorInfo(status, message, stack);
			String errMsg = JsonGson.toJson(err);
			response.setContentType("application/json;charset=UTF-8");
			response.setStatus(status);
			response.getWriter().write(errMsg);
		} catch (IOException e) {
			// TODO log this
			e.printStackTrace();
		}
	}


	static final Class[] sFormalArgs = { HttpServletRequest.class, HttpServletResponse.class };

	protected Method getMethod(Object target, String methodName) {
		try {
			return target.getClass().getMethod(methodName, sFormalArgs);
		} catch (NoSuchMethodException ex) {
			return null;
		}
	}

	protected String getMethodName(HttpServletRequest req) {
		if (req.getPathInfo() == null) return null;

		StringTokenizer tokens = new StringTokenizer(req.getPathInfo(), "/");
		if (tokens.hasMoreTokens()) {
			return tokens.nextToken();
		} else {
			return null;
		}
	}
	
	protected abstract void handleRequest(HttpServletRequest request, HttpServletResponse response);

	class ControllerException extends ServletException {

		ControllerException(String reason) {
			super(reason);
		}

	}
}
