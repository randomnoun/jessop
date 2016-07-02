package com.randomnoun.common.jessop;

/** Things that are defined in the <%@ jessop ... %> declaration.
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
	
	public boolean isSuppressEol() {
		return suppressEol;
	}

	public void setSuppressEol(boolean suppressEol) {
		this.suppressEol = suppressEol;
	}

	String engine;
	boolean suppressEol;
	
	public String getEngine() {
		return engine;
	}

	public void setEngine(String engine) {
		this.engine = engine;
	}
	
}