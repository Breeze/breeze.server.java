package com.breeze.hib;

import java.util.HashMap;

import org.hibernate.Criteria;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.sql.JoinType;

import com.breeze.metadata.IEntityType;
import com.breeze.metadata.INavigationProperty;
import com.breeze.metadata.IProperty;
import com.breeze.metadata.MetadataHelper;

class CriteriaAliasBuilder {
    private HashMap<String, String> _cache = new HashMap<String, String>();
    private int _offset = 0;
    private boolean _containsNavPropertyProxy = false;

    public CriteriaAliasBuilder() {

    }

    public String getPropertyName(CriteriaWrapper crit, String propertyPath) {
        IProperty property = MetadataHelper.getPropertyFromPath(propertyPath, crit.entityType);
        if (property == null) {
            throw new RuntimeException("Unable to locate property: " + propertyPath + " on entityType: " + crit.entityType.getName());
        }
        String[] propNames = propertyPath.split("\\.");
        boolean isNavPath = property instanceof INavigationProperty;
        if (propNames.length == 1 && !isNavPath) {
            return propNames[0];
        } else {
            String nextPropName = propNames[0];
            String nextAlias = "";
            for (int i = 0; i < propNames.length - 1; i = i + 1) {
                nextPropName = nextAlias == "" ? propNames[i] : nextAlias + "." + propNames[i];
                nextAlias = newAlias(crit, nextPropName, propNames[i]);
            }
            String lastPropName = propNames[propNames.length - 1];
            if (isNavPath) {
                INavigationProperty navProp = (INavigationProperty) property;
                if (navProp.isScalar()) {
                    // do not assign to nextAlias;
                    String alias = newAlias(crit, nextPropName, lastPropName);
                    // HACK - because we can't get NavProperties to Eagerly load
                    // when they are the 'projected' item.
                    _containsNavPropertyProxy = true;
                    // TODO: AARGH.... doesn't seem to do anything...
                    
                    ((Criteria) crit.criteria).setFetchMode(alias, FetchMode.JOIN);
                    ((Criteria) crit.criteria).setFetchMode(lastPropName, FetchMode.JOIN);
                } else {
                    // String alias = getAlias(crit, nextPropName, lastPropName);
                    // HACK - because we can't get NavProperties to Eagerly load
                    // when they are the 'projected' item.
                    // _containsNavPropertyProxy = true;
                    // TODO: AARGH.... doesn't seem to do anything...
                    // crit.setFetchMode(alias, FetchMode.JOIN);
                    // crit.setFetchMode(lastPropName, FetchMode.JOIN);
                    throw new RuntimeException("Hibernate does not provide a mechanism to project entity collections");
                }
            }
            nextPropName = nextAlias == "" ? lastPropName : nextAlias + "." + lastPropName;
            return nextPropName;
        }
    }

    private String newAlias(CriteriaWrapper crit, String nextPropName, String aliasRoot) {
        String nextAlias;
        String key = buildCacheKey(crit.entityType, nextPropName);
        nextAlias = _cache.get(key);
        if (nextAlias == null) {
            nextAlias = aliasRoot + "_" + _offset++;
            crit.createAlias(nextPropName, nextAlias); // , JoinType.LEFT_OUTER_JOIN);
            _cache.put(key, nextAlias);
        }
        return nextAlias;
    }
    

    // Used by any/all to get initial expression alias 
    public String getSimpleAlias(CriteriaWrapper crit, String propName) {
        String key = buildCacheKey(crit.entityType, propName);
        String alias = _cache.get(key);
        if (alias == null) {
            alias = propName + "_" + _offset++;
            crit.createAlias(propName, alias);
        }
        return alias;
    }

    public boolean containsNavPropertyProxy() {
        return _containsNavPropertyProxy;
    }
    
    private String buildCacheKey(IEntityType entityType, String propName) {
        return propName + "::" + entityType.getName();
    }

}
