package com.breeze.util;

/**
 * General purpose assertion utilities.
 * @author IdeaBlade
 *
 */
public class Assert {
	public static void notNull(Object value, String name) {
		if (value == null) {
			throw new RuntimeException("Assert: " + name + " cannot be null");
		}
	}
	public static void notNull(Object value) {
		notNull(value, "value");
	}
}
