package com.breezejs.query;

import java.util.ArrayList;
import java.util.List;

import com.breezejs.util.StringFns;

public class OrderByClause {
	private String _source;
	private List<OrderByItem> _orderByItems;

	
	public static final OrderByClause fromString(String source) {
		return (source == null) ? null : new OrderByClause(source);
	}
	
    public OrderByClause(String source) {
    	_source = source;
    	
    	List<String> items = StringFns.ToList(source, "\\,");
    	_orderByItems = new ArrayList<OrderByItem>();
    	for (String item : items) {
    		// Intervening whitespace
    		String[] itemParts = item.trim().split("\\s+");
    		boolean isDesc = itemParts.length == 1 ? false : itemParts[1].equals("desc");
    		OrderByItem obItem = new OrderByItem(itemParts[0], isDesc);
    		_orderByItems.add(obItem);
    	}
   	

    }
    
	public String getSource() {
		return _source;
	}


	public List<OrderByItem> getOrderByItems() {
		return _orderByItems;
	}

    
}
