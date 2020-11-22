package com.randomnoun.common.jessop;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ServiceLoader;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

/** A class to implement the {@link javax.script.ScriptEngineFactory} interface for the jessop templating language.
 * 
 * @author knoxg
 */
// was thinking of creating one of these per jessop language; (e.g. jessop-javascript), but 
// will just use declarations to define the language (and default to javascript if the declaration is missing)
// actually getNames() could return jessop-xxx variants based on what's in the registry, couldn't it.
// can't see how to get the value of that name though, so that's unfortunate
public class JessopScriptEngineFactory implements ScriptEngineFactory {

	/** The version of the jessop 'language' that this script engine implements */
    // may lag the release version if the syntax doesn't change
    // 1.0    - initial release
    // 1.0.2  - declarations can now have a bindingsConverter; syntax is still the same though
	public static final String JESSOP_LANGUAGE_VERSION = "1.0";
	
	@Override
	public String getEngineName() {
		return "jessop";
	}

	@Override
	public String getEngineVersion() {
		return getMavenVersion();
	}

	@Override
	public List<String> getExtensions() {
		return Collections.singletonList("jessop");
	}

	@Override
	public List<String> getMimeTypes() {
		return Collections.singletonList("application/x.jessop");
	}

	@Override
	public List<String> getNames() {
		return Collections.singletonList("jessop");
	}

	@Override
	public String getLanguageName() {
		return "jessop";
	}

	@Override
	public String getLanguageVersion() {
		return JESSOP_LANGUAGE_VERSION;
	}

	@Override
	public Object getParameter(String key) {
		if (key.equals(ScriptEngine.ENGINE)) { return getEngineName(); }
		else if (key.equals(ScriptEngine.ENGINE_VERSION)) { return getEngineVersion(); }
		else if (key.equals(ScriptEngine.LANGUAGE)) { return getLanguageName(); }
		else if (key.equals(ScriptEngine.LANGUAGE_VERSION)) { return getLanguageVersion(); }
		else if (key.equals("THREADING")) { return "MULTITHREADED"; } // probably depends on impl language
		else { 
			throw new IllegalArgumentException("Invalid key"); 
		}
		
	}

	// this is language dependent
	@Override
	public String getMethodCallSyntax(String obj, String m, String... args) {
		 String ret = obj;
	     ret += "." + m + "(";
	     for (int i = 0; i < args.length; i++) {
	         ret += args[i];
	         if (i < args.length - 1) {
	             ret += ",";
	         }
	     }
	     ret += ")";
	     return ret;
	}

	// this is language dependent
	@Override
	public String getOutputStatement(String toDisplay) {
		return "print(" + toDisplay + ")";
	}

	// this is language dependent
	@Override
	public String getProgram(String... statements) {
		String retval = "<?\n";
	    int len = statements.length;
	    for (int i = 0; i < len; i++) {
	        retval += statements[i] + ";\n";
	    }
	    return retval += "?>";
	}

	@Override
	public ScriptEngine getScriptEngine() {
		JessopScriptEngine se = new JessopScriptEngine();
		se.setEngineFactory(this);
		return se;
	}
	
	// versioning
	private static Properties getBuildProperties() throws IOException {
		InputStream is = JessopScriptEngineFactory.class.getClassLoader().getResourceAsStream("build.properties");
    	Properties props = new Properties();
    	if (is==null) {
    		return null;
    	} else {
	    	props.load(is);
	    	is.close();
    	}
    	return props;
	}
	
	private static String getMavenVersion() {
		try {
			Properties buildProperties = getBuildProperties();
			String mavenRelease = "(local build)";
			if (buildProperties!=null) {
				mavenRelease = buildProperties.getProperty("maven.pom.version");
				if (mavenRelease.equals("${pom.version}")) { mavenRelease="(local build)"; }
			}
			return mavenRelease;
		} catch (IOException ioe) {
			return "(unknown)";
		}
	}

	// could make this static, but there aren't going to be too many ScriptEngineFactory's around, surely.
	private Map<String, Class<? extends JessopScriptBuilder>> registry = null;
	
	// these methods are not part of the ScriptEngineFactory interface
	private Map<String, Class<? extends JessopScriptBuilder>> getRegistry() {
		if (registry!=null) { return registry; }
		
		Map<String, Class<? extends JessopScriptBuilder>> newRegistry = new HashMap<String, Class<? extends JessopScriptBuilder>>();
		ServiceLoader<JessopScriptBuilder> jsbLoader = ServiceLoader.load(JessopScriptBuilder.class);
		for (JessopScriptBuilder jsb : jsbLoader) {
			// System.out.println(jsb.getLanguage());
			newRegistry.put(jsb.getLanguage(), jsb.getClass()); // may not be able to re-use these objects, so just store the Class
			// it'd be nice if we could dynamically register new JessopScriptEngineFactories here as well.
			// will set the default language through a ScriptEngine var instead
		}

		registry = newRegistry;
		return registry;
	}
	
	public JessopScriptBuilder getJessopScriptBuilderForLanguage(String language) {
		Map<String, Class<? extends JessopScriptBuilder>> registry = getRegistry();
		Class<? extends JessopScriptBuilder> c = registry.get(language);
		if (c == null) { throw new IllegalArgumentException("No JessopScriptBuilder registered for language '" + language + "'"); }
		try {
			return c.newInstance();
		} catch (InstantiationException e) {
			throw new IllegalStateException("Could not instantiate JessopScriptBuilder for language '" + language + "'", e);
		} catch (IllegalAccessException e) {
			throw new IllegalStateException("Could not instantiate JessopScriptBuilder for language '" + language + "'", e);
		}
	}
	
	
}
