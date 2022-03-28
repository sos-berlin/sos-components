package com.sos.joc.dailyplan.common;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.sos.inventory.model.schedule.Schedule;

public class DailyPlanSchedule {

    // introduced with 2.3.0 - compatibility with scheduler.workflowName (deprecated, use scheduler.workflowNames instead)
    public static final boolean SCHEDULE_CONSIDER_WORKFLOW_NAME = true;

    private final Schedule schedule;
    private final List<DailyPlanScheduleWorkflow> workflows;

    public DailyPlanSchedule(Schedule schedule) {
        this(schedule, null);
    }

    public DailyPlanSchedule(Schedule schedule, List<DailyPlanScheduleWorkflow> workflows) {
        this.schedule = schedule;
        this.workflows = workflows == null ? new ArrayList<>() : workflows;
    }

    public String getWorkflowsAsString() {
        return String.join(",", workflows.stream().map(e -> {
            return e.getName();
        }).collect(Collectors.toList()));
    }

    public void addWorkflow(DailyPlanScheduleWorkflow newW) {
        if (newW == null || newW.getName() == null) {
            return;
        }
        DailyPlanScheduleWorkflow oldW = getWorkflow(newW.getName());
        if (oldW == null) {
            workflows.add(newW);
        }
    }

    public DailyPlanScheduleWorkflow getWorkflow(String name) {
        return workflows.stream().filter(e -> e.getName().equals(name)).findAny().orElse(null);
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public List<DailyPlanScheduleWorkflow> getWorkflows() {
        return workflows;
    }

}
