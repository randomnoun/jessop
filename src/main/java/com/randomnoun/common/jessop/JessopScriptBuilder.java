package com.randomnoun.common.jessop;

import java.io.PrintWriter;

import javax.script.ScriptException;

/** Each language we intend to support within jessop must have an implementation of this interface. 
 * Developers wishing to implement this interface should use the AbstractJessopScriptBuilder abstract class.
 * 
 * <p>Implementations of this class should return the language it supports (and the default script engine name) via the
 * {@link #getLanguage()} and {@link #getDefaultScriptEngineName()} methods.
 * 
 * <p>These values will be used to select the correct JessopScriptBuilder identified in the jessop source file declaration.
 * 
 * <p>When this class is instantiated, the caller will invoke {@link #setPrintWriter()} and {@link #setTokeniser()}.
 * 
 * <p>As the Tokeniser parse the jessop source file, it will invoke emit() methods on this class.
 * As these methods are called, this class should generate code via the printWriter in the output (language-specific) script.
 * 
 * <p>Care should be taken to ensure that code in the output script is on the same line number
 * as the corresponding code on the input script, to make error messages easier to handle.
 * 
 * @author knoxg
 * @version $Id$
 */
public interface JessopScriptBuilder {
	// lines are source line numbers; try to keep these intact in the generated script
	
	/** Returns the name of the langauge that this scriptBuilder can parse; e.g. "javascript" or "python2".
	 * 
	 * <p>This is used to register this JessopScriptBuilder in the registry and is used to lookup the JessopScriptBuilder
	 * from the language defined in the jessop script declaration.
	 * 
	 * @return the name of the langauge that this scriptBuilder can parse; e.g. "javascript" or "python2".
	 */
	String getLanguage();                                             
	
	/** Returns the name of the script engine that is used to evaluate this script; e.g. "rhino" or "jython"
	 * 
	 * @return the name of the script engine that is used to evaluate this script; e.g. "rhino" or "jython"
	 */
	String getDefaultScriptEngineName();                             

	/** Sets the printWriter that this class will write to during emit() methods
	 * 
	 * @param pw the printWriter that this class will write to during emit() methods
	 */
	void setPrintWriter(PrintWriter pw);
	
	/** Sets the tokeniser and declaration that are in effect at the start of parsing. This allows this
	 * ScriptBuilder to switch ScriptBuilders.
	 * 
	 * @param t tokeniser that is processing the jessop source
	 * @param declarations jessop declarations that are in effect
	 */
	void setTokeniser(Tokeniser t, JessopDeclarations declarations);
	
	/** Called by the tokeniser and requests that this JessopScriptBuilder generate code to generate some text output.
	 * 
	 * <p>This method should escape the output and generate code that outputs this text in the implementation language.
	 *  
	 * @param line jessop source line number that begins this text output
	 * @param s the text to output; may include newlines.
	 */
	void emitText(int line, String s);        
	
	/** Called by the tokeniser and requests that this JessopScriptBuilder generate code to generate some evaluated output.
	 * i.e. process a <tt>&lt;%= ... %&gt;</tt> expression.
	 * 
	 * <p>This method should generate the code to evaluate and output the contents of the <tt>&lt;%= ... %&gt;</tt> expression.
	 * 
	 * <p>This method should attempt to preserve line numbers between the jessop source file and the generated code file.
	 * 
	 * @param line jessop source line number that begins this text output.
	 * @param s the contents of the <tt>&lt;%= ... %&gt;</tt> expression.
	 */
	void emitExpression(int line, String s);  // <%= ... %>
	

	/** Called by the tokeniser and requests that this JessopScriptBuilder copy the included code into the generated script.
 	 * i.e. process a <tt>&lt;% ... %&gt;</tt> scriptlet. (The 'scriptlet' term is the term used in the JSP specification)
 	 * 
	 * <p>This method should generate the same code that is contained within the <tt>&lt;% ... %&gt;</tt> scriptlet.
	 * 
	 * <p>This method should attempt to preserve line numbers between the jessop source file and the generated code file.
	 * 
	 * <p>This scriptlet may not be syntactically correct by itself and may require further scriptlets to function, e.g.
	 * <pre>
	 * &lt;% if (a>b) { %&gt;
	 * something
	 * &lt;% } %&gt;
	 * </pre>
	 * 
	 * @param line jessop source line number that begins this scriptlet.
	 * @param s the contents of the <tt>&lt;%= ... %&gt;</tt> expression.
	 */
	void emitScriptlet(int line, String s);

	/** Called by the tokeniser and requests that this JessopScriptBuilder process a <tt>&lt;%@ ... %&gt;</tt> directive.
	 * 
	 * <p>The exact semantics of these declarations can be defined per-language, but 'jessop' declarations 
	 * should be processed by the AbstractJessopScriptBuilder, i.e. declarations that are in the form
	 * <tt>&lt;%@ jessop language="xxx" %&gt;</tt>
	 * 
	 * <p>Ideally, there's only one declaration, at the top of the source script.
	 * 
	 * @param line jessop source line number that begins this directive
	 * @param s the contents of the <tt>&lt;%@ ... %&gt;</tt> directive
	 * @throws ScriptException
	 */
	void emitDeclaration(int line, String s) throws ScriptException; 

	/** Returns the value of any jessop declarations that are in effect.
	 * 
	 * <p>Used to maintain declarations when switching JessopScriptBuilders, and to retrieve declarations after the script is built.
	 */
	JessopDeclarations getDeclarations();
}