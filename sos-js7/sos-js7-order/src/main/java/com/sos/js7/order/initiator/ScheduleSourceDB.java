package com.sos.js7.order.initiator;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        Folder f = new Folder();
        f.setFolder("/");
        f.setRecursive(true);
        dailyPlanOrderSelector.getSelector().setFolders(Collections.singletonList(f));
        dailyPlanOrderSelector.setControllerIds(Collections.singletonList(controllerId));
        fromService = false;
    }

    public ScheduleSourceDB(DailyPlanOrderSelector dailyPlanOrderSelector) {
        this.dailyPlanOrderSelector = dailyPlanOrderSelector;
        fromService = true;
    }

    @Override
    public List<Schedule> fillListOfSchedules() throws IOException, SOSHibernateException {
        FilterSchedules filterSchedules = new FilterSchedules();
        Function<String, String> pathToName = s -> Paths.get(s).getFileName().toString();

        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("ScheduleSourceDB");
            List<Schedule> listOfSchedules = new ArrayList<Schedule>();
            DBLayerSchedules dbLayerSchedules = new DBLayerSchedules(sosHibernateSession);

            filterSchedules.setListOfControllerIds(dailyPlanOrderSelector.getControllerIds());
            filterSchedules.setListOfFolders(dailyPlanOrderSelector.getSelector().getFolders());
            if (dailyPlanOrderSelector.getSelector().getWorkflowPaths() != null) {
                filterSchedules.setListOfWorkflowNames(dailyPlanOrderSelector.getSelector().getWorkflowPaths().stream().map(pathToName).distinct()
                        .collect(Collectors.toList()));
            }
            if (dailyPlanOrderSelector.getSelector().getSchedulePaths() != null) {
                filterSchedules.setListOfScheduleNames(dailyPlanOrderSelector.getSelector().getSchedulePaths().stream().map(pathToName).distinct()
                        .collect(Collectors.toList()));
            }

            List<DBItemInventoryReleasedConfiguration> listOfSchedulesDbItems = dbLayerSchedules.getSchedules(filterSchedules, 0);
            for (DBItemInventoryReleasedConfiguration dbItemInventoryConfiguration : listOfSchedulesDbItems) {
                if (dbItemInventoryConfiguration.getSchedule() != null) {
                    if (fromService || dbItemInventoryConfiguration.getSchedule().getPlanOrderAutomatically()) {
                        listOfSchedules.add(dbItemInventoryConfiguration.getSchedule());
                    }
                }
            }
            return listOfSchedules;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    @Override
    public String fromSource() {
        return "Database";
    }

}
