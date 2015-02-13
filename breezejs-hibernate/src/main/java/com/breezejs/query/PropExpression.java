package com.breezejs.query;

import com.breezejs.metadata.DataType;
import com.breezejs.metadata.IDataProperty;
import com.breezejs.metadata.IEntityType;
import com.breezejs.metadata.INavigationProperty;
import com.breezejs.metadata.IProperty;
import com.breezejs.metadata.MetadataHelper;

public class PropExpression extends Expression {
	private String _propertyPath;
	private IProperty _property; 
	
	public PropExpression(String propertyPath, IEntityType entityType) {
		_propertyPath = propertyPath;
		_property = MetadataHelper.getPropertyFromPath(_propertyPath, entityType);
		if (_property == null) {
			throw new RuntimeException("Unable to validate propertyPath: " + _propertyPath + " on EntityType: " + entityType.getName());
		}
	}
	
	public String getPropertyPath() {
		return _propertyPath;
	}
	
	public IProperty getProperty() {
		return _property;
	}
	
	public DataType getDataType() {
		if (!(_property instanceof IDataProperty )) {
			throw new RuntimeException("This property expression returns a NavigationProperty not a DataProperty");
		}
			
		return ((IDataProperty) _property).getDataType();
	}

}
