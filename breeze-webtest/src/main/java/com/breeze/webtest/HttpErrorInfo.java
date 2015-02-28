package com.breeze.webtest;

public class HttpErrorInfo {
	public String message;
	public int statusCode;
	public HttpErrorInfo(int statusCode, String message) {
		this.message = message;
		this.statusCode = statusCode;
	}
}
