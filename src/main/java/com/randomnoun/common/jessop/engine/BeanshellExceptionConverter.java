package com.randomnoun.common.jessop.engine;

import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;

import com.randomnoun.common.jessop.JessopExceptionConverter;

public class BeanshellExceptionConverter implements JessopExceptionConverter {

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
