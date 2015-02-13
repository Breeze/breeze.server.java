package com.breezejs.query;

import java.util.List;
import java.util.Map;

import com.breezejs.metadata.DataType;
import com.breezejs.metadata.IProperty;
import com.breezejs.metadata.MetadataHelper;

public abstract class Expression {

	// LHS expr
	public static Expression createPropOrFnExpr(Object exprSource,
			ExpressionContext exprContext) {
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

		return ExpressionToken.fromString((String) exprSource).toExpression(exprContext);

	}

	// RHS expr
	public static Expression createPropOrLitExpr(Object exprSource,
			ExpressionContext exprContext, DataType otherExprDataType) {

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
				return new PropExpression((String) value, exprContext);
			} else {
				String dt = (String) exprMap.get("dataType");
				DataType dataType = (dt != null) ? DataType.fromName(dt) : otherExprDataType;
				return new LitExpression(value, dataType, true);
			}
		}

		if (exprSource instanceof String) {
			String source = (String) exprSource;
			if (exprContext.entityType == null) {
				// if entityType is unknown then assume that the rhs is a
				// literal
				return new LitExpression(source, otherExprDataType, false);
			}
			IProperty prop = MetadataHelper.getPropertyFromPath(source,
					exprContext);
			if (prop == null) {
				return new LitExpression(source, otherExprDataType, false);
						
			} else {
				return new PropExpression(source, exprContext);
			}
		}

		if (exprSource instanceof List) {
			// right now this pretty much implies the values on an 'in' clause
			return new LitExpression(exprSource, otherExprDataType, false);
		}

		throw new RuntimeException(
				"Unable to parse the right hand side of this BinaryExpression: "
						+ exprSource.toString());

	}
	
	public abstract DataType getDataType();

}
