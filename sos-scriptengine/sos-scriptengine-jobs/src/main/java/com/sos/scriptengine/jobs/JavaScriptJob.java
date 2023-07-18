package com.sos.scriptengine.jobs;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.Bindings;
import javax.script.Invocable;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.sos.commons.job.ABlockingInternalJob;
import com.sos.commons.job.JobArgument;
import com.sos.commons.job.JobArguments;
import com.sos.commons.job.OrderProcessStep;
import com.sos.commons.job.exception.JobArgumentException;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;

public class JavaScriptJob extends ABlockingInternalJob<JobArguments> {

    private static final String SCRIPT_ENGINE_NAME = "Graal.js";

    private static final String FUNCTION_NAME_GET_JOB = "getJS7Job";
    private static final String JOB_METHOD_GET_DECLARED_ARGUMENTS = "getDeclaredArguments";
    private static final String JOB_METHOD_PROCESS_ORDER = "processOrder";

    private static volatile String BASIC_SCRIPT;

    private String script;
    private JobArguments declaredArguments;

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
        setBasicScript();

        ScriptEngine engine = createScriptEngine();
        engine.eval(BASIC_SCRIPT + "\n" + script);
        Invocable invocable = (Invocable) engine;

        Object job = invocable.invokeFunction(FUNCTION_NAME_GET_JOB, getJobEnvironment());
        declaredArguments = getDeclaredArguments(invocable.invokeMethod(job, JOB_METHOD_GET_DECLARED_ARGUMENTS));
    }

    @Override
    public JobArguments onCreateJobArguments(List<JobArgumentException> exceptions, final OrderProcessStep<JobArguments> step) {
        return declaredArguments;
    }

    @Override
    public void onOrderProcess(OrderProcessStep<JobArguments> step) throws Exception {
        ScriptEngine engine = createScriptEngine();
        engine.eval(BASIC_SCRIPT + "\n" + script);
        Invocable invocable = (Invocable) engine;

        Object job = invocable.invokeFunction(FUNCTION_NAME_GET_JOB, getJobEnvironment());
        invocable.invokeMethod(job, JOB_METHOD_PROCESS_ORDER, step);
    }

    /** com.sos.commons.job.ABlockingInternalJob - [cancel/kill][job name=javascript_job][onOrderProcessCancel]<br/>
     * java.lang.IllegalStateException: <br/>
     * Multi threaded access requested by thread Thread[#46,JS7 blocking job 46,5,main] but is not allowed for language(s) js.<br/>
     * at com.oracle.truffle.polyglot.PolyglotEngineException.illegalState(PolyglotEngineException.java:135) ~[org.graalvm.truffle:?]<br>
     * ... */
    // @Override
    // public void onOrderProcessCancel(OrderProcessStep<JobArguments> step) throws Exception {

    // }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private JobArguments getDeclaredArguments(Object args) throws Exception {
        JobArguments jas = new JobArguments();
        if (args != null) {
            Map<String, Object> m = (Map) args;
            List<JobArgument> l = new ArrayList<>();
            m.entrySet().stream().forEach(e -> {
                Map<String, Object> v = (Map) e.getValue();
                Object required = v.get("required");
                Object defaultValue = v.get("defaultValue");
                Object displayMode = v.get("displayMode");

                JobArgument<String> ja = new JobArgument<>(e.getKey(), Boolean.parseBoolean(required.toString()));
                if (defaultValue != null) {
                    ja.setDefaultValue(defaultValue.toString());
                }
                if (displayMode != null) {
                    ja.setDisplayMode(DisplayMode.valueOf(displayMode.toString()));
                }
                ja.setIsDirty(false);
                l.add(ja);
            });
            jas.setDynamicArgumentFields(l);
        }
        return jas;
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

    private synchronized void setBasicScript() throws Exception {
        if (BASIC_SCRIPT == null) {
            BASIC_SCRIPT = inputStreamToString(this.getClass().getClassLoader().getResourceAsStream(this.getClass().getSimpleName() + ".js"));
        }
    }

    public String inputStreamToString(InputStream inputStream) throws IOException {
        try (ByteArrayOutputStream result = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            return result.toString("UTF-8");
        }
    }

}
