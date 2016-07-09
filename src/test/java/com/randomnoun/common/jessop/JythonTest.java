package com.randomnoun.common.jessop;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import java.util.List;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.TestCase;

// separate jython test since that doesn't seem to be working for me
// so apparently some of the builds in mvn central don't actually work.

/** This is a separate jython test because I'm having issues getting jython to do anything at all.
 * 
 * <p>Turns out that some of the builds in mvn central don't actually work.
 * 
 * @author knoxg
 * @version $Id$
 */
public class JythonTest extends TestCase {

    /** A revision marker to be used in exception stack traces. */
    public static final String _revision = "$Id$";

	public static void listEngines(){ // Note 1
        ScriptEngineManager mgr = new ScriptEngineManager();
        List<ScriptEngineFactory> factories =
                mgr.getEngineFactories();
        for (ScriptEngineFactory factory: factories) {
            System.out.println("ScriptEngineFactory Info");
            String engName = factory.getEngineName();
            String engVersion = factory.getEngineVersion();
            String langName = factory.getLanguageName();
            String langVersion = factory.getLanguageVersion();
            System.out.println("\tScript Engine: " + engName + ":" +
                               engVersion);
            List<String> engNames = factory.getNames();
            for(String name: engNames) {
                System.out.println("\tEngine Alias: " + name);
            }
            System.out.println("\tLanguage: " + langName + ":" +
                               langVersion);
        }
    }
	public void testJython() throws ScriptException {
		// -Dpython.console.encoding=UTF-8
		// see http://stackoverflow.com/questions/30443537/how-do-i-fix-unsupportedcharsetexception-in-eclipse-kepler-luna-with-jython-pyde
		System.setProperty("python.console.encoding", "UTF-8");
	
		listEngines(); // Note 1
		
		// this is the same test code as on https://wiki.python.org/jython/LearningJython#using-script-engine-support
		ScriptEngineManager mgr = new ScriptEngineManager(); // Note 2
	    ScriptEngine eng = mgr.getEngineByName("python");  

	    // except that eng here is null.
	    System.out.println("eng: " + String.valueOf(eng));
	    eng.put("var1", new Integer(257)); // Note 3
	    eng.eval("print 'var1: %s' % var1");
	    eng.eval("import sys"); // Note 4
	    eng.eval("print sys.version");
	}
    
}
