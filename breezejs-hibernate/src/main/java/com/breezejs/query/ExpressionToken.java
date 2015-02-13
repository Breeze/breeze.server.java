package com.breezejs.query;

import java.util.ArrayList;
import java.util.List;

public class ExpressionToken {
	public StringBuilder _sb;
	// public char endsWith;
	public int _nextIx; 
	public List<ExpressionToken> _fnArgs; // only fns have args;
	public ExpressionToken() {
		_sb = new StringBuilder();
	}

	public static ExpressionToken fromString(String source) {
		return parseToken(source, 0);
	}
	
	public Expression toExpression(ExpressionContext exprContext) {
		if (this._fnArgs == null) {
			return new PropExpression(this._sb.toString(), exprContext);
		} else {
			String fnName = this._sb.toString();
			List<Expression> exprs = new ArrayList<Expression>();
			for (ExpressionToken argToken: this._fnArgs) {
				Expression expr = argToken.toExpression(exprContext);
				exprs.add(expr);
			}
			return new FnExpression(fnName, exprs);
		}
	}
	


	private static ExpressionToken parseToken(String source, int ix) {
		ix = skipWhitespace(source, ix);
		ExpressionToken token = collectQuotedToken(source, ix);
		if (token != null) {
			return token;
		}
		token = new ExpressionToken();
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
	
	private static void parseFnArgs(ExpressionToken token, String source, int ix) {
		token._fnArgs = new ArrayList<ExpressionToken>();
	
		while (ix < source.length()) {
			char c = source.charAt(ix);
			
			if (c == ')') {
				ix++;
				break;
			}
			
			ExpressionToken argToken = parseToken(source, ix);
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
	
	private static ExpressionToken collectQuotedToken(String source, int ix) {
		char c = source.charAt(ix);
		if (c != '\'' && c != '"') return null;
		ExpressionToken token = new ExpressionToken();
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

