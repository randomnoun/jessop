package com.example.contrived;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/** Simple jessop example  
 * 
 * @author knoxg
 */
public class JessopExample {

	public void main(String args[]) throws FileNotFoundException, ScriptException {
		String filename = args[1];
		Scanner scanner = new Scanner(new File(filename));
		String input = scanner.useDelimiter("\\Z").next();
		scanner.close();
		ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
		if (engine==null) { throw new IllegalStateException("Missing engine 'jessop'"); }
		// maybe put a function in here, to be called by the script
		// or a compound object
		Bindings b = engine.createBindings();
		b.put("name", "Count von Count"); // I have been told it is actually "Count von Count"
		b.put("maxCount", 4);
		engine.eval(input);
	}
}
