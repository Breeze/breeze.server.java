package com.breeze.hib;

import org.hibernate.Criteria;
import org.hibernate.criterion.DetachedCriteria;

import com.breeze.metadata.IEntityType;

public class CriteriaWrapper {
    
    public Object criteria;
    public IEntityType entityType;
    
    public CriteriaWrapper(Criteria criteria, IEntityType entityType) {
        this.criteria = criteria;
        this.entityType = entityType;    
    }
    
    public CriteriaWrapper(DetachedCriteria criteria, IEntityType entityType) {
        this.criteria = criteria;
        this.entityType = entityType;    
    }
    
    public  String getAlias() {
        if (this.criteria instanceof Criteria) {
            return ((Criteria) criteria).getAlias();
        } else {
            return ((DetachedCriteria) criteria).getAlias();
        } 
    }
    
    public void createAlias(String propName, String alias) {
        if (this.criteria instanceof Criteria) {
            ((Criteria) this.criteria).createAlias(propName, alias);
        } else { 
            ((DetachedCriteria) this.criteria).createAlias(propName, alias);
        } 
    }
}
