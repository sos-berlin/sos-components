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
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.js7.order.initiator.db.DBLayerSchedules;
import com.sos.js7.order.initiator.db.FilterSchedules;
import com.sos.webservices.order.initiator.model.Schedule;

public class ScheduleSourceDB extends ScheduleSource {

    private DailyPlanOrderFilter dailyPlanOrderFilter;

 

    public ScheduleSourceDB(String controllerId) {
        dailyPlanOrderFilter = new DailyPlanOrderFilter();
        dailyPlanOrderFilter.setControllerIds(new ArrayList<String>());
        dailyPlanOrderFilter.getControllerIds().add(controllerId);
    }

    @Override
    public List<Schedule> fillListOfSchedules() throws IOException, SOSHibernateException {
        FilterSchedules filterSchedules = new FilterSchedules();
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("ScheduleSourceDB");
        List<Schedule> listOfSchedules = new ArrayList<Schedule>();
        DBLayerSchedules dbLayerSchedules = new DBLayerSchedules(sosHibernateSession);

        filterSchedules.setListOfControllerIds(dailyPlanOrderFilter.getControllerIds());
        filterSchedules.setListOfFolders(dailyPlanOrderFilter.getFolders());

        List<DBItemInventoryReleasedConfiguration> listOfSchedulesDbItems = dbLayerSchedules.getSchedules(filterSchedules, 0);
        for (DBItemInventoryReleasedConfiguration dbItemInventoryConfiguration : listOfSchedulesDbItems) {
            listOfSchedules.add(dbItemInventoryConfiguration.getSchedule());
        }

        return listOfSchedules;
    }

    @Override
    public String fromSource() {
        return "Database";
    }

}
