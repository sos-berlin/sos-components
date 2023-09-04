package com.sos.joc.dailyplan.common;

public class DailyPlanScheduleWorkflow {

    private final String name;
    private final String path;
    private final String content;

    private String controllerId;
    private Long avg;

    public DailyPlanScheduleWorkflow(String name, String path, String content) {
        this.name = name;
        this.path = path;
        this.content = content;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public String getContent() {
        return content;
    }

    public void setControllerId(String val) {
        controllerId = val;
    }

    public String getControllerId() {
        return controllerId;
    }

    public void setAvg(Long val) {
        avg = val;
    }

    public Long getAvg() {
        return avg;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DailyPlanScheduleWorkflow) {
            DailyPlanScheduleWorkflow o = (DailyPlanScheduleWorkflow) obj;
            return o.name.equals(this.name);
        }
        return false;
    }

}
