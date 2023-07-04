package com.sos.jitl.jobs.examples.graalvm;

import java.nio.file.Path;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.util.common.SOSArgumentHelper.DisplayMode;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class GraalVMJobArguments extends JobArguments {

    private JobArgument<String> script = new JobArgument<>("script", false, DisplayMode.MASKED);
    private JobArgument<Path> scriptFile = new JobArgument<>("script_file", false);

    public GraalVMJobArguments() {
        super(new SOSCredentialStoreArguments());
    }

    public JobArgument<String> getScript() {
        return script;
    }

    public JobArgument<Path> getScriptFile() {
        return scriptFile;
    }
}
