package com.randomnoun.common.jessop.engine;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

import javax.script.ScriptContext;
import javax.script.ScriptException;

import com.randomnoun.common.jessop.JessopExceptionConverter;

public class JRubyExceptionConverter implements JessopExceptionConverter {

	/* jruby doesn't return filenames or line numbers in the ScriptException object, 
	 * but these are contained in the jruby RaiseException object */
    /* example stacktrace
  NameError: undefined local variable or method `floob' for main:Object
  <top> at test.jessop:6
     */
	@Override
	public ScriptException toScriptException(ScriptContext scriptContext, Throwable t) {
		if (t instanceof ScriptException) { 
			ScriptException se = (ScriptException) t;
			StackTraceElement[] stes = getFirstStackTrace(se.getCause());
			if (stes!=null && stes.length > 0) {
				return (ScriptException) new ScriptException(se.getMessage(), 
					stes[0].getFileName(), stes[0].getLineNumber()).initCause(t);	
			}
		}
		return (ScriptException) new ScriptException(t.getMessage()).initCause(t);
	}
	
	private StackTraceElement[] getFirstStackTrace(Throwable e) {
		if (e instanceof org.jruby.exceptions.RaiseException) {
			return e.getStackTrace();
		}
		if (e.getCause()!=null) { return getFirstStackTrace(e.getCause()); }
		// no RaiseException in stacktrace
		return e.getStackTrace();
	}

}
