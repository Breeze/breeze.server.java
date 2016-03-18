package com.breeze.jpa;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.PersistenceUnitUtil;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * TypeAdapter class for serializing objects that are under JPA control.
 * Uses the PersistenceUnitUtil to avoid lazy-loading properties. 
 * TODO use an interface and custom wrapper around PersistenceUnitUtil / HibernateUtil so this class is reusable for both
 *
 * @param <T> Gson creates a different instance for each type
 */
public class JPATypeAdapter<T> extends TypeAdapter<T> {

    private PersistenceUnitUtil puu;
    private List<BoundField> boundFields;

    JPATypeAdapter(PersistenceUnitUtil puu, List<BoundField> boundFields) {
        this.puu = puu;
        this.boundFields = boundFields;
    }

    @Override
    public T read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException("Not supported");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void write(JsonWriter out, T value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.beginObject();
        try {
            for (BoundField bf : this.boundFields) {
                if (!bf.isCore && !puu.isLoaded(value, bf.fieldName)) {
                    out.name(bf.jsonName).nullValue();
                } else {
                    Object fieldValue = bf.field.get(value);
                    if (fieldValue == null) {
                        out.name(bf.jsonName).nullValue();
                    } else if (fieldValue != value) {
                        out.name(bf.jsonName);
                        bf.typeAdapter.write(out, fieldValue);
                    }
                }
            }
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
            throw new IOException(e);
        }
        
        out.endObject();
    }

    /**
     * Creates JPATypeAdapter instance for a given type.  
     * Creates a list of BoundFields, using reflection, to pass to the JPATypeAdapter constructor.
     * This avoids reflection at serialization time.
     */
    public static class Factory implements TypeAdapterFactory {

        private static int MODIFIER_EXCLUDE = Modifier.ABSTRACT | Modifier.INTERFACE | Modifier.STATIC;
        private PersistenceUnitUtil puu;
        public Factory(PersistenceUnitUtil puu) {
            this.puu = puu;
        }
        
        public static boolean isCoreType(Class clazz, boolean langOnly) {
            // eliminate enums
            if (clazz.isEnum()) return true;
            String typeName = clazz.getCanonicalName();
            // eliminate all simple types
            String prefix = langOnly ? "java.lang." : "java.";
            if ((typeName.indexOf('.') == -1) || typeName.startsWith(prefix)) {
                return true;
            }
            return false;
        }
        
        public final <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            
            Class rawType = type.getRawType();
            if (isCoreType(rawType, false)) return null;
            
            List<BoundField> boundFields = new ArrayList<BoundField>();
            
            // populate list with all JSON-serializable fields on the type
            Class raw = type.getRawType();
            while (raw != Object.class) {
                Field[] fields = raw.getDeclaredFields();
                for (Field field : fields) {
                    int mod = field.getModifiers();
                    if ((mod & MODIFIER_EXCLUDE) != 0) continue;
                    
                    field.setAccessible(true);
                    String fieldName = field.getName();
                    SerializedName serializedName = field.getAnnotation(SerializedName.class);
                    String jsonName = serializedName != null ? serializedName.value() : FieldNamingPolicy.IDENTITY.translateName(field);
                    Class fieldType = field.getType();
                    
                    boundFields.add(new BoundField(field, fieldName, jsonName, gson.getAdapter(fieldType), isCoreType(fieldType, true)));
                }
                raw = (Class) raw.getGenericSuperclass(); // go up the hierarchy
            }
            
            return new JPATypeAdapter<T>(puu, boundFields);
        }
        
    }
    
    /**
     * Represents a field on a serializable object
     */
    public static class BoundField
    {
        public BoundField(Field field, String fieldName, String jsonName, TypeAdapter typeAdapter, boolean isCore) {
            super();
            this.field = field;
            this.fieldName = fieldName;
            this.jsonName = jsonName;
            this.typeAdapter = typeAdapter;
            this.isCore = isCore;
        }
        /** Reflection field */
        Field field;
        /** Name of the field on the object */
        String fieldName;
        /** Name of the field in JSON */
        String jsonName;
        /** TypeAdapter for serializing the field */
        TypeAdapter typeAdapter;
        /** Field is primitive or java.lang.*  */
        boolean isCore;
    }
    
}
