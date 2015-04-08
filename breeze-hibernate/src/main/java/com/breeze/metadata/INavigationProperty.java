package com.breeze.metadata;


public interface INavigationProperty extends IProperty {
	public IEntityType getEntityType();
	public boolean isScalar();
	public String[] getInvForeignKeyNames();
}