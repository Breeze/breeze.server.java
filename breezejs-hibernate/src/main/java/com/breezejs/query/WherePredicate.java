package com.breezejs.query;

import java.util.Map;

public class WherePredicate {
	private Map _sourceMap;
	

	public static final WherePredicate fromMap(Map source) {
		return (source == null) ? null : new WherePredicate(source);
	}
	
	
	public WherePredicate(Map sourceMap) {
		_sourceMap = sourceMap;
		
	}
	
	public Map getSourceMap() {
		return _sourceMap;
	}
}
