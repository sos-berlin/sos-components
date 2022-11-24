package com.sos.js7.converter.js1.output.js7.helper;

import java.util.List;

import com.sos.js7.converter.js1.common.jobstreams.condition.Condition;
import com.sos.js7.converter.js1.common.jobstreams.condition.Conditions;

public class JobStreamConditionsHelper {

    private final JobStreamJS1JS7Job js1js7Job;
    private final Conditions conditions;
    private final List<Condition> conditionParts;

    public JobStreamConditionsHelper(JobStreamJS1JS7Job js1js7Job, Conditions conditions, List<Condition> conditionParts) {
        this.js1js7Job = js1js7Job;
        this.conditions = conditions;
        this.conditionParts = conditionParts;
    }

    public JobStreamJS1JS7Job getJS1JS7Job() {
        return js1js7Job;
    }

    public Conditions getConditions() {
        return conditions;
    }

    public List<Condition> getConditionParts() {
        return conditionParts;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("js1Job=").append(js1js7Job.getJS1Job().getName());
        sb.append(",conditionParts=").append(conditionParts);

        return sb.toString();
    }

}
