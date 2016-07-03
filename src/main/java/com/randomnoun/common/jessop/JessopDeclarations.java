package com.randomnoun.common.jessop;

/** Things that are defined in the <%@ jessop ... %> declaration attributes.
 * 
 * <p>The AbstractJessopScriptBuilder currently processes all <tt>&lt;%@ ... %&gt;</tt> declarations,
 * including the jessop declarationType.
 *
 * <p>We're just storing these values as Strings here; this is just a transfer object POJO.
 * 
 * <p>Note that the target language isn't currently stored in here; the AbstractJessopScriptBuilder will change
 * the JessopScriptBuilder implementation instead.
 * 
 * @author knoxg
 * @version $Id$
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
	
	
	
}