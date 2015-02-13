package com.breezejs.query;

import java.util.List;

import com.breezejs.metadata.DataType;
import com.breezejs.metadata.IEntityType;

public class FnExpression extends Expression {
	private String _fnName;
	private List<Expression> _exprs;
	private DataType _dataType;

	public FnExpression(String fnName, List<Expression> exprs) {
		_fnName = fnName;
		_exprs = exprs;
	}
	
	public static FnExpression createFrom(String source, IEntityType entityType) {
		return FnExpressionToken.fromString(source).toExpression(entityType);
	}
		
	public String getFnName() {
		return _fnName;
	}
	public List<Expression> getExpressions() {
		return _exprs;
	}
	// TODO: haven't yet propagated function return types to here.
	public DataType getDataType() {
		return _dataType;
	}
	
	
	
}