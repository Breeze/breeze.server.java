package com.breeze.query;

import java.util.List;

import com.breeze.metadata.IEntityType;

public class BinaryPredicate extends Predicate {
	private Operator _op;
	private Object _expr1Source;
	private Object _expr2Source;
	private Expression _expr1;
	private Expression _expr2;
	
	public BinaryPredicate(Operator op, Object expr1Source, Object expr2Source) {
		_op = op;
		_expr1Source = expr1Source;
		_expr2Source = expr2Source;
	}
	
	public Operator getOperator() {
		return _op;
	}
	
	public Object getExpr1Source() {
		return _expr1Source;
	}
	
	public Object getExpr2Source() {
		return _expr2Source;
	}
	
	public Expression getExpr1() {
		return _expr1;
	}
	
	public Expression getExpr2() {
		return _expr2;
	}
	
	public void validate(IEntityType entityType) {
		if (_expr1Source == null) {
			throw new RuntimeException("Unable to validate 1st expression: " + this._expr1Source);
		}
		
		this._expr1 = Expression.createLHSExpression(_expr1Source, entityType);
		
		if (_op == Operator.In && !(_expr2Source instanceof List)) {
			throw new RuntimeException("The 'in' operator requires that its right hand argument be an array");
		}
		
		this._expr2 = Expression.createRHSExpression(_expr2Source, entityType, this._expr1.getDataType());
	}

}
