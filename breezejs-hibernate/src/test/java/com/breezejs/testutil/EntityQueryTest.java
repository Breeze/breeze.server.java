package com.breezejs.testutil;

import java.util.List;
import java.util.Map;

import com.breezejs.query.EntityQuery;
import com.breezejs.query.OrderByClause;
import com.breezejs.query.OrderByClause.OrderByItem;
import com.breezejs.util.JsonGson;

import junit.framework.TestCase;

public class EntityQueryTest extends TestCase {
	protected void setUp() throws Exception {
		super.setUp();
		
	}
	
	
	public void testParseWhere() {
		String jsFrom = "'resourceName': 'foo'";
		String jsWhere = "'where': { 'freight': { '>': 100}, 'rowVersion': { 'lt': 10}, 'shippedDate': '2015-02-09T00:00:00' }";
		String json = "{" + jsFrom + "," + jsWhere + "}".replace("'", "\""); 
		
		EntityQuery eq = new EntityQuery(json);
		assertTrue(eq != null);
	}
	
	public void testParseOrderBy() {
		String jsFrom = "'resourceName': 'order'";
		String jsOrderBy = "'orderBy': ['freight', 'employee.firstName desc']";
		String json = "{" + jsFrom + "," + jsOrderBy + "}".replace("'", "\""); 
		
		EntityQuery eq = new EntityQuery(json);
		OrderByClause obc = eq.getOrderByClause();
		List<OrderByItem> obItems = obc.getOrderByItems();
		assertTrue(obItems.size() == 2);
		assertTrue(obItems.get(0).getPropertyPath().equals("freight"));
		assertTrue(obItems.get(0).isDesc() == false);
		assertTrue(obItems.get(1).getPropertyPath().equals("employee.firstName"));
		assertTrue(obItems.get(1).isDesc() == true);
	}

	
}
