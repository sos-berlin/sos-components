package com.sos.webservices.order.impl;

import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.CyclicOrderInfos;
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
import com.sos.joc.model.dailyplan.DailyPlanOrderSelector;
import com.sos.joc.model.dailyplan.DailyPlanOrdersSummary;
import com.sos.joc.model.dailyplan.Period;
import com.sos.joc.model.dailyplan.PlannedOrderItem;
import com.sos.joc.model.order.OrderState;
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.order.initiator.OrderInitiatorSettings;
import com.sos.js7.order.initiator.classes.CycleOrderKey;
import com.sos.js7.order.initiator.classes.GlobalSettingsReader;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.resource.IDailyPlanOrdersSummaryResource;

@Path("daily_plan")
public class DailyPlanOrdersSummaryImpl extends JOCResourceImpl implements IDailyPlanOrdersSummaryResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersSummaryImpl.class);
    private static final String API_CALL = "./daily_plan/orders/summary";
    private OrderInitiatorSettings settings;

    private PlannedOrderItem createPlanItem(DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory) {

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

    @Override
    public JOCDefaultResponse postDailyPlanOrdersSummary(String accessToken, byte[] filterBytes) throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        try {

            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanOrderSelector.class);
            DailyPlanOrderFilter dailyPlanOrderFilter = Globals.objectMapper.readValue(filterBytes, DailyPlanOrderFilter.class);

            JOCDefaultResponse jocDefaultResponse = init(API_CALL, dailyPlanOrderFilter, accessToken, dailyPlanOrderFilter.getControllerId(),
                    getPermissonsJocCockpit(getControllerId(accessToken, dailyPlanOrderFilter.getControllerId()), accessToken).getDailyPlan()
                            .getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            this.checkRequiredParameter("filter", dailyPlanOrderFilter.getFilter());
            this.checkRequiredParameter("dailyPlanDate", dailyPlanOrderFilter.getFilter().getDailyPlanDate());
            setSettings();
            LOGGER.debug("Reading the daily plan for day " + dailyPlanOrderFilter.getFilter().getDailyPlanDate());

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            boolean withFolderFilter = dailyPlanOrderFilter.getFilter().getFolders() != null && !dailyPlanOrderFilter.getFilter().getFolders()
                    .isEmpty();
            boolean hasPermission = true;
            Set<Folder> folders = addPermittedFolder(dailyPlanOrderFilter.getFilter().getFolders());

            sosHibernateSession.setAutoCommit(false);
            Globals.beginTransaction(sosHibernateSession);

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();

            if (dailyPlanOrderFilter.getFilter().getSchedulePaths() != null) {
                if (dailyPlanOrderFilter.getFilter().getScheduleNames() == null) {
                    dailyPlanOrderFilter.getFilter().setScheduleNames(new ArrayList<String>());
                }
                for (String path : dailyPlanOrderFilter.getFilter().getSchedulePaths()) {
                    String name = Paths.get(path).getFileName().toString();
                    dailyPlanOrderFilter.getFilter().getScheduleNames().add(name);
                }
            }
            if (dailyPlanOrderFilter.getFilter().getWorkflowPaths() != null) {
                if (dailyPlanOrderFilter.getFilter().getWorkflowNames() == null) {
                    dailyPlanOrderFilter.getFilter().setWorkflowNames(new ArrayList<String>());
                }

                for (String path : dailyPlanOrderFilter.getFilter().getWorkflowPaths()) {
                    String name = Paths.get(path).getFileName().toString();
                    dailyPlanOrderFilter.getFilter().getWorkflowNames().add(name);
                }
            }

            filter.setControllerId(dailyPlanOrderFilter.getControllerId());
            filter.setListOfWorkflowNames(dailyPlanOrderFilter.getFilter().getWorkflowNames());
            filter.setListOfSubmissionIds(dailyPlanOrderFilter.getFilter().getSubmissionHistoryIds());
            filter.setListOfScheduleNames(dailyPlanOrderFilter.getFilter().getScheduleNames());
            filter.setListOfOrders(dailyPlanOrderFilter.getFilter().getOrderIds());
            filter.setDailyPlanDate(dailyPlanOrderFilter.getFilter().getDailyPlanDate(), settings.getTimeZone(), settings.getPeriodBegin());
            filter.setLate(dailyPlanOrderFilter.getFilter().getLate());

            if (withFolderFilter && (folders == null || folders.isEmpty())) {
                hasPermission = false;
            } else if (folders != null && !folders.isEmpty()) {
                filter.addFolderPaths(new HashSet<Folder>(folders));
            }

            ArrayList<PlannedOrderItem> listOfPlannedOrderItems = new ArrayList<PlannedOrderItem>();

            DateFormat periodFormat = new SimpleDateFormat("hh:mm:ss");

            DailyPlanOrdersSummary dailyPlanOrdersSummary = new DailyPlanOrdersSummary();
            dailyPlanOrdersSummary.setFinished(0);
            dailyPlanOrdersSummary.setPending(0);
            dailyPlanOrdersSummary.setPendingLate(0);
            dailyPlanOrdersSummary.setPlanned(0);
            dailyPlanOrdersSummary.setPlannedLate(0);
            if (hasPermission) {
                List<DBItemDailyPlanWithHistory> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanWithHistoryList(filter, 0);

                Map<CycleOrderKey, List<PlannedOrderItem>> mapOfCycledOrders = new TreeMap<CycleOrderKey, List<PlannedOrderItem>>();

                for (DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory : listOfPlannedOrders) {

                    PlannedOrderItem p = createPlanItem(dbItemDailyPlanWithHistory);

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

                for (PlannedOrderItem p : listOfPlannedOrderItems) {
                    if (OrderStateText.PENDING.value().equals(p.getState().get_text().value())) {
                        if (p.getLate()) {
                            dailyPlanOrdersSummary.setPendingLate(dailyPlanOrdersSummary.getPendingLate() + 1);
                        } else {
                            dailyPlanOrdersSummary.setPending(dailyPlanOrdersSummary.getPending() + 1);
                        }
                    }
                    if (OrderStateText.PLANNED.value().equals(p.getState().get_text().value())) {
                        if (p.getLate()) {
                            dailyPlanOrdersSummary.setPlannedLate(dailyPlanOrdersSummary.getPlannedLate() + 1);
                        } else {
                            dailyPlanOrdersSummary.setPlanned(dailyPlanOrdersSummary.getPlanned() + 1);
                        }
                    }
                    if (OrderStateText.FINISHED.value().equals(p.getState().get_text().value())) {
                        dailyPlanOrdersSummary.setFinished(dailyPlanOrdersSummary.getFinished() + 1);
                    }
                }

                Globals.commit(sosHibernateSession);
            }

            return JOCDefaultResponse.responseStatus200(dailyPlanOrdersSummary);

        } catch (JocException e) {
            LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void setSettings() throws Exception {
        GlobalSettingsReader reader = new GlobalSettingsReader();
        AConfigurationSection section = Globals.configurationGlobals.getConfigurationSection(DefaultSections.dailyplan);
        this.settings = reader.getSettings(section);
    }
}
