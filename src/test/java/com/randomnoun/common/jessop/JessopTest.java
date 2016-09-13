package com.randomnoun.common.jessop;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import java.util.Properties;
import java.util.ServiceLoader;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

// !!!!!!!!!!!!!
// if this unit test fails in eclipse because it can't find the 'jessop' engine, 
// a) try modifing the MANIFEST.MF file (add a space and delete it), resave it
// b) try performing a maven install on the top-level project

/** Simple jessop tests (tests language declaration, few different languages, simple loop/output in target language) 
 * 
 * @author knoxg
 * @version $Id$
 */
public class JessopTest extends TestCase {

    /** A revision marker to be used in exception stack traces. */
    public static final String _revision = "$Id$";

	Logger logger = Logger.getLogger(JessopTest.class);
	
	public void setUp() {
		String logFormatPrefix = "[JessopTest] ";
		Properties lp = new Properties();
		lp.put("log4j.rootCategory", "INFO, CONSOLE");
		lp.put("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
		lp.put("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
		lp.put("log4j.appender.CONSOLE.layout.ConversionPattern", logFormatPrefix + "%d{ABSOLUTE} %-5p %c - %m%n");
		lp.put("log4j.logger.org.springframework", "INFO"); // since Spring is a bit too verbose for my liking at DEBUG level
		PropertyConfigurator.configure(lp);

	}
	
	/*
	public void testThings() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("rhino");  // nashorn in JDK9
		engine.eval("print('Hello World!');");
	}
	*/
	
	public void testServiceLoader() {
		ServiceLoader<ScriptEngineFactory> sefLoader = ServiceLoader.load(ScriptEngineFactory.class);
		logger.info("ScriptEngineFactories start");
		for (ScriptEngineFactory sef : sefLoader) {
			logger.info(sef.getEngineName()); // jessop appears when run from mvn test, but not in eclipse.
		}
		logger.info("ScriptEngineFactories end");

		ServiceLoader<JessopScriptBuilder> jsbLoader = ServiceLoader.load(JessopScriptBuilder.class);
		logger.info("JessopScriptBuilder start");
		for (JessopScriptBuilder jsb : jsbLoader) {
			logger.info(jsb.getLanguage()); // jessop appears when run from mvn test, but not in eclipse.
		}
		logger.info("JessopScriptBuilder end");

	}
	
	public final static String JAVASCRIPT_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"javascript\" %>\n" +  // was engine=\"rhino\", but we may want nashorn 
	  "just some text\r\n" +
	  "<% for (var i=1; i<10; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>";

	public final static String LUA_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"lua\" engine=\"luaj\" %>\n" +
	  "just some text\r\n" +
	  "<% for i=1,10 do %>\n" +
	  "<%= i %>\n" +
	  "<% end %>";

	public final static String PYTHON_COUNTING_SCRIPT_1 = 
	  "<%@ jessop language=\"python2\" engine=\"jython\" %>\n" +  // might add languageVersion later (jython is python2.7)
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
	  "<%@ jessop language=\"python2\" engine=\"jython\" %>\n" +  // might add languageVersion later (jython is python2.7)
	  "just some text\n" +
	  "<% for i in range(1, 10): %>\n" +
	  "<%= i %>\n";

	public final static String JAVA_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"java\" engine=\"beanshell\" %>\n" +
	  "just some text\n" + 
	  "<% for (int i=1; i<10; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>";

	public final static String RUBY_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"ruby\" engine=\"jruby\" %>\n" +
	  "just some text\n" + 
	  "<% (1..10).each do |i| %>\n" +
	  "<%= i %>\n" +
	  "<% end %>";


	private String getSource(ScriptEngine engine, String jessopSource) throws ScriptException {
		Compilable compilable = (Compilable) engine;
		JessopCompiledScript compiledScript = (JessopCompiledScript) compilable.compile(jessopSource);
		return compiledScript.getSource();
	}
	
	public void testJessopCompile() throws ScriptException {
		Compilable engine = (Compilable) new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		
		CompiledScript script = engine.compile(JAVASCRIPT_COUNTING_SCRIPT);
		
		JessopCompiledScript jessopScript = (JessopCompiledScript) script;
		logger.info("Start source");
		logger.info(jessopScript.getSource());
		logger.info("End source");
		
	}

	public void _testScript(String input) throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		logger.info("jessop input: " + input);
		logger.info("target language source: " + getSource(engine, input));
		logger.info("Start eval");
		engine.eval(input);
		logger.info("End eval");
	}

	public void testJessopJavascript() throws ScriptException {
		_testScript(JAVASCRIPT_COUNTING_SCRIPT);
	}

	public void testJessopLua() throws ScriptException {
		_testScript(LUA_COUNTING_SCRIPT);
	}

	public void testJessopPython1() throws ScriptException {
		
		// -Dpython.console.encoding=UTF-8
		// see http://stackoverflow.com/questions/30443537/how-do-i-fix-unsupportedcharsetexception-in-eclipse-kepler-luna-with-jython-pyde
		System.setProperty("python.console.encoding", "UTF-8");
		_testScript(PYTHON_COUNTING_SCRIPT_1);
	}

	public void testJessopPython2() throws ScriptException {
		// -Dpython.console.encoding=UTF-8
		// see http://stackoverflow.com/questions/30443537/how-do-i-fix-unsupportedcharsetexception-in-eclipse-kepler-luna-with-jython-pyde
		System.setProperty("python.console.encoding", "UTF-8");
		_testScript(PYTHON_COUNTING_SCRIPT_2);
	}
	
	public void testJessopBeanshell() throws ScriptException {
		_testScript(JAVA_COUNTING_SCRIPT);
	}

	/*
	public void testJessopJRuby() throws ScriptException {
		_testScript(RUBY_COUNTING_SCRIPT);
	}
	*/

	
}
