package com.randomnoun.common.jessop.engine.graaljs;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import com.randomnoun.common.jessop.JessopBindingsConverter;

public class GraalJsBindingsConverter implements JessopBindingsConverter {

	@Override
	public Bindings toScriptBindings(ScriptEngine engine,
			ScriptContext newContext, Bindings bindings, int engineScope) {

		// return the same bindings and add a few graaljs-specific properties
		// see https://github.com/graalvm/graaljs/blob/master/docs/user/ScriptEngine.md
		bindings.put("polyglot.js.allowAllAccess", true);
		return bindings;

	}
	
	

}
