package com.breezejs;

import junit.framework.TestCase;

public class OdataParametersTest extends TestCase {

	public void testParse() {
		OdataParameters p;
		
		p = OdataParameters.parse("?$orderby=companyName&$top=10&$skip=20&$inlinecount=allpages");
		assertEquals("companyName", p.orderby);
		assertEquals(10, p.top);
		assertEquals(20, p.skip);
		assertEquals("allpages", p.inlinecount);
		assertTrue(p.hasInlineCount());
		assertEquals(p.orderbys().length, 1);
		assertEquals(p.orderbys()[0], "companyName");
		assertNull(p.expands());
		assertNull(p.expand);
		assertNull(p.filter);
		assertNull(p.format);
		assertNull(p.select);

		p = OdataParameters.parse("?$top=5&$filter=Country eq 'Brazil'");
		assertEquals(5, p.top);
		assertEquals("Country eq 'Brazil'", p.filter);
		assertEquals(0, p.skip);
		assertFalse(p.hasInlineCount());
		assertNull(p.orderbys());
		assertNull(p.expands());
		assertNull(p.inlinecount);
		assertNull(p.expand);
		assertNull(p.format);
		assertNull(p.select);
		
		p = OdataParameters.parse("http://localhost:7149/breeze/DemoNH/Customers?$top=3&$expand=Orders");
		assertEquals(3, p.top);
		assertNull(p.filter);
		assertEquals(0, p.skip);
		assertFalse(p.hasInlineCount());
		assertEquals(p.expand, "Orders");
		assertEquals(p.expands().length, 1);
		assertEquals(p.expands()[0], "Orders");
		assertNull(p.orderbys());
		assertNull(p.inlinecount);
		assertNull(p.format);
		assertNull(p.select);
		
		p = OdataParameters.parse("$top=3&$select=Country,PostalCode&$inlinecount=allpages");
		assertEquals(3, p.top);
		assertNull(p.filter);
		assertEquals(0, p.skip);
		assertTrue(p.hasInlineCount());
		assertNull(p.orderbys());
		assertNull(p.expands());
		assertNull(p.format);
		assertEquals(p.select, "Country,PostalCode");
		assertEquals(p.selects().length, 2);
		assertEquals(p.selects()[0], "Country");
		assertEquals(p.selects()[1], "PostalCode");
		
	}

	public void testApply() {
		OdataParameters p = new OdataParameters();
		assertEquals(0, p.skip);
		OdataParameters.apply(p, "skip", "15");
		assertEquals(0, p.skip);
		OdataParameters.apply(p, "$skip", "15");
		assertEquals(15, p.skip);
	}

	public void testHasInlineCount() {
		OdataParameters p = new OdataParameters();
		assertFalse(p.hasInlineCount());
		p.inlinecount = "junk";
		assertFalse(p.hasInlineCount());
		p.inlinecount = "allpages";
		assertTrue(p.hasInlineCount());
		p.inlinecount = "none";
		assertFalse(p.hasInlineCount());
	}

	public void testToString() {
		String url = "$top=3&$select=Country,PostalCode&$inlinecount=allpages";
		OdataParameters p = OdataParameters.parse(url);
		String s = p.toString();
		
		assertTrue(s.indexOf("$top=3") >= 0);
		assertTrue(s.indexOf("$select=Country,PostalCode") >= 0);
		assertTrue(s.indexOf("$inlinecount=allpages") >= 0);
	}

	public void testToArray() {
		String s = "this,is,my,array";
		String[] a = OdataParameters.toArray(s);
		assertEquals(4, a.length);
		assertEquals("this", a[0]);
		assertEquals("is", a[1]);
		assertEquals("my", a[2]);
		assertEquals("array", a[3]);
	}

	public void testExpands() {
		OdataParameters p = new OdataParameters();
		p.expand = "Customer,Employee,LineItems/Product";
		String[] expands = p.expands();
		assertEquals(3, expands.length);
		assertEquals("Customer", expands[0]);
		assertEquals("Employee", expands[1]);
		assertEquals("LineItems/Product", expands[2]);
	}


}
