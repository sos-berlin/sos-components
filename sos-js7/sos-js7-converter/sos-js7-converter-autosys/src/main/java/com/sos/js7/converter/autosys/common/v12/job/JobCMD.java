package com.sos.js7.converter.autosys.common.v12.job;

import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.attributes.AJobArguments;

public class JobCMD extends ACommonMachineJob {

    /** command - Specify a Command or Script to Run<br/>
     * This attribute is required for the Command (CMD) job type.<br/>
     * 
     * <br/>
     * JS7 - 100% - Job Instructions<br/>
     */
    private SOSArgument<String> command = new SOSArgument<>("command", true);

    /** std_err_file - Redirect the Standard Error File<br/>
     * This attribute is required for the Command (CMD) job type.<br/>
     *
     * Format: std_err_file: file_name [environment_variable]| blob_name<br/>
     * <br/>
     * JS7 - 80% - Task History<br/>
     */
    private SOSArgument<String> stdErrFile = new SOSArgument<>("std_err_file", false);

    /** std_out_file - Redirect the Standard Output File<br/>
     * This attribute is required for the Command (CMD) job type.<br/>
     * 
     * Format: std_out_file: file_name [environment_variable]| blob_name<br/>
     * <br/>
     * JS7 - 80% - Task History<br/>
     */
    private SOSArgument<String> stdOutFile = new SOSArgument<>("std_out_file", false);

    /** ulimit - Specify UNIX Resource Limits<br/>
     * This attribute is optional for the CMD job type on UNIX.<br/>
     * 
     * Format: ulimit: resource_type="soft_value,hard_value"[, resource_type="soft_value,hard_value"...]<br/>
     * Example: ulimit: c=”100,200”, s=”250,300”, t=”4000,unlimited”, m=”3332,unlimited”<br/>
     * <br/>
     * JS7 - 100% - No a job schedule feature. <br/>
     * Instead, "ulimit" is added to the first line of a job script in JS7 (not: of the shell script executed from disk)<br/>
     */
    private SOSArgument<String> ulimit = new SOSArgument<>("ulimit", false);

    /** fail_codes - Define Exit Codes to Indicate Job Failure<br/>
     * This attribute is optional for the following job types: CMD,HTTP,i5/OS,Micro Focus,Remote Execution,Web Service Document<br/>
     * 
     * Format: fail_codes: exit_codes<br/>
     * Example: fail_codes: 40-50<br/>
     * <br/>
     * JS7 - 50% - Iteration 1: syntax 1,2,4,8<br/>
     * Iteration 2: syntax 1,150,20-30. <br/>
     */
    private SOSArgument<String> failCodes = new SOSArgument<>("fail_codes", false);

    /** Define Exit Codes to Indicate Job Success<br/>
     * see failCodes */
    private SOSArgument<String> successCodes = new SOSArgument<>("success_codes", false);

    /** max_exit_success - Specify Maximum Exit Code for Success<br/>
     * This attribute is optional for the following job types:CMD,I5,MICROFOCUS<br/>
     * 
     * Format: max_exit_success: exit_code<br/>
     * exit_code - Defines the maximum exit code that the job can exit with and be considered a success.<br/>
     * Default: 0<br/>
     * Limits: 0-2147483647 (CMD and i5);<br/>
     * <br/>
     * JS7 - 50% - This feature seems to be redundant to use of the success_codes attribute with a value 0-[max]<br/>
     * Therefore the same iterations apply. <br/>
     */
    private SOSArgument<Integer> maxExitSuccess = new SOSArgument<>("max_exit_success", false);

    /** heartbeat_interval - Set the Monitoring Frequency for a Job<br/>
     * This attribute is optional for the CMD job type.<br/>
     * 
     * Format: heartbeat_interval: mins<br/>
     * Limits: 0-2147483647<br/>
     * Default: 0; the scheduler does not monitor heartbeats from the job<br/>
     * <br/>
     * JS7 - 0% - For a single job an individual solution at job script level should be feasible. <br/>
     */
    private SOSArgument<Long> heartbeatInterval = new SOSArgument<>("heartbeat_interval", false);

    public JobCMD() {
        super(JobType.CMD);
    }

    public SOSArgument<String> getCommand() {
        return command;
    }

    public void setCommand(String val) {
        command.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<String> getStdErrFile() {
        return stdErrFile;
    }

    public void setStdErrFile(String val) {
        stdErrFile.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<String> getStdOutFile() {
        return stdOutFile;
    }

    public void setStdOutFile(String val) {
        stdOutFile.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<String> getUlimit() {
        return ulimit;
    }

    public void setUlimit(String val) {
        ulimit.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<String> getFailCodes() {
        return failCodes;
    }

    public void setFailCodes(String val) {
        failCodes.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<String> getSuccessCodes() {
        return successCodes;
    }

    public void setSuccessCodes(String val) {
        successCodes.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<Integer> getMaxExitSuccess() {
        return maxExitSuccess;
    }

    public void setMaxExitSuccess(String val) {
        maxExitSuccess.setValue(AJobArguments.integerValue(val));
    }

    public SOSArgument<Long> getHeartbeatInterval() {
        return heartbeatInterval;
    }

    public void setHeartbeatInterval(String val) {
        heartbeatInterval.setValue(AJobArguments.longValue(val));
    }
}
