package com.randomnoun.common.jessop;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html ) 
 */

/** Convert Java bindings to more scripting-language-friendly objects.
 *  
 * @author knoxg
 */
public interface JessopBindingsConverter {

	/** Convert a set of bindings to a more scripting-language-friendly object.
	 * 
	 * <p>This is called twice, once for bindings at ScriptContext.GLOBAL_SCOPE, 
	 * and again for bindings at ScriptContext.ENGINE_SCOPE.
	 */
	Bindings toScriptBindings(ScriptEngine engine, ScriptContext newContext,
		Bindings bindings, int engineScope);

	// may add a toJavaObject() to do the reverse later on
	
}
