package com.breeze.query;

import java.util.List;

import com.breeze.metadata.IEntityType;
import com.breeze.metadata.INavigationProperty;
import com.breeze.metadata.IProperty;

public class AnyAllPredicate extends Predicate {
	private Operator _op;
	private Object _exprSource;
	private PropExpression _expr; // calculated as a result of validate;
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
	
	public PropExpression getExpr() {
		return _expr;
	}
	
	public Predicate getPredicate() {
		return _predicate;
	}
	
	public void validate(IEntityType entityType) {
		Expression expr = Expression.createLHSExpression(_exprSource, entityType);
		if (!(expr instanceof PropExpression)) {
			throw new RuntimeException("The first expression of this AnyAllPredicate must be a PropertyExpression");
		}
		PropExpression pexpr = (PropExpression) expr;
		IProperty prop =  pexpr.getProperty();
		if (!(prop instanceof INavigationProperty)) {
			throw new RuntimeException("The first expression of this AnyAllPredicate must be a Navigation PropertyExpression");
		}
		INavigationProperty nprop = (INavigationProperty) prop;
		this._expr = pexpr;
		this._predicate.validate(nprop.getEntityType());
		
	}
}
