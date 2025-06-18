package com.sos.js7.converter.autosys.output.js7.helper;

import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.ExpectNotices;
import com.sos.js7.converter.autosys.common.v12.job.JobBOX;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Conditions;

public class BoardExpectConsumHelper {

    private List<Condition> conditions;
    private String expectNotices;
    private String consumeNotices;

    public String getExpectNotices() {
        return expectNotices;
    }

    public void removeConditionsFromChildrenJobs(JobBOX box) {
        Conditions.removeFromChildrenJobs(box, conditions);
    }

    public ExpectNotices toExpectNotices() {
        String val = tryTrim(expectNotices);
        if (SOSString.isEmpty(val)) {
            return null;
        }
        ExpectNotices n = new ExpectNotices();
        n.setNoticeBoardNames(val);
        return n;
    }

    public ConsumeNotices toConsumeNotices() {
        String val = tryTrim(consumeNotices);
        if (SOSString.isEmpty(val)) {
            return null;
        }
        ConsumeNotices n = new ConsumeNotices();
        n.setNoticeBoardNames(val);
        return n;
    }

    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> val) {
        conditions = val;
    }

    public void setExpectNotices(String val) {
        expectNotices = val;
    }

    public String getConsumeNotices() {
        return consumeNotices;
    }

    public void resetConsumeNotices() {
        setConsumeNotices(null);
    }

    public void setConsumeNotices(String val) {
        consumeNotices = val;
    }

    public boolean isNotEmpty() {
        return !SOSString.isEmpty(expectNotices) || !SOSString.isEmpty(consumeNotices);
    }

    private String tryTrim(String val) {
        return val == null ? null : val.trim();
    }

}
