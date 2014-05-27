package com.breezejs.hib;

import org.hibernate.criterion.Criterion;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.producer.resources.OptionsQueryParser;

import junit.framework.TestCase;

/**
 * 
 * @author Steve
 * @see http://www.odata.org/documentation/odata-version-2-0/uri-conventions/#FilterSystemQueryOption
 */
public class CritieriaGeneratorTest extends TestCase {

	CriteriaGenerator generator;
	
	protected void setUp() throws Exception {
		super.setUp();
		this.generator = new CriteriaGenerator();
	}
	
	private void check(String filterString, String matchString) {
		BoolCommonExpression expression = OptionsQueryParser.parseFilter(filterString);		
		Criterion criterion = generator.toCriterion(expression);
		String critString = criterion.toString();
		assertEquals(matchString, critString);
	}

	public void testEqual() {
		check("Address/City eq 'Redmond'", "Address.City=Redmond");
	}
	public void testNotEqual() {
		check("Address/City ne 'London'", "Address.City<>London");
	}
	public void testGreaterThan() {
		check("Price gt 20", "Price>20");
	}
	public void testGreaterThanOrEqual() {
		check("Price ge 10", "Price>=10");
	}
	public void testLessThan() {
		check("Price lt 20", "Price<20");
	}
	public void testLessThanOrEqual() {
		check("Price le 100", "Price<=100");
	}
	public void testLogicalAnd() {
		check("Price le 200 and Price gt '3.5'", "Price<=200 and Price>3.5");
	}
	public void testLogicalOr() {
		check("Price le '3.5' or Price gt '200'", "Price<=3.5 or Price>200");
	}
	public void testLogicalNot() {
		check("not endswith(Description,'milk')", "not Description like %milk");
	}
	
}
