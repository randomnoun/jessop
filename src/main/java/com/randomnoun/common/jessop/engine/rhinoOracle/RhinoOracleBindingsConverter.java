package com.randomnoun.common.jessop.engine.rhinoOracle;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

import com.randomnoun.common.jessop.JessopBindingsConverter;

// exactly the same as the other rhino BindingsConverters except for this import statement
import sun.org.mozilla.javascript.internal.*;

public class RhinoOracleBindingsConverter implements JessopBindingsConverter {

	@SuppressWarnings("restriction")
	@Override
	public Bindings toScriptBindings(ScriptEngine engine,
			ScriptContext newContext, Bindings bindings, int engineScope) {
		Context cx = Context.enter();
		try {
			// so hopefully initStandardObjects() is less expensive than cloning all these maps/lists
			// this will be a different scope to that used at runtime, which hopefully won't be a problem later
			ScriptableObject newScope = cx.initStandardObjects();
			Bindings newBindings = engine.createBindings();
			for (String k : bindings.keySet()) {
				Object v = bindings.get(k);
				newBindings.put(k, ScriptUtils.javaToJS(v, newScope)); 
				// newBindings.put(k, toScriptObject(newScope, v));
			}
			return newBindings;
		} finally {
			Context.exit();
		}
	}
	

	// old code (clones all Map and List entries as NativeObjects 
	/*
	@SuppressWarnings({ "restriction", "rawtypes" })
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
	*/
	
	

}
