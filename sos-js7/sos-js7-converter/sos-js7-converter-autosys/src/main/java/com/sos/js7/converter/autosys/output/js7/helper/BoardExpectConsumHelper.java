package com.sos.js7.converter.autosys.output.js7.helper;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.instruction.ConsumeNotices;
import com.sos.inventory.model.instruction.ExpectNotices;

public class BoardExpectConsumHelper {

    private String expectNotices;
    private String consumeNotices;

    public String getExpectNotices() {
        return expectNotices;
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
    
    public void setExpectNotices(String val) {
        expectNotices = val;
    }

    public String getConsumeNotices() {
        return consumeNotices;
    }

    public void setConsumeNotices(String val) {
        consumeNotices = val;
    }

    public boolean isNotEmpty() {
        return !SOSString.isEmpty(expectNotices) || !SOSString.isEmpty(consumeNotices);
    }

}
