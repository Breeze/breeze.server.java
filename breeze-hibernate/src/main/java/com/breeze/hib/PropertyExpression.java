package com.breeze.hib;

/**
 * Extends PropertyExpression just to offer a public constructor
 * @author Steve
 *
 */
class PropertyExpression extends org.hibernate.criterion.PropertyExpression {
	private static final long serialVersionUID = 1L;

	public PropertyExpression(String propertyName, String otherPropertyName, String op) {
		super(propertyName, otherPropertyName, op);
	}

}
