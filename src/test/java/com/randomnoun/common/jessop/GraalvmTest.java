package com.randomnoun.common.jessop;

import java.util.ArrayList;
import java.util.HashMap;

/* (c) 2020 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.graalvm.polyglot.proxy.ProxyObject;

import com.randomnoun.common.jessop.engine.graaljs.GraalJsBindingsConverter;

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
	    Bindings bindings = eng.getBindings(ScriptContext.ENGINE_SCOPE);
	    bindings.put("polyglot.js.allowAllAccess", true);
	    // bindings.put("engine.WarnInterpreterOnly", false);
	    
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
	    
	    Map<String, Object> v = new HashMap();
	    v.put("x", 1234);
	    
	    Map<String, Object> v2 = new HashMap();
	    v.put("y", v2);
	    v2.put("z", 1234);
	    
	    List<Object> list = new ArrayList();
	    list.add("abc");
	    Map<String, Object> v3 = new HashMap();
	    v3.put("the", "thing");
	    list.add(v3);
	    
	    v.put("list", list);
	    
	    result = eng.eval("var1.x");
	    System.out.println("var1.x is " + result);

	    result = eng.eval("var1.y");
	    System.out.println("var1.y is " + result);

	    // z is undefined
	    //result = eng.eval("var1.y.z");
	    //System.out.println("var1.y.z is " + result);

	    // list is not defined
	    //result = eng.eval("var1.list[0]");
	    //System.out.println("var1.list[0] is " + result);

	    
	    System.out.println("=== var2");
	    
	    eng.put("var2", v);
	    // var2.get('x') works
	    // var2['x'] is null
	    // var2.x is null
	    result = eng.eval("var2.x");
	    System.out.println("result is " + result);

	    System.out.println("=== var3");
	    
	    eng.put("var3", ProxyObject.fromMap(v));
	    // var2.get('x') works
	    // var2['x'] is null
	    // var2.x is null
	    result = eng.eval("var3.x");
	    System.out.println("var3.x is " + result);
	    result = eng.eval("var3.y");
	    System.out.println("var3.y is " + result);
	    result = eng.eval("var3.y.z");
	    System.out.println("var3.y.z is " + result);
	    result = eng.eval("var3.list");
	    System.out.println("var3.list is " + result);
	    result = eng.eval("var3.list[0]");
	    System.out.println("var3.list[0] is " + result);

	    System.out.println("=== var4");
	    
	    GraalJsBindingsConverter bc = new GraalJsBindingsConverter();
	    eng.put("var4", bc.toScriptObject(v));
	    eng.put("var4List", bc.toScriptObject(v.get("list")));
	    
	    // var2.get('x') works
	    // var2['x'] is null
	    // var2.x is null
	    
	    // lists in maps
	    
	    result = eng.eval("var4.x");
	    System.out.println("var4.x is " + result);
	    result = eng.eval("var4.y");
	    System.out.println("var4.y is " + result);
	    result = eng.eval("var4.y.z");
	    System.out.println("var4.y.z is " + result);
	    result = eng.eval("var4.list");
	    System.out.println("var4.list is " + result);
	    result = eng.eval("var4.list[0]");
	    System.out.println("var4.list[0] is " + result);
	    
	    // maps in lists
	    
	    result = eng.eval("var4List");
	    System.out.println("var4List is " + result);
	    result = eng.eval("var4List[0]");
	    System.out.println("var4List[0] is " + result);
	    result = eng.eval("var4List[1]");
	    System.out.println("var4List[1] is " + result);
	    result = eng.eval("var4List[1].the");
	    System.out.println("var4List[1].the is " + result);
	    
	    
	    /*
		ScriptContext defaultContext = eng.getContext();
		if (eng instanceof Compilable) {
			defaultContext.setAttribute(ScriptEngine.FILENAME, "THEFILENAME", ScriptContext.ENGINE_SCOPE);
		}
	    result = eng.eval("fnarg");
	    */
	    
	}
    
}
