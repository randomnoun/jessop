package com.randomnoun.common.jessop;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import junit.framework.TestCase;

//!!!!!!!!!!!!!
//if this unit test fails in eclipse because it can't find the 'jessop' engine, 
//a) try modifing the MANIFEST.MF file (add a space and delete it), resave it
//b) try performing a maven install on the top-level project

/** This unit test checks whether structured maps and structured lists passed as Bindings to ScriptContexts 
 * are available to the target language within jessop scripts
 * 
 * @author knoxg
 * @version $Id$
 */
public class ScriptContextStructTest extends TestCase {

    /** A revision marker to be used in exception stack traces. */
    public static final String _revision = "$Id$";

	Logger logger = Logger.getLogger(ScriptContextStructTest.class);
	
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
	  // maybe it's a js string after all.
	  "<%@ jessop language=\"javascript\" engine=\"rhino\" %>\n" +
	  "<% var t = 'Baron von Count'; %>\n" + // t is not defined ?
	  "Hello, <%= muppets[0].name %>. <%= typeof t %>. <%= (t == muppets[0].name) %>\n" +
	  "<% for (var i = 1; i < favouriteNumber[muppets[0].name]; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>";

	
	public final static String LUA_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"lua\" engine=\"luaj\" %>\n" +
	  "Hello, <%= muppets:get(0):get('name') %>\n"  +
	  "<% for i = 1, favouriteNumber:get(muppets:get(0):get('name')) - 1 do %>\n" +
	  "<%= i %>\n" +
	  "<% end %>";

	/*
	public final static String PYTHON_COUNTING_SCRIPT_1 = 
	  "<%@ jessop language=\"python2\" engine=\"jython\" %>\n" +  // might add languageVersion later (jython is python2.7)
	  "Hello, <%= muppets[0].name %>\n" +
	  "<%\n" +
	  "for i in range(1, favouriteNumber[muppets[0].name]):\n"+
	  "%>\n" +
	  "<%= i %>\n" +
	  "<%\n" +
	  "pass;\n" +  // end of block
	  "%>\n" +
	  "not in loop\n";
	

	public final static String PYTHON_COUNTING_SCRIPT_2 = 
	  "<%@ jessop language=\"python2\" engine=\"jython\" %>\n" +  // might add languageVersion later (jython is python2.7)
	  "Hello, <%= muppets[0].name %>\n" +
	  "<% for i in range(1, favouriteNumber[muppets[0].name]): %>\n" +
	  "<%= i %>\n";

	public final static String JAVA_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"java\" engine=\"beanshell\" %>\n" +
	  "Hello, <%= muppets[0].name %>\n" + 
	  "<% for (int i=1; i < favouriteNumber[muppets[0].name]; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>";
	*/
	
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

    public Bindings getBindings(ScriptEngine engine) {
    	List muppets = new ArrayList();
    	Map muppet = new HashMap();
    	muppet.put("name",  "Baron von Count");
    	muppets.add(muppet);
    	
    	muppet = new HashMap();
    	muppet.put("name",  "Waldorf");
    	muppets.add(muppet);
    	
    	Map favouriteNumber = new HashMap();
    	favouriteNumber.put("Baron von Count", 3);
    	favouriteNumber.put("Waldorf", 7);
    	
		Bindings b = engine.createBindings();
		b.put("muppets", muppets);
		b.put("favouriteNumber", favouriteNumber);
		
		return b;
    }
    
	public void testJessop1() throws ScriptException {
		String input = COUNTING_SCRIPT; 
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = getBindings(engine);
		
		logger.info("jessop input: " + escapeHtml(input));
		logger.info("target language source: " + getSource(engine, input));
		logger.info("Start eval");
		engine.eval(input, b);
		logger.info("End eval");
	}
	
	/*
	public void testJessopCompile() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = getBindings(engine);

		Compilable compilableEngine = (Compilable) engine;
		CompiledScript script = compilableEngine.compile(COUNTING_SCRIPT);
		
		JessopCompiledScript jessopScript = (JessopCompiledScript) script;
		logger.info("Start source");
		logger.info(jessopScript.getSource());
		logger.info("End source");
		
	}
	*/

	public void testJessopLua() throws ScriptException {
		String input = LUA_COUNTING_SCRIPT; 
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = getBindings(engine);
		//b.put("muppets", CoerceJavaToLua.coerce(b.get("muppets")));
		//Map muppet = new HashMap(); muppet.put("name", "x");
		//b.put("muppet", CoerceJavaToLua.coerce(muppet));
		
		logger.info("jessop input: " + escapeHtml(input));
		logger.info("target language source: " + getSource(engine, input));
		logger.info("Start eval");
		engine.eval(input, b);
		logger.info("End eval");
	}

	/*
	public void testJessopPython1() throws ScriptException {
		String input = PYTHON_COUNTING_SCRIPT_1; 
		// -Dpython.console.encoding=UTF-8
		// see http://stackoverflow.com/questions/30443537/how-do-i-fix-unsupportedcharsetexception-in-eclipse-kepler-luna-with-jython-pyde
		System.setProperty("python.console.encoding", "UTF-8");
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = getBindings(engine);
		
		logger.info("jessop input: " + escapeHtml(input));
		logger.info("target language source: " + getSource(engine, input));
		logger.info("Start eval");
		engine.eval(input, b);
		logger.info("End eval");
	}

	public void testJessopPython2() throws ScriptException {
		String input = PYTHON_COUNTING_SCRIPT_2;
		// -Dpython.console.encoding=UTF-8
		// see http://stackoverflow.com/questions/30443537/how-do-i-fix-unsupportedcharsetexception-in-eclipse-kepler-luna-with-jython-pyde
		System.setProperty("python.console.encoding", "UTF-8");
		
		// can either specify the language here (e.g. jessop-rhino), or just 'jessop' to get language from the script itself
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = getBindings(engine);

		logger.info("jessop input: " + escapeHtml(input));
		logger.info("target language source: " + getSource(engine, input));
		logger.info("Start eval");
		engine.eval(input, b);
		logger.info("End eval");
	}
	
	
	public void testJessopBeanshell() throws ScriptException {
		String input = JAVA_COUNTING_SCRIPT;
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = getBindings(engine);

		logger.info("jessop input: " + escapeHtml(input));
		logger.info("target language source: " + getSource(engine, input));
		logger.info("Start eval");
		engine.eval(input, b);
		logger.info("End eval");
	}
	*/
	
}
