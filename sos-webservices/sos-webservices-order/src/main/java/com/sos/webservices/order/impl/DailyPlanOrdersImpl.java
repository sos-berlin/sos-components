package com.sos.webservices.order.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.db.orders.DBItemDailyPlanWithHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.dailyplan.Period;
import com.sos.joc.model.dailyplan.PlannedOrderItem;
import com.sos.joc.model.dailyplan.PlannedOrders;
import com.sos.joc.model.dailyplan.PlannedOrdersFilter;
import com.sos.joc.model.order.OrderState;
import com.sos.joc.model.order.OrderStateText;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.webservices.order.resource.IDailyPlanOrdersResource;

@Path("daily_plan")
public class DailyPlanOrdersImpl extends JOCResourceImpl implements IDailyPlanOrdersResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanOrdersImpl.class);
    private static final String API_CALL = "./daily_plan/orders";

    private PlannedOrderItem createPlanItem(DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory) {

        PlannedOrderItem p = new PlannedOrderItem();
        p.setLate(dbItemDailyPlanWithHistory.isLate());
        Period period = new Period();
        period.setBegin(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getPeriodBegin());
        period.setEnd(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getPeriodEnd());
        period.setRepeat(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getRepeatInterval());
        p.setPeriod(period);

        p.setPlannedStartTime(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getPlannedStart());
        p.setExpectedEndTime(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getExpectedEnd());
        p.setLate(dbItemDailyPlanWithHistory.isLate());
        p.setSurveyDate(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getCreated());

        p.setStartMode(dbItemDailyPlanWithHistory.getStartMode());

        p.setWorkflow(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getWorkflow());
        p.setOrderId(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getOrderKey());
        p.setOrderTemplatePath(dbItemDailyPlanWithHistory.getDbItemDailyPlannedOrders().getOrderTemplatePath());

        OrderState orderState = new OrderState();
        orderState.set_text(dbItemDailyPlanWithHistory.getStateText());
        orderState.setSeverity(OrdersHelper.severityByGroupedStates.get(orderState.get_text()));
        p.setState(orderState);

        if (dbItemDailyPlanWithHistory.getDbItemOrder() != null) {
            if (dbItemDailyPlanWithHistory.getDbItemOrder().getStartTime().after(new Date(0L)))   {
                p.setStartTime(dbItemDailyPlanWithHistory.getDbItemOrder().getStartTime());
                p.setEndTime(dbItemDailyPlanWithHistory.getDbItemOrder().getEndTime());
            }
            p.setHistoryId(String.valueOf(dbItemDailyPlanWithHistory.getDbItemOrder().getId()));
        }
        return p;

    }

    @Override
    public JOCDefaultResponse postDailyPlan(String xAccessToken, PlannedOrdersFilter plannedOrdersFilter) throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        LOGGER.debug("Reading the daily plan for day " + plannedOrdersFilter.getDailyPlanDate());
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, plannedOrdersFilter, xAccessToken, plannedOrdersFilter.getControllerId(),
                    getPermissonsJocCockpit(plannedOrdersFilter.getControllerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            this.checkRequiredParameter("dailyPlanDate", plannedOrdersFilter.getDailyPlanDate());
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            boolean withFolderFilter = plannedOrdersFilter.getFolders() != null && !plannedOrdersFilter.getFolders().isEmpty();
            boolean hasPermission = true;
            Set<Folder> folders = addPermittedFolder(plannedOrdersFilter.getFolders());

            Globals.beginTransaction(sosHibernateSession);

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(plannedOrdersFilter.getControllerId());
            filter.setWorkflow(plannedOrdersFilter.getWorkflow());
            filter.setSubmissionHistoryId(plannedOrdersFilter.getSubmissionHistoryId());
            if (plannedOrdersFilter.getOrderTemplates() != null) {
                for (String orderTemplatePath : plannedOrdersFilter.getOrderTemplates()) {
                    filter.addOrderTemplatePath(orderTemplatePath);
                }
            }
            filter.setDailyPlanDate(plannedOrdersFilter.getDailyPlanDate());

            filter.setLate(plannedOrdersFilter.getLate());

            if (plannedOrdersFilter.getStates() != null) {
                for (OrderStateText state : plannedOrdersFilter.getStates()) {
                    filter.addState(state);
                }
            }
            if (withFolderFilter && (folders == null || folders.isEmpty())) {
                hasPermission = false;
            } else if (folders != null && !folders.isEmpty()) {
                filter.addFolderPaths(new HashSet<Folder>(folders));
            }

            ArrayList<PlannedOrderItem> result = new ArrayList<PlannedOrderItem>();
            PlannedOrders entity = new PlannedOrders();

            if (hasPermission) {
                List<DBItemDailyPlanWithHistory> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanWithHistoryList(filter, 0);
                for (DBItemDailyPlanWithHistory dbItemDailyPlanWithHistory : listOfPlannedOrders) {

                    boolean add = true;
                    PlannedOrderItem p = createPlanItem(dbItemDailyPlanWithHistory);

                    if (add) {
                        result.add(p);
                    }
                }
            }

            entity.setPlannedOrderItems(result);
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);

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

}
