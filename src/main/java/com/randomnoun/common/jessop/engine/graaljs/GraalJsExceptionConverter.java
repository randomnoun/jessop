package com.randomnoun.common.jessop.engine.graaljs;

import javax.script.ScriptContext;
import javax.script.ScriptException;

import com.randomnoun.common.jessop.JessopExceptionConverter;

public class GraalJsExceptionConverter implements JessopExceptionConverter {

	@Override
	public ScriptException toScriptException(ScriptContext scriptContext, Throwable t) {
		String filename = null;
		Integer lineNumber = null;
		String innerMessage = null;
		if (t instanceof ScriptException) {
			// try to find a stack trace element with "<js>" in it
			Throwable chain = t;
			while (chain != null && filename == null) {
				for (StackTraceElement ste : chain.getStackTrace()) {
					if (ste.getClassName().equals("<js>")) {
						innerMessage = chain.getMessage();
						filename = ste.getFileName();
						lineNumber = ste.getLineNumber();
						break;
					}
				}
				chain = chain.getCause();
			}
			if (filename != null) {
				return (ScriptException) new ScriptException(innerMessage, filename, lineNumber == null ? -1 : lineNumber).initCause(t);
			} else {
				return (ScriptException) t;
			}
		}
		return (ScriptException) new ScriptException(t.getMessage()).initCause(t);
	}

}
