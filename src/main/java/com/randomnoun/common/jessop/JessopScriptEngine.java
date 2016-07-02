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

public class JessopScriptEngine extends AbstractScriptEngine implements Compilable {

	Logger logger = Logger.getLogger(JessopScriptEngine.class);
	
	ScriptEngineFactory factory;
	
	// so I guess we implement this twice then
	
	@Override
	public Object eval(String script, ScriptContext context)
			throws ScriptException {
		CompiledScript cscript = compile(script);
		return cscript.eval(context);
	}

	@Override
	public Object eval(Reader reader, ScriptContext context)
			throws ScriptException {
		CompiledScript cscript = compile(reader);
		return cscript.eval(context);
	}

	// if the user doesn't supply a context, we evaluate it with a null context (not the default context)
	// the JessopCompiledScript should then the appropriate language's default context instead
	public Object eval(String script) throws ScriptException {
        return eval(script, (ScriptContext) null);
    }
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

	public static class JessopCompiledScript extends CompiledScript {
		ScriptEngine engine;
		String source;
		public JessopCompiledScript(ScriptEngine engine, String source) {
			if (engine==null) { throw new NullPointerException("null engine"); }
			this.engine = engine;
			this.source = source;
		}
		
		@Override
		public Object eval(ScriptContext context) throws ScriptException {
			if (context==null) { 
				context = engine.getContext();
			}
			// get this from the jessop declaration eventually, but for now
			PrintWriter out = new PrintWriter(context.getWriter(), true);
			context.setAttribute("out",  out, ScriptContext.ENGINE_SCOPE); // should be something like SCRIPT_SCOPE, really
			Object result = engine.eval(source, context);
			out.flush();
			return result;
		}
		@Override
		public ScriptEngine getEngine() {
			return engine;
		}
		
		public String getSource() {
			return source;
		}
	
	}
	
	@Override
	public CompiledScript compile(Reader script) throws ScriptException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			PrintWriter pw = new PrintWriter(baos);
			JessopScriptBuilder jsb = new JavascriptJessopScriptBuilder(); // default for now
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
			return new JessopCompiledScript(engine, newScript);
			
		} catch (IOException ioe) {
			throw new ScriptException(ioe);
		}
		
		
		
	}
}
