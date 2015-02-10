package com.breezejs.query;

import java.util.Map;

import com.breezejs.util.JsonGson;

public class EntityQuery {
	public EntityQuery(String json) {
		Map qmap = JsonGson.fromJson(json);
		this.resourceName = (String) qmap.get("resourceName");
		this.skipCount = (Integer) qmap.get("skip");
		this.takeCount = (Integer) qmap.get("take");
		this.wherePredicate = WherePredicate.fromMap((Map) qmap.get("where"));
		this.orderByClause = OrderByClause.fromString( (String) qmap.get("orderBy"));
		this.selectClause = SelectClause.fromString( (String) qmap.get("select"));
		this.expandClause = ExpandClause.fromString( (String) qmap.get("expand"));
		if (qmap.containsKey("inlineCountEnabled")) {
			this.inlineCountEnabled = (boolean) qmap.get(inlineCountEnabled);
		}
	}
	
	
	public String getResourceName() {
		return resourceName;
	}
	public void setResourceName(String resourceName) {
		this.resourceName = resourceName;
	}
	public WherePredicate getWherePredicate() {
		return wherePredicate;
	}
	public void setWherePredicate(WherePredicate wherePredicate) {
		this.wherePredicate = wherePredicate;
	}
	public OrderByClause getOrderByClause() {
		return orderByClause;
	}
	public void setOrderByClause(OrderByClause orderByClause) {
		this.orderByClause = orderByClause;
	}
	public ExpandClause getExpandClause() {
		return expandClause;
	}
	public void setExpandClause(ExpandClause expandClause) {
		this.expandClause = expandClause;
	}
	public SelectClause getSelectClause() {
		return selectClause;
	}
	public void setSelectClause(SelectClause selectClause) {
		this.selectClause = selectClause;
	}
	public Integer getSkipCount() {
		return skipCount;
	}
	public void setSkipCount(Integer skipCount) {
		this.skipCount = skipCount;
	}
	public Integer getTakeCount() {
		return takeCount;
	}
	public void setTakeCount(Integer takeCount) {
		this.takeCount = takeCount;
	}
	public boolean isInlineCountEnabled() {
		return inlineCountEnabled;
	}
	public void setInlineCountEnabled(boolean inlineCountEnabled) {
		this.inlineCountEnabled = inlineCountEnabled;
	}
	
	private String resourceName;
	private WherePredicate wherePredicate;
	private OrderByClause orderByClause;
	private ExpandClause expandClause;
	private SelectClause selectClause;
	private Integer skipCount;
	private Integer takeCount;
	private boolean inlineCountEnabled;
	
}

