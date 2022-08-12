package com.sos.jitl.jobs.fileordervariablesjob;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class FileOrderVariablesJobArguments extends JobArguments {

    private JobArgument<String> js7SourceFile = new JobArgument<String>("js7_source_file", false);

    public FileOrderVariablesJobArguments() {
        super(new SOSCredentialStoreArguments());
    }

    public String getJs7SourceFile() {
        return js7SourceFile.getValue();
    }

    public void setJs7SourceFile(String js7SourceFile) {
        this.js7SourceFile.setValue(js7SourceFile);
    }
}
