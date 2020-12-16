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
import com.sos.joc.model.dailyplan.DailyPlanOrderFilter;
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
    public JOCDefaultResponse postDailyPlan(String xAccessToken, DailyPlanOrderFilter dailyPlanOrderFilter) throws JocException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, dailyPlanOrderFilter, xAccessToken, dailyPlanOrderFilter.getControllerId(),
                    getPermissonsJocCockpit(getControllerId(xAccessToken,dailyPlanOrderFilter.getControllerId()), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            this.checkRequiredParameter("filter", dailyPlanOrderFilter.getFilter());
            this.checkRequiredParameter("dailyPlanDate", dailyPlanOrderFilter.getFilter().getDailyPlanDate());

            LOGGER.debug("Reading the daily plan for day " + dailyPlanOrderFilter.getFilter().getDailyPlanDate());

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);
            boolean withFolderFilter = dailyPlanOrderFilter.getFilter().getFolders() != null && !dailyPlanOrderFilter.getFilter().getFolders().isEmpty();
            boolean hasPermission = true;
            Set<Folder> folders = addPermittedFolder(dailyPlanOrderFilter.getFilter().getFolders());

            Globals.beginTransaction(sosHibernateSession);

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(dailyPlanOrderFilter.getControllerId());
            filter.setListOfWorkflowPaths(dailyPlanOrderFilter.getFilter().getWorkflowPaths());
            filter.setListOfSubmissionIds(dailyPlanOrderFilter.getFilter().getDailyPlanSubmissionHistoryIds());
            filter.setListOfSchedules(dailyPlanOrderFilter.getFilter().getSchedulePaths());
            filter.setListOfOrders(dailyPlanOrderFilter.getFilter().getOrderIds());

            filter.setDailyPlanDate(dailyPlanOrderFilter.getFilter().getDailyPlanDate());

            filter.setLate(dailyPlanOrderFilter.getFilter().getLate());

            if (dailyPlanOrderFilter.getFilter().getStates() != null) {
                for (OrderStateText state : dailyPlanOrderFilter.getFilter().getStates()) {
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
