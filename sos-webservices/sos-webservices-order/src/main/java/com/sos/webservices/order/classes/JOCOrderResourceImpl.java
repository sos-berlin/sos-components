package com.sos.webservices.order.classes;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    protected void addOrders(SOSHibernateSession session, FilterDailyPlannedOrders filter, String controllerId,
            DailyPlanOrderFilter dailyPlanOrderFilter, List<DBItemDailyPlanWithHistory> orders, ArrayList<PlannedOrderItem> result,
            boolean getCyclicDetails) {

        if (orders != null) {
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            for (DBItemDailyPlanWithHistory item : orders) {
                PlannedOrderItem p = createPlanItem(item);
                p.setControllerId(controllerId);

                if ((p.getStartMode() == 1 && !dailyPlanOrderFilter.getExpandCycleOrders())) {
                    result.add(getCyclicPlannedOrder(dbLayer, filter, p, getCyclicDetails));
                } else {
                    result.add(p);
                }
            }
        }
    }

    private PlannedOrderItem getCyclicPlannedOrder(DBLayerDailyPlannedOrders dbLayer, FilterDailyPlannedOrders filter, PlannedOrderItem item,
            boolean getCyclicDetails) {
        // item is a max item in the cycle group
        item.setCyclicOrder(new CyclicOrderInfos());

        Object[] minIteminfo = null;
        if (getCyclicDetails) {
            try {
                String mainOrderId = OrdersHelper.getCyclicOrderIdMainPart(item.getOrderId());
                Date dateFrom = filter.getOrderPlannedStartFrom();
                if (dateFrom != null && item.getState() != null) {
                    Date now = new Date();
                    if (DailyPlanOrderStateText.SUBMITTED.value().equals(item.getState().get_text().value()) && now.getTime() > dateFrom.getTime()) {
                        dateFrom = now;
                    }
                }
                minIteminfo = dbLayer.getCyclicOrderMinEntryAndCountTotal(item.getControllerId(), mainOrderId, dateFrom, filter
                        .getOrderPlannedStartTo());
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

    protected Date getCyclicMinPlannedStart(SOSHibernateSession session, FilterDailyPlannedOrders filter, String orderId, String controllerId)
            throws Exception {
        DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
        return dbLayer.getCyclicMinPlannedStart(controllerId, OrdersHelper.getCyclicOrderIdMainPart(orderId), filter.getOrderPlannedStartFrom(),
                filter.getOrderPlannedStartTo());
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
