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
     * @param obj
     * @return
     */
    public static String toJson(Object obj) {
        return toJson(obj, false, false);
    }


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
    public static String toJson(Object obj, boolean includesBreezeEntities, boolean isHibernate) {
        try {
            GsonBuilder gsonBuilder = newGsonBuilder();
            if (includesBreezeEntities) {
                gsonBuilder = gsonBuilder
                        .registerTypeAdapterFactory(
                                new BreezeTypeAdapterFactory());
            }
            if (isHibernate) {
                gsonBuilder = gsonBuilder
                        .registerTypeAdapterFactory(
                                HibernateProxyTypeAdapter.FACTORY)
                        .registerTypeAdapterFactory(
                                HibernateCollectionTypeAdapter.FACTORY);

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
            throw new RuntimeException("Exception deserializing json: "
                    + source, e);
        }
    }

    public static Map[] fromJsonArray(String source) {
        try {
            Gson gson = newGsonBuilder().create();
            LinkedTreeMap[] result = gson.fromJson(source,
                    LinkedTreeMap[].class);
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
            String json = toJson(map);
            
            Gson gson = newGsonBuilder().create();
            Object result = gson.fromJson(json, clazz);
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Unable to populate " + clazz.getName()
                    + " from " + map, e);
        }

    }

    private static GsonBuilder newGsonBuilder() {
        // setDateFormat in commented out line belows works fine for
        // deserialization but doesn't handle
        // serialization properly because of need for a TimeZone setting.
        // Hence the need for the DateTypeAdapter below.
        // return new GsonBuilder().setDateFormat(ISO8601_DATEFORMAT);

        GsonBuilder gson = new GsonBuilder().registerTypeAdapter(Date.class,
                new DateTypeAdapter());
        return gson;
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
