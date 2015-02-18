package com.breezejs.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Set;

public class TypeFns {
	private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();
	
	public static <T> T as(Class<T> t, Object o) {
		return t.isInstance(o) ? t.cast(o) : null;
	}
	
	public static boolean isPrimitive(Object value) {
		Class<?> clazz = value.getClass();
		if (isWrapperType(clazz)) {
			return true;
		} else {
			return clazz.isPrimitive();
		}
	}
	
	public static boolean isPrimitiveOrString(Object value) {
		if (value instanceof String) return true;
		return isPrimitive(value);
	}

    public static boolean isWrapperType(Class<?> clazz)     {
        return WRAPPER_TYPES.contains(clazz);
    }

    private static Set<Class<?>> getWrapperTypes()     {
        Set<Class<?>> ret = new HashSet<Class<?>>();
        ret.add(Boolean.class);
        ret.add(Character.class);
        ret.add(Byte.class);
        ret.add(Short.class);
        ret.add(Integer.class);
        ret.add(Long.class);
        ret.add(Float.class);
        ret.add(Double.class);
        ret.add(Void.class);
        return ret;
    }
    
	/**
	 * Finds a PropertyDescriptor for the given propertyName on the Class
	 * @param clazz
	 * @param propertyName
	 * @return
	 * @throws RuntimeException if property is not found
	 */
	public static PropertyDescriptor findPropertyDescriptor(Class clazz, String propertyName) {
		try {
			BeanInfo binfo = Introspector.getBeanInfo(clazz);
			PropertyDescriptor[] propDescs = binfo.getPropertyDescriptors();
			for (PropertyDescriptor d : propDescs) {
				if (d.getName().equals(propertyName)) {
					return d;
				}
			}
		} catch (IntrospectionException e) {
			throw new RuntimeException("Error finding property " + propertyName + " on " + clazz, e);
		}
		throw new RuntimeException("Property " + propertyName + " not found on " + clazz);
		
	}
	
    
}
