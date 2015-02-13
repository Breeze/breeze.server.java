package com.breezejs.metadata;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

public class DataType {
	private String _name;
	private Class _class;
	private static HashMap<String, DataType> _nameMap = new HashMap<String, DataType>();
	
	public static final DataType Binary = new DataType("Binary");
	public static final DataType Guid = new DataType("Guid", UUID.class);
	public static final DataType String = new DataType("String", String.class);
	
	public static final DataType DateTime = new DataType("DateTime", Date.class);
	public static final DataType DateTimeOffset = new DataType("DateTimeOffset", Date.class);
	public static final DataType Time = new DataType("Time");
	
	public static final DataType Byte = new DataType("Byte", Byte.class);
	public static final DataType Int16 = new DataType("Int16", Short.class );
	public static final DataType Int32 = new DataType("Int32", Integer.class);
	public static final DataType Int64 = new DataType("Int64", Long.class);
	public static final DataType Boolean = new DataType("Boolean", Boolean.class);
	
	public static final DataType Decimal = new DataType("Decimal", BigDecimal.class);
	public static final DataType Double = new DataType("Double", Double.class);
	public static final DataType Single = new DataType("Single", Float.class);
	
 
	
	public DataType(String name) {
		_name = name;
		_nameMap.put(name, this);
	}
	
	public DataType(String name, Class clazz) {
		_name = name;
		_class = clazz;
		_nameMap.put(name, this);
	}

	
	public String getName() {
		return _name;
	}
	
	public Class getJavaClass() {
		return _class;
	}
	
	public static DataType fromName(String name) {
		return _nameMap.get(name);
	}
	
	// Can't use this safely because of missing support for optional parts.
	// private static DateFormat ISO8601_Format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
	
	public static Object coerceData(Object value, DataType dataType) {

		if (value == null || value.getClass() == dataType.getJavaClass()) {
			return value;
		} else if (dataType == DataType.Int16) {
			return Short.parseShort(value.toString());
		} else if (dataType == DataType.Int32) {
			return Integer.parseInt(value.toString());
		} else if (dataType == DataType.Int64) {
			return Long.parseLong(value.toString());
		} else if (dataType == DataType.Decimal) {
			Object dValue = coerceData(value, DataType.Double);
			return BigDecimal.valueOf((Double)dValue);
		} else if (dataType == DataType.Double) {
			return java.lang.Double.parseDouble(value.toString());
		} else if (dataType == DataType.Single) {
			return Float.parseFloat(value.toString());
		} else if (dataType == DataType.Byte) {
			return java.lang.Byte.parseByte(value.toString());
		} else if (dataType == DataType.DateTime || dataType == DataType.DateTimeOffset) {
			return javax.xml.bind.DatatypeConverter.parseDateTime(value.toString()).getTime(); 
		}
		
		return value;
	}

}
