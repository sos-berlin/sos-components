package com.sos.joc.event.bean.dailyplan;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.joc.event.bean.JOCEvent;

public class DailyPlanEvent extends JOCEvent {
    
 
    public DailyPlanEvent(String controllerId, String dailyPlanDate) {
        super("DailyPlanUpdated", controllerId, null);
        if (dailyPlanDate == null) {
            dailyPlanDate = "";
        }
        putVariable("dailyPlanDate", dailyPlanDate);
    }
    
    public DailyPlanEvent(String key, String controllerId, String dailyPlanDate) {
        super(key, controllerId, null);
        if (dailyPlanDate == null) {
            dailyPlanDate = "";
        }
        putVariable("dailyPlanDate", dailyPlanDate);
    }
    
    @JsonIgnore
    public String getDailyPlanDate() {
        return (String) getVariables().get("dailyPlanDate");
    }
}
