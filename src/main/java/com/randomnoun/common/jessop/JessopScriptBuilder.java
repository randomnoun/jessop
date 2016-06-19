package com.randomnoun.common.jessop;

import javax.script.ScriptException;

import com.randomnoun.common.jessop.JessopScriptEngine.JessopDeclarations;
import com.randomnoun.common.jessop.JessopScriptEngine.Tokeniser;

// need one of these for each language we intend to support
// the Tokeniser will call emit() methods in here as it parses the source script;
// classes that implement this interface should then generate code in the output script
interface JessopScriptBuilder {
	// lines are source line numbers; try to keep these intact in the generated script
	JessopDeclarations getDeclarations();                            // to retrieve declarations after the script is built
	void setTokeniser(Tokeniser t, JessopDeclarations declarations); // set at the start of script parsing; allows this ScriptBuilder to switch ScriptBuilders
	void emitText(int line, String s);        // ...
	void emitExpression(int line, String s);  // <%= ... %>
	void emitScriptlet(int line, String s);   // <% ... %> . JSP calls these things scriptlets
	void emitDeclaration(int line, String s) throws ScriptException; // <%@ ... %>. ideally, there's only one of these, at the top of the source script 
}