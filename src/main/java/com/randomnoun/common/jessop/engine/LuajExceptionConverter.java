package com.randomnoun.common.jessop.engine;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import org.luaj.vm2.LuaError;

import com.randomnoun.common.jessop.JessopExceptionConverter;

public class LuajExceptionConverter implements JessopExceptionConverter {

	/* because lua's special */ 
	@Override
	public ScriptException toScriptException(ScriptContext scriptContext, Throwable t) {
		if (t instanceof ScriptException) { 
			return (ScriptException) t;
		} else if (t instanceof LuaError) {
			// there's a 'fileline' attribute in the LuaError, which isn't public, but is
			// set in the LuaClosure class to
			// le.fileline = (p.source != null? p.source.tojstring(): "?") + ":" 
			//  + (p.lineinfo != null && pc >= 0 && pc < p.lineinfo.length? String.valueOf(p.lineinfo[pc]): "?");
			// it's prepended to the error message
			
			LuaError le = (LuaError) t;
			String msg = le.getMessage();
			Pattern p = Pattern.compile("^(.*):([0-9+]|\\?) (.*)$", Pattern.DOTALL);
			Matcher m = p.matcher(msg);
			if (m.matches()) {
				// just to make things even more annoying, the
				//   public CompiledScript compile(Reader script) throws ScriptException
				// method in LuaScriptEngine hardcodes the name 'script' as the script filename
				// String filename = m.group(1);
				String filename = (String) scriptContext.getAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE);
				return (ScriptException) new ScriptException(m.group(3), filename, 
					m.group(2).equals("?") ? -1 : Integer.parseInt(m.group(2))).initCause(t);
			} 
		}
		return (ScriptException) new ScriptException(t.getMessage()).initCause(t);
	}

}
