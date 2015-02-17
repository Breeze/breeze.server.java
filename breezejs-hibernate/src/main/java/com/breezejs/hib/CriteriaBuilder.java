package com.breezejs.hib;

import java.util.HashMap;
import java.util.Iterator;
import org.hibernate.Criteria;

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

/**
 * Converts EntityQuery into Hibernate Criteria.
 * 
 * @author Jay
 * @see http
 *      ://docs.jboss.org/hibernate/core/3.6/javadocs/org/hibernate/Criteria.
 *      html
 */
public class CriteriaBuilder {

	private CriteriaAliasBuilder _aliasBuilder;

	public CriteriaBuilder() {
		_aliasBuilder = new CriteriaAliasBuilder();
	}

	// TODO: handle 'All'
	// TODO: fix prop 'like' prop preds
	// TODO: handle 'In' clauses

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
		if (takeCount != null && takeCount == 0) {
			// Hack because setMaxResults(0) returns all records instead of
			// none.
			// so we do then equiv of skip 'everything' instead.
			// and then insure that we don't overwrite this with another skip.
			crit.setFirstResult(Integer.MAX_VALUE);
		} else {
			if (takeCount != null)
				crit.setMaxResults(takeCount);
			Integer skipCount = entityQuery.getSkipCount();
			if (skipCount != null)
				crit.setFirstResult(skipCount);
		}

		addWhere(crit, entityQuery.getWherePredicate());

		addSelect(crit, entityQuery.getSelectClause());

		addOrderBy(crit, entityQuery.getOrderByClause());

	}

	private void addWhere(Criteria crit, Predicate wherePred) {
		if (wherePred == null)
			return;
		Criterion cr = toCriterion(crit, wherePred, null);
		crit.add(cr);
	}

	private void addSelect(Criteria crit, SelectClause selectClause) {
		if (selectClause == null)
			return;
		ProjectionList projList = Projections.projectionList();

		for (String propertyPath : selectClause.getPropertyPaths()) {
			String propertyName = _aliasBuilder.getPropertyName(crit,
					propertyPath);
			projList.add(Projections.property(propertyName).as(propertyPath));
		}
		crit.setProjection(projList);
		crit.setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
	}

	// Basically doing something like this for nested props
	/*
	 * Criteria criteria = session.createCriteria(OrderDetail.class)
	 * .createAlias("product", "product_1") .createAlias("product_1.category",
	 * "category_2") .addOrder( Order.desc(category_2.name");
	 */
	private void addOrderBy(Criteria crit, OrderByClause obc) {
		if (obc == null)
			return;

		for (OrderByItem item : obc.getOrderByItems()) {
			String propertyName = _aliasBuilder.getPropertyName(crit,
					item.getPropertyPath());
			Order order = item.isDesc() ? Order.desc(propertyName) : Order
					.asc(propertyName);
			crit.addOrder(order);
		}
	}

	/**
	 * Apply the OData $inlinecount to the (already filtered) Criteria. Removes
	 * $skip and $top and $orderby operations and adds a rowCount projection.
	 * 
	 * @param crit
	 *            a Criteria object. Should already contain only filters that
	 *            affect the row count.
	 * @return the same Criteria that was passed in, with operations added.
	 */
	public Criteria applyInlineCount(Criteria crit) {
		crit.setMaxResults(0);
		crit.setFirstResult(0);
		CriteriaImpl impl = (CriteriaImpl) crit;
		Iterator<OrderEntry> iter = impl.iterateOrderings();
		while (iter.hasNext()) {
			iter.next();
			iter.remove();
		}
		crit.setProjection(Projections.rowCount());
		return crit;
	}

	private Criterion toCriterion(Criteria crit, Predicate pred,
			String contextAlias) {

		if (pred instanceof AndOrPredicate) {
			return createCriterion(crit, (AndOrPredicate) pred, contextAlias);
		} else if (pred instanceof AnyAllPredicate) {
			return createCriterion(crit, (AnyAllPredicate) pred, contextAlias);
		} else if (pred instanceof BinaryPredicate) {
			return createCriterion(crit, (BinaryPredicate) pred, contextAlias);
		} else if (pred instanceof UnaryPredicate) {
			return createCriterion(crit, (UnaryPredicate) pred, contextAlias);
		} else {
			throw new RuntimeException("Unable to recognize predicate: "
					+ pred.getOperator().getName());
		}
	}

	// { resourceName: 'Orders', where: or: [{ 'employee.lastName': 'smith }, {
	// freight: 100 }] }
	// createCriteria(Order.class)
	// .createAlias('employee', 'e')
	// .add(disjunction()
	// .add(Restrictions.eq(e.lastName, 'smith')
	// .add(Restrictions.eq(freight, 100);

	private Criterion createCriterion(Criteria crit, AndOrPredicate pred,
			String contextAlias) {
		Operator op = pred.getOperator();
		Junction junction = (op == Operator.And) ? Restrictions.conjunction()
				: Restrictions.disjunction();

		for (Predicate subPred : pred.getPredicates()) {
			Criterion cr = toCriterion(crit, subPred, contextAlias);
			junction.add(cr);
		}
		;
		return junction;
	}

	private Criterion createCriterion(Criteria crit, AnyAllPredicate pred,
			String contextAlias) {
		// throw new
		// RuntimeException("Any/All predicates are not yet supported.");
		// May need additional Metadata for this. In order to construct
		// an EXISTS subquery we need to have the join columns.
		Operator op = pred.getOperator();
		if (op == Operator.Any) {
			PropExpression pexpr = pred.getExpr();
			Predicate nextPred = pred.getPredicate();
			// TODO: should check that propertyPath below is not nested - i.e. a simple navigation propertyName
			String propertyPath = pexpr.getPropertyPath();
			String nextContextAlias = _aliasBuilder.getSimpleAlias(crit, propertyPath);
			Criterion cr = toCriterion(crit, nextPred, nextContextAlias);
			return cr;
		}

		throw new RuntimeException("'All' predicates are not yet supported.");
		// Criteria cdCriteria = session.createCriteria(CD.class, "cd");
		// criteria.createAlias("cd.tracks", "track");
		// criteria.add(Restrictions.eq("track.title", "someTitle"));
		// criteria.setResultTransformer(DistinctRootEntityResultTransformer.INSTANCE);

		// need join columns
		// DetachedCriteria subquery = DetachedCriteria.forClass(Bar.class, "b")
		// .add(Property.forName("b.a_id").eqProperty("a.id"))
		//
		// Criteria criteria = session.createCriteria(Foo.class, "a")
		// .add(Subqueries.notExists(subquery);
	}

	private Criterion createCriterion(Criteria crit, UnaryPredicate pred,
			String contextAlias) {
		Criterion cr = toCriterion(crit, pred.getPredicate(), contextAlias);
		return Restrictions.not(cr);
	}

	private Criterion createCriterion(Criteria crit, BinaryPredicate pred,
			String contextAlias) {
		Operator op = pred.getOperator();
		String symbol = _operatorMap.get(op.getName());
		Expression expr1 = pred.getExpr1();
		Expression expr2 = pred.getExpr2();
		Criterion cr;
		if (expr1 instanceof PropExpression) {
			PropExpression pexpr1 = (PropExpression) expr1;
			String propPath = pexpr1.getPropertyPath();
			String propName;
			if (pexpr1.getProperty().getParentType().isComplexType()) {
				// don't process the property path in this case.
				propName = propPath;
			} else {
				propName = _aliasBuilder.getPropertyName(crit, propPath);
			}

			propName = (contextAlias == null) ? propName : contextAlias + "."
					+ propName;

			if (expr2 instanceof LitExpression) {
				Object value = ((LitExpression) expr2).getValue();
				if (value == null) {
					cr = Restrictions.isNull(propName);
				} else if (symbol != null) {
					cr = new OperatorExpression(propName, value, symbol);
				} else if (op == Operator.In) {
					cr = Restrictions.in(propName, (Object[]) value);
				} else if (op == Operator.StartsWith) {
					cr = Restrictions.like(propName, ((String) value),
							MatchMode.START);
				} else if (op == Operator.EndsWith) {
					cr = Restrictions.like(propName, (String) value,
							MatchMode.END);
				} else if (op == Operator.Contains) {
					cr = Restrictions.like(propName, ((String) value),
							MatchMode.ANYWHERE);
				} else {
					throw new RuntimeException("Binary Predicate with the "
							+ op.getName() + "operator is not yet supported.");
				}
			} else {
				String otherPropPath = ((PropExpression) expr2)
						.getPropertyPath();
				if (symbol != null) {
					cr = new PropertyExpression(propName, otherPropPath, symbol);
				} else if (op == Operator.StartsWith) {
					cr = new LikePropertyExpression(propName, otherPropPath,
							MatchMode.START);
				} else if (op == Operator.EndsWith) {
					cr = new LikePropertyExpression(propName, otherPropPath,
							MatchMode.END);
				} else if (op == Operator.Contains) {
					cr = new LikePropertyExpression(propName, otherPropPath,
							MatchMode.ANYWHERE);
				} else {
					throw new RuntimeException("Property comparison with the "
							+ op.getName() + "operator is not yet supported.");
				}
			}
			return cr;
		} else {
			throw new RuntimeException(
					"Function expressions not yet supported.");
		}

	}

}
