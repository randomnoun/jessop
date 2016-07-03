package com.example.contrived;

import java.io.File;
import java.io.FileNotFoundException;

import javax.script.Bindings;
import javax.script.ScriptEngineManager;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import java.util.Scanner;

/** Simple jessop example  
 * 
 * @author knoxg
 * @version $Id$
 */
public class JessopExample {

	public void main(String args[]) throws FileNotFoundException, ScriptException {
		String filename = args[1];
		String input = new Scanner(new File(filename)).useDelimiter("\\Z").next();
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		// maybe put a function in here, to be called by the script
		// or a compound object
		Bindings b = engine.createBindings();
		b.put("name", "Baron von Count");
		b.put("maxCount", 4);
		engine.eval(input);
	}
}
