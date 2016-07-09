package com.randomnoun.common.jessop;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import java.util.Properties;

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

/** This unit test checks whether suppressEol declarations work as expected
 * 
 * @author knoxg
 * @version $Id$
 */
public class SuppressEolTest extends TestCase {

    /** A revision marker to be used in exception stack traces. */
    public static final String _revision = "$Id$";

	Logger logger = Logger.getLogger(SuppressEolTest.class);
	
	public void setUp() {
		String logFormatPrefix = "[SuppressEolTest] ";
		Properties lp = new Properties();
		lp.put("log4j.rootCategory", "INFO, CONSOLE");
		lp.put("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
		lp.put("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
		lp.put("log4j.appender.CONSOLE.layout.ConversionPattern", logFormatPrefix + "%d{ABSOLUTE} %-5p %c - %m%n");
		lp.put("log4j.logger.org.springframework", "INFO"); // since Spring is a bit too verbose for my liking at DEBUG level
		PropertyConfigurator.configure(lp);

	}
	
	public final static String COUNTING_SCRIPT = 
	  "<%@ jessop language=\"javascript\" engine=\"rhino\" suppressEol=\"true\"%>\n" +
	  "just some text\n" +
	  "<% for (var i=1; i<10; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>";

	public final static String LUA_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"lua\" engine=\"luaj\" suppressEol=\"true\"%>\n" +
	  "just some text\n" +
	  "<% for i=1,10 do %>\n" +
	  "<%= i %>\n" +
	  "<% end %>";

	public final static String PYTHON_COUNTING_SCRIPT_1 = 
	  "<%@ jessop language=\"python2\" engine=\"jython\" suppressEol=\"true\"%>\n" +  // might add languageVersion later (jython is python2.7)
	  "just some text\n" +
	  "<%\n" +
	  "for i in range(1, 10):\n"+
	  "%>\n" +
	  "<%= i %>\n" +
	  "<%\n" +
	  "pass;\n" +  // end of block
	  "%>\n" +
	  "not in loop\n";
	

	public final static String PYTHON_COUNTING_SCRIPT_2 = 
	  "<%@ jessop language=\"python2\" engine=\"jython\" suppressEol=\"true\"%>\n" +  // might add languageVersion later (jython is python2.7)
	  "just some text\n" +
	  "<% for i in range(1, 10): %>\n" +
	  "<%= i %>\n";

	public final static String JAVA_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"java\" engine=\"beanshell\" suppressEol=\"true\"%>\n" +
	  "just some text\n" + 
	  "<% for (int i=1; i<10; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>";

	private String getSource(ScriptEngine engine, String jessopSource) throws ScriptException {
		Compilable compilable = (Compilable) engine;
		JessopCompiledScript compiledScript = (JessopCompiledScript) compilable.compile(jessopSource);
		return compiledScript.getSource();
	}
	
	public void testJessop1() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		logger.info("Start eval");
		engine.eval(COUNTING_SCRIPT);
		logger.info("End eval");
	}
	
	public void testJessopCompile() throws ScriptException {
		Compilable engine = (Compilable) new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		
		CompiledScript script = engine.compile(COUNTING_SCRIPT);
		
		JessopCompiledScript jessopScript = (JessopCompiledScript) script;
		logger.info("Start source");
		logger.info(jessopScript.getSource());
		logger.info("End source");
		
	}

	public void testJessopLua() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		logger.info("lua source: " + ((JessopCompiledScript) (((Compilable) engine).compile(LUA_COUNTING_SCRIPT))).getSource());
		
		logger.info("Start eval");
		engine.eval(LUA_COUNTING_SCRIPT);
		logger.info("End eval");
	}

	public void testJessopPython1() throws ScriptException {
		// -Dpython.console.encoding=UTF-8
		// see http://stackoverflow.com/questions/30443537/how-do-i-fix-unsupportedcharsetexception-in-eclipse-kepler-luna-with-jython-pyde
		System.setProperty("python.console.encoding", "UTF-8");
		
		// can either specify the language here (e.g. jessop-rhino), or just 'jessop' to get language from the script itself
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		logger.info("python source: " + getSource(engine, PYTHON_COUNTING_SCRIPT_1));
		
		logger.info("Start eval");
		engine.eval(PYTHON_COUNTING_SCRIPT_1);
		logger.info("End eval");
	}

	public void testJessopPython2() throws ScriptException {
		// -Dpython.console.encoding=UTF-8
		// see http://stackoverflow.com/questions/30443537/how-do-i-fix-unsupportedcharsetexception-in-eclipse-kepler-luna-with-jython-pyde
		System.setProperty("python.console.encoding", "UTF-8");
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		logger.info("python source: " + getSource(engine, PYTHON_COUNTING_SCRIPT_2));
		
		logger.info("Start eval");
		engine.eval(PYTHON_COUNTING_SCRIPT_2);
		logger.info("End eval");
	}
	
	public void testJessopBeanshell() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		System.out.println("java source: " + getSource(engine, JAVA_COUNTING_SCRIPT));
		
		logger.info("Start eval");
		engine.eval(JAVA_COUNTING_SCRIPT);
		logger.info("End eval");
	}
	
}
