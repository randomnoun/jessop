package com.randomnoun.common.jessop.lang;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.luaj.vm2.LuaError;

import com.randomnoun.common.jessop.AbstractJessopScriptBuilder;
import com.randomnoun.common.jessop.JessopScriptBuilder;

// exactly the same as the JavascriptJessopScriptBuilder
// string coercion is a bit different
public class JavaJessopScriptBuilder extends AbstractJessopScriptBuilder implements JessopScriptBuilder {

    /** A revision marker to be used in exception stack traces. */
    public static final String _revision = "$Id$";
	Logger logger = Logger.getLogger(JavaJessopScriptBuilder.class);
	int outputLine = 1;        // current line number in the target script;
	int lastScriptletLine = 1; // the last line number of the last scriptlet (used for suppressEol)

	public JavaJessopScriptBuilder() { 
	}
	private void skipToLine(int line) {
		// skipToLines are only used in the target script, so aren't affected by suppressEol
		while (outputLine < line) { print("\n"); }
	}
	private void print(String s) {
		// logger.info("** PRINT " + s);
		pw.print(s);
		for (int i=0; i<s.length(); i++) {
			if (s.charAt(i)=='\n') { outputLine++; } 
		}
	}
	private static String escapeJava(String string) {
    	StringBuilder sb = new StringBuilder(string.length());
		for (int i = 0; i<string.length(); i++) {
			char ch = string.charAt(i);
			if (ch=='\n') {
			   sb.append("\\n");	
			} else if (ch=='\\' || ch=='"' || ch=='\'' || ch<32 && ch>126) {
				String hex = Integer.toString(ch, 16);
				sb.append("\\u" + "0000".substring(0, 4-hex.length()) + hex);
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
		print("out.write(\"" + escapeJava(s) + "\");");
		lastScriptletLine = 0; // don't suppress eols on this line
	}
	@Override
	public void emitExpression(int line, String s) {
		skipToLine(line);
		print("out.write(\"\" + (" + s + "));"); // coerce to String
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
		return "java";
	}
	@Override
	public String getDefaultScriptEngineName() {
		return "beanshell";
	}
	@Override
	public String getDefaultExceptionConverterClassName() {
		return "com.randomnoun.common.jessop.engine.BeanshellExceptionConverter";
	}

}