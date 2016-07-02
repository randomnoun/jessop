package com.randomnoun.common.jessop;

import java.util.Properties;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import bsh.engine.BshScriptEngine;
import junit.framework.TestCase;

// !!!!!!!!!!!!!
// if this unit test fails in eclipse because it can't find the 'jessop' engine, just modify the MANIFEST.MF file 
// (add a space and delete it), resave it, and then run the unit test again.

/** This unit test checks error handling in scripts, and checks that line numbers in the target script 
 * match line numbers in the source script
 * 
 * @author knoxg
 * @version $Id$
 */
public class ErrorHandlingTest extends TestCase {

	Logger logger = Logger.getLogger(ErrorHandlingTest.class);
	
	public void setUp() {
		String logFormatPrefix = "[JessopContextTest] ";
		Properties lp = new Properties();
		lp.put("log4j.rootCategory", "INFO, CONSOLE");
		lp.put("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
		lp.put("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
		lp.put("log4j.appender.CONSOLE.layout.ConversionPattern", logFormatPrefix + "%d{ABSOLUTE} %-5p %c - %m%n");
		lp.put("log4j.logger.org.springframework", "INFO"); // since Spring is a bit too verbose for my liking at DEBUG level
		PropertyConfigurator.configure(lp);

	}
	
	// these are the same tests as in JessopTest,
	// with some nonsense thrown on the end to create a runtime error.
	// note that some languages allow undeclared variables, so we have even more contrived nonsense for them.
	
	public final static String COUNTING_SCRIPT = 
	  "<%@ jessop language=\"javascript\" engine=\"rhino\" %>\n" +
	  "Hello, <%= name %>\n" +
	  "<% for (var i=1; i<maxCount; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>\n" +
	  "<% floob; %>\n";  // where 'floob' is the canonical representation of a 'foo'...'bar' block

	public final static String LUA_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"lua\" engine=\"luaj\" %>\n" +
	  "Hello, <%= name %>\n" +
	  "<% for i=1,maxCount do %>\n" +
	  "<%= i %>\n" +
	  "<% end %>\n" +
	  "<% error(\"floob\") %>\n";

	public final static String PYTHON_COUNTING_SCRIPT_1 = 
	  "<%@ jessop language=\"python2\" engine=\"jython\" %>\n" +  // might add languageVersion later (jython is python2.7)
	  "Hello, <%= name %>\n" +
	  "<%\n" +
	  "for i in range(1, maxCount):\n"+
	  "%>\n" +
	  "<%= i %>\n" +
	  "<%\n" +
	  "pass;\n" +  // end of block
	  "%>\n" +
	  "not in loop\n" +
	  "<% floob; %>\n";
	

	public final static String PYTHON_COUNTING_SCRIPT_2 = 
	  "<%@ jessop language=\"python2\" engine=\"jython\" %>\n" +  // might add languageVersion later (jython is python2.7)
	  "Hello, <%= name %>\n" +
	  "<% for i in range(1, maxCount): %>\n" +
	  "<%= i %>\n" +
	  "<% floob; %>\n";

	public final static String JAVA_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"java\" engine=\"beanshell\" %>\n" +
	  "Hello, <%= name %>\n" + 
	  "<% for (int i=1; i<maxCount; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>\n" +
	  "<% floob.flaherdiwordy = flimble; %>\n";
	
	public void testJessop1() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		engine.put(JessopScriptEngine.FILENAME, "test.jessop");
		Bindings b = engine.createBindings();
		b.put(JessopScriptEngine.FILENAME, "test.jessop"); // ScriptContext.ENGINE_SCOPE
		b.put("name", "Baron von Count");
		b.put("maxCount", 3);
		try {
			logger.info("Start eval");
			engine.eval(COUNTING_SCRIPT, b);
			fail("script runtime exception expected");
		} catch (ScriptException e) {
			logger.info("caught ScriptException");
			assertEquals("test.jessop", e.getFileName());
			assertEquals(6, e.getLineNumber());
		}
	}
	
	public void testJessopCompile() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		engine.put(JessopScriptEngine.FILENAME, "test.jessop");
		Bindings b = engine.createBindings();
		b.put("name", "Baron von Count");
		b.put("maxCount", 3);

		Compilable compilableEngine = (Compilable) engine;
		CompiledScript script = compilableEngine.compile(COUNTING_SCRIPT);
		
		// compilation should still succeed
		JessopCompiledScript jessopScript = (JessopCompiledScript) script;
		logger.info("Start source");
		logger.info(jessopScript.getSource());
		logger.info("End source");
		
	}

	public void testJessopLua() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		engine.put(JessopScriptEngine.FILENAME, "test.jessop");
		Bindings b = engine.createBindings();
		b.put("name", "Baron von Count");
		b.put("maxCount", 3);

		logger.info("lua source: " + ((JessopCompiledScript) (((Compilable) engine).compile(LUA_COUNTING_SCRIPT))).getSource());
		// LuajContext l;
		try {
			logger.info("Start eval");
			engine.eval(LUA_COUNTING_SCRIPT, b);
			fail("script runtime exception expected");
		} catch (ScriptException e) {
			logger.info("caught ScriptException");
			assertEquals("test.jessop", e.getFileName());
			assertEquals(6, e.getLineNumber());
		}
	}

	public void testJessopPython1() throws ScriptException {
		// -Dpython.console.encoding=UTF-8
		// see http://stackoverflow.com/questions/30443537/how-do-i-fix-unsupportedcharsetexception-in-eclipse-kepler-luna-with-jython-pyde
		System.setProperty("python.console.encoding", "UTF-8");
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		engine.put(JessopScriptEngine.FILENAME, "test.jessop");
		Bindings b = engine.createBindings();
		b.put("name", "Baron von Count");
		b.put("maxCount", 3);

		logger.info("python source: " + ((JessopCompiledScript) (((Compilable) engine).compile(PYTHON_COUNTING_SCRIPT_1))).getSource());
		try {
			logger.info("Start eval");
			engine.eval(PYTHON_COUNTING_SCRIPT_1, b);
			fail("script runtime exception expected");
		} catch (ScriptException e) {
			logger.info("caught ScriptException");
			assertEquals("test.jessop", e.getFileName());
			assertEquals(11, e.getLineNumber());
		}
	}

	public void testJessopPython2() throws ScriptException {
		// -Dpython.console.encoding=UTF-8
		// see http://stackoverflow.com/questions/30443537/how-do-i-fix-unsupportedcharsetexception-in-eclipse-kepler-luna-with-jython-pyde
		System.setProperty("python.console.encoding", "UTF-8");
		
		// can either specify the language here (e.g. jessop-rhino), or just 'jessop' to get language from the script itself
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		engine.put(JessopScriptEngine.FILENAME, "test.jessop");
		Bindings b = engine.createBindings();
		b.put("name", "Baron von Count");
		b.put("maxCount", 3);

		logger.info("python source: " + ((JessopCompiledScript) (((Compilable) engine).compile(PYTHON_COUNTING_SCRIPT_2))).getSource());
		try {
			logger.info("Start eval");
			engine.eval(PYTHON_COUNTING_SCRIPT_2, b);
			fail("script runtime exception expected");
		} catch (ScriptException e) {
			logger.info("caught ScriptException");
			assertEquals("test.jessop", e.getFileName());
			assertEquals(5, e.getLineNumber());
		}
	}
	
	
	public void testJessopBeanshell() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		engine.put(JessopScriptEngine.FILENAME, "test.jessop");
		Bindings b = engine.createBindings();
		b.put(JessopScriptEngine.FILENAME, "test.jessop"); // ScriptContext.ENGINE_SCOPE
		b.put("name", "Baron von Count");
		b.put("maxCount", 3);

		logger.info("java source: " + ((JessopCompiledScript) (((Compilable) engine).compile(JAVA_COUNTING_SCRIPT))).getSource());
		try {
			logger.info("Start eval");
			engine.eval(JAVA_COUNTING_SCRIPT, b);
			fail("script runtime exception expected");
		} catch (ScriptException e) {
			logger.info("caught ScriptException");
			assertEquals("test.jessop", e.getFileName());
			assertEquals(6, e.getLineNumber());
		}
	}
	
}