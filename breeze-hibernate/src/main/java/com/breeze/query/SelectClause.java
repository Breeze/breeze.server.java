package com.breeze.query;

import java.util.ArrayList;
import java.util.List;

import com.breeze.metadata.IEntityType;
import com.breeze.metadata.IProperty;
import com.breeze.metadata.MetadataHelper;

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
