package com.breeze.query;

import com.breeze.metadata.DataType;
import com.breeze.metadata.IDataProperty;
import com.breeze.metadata.IEntityType;
import com.breeze.metadata.IProperty;
import com.breeze.metadata.MetadataHelper;

public class PropExpression extends Expression {
	private String _propertyPath;
	private IProperty _property; 
	private IEntityType _entityType;
	
	public PropExpression(String propertyPath, IEntityType entityType) {
	    _entityType = entityType;
		_propertyPath = propertyPath;
		_property = MetadataHelper.getPropertyFromPath(_propertyPath, entityType);
		if (_property == null) {
			throw new RuntimeException("Unable to validate propertyPath: " + _propertyPath + " on EntityType: " + entityType.getName());
		}
	}
	
	public IEntityType getEntityType() {
	    return _entityType;
	}
	
	public String getPropertyPath() {
		return _propertyPath;
	}
	
	public IProperty getProperty() {
		return _property;
	}
	
	@Override
    public DataType getDataType() {
		if (!(_property instanceof IDataProperty )) {
			throw new RuntimeException("This property expression returns a NavigationProperty not a DataProperty");
		}
			
		return ((IDataProperty) _property).getDataType();
	}

}
