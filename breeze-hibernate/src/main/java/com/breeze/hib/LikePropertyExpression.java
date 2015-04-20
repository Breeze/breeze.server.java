package com.breeze.hib;


import java.util.ArrayList;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.criterion.CriteriaQuery;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.engine.spi.TypedValue;
import org.hibernate.internal.util.StringHelper;

/**
 * superclass for comparisons between two properties (with like clauses)
 *
 * 
 */
class LikePropertyExpression implements Criterion {
    private static final long serialVersionUID = 1L;
	private static final TypedValue[] NO_TYPED_VALUES = new TypedValue[0];

	private final String propertyName;
	private final String otherPropertyName;
	private final MatchMode mode;

	protected LikePropertyExpression(String propertyName, String otherPropertyName, MatchMode mode) {
		this.propertyName = propertyName;
		this.otherPropertyName = otherPropertyName;
		this.mode = mode;
	}


	@Override
	public String toSqlString(Criteria criteria, CriteriaQuery criteriaQuery) throws HibernateException {
		final String[] lhsColumns = criteriaQuery.findColumns( propertyName, criteria );
		final String[] rhsColumns = criteriaQuery.findColumns( otherPropertyName, criteria );

		List<String> comparisons = new ArrayList<String>(); 
		for (int i = 0; i<lhsColumns.length; i++) {
			String comp = formatExpr(lhsColumns[0], rhsColumns[0], mode);
			comparisons.add(comp);
			
		}
		if ( comparisons.size() > 1 ) {
			return '(' + StringHelper.join( " and ", comparisons.toArray(new String[comparisons.size()]) ) + ')';
		} else {
			return comparisons.get(0);
		}
	}

	@Override
	public TypedValue[] getTypedValues(Criteria criteria, CriteriaQuery criteriaQuery) {
		return NO_TYPED_VALUES;
	}
	
	private String formatExpr(String p1, String p2, MatchMode mode) {
		String pct = "'%'";
		String expr = p1 + " like ";
		if (mode == MatchMode.ANYWHERE) {
			return expr + "concat(" + pct + "," + p2 + "," + pct + ")";  
		} else if (mode == MatchMode.START) {
			return expr + "concat(" + p2 + "," + pct + ")";
		} else if (mode == MatchMode.END) {
			return expr + "concat(" + pct + "," + p2 + ")";
		} else if (mode == MatchMode.EXACT) {
			return expr + p2;
		} else { 
			throw new RuntimeException("Unrecognized MatchMode: " + mode);
		}
	}

	@Override
	public String toString() {
		return propertyName + " like "  + otherPropertyName + "( MatchMode=" + mode.toString() + ")";
	}

}
