package com.breezejs.hib;

import java.util.HashMap;
import java.util.Iterator;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.CriteriaImpl.OrderEntry;

import com.breezejs.OdataParameters;
import com.breezejs.metadata.IEntityType;
import com.breezejs.query.AndOrPredicate;
import com.breezejs.query.AnyAllPredicate;
import com.breezejs.query.BinaryPredicate;
import com.breezejs.query.EntityQuery;
import com.breezejs.query.Expression;
import com.breezejs.query.LitExpression;
import com.breezejs.query.Operator;
import com.breezejs.query.OrderByClause;
import com.breezejs.query.OrderByItem;
import com.breezejs.query.Predicate;
import com.breezejs.query.PropExpression;
import com.breezejs.query.UnaryPredicate;
import com.breezejs.util.Reflect;
import com.breezejs.util.TypeFns;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

/**
 * Converts EntityQuery into Hibernate Criteria.
 * @author Jay
 * @see http://docs.jboss.org/hibernate/core/3.6/javadocs/org/hibernate/Criteria.html
 */
public class CriteriaBuilder {

	private static final HashMap<String, String> _operatorMap = new HashMap<String, String>();
	static {
		_operatorMap.put("eq", "=");
		_operatorMap.put("ne", "<>");
		_operatorMap.put("gt", ">");
		_operatorMap.put("ge", ">=");
		_operatorMap.put("lt", "<");
		_operatorMap.put("le", "<=");
	}
	
	public static Criteria createCriteria(Session session, EntityQuery entityQuery) 	{
		IEntityType entityType = entityQuery.getEntityType();
		Class<?> clazz = Reflect.lookupEntityType(entityType.getName());
		Criteria crit = session.createCriteria(clazz);
		
		Integer takeCount = entityQuery.getTakeCount();
		if (takeCount != null) crit.setMaxResults(takeCount);	
		Integer skipCount = entityQuery.getSkipCount();
    	if (skipCount != null) crit.setFirstResult(skipCount);
    	
    	Predicate wherePredicate = entityQuery.getWherePredicate();
    	addWhere(crit, wherePredicate);
    	
    	OrderByClause orderByClause = entityQuery.getOrderByClause();
       	addOrderBy(crit, orderByClause);
		
		return crit;
	}
	
	
	/**
	 * Apply the OData $inlinecount to the (already filtered) Criteria.
	 * Removes $skip and $top and $orderby operations and adds a rowCount projection.
	 * @param crit a Criteria object.  Should already contain only filters that affect the row count.
	 * @return the same Criteria that was passed in, with operations added.
	 */
	public static Criteria applyInlineCount(Criteria crit) 	{
    	crit.setMaxResults(0);
    	crit.setFirstResult(0);
    	CriteriaImpl impl = (CriteriaImpl) crit;
    	Iterator<OrderEntry> iter = impl.iterateOrderings();
    	while (iter.hasNext()) {
    		iter.next();
    		iter.remove();
    	}
		crit.setProjection( Projections.rowCount());
		return crit;
	}

	private static void addWhere(Criteria crit, Predicate wherePred) {
		Criterion criterion = toCriterion(wherePred);
		crit.add(criterion);
	}
	
	private static Criterion toCriterion(Predicate pred) {
		if (pred instanceof AndOrPredicate) {
			return createCriterion((AndOrPredicate) pred);
		} else if (pred instanceof AnyAllPredicate) {
			return createCriterion((AnyAllPredicate) pred);
		} else if (pred instanceof BinaryPredicate) {
			return createCriterion((BinaryPredicate) pred);
		} else if (pred instanceof UnaryPredicate) {
			return createCriterion((UnaryPredicate) pred);
		} else {
			throw new RuntimeException("Unable to recognize predicate: " + pred.getOperator().getName());
		}
	}
	
	private static Criterion createCriterion(AndOrPredicate pred) {
		Operator op = pred.getOperator();
		Junction j = (op == Operator.And) ? Restrictions.conjunction() : Restrictions.disjunction();
		for (Predicate subPred : pred.getPredicates()) {
			Criterion crit = toCriterion(pred);
			j.add(crit);
		};
		return j;
	}
	
	private static Criterion createCriterion(AnyAllPredicate pred) {
		throw new RuntimeException("Any/All predicates are not yet supported.");
		// May need additional Metadata for this. In order to construct
		// an EXISTS subquery we need to have the join columns.
		//		Operator op = pred.getOperator();
		//		PropExpression pexpr = pred.getExpr();
	}
	
	private static Criterion createCriterion(UnaryPredicate pred) {
		Criterion baseCrit = toCriterion(pred.getPredicate());
		return Restrictions.not(baseCrit);
	}
	
	private static Criterion createCriterion(BinaryPredicate pred) {
		Operator op = pred.getOperator();
		String symbol = _operatorMap.get(op.getName());
		Expression expr1 = pred.getExpr1();
		Expression expr2 = pred.getExpr2();

		if (expr1 instanceof PropExpression) {
			String propPath = ((PropExpression) expr1).getPropertyPath();
			if (expr2 instanceof LitExpression) {
				Object value = ((LitExpression) expr2).getValue();
				if (value == null) {
					return Restrictions.isNull(propPath);
				} else if (symbol != null) {
					return new OperatorExpression(propPath, value, symbol);
				} else if (op == Operator.In) {
					return Restrictions.in(propPath, (Object[]) value);
				} else if (op == Operator.StartsWith) {
					return Restrictions.like(propPath, ((String) value), MatchMode.START );
				} else if (op == Operator.EndsWith) {
					return Restrictions.like(propPath, (String) value, MatchMode.END);
				} else if (op == Operator.Contains) {
					return Restrictions.like(propPath, ((String) value), MatchMode.ANYWHERE);
				} else {
					throw new RuntimeException("Binary Predicate with the " + op.getName() + "operator is not yet supported.");
				}
			} else {
				String otherPropPath = ((PropExpression) expr2).getPropertyPath();
				if (symbol != null) {
					return new PropertyExpression(propPath, otherPropPath, symbol);
				} else {
					throw new RuntimeException("Property comparison with the " + op.getName() + "operator is not yet supported.");
				}
			}
		} else {
			throw new RuntimeException("Function expressions not yet supported.");
		}
		
	}
	
	// Basically doing something like this for nested props
	/* Criteria criteria = session.createCriteria(OrderDetail.class)
			 .createAlias("product", "product_1")
			 .createAlias("product_1.category", "category_2")
			 .addOrder( Order.desc(category_2.name");
    */
	private static void addOrderBy(Criteria crit, OrderByClause obc) {
		if (obc == null) return;

		for(OrderByItem item: obc.getOrderByItems()) {

			String[] fields = item.getPropertyPath().split("\\.");
			
			String nextField;
			Criteria nextCrit = crit;
			if (fields.length == 1) {
				nextField = fields[0];
			} else {
				String nextAlias = "";
				for (int i = 0; i < fields.length - 1; i = i + 1) {
					nextField = nextAlias == "" ? fields[i] : nextAlias + "." + fields[i];
					nextAlias = fields[i] + "_" + i;
					nextCrit = nextCrit.createAlias(nextField, nextAlias);
				}
				nextField = nextAlias + "." + fields[fields.length - 1];
			}
			Order order = item.isDesc() ? Order.desc(nextField) : Order.asc(nextField);
			nextCrit.addOrder(order);
		}

	}
	



}	
