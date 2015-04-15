package com.breeze.save;

import com.breeze.metadata.Metadata;

public abstract class SaveProcessor {
    private Metadata _metadata;
    
    protected SaveProcessor(Metadata metadata) {
        _metadata = metadata;
    }
    
    /**
     * Build the SaveWorkState from the JSON, and use it to save the changes to the data store.
     * @param entities
     * @param saveOptions
     */
    public SaveResult saveChanges(SaveWorkState saveWorkState) {
        saveWorkState.setSaveProcessor(this);
        try {
            saveWorkState.beforeSave();
            saveChangesCore(saveWorkState);
            saveWorkState.afterSave();
        } catch (EntityErrorsException e) {
            saveWorkState.setEntityErrors(e);
        } catch (Exception e) {
            if (! saveWorkState.handleException(e)) {
                throw e;
            }
        }
        
        SaveResult sr = saveWorkState.toSaveResult();
        return sr;
    }

    
    public Metadata getMetadata() {
        return _metadata;
    }
       
	/**
	 * Save the changes to the database.
	 */
	protected abstract void saveChangesCore(SaveWorkState saveWorkState);
	
	// will be called during beforeSaveEntities call for any adds or removalls
	public abstract void processRelationships(EntityInfo entityInfo, boolean removeMode);
	
    public abstract Object getIdentifier(Object entity);
	
}
