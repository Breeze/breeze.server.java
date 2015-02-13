package com.breezejs.metadata;


import java.util.List;



import com.breezejs.query.ExpressionContext;
import com.breezejs.util.StringFns;

public class MetadataHelper {
	// returns the final property from a property path or null if not found.
	public static IProperty getPropertyFromPath(String propertyPath, ExpressionContext context) {
		List<String> paths = StringFns.ToList(propertyPath, "\\.");
		IEntityType nextEntityType = context.entityType;
		IProperty prop = null;
		for (String propName: paths) {
			if (nextEntityType == null) {
				return null;
			}
			prop = nextEntityType.getProperty(propName, context.usesNameOnServer);
			if (prop != null && prop instanceof INavigationProperty) {
				nextEntityType = ((INavigationProperty) prop).getEntityType();
			} else {
				nextEntityType = null;
			}
			
		}
		return prop;
	}
	
	
	
}
