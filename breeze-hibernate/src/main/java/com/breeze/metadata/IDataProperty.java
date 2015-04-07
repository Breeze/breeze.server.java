package com.breeze.metadata;

public interface IDataProperty extends IProperty {
	public DataType getDataType();
	public IEntityType getComplexType();
	public boolean isKeyProperty();
}
