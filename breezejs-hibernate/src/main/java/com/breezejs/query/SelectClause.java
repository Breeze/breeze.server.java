package com.breezejs.query;

import java.util.ArrayList;
import java.util.List;

import com.breezejs.metadata.IEntityType;
import com.breezejs.metadata.IProperty;
import com.breezejs.metadata.MetadataHelper;
import com.breezejs.util.StringFns;

public class SelectClause {
	private String _source;
	private List<String> _propertyPaths;
	private List<IProperty> _properties;
	
	public static final SelectClause fromString(String source) {
		return (source == null) ? null : new SelectClause(source);
	}
	
	public SelectClause(String source) {
		_source = source;
		_propertyPaths = StringFns.ToList(source);
	}
	
	public String getSource() {
		return _source;
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
