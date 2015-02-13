package com.breezejs.metadata;

public interface INavigationProperty extends IProperty {
	public IEntityType getEntityType();
	public boolean isScalar();
}