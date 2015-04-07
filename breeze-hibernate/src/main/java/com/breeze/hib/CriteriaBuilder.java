package com.breeze.hib;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Junction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.ProjectionList;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.criterion.Subqueries;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.CriteriaImpl.OrderEntry;
import org.hibernate.transform.Transformers;

import com.breeze.metadata.IDataProperty;
import com.breeze.metadata.IEntityType;
import com.breeze.metadata.INavigationProperty;
import com.breeze.metadata.IProperty;
import com.breeze.metadata.MetadataHelper;
import com.breeze.query.AndOrPredicate;
import com.breeze.query.AnyAllPredicate;
import com.breeze.query.BinaryOperator;
import com.breeze.query.BinaryPredicate;
import com.breeze.query.EntityQuery;
import com.breeze.query.Expression;
import com.breeze.query.LitExpression;
import com.breeze.query.Operator;
import com.breeze.query.OrderByClause;
import com.breeze.query.OrderByClause.OrderByItem;
import com.breeze.query.Predicate;
import com.breeze.query.PropExpression;
import com.breeze.query.SelectClause;
import com.breeze.query.UnaryPredicate;

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
    private EntityQuery _entityQuery;
    private IEntityType _entityType;
    private int _subqCount = 0;

    public CriteriaBuilder(IEntityType entityType) {
        _entityType = entityType;
        _aliasBuilder = new CriteriaAliasBuilder(entityType);

    }

    // TODO: handle 'All'
    // TODO: fix select with nonscalar nav props.

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
        _entityQuery = entityQuery;
        Integer takeCount = entityQuery.getTakeCount();
        if (takeCount != null && takeCount == 0) {
            // Hack because setMaxResults(0) returns all records instead of
            // none.
            // so we do then equiv of skip 'everything' instead.
            // and then insure that we don't overwrite this with another skip.
            crit.setFirstResult(Integer.MAX_VALUE);
        } else {
            if (takeCount != null) {
                crit.setMaxResults(takeCount);
            }
            Integer skipCount = entityQuery.getSkipCount();
            if (skipCount != null) {
                crit.setFirstResult(skipCount);
            }
        }

        addWhere(crit, entityQuery.getWherePredicate());

        addSelect(crit, entityQuery.getSelectClause());

        addOrderBy(crit, entityQuery.getOrderByClause());

    }

    public boolean containsNavPropertyProxy() {
        return _aliasBuilder.containsNavPropertyProxy();
    }

    private void addWhere(Criteria crit, Predicate wherePred) {
        if (wherePred == null) return;
        Criterion cr = toCriterion(crit, wherePred, null);
        crit.add(cr);
    }

    private void addSelect(Criteria crit, SelectClause selectClause) {
        if (selectClause == null) return;
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
        if (obc == null) return;

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

        return junction;
    }

    private Criterion createCriterion(Criteria crit, AnyAllPredicate pred,
            String contextAlias) {
        // throw new
        // RuntimeException("Any/All predicates are not yet supported.");
        // May need additional Metadata for this. In order to construct
        // an EXISTS subquery we need to have the join columns.
        Operator op = pred.getOperator();
        //        if (op == Operator.Any) {

        //            PropExpression pexpr = pred.getExpr();
        //            Predicate nextPred = pred.getPredicate();
        //            // TODO: should check that propertyPath below is not nested - i.e. a
        //            // simple navigation propertyName
        //            String propertyPath = pexpr.getPropertyPath();
        //            String nextContextAlias = _aliasBuilder.getSimpleAlias(crit, propertyPath);
        //            Criterion cr = toCriterion(crit, nextPred, nextContextAlias);
        //            return cr;
        //        }
        if (op == Operator.Any) {
            PropExpression pexpr = pred.getExpr();
            Predicate nextPred = pred.getPredicate();
            // TODO: should check that propertyPath below is not nested - i.e. a
            // simple navigation propertyName
            String propertyPath = pexpr.getPropertyPath();
            INavigationProperty navProp = (INavigationProperty) MetadataHelper.getPropertyFromPath(propertyPath, _entityType);
            String[] subtypeFkNames = navProp.getInvForeignKeyNames();
            IEntityType subtype = navProp.getEntityType();
            List<IDataProperty> rootKeyProperties = _entityType.getKeyProperties();
            List<IDataProperty> subtypeKeyProperties = subtype.getKeyProperties();

            Class subqueryClass = MetadataHelper.lookupClass(subtype.getName());
            String subqAlias = "subq" + _subqCount++;
            DetachedCriteria detCrit = DetachedCriteria.forClass(subqueryClass, subqAlias);

            Criterion subCrit = toCriterion(crit, nextPred, subqAlias);
            detCrit.add(subCrit);
            Criterion joinCrit = new PropertyExpression(subqAlias + "." + subtypeFkNames[0], "root."
                    + rootKeyProperties.get(0).getName(), "=");
            detCrit.add(joinCrit);
            detCrit.setProjection(Projections.property(subqAlias + "." + subtypeKeyProperties.get(0).getName()));
            Criterion cr = Subqueries.exists(detCrit);
            return cr;
        }

        throw new RuntimeException("'All' predicates are not yet supported.");

        // Criteria criteria = session.createCriteria(CD.class, "cd");
        // criteria.createAlias("cd.tracks", "track");
        // criteria.add(Restrictions.eq("track.title", "someTitle"));

        // IF USING exists then we need join columns

        // Criteria criteria = session.createCriteria(CD.class, "cd");
        // DetachedCriteria trackCriteria = DetachedCriteria.forClass(Track.class, "track");
        // trackCriteria.add(Restrictions.eq("track.title", "SomeTitle"));
        // trackCriteria.add(Restrictions.propertyEq("track.cd.id", "cd.id"));
        // trackCriteria.setProjection(Projections.property("track.title"));
        // criteria.add(Subqueries.exists(trackCriteria));

        // exists need join columns
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
                    if (op == Operator.Equals) {
                        cr = Restrictions.isNull(propName);
                    } else if (op == Operator.NotEquals) {
                        cr = Restrictions.isNotNull(propName);
                    } else {
                        throw new RuntimeException("Binary Predicate with a null value and the "
                                + op.getName() + "operator is not supported .");
                    }
                } else if (symbol != null) {
                    cr = new OperatorExpression(propName, value, symbol);
                } else if (op == Operator.In) {
                    cr = Restrictions.in(propName, ((List) value).toArray());
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
                // javax.persistence.criteria.CriteriaBuilder x = new
                // javax.persistence.criteria.CriteriaBuilder();
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
