package com.randomnoun.common.jessop.engine.jvmRhino;

import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import com.randomnoun.common.jessop.JessopBindingsConverter;

import sun.org.mozilla.javascript.*;

public class JvmRhinoBindingsConverter implements JessopBindingsConverter {

	@Override
	public Bindings toScriptBindings(ScriptEngine engine,
			ScriptContext newContext, Bindings bindings, int engineScope) {
		// RhinoScriptEngine rse = (RhinoScriptEngine) engine;
		// arg. getRuntimeScope is package private.
				
		// arg. indexedProps are private, and ExternalScriptable is package private
		// Scriptable newScope = new ExternalScriptable(newContext, null);
		
		// ok then.
		Bindings newBindings = engine.createBindings();
		for (String k : bindings.keySet()) {
			Object v = bindings.get(k);
			newBindings.put(k, toScriptObject(v));
		}
		return newBindings;
		
		
	}
	
	
	// would prefer to have a custom NativeObject / NativeArray
	// 'backed' by the original Map / List, but this'll do for now
	// (most backing impls seem to require a Scriptable object here, which we don't have access to)
	private Object toScriptObject(Object o) {
		Object result;
		if (o instanceof Map) {
			Map javaMap = (Map) o;
			NativeObject jsMap = new NativeObject();
			for (Object entryObj : javaMap.entrySet()) {
				Map.Entry entry = (Map.Entry) entryObj;
			    jsMap.defineProperty(
			        (String) entry.getKey(), toScriptObject(entry.getValue()), NativeObject.READONLY
			    );
			}
			result = jsMap;
		} else if (o instanceof List) {
			List javaList = (List) o;
			Object[] newArray = new Object[javaList.size()]; 
			for (int i=0; i< javaList.size(); i++) {
				newArray[i] = toScriptObject(javaList.get(i));
			}
			NativeArray jsArray = new NativeArray(newArray);
			result = jsArray;

		// possibly array types here
		} else if (o instanceof String) { 
			// result = new NativeString((String) o); // private constructor
			result = o;
			
			//Context context = Context.getCurrentContext();
			//Scriptable scope = ScriptRuntime.getTopCallScope( context );
			//return context.newObject( scope, "String", new Object[] { o } );
		
		} else {
			result = o; 
		}
		return result;
	}
	
	
	

}
