package com.breezejs.hib;

import org.hibernate.Criteria;
import org.hibernate.criterion.Criterion;

class CriteriaResult {
	public Criteria criteria;
	public Criterion criterion;
	public CriteriaResult(Criteria criteria, Criterion criterion) {
		this.criteria = criteria;
		this.criterion = criterion;
	}

}
