package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.attr.AJobAttributes;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobBox;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobCondition;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobFolder;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobMonitoring;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobNotification;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobRunTime;
import com.sos.js7.converter.autosys.common.v12.job.attr.annotation.JobAttributeInclude;
import com.sos.js7.converter.autosys.common.v12.job.attr.annotation.JobAttributeSetter;

/** see
 * https://techdocs.broadcom.com/us/en/ca-enterprise-software/intelligent-automation/autosys-workload-automation/12-0-01/reference/ae-job-information-language/jil-job-definitions/alarm-if-fail-attribute-specify-whether-to-post-an-alarm-for-failure-status.html<br/>
 * <br/>
 * Not implemented:<br/>
 * monitor_mode - because of supported job types<br/>
 */
public abstract class ACommonJob {

    /** <br/>
     * CMD -<br/>
     * BOX -<br/>
     * FW - File Watcher<br/>
     * FT - File Trigger<br/>
     * OTMF - Text File Reading and Monitoring<br/>
     * <br/>
     * NOT_SUPPORTED - sos type<br/>
     */
    public enum ConverterJobType {
        CMD, BOX, FW, FT, OMTF, NOT_SUPPORTED
    }

    private static final String ATTR_INSERT_JOB = "insert_job";
    private static final String ATTR_JOB_TYPE = "job_type";
    private static final String ATTR_OWNER = "owner";
    private static final String ATTR_PERMISSION = "permission";
    private static final String ATTR_DESCRIPTION = "description";
    private static final String ATTR_JOB_LOAD = "job_load";
    private static final String ATTR_N_RETRYS = "n_retrys";
    private static final String ATTR_AUTO_DELETE = "auto_delete";
    private static final String ATTR_MAX_RUN_ALARM = "max_run_alarm";
    private static final String ATTR_MIN_RUN_ALARM = "min_run_alarm";
    private static final String ATTR_MUST_COMPLETE_TIMES = "must_complete_times";
    private static final String ATTR_MUST_START_TIMES = "must_start_times";
    private static final String ATTR_TERM_RUN_TIME = "term_run_time";

    private final Path source;

    /** Subcommands */

    /** The insert_job subcommand adds a job definition to the database.<br/>
     * Depending on the type of job you are adding, the insert_job subcommand requires one or more additional attributes.<br/>
     * 
     * Format: insert_job: job_name<br/>
     * Defines the name of the job that you want to schedule.<br/>
     * Limits: Up to 64 characters; valid characters are a-z, A-Z, 0-9, period (.), underscore (_), hyphen (-), colon (:), and pound (#);<br/>
     * do not include embedded spaces or tabs.<br/>
     * <br />
     * Note:<br />
     * Enclose the job name that contains a colon with quotation marks (" ") or precede it with a backslash (\).<br />
     * Adapter jobs do not support colons in the job name. If you specify a colon in the adapter job name, the job fails during execution.<br/>
     * <br/>
     * JS7 - Job Instructions<br/>
     */
    private SOSArgument<String> insertJob = new SOSArgument<>(ATTR_INSERT_JOB, true);

    /** Common Job Attribute */
    /** argument includes */
    @JobAttributeInclude(getMethod = "getFolder")
    private CommonJobFolder folder = new CommonJobFolder();
    @JobAttributeInclude(getMethod = "getBox")
    private CommonJobBox box = new CommonJobBox();
    @JobAttributeInclude(getMethod = "getCondition")
    private CommonJobCondition condition = new CommonJobCondition();
    @JobAttributeInclude(getMethod = "getMonitoring")
    private CommonJobMonitoring monitoring = new CommonJobMonitoring();
    @JobAttributeInclude(getMethod = "getNotification")
    private CommonJobNotification notification = new CommonJobNotification();
    @JobAttributeInclude(getMethod = "getRunTime")
    private CommonJobRunTime runTime = new CommonJobRunTime();

    /** not declared/unknown job arguments */
    private List<SOSArgument<String>> unknown = new ArrayList<>();

    /** job_type - Specify Job Type<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Note: If you do not specify the job_type attribute in your job definition, the job type is set to CMD (the default).<br/>
     * Format: job_type: type<br/>
     * type: Specifies the type of the job that you are defining. You can specify one of the following values:<br/>
     * BOX,CMD,FT(File Trigger),FW(File Watcher) + ca. 20 another types<br/>
     */
    private SOSArgument<String> jobType = new SOSArgument<>(ATTR_JOB_TYPE, false);
    private final ConverterJobType converterJobType;

    /** owner - Define the Owner of the Job<br/>
     * This attribute is optional for all job types except for File Trigger (FT) jobs.<br/>
     * 
     * Format:owner: user@host<br/>
     * user@host: Specifies a valid user that is used as the owner of the job.<br/>
     * For a command job, the user must be a valid user with an account on the specified real machine.<br/>
     * The user must have an account on all machines where a command job can run.<br/>
     * For other job types, the credentials can be verified in different ways either through the operating system or a specific application.<br/>
     * <br/>
     * Default: user@host , where user is the user who invokes jil to define the job and host is the real machine that user is logged on to.<br/>
     * Windows Syntax: owner: user@host| user@domain<br/>
     * Example: owner: chris<br/>
     * <br/>
     * JS7 - to be dropped? <br/>
     */
    private SOSArgument<String> owner = new SOSArgument<>(ATTR_OWNER, false);

    /** permission - Specify the Users with Edit and Execute Permissions Contents<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * UNIX Note: The permission attribute is based on the same permissions used in native UNIX.<br/>
     * It uses the user ID (uid), and group ID (gid) from the UNIX environment to control who can edit job definitions and who can execute the actual command
     * specified in the job.<br/>
     * Windows Note:<br/>
     * The permission attribute provides users (by type) with edit and execute permissions for a specific job.<br/>
     * By default, only a job’s owner has edit and execute permissions on a job.<br/>
     * <br/>
     * Format: permission: permission [, permission]<br/>
     * Specifies the comma-delimited permission levels to associate with the job.<br/>
     * The order in which permission values are specified is not important.<br/>
     * Limits : This value can be up to 30 alphanumeric characters in length.<br/>
     * Valid values are:<br>
     * gx - (UNIX only) Assigns group execute permissions to the job.<br/>
     * ge - (UNIX only) Assigns group edit permissions to the job.<br/>
     * me - Indicates that any authorized user may edit the job, regardless of the machine they are on. Otherwise, the user must be logged on to the machine
     * specified in the owner field (for example, user@host_or_domain).<br/>
     * mx - Indicates that any authorized user may execute the job, regardless of the machine they are on. Otherwise, the user must be logged on to the machine
     * specified in the owner field (for example, user@host_or_domain).<br/>
     * we - Assigns world edit permissions to the job.<br/>
     * wx - Assigns world execute permissions to the job. The job's owner always has full edit and execute permissions. Default: Machine permissions are turned
     * off.<br/>
     * Example: permission: ge, wx<br/>
     * <br/>
     * JS7 - to be dropped? <br/>
     */
    private SOSArgument<String> permission = new SOSArgument<>(ATTR_PERMISSION, false);

    /** description - Define a Text Description for a Job<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: description: "text"<br/>
     * Limits: Up to 500 alphanumeric characters (including spaces)<br/>
     * <br/>
     * JS7 - 100% - ??? Documentation ?<br/>
     */
    private SOSArgument<String> description = new SOSArgument<>(ATTR_DESCRIPTION, false);

    /** job_load - Define a Job's Relative Processing Load<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: job_load: load_units<br/>
     * load_units<br/>
     * Defines the relative load of the job.<br/>
     * This number can be any value in the user-defined range of possible values, which are arbitrary.<br/>
     * Define a value that has some relationship to the value defined in the max_load attribute.<br/>
     * Default: 0 (unrestricted)<br/>
     * Limits: Up to 10 digits<br/>
     * Example: job_load: 10<br/>
     * <br/>
     * JS7-to be discussed: we have doubts about the applicability of this AutoSys feature to virtual server architectures<br/>
     */
    private SOSArgument<Long> jobLoad = new SOSArgument<>(ATTR_JOB_LOAD, false);

    /** n_retrys - Define the Number of Times to Restart a Job After a Failure<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: n_retrys: attempts<br/>
     * attempts - Defines the number of times to attempt to restart the job after it exits with a FAILURE status.<br/>
     * Limits: This value can be any integer in the range 0 to 20.<br/>
     * Default: 0<br/>
     * <br/>
     * JS7 - 90% - Retry Instruction.<br/>
     * JS7 allows any number of retries and individual intervals per retry. No use of a "Restart Factor" to calculate retries.<br/>
     */
    private SOSArgument<Integer> nRetrys = new SOSArgument<>(ATTR_N_RETRYS, false);

    /** auto_delete - Automatically Delete a Job on Completion<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: auto_delete: hours | 0 | -1<br/>
     * hours - Defines the number of hours to wait after the job completes. At that time, the job is automatically deleted.<br/>
     * Limits: 1-17520<br/>
     * 0 - Deletes the job immediately after successful completion. If the job does not complete successfully, the job definition is kept for seven days before
     * it is automatically deleted.<br/>
     * -1 - Does not automatically delete the job. This is the default.<br/>
     * <br/>
     * JS7 - to be dropped?<br/>
     */
    private SOSArgument<Integer> autoDelete = new SOSArgument<>(ATTR_AUTO_DELETE, false);

    /** max_run_alarm - Define the Maximum Run Time for a Job<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * The max_run_alarm attribute specifies the maximum run time (in minutes) that a job requires to finish normally.<br/>
     * Format: max_run_alarm: mins<br/>
     * Limits: 0-2147483647; must be an integer.<br/>
     * Example: max_run_alarm: 120<br/>
     * <br/>
     * JS7 - 100% - job warn_if_longer<br/>
     */
    private SOSArgument<Integer> maxRunAlarm = new SOSArgument<>(ATTR_MAX_RUN_ALARM, false);
    /** Define the Minimum Run Time for a Job<br/>
     * JS7 - 100% - job warn_if_shorter */
    private SOSArgument<Integer> minRunAlarm = new SOSArgument<>(ATTR_MIN_RUN_ALARM, false);

    /** must_complete_times - Specify the Time a Job Must Complete By<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format:<br/>
     * must_complete_times: "hh:mm[, hh:mm...]"<br/>
     * must_complete_times: +minutes<br/>
     * <br/>
     * JS7 - 0% - Feature requires development for iteration 3<br/>
     */
    private SOSArgument<String> mustCompleteTimes = new SOSArgument<>(ATTR_MUST_COMPLETE_TIMES, false);

    /** must_start_times - Specify the Time a Job Must Start By<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format:<br/>
     * must_start_times: "hh:mm[, hh:mm...]"<br/>
     * must_start_times: +minutes<br/>
     * <br/>
     * JS7 - 0% - Feature requires development for iteration 3<br/>
     */
    private SOSArgument<String> mustStartTimes = new SOSArgument<>(ATTR_MUST_START_TIMES, false);

    /** term_run_time - Specify the Maximum Runtime<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * The term_run_time attribute specifies the maximum run time (in minutes) that the job you are defining should require to finish normally.<br/>
     * If the job runs longer than the specified time, AutoSys Workload Automation terminates it.<br/>
     * Format:term_run_time: mins mins<br/>
     * Defines the maximum number of minutes the job should ever require to finish normally.<br/>
     * Limits: 0-527040<br/>
     * Default: 0 (the job is allowed to run forever)<br/>
     * Example: term_run_time: 120<br/>
     * <br/>
     * JS7 - 100% - Job Instructions. timeout attribute<br/>
     */
    private SOSArgument<Integer> termRunTime = new SOSArgument<>(ATTR_TERM_RUN_TIME, false);

    public ACommonJob(Path source, ConverterJobType type) {
        this.source = source;
        this.converterJobType = type;
    }

    public Path getSource() {
        return source;
    }

    public ConverterJobType getConverterJobType() {
        return converterJobType;
    }

    public SOSArgument<String> getJobType() {
        return jobType;
    }

    @JobAttributeSetter(name = ATTR_JOB_TYPE)
    public void setJobType(String val) {
        jobType.setValue(AJobAttributes.stringValue(val));
    }

    public SOSArgument<String> addUnknown(String name, String value) {
        SOSArgument<String> a = new SOSArgument<>(name, false);
        a.setValue(value);
        unknown.add(a);
        return a;
    }

    public List<SOSArgument<String>> getUnknown() {
        return unknown;
    }

    public CommonJobFolder getFolder() {
        return folder;
    }

    public CommonJobCondition getCondition() {
        return condition;
    }

    public CommonJobBox getBox() {
        return box;
    }

    public CommonJobMonitoring getMonitoring() {
        return monitoring;
    }

    public CommonJobNotification getNotification() {
        return notification;
    }

    public CommonJobRunTime getRunTime() {
        return runTime;
    }

    public SOSArgument<String> getInsertJob() {
        return insertJob;
    }

    @JobAttributeSetter(name = ATTR_INSERT_JOB)
    public void setInsertJob(String val) {
        String s = AJobAttributes.stringValue(val);
        if (!SOSString.isEmpty(s)) {
            // \myJob
            if (s.startsWith("\\")) {
                s = s.substring(1);
            }
        }
        insertJob.setValue(s);
    }

    public SOSArgument<String> getOwner() {
        return owner;
    }

    @JobAttributeSetter(name = ATTR_OWNER)
    public void setOwner(String val) {
        owner.setValue(AJobAttributes.stringValue(val));
    }

    public SOSArgument<String> getPermission() {
        return permission;
    }

    @JobAttributeSetter(name = ATTR_PERMISSION)
    public void setPermission(String val) {
        permission.setValue(AJobAttributes.stringValue(val));
    }

    public SOSArgument<String> getDescription() {
        return description;
    }

    @JobAttributeSetter(name = ATTR_DESCRIPTION)
    public void setDescription(String val) {
        description.setValue(AJobAttributes.stringValue(val));
    }

    public SOSArgument<Long> getJobLoad() {
        return jobLoad;
    }

    @JobAttributeSetter(name = ATTR_JOB_LOAD)
    public void setJobLoad(String val) {
        jobLoad.setValue(AJobAttributes.longValue(val));
    }

    public SOSArgument<Integer> getNRetrys() {
        return nRetrys;
    }

    @JobAttributeSetter(name = ATTR_N_RETRYS)
    public void setNRetrys(String val) {
        nRetrys.setValue(AJobAttributes.integerValue(val));
    }

    public SOSArgument<Integer> getAutoDelete() {
        return autoDelete;
    }

    @JobAttributeSetter(name = ATTR_AUTO_DELETE)
    public void setAutoDelete(String val) {
        autoDelete.setValue(AJobAttributes.integerValue(val));
    }

    public SOSArgument<Integer> getMaxRunAlarm() {
        return maxRunAlarm;
    }

    @JobAttributeSetter(name = ATTR_MAX_RUN_ALARM)
    public void setMaxRunAlarm(String val) {
        maxRunAlarm.setValue(AJobAttributes.integerValue(val));
    }

    public SOSArgument<Integer> getMinRunAlarm() {
        return minRunAlarm;
    }

    @JobAttributeSetter(name = ATTR_MIN_RUN_ALARM)
    public void setMinRunAlarm(String val) {
        minRunAlarm.setValue(AJobAttributes.integerValue(val));
    }

    public SOSArgument<String> getMustCompleteTimes() {
        return mustCompleteTimes;
    }

    @JobAttributeSetter(name = ATTR_MUST_COMPLETE_TIMES)
    public void setMustCompleteTimes(String val) {
        mustCompleteTimes.setValue(AJobAttributes.stringValue(val));
    }

    public SOSArgument<String> getMustStartTimes() {
        return mustStartTimes;
    }

    @JobAttributeSetter(name = ATTR_MUST_START_TIMES)
    public void setMustStartTimes(String val) {
        mustStartTimes.setValue(AJobAttributes.stringValue(val));
    }

    public SOSArgument<Integer> getTermRunTime() {
        return termRunTime;
    }

    @JobAttributeSetter(name = ATTR_TERM_RUN_TIME)
    public void setTermRunTime(String val) {
        termRunTime.setValue(AJobAttributes.integerValue(val));
    }

}
