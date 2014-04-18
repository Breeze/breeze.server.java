package com.breezejs;

import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import com.breezejs.util.Reflect;

public class QueryResultBeanInfo extends SimpleBeanInfo {
	@Override
	public PropertyDescriptor[] getPropertyDescriptors() {
		return Reflect.makePropertyDescriptors(QueryResult.class, "Results", "InlineCount");
	}

}
