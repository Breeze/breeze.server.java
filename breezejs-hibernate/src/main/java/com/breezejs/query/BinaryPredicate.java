package com.breezejs.query;

public class BinaryPredicate extends Predicate {
	private Operator _op;
	private Expression _expr1;
	private Expression _expr2;
	
	public BinaryPredicate(Operator op, Expression expr1, Expression expr2) {
		_op = op;
		_expr1 = expr1;
		_expr2 = expr2;
	}
	
	public Operator getOperator() {
		return _op;
	}
	
	public Expression getExpression1() {
		return _expr1;
	}
	
	public Expression getExpression2() {
		return _expr2;
	}
}
