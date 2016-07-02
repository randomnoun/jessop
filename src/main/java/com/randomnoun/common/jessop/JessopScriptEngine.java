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
	
	/** {@inheritDoc} */
	@Override
	public Object eval(String script, ScriptContext context) throws ScriptException {
		CompiledScript cscript = compile(script);
		return cscript.eval(context);
	}

	/** {@inheritDoc} */
	@Override
	public Object eval(Reader reader, ScriptContext context) throws ScriptException {
		CompiledScript cscript = compile(reader);
		return cscript.eval(context);
	}

	// if the user doesn't supply a context, we evaluate it with a null context (not the default context)
	// the JessopCompiledScript will then use the appropriate language's default context instead
	
	/** {@inheritDoc} */
	@Override
	public Object eval(String script) throws ScriptException {
        return eval(script, (ScriptContext) null);
    }

	/** {@inheritDoc} */
	@Override
	public Object eval(Reader reader) throws ScriptException {
        return eval(reader, (ScriptContext) null);
    }

	/** {@inheritDoc} */
	@Override
	public Bindings createBindings() {
		return new SimpleBindings();
	}

	/** {@inheritDoc} */
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
	/** {@inheritDoc} */
	public CompiledScript compile(String script) throws ScriptException {
		return compile(new StringReader(script));
	}

	/** {@inheritDoc} */
	@Override
	public CompiledScript compile(Reader script) throws ScriptException {
		try {
			String filename = (String) get(ScriptEngine.FILENAME);
			// logger.info("about to compile; filename=" + filename);
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
			
			// the final JSB is the one used to convert exceptions in the target script at runtime
			jsb = t.jsb;
			JessopDeclarations declarations = jsb.getDeclarations();
			
			// get this from the jessop declaration eventally, but for now:
			// if the underlying engine supports compilation, then compile that here, otherwise just store the source
			ScriptEngine engine = new ScriptEngineManager().getEngineByName(declarations.engine);  // nashorn in JDK9
			if (engine==null) {
				throw new ScriptException("java.scriptx engine '" + declarations.engine + "' not found");
			}
			
			// com.sun.script.javascript.RhinoScriptEngine m = (com.sun.script.javascript.RhinoScriptEngine) engine;

			// the newScript is compiled here, if the engine supports it
			return new JessopCompiledScript(engine, filename, newScript, jsb);
			
		} catch (IOException ioe) {
			throw new ScriptException(ioe);
		}
		
		
		
	}
}
