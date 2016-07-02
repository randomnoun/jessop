package com.randomnoun.common.jessop.lang;

import org.apache.log4j.Logger;

import com.randomnoun.common.jessop.AbstractJessopScriptBuilder;
import com.randomnoun.common.jessop.JessopScriptBuilder;

public class JavascriptJessopScriptBuilder extends AbstractJessopScriptBuilder implements JessopScriptBuilder {
	Logger logger = Logger.getLogger(JavascriptJessopScriptBuilder.class);
	int outputLine = 1; // current output line;
	public JavascriptJessopScriptBuilder() {
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
	private static String escapeJavascript(String string) {
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
		print("out.write(\"" + escapeJavascript(s) + "\");");
	}
	@Override
	public void emitExpression(int line, String s) {
		skipToLine(line);
		print("out.write('' + (" + s + "));"); // coerce to String
	}
	@Override
	public void emitScriptlet(int line, String s) {
		skipToLine(line);
		print(s);
	}
	@Override
	public String getLanguage() {
		return "javascript";
	}
	@Override
	public String getDefaultScriptEngineName() {
		// TODO Auto-generated method stub
		return "rhino";
	}
	
}