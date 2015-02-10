package com.breezejs.testutil;

import java.util.Map;

import com.breezejs.query.EntityQuery;
import com.breezejs.util.JsonGson;

import junit.framework.TestCase;

public class EntityQueryTest extends TestCase {
	protected void setUp() throws Exception {
		super.setUp();
		
	}
	
	
	public void testDeserializeSimple() {
		String jsFrom = "'resourceName': 'foo'";
		String jsWhere = "'where': { 'freight': { '>': 100}, 'rowVersion': { 'lt': 10}, 'shippedDate': '2015-02-09T00:00:00' }";
		String json = "{" + jsFrom + "," + jsWhere + "}".replace("'", "\""); 
		
				
		EntityQuery eq = new EntityQuery(json);
		assertTrue(eq != null);
	}

}
