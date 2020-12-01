package com.sos.js7.order.initiator;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.webservices.order.initiator.model.Schedule;
  
public abstract class ScheduleSource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ScheduleSource.class);

    public abstract List<Schedule> fillListOfSchedules() throws IOException, SOSHibernateException;
    public abstract String fromSource();

    protected boolean checkMandatory(Schedule schedule) {
        if (schedule.getPath() == null ) {
            LOGGER.warn("Adding order for controller:" + schedule.getControllerId() + " and workflow: " + schedule.getWorkflowPath()
                    + " --> scheduleName: must not be null or empty.");
            return false;
        }
        if (schedule.getWorkflowPath() == null || schedule.getWorkflowPath().isEmpty()) {
            LOGGER.warn("Adding order: " + schedule.getPath() + " for controller:" + schedule.getControllerId()
                    + " --> workflowPath: must not be null or empty.");
            return false;
        }

        if (schedule.getControllerId() == null || schedule.getControllerId().isEmpty()) {
            LOGGER.warn("Adding order: " + schedule.getPath() + " for workflow: " + schedule.getWorkflowPath()
                    + " --> JobSchedulerId: must not be null or empty.");
            return false;
        }

        return true;

    }
}
