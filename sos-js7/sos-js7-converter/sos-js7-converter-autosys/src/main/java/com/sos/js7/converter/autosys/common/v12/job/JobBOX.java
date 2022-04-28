package com.sos.js7.converter.autosys.common.v12.job;

import com.sos.commons.util.common.SOSArgument;

public class JobBOX extends ACommonJob {

    private SOSArgument<String> boxName = new SOSArgument<>("box_name", true);

    public JobBOX() {
        super(JobType.BOX);
    }

    public SOSArgument<String> getBoxName() {
        return boxName;
    }

    public void setBoxName(String val) {
        boxName.setValue(stringValue(val));
    }

}
