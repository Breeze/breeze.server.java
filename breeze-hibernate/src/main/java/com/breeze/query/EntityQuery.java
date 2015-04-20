package com.breeze.query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.breeze.metadata.IEntityType;
import com.breeze.util.JsonGson;
import com.breeze.util.StringFns;

/**
 * Represents a query against a strongly typed model.
 * This query is designed to be easily converted into variety of persistence library  
 * query languages via subclasses of the QueryProcessor class.  
 * An EntityQuery is an immutable structure.  This means that mutation methods
 * on an EntityQuery actually all return new EntityQuery instances. 
 * @author IdeaBlade
 *
 */
public class EntityQuery {
    private String _resourceName;
    private Predicate _wherePredicate;
    private OrderByClause _orderByClause;
    private ExpandClause _expandClause;
    private SelectClause _selectClause;
    private Integer _skipCount;
    private Integer _takeCount;
    private boolean _inlineCountEnabled;
    private Map _parameters;
    private IEntityType _entityType;

    public EntityQuery() {

    }

    
    /**
     * Materializes the serialized json representation of an EntityQuery.
     * @param json The serialized json version of the EntityQuery.
     */
    public EntityQuery(String json) {
        if (json == null || json.length() == 0) {
            return;
        }
        Map qmap;
        try {
            qmap = JsonGson.fromJson(json);
        } catch (Exception e) {
            throw new RuntimeException(
                    "This EntityQuery ctor requires a valid json string. The following is not json: "
                            + json);
        }

        this._resourceName = (String) qmap.get("resourceName");
        this._skipCount = processCount(qmap.get("skip"));
        this._takeCount = processCount(qmap.get("take"));
        this._wherePredicate = Predicate.predicateFromMap((Map) qmap
                .get("where"));
        this._orderByClause = OrderByClause.from(toStringList(qmap
                .get("orderBy")));
        this._selectClause = SelectClause
                .from(toStringList(qmap.get("select")));
        this._expandClause = ExpandClause
                .from(toStringList(qmap.get("expand")));
        this._parameters = (Map) qmap.get("parameters");
        if (qmap.containsKey("inlineCount")) {
            this._inlineCountEnabled = ((Boolean) qmap.get("inlineCount"))
                    .booleanValue();
        }
    }

    
    /**
     * Copy constructor
     * @param query
     */
    public EntityQuery(EntityQuery query) {
        this._resourceName = query._resourceName;
        this._skipCount = query._skipCount;
        this._takeCount = query._takeCount;
        this._wherePredicate = query._wherePredicate;
        this._orderByClause = query._orderByClause;
        this._selectClause = query._selectClause;
        this._expandClause = query._expandClause;
        this._inlineCountEnabled = query._inlineCountEnabled;
        this._parameters = query._parameters;

    }

    
    /**
     * Return a new query based on this query with an additional where clause added.
     * @param json Json representation of the where clause.
     * @return A new EntityQuery.
     */
    public EntityQuery where(String json) {
        Map qmap = JsonGson.fromJson(json);
        Predicate pred = Predicate.predicateFromMap(qmap);
        return this.where(pred);
    }

    
    /**
     * Return a new query based on this query with an additional where clause added.
     * @param predicate A Predicate representing the where clause to add.
     * @return A new EntityQuery.
     */
    public EntityQuery where(Predicate predicate) {
        EntityQuery eq = new EntityQuery(this);
        if (eq._wherePredicate == null) {
            eq._wherePredicate = predicate;
        } else if (eq._wherePredicate.getOperator() == Operator.And) {
            AndOrPredicate andOrPred = (AndOrPredicate) eq._wherePredicate;
            List<Predicate> preds = new ArrayList<Predicate>(andOrPred.getPredicates());
            preds.add(predicate);
            eq._wherePredicate = new AndOrPredicate(Operator.And, preds);
        } else {
            eq._wherePredicate = new AndOrPredicate(Operator.And,
                    eq._wherePredicate, predicate);
        }
        return eq;
    }

    /**
     * Return a new query based on this query with the specified orderBy clauses added.
     * @param propertyPaths A varargs array of orderBy clauses ( each consisting of a property path and an optional sort direction).
     * @return A new EntityQuery.
     */
    public EntityQuery orderBy(String... propertyPaths) {
        return orderBy(Arrays.asList(propertyPaths));
    }

    /**
     * Return a new query based on this query with the specified orderBy clauses added.
     * @param propertyPaths An List of orderBy clauses ( each consisting of a property path and an optional sort direction).
     * @return A new EntityQuery.
     */
    public EntityQuery orderBy(List<String> propertyPaths) {
        EntityQuery eq = new EntityQuery(this);
        if (this._orderByClause == null) {
            eq._orderByClause = new OrderByClause(propertyPaths);
        } else {
            List<String> propPaths = new ArrayList<String>(
                    this._orderByClause.getPropertyPaths());
            propPaths.addAll(propertyPaths);
            eq._orderByClause = new OrderByClause(propPaths);
        }
        return eq;
    }
    
    /**
     * Return a new query based on this query with the specified expand clauses added.
     * @param propertyPaths A varargs array of expand clauses ( each a dot delimited property path).
     * @return A new EntityQuery.
     */
    public EntityQuery expand(String... propertyPaths) {
        return expand(Arrays.asList(propertyPaths));
    }

    /**
     * Return a new query based on this query with the specified expand clauses added.
     * @param propertyPaths A list of expand clauses (each a dot delimited property path).
     * @return A new EntityQuery.
     */
    public EntityQuery expand(List<String> propertyPaths) {
        EntityQuery eq = new EntityQuery(this);
        if (this._expandClause == null) {
            eq._expandClause = new ExpandClause(propertyPaths);
        } else {
            // think about checking if any prop paths are duped.
            List<String> propPaths = new ArrayList<String>(
                    this._expandClause.getPropertyPaths());
            propPaths.addAll(propertyPaths);
            eq._expandClause = new ExpandClause(propPaths);
        }
        return eq;
    }
    
    /**
     * Return a new query based on this query with the specified select (projection) clauses added.
     * @param propertyPaths A varargs array of select clauses (each a dot delimited property path).
     * @return A new EntityQuery.
     */
    public EntityQuery select(String... propertyPaths) {
        return select(Arrays.asList(propertyPaths));
    }

    /**
     * Return a new query based on this query with the specified select (projection) clauses added.
     * @param propertyPaths A list of select clauses (each a dot delimited property path).
     * @return A new EntityQuery.
     */
    public EntityQuery select(List<String> propertyPaths) {
        EntityQuery eq = new EntityQuery(this);
        if (this._selectClause == null) {
            eq._selectClause = new SelectClause(propertyPaths);
        } else {
            // think about checking if any prop paths are duped.
            List<String> propPaths = new ArrayList<String>(
                    this._selectClause.getPropertyPaths());
            propPaths.addAll(propertyPaths);
            eq._selectClause = new SelectClause(propPaths);
        }
        return eq;
    }

    
    /**
     * Return a new query based on this query that limits the results to the first n records.
     * @param takeCount The number of records to take.
     * @return A new EntityQuery
     */
    public EntityQuery take(Integer takeCount) {
        EntityQuery eq = new EntityQuery(this);
        eq._takeCount = takeCount;
        return eq;
    }
    
    /**
     * Return a new query based on this query that skips the first n records.
     * @param skipCount The number of records to skip.
     * @return A new EntityQuery
     */
    public EntityQuery skip(Integer skipCount) {
        EntityQuery eq = new EntityQuery(this);
        eq._skipCount = skipCount;
        return eq;
    }
    
    /**
     * Return a new query based on this query that either adds or removes the inline count capability. 
     * @param inlineCountEnabled Whether to enable inlineCount.
     * @return A new EntityQuery
     */
    public EntityQuery enableInlineCount(boolean inlineCountEnabled) {
        EntityQuery eq = new EntityQuery(this);
        eq._inlineCountEnabled = inlineCountEnabled;
        return eq;
    }
    
    /**
     * Return a new query based on this query with the specified resourceName 
     * @param resourceName The name of the url resource.
     * @return A new EntityQuery
     */
    public EntityQuery withResourceName(String resourceName) {
        EntityQuery eq = new EntityQuery(this);
        eq._resourceName = resourceName;
        return eq;
    }


    @SuppressWarnings("unchecked")
    private List<String> toStringList(Object src) {
        if (src == null)
            return null;
        if (src instanceof List) {
            return (List<String>) src;
        } else if (src instanceof String) {
            return StringFns.ToList((String) src);
        }
        throw new RuntimeException("Unable to convert to a List<String>");
    }

    private Integer processCount(Object o) {
        if (o == null)
            return null;
        return ((Double) o).intValue();
    }

    /**
     * Validates that all of the clauses that make up this query are consistent with the 
     * specified EntityType.
     * @param entityType A EntityType
     */
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

    
    /**
     * Returns the EntityType that this query has been validated against. Not that this property
     * will return null until the validate method has been called.
     * @return The EntityType that this query has been validated against.
     */
    public IEntityType getEntityType() {
        return _entityType;
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
    
    public Map getParameters() {
        return _parameters;
    }

}
