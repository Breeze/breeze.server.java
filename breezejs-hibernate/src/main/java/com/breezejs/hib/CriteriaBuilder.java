package com.breezejs.hib;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.CriteriaImpl.OrderEntry;
import org.hibernate.transform.Transformers;

import com.breezejs.OdataParameters;
import com.breezejs.hib.CriteriaAliasBuilder.CriteriaAlias;
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
import com.breezejs.query.SelectClause;
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
	
	private CriteriaAliasBuilder _aliasBuilder;
	public CriteriaBuilder() {
		_aliasBuilder = new CriteriaAliasBuilder();
	}

	private static final HashMap<String, String> _operatorMap = new HashMap<String, String>();
	static {
		_operatorMap.put("eq", "=");
		_operatorMap.put("ne", "<>");
		_operatorMap.put("gt", ">");
		_operatorMap.put("ge", ">=");
		_operatorMap.put("lt", "<");
		_operatorMap.put("le", "<=");
	}
	
	public void updateCriteria(Criteria crit, EntityQuery entityQuery) {
		
		Integer takeCount = entityQuery.getTakeCount();
		if (takeCount != null) crit.setMaxResults(takeCount);	
		
		Integer skipCount = entityQuery.getSkipCount();
    	if (skipCount != null) crit.setFirstResult(skipCount);
    	
    	addWhere(crit, entityQuery.getWherePredicate());
    	
    	addSelect(crit, entityQuery.getSelectClause());
    	
       	addOrderBy(crit, entityQuery.getOrderByClause());
				
	}
	
	private void addWhere(Criteria crit, Predicate wherePred) {
		if (wherePred == null) return;
		CriteriaResult cr = toCriterion(crit, wherePred);
		cr.criteria.add(cr.criterion);
	}
	
	private void addSelect(Criteria crit, SelectClause selectClause) {
		if (selectClause == null) return;
		ProjectionList projList = Projections.projectionList();
		Criteria nextCrit = crit;
		for(String propertyPath : selectClause.getPropertyPaths()) {
			CriteriaAlias ca = _aliasBuilder.create(nextCrit, propertyPath);
			projList.add(Projections.property(ca.alias).as(propertyPath));
			nextCrit = ca.criteria;
		}
		nextCrit.setProjection(projList);
		nextCrit.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
	}
	
	// Basically doing something like this for nested props
	/* Criteria criteria = session.createCriteria(OrderDetail.class)
			 .createAlias("product", "product_1")
			 .createAlias("product_1.category", "category_2")
			 .addOrder( Order.desc(category_2.name");
    */
	private void addOrderBy(Criteria crit, OrderByClause obc) {
		if (obc == null) return;

		for(OrderByItem item: obc.getOrderByItems()) {
			CriteriaAlias ca = _aliasBuilder.create(crit, item.getPropertyPath());
			
			Order order = item.isDesc() ? Order.desc(ca.alias) : Order.asc(ca.alias);
			ca.criteria.addOrder(order);
		}
	}

	/**
	 * Apply the OData $inlinecount to the (already filtered) Criteria.
	 * Removes $skip and $top and $orderby operations and adds a rowCount projection.
	 * @param crit a Criteria object.  Should already contain only filters that affect the row count.
	 * @return the same Criteria that was passed in, with operations added.
	 */
	public Criteria applyInlineCount(Criteria crit) 	{
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


	
	private CriteriaResult toCriterion(Criteria crit, Predicate pred) {
		
		if (pred instanceof AndOrPredicate) {
			return  createCriterion(crit, (AndOrPredicate) pred);
		} else if (pred instanceof AnyAllPredicate) {
			return createCriterion(crit, (AnyAllPredicate) pred);
		} else if (pred instanceof BinaryPredicate) {
			return createCriterion(crit, (BinaryPredicate) pred);
		} else if (pred instanceof UnaryPredicate) {
			return createCriterion(crit, (UnaryPredicate) pred);
		} else {
			throw new RuntimeException("Unable to recognize predicate: " + pred.getOperator().getName());
		}
	}
	
	
	// { resourceName: 'Orders', where: or: [{ 'employee.lastName': 'smith }, { freight: 100 }] }
	//  createCriteria(Order.class)
	//     .createAlias('employee', 'e')
	//     .add(disjunction()
    //  	 .add(Restrictions.eq(e.lastName, 'smith')
	//       .add(Restrictions.eq(freight, 100);
	
	
	private  CriteriaResult createCriterion(Criteria crit, AndOrPredicate pred) {
		Operator op = pred.getOperator();
		Junction j = (op == Operator.And) ? Restrictions.conjunction() : Restrictions.disjunction();
		
		Criteria nextCrit = crit;
		for (Predicate subPred : pred.getPredicates()) {
			CriteriaResult cr = toCriterion(nextCrit, subPred);
			j.add(cr.criterion);
			nextCrit = cr.criteria;
		};
		return new CriteriaResult(nextCrit, j);
	}
	
	private  CriteriaResult createCriterion(Criteria crit, AnyAllPredicate pred) {
		throw new RuntimeException("Any/All predicates are not yet supported.");
		// May need additional Metadata for this. In order to construct
		// an EXISTS subquery we need to have the join columns.
		//		Operator op = pred.getOperator();
		//		PropExpression pexpr = pred.getExpr();
	}
	
	private  CriteriaResult createCriterion(Criteria crit, UnaryPredicate pred) {
		CriteriaResult cr = toCriterion(crit, pred.getPredicate());
		return new CriteriaResult(cr.criteria, Restrictions.not(cr.criterion));
	}
	
	private  CriteriaResult createCriterion(Criteria crit, BinaryPredicate pred) {
		Operator op = pred.getOperator();
		String symbol = _operatorMap.get(op.getName());
		Expression expr1 = pred.getExpr1();
		Expression expr2 = pred.getExpr2();
		Criterion c;
		if (expr1 instanceof PropExpression) {
			PropExpression pexpr1 = (PropExpression) expr1;
			String propPath = pexpr1.getPropertyPath();
			CriteriaAlias ca;
			if (pexpr1.getProperty().getParentType().isComplexType()) {
				// don't process the property path in this case.
				ca = _aliasBuilder.noAlias(crit, propPath);
			} else {
				ca = _aliasBuilder.create(crit, propPath);	
			}
			
			String alias = ca.alias;
			if (expr2 instanceof LitExpression) {
				Object value = ((LitExpression) expr2).getValue();
				if (value == null) {
					c= Restrictions.isNull(alias);
				} else if (symbol != null) {
					c = new OperatorExpression(alias, value, symbol);
				} else if (op == Operator.In) {
					c = Restrictions.in(alias, (Object[]) value);
				} else if (op == Operator.StartsWith) {
					c = Restrictions.like(alias, ((String) value), MatchMode.START );
				} else if (op == Operator.EndsWith) {
					c = Restrictions.like(alias, (String) value, MatchMode.END);
				} else if (op == Operator.Contains) {
					c = Restrictions.like(alias, ((String) value), MatchMode.ANYWHERE);
				} else {
					throw new RuntimeException("Binary Predicate with the " + op.getName() + "operator is not yet supported.");
				}
			} else {
				String otherPropPath = ((PropExpression) expr2).getPropertyPath();
				if (symbol != null) {
					c = new PropertyExpression(alias, otherPropPath, symbol);
				} else if (op == Operator.StartsWith) {
					c = new LikePropertyExpression(alias, otherPropPath, MatchMode.START );
				} else if (op == Operator.EndsWith) {
					c = new LikePropertyExpression(alias, otherPropPath, MatchMode.END);
				} else if (op == Operator.Contains) {
					c = new LikePropertyExpression(alias, otherPropPath, MatchMode.ANYWHERE);
				} else {
					throw new RuntimeException("Property comparison with the " + op.getName() + "operator is not yet supported.");
				}
			}
			return new CriteriaResult(ca.criteria, c);
		} else {
			throw new RuntimeException("Function expressions not yet supported.");
		}
		
	}
	

	
	

}	
