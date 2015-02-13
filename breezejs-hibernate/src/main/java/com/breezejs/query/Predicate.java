package com.breezejs.query;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.breezejs.metadata.IEntityType;
import com.breezejs.util.TypeFns;

public abstract class Predicate {
	
	public static List<Predicate> predicatesFromMap(Map map) {
		List<Predicate> preds = new ArrayList<Predicate>();
		for (Object key : map.keySet()) {
			String skey = (String) key;
			Predicate pred =predicateFromKeyValue(skey, map.get(skey));
			preds.add(pred);
		}
		return preds;
	}

	public static Predicate predicateFromMap(Map sourceMap) {
		if (sourceMap == null) return null;
		List<Predicate> preds = predicatesFromMap(sourceMap);
		return createCompoundPredicate(preds);
	}
	
	public abstract void validate(IEntityType entityType);

	private static Predicate predicateFromKeyValue(String key, Object value) {
		Operator op = Operator.fromString(key);
		if (op != null) {
			if (op.getType() == OperatorType.AndOr) {
				List<Predicate> preds = predicatesFromObject(value);
				return new AndOrPredicate(op, preds);
			} else if (op.getType() == OperatorType.Unary) {
				Predicate pred = predicateFromObject(value);
				return new UnaryPredicate(op, pred);
			} else {
				throw new RuntimeException("Invalid operator in context: " + key);
			}
		}
		
		if (value == null || TypeFns.isPrimitiveOrString(value)) {
			return new BinaryPredicate(BinaryOperator.Equals, key, value);
		} else if (value instanceof Map && ((Map) value).containsKey("value")) {
			return new BinaryPredicate(BinaryOperator.Equals, key, value);
		}
		
		if (!(value instanceof Map)) {
			throw new RuntimeException("Unable to resolve value associated with key:" + key);
		}
		
		List<Predicate> preds = new ArrayList<Predicate>();
		Map map = (Map) value;

		
	    for (Object oKey : map.keySet()) {
			String subKey = (String) oKey;
			Operator subOp = Operator.fromString(subKey);
			Object subVal = map.get(subKey);
			Predicate pred;
			if (subOp != null) {
				if (subOp.getType() == OperatorType.AnyAll) {
					Predicate subPred = predicateFromObject(subVal);
					pred = new AnyAllPredicate(subOp, key, subPred);
				} else if (subOp.getType() == OperatorType.Binary) {
					pred = new BinaryPredicate(subOp, key, subVal);
				} else {
					throw new RuntimeException("Unable to resolve OperatorType for key: " + subKey);
				}
			} else if (subVal instanceof Map && ((Map) subVal).get("value") != null) {
				pred = new BinaryPredicate(BinaryOperator.Equals, key, subVal );	
			} else {
				throw new RuntimeException("Unable to resolve predicate after: " + key);
			}
			preds.add(pred);			
	    }
	    return createCompoundPredicate(preds);
	}
	
	private static Predicate predicateFromObject(Object source) {
		List<Predicate> preds = predicatesFromObject(source);
		if (preds.size() > 1) {
			throw new RuntimeException("should only contain a single item");
		}
		return preds.get(0);
	}

	private static List<Predicate> predicatesFromObject(Object source) {
		List<Predicate> preds = new ArrayList<Predicate>();
		if (source instanceof Map) {
			preds = predicatesFromMap((Map) source);
		} else if (source instanceof List) {
			for (Object item: (List) source) {
				Predicate pred = predicateFromObject(item);
     			preds.add(pred);
			}
		}
		return preds;
	}
	
	private static Predicate createCompoundPredicate(List<Predicate> preds) {
		if (preds.size() > 1) {
			return new AndOrPredicate(Operator.And, preds);
		} else {
			return preds.get(0);
		}
	}
}
