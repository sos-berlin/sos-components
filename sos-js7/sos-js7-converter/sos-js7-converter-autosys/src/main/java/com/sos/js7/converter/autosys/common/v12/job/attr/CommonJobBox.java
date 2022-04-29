package com.sos.js7.converter.autosys.common.v12.job.attr;

import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.attr.annotation.JobAttributeSetter;

public class CommonJobBox extends AJobAttributes {

    private static final String ATTR_BOX_NAME = "box_name";
    private static final String ATTR_BOX_TERMINATOR = "box_terminator";

    /** box_name - Identify a Box as Container for a Job<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * The box_name attribute identifies an existing box job to put a job in.<br/>
     * Boxes let you process a set of jobs as a group.<br/>
     * This is useful for setting starting conditions at the box level to “gate” the jobs in the box.<br/>
     * You can specify each job’s starting conditions relative to the other jobs in the box as appropriate.<br/>
     * Format: box_name: name<br/>
     * name - Specifies an existing box job to put the job in.<br/>
     * Limits: Up to 64 characters<br/>
     * <br/>
     * JS7 - 100% - Workflows<br/>
     */
    private SOSArgument<String> boxName = new SOSArgument<>(ATTR_BOX_NAME, false);

    /** box_terminator - Terminate Box on Job Failure<br/>
     * This attribute applies to all job types and is optional.<br/>
     * 
     * Format: box_terminator: {y|1}|{n|0}}<br/>
     * y - Instructs the scheduler to terminate the parent box when the job ends in FAILURE<br/>
     * n - Default. Does not terminate the parent box when the job ends in FAILURE.<br/>
     * <br/>
     * JS7 - 75% - JS7 workflows ("boxes") to not terminate the parent workflow on failure.<br/>
     */
    private SOSArgument<Boolean> boxTerminator = new SOSArgument<>(ATTR_BOX_TERMINATOR, false);

    public SOSArgument<String> getBoxName() {
        return boxName;
    }

    @JobAttributeSetter(name = ATTR_BOX_NAME)
    public void setBoxName(String val) {
        boxName.setValue(AJobAttributes.stringValue(val));
    }

    public SOSArgument<Boolean> getBoxTerminator() {
        return boxTerminator;
    }

    @JobAttributeSetter(name = ATTR_BOX_TERMINATOR)
    public void setBoxTerminator(String val) {
        boxTerminator.setValue(AJobAttributes.booleanValue(val, false));
    }

}
