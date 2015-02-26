package com.breezejs.query;

import java.util.ArrayList;
import java.util.List;

import com.breezejs.metadata.DataType;
import com.breezejs.metadata.IEntityType;
import com.breezejs.metadata.IProperty;
import com.breezejs.metadata.MetadataHelper;

// local to this package
class FnExpressionToken {
	private StringBuilder _sb;
	private int _nextIx; 
	private boolean _isQuoted;
	private List<FnExpressionToken> _fnArgs; 
	
	private FnExpressionToken() {
		_sb = new StringBuilder();
	}

	public static FnExpression toExpression(String source, IEntityType entityType) {
		FnExpressionToken token = parseToken(source, 0);
		return (FnExpression) token.toExpression(entityType, null);
	}
	
	private Expression toExpression(IEntityType entityType, DataType returnDataType) {
		String text = _sb.toString(); 
		if (this._fnArgs == null) {
			if (_isQuoted) {
				// TODO: we could check that the returnDataType is a String
				return new LitExpression(entityType, DataType.String);
			} else {
				IProperty prop = MetadataHelper.getPropertyFromPath(text,
						entityType);
				if (prop == null) {
					return new LitExpression(text, returnDataType);
				} else {
					// TODO: we could check that the PropExpression dataType is compatible with the returnDataType
					return new PropExpression(text, entityType);
				}
			}
		} else {
			String fnName = text;
			DataType[] argTypes = FnExpression.getArgTypes(fnName);
			if (argTypes.length != _fnArgs.size()) {
			    throw new RuntimeException("Incorrect number of arguments to '" + fnName 
			            + "' function; was expecting " + argTypes.length);
			}
			List<Expression> exprs = new ArrayList<Expression>();
			for (int fnIx = 0; fnIx < _fnArgs.size(); fnIx++) {
				FnExpressionToken argToken = _fnArgs.get(fnIx);
				Expression expr = argToken.toExpression(entityType, argTypes[fnIx] );
				exprs.add(expr);
			}
			// TODO: we could check that the FnExpression dataType is compatible with the returnDataType
			return new FnExpression(text, exprs);
		}

	}
	
	private static FnExpressionToken parseToken(String source, int ix) {
		ix = skipWhitespace(source, ix);
		FnExpressionToken token = collectQuotedToken(source, ix);
		if (token != null) {
			return token;
		}
		token = new FnExpressionToken();
		String badChars = "'\"";
		
		while (ix < source.length()) {
			char c = source.charAt(ix);
			
			if (c == '(') {
				ix++;
				parseFnArgs(token, source, ix);
				return token;
			}
			
			if (c == ',' || c == ')') {
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
						
			FnExpressionToken argToken = parseToken(source, ix);
			ix = argToken._nextIx;
			if (argToken._sb.length() != 0) {
    			token._fnArgs.add(argToken);
			}
			char c = source.charAt(ix);
			ix++;
	        if (c == ')') break;
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
				token._nextIx = ix;
				return token;
			} else {
				token._sb.append(c);
			}
		}
		throw new RuntimeException("Quoted token was not terminated"); 
	}
}

