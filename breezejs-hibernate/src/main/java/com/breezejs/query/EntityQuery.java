package com.breezejs.query;

import java.util.Map;

import com.breezejs.util.JsonGson;

public class EntityQuery {
	private String _resourceName;
	private Predicate _wherePredicate;
	private OrderByClause _orderByClause;
	private ExpandClause _expandClause;
	private SelectClause _selectClause;
	private Integer _skipCount;
	private Integer _takeCount;
	private boolean _inlineCountEnabled;
	
	public EntityQuery(String json) {
		Map qmap = JsonGson.fromJson(json);
		this._resourceName = (String) qmap.get("resourceName");
		this._skipCount = (Integer) qmap.get("skip");
		this._takeCount = (Integer) qmap.get("take");
		this._wherePredicate = Predicate.predicateFromMap((Map) qmap.get("where"));
		this._orderByClause = OrderByClause.fromString( (String) qmap.get("orderBy"));
		this._selectClause = SelectClause.fromString( (String) qmap.get("select"));
		this._expandClause = ExpandClause.fromString( (String) qmap.get("expand"));
		if (qmap.containsKey("inlineCountEnabled")) {
			this._inlineCountEnabled = ((Boolean) qmap.get("inlineCountEnabled")).booleanValue();
		}
	}
	
	
	public String getResourceName() {
		return _resourceName;
	}

	public Predicate getWherePredicate() {
		return _wherePredicate;
	}
	
	public OrderByClause getOrderByClause() {
		return _orderByClause;
	}
	
	public ExpandClause getExpandClause() {
		return _expandClause;
	}
	
	public SelectClause getSelectClause() {
		return _selectClause;
	}
	
	public Integer getSkipCount() {
		return _skipCount;
	}

	public Integer getTakeCount() {
		return _takeCount;
	}

	public boolean isInlineCountEnabled() {
		return _inlineCountEnabled;
	}
	
	public void validate(ExpressionContext exprContext) {
		if (_wherePredicate != null) {
			_wherePredicate.validate(exprContext);
		}
	}

	
}

