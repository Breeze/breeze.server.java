package com.breeze.util;

import java.util.ArrayList;
import java.util.List;

public class StringFns {
	
	public static List<String> ToList(String source) {
		return ToList(source, ",");
	}
	
	public static List<String> ToList(String source, String delimiter) {
		ArrayList<String> strings = new ArrayList<String>();
    	String[] items = source.split(delimiter);
    	for (String item : items) {
    		// individual strings are trimmed
            strings.add(item.trim());
        }
    	return strings;
	}
	
	

}
