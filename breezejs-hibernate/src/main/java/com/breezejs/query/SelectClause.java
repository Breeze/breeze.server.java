package com.breezejs.query;

public class SelectClause {
	private String _source;
	public SelectClause(String source) {
		_source = source;
	}
	
	public boolean isNull() {
		return _source == null;
	}
}
