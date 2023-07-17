package com.sos.scriptengine.jobs;

import java.io.IOException;
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
import com.sos.commons.util.SOSString;

public class JavaScriptJob extends ABlockingInternalJob<JobArguments> {

    private static final String SCRIPT_ENGINE_NAME = "Graal.js";

    private static final String FUNCTION_NAME_GET_JOB = "getJS7Job";
    private static final String JOB_CLASS_NAME = "JS7Job";
    private static final String JOB_METHOD_NAME_ON_START = "onStart";
    private static final String JOB_METHOD_NAME_ON_STOP = "onStop";
    private static final String JOB_METHOD_NAME_ON_ORDER_PROCESS = "onOrderProcess";

    private static final String BASIC_SCRIPT = "function " + FUNCTION_NAME_GET_JOB + "(jobEnvironment){ return new " + JOB_CLASS_NAME
            + "(jobEnvironment);}" + " class ABlockingJob { #jobEnvironment; "
            + "constructor(jobEnvironment){ this.#jobEnvironment=jobEnvironment; } " + JOB_METHOD_NAME_ON_START + "(){} " + JOB_METHOD_NAME_ON_STOP
            + "(){} " + JOB_METHOD_NAME_ON_ORDER_PROCESS + "(_step){} getJobEnvironment(){return this.#jobEnvironment;}}";

    private Invocable invocable = null;
    private Object job;
    private String script;

    public JavaScriptJob(JobContext jobContext) {
        super(jobContext);
        if (jobContext != null) {
            script = jobContext.asScala().executable().script();
            if (SOSString.isEmpty(script)) {// TO REMOVE
                try {
                    String s = jobContext.jobArguments().get("script").convertToString();
                    script = SOSPath.readFile(Paths.get(s));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onStart() throws Exception {
        ScriptEngine engine = createScriptEngine();
        engine.eval(BASIC_SCRIPT + "\n" + script);
        script = null;

        invocable = (Invocable) engine;
        // job is a PolyglotMap and can't be cast to Invocable
        job = invocable.invokeFunction(FUNCTION_NAME_GET_JOB, getJobEnvironment());
        invocable.invokeMethod(job, JOB_METHOD_NAME_ON_START);
    }

    @Override
    public void onStop() throws Exception {
        if (canInvoke()) {
            invocable.invokeMethod(job, JOB_METHOD_NAME_ON_STOP);
        }
    }

    @Override
    public void onOrderProcess(OrderProcessStep<JobArguments> step) throws Exception {
        invocable.invokeMethod(job, JOB_METHOD_NAME_ON_ORDER_PROCESS, step);
    }

    /** com.sos.commons.job.ABlockingInternalJob - [cancel/kill][job name=javascript_job][onOrderProcessCancel]<br/>
     * java.lang.IllegalStateException: <br/>
     * Multi threaded access requested by thread Thread[#46,JS7 blocking job 46,5,main] but is not allowed for language(s) js.<br/>
     * at com.oracle.truffle.polyglot.PolyglotEngineException.illegalState(PolyglotEngineException.java:135) ~[org.graalvm.truffle:?]<br>
     * ... */
    // @Override
    // public void onOrderProcessCancel(OrderProcessStep<JobArguments> step) throws Exception {

    // }

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

}
