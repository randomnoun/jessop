package com.randomnoun.common.jessop;

import java.util.Properties;
import java.util.ServiceLoader;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.apache.log4j.PropertyConfigurator;

import com.randomnoun.common.jessop.JessopScriptEngine.JessopCompiledScript;

import junit.framework.TestCase;

public class JessopTest extends TestCase {
	
	public void setUp() {
		String logFormatPrefix = "[JessopTest] ";
		Properties lp = new Properties();
		lp.put("log4j.rootCategory", "DEBUG, CONSOLE");
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
		System.out.println("ScriptEngineFactories start");
		for (ScriptEngineFactory sef : sefLoader) {
			System.out.println(sef.getEngineName()); // jessop appears when run from mvn test, but not in eclipse.
		}
		System.out.println("ScriptEngineFactories end");
	}
	
	public final static String COUNTING_SCRIPT = 
	  "<%@ jessop language=\"javascript\" engine=\"rhino\" %>\n" +
	  "just some text\n" +
	  "<% for (var i=0; i<10; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } out.flush(); %>";
	
	public void testJessop() throws ScriptException {
		
		// can either specify the language here (e.g. jessop-rhino), or just 'jessop' to get language from the script itself
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		System.out.println("Start eval");
		engine.eval(COUNTING_SCRIPT);
		System.out.println("End eval");
	}
	
	public void testJessopCompile() throws ScriptException {
		Compilable engine = (Compilable) new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		
		CompiledScript script = engine.compile(COUNTING_SCRIPT);
		
		JessopCompiledScript jessopScript = (JessopCompiledScript) script;
		System.out.println("Start source");
		System.out.println(jessopScript.getSource());
		System.out.println("End source");
		
	}

	
}
