package com.breezejs.hib;

import java.util.HashMap;
import java.util.Iterator;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.internal.CriteriaImpl;
import org.hibernate.internal.CriteriaImpl.OrderEntry;

import com.breezejs.OdataParameters;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

/**
 * Converts OData parameters into Hibernate Criteria.
 * @author Steve
 * @see http://docs.jboss.org/hibernate/core/3.6/javadocs/org/hibernate/Criteria.html
 */
public class OdataCriteria {
	
	// Maps OData operators to Hibernate criteria operators
	private static final HashMap<String, String> operatorMap = new HashMap<String, String>();
	static {
		operatorMap.put("eq", "=");
		operatorMap.put("ne", "<>");
		operatorMap.put("gt", ">");
		operatorMap.put("ge", ">=");
		operatorMap.put("lt", "<");
		operatorMap.put("le", "<=");
//		operatorMap.put("and", "and");
//		operatorMap.put("or", "or");
//		operatorMap.put("not", "not");
	}
	
	public static final String WHITESPACE = "\\s+";
	
	/**
	 * Apply the OData $top, $skip, $orderby, and (crude) $filter parameters to the Criteria
	 * @param crit
	 * @param op
	 * @return the same Criteria that was passed in, with operations added.
	 */
	public static Criteria applyParameters(Criteria crit, OdataParameters op)
	{
		if (op == null) return crit;
    	if (op.top > 0) crit.setMaxResults(op.top);
    	if (op.skip > 0) crit.setFirstResult(op.skip);
    	// OLD Code
    	/* if (op.orderby != null) {
    		for(String s: op.orderbys()) {
    			String[] seg = s.split(WHITESPACE, 2);
    			String field = seg[0].replace('/', '.');
    			if (seg.length == 2 && "desc".equalsIgnoreCase(seg[1])) {
    				crit.addOrder(Order.desc(field));
    			} else {
    				crit.addOrder(Order.asc(field));
    			}
    		}
    	}*/
    	addOrderBy(crit, op);
    	if (op.filter != null) {
    		addFilter(crit, op.filter);
    	}
		
		
		return crit;
	}
	
	// Basically doing something like this for nested props
	/* Criteria criteria = session.createCriteria(OrderDetail.class)
			 .createAlias("product", "product_1")
			 .createAlias("product_1.category", "category_2")
			 .addOrder( Order.desc(category_2.name");
    */
	private static void addOrderBy(Criteria crit, OdataParameters op) {
		if (op.orderby == null) return;

		for(String s: op.orderbys()) {
			String[] segs = s.split(WHITESPACE, 2);
			String field = segs[0].replace('/', '.');
			String[] fields = field.split("\\.");
			
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
			Boolean isDesc = (segs.length == 2 && "desc".equalsIgnoreCase(segs[1]));
			if (isDesc) {
				nextCrit.addOrder(Order.desc(nextField));
			} else {
				nextCrit.addOrder(Order.asc(nextField));
			}
		}

	}
	
	/**
	 * Apply the OData $inlinecount and (crude) $filter parameters to the Criteria.
	 * This creates a filter with a rowCount projection, so $skip, $top, $orderby, $select, $expand
	 * are meaningless here.
	 * @param crit a Criteria object.  Should be new or contain only filters when passed in.
	 * @param op OdataParameters object - only op.inlineCount and op.filter are used.
	 * @return the same Criteria that was passed in, with operations added.
	 */
	public static Criteria applyInlineCount(Criteria crit, OdataParameters op)
	{
    	if (op.filter != null) {
    		addFilter(crit, op.filter);
    	}
		crit.setProjection( Projections.rowCount());
		return crit;
	}

	/**
	 * Apply the OData $inlinecount to the (already filtered) Criteria.
	 * Removes $skip and $top and $orderby operations and adds a rowCount projection.
	 * @param crit a Criteria object.  Should already contain only filters that affect the row count.
	 * @return the same Criteria that was passed in, with operations added.
	 */
	public static Criteria applyInlineCount(Criteria crit)
	{
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

	/**
	 * Create a Criterion from a simple filter expression.  Handles OData filters of the form
	 * $filter=country eq 'Brazil'
	 * @param criteria 
	 * @param filterString OData $filter.  Only filters of the form [field] [op] [value] are supported..
	 * @return an OperatorExpression or PropertyExpression
	 */
	public static Criterion makeFilterCriterion(String filterString)
	{
		String[] filter = filterString.split(WHITESPACE, 3);
		if (filter.length != 3)
			throw new IllegalArgumentException("Filter string not handled: " + filterString);

		String field = filter[0].replace('/', '.');
		String op = filter[1].toLowerCase();
		String stringValue = filter[2];
		
		String restrictionOp = operatorMap.get(op);
		if (restrictionOp == null)
			throw new IllegalArgumentException("Filter string not handled: " + filterString);

		Object value;
		if (stringValue.charAt(0) == '\'') {
			// Remove quotes from string values
			value = stringValue.substring(1, stringValue.length() - 1);
		} else {
			// Non-quoted values are numbers or other fields
			value = Ints.tryParse(stringValue);
			if (value == null) {
				value = Longs.tryParse(stringValue);
			}
			if (value == null) {
				value = Doubles.tryParse(stringValue);
			}
			if (value == null) {
				// expression comparing two properties
				return new PropertyExpression(field, stringValue, restrictionOp);
			}
		}
		
		return new OperatorExpression(field, value, restrictionOp);
	}
	
	/**
	 * Add simple filtering to the Criteria.  Handles OData filters of the form
	 * $filter=country eq 'Brazil'
	 * @param crit 
	 * @param filterString OData $filter.  Only filters of the form [field] [op] [value] are supported..
	 */
	static void addFilter(Criteria crit, String filterString)
	{
		Criterion criterion = makeFilterCriterion(filterString);
		crit.add(criterion);
	}
	
	/*
Eq Equal /Suppliers?$filter=Address/City eq ‘Redmond’ 
Ne Not equal /Suppliers?$filter=Address/City ne ‘London’ 
Gt Greater than /Products?$filter=Price gt 20 
Ge Greater than or equal /Products?$filter=Price ge 10 
Lt Less than /Products?$filter=Price lt 20 
Le Less than or equal /Products?$filter=Price le 100 
And Logical and /Products?$filter=Price le 200 and Price gt 3.5 
Or Logical or /Products?$filter=Price le 3.5 or Price gt 200 
Not 
	 */
}
