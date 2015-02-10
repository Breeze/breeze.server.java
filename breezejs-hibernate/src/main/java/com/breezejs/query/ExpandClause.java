package com.breezejs.query;


import java.util.List;

import com.breezejs.util.StringFns;

public class ExpandClause {
	private String _source;
	private List<String> _propertyPaths;
	

	public static final ExpandClause fromString(String source) {
		return (source == null) ? null : new ExpandClause(source);
	}
	
	public ExpandClause(String source) {
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
