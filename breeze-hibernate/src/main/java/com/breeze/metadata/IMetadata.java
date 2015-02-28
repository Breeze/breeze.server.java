package com.breeze.metadata;

public interface IMetadata {
	public IEntityType getEntityType(String entityTypeName);
	public IEntityType getEntityTypeForResourceName(String resourceName);
	public IEntityType getEntityTypeForClass(Class<?> clazz);
}
