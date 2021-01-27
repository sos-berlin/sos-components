package com.sos.js7.order.initiator;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.Schedule;
  
public abstract class ScheduleSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleSource.class);

    public abstract List<Schedule> fillListOfSchedules() throws IOException, SOSHibernateException;
    public abstract String fromSource();

    protected boolean checkMandatory(Schedule schedule) {
        if (schedule.getPath() == null ) {
            LOGGER.warn("Adding order for workflow: " + schedule.getWorkflowPath()
                    + " --> schedulePath: must not be null or empty.");
            return false;
        }
        if (schedule.getWorkflowName() == null || schedule.getWorkflowName().isEmpty()) {
            LOGGER.warn("Adding order: " + schedule.getPath()  + " --> workflowName: must not be null or empty.");
            return false;
        }

        return true;

    }
}
