package com.breezejs;

import java.util.Collection;

import com.breezejs.util.JsonGson;

/**
 * Wrapper for results that have an InlineCount, to support paged result sets.
 * @author Steve
 */
public class QueryResult {
	private Collection results;
	private Long inlineCount;
	
	public QueryResult(Collection results) {
		this.results = results;
		this.inlineCount = null;
	}
	
	public QueryResult(Collection results, Long inlineCount) {
		this.results = results;
		this.inlineCount = inlineCount;
	}
	
	public Collection getResults() {
		return results;
	}
	public void setResults(Collection results) {
		this.results = results;
	}
	public Long getInlineCount() {
		return inlineCount;
	}
	public void setInlineCount(Long inlineCount) {
		this.inlineCount = inlineCount;
	}
	
	public String toJson() {
		if (inlineCount == null) {
			return JsonGson.toJson(results);
		} else {
			return JsonGson.toJson(this);
		}
	}
	
}
