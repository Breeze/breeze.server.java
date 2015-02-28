package com.breeze.query;

import java.util.List;
import java.util.Map;

import com.breeze.metadata.DataType;
import com.breeze.metadata.IEntityType;
import com.breeze.metadata.IProperty;
import com.breeze.metadata.MetadataHelper;
import com.breeze.util.TypeFns;

public abstract class Expression {

	// will return either a PropExpression or a FnExpression
	public static Expression createLHSExpression(Object exprSource,
			IEntityType entityType) {
		if (exprSource == null) {
			throw new RuntimeException(
					"Null expressions are only permitted on the right hand side of a BinaryPredicate");
		}

		if (exprSource instanceof Map) {
			throw new RuntimeException(
					"Object expressions are only permitted on the right hand side of a BinaryPredicate");
		}

		if (exprSource instanceof List) {
			throw new RuntimeException(
					"Array expressions are only permitted on the right hand side of a BinaryPredicate");
		}

		if (!(exprSource instanceof String)) {
			throw new RuntimeException(
					"Only string expressions are permitted on this predicate");
		}
		
		String source = (String) exprSource;
		if (source.indexOf("(") == -1) {
			return new PropExpression(source, entityType);
		} else {
			return FnExpression.createFrom(source,  entityType);	
		}
		

	}

	// will return either a PropExpression or a LitExpression
	public static Expression createRHSExpression(Object exprSource,
			IEntityType entityType, DataType otherExprDataType) {
		
		if (exprSource == null || TypeFns.isPrimitive(exprSource)) {
			return new LitExpression(exprSource, otherExprDataType);
		}
		
		if (exprSource instanceof String) {
			String source = (String) exprSource;
			if (entityType == null) {
				// if entityType is unknown then assume that the rhs is a
				// literal
				return new LitExpression(source, otherExprDataType);
			}
			IProperty prop = MetadataHelper.getPropertyFromPath(source,
					entityType);
			if (prop == null) {
				return new LitExpression(source, otherExprDataType);
			} else {
				return new PropExpression(source, entityType);
			}
		}

		if (exprSource instanceof Map) {
			Map exprMap = (Map) exprSource;
			// note that this is NOT the same a using get and checking for null
			// because null is a valid 'value'.
			if (!exprMap.containsKey("value")) {
				throw new RuntimeException(
						"Unable to locate a 'value' property on: "
								+ exprMap.toString());
			}
			Object value = exprMap.get("value");

			if (exprMap.containsKey("isProperty")) {
				return new PropExpression((String) value, entityType);
			} else {
				String dt = (String) exprMap.get("dataType");
				DataType dataType = (dt != null) ? DataType.fromName(dt) : otherExprDataType;
				return new LitExpression(value, dataType );
			}
		}

	

		if (exprSource instanceof List) {
			// right now this pretty much implies the values on an 'in' clause
			return new LitExpression(exprSource, otherExprDataType);
		}

		throw new RuntimeException(
				"Unable to parse the right hand side of this BinaryExpression: "
						+ exprSource.toString());

	}
	
	public abstract DataType getDataType();

}
