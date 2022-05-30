package com.sos.js7.converter.autosys.common.v12.job.attr;

import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Conditions;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.annotation.ArgumentSetter;

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
    private SOSArgument<List<Object>> condition = new SOSArgument<>(ATTR_CONDITION, false);
    private String originalCondition;

    public SOSArgument<List<Object>> getCondition() {
        return condition;
    }

    @ArgumentSetter(name = ATTR_CONDITION)
    public void setCondition(String val) throws Exception {
        String v = JS7ConverterHelper.stringValue(val);
        originalCondition = val;
        condition.setValue(SOSString.isEmpty(v) ? null : Conditions.parse(v));
    }

    public String getOriginalCondition() {
        return originalCondition;
    }

}
