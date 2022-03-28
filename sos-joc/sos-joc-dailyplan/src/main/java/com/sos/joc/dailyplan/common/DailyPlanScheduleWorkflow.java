package com.sos.joc.dailyplan.common;

public class DailyPlanScheduleWorkflow {

    private final String name;
    private final String path;
    private final String content;

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof DailyPlanScheduleWorkflow) {
            DailyPlanScheduleWorkflow o = (DailyPlanScheduleWorkflow) obj;
            return o.name.equals(this.name);
        }
        return false;
    }

}
