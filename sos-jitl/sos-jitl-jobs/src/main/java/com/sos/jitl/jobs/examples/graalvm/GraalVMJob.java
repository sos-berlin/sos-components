package com.sos.jitl.jobs.examples.graalvm;

import java.nio.charset.Charset;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.OrderProcessStep;

public class GraalVMJob extends ABlockingInternalJob<GraalVMJobArguments> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraalVMJob.class);

    private static final String SCRIPT_ENGINE_NAME = "Graal.js";
    private static final String BINDING_NAME_JOB_ENVIRONMENT = "js7JobEnvironment";
    private static final String BINDING_NAME_STEP = "js7OrderProcessStep";

    public GraalVMJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void onStart() throws Exception {
        String script = getScriptOnStart(getJobEnvironment().getDeclaredArguments());
        if (script != null) {
            ScriptEngine engine = newScriptEngine();
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put(BINDING_NAME_JOB_ENVIRONMENT, getJobEnvironment());
            engine.eval(script);
        }
    }

    @Override
    public void onStop() {
        try {
            String script = getScriptOnStop(getJobEnvironment().getDeclaredArguments());
            if (script != null) {
                ScriptEngine engine = newScriptEngine();
                Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
                bindings.put(BINDING_NAME_JOB_ENVIRONMENT, getJobEnvironment());
                engine.eval(script);
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[onStop]%s", e.toString(), e));
        }
    }

    @Override
    public void onOrderProcess(OrderProcessStep<GraalVMJobArguments> step) throws Exception {
        String script = getScriptOnOrderProcess(step.getDeclaredArguments());
        if (script != null) {
            ScriptEngine engine = newScriptEngine();
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put(BINDING_NAME_JOB_ENVIRONMENT, getJobEnvironment());
            bindings.put(BINDING_NAME_STEP, step);
            engine.eval(script);
        }
    }

    private ScriptEngine newScriptEngine() throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(SCRIPT_ENGINE_NAME);
        if (engine == null) {
            throw new Exception("ScriptEngine " + SCRIPT_ENGINE_NAME + " not found");
        }
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", true);
        return engine;
    }

    private String getScriptOnStart(GraalVMJobArguments args) throws Exception {
        if (args.getScriptFileOnStart().getValue() != null) {
            return SOSPath.readFile(args.getScriptFileOnStart().getValue(), (Charset) null);
        }
        return args.getScriptOnStart().getValue();
    }

    private String getScriptOnStop(GraalVMJobArguments args) throws Exception {
        if (args.getScriptFileOnStop().getValue() != null) {
            return SOSPath.readFile(args.getScriptFileOnStop().getValue(), (Charset) null);
        }
        return args.getScriptOnStop().getValue();
    }

    private String getScriptOnOrderProcess(GraalVMJobArguments args) throws Exception {
        if (args.getScriptFileOnOrderProcess().getValue() != null) {
            return SOSPath.readFile(args.getScriptFileOnOrderProcess().getValue(), (Charset) null);
        }
        return args.getScriptOnOrderProcess().getValue();
    }

}
