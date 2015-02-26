package com.breezejs.util;

import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;

public class JsonGson {

	/**
	 * Convert the object tree to JSON, including the $id. $ref and $type
	 * properties
	 * 
	 * @param obj
	 * @return
	 */
	public static String toJson(Object obj) {
		return toJson(obj, true);
	}
	
	// private static final String ISO8601_DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss";
	// private static final String ISO8601_DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	private static final String ISO8601_DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";	                                                    
	                                                 

	/**
	 * Convert the object tree to JSON
	 * 
	 * @param obj
	 *            - root object
	 * @param isGraph
	 *            - whether to add the $id and $ref and $type properties with
	 *            each object
	 * @return
	 */
	public static String toJson(Object obj, boolean isGraph) {
		try {
			GsonBuilder gsonBuilder = newGsonBuilder();
			if (isGraph) {
				gsonBuilder
				    .registerTypeAdapterFactory(new BreezeTypeAdapterFactory())
					.registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY)
					.registerTypeAdapterFactory(HibernateCollectionTypeAdapter.FACTORY);
					
			}
			Gson gson = gsonBuilder.create();
			return gson.toJson(obj);
		} catch (Exception e) {
			throw new RuntimeException("Exception serializing json: " + obj, e);
		}
	}

	/**
	 * Convert the JSON string to a Map of Lists of Maps...
	 * 
	 * @param source
	 * @return
	 */
	public static Map fromJson(String source) {
		try {
            Gson gson = newGsonBuilder().create();
			LinkedTreeMap result = gson.fromJson(source, LinkedTreeMap.class);
			return result;

		} catch (Exception e) {
			throw new RuntimeException("Exception deserializing json: " + source, e);
		}
	}
	
	public static Map[] fromJsonArray(String source) {
		try {
	        Gson gson = newGsonBuilder().create();
			LinkedTreeMap[] result = gson.fromJson(source, LinkedTreeMap[].class);
			return result;

		} catch (Exception e) {
			throw new RuntimeException("Exception deserializing " + source, e);
		}
	}
	

	/**
	 * Convert the Map into an instance of the given class
	 * 
	 * @param clazz
	 * @param map
	 * @return
	 */
	public static Object fromMap(Class<?> clazz, Map map) {
		try {
			String json = toJson(map);;
	        Gson gson = newGsonBuilder().create();
	        Object result = gson.fromJson(json, clazz);
	        return result;
			// TODO: alternative is too think about using Apache BeanUtils for this.
	        // Old code
			// Object bean = JSONDeserializer.read(clazz, map);
			// return bean;
		} catch (Exception e) {
			throw new RuntimeException("Unable to populate " + clazz.getName()
					+ " from " + map, e);
		}

	}
	
	private static GsonBuilder newGsonBuilder() {
	    return new GsonBuilder().setDateFormat(ISO8601_DATEFORMAT);
	}

}
