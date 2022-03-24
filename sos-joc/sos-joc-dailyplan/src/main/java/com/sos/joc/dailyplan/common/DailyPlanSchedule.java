package com.sos.joc.dailyplan.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.inventory.model.schedule.Schedule;

public class DailyPlanSchedule {

    // introduced with 2.3.0 - compatibility with scheduler.workflowName (deprecated, use scheduler.workflowNames instead)
    public static final boolean SCHEDULE_CONSIDER_WORKFLOW_NAME = true;

    private final Schedule schedule;

    private final List<DailyPlanScheduleWorkflow> workflows;

    public DailyPlanSchedule(Schedule schedule, List<DailyPlanScheduleWorkflow> workflows) {
        this.schedule = schedule;
        this.workflows = workflows == null ? new ArrayList<>() : workflows;

        // TODO - check if not used ...
        if (this.workflows.size() == 0) {
            if (schedule.getWorkflowName() != null) {
                workflows.add(new DailyPlanScheduleWorkflow(schedule.getWorkflowName(), null));
            } else {
                if (schedule.getWorkflowNames() != null) {
                    for (String w : schedule.getWorkflowNames()) {
                        workflows.add(new DailyPlanScheduleWorkflow(w, null));
                    }
                }
            }
        }
    }

    public String getWorkflowsAsString() {
        return String.join(",", workflows.stream().map(e -> {
            return e.getName();
        }).collect(Collectors.toList()));
    }

    @SuppressWarnings("unused")
    private DailyPlanScheduleWorkflow getWorkflow(String name) {
        return workflows.stream().filter(e -> e.getName().equals(name)).findAny().orElse(null);
    }

    public boolean hasMinimumOneOfWorkflows(Set<String> workflowNames) {
        if (workflowNames == null || workflowNames.size() == 0) {
            return false;
        }
        for (DailyPlanScheduleWorkflow w : workflows) {
            if (workflowNames.contains(w.getName())) {
                return true;
            }
        }
        return false;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public List<DailyPlanScheduleWorkflow> getWorkflows() {
        return workflows;
    }

}
