package com.breeze.query;

import com.breeze.metadata.DataType;

public class LitExpression extends Expression {
	@SuppressWarnings("unused")
    private Object _initialValue;
	private Object _coercedValue; 
	private DataType _dataType;

	// TODO: doesn't yet handle case where value is an array - i.e. rhs of in clause.
	public LitExpression(Object value, DataType dataType) {
		_initialValue = value;
		_dataType = dataType;
		_coercedValue = DataType.coerceData(value, dataType);
	}
	
	public Object getValue() {
		return _coercedValue;
	}

	public DataType getDataType() {
		return _dataType;
	}
	
}
