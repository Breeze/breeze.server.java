package com.breeze.metadata;

public interface IEntityType {
	public String getName();
	public IProperty getProperty(String propertyName);
	public boolean isComplexType();
}




