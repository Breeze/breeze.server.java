package com.breeze.query;


import java.util.List;


/**
 * Represents a single expand expand clause that will be part of an EntityQuery. An expand 
 * clause represents the path to other entity types via a navigation path from the current EntityType
 * for a given query. 
 * @author IdeaBlade
 *
 */
public class ExpandClause {
	private List<String> _propertyPaths;
	

	public static final ExpandClause from(List<String> propertyPaths) {
		return (propertyPaths == null) ? null : new ExpandClause(propertyPaths);
	}
	
	public ExpandClause(List<String> propertyPaths) {
		_propertyPaths = propertyPaths;
	}

	
	public List<String> getPropertyPaths() {
		return _propertyPaths;
	}

}
