package com.breezejs.query;

import com.breezejs.metadata.IEntityType;

public class UnaryPredicate extends Predicate {
	private Operator _op;
	private Predicate _predicate;
	
	public UnaryPredicate(Operator op, Predicate predicate) {
		_op = op;
		_predicate = predicate;
	}
	
	public Operator getOperator() {
		return _op;
	}
	public Predicate getPredicate() {
		return _predicate;
	}
	
	public void validate(IEntityType entityType) {
		_predicate.validate(entityType);
	}
}
