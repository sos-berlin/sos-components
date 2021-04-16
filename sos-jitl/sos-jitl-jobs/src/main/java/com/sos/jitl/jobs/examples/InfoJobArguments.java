package com.sos.jitl.jobs.examples;

import com.sos.jitl.jobs.common.JobArgument;

public class InfoJobArguments {

    private JobArgument<String> infoResult = new JobArgument<String>("info_result", "my_default_info_result");

    public String getInfoResult() {
        return infoResult.getValue();
    }

    public void setInfoResult(String val) {
        infoResult.setValue(val);
    }

}
