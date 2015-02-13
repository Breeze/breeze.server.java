package com.breezejs.metadata;

public interface IMetadata {
	public IEntityType getEntityTypeForResourceName(String resourceName);
	public IEntityType getEntityTypeForEntityTypeName(String typeName);
}
