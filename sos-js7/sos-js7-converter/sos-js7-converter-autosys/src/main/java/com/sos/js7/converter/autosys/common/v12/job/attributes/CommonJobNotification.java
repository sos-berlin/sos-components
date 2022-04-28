package com.sos.js7.converter.autosys.common.v12.job.attributes;

import com.sos.commons.util.common.SOSArgument;

public class CommonJobNotification extends AJobArguments {

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
    private SOSArgument<Boolean> alarmIfFail = new SOSArgument<>("alarm_if_fail", false);

    /** alarm_if_terminated - Specify Whether to Post the JOBTERMINATED for TERMINATED Status<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: alarm_if_terminated: y | n<br/>
     * y - Default. Posts the JOBTERMINATED alarm to the scheduler when the job is terminated.<br/>
     * Note: You can specify 1 instead of y.<br/>
     * n - Does not post the JOBTERMINATED alarm to the scheduler when the job is terminated.<br/>
     * You can specify 0 instead of n.<br/>
     */
    private SOSArgument<Boolean> alarmIfTerminated = new SOSArgument<>("alarm_if_terminated", false);

    /** notification_msg - Define the Message to Include in the Notification<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: notification_msg: value<br/>
     * value - Defines a message to include in the notification.<br/>
     * Limits: Up to 255 alphanumeric characters<br/>
     * <br/>
     * JS7 - 80% - Notifications can be customized, however, a specific message per job is not available. This requires a smaller change.<br/>
     */
    private SOSArgument<String> notificationMsg = new SOSArgument<>("notification_msg", false);

    /** send_notification - Specify Whether to Send a Notification<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: send_notification: y | n | f<br/>
     * <br/>
     * JS7 - 80% - Notifications are sent to all jobs or to a selection of jobs.<br/>
     * If individual jobs want to notify then they have to specify recipients.<br/>
     * It requires a small change not to force the job to specify recipients but to use global settings for recipients.<br/>
     */
    private SOSArgument<String> sendNotification = new SOSArgument<>("send_notification", false);

    /** notification_emailaddress - Identify the Recipient of the Email Notification by Email Address<br/>
     * This attribute is optional for all job types.<br/>
     * 
     * Format: notification_emailaddress: address<br/>
     */
    private SOSArgument<String> notificationEmailaddress = new SOSArgument<>("notification_emailaddress", false);

    public SOSArgument<Boolean> getAlarmIfFail() {
        return alarmIfFail;
    }

    public void setAlarmIfFail(String val) {
        alarmIfFail.setValue(AJobArguments.booleanValue(val, true));
    }

    public SOSArgument<Boolean> getAlarmIfTerminated() {
        return alarmIfTerminated;
    }

    public void setAlarmIfTerminated(String val) {
        alarmIfTerminated.setValue(AJobArguments.booleanValue(val, true));
    }

    public SOSArgument<String> getNotificationMsg() {
        return notificationMsg;
    }

    public void setNotificationMsg(String val) {
        notificationMsg.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<String> getSendNotification() {
        return sendNotification;
    }

    public void setSendNotification(String val) {
        sendNotification.setValue(AJobArguments.stringValue(val));
    }

    public SOSArgument<String> getNotificationEmailaddress() {
        return notificationEmailaddress;
    }

    public void setNotificationEmailaddress(String val) {
        notificationEmailaddress.setValue(AJobArguments.stringValue(val));
    }
}
