package com.sos.joc.dailyplan.common;

public class DailyPlanScheduleWorkflow {

    private final String name;
    private String path;

    public DailyPlanScheduleWorkflow(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String val) {
        path = val;
    }

}
