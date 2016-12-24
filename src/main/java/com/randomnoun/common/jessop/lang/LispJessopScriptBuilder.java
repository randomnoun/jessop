package com.randomnoun.common.jessop.lang;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.randomnoun.common.jessop.AbstractJessopScriptBuilder;
import com.randomnoun.common.jessop.JessopScriptBuilder;

public class LispJessopScriptBuilder extends AbstractJessopScriptBuilder implements JessopScriptBuilder {

    /** A revision marker to be used in exception stack traces. */
    public static final String _revision = "$Id$";
	Logger logger = Logger.getLogger(LispJessopScriptBuilder.class);
	int outputLine = 1;        // current line number in the target script;
	int lastScriptletLine = 1; // the last line number of the last scriptlet (used for suppressEol)

	public LispJessopScriptBuilder() {
	}
	private void skipToLine(int line) {
		while (outputLine < line) { print("\n"); }
	}
	private void print(String s) {
		// logger.info("** PRINT " + s);
		pw.print(s);
		for (int i=0; i<s.length(); i++) {
			if (s.charAt(i)=='\n') { outputLine++; } 
		}
	}
	
	
	private static List<String> escapeLisp(String string) {
		// lisp doesn't appear to have an inline escape syntax. so that's terrific, isn't it.
		// instead, you need to put in ~a placeholders which are then filled by 
		// (code-char #x000d) parameters to the format function, or in abcl, a possibly-nonstandard
		// '#\\U000D parameter.		

		// also, '~' characters need to be doubled-up.
		List<String> result = new ArrayList<String>();
		result.add("");
		
    	StringBuilder sb = new StringBuilder(string.length());
    	//String escapeChars = "\u0007" + "\u0008" + "\u000f" + "\n" + "\r" + "\u0009" + "\u000b" + "\u001b" + "\\" + "\"";
    	//String backslashChars = "abfnrtve\\\"";
		for (int i = 0; i<string.length(); i++) {
			char ch = string.charAt(i);
			if (ch=='~') {
				sb.append("~~");
			} else if (ch=='\\') {
				sb.append("\\\\");
			} else if (ch=='"') {
				sb.append("\\\"");
			} else if (ch>=32 && ch<=127) {
				sb.append(ch);
			} else {
				sb.append("~a");
				String hex = Long.toString(ch, 16);
				result.add("'#\\U" + "0000".substring(0, 4-hex.length()) + hex);
			}
		}
		result.set(0, sb.toString());
		return result;
    }
	
	@Override
	public void emitText(int line, String s) {
		//if (outputLine == line) { print ("; "); }
		skipToLine(line);
		s = suppressEol(s, declarations.isSuppressEol() && lastScriptletLine == line);
		List<String> formatParams = escapeLisp(s);
		print("(format t \"" + formatParams.get(0) + "\"");
		for (int i=1; i<formatParams.size(); i++) {
			print(" " + formatParams.get(i));
		}
		print(")");
		lastScriptletLine = 0; // don't suppress eols on this line
	}
	@Override
	public void emitExpression(int line, String s) {
		//if (outputLine == line) { print ("; "); }
		skipToLine(line);
		print("(format t \"~a\" " + s + ")");  // 'aesthetically' print the value of s
		lastScriptletLine = 0; // don't suppress eols on this line
	}
	@Override
	public void emitScriptlet(int line, String s) {
		// if (outputLine == line) { print ("; "); }
		skipToLine(line);
		print(s);
		lastScriptletLine = line;
		for (int i=0; i<s.length(); i++) { if (s.charAt(i)=='\n') { lastScriptletLine++; } }
	}
	@Override
	public String getLanguage() {
		return "lisp";
	}
	@Override
	public String getDefaultScriptEngineName() {
		return "ABCL";
	}
	@Override
	public String getDefaultExceptionConverterClassName() {
		// return "com.randomnoun.common.jessop.engine.JRubyExceptionConverter";
		return null;
	}

	

}