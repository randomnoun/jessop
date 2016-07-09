package com.randomnoun.common.jessop.engine;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.randomnoun.common.jessop.JessopExceptionConverter;

public class BeanshellExceptionConverter implements JessopExceptionConverter {

    /** A revision marker to be used in exception stack traces. */
    public static final String _revision = "$Id$";

	/* bsh doesn't return filenames properly */ 
	@Override
	public ScriptException toScriptException(ScriptContext scriptContext, Throwable t) {
		if (t instanceof ScriptException) { 
			ScriptException se = (ScriptException) t;
			String filename = (String) scriptContext.getAttribute(ScriptEngine.FILENAME, ScriptContext.ENGINE_SCOPE);
			return (ScriptException) new ScriptException(se.getMessage(), 
			  filename, se.getLineNumber()).initCause(t);
		}
		return (ScriptException) new ScriptException(t.getMessage()).initCause(t);
	}

}
