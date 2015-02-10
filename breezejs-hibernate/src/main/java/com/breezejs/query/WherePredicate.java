package com.breezejs.query;

import java.util.Map;

public class WherePredicate {
	private Map _sourceMap;
	public WherePredicate(Map sourceMap) {
		_sourceMap = sourceMap;
		
	}
	
	public boolean isNull() {
		return _sourceMap == null;
	}
}
