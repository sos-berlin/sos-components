package com.sos.js7.converter.autosys.output.js7.helper.beans;

import com.sos.js7.converter.autosys.common.v12.job.ACommonJob;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;

public class Job2Condition {

    private ACommonJob job;
    private Condition condition;

    public Job2Condition(ACommonJob job, Condition condition) {
        this.job = job;
        this.condition = condition;
    }

    public ACommonJob getJob() {
        return job;
    }

    public Condition getCondition() {
        return condition;
    }

    @Override
    public boolean equals(Object o) {
        if (o != null && o instanceof Job2Condition) {
            Job2Condition other = (Job2Condition) o;
            return job.equals(other.job) && condition.equals(other.condition);
        }

        return false;
    }
}