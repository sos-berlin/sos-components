package com.sos.scriptengine.jobs;

import java.nio.file.Paths;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.sos.commons.job.ABlockingInternalJob;
import com.sos.commons.job.JobArguments;
import com.sos.commons.job.OrderProcessStep;
import com.sos.commons.util.SOSPath;

public class JavaScriptJob extends ABlockingInternalJob<JobArguments> {

    private static final String SCRIPT_ENGINE_NAME = "Graal.js";
    private static final String FUNCTION_NAME_GET_JOB = "getJob";
    private static final String METHOD_NAME_ON_START = "onStart";
    private static final String METHOD_NAME_ON_STOP = "onStop";
    private static final String METHOD_NAME_ON_ORDER_PROCESS = "onOrderProcess";

    private static final String BASIC_SCRIPT = "function " + FUNCTION_NAME_GET_JOB + "(jobEnvironment){ return new JS7Job(jobEnvironment);}"
            + " class ABlockingJob { #jobEnvironment; " + "constructor(jobEnvironment){this.#jobEnvironment = jobEnvironment;}" + METHOD_NAME_ON_START
            + "(){} " + METHOD_NAME_ON_STOP + "(){} " + METHOD_NAME_ON_ORDER_PROCESS + "(_step){} getJobEnvironment(){return this.#jobEnvironment;}}";

    private Invocable invocable = null;
    private Object job;
    private String script;

    public JavaScriptJob(JobContext jobContext) {
        super(jobContext);
        // script=jobContext.script();
    }

    @Override
    public void onStart() throws Exception {
        ScriptEngine engine = createScriptEngine();
        script = script == null ? getCustomScript() : script; // TODO use script()
        engine.eval(BASIC_SCRIPT + "\n" + script);

        invocable = (Invocable) engine;
        // method not found - throws NoSuchMethodException : <methodName>
        // job is a PolyglotMap and can't be cast to Invocable -
        job = invocable.invokeFunction(FUNCTION_NAME_GET_JOB, getJobEnvironment());
        invocable.invokeMethod(job, METHOD_NAME_ON_START);
    }

    @Override
    public void onStop() throws Exception {
        if (canInvoke()) {
            invocable.invokeMethod(job, METHOD_NAME_ON_STOP);
        }
    }

    @Override
    public void onOrderProcess(OrderProcessStep<JobArguments> step) throws Exception {
        invocable.invokeMethod(job, METHOD_NAME_ON_ORDER_PROCESS, step);
    }

    private ScriptEngine createScriptEngine() throws Exception {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(SCRIPT_ENGINE_NAME);
        if (engine == null) {
            throw new Exception("ScriptEngine " + SCRIPT_ENGINE_NAME + " not found");
        }
        Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
        bindings.put("polyglot.js.allowHostAccess", true);
        bindings.put("polyglot.js.allowHostClassLookup", true);
        return engine;
    }

    private boolean canInvoke() {
        return invocable != null && job != null;
    }

    // TODO to remove
    private String getCustomScript() throws Exception {
        return SOSPath.readFile(Paths.get(getJobEnvironment().getAllArgumentsAsNameValueMap().get("script").toString()));
    }

}
