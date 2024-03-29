package com.randomnoun.common.jessop;

import java.util.Properties;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

/** This unit test checks whether Bindings set on ScriptContexts are available to the target language within
 * jessop scripts
 * 
 * @author knoxg
 */
public class ScriptContextTest extends TestCase {

	Logger logger = Logger.getLogger(ScriptContextTest.class);
	
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
	
	public final static String JAVASCRIPT_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"javascript\" %>\n" + // was engine=\"rhino\", but we may want nashorn 
	  "Hello, <%= name %>\n" +
	  "<% for (var i = 1; i < maxCount; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>";

	public final static String LUA_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"lua\" engine=\"luaj\" %>\n" +
	  "Hello, <%= name %>\n" +
	  "<% for i = 1, maxCount - 1 do %>\n" +
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
	  "<% for (int i=1; i < maxCount; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>";
	
	// see https://github.com/jruby/jruby/wiki/Embedding-with-JSR-223
	// for the reason why bindings are exposed as global vars in ruby
	public final static String RUBY_COUNTING_SCRIPT_GLOBAL = 
	  "<%@ jessop language=\"ruby\" engine=\"jruby\" %>\n" +
	  "Hello, <%= $name %>\n" + 
	  "<% (1..$maxCount).each do |i| %>\n" +
	  "<%= i %>\n" +
	  "<% end %>";

	public final static String LISP_COUNTING_SCRIPT =
	  "<%@ jessop language=\"lisp\" engine=\"ABCL\" %>\n" +
	  "Hello, <%= name %>\n" + 
	  "<% (loop for i from 1 to maxCount do %>\n" + // inclusive
	  "<%= i %>\n" +
	  "<% ) %>";

	
	private String getSource(ScriptEngine engine, String jessopSource) throws ScriptException {
		Compilable compilable = (Compilable) engine;
		JessopCompiledScript compiledScript = (JessopCompiledScript) compilable.compile(jessopSource);
		return compiledScript.getSource();
	}
	
    /**
     * Returns the HTML-escaped form of a string. The <code>&amp;</code>,
     * <code>&lt;</code>, <code>&gt;</code>, and <code>"</code> characters are converted to
     * <code>&amp;amp;</code>, <code>&amp;lt;<code>, <code>&amp;gt;<code>, and
     * <code>&amp;quot;</code> respectively.
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
		Bindings b = engine.createBindings();
		b.put("name", "Baron von Count");
		b.put("maxCount", 4);
		return b;
    }
	
	public void _testScript(String input) throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		Bindings b = getBindings(engine);
		
		logger.info("jessop input: " + escapeHtml(input));
		logger.info("target language source: " + getSource(engine, input));
		logger.info("Start eval");
		engine.eval(input, b);
		logger.info("End eval");
	}
	
	public void testJessopCompile() throws ScriptException {
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		/*Bindings b =*/ getBindings(engine);

		Compilable compilableEngine = (Compilable) engine;
		CompiledScript script = compilableEngine.compile(JAVASCRIPT_COUNTING_SCRIPT);
		
		JessopCompiledScript jessopScript = (JessopCompiledScript) script;
		logger.info("Start source");
		logger.info(jessopScript.getSource());
		logger.info("End source");
		
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

	public void testJessopLisp() throws ScriptException {
		_testScript(LISP_COUNTING_SCRIPT);
	}
	
	/*
	public void testJessopJRuby() throws ScriptException {
		// if this is set, then we don't need to prefix vars with '$' in our script, but may have other side-affects
		// see https://github.com/jruby/jruby/wiki/Embedding-with-JSR-223
		// System.setProperty("org.jruby.embed.localvariable.behavior", "transient"); // this should be a ScriptEngine property, not a System property
		_testScript(RUBY_COUNTING_SCRIPT_GLOBAL);
	}
	*/

	
}
