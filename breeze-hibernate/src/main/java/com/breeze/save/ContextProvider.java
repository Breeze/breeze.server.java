package com.breeze.save;

import java.util.List;
import java.util.Map;

import com.breeze.metadata.Metadata;
import com.breeze.util.JsonGson;

public class ContextProvider {
    private Metadata _metadata;
    
    protected ContextProvider(Metadata metadata) {
        _metadata = metadata;
    }
    
    protected Metadata getMetadata() {
        return _metadata;
    }
    
	/**
	 * Build the SaveWorkState from the JSON, and use it to save the changes to the data store.
	 * @param entities
	 * @param saveOptions
	 */
	public SaveResult saveChanges(SaveWorkState saveWorkState) {
		saveWorkState.contextProvider = this;
		try {
			saveWorkState.beforeSave();
			saveChangesCore(saveWorkState);
			saveWorkState.afterSave();
		} catch (EntityErrorsException e) {
			saveWorkState.entityErrors = e.entityErrors;
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
