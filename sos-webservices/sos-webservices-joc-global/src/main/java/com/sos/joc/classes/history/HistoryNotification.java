package com.sos.joc.classes.history;

import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.notification.JobNotification;
import com.sos.inventory.model.job.notification.JobNotificationMail;
import com.sos.inventory.model.job.notification.JobNotificationType;

public class HistoryNotification {

    public static boolean isJobMailNotificationEmpty(JobNotification jn) {
        if (jn != null) {
            return isJobNotificationTypesEmpty(jn.getTypes()) && isJobNotificationMailEmpty(jn.getMail());
        }
        return true;
    }

    public static boolean isJobNotificationTypesEmpty(List<JobNotificationType> types) {
        return types == null || types.size() == 0;
    }

    private static boolean isJobNotificationMailEmpty(JobNotificationMail mail) {
        if (mail == null) {
            return true;
        }
        if (SOSString.isEmpty(mail.getTo()) && SOSString.isEmpty(mail.getCc()) && SOSString.isEmpty(mail.getBcc())) {
            return true;
        }
        return false;
    }

}
