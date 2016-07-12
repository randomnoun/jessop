package com.randomnoun.common.jessop;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import java.util.Properties;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

//!!!!!!!!!!!!!
//if this unit test fails in eclipse because it can't find the 'jessop' engine, 
//a) try modifing the MANIFEST.MF file (add a space and delete it), resave it
//b) try performing a maven install on the top-level project

/** This unit test checks whether Bindings set on ScriptContexts are available to the target language within
 * jessop scripts
 * 
 * @author knoxg
 * @version $Id$
 */
public class DefaultDeclarationTest extends TestCase {

    /** A revision marker to be used in exception stack traces. */
    public static final String _revision = "$Id$";

	Logger logger = Logger.getLogger(DefaultDeclarationTest.class);
	
	public void setUp() {
		String logFormatPrefix = "[ScriptContextTest] ";
		Properties lp = new Properties();
		lp.put("log4j.rootCategory", "INFO, CONSOLE");
		lp.put("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
		lp.put("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
		lp.put("log4j.appender.CONSOLE.layout.ConversionPattern", logFormatPrefix + "%d{ABSOLUTE} %-5p %c - %m%n");
		lp.put("log4j.logger.org.springframework", "INFO"); // since Spring is a bit too verbose for my liking at DEBUG level
		PropertyConfigurator.configure(lp);

	}
	
	public final static String COUNTING_SCRIPT = 
	  // "<%@ jessop language=\"javascript\" engine=\"rhino\" %>\n" +
	  "Hello, <%= name %>\n" +
	  "<% for (var i = 1; i < maxCount; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>";

	
	private String getSource(ScriptEngine engine, String jessopSource) throws ScriptException {
		Compilable compilable = (Compilable) engine;
		JessopCompiledScript compiledScript = (JessopCompiledScript) compilable.compile(jessopSource);
		return compiledScript.getSource();
	}
	
    /**
     * Returns the HTML-escaped form of a string. The <tt>&amp;</tt>,
     * <tt>&lt;</tt>, <tt>&gt;</tt>, and <tt>"</tt> characters are converted to
     * <tt>&amp;amp;</tt>, <tt>&amp;lt;<tt>, <tt>&amp;gt;<tt>, and
     * <tt>&amp;quot;</tt> respectively.
     *
     * @param string the string to convert
     *
     * @return the HTML-escaped form of the string
     */
    static public String escapeHtml(String string) {
        if (string == null) {
            return "";
        }

        char c;
        StringBuilder sb = new StringBuilder(string.length());

        for (int i = 0; i < string.length(); i++) {
            c = string.charAt(i);

            switch (c) {
                case '&':
                    sb.append("&amp;");
                    break;
                case '<':
                    sb.append("&lt;");
                    break;
                case '>':
                    sb.append("&gt;");
                    break;
                case '\"':
                    // interestingly, &quote; (with the e) works fine for HTML display,
                    // but not inside hidden field values
                    sb.append("&quot;");
                    break;
                default:
                    sb.append(c);
            }
        }

        return sb.toString();
    }

    
	public void testJessop1() throws ScriptException {
		com.sun.script.javascript.RhinoScriptEngine rse;
		
		
		String input = COUNTING_SCRIPT; 
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = engine.createBindings();
		b.put("name", "Baron von Count");
		b.put("maxCount", 4);
		
		logger.info("jessop input: " + escapeHtml(input));
		logger.info("target language source: " + getSource(engine, input));
		logger.info("Start eval");
		engine.eval(input, b);
		logger.info("End eval");
	}
	
	public void testJessopCompile() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = engine.createBindings();
		b.put("name", "Baron von Count");
		b.put("maxCount", 4);

		Compilable compilableEngine = (Compilable) engine;
		CompiledScript script = compilableEngine.compile(COUNTING_SCRIPT);
		
		JessopCompiledScript jessopScript = (JessopCompiledScript) script;
		logger.info("Start source");
		logger.info(jessopScript.getSource());
		logger.info("End source");
	}

	
}
