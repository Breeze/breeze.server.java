package com.breeze.metadata;

import java.util.List;

public interface INavigationProperty extends IProperty {
	public IEntityType getEntityType();
	public boolean isScalar();
	public String[] getInvForeignKeyNames();
}