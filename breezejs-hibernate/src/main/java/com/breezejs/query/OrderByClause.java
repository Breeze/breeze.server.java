package com.breezejs.query;

public class OrderByClause {
	private String _source;
    public OrderByClause(String source) {
    	_source = source;
    }
    
    public boolean isNull() {
    	return _source == null;
    }
}
