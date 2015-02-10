package com.breezejs.query;

public class ExpandClause {
	private String _source;
	public ExpandClause(String source) {
		_source = source;
	}
	
	public boolean isNull() {
		return _source == null; 
	}

}
