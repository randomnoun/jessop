package com.randomnoun.common.jessop.engine.graaljs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

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
		bindings.put("polyglot.js.allowAllAccess", true);
		
		// return bindings;
		// Bindings newBindings = engine.createBindings();
		// bindings.put("polyglot.js.allowAllAccess", true);
		// this fails with 'failed to set graal-js option "polyglot.js.allowAllAccess": js context is already initialized'
		
		Set<String> keySet = new TreeSet<String>(bindings.keySet());
		for (String k : keySet) {
			Object v = bindings.get(k);
			bindings.put(k, toScriptObject(v)); 
		}
		return bindings;
	}
	
	// wrap Maps and Lists in ProxyObject
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public Object toScriptObject(Object o) {
		Object result;
		
		if (o == null) {
			return null;
		
		} else if (o instanceof Map) {
			Map javaMap = (Map) o;
			Map jsMap = new HashMap<String, Object>();
			
			for (Object entryObj : javaMap.entrySet()) {
				Map.Entry entry = (Map.Entry) entryObj;
				jsMap.put((String) entry.getKey(), toScriptObject(entry.getValue()));
			}
			result = ProxyObject.fromMap(jsMap);

		} else if (o instanceof List) {
			// return o;
			List javaList = (List) o;
			List newList = new ArrayList(javaList.size()); 
			for (int i=0; i < javaList.size(); i++) {
				newList.add(toScriptObject(javaList.get(i)));
			}
			result = newList;

		// possibly array types here
		} else if (o instanceof String) { 
			result = o;
		
		} else {
			result = o; 
		}
		return result;
	}

	
	

}
