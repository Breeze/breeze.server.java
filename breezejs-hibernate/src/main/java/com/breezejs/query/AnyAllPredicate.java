package com.breezejs.query;

import java.util.List;

import com.breezejs.metadata.INavigationProperty;
import com.breezejs.metadata.IProperty;

public class AnyAllPredicate extends Predicate {
	private Operator _op;
	private Object _exprSource;
	private Expression _expr; // calculated as a result of validate;
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
	
	public void validate(ExpressionContext exprContext) {
		this._expr = Expression.createPropOrFnExpr(_exprSource, exprContext);
		if (!(this._expr instanceof PropExpression)) {
			throw new RuntimeException("The first expression of this AnyAllPredicate must be a PropertyExpression");
		}
		PropExpression pexpr = (PropExpression) this._expr;
		IProperty prop =  pexpr.getProperty();
		if (!(prop instanceof INavigationProperty)) {
			throw new RuntimeException("The first expression of this AnyAllPredicate must be a Navigation PropertyExpression");
		}
		INavigationProperty nprop = (INavigationProperty) prop;
		ExpressionContext nextExprContext = new ExpressionContext(nprop.getEntityType(), exprContext.usesNameOnServer);
		this._predicate.validate(nextExprContext);
		
	}
}
