package com.randomnoun.common.jessop;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html )
 */

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import junit.framework.TestCase;

/**
 * Test to verify that GraalJS ScriptEngine instances release memory when closed,
 * and to check whether ScriptEngineManager shares underlying engines.
 *
 * <p>Run with: -Xmx512m to make OOM failures visible sooner.
 */
public class GraalvmMemoryLeakTest extends TestCase {

    /**
     * Tests that creating and closing GraalJS engines does not leak memory.
     * If close() properly releases Truffle internals, heap usage should remain stable.
     * If it leaks, this test will eventually OOM or show unbounded growth.
     */
    public void testEngineCloseReleasesMemory() throws ScriptException {
        long baselineMemory = getUsedMemory();
        System.out.println("Baseline memory: " + (baselineMemory / 1024 / 1024) + " MB");

        int iterations = 200;
        for (int i = 0; i < iterations; i++) {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("graal-js");
            assertNotNull("graal-js engine not found", engine);
            engine.put("polyglot.js.allowAllAccess", true);
            engine.eval("var x = {a: 1, b: 'hello', c: [1,2,3]}; x.a + x.c.length;");

            if (engine instanceof AutoCloseable) {
                try {
                    ((AutoCloseable) engine).close();
                } catch (Exception e) {
                    fail("Failed to close engine: " + e.getMessage());
                }
            } else {
                fail("GraalJSScriptEngine does not implement AutoCloseable");
            }

            if (i % 50 == 49) {
                System.gc();
                long currentMemory = getUsedMemory();
                System.out.println("After " + (i + 1) + " iterations (with close): " +
                    (currentMemory / 1024 / 1024) + " MB (delta: +" +
                    ((currentMemory - baselineMemory) / 1024 / 1024) + " MB)");
            }
        }

        System.gc();
        long finalMemory = getUsedMemory();
        long delta = finalMemory - baselineMemory;
        System.out.println("Final memory delta (with close): " + (delta / 1024 / 1024) + " MB");

        // If close works properly, delta should be modest (under 200MB for 200 iterations)
        assertTrue("Memory grew by " + (delta / 1024 / 1024) + " MB after " + iterations +
            " closed engines - close() may not be releasing Truffle internals",
            delta < 200 * 1024 * 1024);
    }

    /**
     * Tests that creating engines WITHOUT closing leaks memory.
     * This is the control test - it should show unbounded growth.
     */
    public void testEngineWithoutCloseLeaksMemory() throws ScriptException {
        long baselineMemory = getUsedMemory();
        System.out.println("Baseline memory (no-close test): " + (baselineMemory / 1024 / 1024) + " MB");

        int iterations = 200;
        for (int i = 0; i < iterations; i++) {
            ScriptEngine engine = new ScriptEngineManager().getEngineByName("graal-js");
            assertNotNull("graal-js engine not found", engine);
            engine.put("polyglot.js.allowAllAccess", true);
            engine.eval("var x = {a: 1, b: 'hello', c: [1,2,3]}; x.a + x.c.length;");
            // Deliberately NOT closing the engine

            if (i % 50 == 49) {
                System.gc();
                long currentMemory = getUsedMemory();
                System.out.println("After " + (i + 1) + " iterations (no close): " +
                    (currentMemory / 1024 / 1024) + " MB (delta: +" +
                    ((currentMemory - baselineMemory) / 1024 / 1024) + " MB)");
            }
        }

        System.gc();
        long finalMemory = getUsedMemory();
        long delta = finalMemory - baselineMemory;
        System.out.println("Final memory delta (no close): " + (delta / 1024 / 1024) + " MB");

        // This test documents the leak - we expect growth without close
        System.out.println("NOTE: Growth of " + (delta / 1024 / 1024) +
            " MB confirms that unclosed engines leak memory");
    }

    /**
     * Tests whether ScriptEngineManager returns the same engine instance or new ones.
     * If it reuses engines, closing one might affect others.
     */
    public void testEngineIdentity() {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine1 = mgr.getEngineByName("graal-js");
        ScriptEngine engine2 = mgr.getEngineByName("graal-js");

        System.out.println("engine1: " + System.identityHashCode(engine1) + " (" + engine1.getClass().getName() + ")");
        System.out.println("engine2: " + System.identityHashCode(engine2) + " (" + engine2.getClass().getName() + ")");
        System.out.println("Same instance: " + (engine1 == engine2));

        // Document whether instances are shared
        if (engine1 == engine2) {
            System.out.println("WARNING: ScriptEngineManager reuses engine instances - closing one would break others");
        } else {
            System.out.println("OK: ScriptEngineManager creates new engine instances each time");
        }
    }

    /**
     * Tests whether separately-created engines share an underlying polyglot Engine.
     * Even if ScriptEngine instances differ, they may share a polyglot Engine
     * which holds the code cache and shape metadata.
     */
    public void testSharedPolyglotEngine() throws Exception {
        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine engine1 = mgr.getEngineByName("graal-js");
        ScriptEngine engine2 = mgr.getEngineByName("graal-js");

        // Use reflection to check if they share the same polyglot Engine
        try {
            java.lang.reflect.Method getPolyglotContext = engine1.getClass().getMethod("getPolyglotContext");
            Object ctx1 = getPolyglotContext.invoke(engine1);
            Object ctx2 = getPolyglotContext.invoke(engine2);

            java.lang.reflect.Method getEngine = ctx1.getClass().getMethod("getEngine");
            Object polyEngine1 = getEngine.invoke(ctx1);
            Object polyEngine2 = getEngine.invoke(ctx2);

            System.out.println("Polyglot Engine 1: " + System.identityHashCode(polyEngine1));
            System.out.println("Polyglot Engine 2: " + System.identityHashCode(polyEngine2));
            System.out.println("Shared polyglot Engine: " + (polyEngine1 == polyEngine2));

            if (polyEngine1 == polyEngine2) {
                System.out.println("IMPORTANT: Engines share a polyglot Engine - " +
                    "compiled code and shapes accumulate in the shared Engine even after Context.close()");
            } else {
                System.out.println("OK: Each ScriptEngine has its own polyglot Engine - " +
                    "close() should fully release resources");
            }
        } catch (NoSuchMethodException e) {
            System.out.println("Could not reflectively access polyglot context - " + e.getMessage());
        }
    }

    private long getUsedMemory() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }
}
