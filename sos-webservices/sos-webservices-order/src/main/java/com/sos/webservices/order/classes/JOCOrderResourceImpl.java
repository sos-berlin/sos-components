package com.sos.webservices.order.classes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.model.dailyplan.CyclicOrderInfos;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.dailyplan.Period;
import com.sos.joc.model.dailyplan.PlannedOrderItem;
import com.sos.joc.model.order.OrderState;
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.classes.CycleOrderKey;
import com.sos.js7.order.initiator.classes.GlobalSettingsReader;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.webservices.order.impl.CyclicOrdersImpl;

public class JOCOrderResourceImpl extends JOCResourceImpl {

    protected OrderInitiatorSettings settings;
    private static final Logger LOGGER = LoggerFactory.getLogger(CyclicOrdersImpl.class);
    protected boolean stateFilterContainsPendingOrPlanned;

    protected void setSettings() {
        if (Globals.configurationGlobals == null) {
            settings = new OrderInitiatorSettings();
            settings.setTimeZone("Europe/Berlin");
            settings.setPeriodBegin("00:00");
            LOGGER.warn("Could not read settings. Using defaults");
        } else {
            GlobalSettingsReader reader = new GlobalSettingsReader();
            AConfigurationSection section = Globals.configurationGlobals.getConfigurationSection(DefaultSections.dailyplan);
            this.settings = reader.getSettings(section);
        }
    }

    protected FilterDailyPlannedOrders getOrderFilter(String controllerId, DailyPlanOrderFilter dailyPlanOrderFilter) throws SOSHibernateException {
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();

        FolderPermissionEvaluator folderPermissionEvaluator = new FolderPermissionEvaluator();

        folderPermissionEvaluator.setListOfScheduleFolders(dailyPlanOrderFilter.getFilter().getScheduleFolders());
        folderPermissionEvaluator.setListOfScheduleNames(dailyPlanOrderFilter.getFilter().getScheduleNames());
        folderPermissionEvaluator.setListOfSchedulePaths(dailyPlanOrderFilter.getFilter().getSchedulePaths());
        folderPermissionEvaluator.setListOfWorkflowFolders(dailyPlanOrderFilter.getFilter().getWorkflowFolders());
        folderPermissionEvaluator.setListOfWorkflowNames(dailyPlanOrderFilter.getFilter().getWorkflowNames());
        folderPermissionEvaluator.setListOfWorkflowPaths(dailyPlanOrderFilter.getFilter().getWorkflowPaths());

        folderPermissionEvaluator.getPermittedNames(folderPermissions, controllerId, filter);

        List<String> listOfOrderIds = null;
        if (folderPermissionEvaluator.isHasPermission()) {
            if (dailyPlanOrderFilter.getFilter().getOrderIds() != null && !dailyPlanOrderFilter.getFilter().getOrderIds().isEmpty()) {

                listOfOrderIds = new ArrayList<String>();
                for (String orderId : dailyPlanOrderFilter.getFilter().getOrderIds()) {
                    listOfOrderIds.add(orderId);
                }

                for (String orderId : dailyPlanOrderFilter.getFilter().getOrderIds()) {
                    addCyclicOrderIds(listOfOrderIds, orderId, controllerId);
                }
            }            
            
            filter.setControllerId(controllerId);
            filter.setListOfSubmissionIds(dailyPlanOrderFilter.getFilter().getSubmissionHistoryIds());
            filter.setListOfOrders(listOfOrderIds);
            filter.setDailyPlanDate(dailyPlanOrderFilter.getFilter().getDailyPlanDate(), settings.getTimeZone(), settings.getPeriodBegin());
            filter.setLate(dailyPlanOrderFilter.getFilter().getLate());

            stateFilterContainsPendingOrPlanned = false;
            if (dailyPlanOrderFilter.getFilter().getStates() != null) {
                for (OrderStateText state : dailyPlanOrderFilter.getFilter().getStates()) {
                    if (state.equals(OrderStateText.PENDING) || state.equals(OrderStateText.SCHEDULED) || state.equals(OrderStateText.PLANNED)) {
                        stateFilterContainsPendingOrPlanned = true;
                    }
                    filter.addState(state);
                }
            }
        } else {
            return null;
        }
        return filter;
    }

    protected List<DBItemDailyPlanWithHistory> getOrders(SOSHibernateSession sosHibernateSession, String controllerId,
            DailyPlanOrderFilter dailyPlanOrderFilter) throws SOSHibernateException {

        sosHibernateSession.setAutoCommit(false);
        Globals.beginTransaction(sosHibernateSession);
        FilterDailyPlannedOrders filter = getOrderFilter(controllerId, dailyPlanOrderFilter);
        DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
        List<DBItemDailyPlanWithHistory> listOfPlannedOrders = null;

        if (filter != null) {
            listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanWithHistoryList(filter, 0);
        }
        Globals.commit(sosHibernateSession);

        return listOfPlannedOrders;
    }

    protected List<String> getAllowedControllersOrdersView(String controllerId, List<String> controllerIds, String accessToken) {
        if (controllerIds == null) {
            controllerIds = new ArrayList<String>();
            controllerIds.add(controllerId);
        } else {
            if (controllerId != null && !controllerIds.contains(controllerId)) {
                controllerIds.add(controllerId);
            }
        }

        return controllerIds;
    }

    protected PlannedOrderItem createPlanItem(DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory) {

        PlannedOrderItem p = new PlannedOrderItem();
        p.setLate(dbItemDailyPlanWithHistory.isLate());
        Period period = new Period();
        period.setBegin(dbItemDailyPlanWithHistory.getPeriodBegin());
        period.setEnd(dbItemDailyPlanWithHistory.getPeriodEnd());
        period.setRepeat(dbItemDailyPlanWithHistory.getRepeatInterval());
        p.setPeriod(period);

        p.setPlannedStartTime(dbItemDailyPlanWithHistory.getPlannedStart());
        p.setExpectedEndTime(dbItemDailyPlanWithHistory.getExpectedEnd());
        p.setLate(dbItemDailyPlanWithHistory.isLate());
        p.setSurveyDate(dbItemDailyPlanWithHistory.getPlannedOrderCreated());

        p.setStartMode(dbItemDailyPlanWithHistory.getStartMode());

        p.setWorkflowPath(dbItemDailyPlanWithHistory.getWorkflowPath());
        p.setOrderId(dbItemDailyPlanWithHistory.getOrderId());
        p.setSchedulePath(dbItemDailyPlanWithHistory.getSchedulePath());

        OrderState orderState = new OrderState();
        if (dbItemDailyPlanWithHistory.isSubmitted()) {
            orderState.set_text(dbItemDailyPlanWithHistory.getStateText());
            orderState.setSeverity(OrdersHelper.severityByGroupedStates.get(orderState.get_text()));
        } else {
            orderState.set_text(OrderStateText.PLANNED);
            orderState.setSeverity(OrdersHelper.severityByGroupedStates.get(orderState.get_text()));
        }
        p.setState(orderState);

        if (dbItemDailyPlanWithHistory.getOrderHistoryId() != null) {
            if (dbItemDailyPlanWithHistory.getStartTime().after(new Date(0L))) {
                p.setStartTime(dbItemDailyPlanWithHistory.getStartTime());
                p.setEndTime(dbItemDailyPlanWithHistory.getEndTime());
            }
            p.setHistoryId(String.valueOf(dbItemDailyPlanWithHistory.getOrderHistoryId()));
        }
        return p;

    }

    protected void addOrders(String controllerId, DailyPlanOrderFilter dailyPlanOrderFilter, List<DBItemDailyPlanWithHistory> listOfPlannedOrders,
            ArrayList<PlannedOrderItem> listOfPlannedOrderItems) {

        if (listOfPlannedOrders != null) {
            DateFormat periodFormat = new SimpleDateFormat("hh:mm:ss");

            Map<CycleOrderKey, List<PlannedOrderItem>> mapOfCycledOrders = new TreeMap<CycleOrderKey, List<PlannedOrderItem>>();

            for (DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory : listOfPlannedOrders) {

                boolean add = true;
                PlannedOrderItem p = createPlanItem(dbItemDailyPlanWithHistory);
                p.setControllerId(controllerId);

                if (dailyPlanOrderFilter.getFilter().getStates() != null && !stateFilterContainsPendingOrPlanned && dbItemDailyPlanWithHistory
                        .getOrderHistoryId() == null) {
                    add = false;
                }

                if (add) {

                    if ((p.getStartMode() == 1 && !dailyPlanOrderFilter.getExpandCycleOrders())) {
                        CycleOrderKey cycleOrderKey = new CycleOrderKey();
                        cycleOrderKey.setPeriodBegin(periodFormat.format(p.getPeriod().getBegin()));
                        cycleOrderKey.setPeriodEnd(periodFormat.format(p.getPeriod().getEnd()));
                        cycleOrderKey.setRepeat(String.valueOf(p.getPeriod().getRepeat()));
                        cycleOrderKey.setSchedulePath(p.getSchedulePath());
                        cycleOrderKey.setWorkflowPath(p.getWorkflowPath());
                        if (mapOfCycledOrders.get(cycleOrderKey) == null) {
                            mapOfCycledOrders.put(cycleOrderKey, new ArrayList<PlannedOrderItem>());
                        }

                        mapOfCycledOrders.get(cycleOrderKey).add(p);

                    } else {
                        listOfPlannedOrderItems.add(p);
                    }
                }
            }

            for (Entry<CycleOrderKey, List<PlannedOrderItem>> entry : mapOfCycledOrders.entrySet()) {
                if (entry.getValue().size() > 0) {
                    PlannedOrderItem firstP = entry.getValue().get(0);
                    PlannedOrderItem lastP = entry.getValue().get(entry.getValue().size() - 1);
                    firstP.setCyclicOrder(new CyclicOrderInfos());
                    firstP.getCyclicOrder().setCount(entry.getValue().size());
                    firstP.getCyclicOrder().setFirstOrderId(firstP.getOrderId());
                    firstP.getCyclicOrder().setFirstStart(firstP.getPlannedStartTime());
                    firstP.getCyclicOrder().setLastOrderId(lastP.getOrderId());
                    firstP.getCyclicOrder().setLastStart(lastP.getPlannedStartTime());
                    listOfPlannedOrderItems.add(firstP);
                }
            }
        }
    }
    
    protected void addCyclicOrderIds(List<String> orderIds, String orderId, String controllerId) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("ADD_CYVLIC_ORDERS");
            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            dbLayerDailyPlannedOrders.addCyclicOrderIds(orderIds, orderId, controllerId, settings.getTimeZone(), settings
                    .getPeriodBegin());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }
}
