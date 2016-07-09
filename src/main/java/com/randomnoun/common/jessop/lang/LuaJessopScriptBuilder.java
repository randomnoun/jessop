package com.randomnoun.common.jessop.lang;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import org.apache.log4j.Logger;

import com.randomnoun.common.jessop.AbstractJessopScriptBuilder;
import com.randomnoun.common.jessop.JessopScriptBuilder;

public class LuaJessopScriptBuilder extends AbstractJessopScriptBuilder implements JessopScriptBuilder {

    /** A revision marker to be used in exception stack traces. */
    public static final String _revision = "$Id$";
	Logger logger = Logger.getLogger(LuaJessopScriptBuilder.class);
	int outputLine = 1;        // current line number in the target script;
	int lastScriptletLine = 1; // the last line number of the last scriptlet (used for suppressEol)

	public LuaJessopScriptBuilder() {
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
	private static String escapeLua(String string) {
		/* valid escapes ( https://www.lua.org/pil/2.4.html )
\a	bell
\b	back space
\f	form feed
\n	newline
\r	carriage return
\t	horizontal tab
\v	vertical tab
\\	backslash
\"	double quote
\'	single quote
\[	left square bracket
\]	right square bracket
		 */
		
    	StringBuilder sb = new StringBuilder(string.length());
    	String escapeChars = "\u0007" + "\u0008" + "\u000f" + "\n" + "\r" + "\u0009" + "\u000b" + "\\" + "\"" + "'" + "[" + "]";
    	String backslashChars = "abfnrtv\\\"'[]";
		for (int i = 0; i<string.length(); i++) {
			char ch = string.charAt(i);
			int pos = escapeChars.indexOf(ch);
			if (pos !=- 1) {
			   sb.append("\\" + backslashChars.charAt(pos));
			
			// so apparently lua allows any character in a string whatsoever. looking forward to seeing this breaking.
			// have seen some examples of, e.g. \006 to represent chars, but nothing in the lua spec
			   
			//} else if (ch=='\\' || ch=='"' || ch=='\'' || ch<32 && ch>126) {
			//	String hex = Integer.toString(ch, 16);
			//	// sb.append("\\u" + "0000".substring(0, 4-hex.length()) + hex);
			//	sb.append(ch); 
			} else {
				sb.append(ch);
			}
		}
        return sb.toString();
    }
	
	@Override
	public void emitText(int line, String s) {
		skipToLine(line);
		s = suppressEol(s, declarations.isSuppressEol() && lastScriptletLine == line);
		print("out:write(\"" + escapeLua(s) + "\")");
		lastScriptletLine = 0; // don't suppress eols on this line
	}
	@Override
	public void emitExpression(int line, String s) {
		skipToLine(line);
		print("out:write((" + s + ") .. \"\")"); // coerce to String
		lastScriptletLine = 0; // don't suppress eols on this line
	}
	@Override
	public void emitScriptlet(int line, String s) {
		skipToLine(line);
		print(s);
		lastScriptletLine = line;
		for (int i=0; i<s.length(); i++) { if (s.charAt(i)=='\n') { lastScriptletLine++; } }
	}
	@Override
	public String getLanguage() {
		return "lua";
	}
	@Override
	public String getDefaultScriptEngineName() {
		return "luaj";
	}
	@Override
	public String getDefaultExceptionConverterClassName() {
		return "com.randomnoun.common.jessop.engine.LuajExceptionConverter";
	}

	

}