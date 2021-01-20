package com.sos.js7.order.initiator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelector;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelectorDef;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.js7.order.initiator.db.DBLayerSchedules;
import com.sos.js7.order.initiator.db.FilterSchedules;
import com.sos.webservices.order.initiator.model.Schedule;

public class ScheduleSourceDB extends ScheduleSource {

    private DailyPlanOrderSelector dailyPlanOrderSelector;

    public ScheduleSourceDB(String controllerId) {
        dailyPlanOrderSelector = new DailyPlanOrderSelector();
        dailyPlanOrderSelector.setSelector(new DailyPlanOrderSelectorDef());
        dailyPlanOrderSelector.getSelector().setFolders(new ArrayList<Folder>());
        Folder f = new Folder();
        f.setFolder("/");
        f.setRecursive(true);
        dailyPlanOrderSelector.getSelector().getFolders().add(f);
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
            if (dbItemInventoryConfiguration.getSchedule() != null) {
                if (dbItemInventoryConfiguration.getSchedule().getPlanOrderAutomatically()) {
                    
                    listOfSchedules.add(dbItemInventoryConfiguration.getSchedule());
                }
            }
        }

        return listOfSchedules;
    }

    @Override
    public String fromSource() {
        return "Database";
    }

}
