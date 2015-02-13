package com.breezejs.query;

import java.util.List;

public class AndOrPredicate extends Predicate {
	private Operator _op;
	private List<Predicate> _predicates;
	
	public AndOrPredicate(Operator op, List<Predicate> predicates) {
		_op = op;
		_predicates = predicates;
	}
	
	public Operator getOperator() {
		return _op;
	}
	public List<Predicate> getPredicates() {
		return _predicates;
	}
	
	public void validate(ExpressionContext exprContext) {
		for (Predicate pred: _predicates ) {
			pred.validate(exprContext);
		}
	}
}
