package com.breezejs.query;

import java.util.ArrayList;
import java.util.List;

import com.breezejs.metadata.IEntityType;

public class FnExpressionToken {
	private StringBuilder _sb;
	private int _nextIx; 
	private List<FnExpressionToken> _fnArgs; 
	
	public FnExpressionToken() {
		_sb = new StringBuilder();
	}

	public static FnExpressionToken fromString(String source) {
		return parseToken(source, 0);
	}
	
	public FnExpression toExpression(IEntityType entityType) {

		String fnName = this._sb.toString();
		List<Expression> exprs = new ArrayList<Expression>();
		for (FnExpressionToken argToken: this._fnArgs) {
			Expression expr = argToken.toExpression(entityType);
			exprs.add(expr);
		}
		return new FnExpression(fnName, exprs);

	}
	
	private static FnExpressionToken parseToken(String source, int ix) {
		ix = skipWhitespace(source, ix);
		FnExpressionToken token = collectQuotedToken(source, ix);
		if (token != null) {
			return token;
		}
		token = new FnExpressionToken();
		String badChars = ")'\"";
		
		while (ix < source.length()) {
			char c = source.charAt(ix);
			
			if (c == '(') {
				ix++;
				parseFnArgs(token, source, ix);
				return token;
			}
			
			if (c == ',') {
				ix++;
				token._nextIx = ix;
				return token;
			}
			
			if (badChars.indexOf(c) >= 0) {
				throw new RuntimeException("Unable to parse Fn name - encountered: " + c);
			}
			token._sb.append(c);
			ix++;
		}
		token._nextIx = ix;
		return token;
		
	}
	
	private static void parseFnArgs(FnExpressionToken token, String source, int ix) {
		token._fnArgs = new ArrayList<FnExpressionToken>();
	
		while (ix < source.length()) {
			char c = source.charAt(ix);
			
			if (c == ')') {
				ix++;
				break;
			}
			
			FnExpressionToken argToken = parseToken(source, ix);
			token._fnArgs.add(argToken);
			ix = argToken._nextIx;
		}
		token._nextIx = ix;
		return;
		
	}
	
	private static int skipWhitespace(String source, int ix) {
		while (ix < source.length()) {
			char c = source.charAt(ix);
			if (c == ' ') {
				ix++;
			} else {
				return ix;
			}
		}
		return ix;
	}
	
	private static FnExpressionToken collectQuotedToken(String source, int ix) {
		char c = source.charAt(ix);
		if (c != '\'' && c != '"') return null;
		FnExpressionToken token = new FnExpressionToken();
		char quoteChar = c;
		ix++;
		while (ix < source.length()) {
			c = source.charAt(ix);
			ix++;
			if (c == quoteChar ) {
				return token;
			} else {
				token._sb.append(c);
			}
		}
		throw new RuntimeException("Quoted token was not terminated"); 
	}
}

