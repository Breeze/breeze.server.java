package com.breezejs.query;

import java.util.List;

import com.breezejs.util.StringFns;

public class SelectClause {
	private String _source;
	private List<String> _propertyPaths;
	
	
	public static final SelectClause fromString(String source) {
		return (source == null) ? null : new SelectClause(source);
	}
	
	public SelectClause(String source) {
		_source = source;
		_propertyPaths = StringFns.ToList(source, "\\,");
	}
	
	public String getSource() {
		return _source;
	}
	
	public List<String> getPropertyPaths() {
		return _propertyPaths;
	}

	
}
