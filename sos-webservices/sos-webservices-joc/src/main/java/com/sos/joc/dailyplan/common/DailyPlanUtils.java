package com.sos.joc.dailyplan.common;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;

public class DailyPlanUtils {

    public static Map<String, List<DBItemDailyPlanOrder>> getOrderIdsFromDailyplanDate(DailyPlanOrderFilterDef in,
            DailyPlanSettings settings, boolean submitted, String API_CALL) throws SOSHibernateException {
        SOSHibernateSession session = null;
        try {
            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerIds(in.getControllerIds());
            filter.setOrderIds(in.getOrderIds());
            if (in.getSchedulePaths() != null) {
                filter.setScheduleNames(in.getSchedulePaths().stream().map(path -> JocInventory.pathToName(path)).distinct().collect(Collectors
                        .toList()));
            }
            if (in.getWorkflowPaths() != null) {
                filter.setWorkflowNames(in.getWorkflowPaths().stream().map(path -> JocInventory.pathToName(path)).distinct().collect(Collectors
                        .toList()));
            }
            filter.setScheduleFolders(in.getScheduleFolders());
            filter.setWorkflowFolders(in.getWorkflowFolders());
            filter.setSubmitted(submitted);
            
            // TODO not planned start time is relevant
            //filter.setDailyPlanInterval(in.getDailyPlanDateFrom(), in.getDailyPlanDateTo(), settings.getTimeZone(), settings.getPeriodBegin());
            // instead join to submissions
            filter.setSubmissionForDateFrom(JobSchedulerDate.getDateFrom(in.getDailyPlanDateFrom() + "T00:00:00Z", "UTC"));
            filter.setSubmissionForDateTo(JobSchedulerDate.getDateFrom(in.getDailyPlanDateTo() + "T00:00:00Z", "UTC"));

            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            return dbLayer.getDailyPlanList(filter, 0).stream().collect(Collectors.groupingBy(DBItemDailyPlanOrder::getControllerId));
        } finally {
            Globals.disconnect(session);
        }
    }
}
