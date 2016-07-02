package com.randomnoun.common.jessop.lang;

import java.io.Reader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.luaj.vm2.LuaError;

import com.randomnoun.common.jessop.AbstractJessopScriptBuilder;
import com.randomnoun.common.jessop.JessopScriptBuilder;

public class LuaJessopScriptBuilder extends AbstractJessopScriptBuilder implements JessopScriptBuilder {
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

	/* because lua's special */ 
	@Override
	public ScriptException toScriptException(ScriptContext scriptContext, Throwable t) {
		if (t instanceof ScriptException) { 
			return (ScriptException) t;
		} else if (t instanceof LuaError) {
			// there's a 'fileline' attribute in the LuaError, which isn't public, but is
			// set in the LuaClosure class to
			// le.fileline = (p.source != null? p.source.tojstring(): "?") + ":" 
			//  + (p.lineinfo != null && pc >= 0 && pc < p.lineinfo.length? String.valueOf(p.lineinfo[pc]): "?");
			// it's prepended to the error message
			
			LuaError le = (LuaError) t;
			String msg = le.getMessage();
			Pattern p = Pattern.compile("^(.*):([0-9+]|\\?) (.*)$", Pattern.DOTALL);
			Matcher m = p.matcher(msg);
			if (m.matches()) {
				// just to make things even more annoying, the
				//   public CompiledScript compile(Reader script) throws ScriptException
				// method in LuaScriptEngine hardcodes the name 'script' as the script filename
				// String filename = m.group(1);
				String filename = (String) scriptContext.getAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE);
				return (ScriptException) new ScriptException(m.group(3), filename, 
					m.group(2).equals("?") ? -1 : Integer.parseInt(m.group(2))).initCause(t);
			} 
		}
		return (ScriptException) new ScriptException(t.getMessage()).initCause(t);
	}
	

}