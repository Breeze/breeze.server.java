package com.breeze.save;

import java.util.ArrayList;
import java.util.List;

public class EntityErrorsException extends Exception {
	private static final long serialVersionUID = 1L;
	public int httpStatusCode;
	public List<EntityError> entityErrors;
	public String message;

    public EntityErrorsException(String message, List<EntityError> entityErrors) {
		super(message);
		this.message = message;
		this.entityErrors = entityErrors;
		this.httpStatusCode = 403;
	}
    
    public EntityErrorsException(EntityError error) {
        super(error.getErrorMessage());
        this.message = error.getErrorMessage();
        this.entityErrors = new ArrayList<EntityError>();
        this.entityErrors.add(error);
        this.httpStatusCode = 403;
    }
	
}
