package com.breeze.query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.breeze.metadata.IEntityType;
import com.breeze.metadata.IProperty;
import com.breeze.metadata.MetadataHelper;

/**
 * Represents a single orderBy clause that will be part of an EntityQuery. An orderBy 
 * clause represents either the name of a property or a path to the property of another entity via its navigation path 
 * from the current EntityType for a given query. 
 * @author IdeaBlade
 *
 */
public class OrderByClause {

	private List<OrderByItem> _orderByItems;
	private List<String> _propertyPaths;
	
	public static final OrderByClause from(List<String> propertyPaths) {
		return (propertyPaths == null) ? null : new OrderByClause(propertyPaths);
	}
	
    public OrderByClause(List<String> propertyPaths) {

    	_orderByItems = new ArrayList<OrderByItem>();
    	for (String item : propertyPaths) {
    		// Intervening whitespace
    		String[] itemParts = item.trim().split("\\s+");
    		boolean isDesc = itemParts.length == 1 ? false : itemParts[1].equals("desc");
    		OrderByItem obItem = new OrderByItem(itemParts[0], isDesc);
    		_orderByItems.add(obItem);
    	}
    }
    
    public void validate(IEntityType entityType) {
    	for (OrderByItem item: _orderByItems) {
    		item.validate(entityType);
    	}
    }
    
    public List<String> getPropertyPaths() {
        return Collections.unmodifiableList(_propertyPaths);
    }
    
	public List<OrderByItem> getOrderByItems() {
		return Collections.unmodifiableList(_orderByItems);
	}

	public class OrderByItem {
		String _propertyPath;
		boolean _isDesc;	
		IProperty _property;
		
		public OrderByItem(String propertyPath, boolean isDesc) {
			_propertyPath = propertyPath;
			_isDesc = isDesc;
		}
		
		public String getPropertyPath() {
			return _propertyPath;
		}

		public boolean isDesc() {
			return _isDesc;
		}
		
		public IProperty getProperty() {
			return _property;
		}
		
	    public void validate(IEntityType entityType) {
	    	_property = MetadataHelper.getPropertyFromPath(_propertyPath, entityType);
	    }

	}
}
