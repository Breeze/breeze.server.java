package com.breezejs.query;

import com.breezejs.metadata.DataType;

public class LitExpression extends Expression {
	private Object _value;
	private DataType _dataType;
	private boolean _hasExplicitValue;
	public LitExpression(Object value, DataType dataType, boolean hasExplicitValue) {
		_value = value;
		_dataType = dataType;
		_hasExplicitValue = hasExplicitValue;
	}
	
	public Object getValue() {
		return _value;
	}
	public DataType getDataType() {
		return _dataType;
	}
}
