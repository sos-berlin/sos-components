package com.sos.joc.dailyplan.common;

import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.CyclicOrderInfos;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilterDef;
import com.sos.joc.model.dailyplan.DailyPlanOrderState;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;
import com.sos.joc.model.dailyplan.Period;
import com.sos.joc.model.dailyplan.PlannedOrderItem;

public class JOCOrderResourceImpl extends JOCResourceImpl {

    private static final Logger LOGGER = LoggerFactory.getLogger(JOCOrderResourceImpl.class);
    private DailyPlanSettings settings;

    public static DailyPlanSettings getDailyPlanSettings() {
        DailyPlanSettings settings = new DailyPlanSettings();
        if (Globals.configurationGlobals == null) {// TODO to remove
            settings.setTimeZone("Etc/UTC");
            settings.setPeriodBegin("00:00");
            settings.setDayAheadPlan(7);
            settings.setDayAheadSubmit(3);
            LOGGER.warn("Could not read settings. Using defaults");
        } else {
            AConfigurationSection section = Globals.configurationGlobals.getConfigurationSection(DefaultSections.dailyplan);
            settings = new GlobalSettingsReader().getSettings(section);
        }
        return settings;
    }
    
    public ZoneId getZoneId() {
       if (settings == null) {
           setSettings();
       }
       return ZoneId.of(settings.getTimeZone());
    }

    protected void setSettings() {
        this.settings = getDailyPlanSettings();
    }

    protected DailyPlanSettings getSettings() {
        return settings;
    }

    protected FilterDailyPlannedOrders getOrderFilter(String caller, String controllerId, DailyPlanOrderFilterDef in, boolean selectCyclicOrders)
            throws SOSHibernateException {
        return getOrderFilter(caller, controllerId, in, selectCyclicOrders, true);
    }

    
    protected FilterDailyPlannedOrders getOrderFilter(String caller, String controllerId, DailyPlanOrderFilterDef in, boolean selectCyclicOrders,
            boolean evalPermissions) throws SOSHibernateException {
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
        
        boolean hasPermission = true;

        if(!evalPermissions) {
            if(in.getSchedulePaths() != null && !in.getSchedulePaths().isEmpty()) {
                filter.setScheduleNames(in.getSchedulePaths().stream().map(JocInventory::pathToName).collect(Collectors.toList()));
            }
            if(in.getScheduleFolders() != null && !in.getScheduleFolders().isEmpty()) {
                Set<Folder> permitted = addPermittedFolder(in.getScheduleFolders(), folderPermissions);
                if (permitted.isEmpty()) {
                    // hasPermission = false; //maybe the schedules were deleted
                } else {
                    filter.addScheduleFolders(permitted);
                }
            }
            if(in.getWorkflowFolders() != null && !in.getWorkflowFolders().isEmpty()) {
                Set<Folder> permitted = addPermittedFolder(in.getWorkflowFolders(), folderPermissions);
                if (permitted.isEmpty()) {
                    // hasPermission = false;
                } else {
                    filter.addWorkflowFolders(permitted);
                }
            }
            if(in.getWorkflowPaths() != null && !in.getWorkflowPaths().isEmpty()) {
                filter.setWorkflowNames(in.getWorkflowPaths().stream().map(JocInventory::pathToName).collect(Collectors.toList()));
            }
        } else {
            FolderPermissionEvaluator evaluator = new FolderPermissionEvaluator();

            evaluator.setScheduleFolders(in.getScheduleFolders());
            evaluator.setSchedulePaths(in.getSchedulePaths());
            evaluator.setWorkflowFolders(in.getWorkflowFolders());
            evaluator.setWorkflowPaths(in.getWorkflowPaths());
            
            evaluator.getPermittedNames(folderPermissions, controllerId, filter);
            hasPermission = evaluator.isHasPermission();
        }
        if (hasPermission) {
            if (in.getOrderIds() != null && !in.getOrderIds().isEmpty()) {
                Set<String> orderIds = new HashSet<>();
                if (selectCyclicOrders) {
                    orderIds.addAll(in.getOrderIds());
                    for (String orderId : in.getOrderIds()) {
                        addCyclicOrderIds(orderIds, orderId, controllerId);
                    }
                } else {
                    List<String> cyclicMainParts = new ArrayList<>();
                    for (String orderId : in.getOrderIds()) {
                        if (OrdersHelper.isCyclicOrderId(orderId)) {
                            cyclicMainParts.add(OrdersHelper.getCyclicOrderIdMainPart(orderId));
                        } else {
                            orderIds.add(orderId);
                        }
                    }
                    filter.setCyclicOrdersMainParts(cyclicMainParts);
                }
                filter.setOrderIds(orderIds);
            }

            filter.setControllerId(controllerId);
            filter.setSubmissionIds(in.getSubmissionHistoryIds());
            filter.setDailyPlanInterval(in.getDailyPlanDateFrom(), in.getDailyPlanDateTo(), settings.getTimeZone(), settings.getPeriodBegin());
            filter.setLate(in.getLate());
            filter.setStates(in.getStates());
        } else {
            LOGGER.info(String.format("[%s][getOrderFilter][%s]missing permissions", caller, controllerId));
            return null;
        }
        return filter;
    }

    protected List<DBItemDailyPlanWithHistory> getOrders(SOSHibernateSession session, FilterDailyPlannedOrders filter, boolean sort)
            throws SOSHibernateException {

        DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
        List<DBItemDailyPlanWithHistory> result = null;

        if (filter != null) {
            // filter.setOrderCriteria("plannedStart");
            filter.setOrderCriteria(null);
            filter.setSingleStart();
            result = dbLayer.getDailyPlanWithHistoryList(filter, 0);
            filter.setCyclicStart();
            result.addAll(dbLayer.getDailyPlanWithHistoryList(filter, 0));
            if (sort) {
                // sort on client
                // result.sort((o1, o2) -> o1.getPlannedStart().compareTo(o2.getPlannedStart()));
            }
        }
        return result;
    }

    protected PlannedOrderItem createPlanItem(DBItemDailyPlanWithHistory item) {

        PlannedOrderItem p = new PlannedOrderItem();
        p.setLate(item.isLate());
        Period period = new Period();
        period.setBegin(item.getPeriodBegin());
        period.setEnd(item.getPeriodEnd());
        period.setRepeat(item.getRepeatInterval());
        p.setPeriod(period);

        p.setPlannedStartTime(item.getPlannedStart());
        p.setExpectedEndTime(item.getExpectedEnd());
        p.setLate(item.isLate());
        p.setSurveyDate(item.getPlannedOrderCreated());

        p.setStartMode(item.getStartMode());

        p.setWorkflowPath(item.getWorkflowPath());
        p.setOrderId(item.getOrderId());
        p.setSchedulePath(item.getSchedulePath());
        p.setOrderName(item.getOrderName());

        DailyPlanOrderState orderState = new DailyPlanOrderState();
        if (item.isSubmitted()) {
            orderState.set_text(item.getStateText());
            orderState.setSeverity(OrdersHelper.severityByGroupedDailyPlanStates.get(orderState.get_text()));
        } else {
            orderState.set_text(DailyPlanOrderStateText.PLANNED);
            orderState.setSeverity(OrdersHelper.severityByGroupedDailyPlanStates.get(orderState.get_text()));
        }
        p.setState(orderState);

        if (item.getOrderHistoryId() != null) {
            if (item.getStartTime().after(new Date(0L))) {
                p.setStartTime(item.getStartTime());
                p.setEndTime(item.getEndTime());
            }
            p.setHistoryId(String.valueOf(item.getOrderHistoryId()));
        }
        return p;

    }

    protected void addOrders(SOSHibernateSession session, String controllerId, Date plannedStartFrom, Date plannedStartTo, DailyPlanOrderFilterDef in,
            List<DBItemDailyPlanWithHistory> orders, List<PlannedOrderItem> result, boolean getCyclicDetails) {

        if (orders != null) {
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            for (DBItemDailyPlanWithHistory item : orders) {
                PlannedOrderItem p = createPlanItem(item);
                p.setControllerId(controllerId);

                if ((p.getStartMode().equals(DBLayerDailyPlannedOrders.START_MODE_CYCLIC) && !in.getExpandCycleOrders())) {
                    result.add(getCyclicPlannedOrder(dbLayer, plannedStartFrom, plannedStartTo, p, getCyclicDetails));
                } else {
                    result.add(p);
                }
            }
        }
    }

    private PlannedOrderItem getCyclicPlannedOrder(DBLayerDailyPlannedOrders dbLayer, Date plannedStartFrom, Date plannedStartTo,
            PlannedOrderItem item, boolean getCyclicDetails) {
        // item is a max item in the cycle group
        item.setCyclicOrder(new CyclicOrderInfos());

        Object[] minIteminfo = null;
        if (getCyclicDetails) {
            try {
                String mainOrderId = OrdersHelper.getCyclicOrderIdMainPart(item.getOrderId());
                Date dateFrom = null;
                if (plannedStartFrom != null && item.getState() != null) {
                    Date now = new Date();
                    if (DailyPlanOrderStateText.SUBMITTED.value().equals(item.getState().get_text().value()) && now.getTime() > plannedStartFrom
                            .getTime()) {
                        dateFrom = now;
                    }
                }
                // minIteminfo = dbLayer.getCyclicOrderMinEntryAndCountTotal(item.getControllerId(), mainOrderId, dateFrom, plannedStartTo);
                minIteminfo = dbLayer.getCyclicOrderMinEntryAndCountTotal(item.getControllerId(), mainOrderId, dateFrom, null);
            } catch (SOSHibernateException e) {
                LOGGER.warn(e.toString(), e);
            }
        }
        if (minIteminfo == null) {
            item.getCyclicOrder().setCount(1);

            item.getCyclicOrder().setFirstOrderId(item.getOrderId());
            item.getCyclicOrder().setFirstStart(item.getPlannedStartTime());

            item.getCyclicOrder().setLastOrderId(item.getOrderId());
            item.getCyclicOrder().setLastStart(item.getPlannedStartTime());
        } else {
            item.getCyclicOrder().setCount(Integer.parseInt(minIteminfo[0].toString()));

            item.getCyclicOrder().setFirstOrderId((String) minIteminfo[1]);
            item.getCyclicOrder().setFirstStart((Date) minIteminfo[2]);

            item.getCyclicOrder().setLastOrderId(item.getOrderId());
            item.getCyclicOrder().setLastStart(item.getPlannedStartTime());

            item.setOrderId(item.getCyclicOrder().getFirstOrderId());
            item.setPlannedStartTime(item.getCyclicOrder().getFirstStart());
            item.setExpectedEndTime((Date) minIteminfo[3]);

            String state = item.getState().get_text().value();
            if (DailyPlanOrderStateText.PLANNED.value().equals(state) && !item.getLate() && new Date().getTime() > item.getCyclicOrder()
                    .getFirstStart().getTime()) {
                item.setLate(true);
            }
        }
        return item;
    }

    protected Date getCyclicMinPlannedStart(SOSHibernateSession session, String controllerId, Date plannedStartFrom, Date plannedStartTo,
            String orderId) throws Exception {
        DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
        return dbLayer.getCyclicMinPlannedStart(controllerId, OrdersHelper.getCyclicOrderIdMainPart(orderId), plannedStartFrom, plannedStartTo);
    }

    protected DBItemDailyPlanOrder addCyclicOrderIds(Set<String> orderIds, String orderId, String controllerId) throws SOSHibernateException {
        // re - a new session will be opened in the dbLayerDailyPlannedOrders.addCyclicOrderIds
        DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(null);
        return dbLayer.addCyclicOrderIds(orderIds, orderId, controllerId, settings.getTimeZone(), settings.getPeriodBegin());

    }

    protected Date toUTCDate(String date) {
        if (date == null) {
            return null;
        }
        if (date.length() == 10) {
            date = date + "T00:00:00Z";
        } else if (date.equals("0")) {
            date = "0d";
        }
        return JobSchedulerDate.getDateFrom(date, "UTC");
    }
}
