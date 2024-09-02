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
        if (SOSString.isEmpty(expectNotices)) {
            return null;
        }
        ExpectNotices n = new ExpectNotices();
        n.setNoticeBoardNames(expectNotices);
        return n;
    }

    public ConsumeNotices toConsumeNotices() {
        if (SOSString.isEmpty(consumeNotices)) {
            return null;
        }
        ConsumeNotices n = new ConsumeNotices();
        n.setNoticeBoardNames(consumeNotices);
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

}
