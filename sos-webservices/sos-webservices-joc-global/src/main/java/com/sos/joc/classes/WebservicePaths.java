package com.sos.joc.classes;

import com.sos.commons.util.SOSString;

public class WebservicePaths {

    public static final String ORDERS = "orders";
    public static final String ORDER = "order";
    public static final String TASKS = "tasks";
    public static final String SCHEDULES = "schedules";
    public static final String UTILITIES = "utilities";
    public static final String MONITORING = "monitoring";
    public static final String DAILYPLAN = "daily_plan";

    public static String getResourceImplPath(String mainPath) {
        return getResourceImplPath(mainPath, null);
    }

    public static String getResourceImplPath(String mainPath, final String subPath) {
        if (SOSString.isEmpty(subPath)) {
            return String.format("./%s", mainPath);
        }
        return String.format("./%s/%s", mainPath, subPath);
    }

}