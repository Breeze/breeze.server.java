package com.breezejs.metadata;

public interface IDataProperty extends IProperty {
	public DataType getDataType();
	public IEntityType getComplexType();
}
