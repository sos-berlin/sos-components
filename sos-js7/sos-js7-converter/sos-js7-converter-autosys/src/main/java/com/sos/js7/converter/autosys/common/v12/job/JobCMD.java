package com.sos.js7.converter.autosys.common.v12.job;

import com.sos.commons.util.common.SOSArgument;

public class JobCMD extends ACommonMachineJob {

    /** command - This attribute is required for the Command (CMD) job type.<br/>
     */
    private SOSArgument<String> command = new SOSArgument<>("command", true);

    /** std_err_file<br/>
     * Format: std_err_file: file_name [environment_variable]| blob_name<br/>
     * <br/>
     * JS7-JS7 Task History<br/>
     */
    private SOSArgument<String> stdErrFile = new SOSArgument<>("std_err_file", false);

    /** std_out_file<br/>
     * Format: std_out_file: file_name [environment_variable]| blob_name<br/>
     * <br/>
     * JS7-JS7 Task History<br/>
     */
    private SOSArgument<String> stdOutFile = new SOSArgument<>("std_out_file", false);

    private SOSArgument<String> startTimes = new SOSArgument<>("start_times", true);

    public JobCMD() {
        super(JobType.CMD);
    }

    public SOSArgument<String> getCommand() {
        return command;
    }

    public void setCommand(String val) {
        command.setValue(stringValue(val));
    }

    public SOSArgument<String> getStdErrFile() {
        return stdErrFile;
    }

    public void setStdErrFile(String val) {
        stdErrFile.setValue(stringValue(val));
    }

    public SOSArgument<String> getStdOutFile() {
        return stdOutFile;
    }

    public void setStdOutFile(String val) {
        stdOutFile.setValue(stringValue(val));
    }

    public SOSArgument<String> getStartTimes() {
        return startTimes;
    }

    public void setStartTimes(String val) {
        startTimes.setValue(stringValue(val));
    }

}
