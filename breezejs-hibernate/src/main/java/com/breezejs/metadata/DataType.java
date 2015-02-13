package com.breezejs.metadata;

import java.util.HashMap;

public class DataType {
	private String _name;
	private static HashMap<String, DataType> _nameMap = new HashMap<String, DataType>();
	
	public static final DataType Binary = new DataType("Binary");
	public static final DataType Guid = new DataType("Guid");
	public static final DataType String = new DataType("String");
	
	public static final DataType DateTime = new DataType("DateTime");
	public static final DataType DateTimeOffset = new DataType("DateTimeOffset");
	public static final DataType Time = new DataType("Time");
	
	public static final DataType Byte = new DataType("Byte");
	public static final DataType Int16 = new DataType("Int16");
	public static final DataType Int32 = new DataType("Int32");
	public static final DataType Int64 = new DataType("Int64");
	public static final DataType Boolean = new DataType("Boolean");
	
	public static final DataType Decimal = new DataType("Decimal");
	public static final DataType Double = new DataType("Double");
	public static final DataType Single = new DataType("Single");
	
	
	public DataType(String name) {
		_name = name;
		_nameMap.put(name, this);
	}
	
	public String getName() {
		return _name;
	}
	
	public static DataType fromName(String name) {
		return _nameMap.get(name);
	}
}
