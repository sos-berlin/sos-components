package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.annotation.ArgumentSetter;

public class JobBOX extends ACommonJob {

    private static final String ATTR_BOX_SUCCESS = "box_success";
    private static final String ATTR_BOX_FAILURE = "box_failure";
    private static final String ATTR_PRIORITY = "priority";

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

    /** !!! Description from ACommonMachineJob<bR/>
     * priority - Define the Queue Priority of the Job<br/>
     * This attribute is optional for all job types. This attribute does not apply to box jobs.<br/>
     * 
     * Format: priority: priority_level<br/>
     * Example: priority: 0 <br/>
     * JS7 - JS7 feature currently not available and is developed for iteration 3<br/>
     */
    private SOSArgument<Integer> priority = new SOSArgument<>(ATTR_PRIORITY, false);

    private List<ACommonJob> jobs = new ArrayList<>();

    public JobBOX(Path source) {
        super(source, ConverterJobType.BOX);
    }

    public SOSArgument<String> getBoxSuccess() {
        return boxSuccess;
    }

    @ArgumentSetter(name = ATTR_BOX_SUCCESS)
    public void setBoxSuccess(String val) {
        boxSuccess.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getBoxFailure() {
        return boxFailure;
    }

    @ArgumentSetter(name = ATTR_BOX_FAILURE)
    public void setBoxFailure(String val) {
        boxFailure.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<Integer> getPriority() {
        return priority;
    }

    @ArgumentSetter(name = ATTR_PRIORITY)
    public void setPriority(String val) {
        priority.setValue(JS7ConverterHelper.integerValue(val));
    }

    public void setJobs(List<ACommonJob> val) {
        jobs = val;
    }

    public List<ACommonJob> getJobs() {
        return jobs;
    }

    public boolean hasBoxSuccessOrBoxFailure() {
        return boxSuccess.getValue() != null || boxFailure.getValue() != null;
    }

}
