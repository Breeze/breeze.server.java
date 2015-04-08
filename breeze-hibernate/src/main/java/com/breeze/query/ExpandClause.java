package com.breeze.query;


import java.util.List;


public class ExpandClause {
	private List<String> _propertyPaths;
	

	public static final ExpandClause from(List<String> propertyPaths) {
		return (propertyPaths == null) ? null : new ExpandClause(propertyPaths);
	}
	
	public ExpandClause(List<String> propertyPaths) {
		_propertyPaths = propertyPaths;
	}

	
	public List<String> getPropertyPaths() {
		return _propertyPaths;
	}

}
