package com.breezejs.testutil;



import java.util.Map;

import com.breezejs.query.Predicate;
import com.breezejs.util.JsonGson;

import junit.framework.TestCase;

public class PredicateTest extends TestCase {
	protected void setUp() throws Exception {
		super.setUp();
		
	}
	
	public void test1() {
		 String pJson = "{ freight: { '>' : 100}}";
		 Map map = JsonGson.fromJson(pJson);
		 Predicate pred = Predicate.predicateFromMap(map);
		 assertTrue(pred != null);
		 
	}
}
