package com.breezejs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.breezejs.metadata.DataType;
import com.breezejs.metadata.IDataProperty;
import com.breezejs.metadata.IEntityType;
import com.breezejs.metadata.IMetadata;
import com.breezejs.metadata.INavigationProperty;
import com.breezejs.metadata.IProperty;
import com.breezejs.metadata.MetadataHelper;

public class MetadataAdapter implements IMetadata {
	private HashMap<String, Object> _resourceEntityTypeMap;
	private HashMap<String, IEntityType> _entityTypeMap = new HashMap<String, IEntityType>();
	
	public MetadataAdapter(Metadata metadata) {
		
		List<HashMap<String, Object>> structuralTypes = (List<HashMap<String, Object>>) metadata.get("structuralTypes");
		_resourceEntityTypeMap = (HashMap<String, Object>) metadata.get("resourceEntityTypeMap");
		for (Map<String, Object> entityMap: structuralTypes) {
			IEntityType entityType = new EntityType(entityMap, this);
			_entityTypeMap.put(entityType.getName(), entityType);
		}
	}
	
	public IEntityType getEntityTypeForResourceName(String resourceName) {
		String entityTypeName = (String) _resourceEntityTypeMap.get(resourceName);
		return getEntityType(entityTypeName);
	}
	
	public IEntityType getEntityTypeForClass(Class<?> clazz) {
		String entityTypeName = MetadataHelper.getEntityTypeName(clazz);
		return getEntityType(entityTypeName);
	}

	public IEntityType getEntityType(String entityTypeName) {
		if (entityTypeName == null) return null;
		return _entityTypeMap.get(entityTypeName);
	}
	
	
	public class EntityType implements IEntityType {
		private Map<String, Object> _entityMap;
		private IMetadata _metadataWrapper;
		
		private String _entityTypeName;
		private Map<String, IProperty> _propertyMap = new HashMap<String, IProperty>();

		public EntityType(Map<String, Object> entityMap, IMetadata metadataWrapper) {
			_entityMap = entityMap;
			_metadataWrapper = metadataWrapper;
			String ns = (String) _entityMap.get("namespace");
			String shortName = (String) _entityMap.get("shortName");
			_entityTypeName = MetadataHelper.getEntityTypeName(ns, shortName);
			
			List<HashMap<String, Object>> properties;
			properties = (List<HashMap<String, Object>>) _entityMap.get("dataProperties");
			for (HashMap<String, Object> p: properties) {
				IDataProperty prop = new DataProperty(p);
				_propertyMap.put(prop.getName(), prop);
			}
			properties = (List<HashMap<String, Object>>) _entityMap.get("navigationProperties");
			if (properties != null) {
				for (HashMap<String, Object> p: properties) {
					INavigationProperty prop = new NavigationProperty(p, metadataWrapper);
					_propertyMap.put(prop.getName(), prop);
				}
			}
		}

		@Override
		public String getName() {
			return _entityTypeName;
		}

		@Override
		public IProperty getProperty(String propertyName) {
			return _propertyMap.get(propertyName);
		}
	}
	
	public class DataProperty implements IDataProperty {
		private Map<String, Object> _dpMap;
		public DataProperty(Map<String, Object> dpMap) {
			_dpMap = dpMap;
		}
		@Override
		public String getName() {
			return (String) _dpMap.get("nameOnServer");
		}

		@Override
		public DataType getDataType() {
			String dataTypeName = (String) _dpMap.get("dataType");
			return DataType.fromName(dataTypeName);
		}
	}
	
	public class NavigationProperty implements INavigationProperty {
		private Map<String, Object> _npMap;
		private IMetadata _metadataWrapper;
		public NavigationProperty(Map<String, Object> npMap, IMetadata metadataWrapper) {
			_npMap = npMap;
			_metadataWrapper = metadataWrapper;
		}
		@Override
		public String getName() {
			return (String) _npMap.get("nameOnServer");
		}

		@Override
		public IEntityType getEntityType() {
			String entityTypeName = (String) _npMap.get("entityTypeName");
			return _metadataWrapper.getEntityType(entityTypeName);
		}

		@Override
		public boolean isScalar() {
			return (boolean) _npMap.get("isScalar");
		}
		
	}
	

}
