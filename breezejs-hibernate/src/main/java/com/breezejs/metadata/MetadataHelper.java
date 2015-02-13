package com.breezejs.metadata;

import java.util.List;

import com.breezejs.util.StringFns;

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
    

	// returns the final property from a property path or null if not found.
	public static IProperty getPropertyFromPath(String propertyPath, IEntityType entityType) {
		// List<String> paths = StringFns.ToList(propertyPath, "\\.");
		String[] paths = propertyPath.split("\\.");
		IEntityType nextEntityType = entityType;
		IProperty prop = null;
		for (String propName: paths) {
			if (nextEntityType == null) {
				return null;
			}
			prop = nextEntityType.getProperty(propName);
			if (prop != null && prop instanceof INavigationProperty) {
				nextEntityType = ((INavigationProperty) prop).getEntityType();
			} else {
				nextEntityType = null;
			}
		}
		return prop;	
	}
	
}
