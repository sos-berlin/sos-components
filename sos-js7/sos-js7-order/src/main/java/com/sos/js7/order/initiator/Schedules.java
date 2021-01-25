package com.sos.js7.order.initiator;

import java.io.IOException;
import java.util.List;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.Schedule;
 
 
public class Schedules {

    private List<Schedule> listOfSchedules;

    public void fillListOfSchedules(ScheduleSource scheduleSource) throws IOException, SOSHibernateException {
        listOfSchedules = scheduleSource.fillListOfSchedules();
    }

    public List<Schedule> getListOfSchedules() {
        return listOfSchedules;
    }
}
