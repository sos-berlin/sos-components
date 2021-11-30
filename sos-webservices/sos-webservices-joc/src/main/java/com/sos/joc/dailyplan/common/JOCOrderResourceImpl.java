package com.sos.joc.dailyplan.common;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.dailyplan.DBItemDailyPlanWithHistory;
import com.sos.joc.model.dailyplan.CyclicOrderInfos;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.dailyplan.DailyPlanOrderState;
import com.sos.joc.model.dailyplan.DailyPlanOrderStateText;
import com.sos.joc.model.dailyplan.Period;
import com.sos.joc.model.dailyplan.PlannedOrderItem;

public class JOCOrderResourceImpl extends JOCResourceImpl {

    protected DailyPlanSettings settings;
    private static final Logger LOGGER = LoggerFactory.getLogger(JOCOrderResourceImpl.class);

    protected void setSettings() {
        if (Globals.configurationGlobals == null) {
            settings = new DailyPlanSettings();
            settings.setTimeZone("Etc/UTC");
            settings.setPeriodBegin("00:00");
            LOGGER.warn("Could not read settings. Using defaults");
        } else {
            GlobalSettingsReader reader = new GlobalSettingsReader();
            AConfigurationSection section = Globals.configurationGlobals.getConfigurationSection(DefaultSections.dailyplan);
            this.settings = reader.getSettings(section);
        }
    }

    protected FilterDailyPlannedOrders getOrderFilter(String caller, String controllerId, DailyPlanOrderFilter in, boolean selectCyclicOrders)
            throws SOSHibernateException {
        FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();

        FolderPermissionEvaluator evaluator = new FolderPermissionEvaluator();

        evaluator.setScheduleFolders(in.getFilter().getScheduleFolders());
        evaluator.setSchedulePaths(in.getFilter().getSchedulePaths());
        evaluator.setWorkflowFolders(in.getFilter().getWorkflowFolders());
        evaluator.setWorkflowPaths(in.getFilter().getWorkflowPaths());

        evaluator.getPermittedNames(folderPermissions, controllerId, filter);

        if (evaluator.isHasPermission()) {
            if (in.getFilter().getOrderIds() != null && !in.getFilter().getOrderIds().isEmpty()) {
                List<String> orderIds = new ArrayList<String>();
                if (selectCyclicOrders) {
                    orderIds.addAll(in.getFilter().getOrderIds());
                    for (String orderId : in.getFilter().getOrderIds()) {
                        addCyclicOrderIds(orderIds, orderId, controllerId);
                    }
                } else {
                    List<String> cyclicMainParts = new ArrayList<String>();
                    for (String orderId : in.getFilter().getOrderIds()) {
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
            filter.setSubmissionIds(in.getFilter().getSubmissionHistoryIds());
            filter.setDailyPlanDate(in.getFilter().getDailyPlanDate(), settings.getTimeZone(), settings.getPeriodBegin());
            filter.setLate(in.getFilter().getLate());

            if (in.getFilter().getStates() != null) {
                for (DailyPlanOrderStateText state : in.getFilter().getStates()) {
                    filter.addState(state);
                }
            }
        } else {
            LOGGER.info(String.format("[%s][getOrderFilter][%s]missing permissions", caller, controllerId));
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

    protected void addOrders(SOSHibernateSession session, String controllerId, Date plannedStartFrom, Date plannedStartTo, DailyPlanOrderFilter in,
            List<DBItemDailyPlanWithHistory> orders, ArrayList<PlannedOrderItem> result, boolean getCyclicDetails) {

        if (orders != null) {
            DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
            for (DBItemDailyPlanWithHistory item : orders) {
                PlannedOrderItem p = createPlanItem(item);
                p.setControllerId(controllerId);

                if ((p.getStartMode() == 1 && !in.getExpandCycleOrders())) {
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
                Date dateFrom = plannedStartFrom;
                if (dateFrom != null && item.getState() != null) {
                    Date now = new Date();
                    if (DailyPlanOrderStateText.SUBMITTED.value().equals(item.getState().get_text().value()) && now.getTime() > dateFrom.getTime()) {
                        dateFrom = now;
                    }
                }
                minIteminfo = dbLayer.getCyclicOrderMinEntryAndCountTotal(item.getControllerId(), mainOrderId, dateFrom, plannedStartTo);
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

    protected DBItemDailyPlanOrder addCyclicOrderIds(List<String> orderIds, String orderId, String controllerId) throws SOSHibernateException {
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
