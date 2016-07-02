package com.randomnoun.common.jessop;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptException;

import org.apache.log4j.Logger;


/** This is an abstract class that supports generic support for creating template scripts from jessop source.
 * 
 * <p>This class is responsible for processing jessop declarations (e.g. <%@ jessop language="javascript" engine="rhino" %>),
 * and switching to the correct language JessopScriptBuilder implementation.
 * 
 * <p>Note that having multiple languages in the same script file is not yet supported by jessop. 
 * The declaration (if it exists) should therefore only appear once, and be the first thing that appears in a jessop source file.
 * 
 * <p>If the declaration is missing, then the default JavascriptJessopScriptBuilder is used, using the 'rhino' engine.
 * 
 * @author knoxg
 *
 */
// this should be subclassed by specific languages (javascript etc)
public abstract class AbstractJessopScriptBuilder implements JessopScriptBuilder {
	Logger logger = Logger.getLogger(AbstractJessopScriptBuilder.class);
	JessopDeclarations declarations;
	Tokeniser tokeniser;
	PrintWriter pw;

	public void setPrintWriter(PrintWriter pw) {
		this.pw = pw;
	}
	
	@Override
	public void setTokeniser(Tokeniser t, JessopDeclarations declarations) {
		this.tokeniser = t;
		this.declarations = declarations;
	}
	public JessopDeclarations getDeclarations() {
		return declarations;
	}

	@Override
	public void emitDeclaration(int line, String s) throws ScriptException {
		// declType attr1="val1" attr2="val2"
		// don't really feel like tokenising this at the moment
		s = s.trim();
		// can't do this in 1 regex for some reason
		//   Pattern declPattern = Pattern.compile("([^\\s\"]+)\\s*(?:(\\S+)=\"([^\"]*)\"\\s*)*$");
		// so breaking into subregexes
		String declType;
		Pattern declTypePattern = Pattern.compile("^([^\\s\"]+)");
		Matcher m = declTypePattern.matcher(s);
		if (m.find()) {
			declType = m.group(1);
		} else {
			throw new ScriptException("Could not parse declaration '" + s + "'", null, line);
		}
		s = s.substring(declType.length()).trim();
		logger.info("s=" + s);
		Pattern declAttrPattern = Pattern.compile("(\\S+)=\"([^\"]*)\"");
		m = declAttrPattern.matcher(s);
		while (m.find()) {
			// do something
			String attrName = m.group(1);
			String attrValue = m.group(2);
			if (attrName.equals("language")) {
				// change the JessopScriptBuilder based on the language
				// have a registry of these somewhere.
				
				JessopScriptBuilder newBuilder = ((JessopScriptEngineFactory) tokeniser.jse.getFactory()).getJessopScriptBuilderForLanguage(attrValue);
				newBuilder.setPrintWriter(pw);
				newBuilder.setTokeniser(tokeniser, declarations);   // pass on tokeniser state and declarations to new jsb
				tokeniser.setJessopScriptBuilder(newBuilder);       // tokeniser should use this jsb from this point on
				// should wait until all attributes are parsed, but hey
				if (declarations.engine==null) { declarations.engine = newBuilder.getDefaultScriptEngineName(); }
				
				/*
				JessopScriptBuilder newBuilder;
				if (attrValue.equals("javascript")) {
					newBuilder = new JavascriptJessopScriptBuilder(); 
					newBuilder.setPrintWriter(pw);
					newBuilder.setTokeniser(tokeniser, declarations);   // pass on tokeniser state and declarations to new jsb
					tokeniser.setJessopScriptBuilder(newBuilder);       // tokeniser should use this jsb from this point on
					if (declarations.engine==null) { declarations.engine = "rhino"; }  // default engine for javascript

				} else if (attrValue.equals("java")) {
					newBuilder = new JavaJessopScriptBuilder(); 
					newBuilder.setPrintWriter(pw);
					newBuilder.setTokeniser(tokeniser, declarations);   
					tokeniser.setJessopScriptBuilder(newBuilder);       
					if (declarations.engine==null) { declarations.engine = "beanshell"; }  // default engine for java

				} else if (attrValue.equals("lua")) {
					newBuilder = new LuaJessopScriptBuilder(); 
					newBuilder.setPrintWriter(pw);
					newBuilder.setTokeniser(tokeniser, declarations);   
					tokeniser.setJessopScriptBuilder(newBuilder);       
					if (declarations.engine==null) { declarations.engine = "luaj"; }  // default engine for lua

				} else if (attrValue.equals("python") || attrValue.equals("python2")) {
					newBuilder = new Python2JessopScriptBuilder(); 
					newBuilder.setPrintWriter(pw);
					newBuilder.setTokeniser(tokeniser, declarations);   
					tokeniser.setJessopScriptBuilder(newBuilder);       
					if (declarations.engine==null) { declarations.engine = "jython"; }  // default engine for lua

				} else {
					throw new IllegalArgumentException("Unknown language '" + attrValue + "'");
				}
				*/
				
			} else if (attrName.equals("engine")) {
				declarations.engine = attrValue;
			}
			logger.info("Found attr " + m.group(1) + "," + m.group(2));
		}
	}
	
	public abstract void emitText(int line, String s);
	public abstract void emitExpression(int line, String s);
	public abstract void emitScriptlet(int line, String s);
}