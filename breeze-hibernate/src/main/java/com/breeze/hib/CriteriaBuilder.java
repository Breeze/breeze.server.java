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
import com.breeze.metadata.MetadataHelper;
import com.breeze.query.AndOrPredicate;
import com.breeze.query.AnyAllPredicate;
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
 * @see http://docs.jboss.org/hibernate/core/3.6/javadocs/org/hibernate/Criteria.html
 */
/**
 * @author Jay
 *
 */
class CriteriaBuilder {
    
    // TODO: fix select (projection) with nonscalar nav props.  So far no idea how to do this... 

    private CriteriaAliasBuilder _aliasBuilder;
    @SuppressWarnings("unused")
    private EntityQuery _entityQuery;
    private IEntityType _entityType;
    private int _subqCount = 0;

    
    private CriteriaBuilder() {

    }
    
    public static CriteriaBuilder create(Criteria crit, IEntityType entityType, EntityQuery entityQuery) {
        CriteriaBuilder critBuilder = new CriteriaBuilder();
        critBuilder.updateCriteria(crit, entityType, entityQuery);
        return critBuilder;
    }

    /**
     * @param crit
     *      a Criteria object that will be updated to match the entityQuery
     * @param entityQuery
     *      the EntityQuery object from which the criteria should be updated. 
     */
    private void updateCriteria(Criteria crit, IEntityType entityType, EntityQuery entityQuery) {
        _entityType = entityType;
        _entityQuery = entityQuery;
        _aliasBuilder = new CriteriaAliasBuilder();
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

    
    /**
     * @return whether this 
     */
    public boolean containsNavPropertyProxy() {
        return _aliasBuilder.containsNavPropertyProxy();
    }

    private void addWhere(Criteria crit, Predicate wherePred) {
        if (wherePred == null) return;
        CriteriaWrapper critWrapper = new CriteriaWrapper(crit, _entityType);
        Criterion cr = toCriterion(critWrapper, wherePred, null);
        crit.add(cr);
    }

    private void addSelect(Criteria crit, SelectClause selectClause) {
        if (selectClause == null) return;
        ProjectionList projList = Projections.projectionList();
        CriteriaWrapper critWrapper = new CriteriaWrapper(crit, _entityType);
        for (String propertyPath : selectClause.getPropertyPaths()) {
            String propertyName = _aliasBuilder.getPropertyName(critWrapper,
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
        CriteriaWrapper critWrapper = new CriteriaWrapper(crit, _entityType);
        for (OrderByItem item : obc.getOrderByItems()) {
            String propertyName = _aliasBuilder.getPropertyName(critWrapper,
                    item.getPropertyPath());
            Order order = item.isDesc() ? Order.desc(propertyName) : Order
                    .asc(propertyName);
            crit.addOrder(order);
        }
    }



    // crit is cast as Object because it can be either a Criteria or a DetachedCriteria
    // and these two classes do not share any useful interfaces.
    private Criterion toCriterion(CriteriaWrapper crit, Predicate pred,
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

    private Criterion createCriterion(CriteriaWrapper crit, AndOrPredicate pred,
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

    private Criterion createCriterion(CriteriaWrapper crit, AnyAllPredicate pred,
            String contextAlias) {

        Operator op = pred.getOperator();

        if (op == Operator.Any) {
            DetachedCriteria detCrit = makeSubcrit(crit, pred);
            Criterion cr = Subqueries.exists(detCrit);
            return cr;
        } else if (op == Operator.All) {
            DetachedCriteria detCrit = makeSubcrit(crit, pred);
            Criterion cr = Subqueries.notExists(detCrit);
            return cr;
        }

        //   OLD logic using joins - cannot be 'inverted' to make an 'any' into an 'all though
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

        throw new RuntimeException("'All' predicates are not yet supported.");

        // Criteria criteria = session.createCriteria(CD.class, "cd");
        // criteria.createAlias("cd.tracks", "track");
        // criteria.add(Restrictions.eq("track.title", "someTitle"));

        // IF USING exists then we need join columns

        // Criteria criteria = session.createCriteria(CD.class, "cd");
        // DetachedCriteria trackCriteria = DetachedCriteria.forClass(Track.class, "track");
        // trackCriteria.add(Restrictions.eq("track.title", "SomeTitle"));
        // trackCriteria.add(Restrictions.propertyEq("track.cd_id", "cd.id"));
        // trackCriteria.setProjection(Projections.property("track.id"));
        // criteria.add(Subqueries.exists(trackCriteria));

    }

    private DetachedCriteria makeSubcrit(CriteriaWrapper crit, AnyAllPredicate pred) {

        PropExpression pexpr = pred.getExpr();
        Predicate nextPred = pred.getPredicate();
        if (pred.getOperator() == Operator.All) {
            nextPred = new UnaryPredicate(Operator.Not, nextPred);
        }

        // TODO: should check that propertyPath below is not nested - 
        // i.e. should be a simple navigation propertyName
        IEntityType parentType = pexpr.getEntityType();
        List<IDataProperty> rootKeyProperties = parentType.getKeyProperties();

        String propertyPath = pexpr.getPropertyPath();
        INavigationProperty navProp = (INavigationProperty) MetadataHelper.getPropertyFromPath(propertyPath, parentType);
        String[] subtypeFkNames = navProp.getInvForeignKeyNames();
        IEntityType subtype = navProp.getEntityType();
        List<IDataProperty> subtypeKeyProperties = subtype.getKeyProperties();

        Class subqueryClass = MetadataHelper.lookupClass(subtype.getName());
        String subqAlias = "subq" + _subqCount++;
        DetachedCriteria detCrit = DetachedCriteria.forClass(subqueryClass, subqAlias);
        CriteriaWrapper detWrapper = new CriteriaWrapper(detCrit, subtype);
        Criterion subCrit = toCriterion(detWrapper, nextPred, null);
        detCrit.add(subCrit);
        Criterion joinCrit = new PropertyExpression(
                subqAlias + "." + subtypeFkNames[0],
                crit.getAlias() + "." + rootKeyProperties.get(0).getName(),
                "=");
        detCrit.add(joinCrit);
        detCrit.setProjection(Projections.property(subqAlias + "." + subtypeKeyProperties.get(0).getName()));
        return detCrit;

    }

    private Criterion createCriterion(CriteriaWrapper crit, UnaryPredicate pred,
            String contextAlias) {
        Criterion cr = toCriterion(crit, pred.getPredicate(), contextAlias);
        return Restrictions.not(cr);
    }

    private Criterion createCriterion(CriteriaWrapper crit, BinaryPredicate pred,
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

    private static final HashMap<String, String> _operatorMap = new HashMap<String, String>();
    static {
        _operatorMap.put("eq", "=");
        _operatorMap.put("ne", "<>");
        _operatorMap.put("gt", ">");
        _operatorMap.put("ge", ">=");
        _operatorMap.put("lt", "<");
        _operatorMap.put("le", "<=");
    }
}
