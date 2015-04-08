package com.breeze.webserver;

public class HttpErrorInfo {
	public String message;
	public int statusCode;
	public String stack;
	public HttpErrorInfo(int statusCode, String message, String stack) {
		this.message = message;
		this.statusCode = statusCode;
		this.stack = stack;
	}
}
