package com.breeze.save;

import java.util.List;

import com.breeze.util.JsonGson;

/**
 * Result of a save, which may have either Entities & KeyMappings, or
 * EntityErrors
 * 
 * @author Steve/Jay
 */
public class SaveResult {
    private List<Object> entities;
    private List<KeyMapping> keyMappings;
    private List<EntityError> errors;
    private String message;

    public SaveResult(List<Object> entities, List<KeyMapping> keyMappings) {
        this.entities = entities;
        this.keyMappings = keyMappings;
    }

    public SaveResult(EntityErrorsException exception) {
        this.errors = exception.entityErrors;
        this.message = exception.message;
    }

    public List<Object> getEntities() {
        return entities;
    }

    public void setEntities(List<Object> entities) {
        this.entities = entities;
    }

    public List<KeyMapping> getKeyMappings() {
        return keyMappings;
    }

    public void setKeyMappings(List<KeyMapping> keyMappings) {
        this.keyMappings = keyMappings;
    }

    public List<EntityError> getErrors() {
        return this.errors;
    }
    
    public String getMessage() {
        return this.message;
    }

    public boolean hasErrors() {
        return this.errors != null || this.message != null;
    }

    public String toJson() {
        return JsonGson.toJson(this, true, true);
    }

}
