package com.breezejs.query;

import com.breezejs.metadata.DataType;
import com.breezejs.metadata.IEntityType;

public class ExpressionContext {
	public IEntityType entityType;
	public boolean usesNameOnServer;
	
	public ExpressionContext(IEntityType entityType, boolean usesNameOnServer) {
		this.entityType = entityType;
		this.usesNameOnServer = usesNameOnServer;
	}

}
