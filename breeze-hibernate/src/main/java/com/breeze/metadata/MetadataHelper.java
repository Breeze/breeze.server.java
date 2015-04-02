package com.breeze.metadata;

import java.util.List;

import com.breeze.util.StringFns;

public class MetadataHelper {
	
	 /**
     * Get the type name in the form "Order:#northwind.model"
     * @param clazz
     * @return
     */
    public static String getEntityTypeName(Class clazz)  {
    	return clazz.getSimpleName() + ":#" + clazz.getPackage().getName();
    }

    public static String getEntityTypeName(String packageName, String simpleName) {
    	return simpleName + ":#" + packageName;
    }
    
	/**
	 * Given a name in the form "Customer:#northwind.model", returns Class northwind.model.Customer.
	 * @param entityTypeName
	 * @return
	 */
	public static Class lookupClass(String entityTypeName) {
		String[] parts = entityTypeName.split(":#", 2);
		String className = parts[1] + '.' + parts[0];
		
		try {
			Class clazz = Class.forName(className);
			return clazz;
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("No class found for " + entityTypeName, e);
		}
	}
    
	// returns the final property from a property path or null if not found.
	public static IProperty getPropertyFromPath(String propertyPath, IEntityType entityType) {
		String[] paths = propertyPath.split("\\.");
		IEntityType nextEntityType = entityType;
		IProperty prop = null;
		for (String propName: paths) {
			if (nextEntityType == null) {
				return null;
			}
			prop = nextEntityType.getProperty(propName);
			if (prop != null) {
				if (prop instanceof INavigationProperty) {
					nextEntityType = ((INavigationProperty) prop).getEntityType();
				} else {
					// may return null - this is ok;
					nextEntityType = ((IDataProperty) prop).getComplexType();
				}
			} else {
				nextEntityType = null;
			}
		}
		return prop;	
	}
	
}
