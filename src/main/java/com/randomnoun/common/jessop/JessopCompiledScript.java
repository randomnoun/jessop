package com.randomnoun.common.jessop;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import java.io.PrintWriter;

import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.luaj.vm2.LuaError;

/** Container class for the target script generated by the JessopScriptBuilder from the jessop source.
 * 
 * <p>If the implementation language supports compilation then this class also contains
 * the compiled form of the generated script.
 * 
 * @author knoxg
 * @version $Id$
 */
public class JessopCompiledScript extends CompiledScript {

    /** A revision marker to be used in exception stack traces. */
    public static final String _revision = "$Id$";
	
	/** target implementation engine (e.g. rhino, jython) */
	ScriptEngine engine;    
	
	/** generated script in target language */
	String source;         

	/** filename */
	String filename;         

	/** compiled target language source, if the implementation engine supports it */
	CompiledScript compiledSource;
	
	/** the JessopExceptionConverter to use to convert runtime exceptions */
	JessopExceptionConverter jec;
	
	public JessopCompiledScript(ScriptEngine engine,
		boolean isCompileTarget,
		String filename, String source,
		JessopExceptionConverter jec) throws ScriptException {
		if (engine==null) { throw new NullPointerException("null engine"); }
		this.filename = filename;
		this.engine = engine;
		this.source = source;
		this.jec = jec;
		if (isCompileTarget && (engine instanceof Compilable)) {
			try {
				// could have another declaration to suppress compilation here
				// for rhino, the FILENAME needs to be defined at compilation time, not execution time
				if (filename!=null) {
					// this is the default context, not the context used at runtime
					ScriptContext defaultContext = engine.getContext();
					defaultContext.setAttribute(ScriptEngine.FILENAME, filename, ScriptContext.ENGINE_SCOPE);
					// this is the target engine, not the jessop engine
					engine.put(ScriptEngine.FILENAME, filename); 
				}
				this.compiledSource = ((Compilable) engine).compile(source);
			} catch (Error e) {
				// bsh says it implements Compilable, but then throws an 'unimplemented' Error.
				if ("unimplemented".equals(e.getMessage())) { 
					/* ignore */ 
				} else {
					throw e;
				}
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	public Object eval(ScriptContext context) throws ScriptException {
		if (context==null) { 
			context = engine.getContext();
		} else {
			// may have to convert this context to whatever this engine expects (here's looking at you, lua)
			ScriptContext newContext = engine.getContext();
			if (newContext.getClass().equals(context.getClass())) {
				// it's fine
			} else {
		        Bindings gs = context.getBindings(ScriptContext.GLOBAL_SCOPE);
		        if (gs != null) {
		            newContext.setBindings(gs, ScriptContext.GLOBAL_SCOPE);
		        }
		        // this should transfer the filename for lua, but doesn't. Ah. filename may not be in the source context yet.
	            newContext.setBindings(context.getBindings(ScriptContext.ENGINE_SCOPE), ScriptContext.ENGINE_SCOPE);
		        newContext.setReader(context.getReader());
		        newContext.setWriter(context.getWriter());
		        newContext.setErrorWriter(context.getErrorWriter());
		        // newContext.setAttribute(ScriptEngine.FILENAME, filename, ScriptContext.ENGINE_SCOPE);
				// this is the target engine, not the jessop engine
				// engine.put(ScriptEngine.FILENAME, filename); 
		        
		        context = newContext;
			}
		}
		// get this from the jessop declaration eventually, but for now
		PrintWriter out = new PrintWriter(context.getWriter(), true);
		context.setAttribute("out",  out, ScriptContext.ENGINE_SCOPE); // should be something like SCRIPT_SCOPE, really
		context.setAttribute(ScriptEngine.FILENAME,  filename, ScriptContext.ENGINE_SCOPE); // for engines that need this at runtime
		Object result;
		try {
			if (compiledSource!=null) {
				result = compiledSource.eval(context); // these throw LuaErrors instead of ScriptExceptions
			} else {
				result = engine.eval(source, context);
			}
		} catch (Throwable t) {
			if (jec==null) {
				throw t;
			} else {
				throw jec.toScriptException(context, t);
			}
		}
		out.flush();
		return result;
	}

	/** {@inheritDoc} */
	@Override
	public ScriptEngine getEngine() {
		return engine;
	}
	
	/** Return the generated source; can be used for debugging.
	 * 
	 * @return the generated source
	 */
	public String getSource() {
		return source;
	}

}