package com.randomnoun.common.jessop;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

/** This object contains values that are defined in the <%@ jessop ... %> declaration attributes.
 * 
 * <p>The {@link AbstractJessopScriptBuilder} currently processes all <tt>&lt;%@ ... %&gt;</tt> declarations,
 * including the <tt>jessop</tt> declarationType.
 *
 * <p>This class acts as a Plain Old Java Object (POJO) and does not perform any processing of directives.
 * 
 * <p>Note that the target language is not stored in this class; the {@link AbstractJessopScriptBuilder} will change
 * the {@link JessopScriptBuilder} implementation instead.
 * 
 * @author knoxg
 */
public class JessopDeclarations {


	// things we might want to add later:
	// contentType="text/html; charset=utf-8"
    // pageEncoding="utf-8"
    // suppressEol="true" <-- if a line consists just of a '%>' token, then don't generate the newline after it in the output
	//   (seeing as I'm mainly using this for code generators at the moment, and I don't like all this whitespace)
	// NB: still want line numbers to match between jessop source and target language source.
	
	// might want to add a postProcessor attribute to this
	// to do things like css/js minification, or wiki markup processing, or whatever
	// but I'm intentionally keeping this simple for now
	// may want to use different declarationTypes for things like that

	String engine;
	String exceptionConverter;
	String bindingsConverter;
	String filename;
	boolean suppressEol = false;
	boolean compileTarget = true;
	
	public boolean isSuppressEol() {
		return suppressEol;
	}

	public void setSuppressEol(boolean suppressEol) {
		this.suppressEol = suppressEol;
	}

	public String getEngine() {
		return engine;
	}

	public void setEngine(String engine) {
		this.engine = engine;
	}

	public String getExceptionConverter() {
		return exceptionConverter;
	}

	public void setExceptionConverter(String exceptionConverter) {
		this.exceptionConverter = exceptionConverter;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public boolean isCompileTarget() {
		return compileTarget;
	}

	public void setCompileTarget(boolean compileTarget) {
		this.compileTarget = compileTarget;
	}

	public String getBindingsConverter() {
		return bindingsConverter;
	}

	public void setBindingsConverter(String bindingsConverter) {
		this.bindingsConverter = bindingsConverter;
	}
	
	
	
}