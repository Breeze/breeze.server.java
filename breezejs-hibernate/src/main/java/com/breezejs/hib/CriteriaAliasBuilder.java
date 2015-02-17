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
			String nextPropName = null;
			String nextAlias = "";
			for (int i = 0; i < propNames.length - 1; i = i + 1) {
				nextPropName = nextAlias == "" ? propNames[i] : nextAlias + "." + propNames[i];
				nextAlias = getAlias(crit, nextPropName);
			}
			nextPropName = nextAlias + "." + propNames[propNames.length - 1];
			return nextPropName;
		}
		
	}
	
	private String getAlias(Criteria crit, String propPath) {
		String alias = _cache.get(propPath);
		if (alias == null) {
			String[] propNames = propPath.split("\\.");
			String propName = propNames[propNames.length - 1];
			alias = propName + "_" + _offset++;
			crit.createAlias(propPath, alias);
			_cache.put(propPath, alias);
		}
		return alias;

	}

}
