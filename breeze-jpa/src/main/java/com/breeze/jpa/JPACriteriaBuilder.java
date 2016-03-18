package com.breeze.jpa;

import java.util.List;

import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Root;

import com.breeze.metadata.IEntityType;
import com.breeze.query.BinaryPredicate;
import com.breeze.query.EntityQuery;
import com.breeze.query.Expression;
import com.breeze.query.LitExpression;
import com.breeze.query.Operator;
import com.breeze.query.Predicate;
import com.breeze.query.PropExpression;

public class JPACriteriaBuilder {

//    private CriteriaAliasBuilder _aliasBuilder;
    @SuppressWarnings("unused")
    private EntityQuery _entityQuery;
    private IEntityType _entityType;
    private int _subqCount = 0;
    private CriteriaBuilder _cb;
    private Root<?> _root;
    
    private JPACriteriaBuilder() {
    }
    
    public static JPACriteriaBuilder create(CriteriaQuery<?> crit, CriteriaBuilder criteriaBuilder, IEntityType entityType, EntityQuery entityQuery) {
        JPACriteriaBuilder critBuilder = new JPACriteriaBuilder();
        critBuilder.updateCriteria(crit, criteriaBuilder, entityType, entityQuery);
        return critBuilder;
    }
    

    /**
     * @param crit
     *      a Criteria object that will be updated to match the entityQuery
     * @param entityQuery
     *      the EntityQuery object from which the criteria should be updated. 
     */
    private void updateCriteria(CriteriaQuery<?> crit, CriteriaBuilder criteriaBuilder, IEntityType entityType, EntityQuery entityQuery) {
        _entityType = entityType;
        _entityQuery = entityQuery;
        _cb = criteriaBuilder;
//        _aliasBuilder = new CriteriaAliasBuilder();

        _root = crit.from(crit.getResultType());
        addWhere(crit, entityQuery.getWherePredicate());

//        addSelect(crit, entityQuery.getSelectClause());

//        addOrderBy(crit, entityQuery.getOrderByClause());
    }
    
    
    @SuppressWarnings("unchecked")
    private void addWhere(CriteriaQuery<?> crit, Predicate wherePred) {
        if (wherePred == null) return;
//        CriteriaWrapper critWrapper = new CriteriaWrapper(crit, _entityType);
//        Criterion cr = toCriterion(crit, wherePred, null);
//        crit.add(cr);
        BinaryPredicate breezePred = (BinaryPredicate) wherePred;
        
        Operator op = breezePred.getOperator();
        String symbol = op.getName();
        Expression expr1 = breezePred.getExpr1();
        Expression expr2 = breezePred.getExpr2();
        String contextAlias = null;
        javax.persistence.criteria.Predicate xpred;
        
        if (expr1 instanceof PropExpression) {
            PropExpression pexpr1 = (PropExpression) expr1;
            String propPath = pexpr1.getPropertyPath();
            String propName;
            if (pexpr1.getProperty().getParentType().isComplexType()) {
                // don't process the property path in this case.
                propName = propPath;
            } else {
                propName = propPath; //crit. _aliasBuilder.getPropertyName(crit, propPath);
            }

            propName = (contextAlias == null) ? propName : contextAlias + "."
                    + propName;
            Path path = _root.get(propName);
            if (expr2 instanceof LitExpression) {
                Object value = ((LitExpression) expr2).getValue();
                if (value == null) {
                    if (op == Operator.Equals) {
                        xpred = _cb.isNull(path);
                    } else if (op == Operator.NotEquals) {
                        xpred = _cb.isNotNull(path);
                    } else {
                        throw new RuntimeException("Binary Predicate with a null value and the "
                                + op.getName() + "operator is not supported .");
                    }
                } else if (op == Operator.Equals) {
                    xpred = _cb.equal(path, value);
                } else if (op == Operator.NotEquals) {
                    xpred = _cb.notEqual(path, value);
                } else if (op == Operator.GreaterThan) {
                    xpred = _cb.greaterThan(_root.<Comparable>get(propName), (Comparable) value);
                } else if (op == Operator.GreaterThanOrEqual) {
                    xpred = _cb.greaterThanOrEqualTo(_root.<Comparable>get(propName), (Comparable) value);
                } else if (op == Operator.LessThan) {
                    xpred = _cb.lessThan(_root.<Comparable>get(propName), (Comparable) value);
                } else if (op == Operator.LessThanOrEqual) {
                    xpred = _cb.lessThanOrEqualTo(_root.<Comparable>get(propName), (Comparable) value);
                } else if (op == Operator.In) {
                    xpred = path.in((List) value);
                } else if (op == Operator.StartsWith) {
                    xpred = _cb.like(path, "" + value + "%");
                } else if (op == Operator.EndsWith) {
                    xpred = _cb.like(path, "%" + value);
                } else if (op == Operator.Contains) {
                    xpred = _cb.like(path, "%" + value + "%");
                } else {
                    throw new RuntimeException("Binary Predicate with the "
                            + op.getName() + "operator is not yet supported.");
                }
            } else {
                String otherPropPath = ((PropExpression) expr2)
                        .getPropertyPath();
//                if (symbol != null) {
//                    cr = new PropertyExpression(propName, otherPropPath, symbol);
//                } else if (op == Operator.StartsWith) {
//                    cr = new LikePropertyExpression(propName, otherPropPath,
//                            MatchMode.START);
//                } else if (op == Operator.EndsWith) {
//                    cr = new LikePropertyExpression(propName, otherPropPath,
//                            MatchMode.END);
//                } else if (op == Operator.Contains) {
//                    cr = new LikePropertyExpression(propName, otherPropPath,
//                            MatchMode.ANYWHERE);
//                } else {
                    throw new RuntimeException("Property comparison with the "
                            + op.getName() + "operator is not yet supported.");
//                }

            }
            crit.where(xpred);
            return;
        } else {
            throw new RuntimeException(
                    "Function expressions not yet supported.");
        }
        
    }
    
}
