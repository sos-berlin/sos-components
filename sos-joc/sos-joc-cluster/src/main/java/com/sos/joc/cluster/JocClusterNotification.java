package com.sos.joc.cluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.joc.model.cluster.common.ClusterServices;

public class JocClusterNotification {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterNotification.class);

    private static final String DELIMITER_VALUES = "->";

    private final long expiredInterval;// seconds

    private boolean expired = false;
    private List<String> sections;
    private List<String> serviceSections;
    private List<String> jocSections;

    public JocClusterNotification(int pollingInterval) {
        expiredInterval = pollingInterval + 1;
    }

    public String toString(AtomicReference<List<String>> notification) {
        try {
            if (notification == null || notification.get().size() == 0) {
                return null;
            }
            StringBuilder sb = new StringBuilder("[globals=");
            sb.append(String.join(",", notification.get()));
            sb.append(DELIMITER_VALUES);
            try {
                sb.append(SOSDate.getCurrentTimeAsString(SOSDate.dateTimeFormat));
            } catch (Exception e) {
            }
            sb.append("]");
            return sb.toString();
        } catch (Throwable e) {
            return null;
        }
    }

    public void parse(String notification) {
        if (SOSString.isEmpty(notification)) {
            expired = true;
            return;
        }
        notification = notification.replaceAll("\\[globals=", "").replaceAll("\\]", "");
        LOGGER.debug("notification=" + notification);
        String arr[] = notification.split(DELIMITER_VALUES);
        if (arr.length == 1) {
            expired = true;
            return;
        }
        try {
            Date d = SOSDate.getDate(arr[1], SOSDate.dateTimeFormat);
            long diff = (new Date().getTime() - d.getTime()) / 1_000;
            LOGGER.debug("diff = " + diff + "s, expiredInterval=" + expiredInterval);
            expired = diff >= expiredInterval ? true : false;
        } catch (Exception e) {
        }

        sections = new ArrayList<String>();
        serviceSections = new ArrayList<String>();
        jocSections = new ArrayList<String>();

        List<String> list = Stream.of(arr[0].split(",", -1)).collect(Collectors.toList());
        for (String section : list) {
            sections.add(section);
            try {
                ClusterServices.valueOf(section);
                serviceSections.add(section);
            } catch (Throwable e) {
                jocSections.add(section);
            }
        }

    }

    public boolean isExpired() {
        return expired;
    }

    public List<String> getSections() {
        return sections;
    }

}
