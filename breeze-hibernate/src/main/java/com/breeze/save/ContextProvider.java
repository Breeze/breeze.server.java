package com.breeze.save;

import java.util.List;
import java.util.Map;

import com.breeze.util.JsonGson;

public class ContextProvider {
    
    
	/**
	 * Build the SaveWorkState from the JSON, and use it to save the changes to the data store.
	 * @param entities
	 * @param saveOptions
	 */
	public SaveResult saveChanges(SaveWorkState saveWorkState) {
		saveWorkState.SetContextProvider(this);
		try {
			saveWorkState.beforeSave();
			saveChangesCore(saveWorkState);
			saveWorkState.afterSave();
		} catch (EntityErrorsException e) {
			saveWorkState.entityErrors = e.entityErrors;
		} catch (Exception e) {
			if (!handleSaveException(e, saveWorkState)) {
				throw e;
			}
		}
		
		SaveResult sr = saveWorkState.toSaveResult();
		return sr;
	}
	
	/**
	 * Process the saveMap after entities are saved (and temporary keys replaced)
	 * @param saveMap all entities which have been saved
	 * @param keyMappings mapping of temporary keys to real keys
	 */
	public void afterSaveEntities(Map<Class, List<EntityInfo>> saveMap, List<KeyMapping> keyMappings) throws EntityErrorsException {
	}
	
	/**
	 * Save the changes to the database.
	 */
	protected void saveChangesCore(SaveWorkState sw) {
	}
	
	/**
	 * Allows subclasses to plug in their own exception handling.  
	 * This method is called when saveChangesCore throws an exception.
	 * Subclass implementations of this method should either:
	 *  1. Throw an exception
	 *  2. Return false (exception not handled)
	 *  3. Return true (exception handled) and modify the SaveWorkState accordingly.
	 * Base implementation returns false (exception not handled).
	 * @param e Exception that was thrown by saveChangesCore
	 * @param saveWorkState SaveWorkState when the exception was thrown
	 * @return true (exception handled) or false (exception not handled)
	 */
	protected boolean handleSaveException(Exception e, SaveWorkState saveWorkState) {
		return false;
	}
	
}
