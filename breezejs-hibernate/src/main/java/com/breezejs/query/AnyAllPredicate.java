package com.breezejs.query;

import java.util.List;

public class AnyAllPredicate extends Predicate {
	private Operator _op;
	private Object _exprSource;
	private Predicate _predicate;
	
	public AnyAllPredicate(Operator op, Object exprSource, Predicate predicate) {
		_op = op;
		_exprSource = exprSource;
		_predicate = predicate;
	}
	
	public Operator getOperator() {
		return _op;
	}
	
	public Object getExprSource() {
		return _exprSource;
	}
	
	public Predicate getPredicate() {
		return _predicate;
	}
}
