package com.sos.joc.classes;

public class WebservicePaths {

    public static final String ORDERS = "orders";
    public static final String ORDER = "order";
    public static final String TASKS = "tasks";
    public static final String MONITORING = "monitoring";
    public static final String DAILYPLAN = "daily_plan";

    public static String getResourceImplPath(String mainPath, final String subPath) {
        return String.format("./%s/%s", mainPath, subPath);
    }

}