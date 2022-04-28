package com.sos.js7.converter.autosys.common.v12.job;

import org.apache.commons.lang3.StringUtils;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.common.SOSArgument;

/** see
 * https://techdocs.broadcom.com/us/en/ca-enterprise-software/intelligent-automation/autosys-workload-automation/12-0-01/reference/ae-job-information-language/jil-job-definitions/alarm-if-fail-attribute-specify-whether-to-post-an-alarm-for-failure-status.html<br/>
 */
public abstract class ACommonJob {

    public enum JobType {
        CMD, BOX, FILE_WATCHER
    }

    private final JobType type;

    /** Subcommands */

    /** The insert_job subcommand adds a job definition to the database.<br/>
     * Depending on the type of job you are adding, the insert_job subcommand requires one or more additional attributes.<br/>
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
    private SOSArgument<String> insertJob = new SOSArgument<>("insert_job", true);

    /** Common Job Attribute */

    /** owner - This attribute is optional for all job types except for File Trigger (FT) jobs.<br/>
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
     * JS7 - ? <br/>
     */
    private SOSArgument<String> owner = new SOSArgument<>("owner", false);

    /** permission - This attribute is optional for all job types.<br/>
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
     * JS7 - ? <br/>
     */
    private SOSArgument<String> permission = new SOSArgument<>("permission", false);

    /** application - This attribute is optional for all job types.<br/>
     * Format: application: application_name<br/>
     * <br/>
     * The application attribute associates a job with a specific application so users can classify, sort, and filter jobs by application name.<br/>
     * Note: If both the box and child job belongs to the same application, you have to specify the application attribute only for a box job.<br/>
     * Limits: Up to 64 characters; valid characters are a-z, A-Z, 0-9, period (.), underscore (_), pound (#), and hyphen (-);<br/>
     * do not include embedded spaces or tabs<br/>
     * <br/>
     * JS7 - Mapping Folder - Inventory <br/>
     */
    private SOSArgument<String> application = new SOSArgument<>("application", false);

    /** condition - This attribute is optional for all job types.<br/>
     * Format: condition: [(]condition[)][(AND|OR)[(]condition[)]] condition: [(]condition,look_back[)][(AND|OR)[(]condition,look_back[)]]<br/>
     * Example: condition: (success(JobA) AND success(JobB)) OR (done(JobD) AND done(JobE))<br/>
     * <br/>
     * JS7-Notice Board<br/>
     */
    private SOSArgument<String> condition = new SOSArgument<>("condition", false);

    /** description - This attribute is optional for all job types.<br/>
     * Format: description: "text"<br/>
     * Limits: Up to 500 alphanumeric characters (including spaces)<br/>
     * <br/>
     * JS7-??? Documentation ?<br/>
     */
    private SOSArgument<String> description = new SOSArgument<>("description", false);

    /** svcdesk_sev - This attribute is optional for all job types.<br/>
     * The svcdesk_sev attribute specifies the severity level to assign the Service Desk request generated when you have set the service_desk attribute to y or
     * 1 and the job you are defining completes with a FAILURE status.<br/>
     * The severity level indicates how much you expect the request to affect other users.<br/>
     * <br/>
     * Format: svcdesk_sev: level<br/>
     * level limits: 0 to 5<br/>
     * Example: svcdesk_sev: 3<br/>
     */
    private SOSArgument<Integer> svcdeskSev = new SOSArgument<>("svcdesk_sev", false);

    /** svcdesk_imp - This attribute is optional for all job types.<br/>
     * The svcdesk_imp attribute specifies the impact level to assign the Service Desk request generated when you have set the service_desk attribute to y or 1
     * and the job you are defining completes with a FAILURE status.<br/>
     * The impact level indicates how much you expect the request to affect work being performed.<br/>
     * <br/>
     * Format: svcdesk_imp: level<br/>
     * level limits: 0 to 5<br/>
     * Example: svcdesk_imp: 3<br/>
     */
    private SOSArgument<Integer> svcdeskImp = new SOSArgument<>("svcdesk_imp", false);

    /** days_of_week - This attribute is optional for all job types.<br/>
     * Format: <br/>
     * - days_of_week: day [,day...]<br/>
     * - days_of_week: all<br/>
     * day -Specifies the days of the week when the job runs. Options are the following:<br/>
     * – mo -- Specifies Monday.<br/>
     * – tue -- Specifies Tuesday.<br/>
     * – we -- Specifies Wednesday.<br/>
     * – th -- Specifies Thursday.<br/>
     * – fr -- Specifies Friday.<br/>
     * – sa -- Specifies Saturday.<br/>
     * – su -- Specifies Sunday.<br/>
     * Default: No days are selected.<br/>
     * <br/>
     * JS7 - Calendar & Schedules<br/>
     */
    private SOSArgument<String> daysOfWeek = new SOSArgument<>("days_of_week", false);

    /** date_conditions - This attribute is optional for all job types.<br/>
     * The date_conditions attribute specifies whether to use the date or time conditions defined in the following attributes to determine when to run the
     * job:<br/>
     * autocal_asc<br/>
     * days_of_week<br/>
     * exclude_calendar<br/>
     * must_complete_times<br/>
     * must_start_times<br/>
     * run_calendar<br/>
     * run_window<br/>
     * start_mins<br/>
     * start_times<br/>
     * timezone<br/>
     * Format: date_conditions: y | n<br/>
     * y - Uses the date and time conditions defined in other attributes to determine when to run the job.<br/>
     * Note:You can specify 1 instead of y.<br/>
     * n - Default, Ignores the date and time conditions defined in other attributes to determine when to run the job.<br/>
     * Note: You can specify 0 instead of n.<br/>
     */
    private SOSArgument<Boolean> dateConditions = new SOSArgument<>("date_conditions", false);

    /** alarm_if_fail - This attribute is optional for all job types.<br/>
     * Format: alarm_if_fail: y | n<br/>
     * y - Default. Posts an alarm to the scheduler when the job fails.<br/>
     * Note: You can specify 1 instead of y.<br/>
     * n - Does not post an alarm to the scheduler when the job fails.<br/>
     * You can specify 0 instead of n.<br/>
     */
    private SOSArgument<Boolean> alarmIfFail = new SOSArgument<>("alarm_if_fail", false);

    /** alarm_if_terminated - This attribute is optional for all job types.<br/>
     * Format: alarm_if_terminated: y | n<br/>
     * y - Default. Posts the JOBTERMINATED alarm to the scheduler when the job is terminated.<br/>
     * Note: You can specify 1 instead of y.<br/>
     * n - Does not post the JOBTERMINATED alarm to the scheduler when the job is terminated.<br/>
     * You can specify 0 instead of n.<br/>
     */
    private SOSArgument<Boolean> alarmIfTerminated = new SOSArgument<>("alarm_if_terminated", false);

    public ACommonJob(JobType type) {
        this.type = type;
    }

    public SOSArgument<String> getInsertJob() {
        return insertJob;
    }

    public void setInsertJob(String val) {
        String s = stringValue(val);
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

    public void setOwner(String val) {
        owner.setValue(stringValue(val));
    }

    public SOSArgument<String> getPermission() {
        return permission;
    }

    public void setPermission(String val) {
        permission.setValue(stringValue(val));
    }

    public SOSArgument<String> getApplication() {
        return application;
    }

    public void setApplication(String val) {
        application.setValue(stringValue(val));
    }

    public SOSArgument<Boolean> getDateConditions() {
        return dateConditions;
    }

    public void setDateConditions(String val) {
        dateConditions.setValue(booleanValue(val, false));
    }

    public SOSArgument<Boolean> getAlarmIfFail() {
        return alarmIfFail;
    }

    public void setAlarmIfFail(String val) {
        alarmIfFail.setValue(booleanValue(val, true));
    }

    public SOSArgument<Boolean> getAlarmIfTerminated() {
        return alarmIfTerminated;
    }

    public void setAlarmIfTerminated(String val) {
        alarmIfTerminated.setValue(booleanValue(val, true));
    }

    public SOSArgument<String> getCondition() {
        return condition;
    }

    public void setCondition(String val) {
        condition.setValue(stringValue(val));
    }

    public SOSArgument<String> getDescription() {
        return description;
    }

    public void setDescription(String val) {
        description.setValue(stringValue(val));
    }

    public SOSArgument<Integer> getSvcdeskSev() {
        return svcdeskSev;
    }

    public void setSvcdeskSev(String val) {
        svcdeskSev.setValue(integerValue(val));
    }

    public SOSArgument<Integer> getSvcdeskImp() {
        return svcdeskImp;
    }

    public SOSArgument<String> getDaysOfWeek() {
        return daysOfWeek;
    }

    public void setDaysOfWeek(String val) {
        daysOfWeek.setValue(stringValue(val));
    }

    public void setSvcdeskImp(String val) {
        svcdeskImp.setValue(integerValue(val));
    }

    protected String stringValue(String val) {
        return val == null ? null : StringUtils.strip(val.trim(), "\"");
    }

    protected Integer integerValue(String val) {
        return val == null ? null : Integer.parseInt(val.trim());
    }

    protected boolean booleanValue(String val, boolean defaultValue) {
        boolean v = defaultValue;
        if (val != null) {
            switch (val.trim().toLowerCase()) {
            case "y":
            case "1":
                v = true;
                break;
            case "n":
            case "0":
                v = false;
                break;
            }
        }
        return v;
    }

    public JobType getType() {
        return type;
    }

}
