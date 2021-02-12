package com.sos.webservices.order.impl;

import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.orders.DBItemDailyPlanOrders;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionsFilter;
import com.sos.joc.model.order.OrderIds;
import com.sos.joc.model.order.OrdersFilterV;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.resource.ICyclicOrdersResource;

@Path("utilities")
public class CyclicOrdersImpl extends JOCResourceImpl implements ICyclicOrdersResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CyclicOrdersImpl.class);
    private static final String API_CALL = "./daily_plan/orders/cyclic";

    @Override
    public JOCDefaultResponse postCyclicOrders(String accessToken, byte[] filterBytes) {
        SOSHibernateSession sosHibernateSession = null;
        LOGGER.debug("reading list of cyclic orders");
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanSubmissionsFilter.class);
            OrdersFilterV ordersFilterV = Globals.objectMapper.readValue(filterBytes, OrdersFilterV.class);

            JOCDefaultResponse jocDefaultResponse = init(API_CALL, ordersFilterV, accessToken, ordersFilterV.getControllerId(),
                    getPermissonsJocCockpit(getControllerId(accessToken, ordersFilterV.getControllerId()), accessToken).getWorkflow().getExecute()
                            .isAddOrder());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            OrderIds expandedOrderIds = new OrderIds();
            for (String orderId : ordersFilterV.getOrderIds()) {
                expandedOrderIds.getOrderIds().add(orderId);
                addCyclicOrderIds(expandedOrderIds.getOrderIds(), orderId, ordersFilterV);
            }

            return JOCDefaultResponse.responseStatus200(expandedOrderIds);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

    private void addCyclicOrderIds(List<String> orderIds, String orderId, OrdersFilterV ordersFilterV) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(ordersFilterV.getControllerId());
            filter.setOrderId(orderId);

            List<DBItemDailyPlanOrders> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);

            if (listOfPlannedOrders.size() == 1) {
                DBItemDailyPlanOrders dbItemDailyPlanOrder = listOfPlannedOrders.get(0);
                if (dbItemDailyPlanOrder.getStartMode() == 1) {

                    FilterDailyPlannedOrders filterCyclic = new FilterDailyPlannedOrders();
                    filterCyclic.setControllerId(ordersFilterV.getControllerId());
                    filterCyclic.setRepeatInterval(dbItemDailyPlanOrder.getRepeatInterval());
                    filterCyclic.setPeriodBegin(dbItemDailyPlanOrder.getPeriodBegin());
                    filterCyclic.setPeriodEnd(dbItemDailyPlanOrder.getPeriodEnd());
                    filterCyclic.setWorkflowName(dbItemDailyPlanOrder.getWorkflowName());
                    filterCyclic.setScheduleName(dbItemDailyPlanOrder.getScheduleName());
                    filterCyclic.setDailyPlanDate(dbItemDailyPlanOrder.getDailyPlanDate());

                    List<DBItemDailyPlanOrders> listOfPlannedCyclicOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filterCyclic, 0);
                    for (DBItemDailyPlanOrders dbItemDailyPlanOrders : listOfPlannedCyclicOrders) {
                        if (!dbItemDailyPlanOrders.getOrderId().equals(orderId)) {
                            orderIds.add(dbItemDailyPlanOrders.getOrderId());
                        }
                    }
                }
            } else {
                LOGGER.warn("Expected one record for order-id " + filter.getOrderId());
                throw new DBMissingDataException("Expected one record for order-id " + filter.getOrderId());
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
