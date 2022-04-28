package com.sos.js7.converter.autosys.common.v12.job;

import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.attributes.AJobArguments;

public class JobBOX extends ACommonJob {

    /** box_success - Define Criteria for Box Job Success<br/>
     * This attribute applies only to the BOX job type and is optional.<br/>
     * 
     * Format: box_success: conditions<br/>
     * Example: box_success: (success(JobA) AND success(JobB)) OR (done(JobD) AND done(Job E))<br/>
     * <br/>
     * JS7 - to be dropped?<br/>
     */
    private SOSArgument<String> boxSuccess = new SOSArgument<>("box_success", false);

    /** box_failure - Define Criteria for Box Job Failure<br/>
     * This attribute applies only to the BOX job type and is optional.<br/>
     * 
     * Format: box_failure: conditions<br/>
     * Example: box_failure: (success(JobA) AND success(JobB)) OR (done(JobD) AND done(Job E))<br/>
     * <br/>
     * JS7 - to be dropped?<br/>
     */
    private SOSArgument<String> boxFailure = new SOSArgument<>("box_failure", false);

    public JobBOX() {
        super(JobType.BOX);
    }

    public SOSArgument<String> getBoxSuccess() {
        return boxSuccess;
    }

    public void setBoxSuccess(String val) {
        boxSuccess.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<String> getBoxFailure() {
        return boxFailure;
    }

    public void setBoxFailure(String val) {
        boxFailure.setValue(AJobArguments.stringValue(val));
    }

}
