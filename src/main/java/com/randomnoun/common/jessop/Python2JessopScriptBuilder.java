package com.randomnoun.common.jessop;

import org.apache.log4j.Logger;

public class Python2JessopScriptBuilder extends AbstractJessopScriptBuilder implements JessopScriptBuilder {
	Logger logger = Logger.getLogger(Python2JessopScriptBuilder.class);
	int outputLine = 1; // current output line
	int outputCol = 1;  // current output column
	int indent = 0;     // current number of spaces at start of line (we use 4-space indents)
	public Python2JessopScriptBuilder() {
	}
	private void skipToLine(int line, int indent) {
		if (outputLine > line) {
			// could allow, but then that'll open another can of worms 
			// throw new IllegalArgumentException("cannot generate output on same line as starting new python block");
			logger.warn("can't go back to line " + line + " (outputLine=" + outputLine + "); line numbers may be inaccurate");
		}
		while (outputLine < line) { print("\n"); }
		while (outputCol < indent) { print(" "); }
		// for (int i=0; i<indent; i++) { print(" "); }
	}
	private void print(String s) {
		// logger.info("** PRINT " + s);
		pw.print(s);
		for (int i=0; i<s.length(); i++) {
			if (s.charAt(i)=='\n') { outputLine++; outputCol = 1; }
			else { outputCol++; }
		}
	}
	private static String escapePython(String string) {
		/* valid escapes ( https://docs.python.org/2.0/ref/strings.html )
\a	bell
\b	back space
\f	form feed
\n	newline
\r	carriage return
\t	horizontal tab
\v	vertical tab
\\	backslash
\"	double quote
\'	single quote
		 */
		
    	StringBuilder sb = new StringBuilder(string.length());
    	String escapeChars = "\u0007" + "\u0008" + "\u000f" + "\n" + "\r" + "\u0009" + "\u000b" + "\\" + "\"" + "'";
    	String backslashChars = "abfnrtv\\\"'";
		for (int i = 0; i<string.length(); i++) {
			char ch = string.charAt(i);
			int pos = escapeChars.indexOf(ch);
			if (pos !=- 1) {
			   sb.append("\\" + backslashChars.charAt(pos));
			
			// so apparently lua allows any character in a string whatsoever. looking forward to seeing this breaking.
			// have seen some examples of, e.g. \006 to represent chars, but nothing in the lua spec
			   
			} else if (ch<32 && (ch>126 && ch <= 255)) {
				String hex = Integer.toString(ch, 16);
				sb.append("\\x" + "00".substring(0, 2-hex.length()) + hex);
				sb.append(ch);
				
			} else if (ch<=255) {
				sb.append(ch);
				
			} else {
				throw new IllegalArgumentException("Cannot escape characters > 0xFF in python2 (found char '" + ch + "'; code=" + ((int) ch) + ")");
			}
		}
        return sb.toString();
    }
	
	@Override
	public void emitText(int line, String s) {
		skipToLine(line, indent);
		print("out.write(\"" + escapePython(s) + "\");");
	}
	@Override
	public void emitExpression(int line, String s) {
		skipToLine(line, indent);
		print("out.write((str) (" + s + "));"); // coerce to String
	}
	@Override
	public void emitScriptlet(int line, String s) {
		if (outputLine > line) {
			throw new IllegalArgumentException("cannot generate scriptlet output on same line as starting new python block");
		}

		skipToLine(line, indent);
		// if there's content before the first newline, remove whitespace from the beginning
		// so that we maintain our line/indentation position
		int pos = s.indexOf("\n"); if (pos==-1) { pos = s.length(); }
		int i=0;
		while (i < s.length() && s.charAt(i)==' ') { i++; }
		if (s.charAt(i)=='\t') { throw new IllegalArgumentException("tab indentation for python scriplets not supported"); }
		s = s.substring(i);
		
		logger.debug("scriptlet is '" + s + "'");
		print(s);
		/*
		  <%
		      for i=1..10:
		        something
		        somethingElse
		  %>no idea whether this is in the for loop or not
		  
 		  <%
 		      for i=1..10:
		  %>presumably this is in the for loop. not sure how to terminate it though.
		  <%
		      pass;  # empty statement on a line could indicate end of block
		  %>
		      
		 */
		
		// get the amount of indentation on the last non-blank line
		int endLinePos = s.length();
		int startLinePos = s.lastIndexOf("\n");
		while (s.substring(startLinePos+1).trim().equals("")) { 
			endLinePos = startLinePos; 
			startLinePos = s.lastIndexOf("\n", startLinePos-1); 
		}
		startLinePos++; // don't include the '\n'
		
		i = 0;
		while (startLinePos + i < endLinePos && s.charAt(i)==' ') { i++; }
		logger.debug("python last newline pos=" + startLinePos + ", indent on last line=" + i);
		if (s.charAt(startLinePos + i)=='\t') { 
			// could support this later, perhaps
			throw new IllegalArgumentException("tab indentation for python scriplets not supported");
		}
		indent = i;
		if (s.substring(startLinePos, endLinePos).trim().endsWith(":")) {
			logger.debug("last non-blank line endsWith ':', indenting by 4");
			// this may mean some loop constructs will now fail
			// e.g. <% for i in range (0,10): %><%= something %>
			print("\n"); 
			indent += 4;
		}
	}
	@Override
	public String getLanguage() {
		return "python2";
	}
	@Override
	public String getDefaultScriptEngineName() {
		return "jython";
	}
	
}