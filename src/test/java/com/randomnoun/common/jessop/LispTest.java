package com.randomnoun.common.jessop;
import javax.script.*;


// adapted from JSR223Example in the abcl distribution

public class LispTest {

  public static void main(String[] args) {
      //Script Engine instantiation using ServiceProvider - this will
      //look in the classpath for a file
      //  /META-INF/services/javax.script.ScriptEngineFactory
      //where the AbclScriptEngineFactory is registered
	      ScriptEngine lispEngine = new ScriptEngineManager().getEngineByExtension("lisp");
	
	      //Alternatively, you can directly instantiate the script engine:
	     
	      //ScriptEngineManager scriptManager = new ScriptEngineManager();
	      //scriptManager.registerEngineExtension("lisp", new AbclScriptEngineFactory());
	      //ScriptEngine lispEngine = scriptManager.getEngineByExtension("lisp");
	
	      //(thanks to Peter Tsenter for suggesting this)
	       
	    //Accessing variables
	    System.out.println();
	    System.out.println("*package* = " + lispEngine.get("*package*"));
	    Object someValue = new Object();
	    lispEngine.put("someVariable", someValue);
	    System.out.println("someVariable = " + lispEngine.get("someVariable"));
	    try {
	      // simple loop in lisp
	      // https://www.tutorialspoint.com/lisp/lisp_loops.htm
          lispEngine.eval(
        	"(loop for a from 10 to 20\n" +
	        "do (print a)\n" +
        	")");

          lispEngine.eval(
	      	"(loop for a from 10 to 20\n" +
	        "do (format t \"~a\" a )\n" +
	      	"(format t \"!\")\n" +  // also in the loop
        	")");
        		  
	    	
	      // simple condition in lisp
	      // lispEngine.eval("however that works");
          lispEngine.eval(
        	"(setq a 100)\n" +
			"(if (> a 20)\n" +
			"   (format t \"~% a is greater than 20\")\n" + 
			"   (format t \"~% a is less than 20\"))\n" +
			"(format t \"~% value of a is ~d \" a)\n");

          // platform dependent
          lispEngine.eval("(format t \"line 1 ending with lisp newline ~%\")\n");
          System.out.println("===");
          // syntax from https://common-lisp.net/project/armedbear/releases/1.3.3/abcl-1.3.3.pdf, p41, doesn't work
          lispEngine.eval("(format t \"line 2 ending with inline unicode D #\\U000D\")\n");
          System.out.println("===");
          lispEngine.eval("(format t \"line 3 ending with inline unicode D A #\\U000D#\\U000A\")\n");
          System.out.println("===");
          lispEngine.eval("(format t \"line 3b without the hash D A \\U000D\\U000A\")\n");
          // syntax from https://rosettacode.org/wiki/Terminal_control/Unicode_output
          System.out.println("===");
          lispEngine.eval("(format t \"line 4 ending with code-chars D A ~a~a\" (code-char #x000d) (code-char #x000a))\n");
          System.out.println("===");
          // this seems to work ok
          lispEngine.eval("(format t \"line 5 ending with unicode chars D A ~a~a\" '#\\U000D '#\\U000A)\n");
          System.out.println("===");
          lispEngine.eval("(format t \"and\")(format t \"so\")");
          System.out.println("===");
          lispEngine.eval("(princ \"this\" *standard-output*)(princ \"that\" *standard-output*)");
          System.out.println("===");
          		
          
          // need to use ~~ to output a '~'
          // probably some weirdness require to output a '#' as well
          // so this'll probably be format out \"whatever\" in jessop then
          // don't like the ~a syntax, would prefer to keep chars inline
          
          
          
          lispEngine.eval("(format t \"line 4 with \\\" embedded quote ~%\")\n");
	      
	      
	    	
	      //Interpretation (also from streams)
	      lispEngine.eval("(defun hello (arg) (print (list arg someVariable)) (terpri))");
	     
	      //Direct function invocation
	      ((Invocable) lispEngine).invokeFunction("hello", "world");
	     
	      //Implementing a Java interface in Lisp
	      lispEngine.eval("(defun compare-to (&rest args) 42)");
	      Comparable c = ((Invocable) lispEngine).getInterface(java.lang.Comparable.class);
	      System.out.println("compareTo: " + c.compareTo(null));
	     
	      //Compilation!
	      lispEngine.eval("(defmacro slow-compiling-macro (arg) (dotimes (i 1000000) (incf i)) `(print ,arg))");
	     
	      long millis = System.currentTimeMillis();
	      lispEngine.eval("(slow-compiling-macro 42)");
	      millis = System.currentTimeMillis() - millis;
	      System.out.println("interpretation took " + millis);
	     
	      millis = System.currentTimeMillis();
	      CompiledScript cs = ((Compilable) lispEngine).compile("(slow-compiling-macro 42)");
	      millis = System.currentTimeMillis() - millis;
	      System.out.println("compilation took " + millis);
	     
	      millis = System.currentTimeMillis();
	      cs.eval();
	      millis = System.currentTimeMillis() - millis;
	      System.out.println("evaluation took " + millis);
	
	      millis = System.currentTimeMillis();
	      cs.eval();
	      millis = System.currentTimeMillis() - millis;
	      System.out.println("evaluation took " + millis);
	
	      //Ecc. ecc.
	    } catch (NoSuchMethodException e) {
	      e.printStackTrace();
	    } catch (ScriptException e) {
	      e.printStackTrace();
	    }
	  }
	 
	}