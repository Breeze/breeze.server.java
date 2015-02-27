package com.breezejs.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.breezejs.metadata.IEntityType;

public class AndOrPredicate extends Predicate {
	private Operator _op;
	private List<Predicate> _predicates;
	
	public AndOrPredicate(Operator op, Predicate... predicates) {
	    this(op, Arrays.asList(predicates));
	}
	
	public AndOrPredicate(Operator op, List<Predicate> predicates) {
		_op = op;
		_predicates = predicates;
	}
	
	public Operator getOperator() {
		return _op;
	}
	public List<Predicate> getPredicates() {
		return new ArrayList<Predicate>(_predicates);
	}
	
	public void validate(IEntityType entityType) {
		for (Predicate pred: _predicates ) {
			pred.validate(entityType);
		}
	}
}
