package com.sos.js7.order.initiator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelector;
import com.sos.js7.order.initiator.db.DBLayerSchedules;
import com.sos.js7.order.initiator.db.FilterSchedules;
import com.sos.webservices.order.initiator.model.Schedule;

public class ScheduleSourceDB extends ScheduleSource {

    private DailyPlanOrderSelector dailyPlanOrderSelector;

    public ScheduleSourceDB(String controllerId) {
        dailyPlanOrderSelector = new DailyPlanOrderSelector();
        dailyPlanOrderSelector.setControllerIds(new ArrayList<String>());
        dailyPlanOrderSelector.getControllerIds().add(controllerId);
    }

    public ScheduleSourceDB(DailyPlanOrderSelector dailyPlanOrderSelector) {
        this.dailyPlanOrderSelector = dailyPlanOrderSelector;
    }

    @Override
    public List<Schedule> fillListOfSchedules() throws IOException, SOSHibernateException {
        FilterSchedules filterSchedules = new FilterSchedules();
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("ScheduleSourceDB");
        List<Schedule> listOfSchedules = new ArrayList<Schedule>();
        DBLayerSchedules dbLayerSchedules = new DBLayerSchedules(sosHibernateSession);

        filterSchedules.setListOfControllerIds(dailyPlanOrderSelector.getControllerIds());
        filterSchedules.setListOfFolders(dailyPlanOrderSelector.getSelector().getFolders());
        filterSchedules.setListOfWorkflowPaths(dailyPlanOrderSelector.getSelector().getWorkflowPaths());
        filterSchedules.setListOfSchedules(dailyPlanOrderSelector.getSelector().getSchedulePaths());

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
