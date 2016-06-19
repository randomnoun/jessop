package com.randomnoun.common.jessop;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

public class JessopScriptEngineFactory implements ScriptEngineFactory {

	/** The version of the jessop 'language' that this script engine implements */
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
		// TODO Auto-generated method stub
		return null;
	}

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

	@Override
	public String getOutputStatement(String toDisplay) {
		return "print(" + toDisplay + ")";
	}

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

	
}
