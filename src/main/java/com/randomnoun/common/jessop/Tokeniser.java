package com.randomnoun.common.jessop;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import javax.script.ScriptException;

import org.apache.log4j.Logger;

// could have some kind of lineCountingPrintWriter, but let's just keep that in the JSB class
// going to use unix EOLs for everything for now
// so should I have a Lexer here as well ? hmm. skip it for now.

public class Tokeniser {

    /** A revision marker to be used in exception stack traces. */
    public static final String _revision = "$Id$";
	Logger logger = Logger.getLogger(Tokeniser.class);
	int state;         // parse state
	int charOffset;    // character number (from start of file); starts at 0
	int line;          // source line number; starts at 1
	int eline;         // expression start line. Whenever we emit anything, reset the eline to line
	String unclosed;   // used in the error message if we hit EOF in an invalid state
	StringBuilder sb;  // output stringBuilder
	StringBuilder esb; // expression (or directive) stringBuilder

	JessopScriptEngine jse;  // only used to access the registry of JessopScriptBuilders
	JessopScriptBuilder jsb; // emit() methods are called on this during parsing
	public Tokeniser(JessopScriptEngine jse, JessopScriptBuilder jsb) {
		state = 0;
		line = 1; eline = 1; 
		charOffset = 0;
		this.jse = jse;
		this.jsb = jsb;
		sb = new StringBuilder();
		esb = new StringBuilder();
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
					unclosed = "<%";
					state = 2;
				} else if (ch=='<') {
					// normal '<' followed by an possible initial '<'
					sb.append(ch);
					unclosed = null;
					state = 1;
				} else {
					// just a normal tag
					sb.append('<');
					sb.append(ch);
					unclosed = null;
					state = 0;
				}
				break;
				
			case 2: // parsed initial '<%'
				if (ch == '=') {  // <%= ... %>
					unclosed = "<%=";
					state = 3;
				} else if (ch=='@') { // <%@ ... %> declaration 
					unclosed = "<%@";
					state = 5;
				} else if (ch=='!') { // <%! ... %> block
					unclosed = "<%!";
					state = 6;
				} else if (ch=='-') { // <%-- ... --%> block
					unclosed = "<%--";
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
				} else if (ch=='%') { // closing % of <%@ ... %>
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
					unclosed = null;
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
					unclosed = null;
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
					unclosed = null;
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
					unclosed = null;
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
		// emit anything that's left, raise exceptions if in invalid state
		// logger.debug("state " + state + " EOF");
		if (state != 0 && unclosed != null) {
			throw new ScriptException("unexpected EOF (unclosed '" + unclosed + "')", null, line); // charOffset
		}
		if (sb.length()>0) {
			jsb.emitText(eline, sb.toString());
			sb.setLength(0);
			eline = line;
		}
	}
}