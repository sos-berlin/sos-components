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
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.model.dailyplan.CyclicOrderInfos;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.dailyplan.DailyPlanOrderState;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;
import com.sos.joc.model.dailyplan.Period;
import com.sos.joc.model.dailyplan.PlannedOrderItem;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.classes.CycleOrderKey;
import com.sos.js7.order.initiator.classes.GlobalSettingsReader;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;

public class JOCOrderResourceImpl extends JOCResourceImpl {

    protected OrderInitiatorSettings settings;
    private static final Logger LOGGER = LoggerFactory.getLogger(JOCOrderResourceImpl.class);

    protected void setSettings() {
        if (Globals.configurationGlobals == null) {
            settings = new OrderInitiatorSettings();
            settings.setTimeZone("Etc/UTC");
            settings.setPeriodBegin("00:00");
            LOGGER.warn("Could not read settings. Using defaults");
        } else {
            GlobalSettingsReader reader = new GlobalSettingsReader();
            AConfigurationSection section = Globals.configurationGlobals.getConfigurationSection(DefaultSections.dailyplan);
            this.settings = reader.getSettings(section);
        }
    }

    protected FilterDailyPlannedOrders getOrderFilter(String controllerId, DailyPlanOrderFilter dailyPlanOrderFilter, boolean selectCyclicOrders)
            throws SOSHibernateException {
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();

        FolderPermissionEvaluator folderPermissionEvaluator = new FolderPermissionEvaluator();

        folderPermissionEvaluator.setListOfScheduleFolders(dailyPlanOrderFilter.getFilter().getScheduleFolders());
        folderPermissionEvaluator.setListOfSchedulePaths(dailyPlanOrderFilter.getFilter().getSchedulePaths());
        folderPermissionEvaluator.setListOfWorkflowFolders(dailyPlanOrderFilter.getFilter().getWorkflowFolders());
        folderPermissionEvaluator.setListOfWorkflowPaths(dailyPlanOrderFilter.getFilter().getWorkflowPaths());

        folderPermissionEvaluator.getPermittedNames(folderPermissions, controllerId, filter);

        if (folderPermissionEvaluator.isHasPermission()) {
            if (dailyPlanOrderFilter.getFilter().getOrderIds() != null && !dailyPlanOrderFilter.getFilter().getOrderIds().isEmpty()) {

                List<String> listOfOrderIds = new ArrayList<String>();
                if (selectCyclicOrders) {
                    listOfOrderIds.addAll(dailyPlanOrderFilter.getFilter().getOrderIds());
                    for (String orderId : dailyPlanOrderFilter.getFilter().getOrderIds()) {
                        addCyclicOrderIds(listOfOrderIds, orderId, controllerId);
                    }
                } else {
                    List<String> cyclicMainParts = new ArrayList<String>();
                    for (String orderId : dailyPlanOrderFilter.getFilter().getOrderIds()) {
                        if (OrdersHelper.isCyclicOrderId(orderId)) {
                            cyclicMainParts.add(OrdersHelper.getCyclicOrderIdMainPart(orderId));
                        } else {
                            listOfOrderIds.add(orderId);
                        }
                    }
                    filter.setListOfCyclicOrdersMainParts(cyclicMainParts);
                }
                filter.setListOfOrders(listOfOrderIds);
            }

            filter.setControllerId(controllerId);
            filter.setListOfSubmissionIds(dailyPlanOrderFilter.getFilter().getSubmissionHistoryIds());
            filter.setDailyPlanDate(dailyPlanOrderFilter.getFilter().getDailyPlanDate(), settings.getTimeZone(), settings.getPeriodBegin());
            filter.setLate(dailyPlanOrderFilter.getFilter().getLate());

            if (dailyPlanOrderFilter.getFilter().getStates() != null) {
                for (DailyPlanOrderStateText state : dailyPlanOrderFilter.getFilter().getStates()) {
                    filter.addState(state);
                }
            }
        } else {
            return null;
        }
        return filter;
    }

    protected List<DBItemDailyPlanWithHistory> getOrders(SOSHibernateSession session, String controllerId, FilterDailyPlannedOrders filter,
            boolean sort) throws SOSHibernateException {

        // session.setAutoCommit(false);
        // Globals.beginTransaction(session);

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
        // Globals.commit(session);

        return result;
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
        p.setOrderName(dbItemDailyPlanWithHistory.getOrderName());

        DailyPlanOrderState orderState = new DailyPlanOrderState();
        if (dbItemDailyPlanWithHistory.isSubmitted()) {
            orderState.set_text(dbItemDailyPlanWithHistory.getStateText());
            orderState.setSeverity(OrdersHelper.severityByGroupedDailyPlanStates.get(orderState.get_text()));
        } else {
            orderState.set_text(DailyPlanOrderStateText.PLANNED);
            orderState.setSeverity(OrdersHelper.severityByGroupedDailyPlanStates.get(orderState.get_text()));
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

    protected void addOrders(SOSHibernateSession session, FilterDailyPlannedOrders filter, String controllerId,
            DailyPlanOrderFilter dailyPlanOrderFilter, List<DBItemDailyPlanWithHistory> listOfPlannedOrders,
            ArrayList<PlannedOrderItem> listOfPlannedOrderItems, boolean getCyclicDetails) {

        if (listOfPlannedOrders != null) {
            DateFormat periodFormat = new SimpleDateFormat("hh:mm:ss");

            Map<CycleOrderKey, List<PlannedOrderItem>> mapOfCycledOrders = new TreeMap<CycleOrderKey, List<PlannedOrderItem>>();

            for (DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory : listOfPlannedOrders) {

                PlannedOrderItem p = createPlanItem(dbItemDailyPlanWithHistory);
                p.setControllerId(controllerId);

                if ((p.getStartMode() == 1 && !dailyPlanOrderFilter.getExpandCycleOrders())) {
                    CycleOrderKey cycleOrderKey = new CycleOrderKey();
                    cycleOrderKey.setPeriodBegin(periodFormat.format(p.getPeriod().getBegin()));
                    cycleOrderKey.setPeriodEnd(periodFormat.format(p.getPeriod().getEnd()));
                    cycleOrderKey.setRepeat(String.valueOf(p.getPeriod().getRepeat()));
                    cycleOrderKey.setOrderName(p.getOrderName());
                    cycleOrderKey.setWorkflowPath(p.getWorkflowPath());
                    if (mapOfCycledOrders.get(cycleOrderKey) == null) {
                        mapOfCycledOrders.put(cycleOrderKey, new ArrayList<PlannedOrderItem>());
                    }

                    mapOfCycledOrders.get(cycleOrderKey).add(p);

                } else {
                    listOfPlannedOrderItems.add(p);
                }
            }

            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);

            for (Entry<CycleOrderKey, List<PlannedOrderItem>> entry : mapOfCycledOrders.entrySet()) {
                if (entry.getValue().size() > 0) {
                    PlannedOrderItem maxPOI = entry.getValue().get(0);
                    maxPOI.setCyclicOrder(new CyclicOrderInfos());

                    Object[] minPOIinfo = null;
                    if (getCyclicDetails) {
                        try {
                            String mainOrderId = OrdersHelper.getCyclicOrderIdMainPart(maxPOI.getOrderId());
                            Date dateFrom = filter.getOrderPlannedStartFrom();
                            if (dateFrom != null && maxPOI.getState() != null) {
                                Date now = new Date();
                                if (DailyPlanOrderStateText.SUBMITTED.value().equals(maxPOI.getState().get_text().value()) && now.getTime() > dateFrom
                                        .getTime()) {
                                    dateFrom = now;
                                }
                            }
                            minPOIinfo = dbLayer.getCyclicOrderMinEntryAndCountTotal(controllerId, mainOrderId, dateFrom, filter
                                    .getOrderPlannedStartTo());
                        } catch (SOSHibernateException e) {
                            LOGGER.warn(e.toString(), e);
                        }
                    }
                    if (minPOIinfo == null) {
                        maxPOI.getCyclicOrder().setFirstOrderId(maxPOI.getOrderId());
                        maxPOI.getCyclicOrder().setFirstStart(maxPOI.getPlannedStartTime());
                        maxPOI.getCyclicOrder().setCount(entry.getValue().size());

                        PlannedOrderItem lastPOI = entry.getValue().get(entry.getValue().size() - 1);
                        maxPOI.getCyclicOrder().setLastOrderId(lastPOI.getOrderId());
                        maxPOI.getCyclicOrder().setLastStart(lastPOI.getPlannedStartTime());
                    } else {
                        maxPOI.getCyclicOrder().setFirstOrderId((String) minPOIinfo[0]);
                        maxPOI.getCyclicOrder().setFirstStart((Date) minPOIinfo[1]);
                        maxPOI.getCyclicOrder().setCount(Integer.parseInt(minPOIinfo[2].toString()));

                        maxPOI.getCyclicOrder().setLastOrderId(maxPOI.getOrderId());
                        maxPOI.getCyclicOrder().setLastStart(maxPOI.getPlannedStartTime());

                        maxPOI.setOrderId(maxPOI.getCyclicOrder().getFirstOrderId());
                    }
                    listOfPlannedOrderItems.add(maxPOI);
                }
            }
        }
    }

    protected DBItemDailyPlanOrder addCyclicOrderIds(List<String> orderIds, String orderId, String controllerId) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            // re - a new session will be opened in the dbLayerDailyPlannedOrders.addCyclicOrderIds
            // sosHibernateSession = Globals.createSosHibernateStatelessConnection("ADD_CYCLIC_ORDERS");
            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            return dbLayerDailyPlannedOrders.addCyclicOrderIds(orderIds, orderId, controllerId, settings.getTimeZone(), settings.getPeriodBegin());
        } finally {
            // Globals.disconnect(sosHibernateSession);
        }
    }
}
