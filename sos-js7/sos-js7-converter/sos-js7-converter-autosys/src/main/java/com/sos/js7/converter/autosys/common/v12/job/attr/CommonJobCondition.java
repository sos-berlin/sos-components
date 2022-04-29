package com.sos.js7.converter.autosys.common.v12.job.attr;

import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.attr.annotation.JobAttributeSetter;

public class CommonJobCondition extends AJobAttributes {

    private static final String ATTR_CONDITION = "condition";

    /** condition - Define Starting Conditions for a Job Contents<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: condition: [(]condition[)][(AND|OR)[(]condition[)]] condition: [(]condition,look_back[)][(AND|OR)[(]condition,look_back[)]]<br/>
     * Example: condition: (success(JobA) AND success(JobB)) OR (done(JobD) AND done(JobE))<br/>
     * <br/>
     * JS7 - 100% - Notice Board<br/>
     */
    private SOSArgument<String> condition = new SOSArgument<>(ATTR_CONDITION, false);

    public SOSArgument<String> getCondition() {
        return condition;
    }

    @JobAttributeSetter(name = ATTR_CONDITION)
    public void setCondition(String val) {
        condition.setValue(AJobAttributes.stringValue(val));
    }

}
