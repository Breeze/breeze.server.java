package com.breezejs.query;

public class OrderByItem {
	String _propertyPath;
	boolean _isDesc;	
	// TODO: add logic to set this once we have access to metadata
	// private DataProperty lastProperty 
	
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
