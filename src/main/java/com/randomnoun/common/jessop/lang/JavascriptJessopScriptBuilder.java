package com.randomnoun.common.jessop.lang;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import org.apache.log4j.Logger;

import com.randomnoun.common.jessop.AbstractJessopScriptBuilder;
import com.randomnoun.common.jessop.JessopScriptBuilder;

public class JavascriptJessopScriptBuilder extends AbstractJessopScriptBuilder implements JessopScriptBuilder {

    /** A revision marker to be used in exception stack traces. */
    public static final String _revision = "$Id$";
	Logger logger = Logger.getLogger(JavascriptJessopScriptBuilder.class);
	int outputLine = 1;        // current line number in the target script;
	int lastScriptletLine = 1; // the last line number of the last scriptlet (used for suppressEol)

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
		s = suppressEol(s, declarations.isSuppressEol() && lastScriptletLine == line);
		print("out.write(\"" + escapeJavascript(s) + "\");");
		lastScriptletLine = 0; // don't suppress eols on this line
	}
	@Override
	public void emitExpression(int line, String s) {
		skipToLine(line);
		print("out.write('' + (" + s + "));"); // coerce to String
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
		return "javascript";
	}
	@Override
	public String getDefaultScriptEngineName() {
		// possibly use 'js' here. let's see.
		// the phobos jsr223 wrapper calls itself 'rhino-nonjdk', as well as 'rhino'
		return "rhino";
	}
	@Override
	public String getDefaultBindingsConverterClassName() {

		boolean isComSunRhino = false; // rhino engine is under the com.sun package 
		// let's see what class we get if we try to load the 'rhino' engine, then work from there
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("rhino");  // nashorn in JDK9
		if (engine!=null && engine.getClass().getName().equals("com.sun.script.javascript.RhinoScriptEngine")) {
			// it's either oracle or openjdk
			isComSunRhino = true;
		}
		String result = null;
		if (!isComSunRhino) {
			// maybe we've got com.sun.phobos:phobos-rhino 
			// or org.rhq:rhq-scripting-javascript 
			// or de.christophkraemer:rhino-script-engine 
			// or any of the other JSR223 wrappers for rhino in central 
			// at http://search.maven.org/#search%7Cga%7C1%7Cc%3A%22RhinoScriptEngine%22
			try {
				/*Class c =*/ Class.forName("org.mozilla.javascript.NativeObject");
				// this exists, so use the mozilla rhino binding converter
				result = "com.randomnoun.common.jessop.engine.rhino.RhinoBindingsConverter";
			} catch (ClassNotFoundException cnfe) { }
		}
		
		// ok, it's probably oracle or openjdk at this stage
		if (result == null) {
			try {
				/*Class c =*/ Class.forName("sun.org.mozilla.javascript.internal.NativeObject");
				// this exists, so use the oracle binding converter
				result = "com.randomnoun.common.jessop.engine.rhinoOracle.RhinoOracleBindingsConverter";
			} catch (ClassNotFoundException cnfe2) { }
		}
		
		if (result == null) {
			try {
				/*Class c =*/ Class.forName("sun.org.mozilla.javascript.NativeObject");
				// this exists, so use the openjdk binding converter
				result = "com.randomnoun.common.jessop.engine.rhinoOpenjdk.RhinoOpenjdkBindingsConverter";
			} catch (ClassNotFoundException cnfe3) {
				// logger.warn("No known rhino implementation on classpath; setting JessopBindingsConverter to null");
				result = null;
			}
		}
		
		return result;
	}
	
	// going to use this for debugging only
	public void testEngine() {
		// this should match the engine in use, so let's see what 'rhino' gives us
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("rhino");  // nashorn in JDK9
		if (engine!=null) {
			ScriptEngineFactory factory = engine.getFactory();
			logger.info("default rhino ScriptEngine is " + engine.getClass().getName());
			logger.info("ENGINE=" + factory.getParameter(ScriptEngine.ENGINE));
			logger.info("ENGINE_VERSION=" + factory.getParameter(ScriptEngine.ENGINE_VERSION));
			logger.info("LANGUAGE=" + factory.getParameter(ScriptEngine.LANGUAGE_VERSION));
			logger.info("LANGUAGE_VERSION=" + factory.getParameter(ScriptEngine.LANGUAGE_VERSION));
		} else {
			logger.warn("default 'rhino' ScriptEngine not found");
		}
	}
	
}