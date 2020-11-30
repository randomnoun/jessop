package com.randomnoun.common.jessop;

/* (c) 2020 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import java.util.List;

import javax.script.Compilable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.TestCase;

/** Test that graalvm works
 * 
 * @author knoxg
 */
public class GraalvmTest extends TestCase {

	public static void listEngines(){ // Note 1
        ScriptEngineManager mgr = new ScriptEngineManager();
        List<ScriptEngineFactory> factories = mgr.getEngineFactories();
        for (ScriptEngineFactory factory: factories) {
            System.out.println("ScriptEngineFactory Info");
            String engName = factory.getEngineName();
            String engVersion = factory.getEngineVersion();
            String langName = factory.getLanguageName();
            String langVersion = factory.getLanguageVersion();
            System.out.println("\tScript Engine: " + engName + ":" + engVersion);
            List<String> engNames = factory.getNames();
            for(String name: engNames) {
                System.out.println("\tEngine Alias: " + name);
            }
            System.out.println("\tLanguage: " + langName + ":" + langVersion);
        }
    }
	public void testGraalvm() throws ScriptException {
	
		listEngines();
		
		// this is the same test code as on https://wiki.python.org/jython/LearningJython#using-script-engine-support
		ScriptEngineManager mgr = new ScriptEngineManager();
	    ScriptEngine eng = mgr.getEngineByName("graal-js");
		// ScriptEngine eng = mgr.getEngineByName("rhino");
	    
	    // this is GraalJSScriptEngine
		// rhino is com.sun.script.javascript.RhinoScriptEngine
	    // graal is  com.oracle.truffle.js.scriptengine.GraalJSScriptEngine
	    System.out.println("eng is " + eng.getClass().getName());

	    // except that eng here is null.
	    System.out.println("eng: " + String.valueOf(eng));
	    eng.put("var1", Integer.valueOf(257));
	    Object result = eng.eval("var1");
	    System.out.println("result is " + result);
	    
	    /*
		ScriptContext defaultContext = eng.getContext();
		if (eng instanceof Compilable) {
			defaultContext.setAttribute(ScriptEngine.FILENAME, "THEFILENAME", ScriptContext.ENGINE_SCOPE);
		}
	    result = eng.eval("fnarg");
	    */
	    
	}
    
}
