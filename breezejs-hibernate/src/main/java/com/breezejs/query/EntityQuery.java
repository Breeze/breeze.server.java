package com.breezejs.query;

import java.util.Map;

import com.breezejs.metadata.IEntityType;
import com.breezejs.metadata.IMetadata;
import com.breezejs.metadata.MetadataHelper;
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
	private IEntityType _entityType;
	
	
	public EntityQuery(String json) {
		Map qmap = JsonGson.fromJson(json);
		this._resourceName = (String) qmap.get("resourceName");
		this._skipCount = processCount(qmap.get("skip"));
		this._takeCount = processCount(qmap.get("take"));
		this._wherePredicate = Predicate.predicateFromMap((Map) qmap.get("where"));
		this._orderByClause = OrderByClause.fromString( (String) qmap.get("orderBy"));
		this._selectClause = SelectClause.fromString( (String) qmap.get("select"));
		this._expandClause = ExpandClause.fromString( (String) qmap.get("expand"));
		if (qmap.containsKey("inlineCountEnabled")) {
			this._inlineCountEnabled = ((Boolean) qmap.get("inlineCountEnabled")).booleanValue();
		}
	}
	
	private Integer processCount(Object o) {
		if (o == null) return null;
		return ((Double) o).intValue();
	}
	
	public void validate(IEntityType entityType) {
		_entityType = entityType;
		if (_wherePredicate != null) {
			_wherePredicate.validate(entityType);
		}
		if (_orderByClause != null) {
			_orderByClause.validate(entityType);
		}
		if (_selectClause != null) {
			_selectClause.validate(entityType);
		}
	}

	// only available after validate is called.
	public IEntityType getEntityType() {
		return _entityType;
	}
	
	public void setResourceName(String resourceName) {
		_resourceName = resourceName;
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
	


	
}

