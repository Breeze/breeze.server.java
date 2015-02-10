package com.breezejs.util;

import java.io.IOException;


import java.util.IdentityHashMap;
import java.util.Map;

// import com.google.gson.*;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;


public class BreezeTypeAdapterFactory implements TypeAdapterFactory {
	private Map<Object, String> _entityMap;
	private long _nextId;
	

	public BreezeTypeAdapterFactory() {
		_entityMap = new IdentityHashMap<Object, String>();
		_nextId = 1;
	}

	public final <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
		
		String name = type.getRawType().getCanonicalName();
		// eliminate all simple types
	    if ((name.indexOf('.') == -1) || name.startsWith("java.")) {
	    	return null;
	    }
	    
	    
		final TypeAdapter<T> delegateAdapter = gson.getDelegateAdapter(this, type);
		final TypeAdapter<JsonElement> elementAdapter = gson.getAdapter(JsonElement.class);
		return new TypeAdapter<T>() {
			@Override
			public void write(JsonWriter out, T value) throws IOException {
				
				if (value == null) {
					// elementAdapter.write(out, delegateAdapter.toJsonTree(value));
                    out.nullValue();
					return;
				}
				JsonElement tree;
				JsonObject jo;
				String ref =_entityMap.get(value);
			
				if (ref == null) {
					String id = Long.toString(_nextId++);
					_entityMap.put(value, id);
					tree = delegateAdapter.toJsonTree(value);
					jo = tree.getAsJsonObject();
					jo.add("$type", new JsonPrimitive(value.getClass().getName()));
					jo.add("$id", new JsonPrimitive(id));				
				
				} else {
					jo = new JsonObject();
					jo.addProperty("$ref", ref);
					tree = (JsonElement) jo;
				}

				elementAdapter.write(out, tree);
			}
			
			@Override 
			public T read(JsonReader in) throws IOException {
				JsonElement tree = elementAdapter.read(in);
				tree = afterRead(tree);
				return delegateAdapter.fromJsonTree(tree);
			}
		};
	}

	
	/**
	* Override this to muck with {@code deserialized} before it parsed into
	* the application type.
	*/
	protected JsonElement afterRead(JsonElement deserialized) {
		return deserialized;
	}
}

