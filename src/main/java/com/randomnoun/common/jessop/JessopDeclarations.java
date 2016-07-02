package com.randomnoun.common.jessop;

/** Things that are defined in the <%@ jessop ... %> declaration.
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
	
	String engine;
	
	public String getEngine() {
		return engine;
	}

	public void setEngine(String engine) {
		this.engine = engine;
	}
	
}