package com.breezejs.query;

import java.util.HashMap;
import java.util.List;

import com.breezejs.util.StringFns;

public class Operator {
	public static final HashMap<String, Operator>_opMap = new HashMap<String, Operator>();
	public static Operator Any = new Operator("any,some", OperatorType.AnyAll);
	public static Operator All = new Operator("all,every", OperatorType.AnyAll);
	public static Operator And = new Operator("and,&&", OperatorType.AndOr);
	public static Operator Or = new Operator("or,||", OperatorType.AndOr);
	public static Operator Not = new Operator("not,!,~", OperatorType.Unary);
	
	public static BinaryOperator Equals = new BinaryOperator("eq,==");
	public static BinaryOperator NotEquals = new BinaryOperator("ne,!=");
	public static BinaryOperator LessThan = new BinaryOperator("lt,<");
	public static BinaryOperator LessThanOrEqual = new BinaryOperator("le,<=");
	public static BinaryOperator GreaterThan = new BinaryOperator("gt,>");
	public static BinaryOperator GreaterThanOrEqual = new BinaryOperator("ge,>=");
	
	public static BinaryOperator StartsWith = new BinaryOperator("startswith");
	public static BinaryOperator EndsWith = new BinaryOperator("endswith");
	public static BinaryOperator Contains = new BinaryOperator("contains");
	
	public List<String> _aliases;
	public String _name;
	public OperatorType _type;
	
	public static Operator fromString(String op) {
		 return _opMap.get(op.toLowerCase());
	}
	
	public Operator(String aliases, OperatorType type) {
		_aliases = StringFns.ToList(aliases);
		
		_name = _aliases.get(0);
		_type = type;
		AddOperator(this);
	}
	
	public OperatorType getType() {
		return _type;
	}
	
	public String getName() {
		return _name;
	}
	
	private static void AddOperator(Operator op) {
		for (String opName: op._aliases) {
			_opMap.put(opName.toLowerCase(), op);	
		}
	}
}
