package com.breeze.query;

public enum OperatorType {
	
    AnyAll(1),
    AndOr(2),
    Binary(3),
    Unary(4);
	
	public final int _value;
    
	OperatorType(int value) {
    	_value = value;
    }
	
}
