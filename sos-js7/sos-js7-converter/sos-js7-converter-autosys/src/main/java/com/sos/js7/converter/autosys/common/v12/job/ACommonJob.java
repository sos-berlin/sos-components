package com.sos.js7.converter.autosys.common.v12.job;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobBox;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobCondition;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobFolder;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobMonitoring;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobNotification;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobResource;
import com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobRunTime;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Condition;
import com.sos.js7.converter.autosys.common.v12.job.attr.condition.Conditions;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.annotation.ArgumentInclude;
import com.sos.js7.converter.commons.annotation.ArgumentSetter;

/** see
 * https://techdocs.broadcom.com/us/en/ca-enterprise-software/intelligent-automation/autosys-workload-automation/12-0-01/reference/ae-job-information-language/jil-job-definitions/alarm-if-fail-attribute-specify-whether-to-post-an-alarm-for-failure-status.html<br/>
 * <br/>
 * Not implemented:<br/>
 * monitor_mode - because of supported job types<br/>
 */
public abstract class ACommonJob {

    private static final Logger LOGGER = LoggerFactory.getLogger(ACommonJob.class);

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

    public static final String LIST_VALUE_DELIMITER = ";";

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
    private static final String ATTR_INTERACTIVE = "interactive";
    private static final String ATTR_RESOURCES = "resources";
    private static final String ATTR_JOB_TERMINATOR = "job_terminator";

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
    @ArgumentInclude(getMethod = "getFolder")
    private CommonJobFolder folder = new CommonJobFolder();
    @ArgumentInclude(getMethod = "getBox")
    private CommonJobBox box = new CommonJobBox();
    @ArgumentInclude(getMethod = "getCondition")
    private CommonJobCondition condition = new CommonJobCondition();
    @ArgumentInclude(getMethod = "getMonitoring")
    private CommonJobMonitoring monitoring = new CommonJobMonitoring();
    @ArgumentInclude(getMethod = "getNotification")
    private CommonJobNotification notification = new CommonJobNotification();
    @ArgumentInclude(getMethod = "getRunTime")
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

    /** interactive - Specify Whether to Run a Command Job in Interactive Mode on Windows<br/>
     * This attribute is optional for the Command (CMD) job type on Windows.<br/>
     * This attribute is ignored for Command jobs that run on UNIX.<br/>
     * 
     * Interactive mode lets users view and interact with jobs that invoke Windows Terminal Services or user interface processes.<br/>
     * For example, you can define a job to open a GUI application, such as Notepad, on the Windows desktop.<br/>
     * 
     * Format: interactive: y | n<br/>
     * 
     * Example: Run a Command Job in Interactive Mode on Windows<br/>
     * This example opens the configuration text file (config.txt) in the Windows Desktop application named Notepad.<br/>
     * insert_job: edit_file<br/>
     * job_type: CMD<br/>
     * machine: winagent<br/>
     * description: "Edit/review a configuration file"<br/>
     * command: notepad.exe "c:\run_info\config.txt"<br/>
     * interactive: y<br/>
     */
    private SOSArgument<Boolean> interactive = new SOSArgument<>(ATTR_INTERACTIVE, false);

    /** resourses - Define or Update Virtual Resource Dependencies in a Job<br/>
     * The resources attribute defines or updates virtual resource dependencies in a job.<br/>
     */
    private SOSArgument<List<CommonJobResource>> resourses = new SOSArgument<>(ATTR_RESOURCES, false);

    /** job_terminator - Kill a Job if Its Box Fails<br/>
     * The job_terminator attribute specifies whether to use a KILLJOB event to terminate the job if its containing box job completes with a FAILURE or
     * TERMINATED status.<br/>
     * Use this attribute with the box_terminator attribute to control how nested jobs behave when a job fails.<br/>
     * This attribute only applies to jobs that are in a box. This attribute is optional for the following job types: BOX, CMD,FW, ...<br/>
     * 
     * Format: job_terminator: {y|1}|{n|0}}<br/>
     * y - Terminates the job if the containing box fails or terminates.<br/>
     * n - Default. Does not terminate the job if the containing box fails or terminates.<br/>
     */
    private SOSArgument<Boolean> jobTerminator = new SOSArgument<>(ATTR_JOB_TERMINATOR, false);

    // --------------------calculated properties
    private Path jobFullPathFromJILDefinition;

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

    @ArgumentSetter(name = ATTR_JOB_TYPE)
    public void setJobType(String val) {
        jobType.setValue(JS7ConverterHelper.stringValue(val));
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

    public SOSArgument<String> getInsertJobX() {
        return insertJob;
    }

    @ArgumentSetter(name = ATTR_INSERT_JOB)
    public void setInsertJob(String val) {
        String s = JS7ConverterHelper.stringValue(val);
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

    @ArgumentSetter(name = ATTR_OWNER)
    public void setOwner(String val) {
        owner.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getPermission() {
        return permission;
    }

    @ArgumentSetter(name = ATTR_PERMISSION)
    public void setPermission(String val) {
        permission.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getDescription() {
        return description;
    }

    @ArgumentSetter(name = ATTR_DESCRIPTION)
    public void setDescription(String val) {
        description.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<Long> getJobLoad() {
        return jobLoad;
    }

    @ArgumentSetter(name = ATTR_JOB_LOAD)
    public void setJobLoad(String val) {
        jobLoad.setValue(JS7ConverterHelper.longValue(val));
    }

    public SOSArgument<Integer> getNRetrys() {
        return nRetrys;
    }

    @ArgumentSetter(name = ATTR_N_RETRYS)
    public void setNRetrys(String val) {
        nRetrys.setValue(JS7ConverterHelper.integerValue(val));
    }

    public SOSArgument<Integer> getAutoDelete() {
        return autoDelete;
    }

    @ArgumentSetter(name = ATTR_AUTO_DELETE)
    public void setAutoDelete(String val) {
        autoDelete.setValue(JS7ConverterHelper.integerValue(val));
    }

    public SOSArgument<Integer> getMaxRunAlarm() {
        return maxRunAlarm;
    }

    @ArgumentSetter(name = ATTR_MAX_RUN_ALARM)
    public void setMaxRunAlarm(String val) {
        maxRunAlarm.setValue(JS7ConverterHelper.integerValue(val));
    }

    public SOSArgument<Integer> getMinRunAlarm() {
        return minRunAlarm;
    }

    @ArgumentSetter(name = ATTR_MIN_RUN_ALARM)
    public void setMinRunAlarm(String val) {
        minRunAlarm.setValue(JS7ConverterHelper.integerValue(val));
    }

    public SOSArgument<String> getMustCompleteTimes() {
        return mustCompleteTimes;
    }

    @ArgumentSetter(name = ATTR_MUST_COMPLETE_TIMES)
    public void setMustCompleteTimes(String val) {
        mustCompleteTimes.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getMustStartTimes() {
        return mustStartTimes;
    }

    @ArgumentSetter(name = ATTR_MUST_START_TIMES)
    public void setMustStartTimes(String val) {
        mustStartTimes.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<Integer> getTermRunTime() {
        return termRunTime;
    }

    @ArgumentSetter(name = ATTR_TERM_RUN_TIME)
    public void setTermRunTime(String val) {
        termRunTime.setValue(JS7ConverterHelper.integerValue(val));
    }

    public SOSArgument<Boolean> getInteractive() {
        return interactive;
    }

    public boolean isInteractive() {
        return interactive.getValue() != null && interactive.getValue();
    }

    @ArgumentSetter(name = ATTR_INTERACTIVE)
    public void setInteractive(String val) {
        interactive.setValue(JS7ConverterHelper.booleanValue(val, false));
    }

    @ArgumentSetter(name = ATTR_RESOURCES)
    public void setResources(String val) {
        if (SOSString.isEmpty(val)) {
            resourses.setValue(null);
        } else {
            try {
                String[] arr = val.split(LIST_VALUE_DELIMITER);
                List<CommonJobResource> l = new ArrayList<>();
                for (String r : arr) {
                    l.add(new CommonJobResource(r, false));// not exclusive (shared)
                }
                resourses.setValue(l.size() == 0 ? null : l);
            } catch (Throwable e) {
                // TODO Report
                LOGGER.error("[setResources=" + val + "]" + e, e);
                resourses.setValue(null);
            }
        }
    }

    public SOSArgument<List<CommonJobResource>> getResources() {
        return resourses;
    }

    public SOSArgument<Boolean> getJobTerminator() {
        return jobTerminator;
    }

    @ArgumentSetter(name = ATTR_JOB_TERMINATOR)
    public void setJobTerminator(String val) {
        jobTerminator.setValue(JS7ConverterHelper.booleanValue(val, false));
    }

    public boolean hasResources() {
        return resourses.getValue() != null && resourses.getValue().size() > 0;
    }

    // Help-method to convert NOTRUNNING conditions to the JS7 Exclusive Lock
    public void addExclusiveResourcePaarIfNotExists(ACommonJob otherJob) {
        if (otherJob == null) {
            return;
        }
        String otherJobName = otherJob.getName();
        CommonJobResource jr = getExclusiveResource(otherJobName);
        CommonJobResource jrO = otherJob.getExclusiveResource(getName());
        try {
            // 1) not set
            if (jr == null && jrO == null) {
                addExclusiveResource(otherJobName);
                otherJob.addExclusiveResource(otherJobName);
            }
            // 2) set by this job
            else if (jr != null && jrO == null) {
                otherJob.addExclusiveResource(jr.getName());
            }
            // 3) set by other job
            else if (jr == null && jrO != null) {
                addExclusiveResource(jrO.getName());
            }
            // 4) set by both jobs
            else {
                // set but different names
                if (!jr.getName().equals(jrO.getName())) {
                    otherJob.removeExclusiveResource(jrO.getName());
                    otherJob.addExclusiveResource(jr.getName());
                }
            }
        } catch (Exception e) {
            LOGGER.error("[addResourceIfNotExists]" + e);
        }
    }

    private void addExclusiveResource(String name) throws Exception {
        initResources();
        resourses.getValue().add(new CommonJobResource("(" + name + ",QUANTITY=0,FREE=Y)", true));
    }

    private void removeExclusiveResource(String name) throws Exception {
        resourses.getValue().removeIf(r -> r.isExclusive() && r.getName().equals(name));
    }

    private CommonJobResource getExclusiveResource(String otherJobName) {
        if (resourses.getValue() == null) {
            return null;
        }
        return resourses.getValue().stream().filter(r -> r.isExclusive() && (r.getName().equals(otherJobName)) || r.getName().equals(getName()))
                .findFirst().orElse(null);
    }

    private void initResources() {
        if (resourses.getValue() == null) {
            resourses.setValue(new ArrayList<>());
        }
    }

    public void addExclusiveResourceIfNotExistsXXX(Condition c) {
        if (c == null) {
            return;
        }
        String name = c.getJobName();
        if (name == null || !c.isNotrunning()) {
            return;
        }

        if (resourses.getValue() == null) {
            resourses.setValue(new ArrayList<>());
        }
        CommonJobResource jr = resourses.getValue().stream().filter(r -> r.getName().equals(name)).findFirst().orElse(null);
        if (jr == null) {
            try {
                // (GLOB.my_resource,QUANTITY=1,FREE=Y)
                // QUANTITY=0, true - because Exclusive(Non Shared) Lock in JS7
                resourses.getValue().add(new CommonJobResource("(" + name + ",QUANTITY=0,FREE=Y)", true));
            } catch (Exception e) {
                LOGGER.error("[addResourceIfNotExists]" + e);
            }
        }

    }

    public boolean isJobTerminator() {
        return jobTerminator.getValue() != null && jobTerminator.getValue();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("type=").append(converterJobType);
        sb.append(",name=").append(insertJob.getValue());
        return sb.toString();
    }

    public boolean hasMonitoring() {
        return monitoring != null && monitoring.exists();
    }

    public boolean hasNotification() {
        return notification != null && notification.exists();
    }

    public boolean hasRunTime() {
        return runTime != null && runTime.exists();
    }

    public boolean hasCondition() {
        List<Condition> l = conditionsAsList();
        return l != null && l.size() > 0;
    }

    public List<Condition> conditionsAsList() {
        if (condition == null) {
            return null;
        }
        return Conditions.getConditions(condition.getCondition().getValue());
    }

    public boolean hasJobConditions() {
        if (!hasCondition()) {
            return false;
        }
        return conditionsAsList().stream().filter(c -> c.getJobName() != null).count() > 0;
    }

    public boolean hasORConditions() {
        if (!hasCondition()) {
            return false;
        }
        return Conditions.getOROperators(condition.getCondition().getValue()).size() > 0;
    }

    public boolean hasLookBackConditions() {
        if (!hasCondition()) {
            return false;
        }
        return Conditions.getConditionsWithLookBack(condition.getCondition().getValue()).size() > 0;
    }

    public boolean isNameEquals(String otherName) {
        if (otherName == null) {
            return false;
        }
        String name = getName();
        return name != null && name.equals(otherName);
    }

    public boolean isNameEquals(Condition c) {
        if (c == null) {
            return false;
        }
        return isNameEquals(c.getJobName());
    }

    public boolean isNameEquals(ACommonJob j) {
        if (j == null) {
            return false;
        }
        return isNameEquals(j.getName());
    }

    public boolean isBoxNameEquals(String otherBoxName) {
        if (otherBoxName == null) {
            return false;
        }
        String boxName = getBoxName();
        if (boxName == null) {
            return false;
        }
        return boxName.equals(otherBoxName);
    }

    public String getBoxName() {
        if (isBox()) {
            return getName();
        }
        return getBox().getBoxName().getValue();
    }

    public boolean isStandalone() {
        return !ConverterJobType.BOX.equals(converterJobType) && (getBox() == null || getBox().getBoxName().getValue() == null);
    }

    public boolean isBox() {
        return (this instanceof JobBOX);
    }

    public boolean isBoxChildJob() {
        return !isBox() && getBox().getBoxName().getValue() != null;
    }

    public String getBaseName() {
        String name = getName();
        if (name == null) {
            return null;
        }
        int i = name.lastIndexOf(".");
        return i > -1 ? name.substring(i + 1) : name;
    }

    public String getName() {
        if (insertJob == null || insertJob.getValue() == null) {
            return null;
        }
        return insertJob.getValue();
    }

    public String getJobParentAsJILDefinition() {
        String jobName = getName();
        if (jobName == null) {
            return null;
        }
        int i = jobName.lastIndexOf(".");
        return i > -1 ? jobName.substring(0, i) : null;
    }

    // see com.sos.js7.converter.autosys.common.v12.job.attr.CommonJobFolder
    // TODO not works for PNG -> arcp.test_conn.ksh ...
    public Path getJobFullPathFromJILDefinition() {
        if (jobFullPathFromJILDefinition != null) {
            return jobFullPathFromJILDefinition;
        }

        Path path = Paths.get("");
        if (folder != null) {
            String jobParent = getJobParentAsJILDefinition();
            if (!SOSString.isEmpty(folder.getApplication().getValue())) {
                String[] parts = folder.getApplication().getValue().split("\\.");
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    path = path.resolve(part);
                    if (jobParent != null) {
                        if (jobParent.contains(".")) {
                            if (jobParent.startsWith(part + ".")) {
                                jobParent = jobParent.substring(0, (part + ".").length());
                                if (jobParent.length() == 0) {
                                    jobParent = null;
                                }
                            }
                        } else {
                            if (jobParent.equals(part)) {
                                jobParent = null;
                            }
                        }

                    }
                }
            }
            List<String> addedParts = new ArrayList<>();
            if (!SOSString.isEmpty(jobParent)) {
                String[] parts = jobParent.split("\\.");
                for (String part : parts) {
                    path = path.resolve(part);
                    addedParts.add(part);
                }
            }

            if (!SOSString.isEmpty(folder.getGroup().getValue())) {
                String[] parts = folder.getGroup().getValue().split("\\.");
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    if (addedParts.size() > i && addedParts.get(i).equals(part)) {
                        continue;
                    } else {
                        path = path.resolve(part);
                    }
                }
            } else {
            }

            path = path.resolve(getBaseName());
        } else {
            String[] parts = insertJob.getValue().split("\\.");
            for (String part : parts) {
                path = path.resolve(part);
            }
        }
        jobFullPathFromJILDefinition = path;
        return jobFullPathFromJILDefinition;
    }

    public String getApplication() {
        if (folder == null || folder.getApplication().getValue() == null) {
            return null;
        }
        return folder.getApplication().getValue();
    }

    @Override
    public boolean equals(Object other) {
        if (other != null && other instanceof ACommonJob) {
            return isNameEquals((ACommonJob) other);
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

}
