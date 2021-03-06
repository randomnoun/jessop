package com.randomnoun.common.jessop;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import java.io.PrintWriter;

import javax.script.ScriptException;

/** Each target language we intend to support within jessop must have an implementation of this interface. 
 * Developers wishing to implement this interface should use the {@link AbstractJessopScriptBuilder} abstract class.
 * 
 * <p>Implementations of this class should return the language it supports (and the default script engine name) via the
 * {@link #getLanguage()} and {@link #getDefaultScriptEngineName()} methods.
 * 
 * <p>These values will be used to select the correct JessopScriptBuilder identified in the 
 * <code>&lt;%@ jessop language="xxx"%&gt;</code>
 * declaration in the jessop source file.
 * 
 * <p>When this class is instantiated, the caller will invoke {@link #setPrintWriter(PrintWriter)} and 
 * {@link #setTokeniserAndDeclarations(Tokeniser, JessopDeclarations)}.
 * 
 * <p>As the Tokeniser parses the jessop source file, it will invoke emit() methods on this class.
 * As these methods are called, this class should generate code in the target language via the printWriter.
 * 
 * <p>Care should be taken to ensure that code in the target language script is on the same line number
 * as the corresponding code on the input script, to make error messages more developer-friendly.
 * 
 * @author knoxg
 */
public interface JessopScriptBuilder {
	// lines are source line numbers; try to keep these intact in the generated script
	
	/** Returns the name of the language that this scriptBuilder can parse; e.g. "javascript" or "python2".
	 * 
	 * <p>This is used to register this JessopScriptBuilder in the registry and is used to lookup the JessopScriptBuilder
	 * from the language defined in the jessop script declaration.
	 * 
	 * @return the name of the language that this scriptBuilder can parse; e.g. "javascript" or "python2".
	 */
	String getLanguage();                                             
	
	/** Returns the name of the script engine that is used to evaluate this script; e.g. "rhino" or "jython"
	 * 
	 * @return the name of the script engine that is used to evaluate this script; e.g. "rhino" or "jython"
	 */
	String getDefaultScriptEngineName();                             

	/** Returns the name of the default exceptionConverter class that should be used for the default script engine,
	 * or null if no converter is required.
	 * 
	 * <p>Changing the engine in the jessop declaration will reset the the converter to null. 
	 * 
	 * @return the name of the default exceptionConverter class that should be used for the default script engine
	 */
	String getDefaultExceptionConverterClassName();                             

	/** Returns the name of the default bindingsConverter class that should be used for the default script engine,
	 * or null if no converter is required.
	 * 
	 * <p>Changing the engine in the jessop declaration will reset the the converter to null. 
	 * 
	 * @return the name of the default bindingsConverter class that should be used for the default script engine
	 */
	String getDefaultBindingsConverterClassName();                             

	
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
	void setTokeniserAndDeclarations(Tokeniser t, JessopDeclarations declarations);
	
	/** Called by the tokeniser and requests that this JessopScriptBuilder generate code to generate some text output.
	 * 
	 * <p>This method should escape the output and generate code that outputs this text in the implementation language.
	 *  
	 * @param line jessop source line number that begins this text output
	 * @param s the text to output; may include newlines.
	 */
	void emitText(int line, String s);        
	
	/** Called by the tokeniser and requests that this JessopScriptBuilder generate code to generate some evaluated output.
	 * i.e. process a <code>&lt;%= ... %&gt;</code> expression.
	 * 
	 * <p>This method should generate the code to evaluate and output the contents of the <code>&lt;%= ... %&gt;</code> expression.
	 * 
	 * <p>This method should attempt to preserve line numbers between the jessop source file and the generated code file.
	 * 
	 * @param line jessop source line number that begins this text output.
	 * @param s the contents of the <code>&lt;%= ... %&gt;</code> expression.
	 */
	void emitExpression(int line, String s);  // <%= ... %>
	

	/** Called by the tokeniser and requests that this JessopScriptBuilder copy the included code into the generated script.
 	 * i.e. process a <code>&lt;% ... %&gt;</code> scriptlet. (The 'scriptlet' term is the term used in the JSP specification)
 	 * 
	 * <p>This method should generate the same code that is contained within the <code>&lt;% ... %&gt;</code> scriptlet.
	 * 
	 * <p>This method should attempt to preserve line numbers between the jessop source file and the generated code file.
	 * 
	 * <p>This scriptlet may not be syntactically correct by itself and may require further scriptlets to function, e.g.
	 * <pre>
	 * &lt;% if (a &gt; b) { %&gt;
	 * something
	 * &lt;% } %&gt;
	 * </pre>
	 * 
	 * @param line jessop source line number that begins this scriptlet.
	 * @param s the contents of the <code>&lt;%= ... %&gt;</code> expression.
	 */
	void emitScriptlet(int line, String s);

	/** Called by the tokeniser and requests that this JessopScriptBuilder process a <code>&lt;%@ ... %&gt;</code> directive.
	 * 
	 * <p>The exact semantics of these declarations can be defined per-language, but 'jessop' declarations 
	 * should be processed by the AbstractJessopScriptBuilder, i.e. declarations that are in the form
	 * <code>&lt;%@ jessop language="xxx" %&gt;</code>
	 * 
	 * <p>Ideally, there's only one declaration, at the top of the source script.
	 * 
	 * @param line jessop source line number that begins this directive
	 * @param s the contents of the <code>&lt;%@ ... %&gt;</code> directive
	 * @throws ScriptException
	 */
	void emitDeclaration(int line, String s) throws ScriptException; 

	/** Returns the value of any jessop declarations that are in effect.
	 * 
	 * <p>Used to maintain declarations when switching JessopScriptBuilders, and to retrieve declarations after the script is built.
	 */
	JessopDeclarations getDeclarations();
	
}