package com.randomnoun.common.jessop;

/* (c) 2016 randomnoun. All Rights Reserved. This work is licensed under a
 * BSD Simplified License. ( http://www.randomnoun.com/bsd-simplified.html )
 */

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Properties;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import junit.framework.TestCase;

/**
 * Test to verify that the JessopScriptEngine properly releases memory after evaluation,
 * including the underlying GraalJS polyglot Engine.
 *
 * <p>Run with: -Xmx512m to make OOM failures visible sooner.
 */
public class JessopMemoryLeakTest extends TestCase {

    Logger logger = Logger.getLogger(JessopMemoryLeakTest.class);

    public void setUp() {
        String logFormatPrefix = "[JessopMemoryLeakTest] ";
        Properties lp = new Properties();
        lp.put("log4j.rootCategory", "INFO, CONSOLE");
        lp.put("log4j.appender.CONSOLE", "org.apache.log4j.ConsoleAppender");
        lp.put("log4j.appender.CONSOLE.layout", "org.apache.log4j.PatternLayout");
        lp.put("log4j.appender.CONSOLE.layout.ConversionPattern", logFormatPrefix + "%d{ABSOLUTE} %-5p %c - %m%n");
        PropertyConfigurator.configure(lp);
    }

    /**
     * Tests that evaluating jessop scripts via the JessopScriptEngine does not leak memory.
     * Each eval creates an underlying GraalJS engine with an isolated polyglot Engine;
     * after eval completes, both the Context and Engine should be closed and memory released.
     */
    public void testJessopEvalReleasesMemory() throws ScriptException {
        long baselineMemory = getUsedMemory();
        System.out.println("Baseline memory: " + (baselineMemory / 1024 / 1024) + " MB");

        int iterations = 100;
        for (int i = 0; i < iterations; i++) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
            assertNotNull("jessop engine not found", engine);
            Bindings b = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            b.put("polyglot.js.allowAllAccess", true);

            ScriptContext context = engine.getContext();
            context.setWriter(pw);
            b.put("out", pw);

            // Use a slightly different expression each time to prevent trivial caching
            String script = "<%= 'hello' + ' ' + " + i + " %>";
            engine.eval(script, b);

            String output = sw.toString();
            assertTrue("Expected output containing 'hello', got: " + output,
                output.contains("hello"));

            if (i % 25 == 24) {
                System.gc();
                long currentMemory = getUsedMemory();
                System.out.println("After " + (i + 1) + " iterations: " +
                    (currentMemory / 1024 / 1024) + " MB (delta: +" +
                    ((currentMemory - baselineMemory) / 1024 / 1024) + " MB)");
            }
        }

        System.gc();
        long finalMemory = getUsedMemory();
        long delta = finalMemory - baselineMemory;
        System.out.println("Final memory delta: " + (delta / 1024 / 1024) + " MB after " + iterations + " iterations");

        // With isolated engines, delta should be modest (under 100MB for 100 iterations)
        assertTrue("Memory grew by " + (delta / 1024 / 1024) + " MB after " + iterations +
            " jessop evals - engines may not be releasing memory",
            delta < 100 * 1024 * 1024);
    }

    /**
     * Tests with varied expressions to simulate real-world usage where each report
     * has different dynamic content (which creates different shapes in the Truffle engine).
     */
    public void testJessopEvalVariedExpressionsReleasesMemory() throws ScriptException {
        long baselineMemory = getUsedMemory();
        System.out.println("Baseline memory (varied): " + (baselineMemory / 1024 / 1024) + " MB");

        int iterations = 100;
        for (int i = 0; i < iterations; i++) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            ScriptEngine engine = new ScriptEngineManager().getEngineByName("jessop");
            assertNotNull("jessop engine not found", engine);
            Bindings b = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            b.put("polyglot.js.allowAllAccess", true);

            ScriptContext context = engine.getContext();
            context.setWriter(pw);
            b.put("out", pw);

            String[] scripts = {
                "<%= 'report ' + " + i + " %>",
                "<%= ({name: 'test', value: " + i + "}).name %>",
                "<%= [1,2,3," + i + "].length %>",
                "<%= (function(x) { return x * 2; })(" + i + ") %>",
                "<%= 'item_' + " + i + " + '_end' %>"
            };
            String script = scripts[i % scripts.length];
            engine.eval(script, b);

            if (i % 25 == 24) {
                System.gc();
                long currentMemory = getUsedMemory();
                System.out.println("After " + (i + 1) + " varied iterations: " +
                    (currentMemory / 1024 / 1024) + " MB (delta: +" +
                    ((currentMemory - baselineMemory) / 1024 / 1024) + " MB)");
            }
        }

        System.gc();
        long finalMemory = getUsedMemory();
        long delta = finalMemory - baselineMemory;
        System.out.println("Final memory delta (varied): " + (delta / 1024 / 1024) + " MB after " + iterations + " iterations");

        assertTrue("Memory grew by " + (delta / 1024 / 1024) + " MB after " + iterations +
            " varied jessop evals - shared Engine may still be accumulating shapes",
            delta < 100 * 1024 * 1024);
    }

    private long getUsedMemory() {
        Runtime rt = Runtime.getRuntime();
        return rt.totalMemory() - rt.freeMemory();
    }
}
