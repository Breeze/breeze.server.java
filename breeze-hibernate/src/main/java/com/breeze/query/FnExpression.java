package com.breeze.query;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import com.breeze.metadata.DataType;
import com.breeze.metadata.IEntityType;

public class FnExpression extends Expression {
    private String _fnName;
    private List<Expression> _exprs;

    // first DataType in the list is the return type the rest are argument
    // types;
    private static HashMap<String, DataType[]> _fnMap = new HashMap<String, DataType[]>();
    static {
        registerFn("toupper", DataType.String, DataType.String);
        registerFn("tolower", DataType.String, DataType.String);
        registerFn("trim", DataType.String, DataType.String);
        registerFn("concat", DataType.String, DataType.String, DataType.String);
        registerFn("substring", DataType.String, DataType.String,
                DataType.Int32, DataType.Int32);
        registerFn("replace", DataType.String, DataType.String, DataType.String);
        registerFn("length", DataType.Int32, DataType.String);
        registerFn("indexof", DataType.Int32, DataType.String, DataType.String);

        registerFn("year", DataType.Int32, DataType.DateTime);
        registerFn("month", DataType.Int32, DataType.DateTime);
        registerFn("day", DataType.Int32, DataType.DateTime);
        registerFn("minute", DataType.Int32, DataType.DateTime);
        registerFn("second", DataType.Int32, DataType.DateTime);

        registerFn("round", DataType.Int32, DataType.Double);
        registerFn("ceiling", DataType.Int32, DataType.Double);
        registerFn("floor", DataType.Int32, DataType.Double);

        registerFn("substringof", DataType.Boolean, DataType.String,
                DataType.String);
        registerFn("startsWith", DataType.Boolean, DataType.String,
                DataType.String);
        registerFn("endsWith", DataType.Boolean, DataType.String,
                DataType.String);
    }

    public FnExpression(String fnName, List<Expression> exprs) {
        _fnName = fnName;
        _exprs = exprs;
    }

    public static FnExpression createFrom(String source, IEntityType entityType) {
        return FnExpressionToken.toExpression(source, entityType);
    }

    public String getFnName() {
        return _fnName;
    }

    public List<Expression> getExpressions() {
        return _exprs;
    }

    public DataType getDataType() {
        return getReturnType(_fnName);
    }

    public static void registerFn(String name, DataType... dataTypes) {
        _fnMap.put(name.toLowerCase(), dataTypes);
    }

    public static DataType getReturnType(String fnName) {
        DataType[] dataTypes = _fnMap.get(fnName.toLowerCase());
        return (dataTypes != null) ? dataTypes[0] : null;
    }

    public static DataType[] getArgTypes(String fnName) {
        DataType[] dataTypes = _fnMap.get(fnName.toLowerCase());
        if (dataTypes == null) {
            throw new RuntimeException("Unable to recognize a function named: "
                    + fnName);
        }
        return Arrays.copyOfRange(dataTypes, 1, dataTypes.length);
    }
}