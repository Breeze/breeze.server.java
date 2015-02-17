package com.breezejs.query;

public class OrderByItem {
	String _propertyPath;
	boolean _isDesc;	

	
	public OrderByItem(String propertyPath, boolean isDesc) {
		_propertyPath = propertyPath;
		_isDesc = isDesc;
	}
	
	public String getPropertyPath() {
		return _propertyPath;
	}

	public boolean isDesc() {
		return _isDesc;
	}

}
