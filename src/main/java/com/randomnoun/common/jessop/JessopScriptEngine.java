package com.randomnoun.common.jessop;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;

import javax.script.AbstractScriptEngine;
import javax.script.Bindings;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import org.apache.log4j.Logger;

public class JessopScriptEngine extends AbstractScriptEngine implements Compilable {

	Logger logger = Logger.getLogger(JessopScriptEngine.class);
	
	ScriptEngineFactory factory;
	
	// so I guess we implement this twice then
	// going to use unix EOLs for everything for now
	
	// so should I have a Lexer here as well ? hmm. skip it for now.
	
	public static class JessopDeclarations {
		String engine;
	}

	
	// could have some kind of lineCountingPrintWriter, but let's just keep that in the JSB class
	static class Tokeniser {
		Logger logger = Logger.getLogger(Tokeniser.class);
		int state;
		int charOffset;    // character number; starts at 0
		int line;          // source line number; starts at 1
		int eline;         // expression start line. Whenever we emit anything, reset the eline to line
		StringBuilder sb;  // output stringBuilder
		StringBuilder esb; // expression (or directive) stringBuilder
		JessopScriptBuilder jsb;
		public Tokeniser(JessopScriptBuilder jsb) {
			state = 0;
			line = 1; eline = 1; 
			charOffset = 0;
			this.jsb = jsb;
			sb = new StringBuilder();
			esb = new StringBuilder();
			jsb.setTokeniser(this,  new JessopDeclarations());
		}
		public void setJessopScriptBuilder(JessopScriptBuilder jsb) {
			// use this to switch languages within the tokeniser
			this.jsb = jsb;
		}
		public void parseChar(char ch) throws ScriptException {
			charOffset++;
			// logger.debug("state " + state + " ch " + ch );
			switch (state) {
				case 0:  // initial state; parsing text to display
					if (ch=='<') {
						state = 1;
					} else {
						sb.append(ch);
					}
					break;
					
				case 1:  // parsed initial '<'
					if (ch=='%') {  // <% ... %> or <%= ... %>
						if (sb.length()>0) {
							jsb.emitText(eline, sb.toString());
							sb.setLength(0);
							eline = line;
						}
						state = 2;
					} else {
						// just a normal tag
						sb.append('<');
						sb.append(ch);
					}
					break;
					
				case 2: // parsed initial '<%'
					if (ch == '=') {  // <%= ... %>
						state = 3;
					} else if (ch=='@') { // <%@ ... %> declaration 
						state = 5;
					} else if (ch=='!') { // <%! ... %> block
						state = 6;
					} else if (ch=='-') { // <%-- ... --%> block
						state = 7;
					} else {        // <%  ... %>   NB: no space required after '<%'
						esb.append(ch);
						state = 4;
					}
					break;
					
				case 3:
					if (ch=='%') {
						state = 13;  // possibly closing % of <%= ... %> 
					} else {
						esb.append(ch);
					}
					break;
					
				case 4:
					if (ch=='%') {
						state = 14;  // possibly closing % of <% ... %> 
					} else {
						esb.append(ch);
					}
					break;
					
				case 5:
					if (ch=='"') {
						state = 16;  // start of directive attribute
						esb.append(ch);
					} else if (ch=='%') { // closing % of <@ ... %>
						state = 15;
					} else {
						esb.append(ch);
					}
					break;

				case 6:
					if (ch=='%') {
						state = 16;  // possibly closing % of <%! ... %> 
					} else {
						esb.append(ch);
					}
					break;
					
				case 7:
					if (ch=='-') {
						state = 8;   // second '-' of starting <%-- ... --%>
					} else {
						// could say that this is in state 4; e.g. <%-someFunction%>
						// but I'm going to chuck an exception
						throw new ScriptException("'<%-' can only start a '<%--' comment block", null, line);  // charOffset
					}
					break;
					
				case 8:
					if (ch=='-') {
						state = 9;   // possibly close '-' of <%-- ... --%>
					} else {
						// stay in state 8
						// ignore comments
					}
					break;
					
				case 9:
					if (ch=='-') {
						state = 10;  // possibly closing '--' of <%-- ... --%>
					} else {
						state = 8;
						// ignore comments
					}
					break;
				
				case 10:
					if (ch=='%') {   // possibly closing '--%' of <%-- ... --%>
						state = 11;
					} else {
						state = 8;
						// ignore comments
					}
					
				case 11:
					if (ch=='>') {   // closing '--%>' of <%-- ... --%>
						state = 0;
					} else {
						state = 8; 
					}
					break;
					
				case 13:
					if (ch=='>') {   // closing '%>' of <%= ... %>
						jsb.emitExpression(eline, esb.toString());
						esb.setLength(0);
						eline = line;
						state = 0;
					} else {
						esb.append(ch);
						state = 3; 
					}
					break;
				
				case 14:
					if (ch=='>') {   // closing '%>' of <% ... %>
						jsb.emitScriptlet(eline, esb.toString());
						esb.setLength(0);
						eline = line;
						state = 0;
					} else {
						esb.append(ch);
						state = 4; 
					}
					break;
					
				case 15:
					if (ch=='>') {   // closing '%>' of <%@ ... %> declaration
						jsb.emitDeclaration(eline, esb.toString());
						esb.setLength(0);
						eline = line;
						state = 0;
					} else {
						esb.append(ch);
						state = 5; 
					}
					break;
				
				case 16:
					if (ch=='"') {   // closing quote of <%@ ... %> declaration attribute
						esb.append(ch);
						state = 5;
					} else {
						esb.append(ch);
						// stay in state 16
					}
					break;
			}
			
			if (ch=='\n') { line++; }			
		}
		
		public void parseEndOfFile() throws ScriptException {
			// emit anythign that's left, raise exceptions if in invalid state
			// logger.debug("state " + state + " EOF");
			if (state!=0) {
				// @TODO better error messages
				throw new ScriptException("unexpected EOF (parse state=" + state + ")", null, line); // charOffset
			}
			if (sb.length()>0) {
				jsb.emitText(eline, sb.toString());
				sb.setLength(0);
				eline = line;
			}
		}
	}
	
	@Override
	public Object eval(String script, ScriptContext context)
			throws ScriptException {
		CompiledScript cscript = compile(script);
		return cscript.eval(context);
	}

	@Override
	public Object eval(Reader reader, ScriptContext context)
			throws ScriptException {
		CompiledScript cscript = compile(reader);
		return cscript.eval(context);
	}

	// if the user doesn't supply a context, we evaluate it with a null context (not the default context)
	// the JessopCompiledScript should then the appropriate language's default context instead
	public Object eval(String script) throws ScriptException {
        return eval(script, (ScriptContext) null);
    }
	public Object eval(Reader reader) throws ScriptException {
        return eval(reader, (ScriptContext) null);
    }

	@Override
	public Bindings createBindings() {
		return new SimpleBindings();
	}

	@Override
	public ScriptEngineFactory getFactory() {
		if (factory != null) {
			return factory;
		} else {
			return new JessopScriptEngineFactory();
		}
	}
	
	void setEngineFactory(ScriptEngineFactory fac) {
		factory = fac;
	}

	@Override
	public CompiledScript compile(String script) throws ScriptException {
		return compile(new StringReader(script));
	}

	public static class JessopCompiledScript extends CompiledScript {
		ScriptEngine engine;
		String source;
		public JessopCompiledScript(ScriptEngine engine, String source) {
			if (engine==null) { throw new NullPointerException("null engine"); }
			this.engine = engine;
			this.source = source;
		}
		
		@Override
		public Object eval(ScriptContext context) throws ScriptException {
			if (context==null) { 
				context = engine.getContext();
			}
			// get this from the jessop declaration eventually, but for now
			PrintWriter out = new PrintWriter(System.out, true);
			context.setAttribute("out",  out, ScriptContext.ENGINE_SCOPE); // should be something like SCRIPT_SCOPE, really
			Object result = engine.eval(source, context);
			out.flush();
			return result;
		}
		@Override
		public ScriptEngine getEngine() {
			return engine;
		}
		
		public String getSource() {
			return source;
		}
	
	}
	
	@Override
	public CompiledScript compile(Reader script) throws ScriptException {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
			PrintWriter pw = new PrintWriter(baos);
			JessopScriptBuilder jsb = new JavascriptJessopScriptBuilder(pw); // default for now
			Tokeniser t = new Tokeniser(jsb);
			int ch = script.read();
			while (ch!=-1) {
				t.parseChar((char) ch);
				ch = script.read();
			}
			t.parseEndOfFile();
			pw.flush();
			String newScript = baos.toString();
			
			JessopDeclarations declarations = jsb.getDeclarations();
			
			// get this from the jessop declaration eventally, but for now:
			// if the underlying engine supports compilation, then compile that here, otherwise just store the source
			ScriptEngine engine = new ScriptEngineManager().getEngineByName(declarations.engine);  // nashorn in JDK9
			if (engine==null) {
				throw new ScriptException("java.scriptx engine '" + declarations.engine + "' not found");
			}
			
			// com.sun.script.javascript.RhinoScriptEngine m = (com.sun.script.javascript.RhinoScriptEngine) engine;
			return new JessopCompiledScript(engine, newScript);
			
		} catch (IOException ioe) {
			throw new ScriptException(ioe);
		}
		
		
		
	}
}
