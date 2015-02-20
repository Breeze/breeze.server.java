package com.breeze.webtest;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.StringTokenizer;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public abstract class ControllerServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) {
		doGetOrPost(request, response);
	}

	public void doPost(HttpServletRequest request, HttpServletResponse response) {
		doGetOrPost(request, response);
	}

	private void doGetOrPost(HttpServletRequest request, HttpServletResponse response) {
		try {
			request.setAttribute("servletPath", request.getServletPath());
			String methodName = getMethodName(request);
			Method method = getMethod(this, methodName); 
			dispatch(method, this, request, response);
		} catch (ControllerNotFoundException ex) {
			sendError(response, HttpServletResponse.SC_BAD_REQUEST, ex.getMessage());
		} catch (Throwable ex) {
			sendError(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, ex.getMessage());
		}
	}

	protected void dispatch(Method m, Object target, HttpServletRequest request, HttpServletResponse response)
			throws ControllerNotFoundException {
		try {
			m.invoke(target, new Object[] { request, response });
		} catch (IllegalAccessException ex) {
			throw new ControllerNotFoundException("couldn't access method");
		} catch (InvocationTargetException ex) {
			throw new ControllerNotFoundException("object doesn't have method");
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
			response.getWriter().write(data);
		} catch (IOException e) {
			// TODO log this
			e.printStackTrace();
		}
	}
	
	protected void sendError(HttpServletResponse response, int statusCode, String message) {
		try {
			response.sendError(statusCode, message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static final Class[] sFormalArgs = { HttpServletRequest.class, HttpServletResponse.class };

	protected Method getMethod(Object target, String methodName) throws ControllerNotFoundException {
		try {
			return target.getClass().getMethod(methodName, sFormalArgs);
		} catch (NoSuchMethodException ex) {
			throw new ControllerNotFoundException("couldn't get method");
		}
	}

	protected String getMethodName(HttpServletRequest req) {
		String defaultMethod = "handleRequest";
		if (req.getPathInfo() == null)
			return defaultMethod;

		StringTokenizer tokens = new StringTokenizer(req.getPathInfo(), "/");
		if (tokens.hasMoreTokens()) {
			return tokens.nextToken();
		} else
			return defaultMethod;
	}
	
	protected abstract void handleRequest(HttpServletRequest request, HttpServletResponse response);

	class ControllerNotFoundException extends ServletException {

		ControllerNotFoundException(String reason) {
			super(reason);
		}

	}
}
