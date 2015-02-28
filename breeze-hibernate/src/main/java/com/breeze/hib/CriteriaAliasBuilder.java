package com.breeze.hib;

import java.util.HashMap;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.sql.JoinType;

import com.breeze.metadata.IEntityType;
import com.breeze.metadata.INavigationProperty;
import com.breeze.metadata.IProperty;
import com.breeze.metadata.MetadataHelper;


class CriteriaAliasBuilder  {
	private HashMap<String, String> _cache = new HashMap<String, String>();
	private int _offset = 0;
	private IEntityType _entityType;
	private boolean _containsNavPropertyProxy = false;
	public CriteriaAliasBuilder(IEntityType entityType) {
		_entityType = entityType;
	}
	
	public String getPropertyName(Criteria crit, String propertyPath) {
		IProperty property = MetadataHelper.getPropertyFromPath(propertyPath, _entityType);
		String[] propNames = propertyPath.split("\\.");
		boolean isNavPath = property instanceof INavigationProperty;
		if (propNames.length == 1 && !isNavPath) {
			return propNames[0];
		} else {
			String nextPropName = propNames[0];
			String nextAlias = "";
			for (int i = 0; i < propNames.length - 1; i = i + 1) {
				nextPropName = nextAlias == "" ? propNames[i] : nextAlias + "." + propNames[i];
				nextAlias = getAlias(crit, nextPropName, propNames[i]);
			}
			String lastPropName = propNames[propNames.length - 1];
			if (isNavPath) {
				// do not assign to nextAlias;
				String alias = getAlias(crit, nextPropName, lastPropName);
				// HACK - because we can't get NavProperties to Eagerly load
				// when they are the 'projected' item.
				_containsNavPropertyProxy = true;
				// TODO: AARGH.... doesn't seem to do anything...
				crit.setFetchMode(alias, FetchMode.JOIN);
				crit.setFetchMode(lastPropName, FetchMode.JOIN);
			} 
			nextPropName = nextAlias == "" ? lastPropName : nextAlias + "." + lastPropName;
			return nextPropName;
		}
	}

	private String getAlias(Criteria crit, String nextPropName, String aliasRoot) {
		String nextAlias;
		nextAlias = _cache.get(nextPropName);
		if (nextAlias == null) {
			nextAlias = aliasRoot + "_" + _offset++;
			crit.createAlias(nextPropName, nextAlias); // , JoinType.LEFT_OUTER_JOIN);
			_cache.put(nextPropName, nextAlias);
		}
		return nextAlias;
	}
	

	// Used by any/all to get initial expression alias 
	public String getSimpleAlias(Criteria crit, String propName) {
		String alias = _cache.get(propName);
		if (alias == null) {
			alias = propName + "_" + _offset++;
			crit.createAlias(propName,  alias);
		}
		return alias;
	}
	
	public boolean containsNavPropertyProxy() {
		return _containsNavPropertyProxy;
	}

}
