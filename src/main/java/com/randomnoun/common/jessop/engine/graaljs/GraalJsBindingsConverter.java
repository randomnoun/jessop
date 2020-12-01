package com.randomnoun.common.jessop.engine.graaljs;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;

// import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyObject;

import com.randomnoun.common.jessop.JessopBindingsConverter;

public class GraalJsBindingsConverter implements JessopBindingsConverter {

	@Override
	public Bindings toScriptBindings(ScriptEngine engine,
			ScriptContext newContext, Bindings bindings, int engineScope) {

		// return the same bindings and add a few graaljs-specific properties
		// see https://github.com/graalvm/graaljs/blob/master/docs/user/ScriptEngine.md
		// bindings.put("polyglot.js.allowAllAccess", true);
		// return bindings;
		
		Bindings newBindings = engine.createBindings();
		bindings.put("polyglot.js.allowAllAccess", true);
		for (String k : bindings.keySet()) {
			Object v = bindings.get(k);
			newBindings.put(k, toScriptObject(v)); 
		}
		return newBindings;
	}
	
	// wrap Maps and Lists in ProxyObject
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object toScriptObject(Object o) {
		Object result;
		if (o instanceof Map) {
			Map javaMap = (Map) o;
			Map jsMap = new HashMap<String, Object>();
			
			for (Object entryObj : javaMap.entrySet()) {
				Map.Entry entry = (Map.Entry) entryObj;
				jsMap.put((String) entry.getKey(), toScriptObject(entry.getValue()));
			}
			result = ProxyObject.fromMap(jsMap);
		} else if (o instanceof List) {
			return o;
			
			/* looks like we don't need to wrap these 
			List javaList = (List) o;
			Object[] newArray = new Object[javaList.size()]; 
			for (int i=0; i< javaList.size(); i++) {
				newArray[i] = toScriptObject(javaList.get(i));
			}
			// NativeArray jsArray = new NativeArray(newArray);
			result = Value.asValue(newArray);
			*/

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
