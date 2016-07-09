package com.example.contrived;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

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

    /** A revision marker to be used in exception stack traces. */
    public static final String _revision = "$Id$";

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
		b.put("name", "Baron von Count"); // I have been told it is actually "Count von Count"
		b.put("maxCount", 4);
		engine.eval(input);
	}
}
