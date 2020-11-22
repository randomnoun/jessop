package com.randomnoun.common.jessop;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

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

import com.randomnoun.common.jessop.lang.JavascriptJessopScriptBuilder;

/** The jessop ScriptEngine class.
 * 
 * @author knoxg
 */
public class JessopScriptEngine extends AbstractScriptEngine implements Compilable {

	/** Logger instance for this class */
	Logger logger = Logger.getLogger(JessopScriptEngine.class);

	/** ScriptEngineFactory that created this class */
	ScriptEngineFactory factory;
	
    /** Reserved key for a named value that identifies the initial language used for jessop scripts.
     * If not set, will default to 'javascript'
     */
	public static final String JESSOP_LANGUAGE = "com.randommoun.common.jessop.language";
	
	/** Default value for the JESSOP_LANGUAGE key; has the value "javascript" */
	public static final String JESSOP_DEFAULT_LANGUAGE = "javascript";
	
    /** Reserved key for a named value that identifies the initial ScriptEngine used for jessop scripts.
     * If not set, will use the default engine for the default language.
     */
	public static final String JESSOP_ENGINE = "com.randommoun.common.jessop.engine";

	/** Default value for the JESSOP_ENGINE key; has the value "rhino" */
	public static final String JESSOP_DEFAULT_ENGINE = "rhino";
	
	
    /** Reserved key for a named value that sets the initial exception converter.
     * If not set, will use the default converter for the default language
     */
	public static final String JESSOP_EXCEPTION_CONVERTER = "com.randommoun.common.jessop.exceptionConverter";

	/** Default value for the JESSOP_EXCEPTION_CONVERTER key; has the value null */
	public static final String JESSOP_DEFAULT_EXCEPTION_CONVERTER = null;

    /** Reserved key for a named value that sets the initial bindings converter.
     * If not set, will use the default converter for the default language
     */
	public static final String JESSOP_BINDINGS_CONVERTER = "com.randommoun.common.jessop.bindingsConverter";

	/** Default value for the JESSOP_EXCEPTION_CONVERTER key; has the value null */
	// so this isn't final any more, since it might change depending on what's on the classpath
	public static String JESSOP_DEFAULT_BINDINGS_CONVERTER; //  = "com.randomnoun.common.jessop.engine.jvmRhino.JvmRhinoBindingsConverter";

	static {
		JavascriptJessopScriptBuilder jjsb = new JavascriptJessopScriptBuilder(); 
		JESSOP_DEFAULT_BINDINGS_CONVERTER = jjsb.getDefaultBindingsConverterClassName();
	}
	
	
    /** Reserved key for a named value that controls whether the target script is compiled
     * (providing the target engine allows it).
     * If not set, will default to 'true'.
     */
	public static final String JESSOP_COMPILE_TARGET = "com.randommoun.common.jessop.compileTarget";

	/** Default value for the JESSOP_ENGINE key; has the value "false" */
	public static final String JESSOP_DEFAULT_COMPILE_TARGET = "false";

	
    /** Reserved key for a named value that controls whether the target script 
     * will have EOLs suppressed after scriptlets that appear at the end of a line.
     * If not set, will default to 'false'.
     */
	public static final String JESSOP_SUPPRESS_EOL = "com.randommoun.common.jessop.suppressEol";

	/** Default value for the JESSOP_SUPPRESS_EOL key; has the value "false" */
	public static final String JESSOP_DEFAULT_SUPPRESS_EOL = "false";

	
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
			// if (initialLanguage == null) { initialLanguage = "javascript"; }

			JessopDeclarations declarations = new JessopDeclarations();
			// jessop defaults
			declarations.setEngine(JESSOP_DEFAULT_ENGINE);
			declarations.setExceptionConverter(JESSOP_DEFAULT_EXCEPTION_CONVERTER);
			declarations.setBindingsConverter(JESSOP_DEFAULT_BINDINGS_CONVERTER);
			declarations.setCompileTarget(Boolean.valueOf(JESSOP_DEFAULT_COMPILE_TARGET));
			declarations.setSuppressEol(Boolean.valueOf(JESSOP_DEFAULT_SUPPRESS_EOL));

			// ScriptEngine defaults
			String filename = (String) get(ScriptEngine.FILENAME);
			String initialLanguage = (String) get(JessopScriptEngine.JESSOP_LANGUAGE);
			String initialEngine = (String) get(JessopScriptEngine.JESSOP_ENGINE);
			String initialExceptionConverter = (String) get(JessopScriptEngine.JESSOP_EXCEPTION_CONVERTER);
			String initialBindingsConverter = (String) get(JessopScriptEngine.JESSOP_BINDINGS_CONVERTER);
			String initialCompileTarget = (String) get(JessopScriptEngine.JESSOP_COMPILE_TARGET);
			String initialSuppressEol = (String) get(JessopScriptEngine.JESSOP_SUPPRESS_EOL);
			if (initialLanguage==null) { initialLanguage = JESSOP_DEFAULT_LANGUAGE; }
			if (filename!=null) { declarations.setFilename(filename); }
			if (initialEngine!=null) { declarations.setEngine(initialEngine); }
			if (initialExceptionConverter!=null) { declarations.setExceptionConverter(initialExceptionConverter); }
			if (initialBindingsConverter!=null) { declarations.setBindingsConverter(initialBindingsConverter); }
			if (initialCompileTarget!=null) { declarations.setCompileTarget(Boolean.valueOf(initialCompileTarget)); }
			if (initialSuppressEol!=null) { declarations.setSuppressEol(Boolean.valueOf(initialSuppressEol)); }
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			PrintWriter pw = new PrintWriter(baos);
		    // new JavascriptJessopScriptBuilder(); // default for now
			JessopScriptBuilder jsb = ((JessopScriptEngineFactory) getFactory()).getJessopScriptBuilderForLanguage(initialLanguage);
			jsb.setPrintWriter(pw);
			Tokeniser t = new Tokeniser(this, jsb);
			jsb.setTokeniserAndDeclarations(t, declarations);
			
			// tokenise the script
			int ch = script.read();
			while (ch!=-1) {
				t.parseChar((char) ch);
				ch = script.read();
			}
			t.parseEndOfFile();
			pw.flush();
			
			// get the output from the PrintWriter
			String newScript = baos.toString();
			
			// the final JSB contains the final declarations that were in effect 
			declarations = t.jsb.getDeclarations();
			
			// get this from the jessop declaration eventally, but for now:
			// if the underlying engine supports compilation, then compile that here, otherwise just store the source
			ScriptEngine engine = new ScriptEngineManager().getEngineByName(declarations.engine);  // nashorn in JDK9
			if (engine==null) {
				throw new ScriptException("java.scriptx engine '" + declarations.engine + "' not found");
			}
			
			JessopBindingsConverter jbc = null;
			if (declarations.bindingsConverter !=null && !declarations.bindingsConverter.equals("")) {
				try {
					jbc = (JessopBindingsConverter) 
						Class.forName(declarations.bindingsConverter).newInstance();
				} catch (Exception e) {
					throw (ScriptException) new ScriptException(
					  "bindingsConverter '" + declarations.bindingsConverter + "' not loaded").initCause(e);
				}
			}
			
			JessopExceptionConverter jec = null;
			if (declarations.exceptionConverter!=null && !declarations.exceptionConverter.equals("")) {
				try {
					jec = (JessopExceptionConverter) 
						Class.forName(declarations.exceptionConverter).newInstance();
				} catch (Exception e) {
					throw (ScriptException) new ScriptException(
					  "exceptionConverter '" + declarations.exceptionConverter + "' not loaded").initCause(e);
				}
			}
			 
			// com.sun.script.javascript.RhinoScriptEngine m = (com.sun.script.javascript.RhinoScriptEngine) engine;
			// the newScript is compiled here, if the engine supports it
			return new JessopCompiledScript(engine, declarations.isCompileTarget(), 
				declarations.getFilename(), newScript, jec, jbc);
			
		} catch (IOException ioe) {
			throw new ScriptException(ioe);
		}
		
		
		
	}
}
