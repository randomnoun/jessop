package com.randomnoun.common.jessop;

import java.io.PrintWriter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import org.apache.log4j.Logger;


/** This is an abstract class that supports generic support for creating template scripts from jessop source.
 * 
 * <p>This class is responsible for processing jessop declarations (e.g. <tt>&lt;%@ jessop language="javascript" engine="rhino" %&gt;</tt>),
 * and switching to the correct language JessopScriptBuilder implementation.
 * 
 * <p>Note that having multiple languages in the same script file is not yet supported by jessop. 
 * The declaration (if it exists) should therefore only appear once, and be the first thing that appears in a jessop source file.
 * 
 * <p>If the declaration is missing, then the default JavascriptJessopScriptBuilder is used, using the 'rhino' engine.
 * 
 * @author knoxg
 */
// this should be subclassed by specific languages (javascript etc)
public abstract class AbstractJessopScriptBuilder implements JessopScriptBuilder {
	protected Logger logger = Logger.getLogger(AbstractJessopScriptBuilder.class);
	protected JessopDeclarations declarations;
	protected Tokeniser tokeniser;
	protected PrintWriter pw;

	@Override
	public void setPrintWriter(PrintWriter pw) {
		this.pw = pw;
	}
	
	@Override
	public void setTokeniserAndDeclarations(Tokeniser t, JessopDeclarations declarations) {
		this.tokeniser = t;
		this.declarations = declarations;
	}
	@Override
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
		if (!declType.equals("jessop")) {
			logger.warn("Unknown declaration type '" + declType + "'");
			// just ignore unknown declarations
			return;
		}
		s = s.substring(declType.length()).trim();
		logger.debug("s=" + s);
		Pattern declAttrPattern = Pattern.compile("(\\S+)=\"([^\"]*)\"");
		m = declAttrPattern.matcher(s);
		while (m.find()) {
			// do something
			String attrName = m.group(1);
			String attrValue = m.group(2);
			if (attrName.equals("language")) {
				// change the JessopScriptBuilder based on the language
				// the registry of ScriptBuilders is kept in the EngineFactory
				JessopScriptEngineFactory jsf = (JessopScriptEngineFactory) tokeniser.jse.getFactory();
				JessopScriptBuilder newBuilder = jsf.getJessopScriptBuilderForLanguage(attrValue);
				newBuilder.setPrintWriter(pw);
				newBuilder.setTokeniserAndDeclarations(tokeniser, declarations);   // pass on tokeniser state and declarations to new jsb
				tokeniser.setJessopScriptBuilder(newBuilder);       // tokeniser should use this jsb from this point on
				// should probably wait until all attributes are parsed, but hey
				if (declarations.engine==null) { 
					declarations.engine = newBuilder.getDefaultScriptEngineName();
					declarations.exceptionConverter = newBuilder.getDefaultExceptionConverterClassName();
				}
				
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
				// if we're changing engines, this will reset the default exception converter.
				// we may want to keep a registry of engine names -> ExceptionConverters
				// at a later stage
				if (!attrValue.equals(declarations.getEngine())) {
					declarations.setExceptionConverter(null);
				}
				declarations.setEngine(attrValue);
				
			} else if (attrName.equals("suppressEol")) {
				declarations.setSuppressEol(Boolean.valueOf(attrValue));

			} else if (attrName.equals("compileTarget")) {
				declarations.setCompileTarget(Boolean.valueOf(attrValue));

			} else if (attrName.equals("filename")) {
				declarations.setFilename(attrValue);

			} else if (attrName.equals("exceptionConverter")) {
				declarations.setExceptionConverter(attrValue);
			
			}
			logger.debug("Found attr " + m.group(1) + "," + m.group(2));
		}
	}
	
	@Override
	public abstract void emitText(int line, String s);
	
	@Override
	public abstract void emitExpression(int line, String s);
	
	@Override
	public abstract void emitScriptlet(int line, String s);
	
	@Override
	public String getDefaultExceptionConverterClassName() {
		return null;
	}
	
	/** Conditionally remove the first newline from the supplied string.
	 * 
	 * <p>This method is used to perform <tt>suppressEol</tt> declaration processing.
	 * 
	 * <p>When the <tt>suppressEol</tt> declaration is <tt>true</tt>, and the text to be emitted by the output script
	 * immediately follows a scriptlet and begins with a newline (or whitespace followed by a newline), 
	 * then we want to remove that (whitespace and) newline.
	 * 
	 * <p>If there are non-whitespace characters before the first newline, then it is not suppressed.
	 * 
	 * @param s text which is to be emitted by the output script
	 * @param suppressEol if true, remove the beginning whitespace and newline, if it exists.
	 * 
	 * @return the supplied string, with the first newline conditionally removed
	 */
	protected String suppressEol(String s, boolean suppressEol) {
		// ok. if s starts with a newline, 
		// *and* suppressEol is true,
		// *and* this text is being emitted on a line that has nothing but expressions (and whitespace), 
		// then suppress the newline.
		if (s.indexOf("\n")!=-1 && suppressEol) {
			boolean isFirstLineJustWhitespace = true;
			int pos = 0;
			while (pos<s.length() && isFirstLineJustWhitespace) {
				char ch = s.charAt(pos);
				if (ch=='\n') { pos++; break; }
				if (!Character.isWhitespace(ch)) { isFirstLineJustWhitespace = false; break; }
				pos++;
			}
			if (isFirstLineJustWhitespace) {
				s = s.substring(pos);
			}
		}
		return s;
	}
}