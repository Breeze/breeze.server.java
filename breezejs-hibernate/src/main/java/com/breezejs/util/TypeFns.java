package com.breezejs.util;

import java.util.HashSet;
import java.util.Set;

public class TypeFns {
	private static final Set<Class<?>> WRAPPER_TYPES = getWrapperTypes();
	
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
}
