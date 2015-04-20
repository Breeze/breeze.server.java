package com.breeze.save;


import com.breeze.metadata.Metadata;


/**
 * This is the base class that supports all save opertions within breeze.
 * The is an abstract class that may be extended by each persistence library. 
 * @author IdeaBlade
 *
 */
public abstract class SaveProcessor {
    private Metadata _metadata;
    
    private SaveState _saveState;
    
    
    /**
     * Describes the current stage of processing for the entities within a SaveWorkState.
     * @author IdeaBlade
     *
     */
    public enum SaveState {
        BeforeFixup,
        AfterFixup,
        BeforeCommit
    }
    
    protected SaveProcessor(Metadata metadata) {
        _metadata = metadata;
    }
    
    /**
     * Persist the changes to the entities in the SaveWorkState that is passed in.
     * This method calls the saveChangesCore method that will be implemented for each persistence library. 
     * 
     * This method both persists the entities and creates the collection of KeyMappings,
     * @param saveWorkState A saveWorkState that consists of the entities to be saved and any
     * interceptors that should be called during the save process.
     * @return A SaveResult consisting of the saved entities and any related KeyMappings.
     */
    public SaveResult saveChanges(SaveWorkState saveWorkState) {
        saveWorkState.setSaveProcessor(this);
        try {
            _saveState = SaveState.BeforeFixup;
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

    
    /**
     * @return The Metadata associated with this SaveWorkState.
     */
    public Metadata getMetadata() {
        return _metadata;
    }
    
 
    /**
     * @return The current SaveState ( state of processing).
     */
    protected SaveState getSaveState() {
        return _saveState;
    }
    
    
    /**
     * @param saveState SaveState to set.
     */
    protected void setSaveState(SaveState saveState) {
        _saveState = saveState;
    }
       
	/**
	 * Core method to save the changes to the database.
	 */
	protected abstract void saveChangesCore(SaveWorkState saveWorkState);
	
 
	
	/**
	 * Called for each entity to attach of detach it from any related entities. 
	 * @param entityInfo The EntityInfo to attach or detach.
	 * @param removeMode Whether to detach.
	 */
	public abstract void processRelationships(EntityInfo entityInfo, boolean removeMode);
	
	
    /**
     * Used to return the id for an entity.  This logic is often dependent on the 
     * persistence library is use.
     * @param entity The entity to return an identifier for.
     * @return The id of the specified entity.
     */
    public abstract Object getIdentifier(Object entity);
	
}
