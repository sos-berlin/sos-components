package com.sos.js7.order.initiator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.js7.order.initiator.db.DBLayerSchedules;
import com.sos.js7.order.initiator.db.FilterSchedules;
import com.sos.webservices.order.initiator.model.Schedule;

public class ScheduleSourceList extends ScheduleSource {

    private String controllerId;
    private List <String>schedules;

    public ScheduleSourceList(String controllerId,List<String> schedules) {
        super();
        this.controllerId = controllerId;
        this.schedules = schedules;
    }

    
    @Override
    public List<Schedule> fillListOfSchedules() throws IOException, SOSHibernateException {        
        List<Schedule> listOfSchedules = new ArrayList<Schedule>();
        FilterSchedules filterSchedules = new FilterSchedules();
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("ScheduleSourceDB");
        DBLayerSchedules dbLayerSchedules = new DBLayerSchedules(sosHibernateSession);

        filterSchedules.setControllerId(controllerId);
        for (String schedulePath: this.schedules) {
            filterSchedules.addSchedulePath(schedulePath);
        }

        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<DBItemInventoryConfiguration> listOfSchedulesDbItems = dbLayerSchedules.getSchedules(filterSchedules, 0);
        for (DBItemInventoryConfiguration dbItemInventoryConfiguration : listOfSchedulesDbItems) {
            // temp replace because of introducing enum for whenHoliday
            Schedule schedule = objectMapper.readValue(dbItemInventoryConfiguration.getContent().replaceAll("\"suppress\"", "\"SUPPRESS\""),
                    Schedule.class);
            schedule.setPath(dbItemInventoryConfiguration.getPath());
            if (schedule.getControllerId().equals(this.controllerId)) {
                listOfSchedules.add(schedule);
            }
        }

        return listOfSchedules;
    }

    @Override
    public String fromSource() {
        return "List";
    }

}
