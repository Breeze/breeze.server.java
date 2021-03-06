package com.breeze.util;

import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.internal.LinkedTreeMap;

public class JsonGson {

    /**
     * Convert the object tree to JSON, including the $id. $ref and $type
     * properties
     * 
     * @param obj The obj to serialize to json
     * @return The json string.
     */
    public static String toJson(Object obj) {
        return toJson(obj, false, false);
    }

    /**
     * Convert the object tree to JSON
     * 
     * @param obj The root object to be serialized to json.
     * @param includesBreezeEntities 
     *      Whether to add the $id and $ref and $type properties when serializing the object.
     * @param isHibernate 
     *      Whether to add Hibernate proxy handling when serializing the object.
     * @return A json string.
     */
    public static String toJson(Object obj, boolean includesBreezeEntities, boolean isHibernate) {
        try {
            GsonBuilder gsonBuilder = newGsonBuilder(includesBreezeEntities, isHibernate);
            Gson gson = gsonBuilder.create();
            return gson.toJson(obj);
        } catch (Exception e) {
            throw new RuntimeException("Exception serializing json: " + obj, e);
        }
    }

    /**
     * Convert the JSON string to a Map of Lists of Maps...
     * 
     * @param source A json string
     * @return A Map representing the deserialized json.
     */
    public static Map fromJson(String source) {
        try {
            Gson gson = newGsonBuilder().create();
            LinkedTreeMap result = gson.fromJson(source, LinkedTreeMap.class);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Exception deserializing json: "
                    + source, e);
        }
    }

    /**
     * 
     * Convert a JSON string containing a json Array into an Array of Maps
     *
     * @param source A string containing a json array i.e. "[....]".
     * @return An array of Maps.
     */
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
     * @param clazz The class to deserialize into.
     * @param map The data to deserialize.
     * @return An instance of the specified class.
     */
    public static Object fromMap(Class<?> clazz, Map map) {
        String json = toJson(map);
        return fromJson(clazz, json);
    }

    /**
     * Convert a json string ito an instance of the specified class.
     * @param clazz The class to deserialize into.
     * @param json The data to deserialize.
     * @return An instance of the specified class.
     */
    public static Object fromJson(Class<?> clazz, String json) {
        try {
            Gson gson = newGsonBuilder().create();
            Object result = gson.fromJson(json, clazz);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Unable to populate " + clazz.getName()
                    + " from " + json, e);
        }
    }

    /** @return newGsonBuilder(false, false) */
    public static GsonBuilder newGsonBuilder() {
        return newGsonBuilder(false, false);
    }

    /**
     * @param useBreezeTypeAdapter 
     *      Whether to add the $id and $ref and $type properties when serializing the object.
     * @param useHibernateProxyAdapter 
     *      Whether to add Hibernate proxy handling when serializing the object.
     * @return a GsonBuilder with a DateTypeAdapter
     */
    public static GsonBuilder newGsonBuilder(boolean useBreezeTypeAdapter, boolean useHibernateProxyAdapter) {
        // setDateFormat in commented out line below works fine for
        // deserialization but doesn't handle
        // serialization properly because of need for a TimeZone setting.
        // Hence the need for the DateTypeAdapter below.
        // return new GsonBuilder().setDateFormat(ISO8601_DATEFORMAT);

        GsonBuilder gsonBuilder = new GsonBuilder()
                .registerTypeAdapter(Date.class, new DateTypeAdapter());
        
        if (useBreezeTypeAdapter) {
            gsonBuilder = gsonBuilder
                    .registerTypeAdapterFactory(new BreezeTypeAdapterFactory());
        }
        if (useHibernateProxyAdapter) {
            gsonBuilder = gsonBuilder
                    .registerTypeAdapterFactory(HibernateProxyTypeAdapter.FACTORY)
                    .registerTypeAdapterFactory(HibernateCollectionTypeAdapter.FACTORY);

        }

        return gsonBuilder;
    }
    
    // Ugh....
    // private static final String ISO8601_DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    // private static final String ISO8601_DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
    // private static final String ISO8601_DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSX";
    // private static final String ISO8601_DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ssZ";
    // private static final String ISO8601_DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ssX";
    private static final String ISO8601_DATEFORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";

    private static class DateTypeAdapter implements
            JsonSerializer<Date>,
            JsonDeserializer<Date> {
        private final DateFormat _dateFormat;

        private DateTypeAdapter() {
            _dateFormat = new SimpleDateFormat(ISO8601_DATEFORMAT, Locale.US);
            _dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
        }

        @Override
        public synchronized JsonElement serialize(Date date, Type type,
                JsonSerializationContext jsonSerializationContext) {
            return new JsonPrimitive(_dateFormat.format(date));
        }

        @Override
        public synchronized Date deserialize(JsonElement jsonElement,
                Type type, JsonDeserializationContext jsonDeserializationContext) {
            try {
                return _dateFormat.parse(jsonElement.getAsString());
            } catch (ParseException e) {
                throw new JsonParseException(e);
            }
        }
    }

}
