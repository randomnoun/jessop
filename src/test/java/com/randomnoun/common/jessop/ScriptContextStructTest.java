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
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
// import org.luaj.vm2.lib.jse.CoerceJavaToLua;

// import org.armedbear.lisp.LispObject;

import junit.framework.TestCase;

/** This unit test checks whether structured maps and structured lists passed as Bindings to ScriptContexts 
 * are available to the target language within jessop scripts
 * 
 * @author knoxg
 */
public class ScriptContextStructTest extends TestCase {

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
	
	public final static String JAVASCRIPT_COUNTING_SCRIPT = 
	  // maybe it's a js string after all.
	  "<%@ jessop language=\"javascript\" %>\n" + // was engine=\"rhino\", but we may want nashorn 
	  "<% var t = 'Baron von Count'; %>\n" + // t is not defined ?
	  "Hello, <%= muppets[0].name %>. <%= typeof t %>. <%= (t == muppets[0].name) %>\n" +
	  "<% for (var i = 1; i < favouriteNumber[muppets[0].name]; i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>";

	// might be nice to be able to do muppets[0] here, or even muppets[0].name
	public final static String LUA_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"lua\" engine=\"luaj\" %>\n" +
	  "Hello, <%= muppets:get(0):get('name') %>\n"  +
	  "<% for i = 1, favouriteNumber:get(muppets:get(0):get('name')) - 1 do %>\n" +
	  "<%= i %>\n" +
	  "<% end %>";

	
	// might be nice to be able to do muppets[0].name 
	public final static String PYTHON_COUNTING_SCRIPT_1 = 
	  "<%@ jessop language=\"python2\" engine=\"jython\" %>\n" +  // might add languageVersion later (jython is python2.7)
	  "Hello, <%= muppets[0]['name'] %>\n" +
	  "<%\n" +
	  "for i in range(1, favouriteNumber[muppets[0]['name']]):\n"+
	  "%>\n" +
	  "<%= i %>\n" +
	  "<%\n" +
	  "pass;\n" +  // end of block
	  "%>\n" +
	  "not in loop\n";
	
	
	public final static String PYTHON_COUNTING_SCRIPT_2 = 
	  "<%@ jessop language=\"python2\" engine=\"jython\" %>\n" +  // might add languageVersion later (jython is python2.7)
	  "Hello, <%= muppets[0]['name'] %>\n" +
	  "<% for i in range(1, favouriteNumber[muppets[0]['name']]): %>\n" +
	  "<%= i %>\n";

	public final static String JAVA_COUNTING_SCRIPT = 
	  "<%@ jessop language=\"java\" engine=\"beanshell\" %>\n" +
	  "Hello, <%= muppets.get(0).get(\"name\") %>\n" + 
	  "<% for (int i=1; i < favouriteNumber.get(muppets.get(0).get(\"name\")); i++) { %>\n" +
	  "<%= i %>\n" +
	  "<% } %>";
	
	// see https://github.com/jruby/jruby/wiki/Embedding-with-JSR-223
	// for the reason why bindings are exposed as global vars in ruby
	public final static String RUBY_COUNTING_SCRIPT_GLOBAL = 
	  "<%@ jessop language=\"ruby\" engine=\"jruby\" %>\n" +
	  "Hello, <%= $muppets[0][\"name\"] %>\n" + 
	  "<% (1..$favouriteNumber[$muppets[0][\"name\"]]).each do |i| %>\n" +
	  "<%= i %>\n" +
	  "<% end %>";
	
	public final static String LISP_COUNTING_SCRIPT =
	  "<%@ jessop language=\"lisp\" engine=\"ABCL\" %>\n" +

      // this would be preferable, but would require a BindingsConverter that I haven't written yet
	  //"Hello, <%= (gethash 'name (nth muppets 0)) %>\n" + 
	  
	  // this works but is verbose
	  "Hello, <%= (jcall \"get\" (jcall \"get\" muppets 0) \"name\") %>\n" +
	  "<% (loop for i from 1 to (jcall \"get\" favouriteNumber (jcall \"get\" (jcall \"get\" muppets 0) \"name\") ) do %>\n" + // inclusive
	  "<%= i %>\n" +
	  "<% ) %>";

	  // doesn't work; returns Hello, NIL
	  // "Hello, <%= (gethash 'name (jss:hashmap-to-hashtable (nth 0 (jss:jlist-to-list muppets)))) %>\n" +
	  
	  // works, but still fairly verbose
	  // adding (in-package :jss) causes 'The variable MUPPETS is unbound.'
	  // doesn't work in 'mvn install' tests though; probably some classpath issue which I can't be arsed debugging 
	  //"<% (require 'abcl-contrib)(require :jss) %>" +  
	  //"Hello, <%= (#\"get\" (nth 0 (jss:jlist-to-list muppets)) \"name\") %>\n";

	
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

    @SuppressWarnings({ "rawtypes", "unchecked" })
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
		b.put("muppets", muppets); // LispObject.getInstance(muppets, true));
		b.put("favouriteNumber", favouriteNumber);
		
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
		_testScript(RUBY_COUNTING_SCRIPT_GLOBAL);
	}
	*/
	
}
