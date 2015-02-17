package com.breezejs.hib;

import java.util.HashMap;

import org.hibernate.Criteria;


class CriteriaAliasBuilder  {
	private HashMap<String, String> _cache = new HashMap<String, String>();
	private int _offset = 0;
	public CriteriaAliasBuilder() {
		
	}
	
	public String getPropertyName(Criteria crit, String propertyPath) {

		String[] propNames = propertyPath.split("\\.");

		if (propNames.length == 1) {
			return propNames[0];
		} else {
			// check cache
			String cachedAlias = _cache.get(propertyPath);
			if (cachedAlias != null) {
				return cachedAlias;
			}
			String nextPropName;
			String nextAlias = "";
			for (int i = 0; i < propNames.length - 1; i = i + 1) {
				nextPropName = nextAlias == "" ? propNames[i] : nextAlias + "." + propNames[i];
				nextAlias = _cache.get(nextPropName);
				if (nextAlias == null) {
					nextAlias = propNames[i] + "_" + _offset++;
					crit.createAlias(nextPropName, nextAlias);
					_cache.put(nextPropName, nextAlias);
				}
			}
			nextPropName = nextAlias + "." + propNames[propNames.length - 1];
			_cache.put(propertyPath, nextPropName);
			return nextPropName;
		}
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

}
