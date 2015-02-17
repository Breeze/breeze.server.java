package com.breezejs.hib;

import java.util.HashMap;

import org.hibernate.Criteria;


class CriteriaAliasBuilder  {
	private HashMap<String, CriteriaAlias> _cache = new HashMap<String, CriteriaAlias>();
	public CriteriaAliasBuilder() {
		
	}
	
	class CriteriaAlias {
		public Criteria criteria;
		public String alias;
	
		public CriteriaAlias(Criteria crit, String alias) {
			this.criteria = crit;
			this.alias = alias;
		}
	}
	
	public CriteriaAlias noAlias(Criteria crit, String alias) {
		return new CriteriaAlias(crit, alias);
	}

	public CriteriaAlias create(Criteria crit, String propertyPath) {
		// check cache
		String cacheKey = getCacheKey(crit, propertyPath);
		CriteriaAlias cachedAlias = _cache.get(cacheKey);
		if (cachedAlias != null) {
			return cachedAlias;
		}
		String[] propNames = propertyPath.split("\\.");
		String nextPropName;
		Criteria nextCrit = crit;
		if (propNames.length == 1) {
			nextPropName = propNames[0];
		} else {
			String nextAlias = "";
			for (int i = 0; i < propNames.length - 1; i = i + 1) {
				nextPropName = nextAlias == "" ? propNames[i] : nextAlias + "." + propNames[i];
				nextAlias = propNames[i] + "_" + i;
				nextCrit = nextCrit.createAlias(nextPropName, nextAlias);
			}
			nextPropName = nextAlias + "." + propNames[propNames.length - 1];
		}
		CriteriaAlias newAlias = new CriteriaAlias(nextCrit, nextPropName);
		_cache.put(cacheKey, newAlias);
		return newAlias;
	}
	
	public String getCacheKey(Criteria crit, String propertyPath) {
		return System.identityHashCode(crit) + "__" + propertyPath;
	}

}
