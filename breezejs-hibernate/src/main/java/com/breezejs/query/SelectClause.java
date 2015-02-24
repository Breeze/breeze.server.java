package com.breezejs.query;

import java.util.ArrayList;
import java.util.List;

import com.breezejs.metadata.IEntityType;
import com.breezejs.metadata.IProperty;
import com.breezejs.metadata.MetadataHelper;
import com.breezejs.util.StringFns;

public class SelectClause {
	private List<String> _propertyPaths;
	private List<IProperty> _properties;
	
	public static final SelectClause from(List<String> propertyPaths) {
		return (propertyPaths == null) ? null : new SelectClause(propertyPaths);
	}
	
	public SelectClause(List<String> propertyPaths) {	
		_propertyPaths = propertyPaths;
	}
	
	
	public List<String> getPropertyPaths() {
		return _propertyPaths;
	}

	public List<IProperty> getProperties() {
		return _properties;
	}
	
	public void validate(IEntityType entityType) {
	    _properties = new ArrayList<IProperty>();
	    for( String propPath: _propertyPaths) {
	    	_properties.add( MetadataHelper.getPropertyFromPath(propPath, entityType));
	    }
	}
	
}
