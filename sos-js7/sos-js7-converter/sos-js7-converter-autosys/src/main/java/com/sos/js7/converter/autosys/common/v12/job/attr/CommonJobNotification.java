package com.sos.js7.converter.autosys.common.v12.job.attr;

import java.util.List;
import java.util.stream.Collectors;

import com.sos.commons.util.SOSString;
import com.sos.commons.util.arguments.base.SOSArgument;
import com.sos.js7.converter.commons.JS7ConverterHelper;
import com.sos.js7.converter.commons.annotation.ArgumentSetter;

public class CommonJobNotification extends AJobAttributes {

    public enum AlarmType {
        MINRUNALARM, JOBFAILURE, MAX_RETRYS, STARTJOBFAIL, EVENT_HDLR_ERROR, EVENT_QUE_ERROR, JOBNOT_ONICEHOLD, MAXRUNALARM, RESOURCE, MISSING_HEARTBEAT, CHASE, DATABASE_COMM, VERSION_MISMATCH, DUPLICATE_EVENT, INSTANCE_UNAVAILABLE, AUTO_PING, EXTERN_DEPS_ERROR, SERVICEDESK_FAILURE, UNINOTIFY_FAILURE, CPI_JOBNAME_INVALID, CPI_UNAVAILABLE, MUST_START_ALARM, MUST_COMPLETE_ALARM, WAIT_REPLY_ALARM, KILLJOBFAIL, SENDSIGFAIL, REPLY_RESPONSE_FAIL, RETURN_RESOURCE_FAIL, RESTARTJOBFAIL, JOBNOT_ONNOEXEC, QUEUEDJOB_STARTFAIL, SUSPENDJOBFAIL, RESUMEJOBFAIL
    }

    private static final String ATTR_ALARM_IF_FAIL = "alarm_if_fail";
    private static final String ATTR_ALARM_IF_TERMINATED = "alarm_if_terminated";

    private static final String ATTR_SEND_NOTIFICATION = "send_notification";

    private static final String ATTR_NOTIFICATION_ALARM_TYPES = "notification_alarm_types";
    private static final String ATTR_NOTIFICATION_MSG = "notification_msg";
    private static final String ATTR_NOTIFICATION_EMAILADDRESS = "notification_emailaddress";
    private static final String ATTR_NOTIFICATION_EMAILADDRESS_ON_ALARM = "notification_emailaddress_on_alarm";
    private static final String ATTR_NOTIFICATION_EMAILADDRESS_ON_FAILURE = "notification_emailaddress_on_failure";
    private static final String ATTR_NOTIFICATION_EMAILADDRESS_ON_SUCCESS = "notification_emailaddress_on_success";
    private static final String ATTR_NOTIFICATION_EMAILADDRESS_ON_TERMINATED = "notification_emailaddress_on_terminated";

    /** alarm_if_fail - Specify Whether to Post an Alarm for FAILURE Status<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: alarm_if_fail: y | n<br/>
     * y - Default. Posts an alarm to the scheduler when the job fails.<br/>
     * Note: You can specify 1 instead of y.<br/>
     * n - Does not post an alarm to the scheduler when the job fails.<br/>
     * You can specify 0 instead of n.<br/>
     * <br/>
     * JS7 - 100% - Notification<br/>
     */
    private SOSArgument<Boolean> alarmIfFail = new SOSArgument<>(ATTR_ALARM_IF_FAIL, false);

    /** alarm_if_terminated - Specify Whether to Post the JOBTERMINATED for TERMINATED Status<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: alarm_if_terminated: y | n<br/>
     * y - Default. Posts the JOBTERMINATED alarm to the scheduler when the job is terminated.<br/>
     * Note: You can specify 1 instead of y.<br/>
     * n - Does not post the JOBTERMINATED alarm to the scheduler when the job is terminated.<br/>
     * You can specify 0 instead of n.<br/>
     */
    private SOSArgument<Boolean> alarmIfTerminated = new SOSArgument<>(ATTR_ALARM_IF_TERMINATED, false);

    /** The notification_alarm_types attribute specifies the alarm for which to send the email notification.<br/>
     * Supported Job Types: This attribute is optional for all job types. */
    private SOSArgument<List<AlarmType>> notificationAlarmTypes = new SOSArgument<>(ATTR_NOTIFICATION_ALARM_TYPES, false);

    /** notification_msg - Define the Message to Include in the Notification<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: notification_msg: value<br/>
     * value - Defines a message to include in the notification.<br/>
     * Limits: Up to 255 alphanumeric characters<br/>
     * <br/>
     * JS7 - 80% - Notifications can be customized, however, a specific message per job is not available. This requires a smaller change.<br/>
     */
    private SOSArgument<String> notificationMsg = new SOSArgument<>(ATTR_NOTIFICATION_MSG, false);

    /** send_notification - Specify Whether to Send a Notification<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: send_notification: y | n | f<br/>
     * <br/>
     * JS7 - 80% - Notifications are sent to all jobs or to a selection of jobs.<br/>
     * If individual jobs want to notify then they have to specify recipients.<br/>
     * It requires a small change not to force the job to specify recipients but to use global settings for recipients.<br/>
     */
    private SOSArgument<String> sendNotification = new SOSArgument<>(ATTR_SEND_NOTIFICATION, false);

    /** notification_emailaddress - Identify the Recipient of the Email Notification by Email Address<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: notification_emailaddress: address<br/>
     */
    private SOSArgument<String> notificationEmailaddress = new SOSArgument<>(ATTR_NOTIFICATION_EMAILADDRESS, false);

    /** The notification_emailaddress_on_alarm attribute specifies the email address of the recipient of the email notification when an alarm is raised.<br/>
     * Supported Job Types: This attribute is optional for all job types. */

    private SOSArgument<String> notificationEmailaddressOnAlarm = new SOSArgument<>(ATTR_NOTIFICATION_EMAILADDRESS_ON_ALARM, false);
    /** The notification_emailaddress_on_failure attribute specifies the email address of the recipient of the email notification when the job fails.<br />
     * Supported Job Types: This attribute is optional for all job types. */
    private SOSArgument<String> notificationEmailaddressOnFailure = new SOSArgument<>(ATTR_NOTIFICATION_EMAILADDRESS_ON_FAILURE, false);

    /** The notification_emailaddress_on_success attribute specifies the email address of the recipient of the email notification when the job completes
     * successfully.<br/>
     * Supported Job Types: This attribute is optional for all job types. */
    private SOSArgument<String> notificationEmailaddressOnSuccess = new SOSArgument<>(ATTR_NOTIFICATION_EMAILADDRESS_ON_SUCCESS, false);

    /** The notification_emailaddress_on_terminated attribute specifies the email address of the recipient of the email notification when the job
     * terminates.<br/>
     * Supported Job Types: This attribute is optional for all job types. */
    private SOSArgument<String> notificationEmailaddressOnTerminated = new SOSArgument<>(ATTR_NOTIFICATION_EMAILADDRESS_ON_TERMINATED, false);

    public SOSArgument<Boolean> getAlarmIfFail() {
        return alarmIfFail;
    }

    @ArgumentSetter(name = ATTR_ALARM_IF_FAIL)
    public void setAlarmIfFail(String val) {
        alarmIfFail.setValue(JS7ConverterHelper.booleanValue(val, true));
    }

    public SOSArgument<Boolean> getAlarmIfTerminated() {
        return alarmIfTerminated;
    }

    @ArgumentSetter(name = ATTR_ALARM_IF_TERMINATED)
    public void setAlarmIfTerminated(String val) {
        alarmIfTerminated.setValue(JS7ConverterHelper.booleanValue(val, true));
    }

    public SOSArgument<List<AlarmType>> getNotificationAlarmTypes() {
        return notificationAlarmTypes;
    }

    @ArgumentSetter(name = ATTR_NOTIFICATION_ALARM_TYPES)
    public void setNotificationAlarmTypes(String val) {
        if (SOSString.isEmpty(val)) {
            notificationAlarmTypes.setValue(null);
        } else {
            notificationAlarmTypes.setValue(JS7ConverterHelper.stringListValue(val, ",").stream().map(v -> AlarmType.valueOf(v.toUpperCase()))
                    .collect(Collectors.toList()));
        }
    }

    public SOSArgument<String> getNotificationMsg() {
        return notificationMsg;
    }

    @ArgumentSetter(name = ATTR_NOTIFICATION_MSG)
    public void setNotificationMsg(String val) {
        notificationMsg.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getSendNotification() {
        return sendNotification;
    }

    @ArgumentSetter(name = ATTR_SEND_NOTIFICATION)
    public void setSendNotification(String val) {
        sendNotification.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getNotificationEmailaddress() {
        return notificationEmailaddress;
    }

    @ArgumentSetter(name = ATTR_NOTIFICATION_EMAILADDRESS)
    public void setNotificationEmailaddress(String val) {
        notificationEmailaddress.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getNotificationEmailaddressOnAlarm() {
        return notificationEmailaddressOnAlarm;
    }

    @ArgumentSetter(name = ATTR_NOTIFICATION_EMAILADDRESS_ON_ALARM)
    public void setNotificationEmailaddressOnAlarm(String val) {
        notificationEmailaddressOnAlarm.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getNotificationEmailaddressOnFailure() {
        return notificationEmailaddressOnFailure;
    }

    @ArgumentSetter(name = ATTR_NOTIFICATION_EMAILADDRESS_ON_FAILURE)
    public void setNotificationEmailaddressOnFailure(String val) {
        notificationEmailaddressOnFailure.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getNotificationEmailaddressOnSuccess() {
        return notificationEmailaddressOnSuccess;
    }

    @ArgumentSetter(name = ATTR_NOTIFICATION_EMAILADDRESS_ON_SUCCESS)
    public void setNotificationEmailaddressOnSuccess(String val) {
        notificationEmailaddressOnSuccess.setValue(JS7ConverterHelper.stringValue(val));
    }

    public SOSArgument<String> getNotificationEmailaddressOnTerminated() {
        return notificationEmailaddressOnTerminated;
    }

    @ArgumentSetter(name = ATTR_NOTIFICATION_EMAILADDRESS_ON_TERMINATED)
    public void setNotificationEmailaddressOnTerminated(String val) {
        notificationEmailaddressOnTerminated.setValue(JS7ConverterHelper.stringValue(val));
    }

    public boolean exists() {
        return alarmIfFail.getValue() != null || alarmIfTerminated.getValue() != null || notificationAlarmTypes.getValue() != null || notificationMsg
                .getValue() != null || sendNotification.getValue() != null || notificationEmailaddress.getValue() != null
                || notificationEmailaddressOnAlarm.getValue() != null || notificationEmailaddressOnFailure.getValue() != null
                || notificationEmailaddressOnSuccess.getValue() != null || notificationEmailaddressOnTerminated.getValue() != null;
    }

    @Override
    public String toString() {
        return toString(alarmIfFail, alarmIfTerminated, notificationAlarmTypes, sendNotification, notificationMsg, notificationEmailaddress,
                notificationEmailaddressOnAlarm, notificationEmailaddressOnFailure, notificationEmailaddressOnSuccess,
                notificationEmailaddressOnTerminated);
    }
}
