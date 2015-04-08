package com.breeze.query;

import java.util.List;

import com.breeze.util.JsonGson;

/**
 * Wrapper for results that have an InlineCount, to support paged result sets.
 * @author Steve
 */
public class QueryResult {
	private List<?> results;
	private Long inlineCount;
	
	public QueryResult(List results) {
		this.results = results;
		this.inlineCount = null;
	}
	
	public QueryResult(List results, Long inlineCount) {
		this.results = results;
		this.inlineCount = inlineCount;
	}
	
	public List getResults() {
		return results;
	}
	public void setResults(List results) {
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
			return JsonGson.toJson(results, true, true);
		} else {
			return JsonGson.toJson(this, true, true);
		}
	}
	
}
