package com.randomnoun.common.jessop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.log4j.Logger;
import org.luaj.vm2.script.LuajContext;

/** The jessop ScriptEngine class.
 * 
 * @author knoxg
 */
public class JessopScriptEngine extends AbstractScriptEngine implements Compilable {

	/** Logger instance for this class */
	Logger logger = Logger.getLogger(JessopScriptEngine.class);

	/** ScriptEngineFactory that created this class */
	ScriptEngineFactory factory;
	
    /**
     * Reserved key for a named value that identifies
     * the initial language used for jessop scripts.
     */
	public static final String JESSOP_LANGUAGE = "com.randommoun.common.jessop.language";
	
	// so I guess we implement this twice then
	// let's always compile it if we can
	
	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		CompiledScript cscript = compile(script);
		return cscript.eval(context);
	}

	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		CompiledScript cscript = compile(reader);
		return cscript.eval(context);
	}

	// if the user doesn't supply a context, we evaluate it with a null context (not the default context)
	// the JessopCompiledScript will then use the appropriate language's default context instead
	
	@Override
	public Object eval(String script) throws ScriptException {
        return eval(script, (ScriptContext) null);
    }

	@Override
	public Object eval(Reader reader) throws ScriptException {
        return eval(reader, (ScriptContext) null);
    }

	@Override
	public Bindings createBindings() {
		return new SimpleBindings();
	}

	@Override
	public ScriptEngineFactory getFactory() {
		if (factory != null) {
			return factory;
		} else {
			return new JessopScriptEngineFactory();
		}
	}
	
	// package private; called by ScriptEngineFactory only
	void setEngineFactory(ScriptEngineFactory fac) {
		factory = fac;
	}

	@Override
	public CompiledScript compile(String script) throws ScriptException {
		return compile(new StringReader(script));
	}

	/** Container class for the script generated by the JessopScriptBuilder from the jessop source.
	 * 
	 * <p>If the implementation language supports compilation then this class also contains
	 * the compiled form of the generated script.
	 * 
	 * @author knoxg
	 */
	public static class JessopCompiledScript extends CompiledScript {
		ScriptEngine engine;   // implementation engine (e.g. rhino, jython)
		String source;         // generated script
		CompiledScript compiledSource; // compiled of script, if the implementation engine supports it
		
		public JessopCompiledScript(ScriptEngine engine, String source) throws ScriptException {
			if (engine==null) { throw new NullPointerException("null engine"); }
			this.engine = engine;
			this.source = source;
			if (engine instanceof Compilable) {
				try {
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
		            newContext.setBindings(context.getBindings(ScriptContext.ENGINE_SCOPE), ScriptContext.ENGINE_SCOPE);
			        newContext.setReader(context.getReader());
			        newContext.setWriter(context.getWriter());
			        newContext.setErrorWriter(context.getErrorWriter());
			        context = newContext;
				}
			}
			// get this from the jessop declaration eventually, but for now
			PrintWriter out = new PrintWriter(context.getWriter(), true);
			context.setAttribute("out",  out, ScriptContext.ENGINE_SCOPE); // should be something like SCRIPT_SCOPE, really
			Object result;
			if (compiledSource!=null) {
				result = compiledSource.eval(context);
			} else {
				result = engine.eval(source, context);
			}
			out.flush();
			return result;
		}

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
	
	@Override
	public CompiledScript compile(Reader script) throws ScriptException {
		try {
			String filename = (String) get(ScriptEngine.FILENAME);
			// String scriptEngineName = (String) get(ScriptEngine.NAME); // hmm. hmm i say. could use this to get the initial JessopScriptBuilder
			// let's just use a new engine-scoped value
			String language = (String) get(JessopScriptEngine.JESSOP_LANGUAGE);
			if (language == null) { language = "javascript"; }
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			PrintWriter pw = new PrintWriter(baos);
		    // new JavascriptJessopScriptBuilder(); // default for now
			JessopScriptBuilder jsb = ((JessopScriptEngineFactory) getFactory()).getJessopScriptBuilderForLanguage(language);
			jsb.setPrintWriter(pw);
			Tokeniser t = new Tokeniser(this, jsb);
			int ch = script.read();
			while (ch!=-1) {
				t.parseChar((char) ch);
				ch = script.read();
			}
			t.parseEndOfFile();
			pw.flush();
			String newScript = baos.toString();
			
			JessopDeclarations declarations = jsb.getDeclarations();
			
			// get this from the jessop declaration eventally, but for now:
			// if the underlying engine supports compilation, then compile that here, otherwise just store the source
			ScriptEngine engine = new ScriptEngineManager().getEngineByName(declarations.engine);  // nashorn in JDK9
			if (engine==null) {
				throw new ScriptException("java.scriptx engine '" + declarations.engine + "' not found");
			}
			
			// com.sun.script.javascript.RhinoScriptEngine m = (com.sun.script.javascript.RhinoScriptEngine) engine;

			// the newScript is compiled here, if the engine supports it
			return new JessopCompiledScript(engine, newScript);
			
		} catch (IOException ioe) {
			throw new ScriptException(ioe);
		}
		
		
		
	}
}
