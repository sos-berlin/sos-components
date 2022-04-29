package com.sos.js7.converter.autosys.common.v12.job.attr;

import com.sos.commons.util.common.SOSArgument;
import com.sos.js7.converter.autosys.common.v12.job.attr.annotation.JobAttributeSetter;

public class CommonJobNotification extends AJobAttributes {

    private static final String ATTR_ALARM_IF_FAIL = "alarm_if_fail";
    private static final String ATTR_ALARM_IF_TERMINATED = "alarm_if_terminated";
    private static final String ATTR_NOTIFICATION_MSG = "notification_msg";
    private static final String ATTR_SEND_NOTIFICATION = "send_notification";
    private static final String ATTR_NOTIFICATION_EMAILADDRESS = "notification_emailaddress";

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

    public SOSArgument<Boolean> getAlarmIfFail() {
        return alarmIfFail;
    }

    @JobAttributeSetter(name = ATTR_ALARM_IF_FAIL)
    public void setAlarmIfFail(String val) {
        alarmIfFail.setValue(AJobAttributes.booleanValue(val, true));
    }

    public SOSArgument<Boolean> getAlarmIfTerminated() {
        return alarmIfTerminated;
    }

    @JobAttributeSetter(name = ATTR_ALARM_IF_TERMINATED)
    public void setAlarmIfTerminated(String val) {
        alarmIfTerminated.setValue(AJobAttributes.booleanValue(val, true));
    }

    public SOSArgument<String> getNotificationMsg() {
        return notificationMsg;
    }

    @JobAttributeSetter(name = ATTR_NOTIFICATION_MSG)
    public void setNotificationMsg(String val) {
        notificationMsg.setValue(AJobAttributes.stringValue(val));
    }

    public SOSArgument<String> getSendNotification() {
        return sendNotification;
    }

    @JobAttributeSetter(name = ATTR_SEND_NOTIFICATION)
    public void setSendNotification(String val) {
        sendNotification.setValue(AJobAttributes.stringValue(val));
    }

    public SOSArgument<String> getNotificationEmailaddress() {
        return notificationEmailaddress;
    }

    @JobAttributeSetter(name = ATTR_NOTIFICATION_EMAILADDRESS)
    public void setNotificationEmailaddress(String val) {
        notificationEmailaddress.setValue(AJobAttributes.stringValue(val));
    }
}
