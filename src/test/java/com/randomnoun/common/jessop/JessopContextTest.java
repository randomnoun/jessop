package com.randomnoun.common.jessop;

import java.util.Properties;
import java.util.ServiceLoader;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.luaj.vm2.script.LuajContext;

import com.randomnoun.common.jessop.JessopScriptEngine.JessopCompiledScript;

import junit.framework.TestCase;

// !!!!!!!!!!!!!
// if this unit test fails in eclipse because it can't find the 'jessop' engine, just modify the MANIFEST.MF file 
// (add a space and delete it), resave it, and then run the unit test again.

public class JessopContextTest extends TestCase {

	Logger logger = Logger.getLogger(JessopContextTest.class);
	
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
	
	public void testThings() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("rhino");  // nashorn in JDK9
		engine.eval("print('Hello World!');");
	}
	
	public void testServiceLoader() {
		ServiceLoader<ScriptEngineFactory> sefLoader = ServiceLoader.load(ScriptEngineFactory.class);
		logger.info("ScriptEngineFactories start");
		for (ScriptEngineFactory sef : sefLoader) {
			logger.info(sef.getEngineName()); // jessop appears when run from mvn test, but not in eclipse.
		}
		logger.info("ScriptEngineFactories end");
	}
	
	public final static String COUNTING_SCRIPT = 
	  "<%@ jessop language=\"javascript\" engine=\"rhino\" %>\n" +
	  "Hello, <%= name %>\n" +
	  "<% for (var i=1; i<maxCount; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>";

	public final static String LUA_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"lua\" engine=\"luaj\" %>\n" +
	  "Hello, <%= name %>\n" +
	  "<% for i=1,maxCount do %>\n" +
	  "<%= i %>\n" +
	  "<% end %>";

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
	  "not in loop\n";
	

	public final static String PYTHON_COUNTING_SCRIPT_2 = 
	  "<%@ jessop language=\"python2\" engine=\"jython\" %>\n" +  // might add languageVersion later (jython is python2.7)
	  "Hello, <%= name %>\n" +
	  "<% for i in range(1, maxCount): %>\n" +
	  "<%= i %>\n";

	public final static String JAVA_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"java\" engine=\"beanshell\" %>\n" +
	  "Hello, <%= name %>\n" + 
	  "<% for (int i=1; i<maxCount; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>";
	
	public void testJessop1() throws ScriptException {
		// can either specify the language here (e.g. jessop-rhino), or just 'jessop' to get language from the script itself
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = engine.createBindings();
		b.put("name", "Baron von Count");
		b.put("maxCount", 3);
		logger.info("Start eval");
		engine.eval(COUNTING_SCRIPT, b);
		logger.info("End eval");
	}
	
	public void testJessopCompile() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = engine.createBindings();
		b.put("name", "Baron von Count");
		b.put("maxCount", 3);

		Compilable compilableEngine = (Compilable) engine;
		CompiledScript script = compilableEngine.compile(COUNTING_SCRIPT);
		
		JessopCompiledScript jessopScript = (JessopCompiledScript) script;
		logger.info("Start source");
		logger.info(jessopScript.getSource());
		logger.info("End source");
		
	}

	public void testJessopLua() throws ScriptException {
		
		// can either specify the language here (e.g. jessop-rhino), or just 'jessop' to get language from the script itself
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = engine.createBindings();
		b.put("name", "Baron von Count");
		b.put("maxCount", 3);

		logger.info("lua source: " + ((JessopCompiledScript) (((Compilable) engine).compile(LUA_COUNTING_SCRIPT))).getSource());
		// LuajContext l;
		
		logger.info("Start eval");
		engine.eval(LUA_COUNTING_SCRIPT, b);
		logger.info("End eval");
	}

	public void testJessopPython1() throws ScriptException {
		// -Dpython.console.encoding=UTF-8
		// see http://stackoverflow.com/questions/30443537/how-do-i-fix-unsupportedcharsetexception-in-eclipse-kepler-luna-with-jython-pyde
		System.setProperty("python.console.encoding", "UTF-8");
		
		// can either specify the language here (e.g. jessop-rhino), or just 'jessop' to get language from the script itself
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = engine.createBindings();
		b.put("name", "Baron von Count");
		b.put("maxCount", 3);

		logger.info("python source: " + ((JessopCompiledScript) (((Compilable) engine).compile(PYTHON_COUNTING_SCRIPT_1))).getSource());
		
		logger.info("Start eval");
		engine.eval(PYTHON_COUNTING_SCRIPT_1, b);
		logger.info("End eval");
	}

	public void testJessopPython2() throws ScriptException {
		// -Dpython.console.encoding=UTF-8
		// see http://stackoverflow.com/questions/30443537/how-do-i-fix-unsupportedcharsetexception-in-eclipse-kepler-luna-with-jython-pyde
		System.setProperty("python.console.encoding", "UTF-8");
		
		// can either specify the language here (e.g. jessop-rhino), or just 'jessop' to get language from the script itself
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = engine.createBindings();
		b.put("name", "Baron von Count");
		b.put("maxCount", 3);

		logger.info("python source: " + ((JessopCompiledScript) (((Compilable) engine).compile(PYTHON_COUNTING_SCRIPT_2))).getSource());
		
		logger.info("Start eval");
		engine.eval(PYTHON_COUNTING_SCRIPT_2, b);
		logger.info("End eval");
	}
	
	
	public void testJessopBeanshell() throws ScriptException {
		
		// can either specify the language here (e.g. jessop-rhino), or just 'jessop' to get language from the script itself
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = engine.createBindings();
		b.put("name", "Baron von Count");
		b.put("maxCount", 3);

		System.out.println("java source: " + ((JessopCompiledScript) (((Compilable) engine).compile(JAVA_COUNTING_SCRIPT))).getSource());
		
		logger.info("Start eval");
		engine.eval(JAVA_COUNTING_SCRIPT, b);
		logger.info("End eval");
	}
	
}
