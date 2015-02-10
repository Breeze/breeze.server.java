package com.breezejs.util;

import java.util.ArrayList;
import java.util.List;

public class StringFns {
	public static List<String> ToList(String source, String delimiter) {
		ArrayList<String> strings = new ArrayList<String>();
    	String[] items = source.split("\\,");
    	for (String item : items) {
            strings.add(item.trim());
        }
    	return strings;
	}
	

}
