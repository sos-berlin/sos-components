package com.sos.jitl.jobs.examples.graalvm;

import java.nio.charset.Charset;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import com.sos.commons.util.SOSPath;
import com.sos.jitl.jobs.common.ABlockingInternalJob;
import com.sos.jitl.jobs.common.JobStep;
import com.sos.jitl.jobs.common.JobStepOutcome;

import js7.data_for_java.order.JOutcome;

public class GraalVMJob extends ABlockingInternalJob<GraalVMJobArguments> {

    public GraalVMJob(JobContext jobContext) {
        super(jobContext);
    }

    @Override
    public void onStart(GraalVMJobArguments args) throws Exception {
    }

    @Override
    public void onStop(GraalVMJobArguments args) {
    }

    @Override
    public JOutcome.Completed onOrderProcess(JobStep<GraalVMJobArguments> step) throws Exception {
        String engineName = "Graal.js";// or JavaScript

        JobStepOutcome outcome = step.newJobStepOutcome();
        ScriptEngine engine = new ScriptEngineManager().getEngineByName(engineName);
        if (engine == null) {
            outcome.setFailed();
            outcome.setMessage("ScriptEngine " + engineName + " not found");
        } else {
            Bindings bindings = engine.getBindings(ScriptContext.ENGINE_SCOPE);
            bindings.put("polyglot.js.allowHostAccess", true);
            bindings.put("polyglot.js.allowHostClassLookup", true);
            bindings.put("step", step);
            bindings.put("outcome", outcome);
            String script = getScript(step.getDeclaredArguments());
            if (script != null) {
                engine.eval(script); // it will not work without allowHostAccess and allowHostClassLookup
            }
        }
        return outcome.isFailed() ? step.failed(outcome) : step.success(outcome);
    }

    private String getScript(GraalVMJobArguments args) throws Exception {
        if (args.getScriptFile().getValue() != null) {
            return SOSPath.readFile(args.getScriptFile().getValue(), (Charset) null);
        }
        return args.getScript().getValue();
    }

}
