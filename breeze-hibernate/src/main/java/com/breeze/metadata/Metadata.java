package com.breeze.metadata;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class Metadata extends LinkedHashMap<String, Object> {

	private static final long serialVersionUID = 1L;

	/**
	 * Map of navigation property to foreign key property.  For example, 
	 * if the Order entity has a Customer navigation property, the map would contain
	 * "Models.NorthwindIB.NH.Order.Customer": "CustomerID"
	 */
	public HashMap<String, String> foreignKeyMap;
}
