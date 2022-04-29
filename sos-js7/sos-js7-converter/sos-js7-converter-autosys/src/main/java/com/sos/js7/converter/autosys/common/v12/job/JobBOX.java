package com.sos.js7.converter.autosys.common.v12.job;

import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.attr.AJobAttributes;
import com.sos.js7.converter.autosys.common.v12.job.attr.annotation.JobAttributeSetter;

public class JobBOX extends ACommonJob {

    private static final String ATTR_BOX_SUCCESS = "box_success";
    private static final String ATTR_BOX_FAILURE = "box_failure";

    /** box_success - Define Criteria for Box Job Success<br/>
     * This attribute applies only to the BOX job type and is optional.<br/>
     * 
     * Format: box_success: conditions<br/>
     * Example: box_success: (success(JobA) AND success(JobB)) OR (done(JobD) AND done(Job E))<br/>
     * <br/>
     * JS7 - to be dropped?<br/>
     */
    private SOSArgument<String> boxSuccess = new SOSArgument<>(ATTR_BOX_SUCCESS, false);

    /** box_failure - Define Criteria for Box Job Failure<br/>
     * This attribute applies only to the BOX job type and is optional.<br/>
     * 
     * Format: box_failure: conditions<br/>
     * Example: box_failure: (success(JobA) AND success(JobB)) OR (done(JobD) AND done(Job E))<br/>
     * <br/>
     * JS7 - to be dropped?<br/>
     */
    private SOSArgument<String> boxFailure = new SOSArgument<>(ATTR_BOX_FAILURE, false);

    public JobBOX() {
        super(ConverterJobType.BOX);
    }

    public SOSArgument<String> getBoxSuccess() {
        return boxSuccess;
    }

    @JobAttributeSetter(name = ATTR_BOX_SUCCESS)
    public void setBoxSuccess(String val) {
        boxSuccess.setValue(AJobAttributes.stringValue(val));
    }

    public SOSArgument<String> getBoxFailure() {
        return boxFailure;
    }

    @JobAttributeSetter(name = ATTR_BOX_FAILURE)
    public void setBoxFailure(String val) {
        boxFailure.setValue(AJobAttributes.stringValue(val));
    }

}
