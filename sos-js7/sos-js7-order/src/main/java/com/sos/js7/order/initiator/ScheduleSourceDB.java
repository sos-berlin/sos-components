package com.sos.js7.order.initiator;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelector;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelectorDef;
import com.sos.js7.order.initiator.db.DBLayerSchedules;
import com.sos.js7.order.initiator.db.FilterSchedules;

public class ScheduleSourceDB extends ScheduleSource {

    private DailyPlanOrderSelector dailyPlanOrderSelector;
    private Boolean fromService;

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
        fromService = false;
    }

    public ScheduleSourceDB(DailyPlanOrderSelector dailyPlanOrderSelector) {
        this.dailyPlanOrderSelector = dailyPlanOrderSelector;
        fromService = true;
    }

    @Override
    public List<Schedule> fillListOfSchedules() throws IOException, SOSHibernateException {
        FilterSchedules filterSchedules = new FilterSchedules();
        
        if (dailyPlanOrderSelector.getSelector().getSchedulePaths() != null) {
            if (dailyPlanOrderSelector.getSelector().getScheduleNames() == null) {
                dailyPlanOrderSelector.getSelector().setScheduleNames(new ArrayList<String>());
            }
            for (String path : dailyPlanOrderSelector.getSelector().getSchedulePaths()) {
                String name = Paths.get(path).getFileName().toString();
                dailyPlanOrderSelector.getSelector().getScheduleNames().add(name);
            }
        }
        if (dailyPlanOrderSelector.getSelector().getWorkflowPaths() != null) {
            if (dailyPlanOrderSelector.getSelector().getWorkflowNames() == null) {
                dailyPlanOrderSelector.getSelector().setWorkflowNames(new ArrayList<String>());
            }

            for (String path : dailyPlanOrderSelector.getSelector().getWorkflowPaths()) {
                String name = Paths.get(path).getFileName().toString();
                dailyPlanOrderSelector.getSelector().getWorkflowNames().add(name);
            }
        }        
        
        SOSHibernateSession sosHibernateSession = Globals.createSosHibernateStatelessConnection("ScheduleSourceDB");
        List<Schedule> listOfSchedules = new ArrayList<Schedule>();
        DBLayerSchedules dbLayerSchedules = new DBLayerSchedules(sosHibernateSession);
         
        filterSchedules.setListOfControllerIds(dailyPlanOrderSelector.getControllerIds());
        filterSchedules.setListOfFolders(dailyPlanOrderSelector.getSelector().getFolders());
        filterSchedules.setListOfWorkflowNames(dailyPlanOrderSelector.getSelector().getWorkflowNames());
        filterSchedules.setListOfScheduleNames(dailyPlanOrderSelector.getSelector().getScheduleNames());
        

        List<DBItemInventoryReleasedConfiguration> listOfSchedulesDbItems = dbLayerSchedules.getSchedules(filterSchedules, 0);
        for (DBItemInventoryReleasedConfiguration dbItemInventoryConfiguration : listOfSchedulesDbItems) {
            if (dbItemInventoryConfiguration.getSchedule() != null) {
                if (fromService || dbItemInventoryConfiguration.getSchedule().getPlanOrderAutomatically()) {
                    
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
