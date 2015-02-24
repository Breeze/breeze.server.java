package com.breezejs.query;

import java.util.List;
import java.util.Map;

import com.breezejs.metadata.IEntityType;
import com.breezejs.metadata.IMetadata;
import com.breezejs.metadata.MetadataHelper;
import com.breezejs.util.JsonGson;
import com.breezejs.util.StringFns;

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
		if (json == null || json.length() == 0) {
			return;
		}
		Map qmap;
		try {
			qmap = JsonGson.fromJson(json);
		} catch(Exception e) {
			throw new RuntimeException("This EntityQuery ctor requires a valid json string. The following is not json: " + json);
		}
		
		this._resourceName = (String) qmap.get("resourceName");
		this._skipCount = processCount(qmap.get("skip"));
		this._takeCount = processCount(qmap.get("take"));
		this._wherePredicate = Predicate.predicateFromMap((Map) qmap.get("where"));
		this._orderByClause = OrderByClause.from(toStringList(qmap.get("orderBy")));
		this._selectClause = SelectClause.from(toStringList(qmap.get("select")));
		this._expandClause = ExpandClause.from(toStringList(qmap.get("expand")));
		if (qmap.containsKey("inlineCount")) {
			this._inlineCountEnabled = ((Boolean) qmap.get("inlineCount")).booleanValue();
		}
	}
	
	private List<String> toStringList(Object src) {
		if (src == null) return null;
		if (src instanceof List) {
			return (List<String>) src;
		} else if (src instanceof String) {
			return StringFns.ToList((String) src);
		} throw new RuntimeException("Unable to convert to a List<String>");
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

