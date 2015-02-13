package com.breezejs.query;

import java.util.List;

import com.breezejs.metadata.DataType;

public class FnExpression extends Expression {
	private String _fnName;
	private List<Expression> _exprs;
	private DataType _dataType;
	public FnExpression(String fnName, List<Expression> exprs) {
		_fnName = fnName;
		_exprs = exprs;
	}
	public String getFnName() {
		return _fnName;
	}
	public List<Expression> getExpressions() {
		return _exprs;
	}
	// TODO: haven't yet propogated function return types to here.
	public DataType getDataType() {
		return _dataType;
	}
	
}