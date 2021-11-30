package com.sos.joc.dailyplan.common;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.joc.Globals;
import com.sos.joc.dailyplan.db.DBLayerSchedules;
import com.sos.joc.dailyplan.db.FilterSchedules;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelector;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelectorDef;

public class ScheduleSourceDB extends ScheduleSource {

    private DailyPlanOrderSelector selector;
    private Boolean fromService;

    public ScheduleSourceDB(String controllerId) {
        Folder f = new Folder();
        f.setFolder("/");
        f.setRecursive(true);

        selector = new DailyPlanOrderSelector();
        selector.setSelector(new DailyPlanOrderSelectorDef());
        selector.getSelector().setFolders(Collections.singletonList(f));
        selector.setControllerIds(Collections.singletonList(controllerId));
        fromService = false;
    }

    public ScheduleSourceDB(DailyPlanOrderSelector selector) {
        this.selector = selector;
        this.fromService = true;
    }

    @Override
    public List<Schedule> getSchedules() throws IOException, SOSHibernateException {
        FilterSchedules filter = new FilterSchedules();
        Function<String, String> pathToName = s -> Paths.get(s).getFileName().toString();

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("ScheduleSourceDB");
            DBLayerSchedules dbLayer = new DBLayerSchedules(session);

            filter.setControllerIds(selector.getControllerIds());
            filter.setFolders(selector.getSelector().getFolders());
            if (selector.getSelector().getWorkflowPaths() != null) {
                filter.setWorkflowNames(selector.getSelector().getWorkflowPaths().stream().map(pathToName).distinct().collect(Collectors.toList()));
            }
            if (selector.getSelector().getSchedulePaths() != null) {
                filter.setScheduleNames(selector.getSelector().getSchedulePaths().stream().map(pathToName).distinct().collect(Collectors.toList()));
            }
            List<DBItemInventoryReleasedConfiguration> items = dbLayer.getSchedules(filter, 0);
            session.close();
            session = null;

            List<Schedule> schedules = new ArrayList<Schedule>();
            for (DBItemInventoryReleasedConfiguration item : items) {
                if (item.getSchedule() != null) {
                    if (fromService || item.getSchedule().getPlanOrderAutomatically()) {
                        schedules.add(item.getSchedule());
                    }
                }
            }
            return schedules;
        } finally {
            Globals.disconnect(session);
        }
    }

    @Override
    public String getSource() {
        return "Database";
    }

}
