package com.sos.jitl.jobs.checklicense;

import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class CheckLicenseJobArguments extends JobArguments {

    private JobArgument<Integer> validityDays  = new JobArgument<>("validity_days ", false,60);

    public Integer getValidityDays() {
        return validityDays.getValue();
    }
}
