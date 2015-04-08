package com.breeze.util;

import java.io.IOException;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;


public class HibernateCollectionTypeAdapter extends TypeAdapter<Iterable> {

    public static final TypeAdapterFactory FACTORY = new TypeAdapterFactory() {
        @Override
        @SuppressWarnings("unchecked")
        public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
            return (Iterable.class.isAssignableFrom(type.getRawType()) ? (TypeAdapter<T>) new HibernateCollectionTypeAdapter(gson) : null);
        }
    };
    private final Gson context;

    private HibernateCollectionTypeAdapter(Gson context) {
        this.context = context;
    }

    @Override
    public Iterable read(JsonReader in) throws IOException {
        throw new UnsupportedOperationException("Not supported");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void write(JsonWriter out, Iterable value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        if (value instanceof org.hibernate.collection.spi.PersistentCollection && 
    			!((org.hibernate.collection.spi.PersistentCollection) value).wasInitialized()) {
            out.beginArray();        	
        } else {
            out.beginArray();
        	for (Object o: value) {
        		TypeAdapter delegate = context.getAdapter(o.getClass());
        		delegate.write(out, o);
			}
        }         
    	out.endArray();
		
    }
}

