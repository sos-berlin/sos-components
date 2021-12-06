package com.sos.joc.dailyplan.common;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
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
import com.sos.joc.model.dailyplan.generate.GenerateRequest;

@Deprecated
public class ScheduleSourceDB {

    private GenerateRequest request;
    private boolean useAllSchedules;

    public ScheduleSourceDB(GenerateRequest request) {
        this.request = request;
        this.useAllSchedules = true;
    }

    public List<Schedule> getSchedules() throws IOException, SOSHibernateException {
        FilterSchedules filter = new FilterSchedules();
        Function<String, String> pathToName = s -> Paths.get(s).getFileName().toString();

        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection("ScheduleSourceDB");
            DBLayerSchedules dbLayer = new DBLayerSchedules(session);

            // filter.setFolders(selector.getFolders());
            if (request.getWorkflowPaths() != null && request.getWorkflowPaths().getSingles() != null) {
                filter.setWorkflowNames(request.getWorkflowPaths().getSingles().stream().map(pathToName).distinct().collect(Collectors.toList()));
            }
            if (request.getSchedulePaths() != null && request.getSchedulePaths().getSingles() != null) {
                filter.setScheduleNames(request.getSchedulePaths().getSingles().stream().map(pathToName).distinct().collect(Collectors.toList()));
            }
            List<DBItemInventoryReleasedConfiguration> items = dbLayer.getSchedules(filter, 0);
            session.close();
            session = null;

            List<Schedule> schedules = new ArrayList<Schedule>();
            for (DBItemInventoryReleasedConfiguration item : items) {
                if (item.getSchedule() != null) {
                    if (useAllSchedules || item.getSchedule().getPlanOrderAutomatically()) {
                        schedules.add(item.getSchedule());
                    }
                }
            }
            return schedules;
        } finally {
            Globals.disconnect(session);
        }
    }

}
