package com.breezejs.hib;

import org.hibernate.Criteria;


class CriteriaAlias {
	public Criteria criteria;
	public String alias;

	public CriteriaAlias(Criteria crit, String alias) {
		this.criteria = crit;
		this.alias = alias;
		
	}

	public  static CriteriaAlias create(Criteria crit, String propertyPath) {
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
		return new CriteriaAlias(nextCrit, nextPropName);
	}

}
