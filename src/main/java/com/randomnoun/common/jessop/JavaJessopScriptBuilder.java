package com.randomnoun.common.jessop;

import org.apache.log4j.Logger;

// exactly the same as the JavascriptJessopScriptBuilder
// string coercion is a bit different
public class JavaJessopScriptBuilder extends AbstractJessopScriptBuilder implements JessopScriptBuilder {
	Logger logger = Logger.getLogger(JavaJessopScriptBuilder.class);
	int outputLine = 1; // current output line;

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
		print("out.write(\"" + escapeJava(s) + "\");");
	}
	@Override
	public void emitExpression(int line, String s) {
		skipToLine(line);
		print("out.write(\"\" + (" + s + "));"); // coerce to String
	}
	@Override
	public void emitScriptlet(int line, String s) {
		skipToLine(line);
		print(s);
	}
	@Override
	public String getLanguage() {
		return "java";
	}
	@Override
	public String getDefaultScriptEngineName() {
		return "beanshell";
	}
	
}