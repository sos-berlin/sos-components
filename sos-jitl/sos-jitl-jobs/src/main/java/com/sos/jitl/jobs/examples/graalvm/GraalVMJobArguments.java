package com.sos.jitl.jobs.examples.graalvm;

import java.nio.file.Path;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class GraalVMJobArguments extends JobArguments {

    private JobArgument<String> scriptOnStart = new JobArgument<>("script_on_start", false, DisplayMode.NONE);
    private JobArgument<Path> scriptFileOnStart = new JobArgument<>("script_file_on_start", false);

    private JobArgument<String> scriptOnStop = new JobArgument<>("script_on_stop", false, DisplayMode.NONE);
    private JobArgument<Path> scriptFileOnStop = new JobArgument<>("script_file_on_stop", false);

    private JobArgument<String> scriptOnOrderProcess = new JobArgument<>("script_on_order_process", false, DisplayMode.NONE);
    private JobArgument<Path> scriptFileOnOrderProcess = new JobArgument<>("script_file_on_order_process", false);

    public GraalVMJobArguments() {
        super(new SOSCredentialStoreArguments());
    }

    public JobArgument<String> getScriptOnStart() {
        return scriptOnStart;
    }

    public JobArgument<Path> getScriptFileOnStart() {
        return scriptFileOnStart;
    }

    public JobArgument<String> getScriptOnStop() {
        return scriptOnStop;
    }

    public JobArgument<Path> getScriptFileOnStop() {
        return scriptFileOnStop;
    }

    public JobArgument<String> getScriptOnOrderProcess() {
        return scriptOnOrderProcess;
    }

    public JobArgument<Path> getScriptFileOnOrderProcess() {
        return scriptFileOnOrderProcess;
    }
}
