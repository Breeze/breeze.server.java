package com.breeze.query;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.breeze.metadata.IEntityType;

/**
 * @author IdeaBlade
 *
 */
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
		return Collections.unmodifiableList(_predicates);
	}
	
	public void validate(IEntityType entityType) {
		for (Predicate pred: _predicates ) {
			pred.validate(entityType);
		}
	}
}
