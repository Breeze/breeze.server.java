package com.breeze.save;

import com.breeze.metadata.Metadata;

public class ContextProvider {
    private Metadata _metadata;
    
    protected ContextProvider(Metadata metadata) {
        _metadata = metadata;
    }
    
    public Metadata getMetadata() {
        return _metadata;
    }
    
	/**
	 * Build the SaveWorkState from the JSON, and use it to save the changes to the data store.
	 * @param entities
	 * @param saveOptions
	 */
	public SaveResult saveChanges(SaveWorkState saveWorkState) {
		saveWorkState.setContextProvider(this);
		try {
			saveWorkState.beforeSave();
			saveChangesCore(saveWorkState);
			saveWorkState.afterSave();
		} catch (EntityErrorsException e) {
            saveWorkState.setEntityErrors(e);
		} catch (Exception e) {
			if (!saveWorkState.handleException(e)) {
				throw e;
			}
		}
		
		SaveResult sr = saveWorkState.toSaveResult();
		return sr;
	}
	
	
	
	/**
	 * Save the changes to the database.
	 */
	protected void saveChangesCore(SaveWorkState sw) throws EntityErrorsException {
	}
	

	
}
