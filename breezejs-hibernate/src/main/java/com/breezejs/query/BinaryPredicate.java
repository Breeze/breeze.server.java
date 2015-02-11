package com.breezejs.query;

public class BinaryPredicate extends Predicate {
	private Operator _op;
	private Object _expr1Source;
	private Object _expr2Source;
	
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
}
