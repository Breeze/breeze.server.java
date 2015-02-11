package com.breezejs.query;

import java.util.List;

public class AnyAllPredicate extends Predicate {
	private Operator _op;
	private Expression _expr;
	private Predicate _predicate;
	
	public AnyAllPredicate(Operator op, Expression expr, Predicate predicate) {
		_op = op;
		_predicate = predicate;
	}
	
	public Operator getOperator() {
		return _op;
	}
	
	public Expression getExpressionr() {
		return _expr;
	}
	
	public Predicate getPredicate() {
		return _predicate;
	}
}
