package com.sos.joc.db.common;

import com.sos.history.JobWarning;
import com.sos.monitoring.notification.NotificationApplication;
import com.sos.monitoring.notification.NotificationType;
import com.sos.monitoring.notification.OrderNotificationRange;
import com.sos.monitoring.notification.SystemNotificationCategory;

public class MonitoringConstants {

    public static final NotificationType NOTIFICATION_DEFAULT_TYPE = NotificationType.ERROR;
    public static final NotificationApplication NOTIFICATION_DEFAULT_APPLICATION = NotificationApplication.ORDER_NOTIFICATION;
    public static final OrderNotificationRange NOTIFICATION_DEFAULT_RANGE = OrderNotificationRange.WORKFLOW;
    public static final JobWarning NOTIFICATION_DEFAULT_JOB_WARNING = JobWarning.NONE;
    public static final SystemNotificationCategory SYSTEM_NOTIFICATION_DEFAULT_CATEGORY = SystemNotificationCategory.SYSTEM;
    public static final String SYSTEM_NOTIFICATION_DEFAULT_JOC_ID = "joc#0";

    public static final String MAX_LEN_DEFAULT_SUFFIX = "...";

    public static final int MAX_LEN_CONFIGURATION = 500;
    public static final int MAX_LEN_WARN_TEXT = 500;
    public static final int MAX_LEN_COMMENT = 2_000;
    public static final int MAX_LEN_MESSAGE = 4_000;

    public static final int MAX_LEN_SYSTEM_NOTIFICATION_JOC_ID = 13;
    public static final int MAX_LEN_SYSTEM_NOTIFICATION_SOURCE = 100;
    public static final int MAX_LEN_SYSTEM_NOTIFICATION_NOTIFIER = 255;
    public static final int MAX_LEN_SYSTEM_NOTIFICATION_MESSAGE = 1_000;
    public static final int MAX_LEN_SYSTEM_NOTIFICATION_EXCEPTION = 4_000;

}
