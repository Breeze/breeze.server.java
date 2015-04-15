package com.breeze.save;

import com.breeze.metadata.Metadata;

public abstract class ContextProvider {
    protected Metadata _metadata;
    protected SaveWorkState _saveWorkState;
    
    protected ContextProvider(Metadata metadata, SaveWorkState saveWorkState) {
        _metadata = metadata;
        _saveWorkState = saveWorkState;
    }
    
    public Metadata getMetadata() {
        return _metadata;
    }
       
	/**
	 * Build the SaveWorkState from the JSON, and use it to save the changes to the data store.
	 * @param entities
	 * @param saveOptions
	 */
	public SaveResult saveChanges() {
		_saveWorkState.setContextProvider(this);
		try {
			_saveWorkState.beforeSave();
			saveChangesCore();
			_saveWorkState.afterSave();
		} catch (EntityErrorsException e) {
            _saveWorkState.setEntityErrors(e);
		} catch (Exception e) {
			if (! _saveWorkState.handleException(e)) {
				throw e;
			}
		}
		
		SaveResult sr = _saveWorkState.toSaveResult();
		return sr;
	}
	
	
	
	/**
	 * Save the changes to the database.
	 */
	protected abstract void saveChangesCore();
	
	// will be called during beforeSaveEntities call for any adds or removalls
	public abstract void processRelationships(EntityInfo entityInfo, boolean removeMode);
	
    public abstract Object getIdentifier(Object entity);
	
}
