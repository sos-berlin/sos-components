package com.sos.joc.utilities.impl;

import java.util.Collection;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderIds;
import com.sos.joc.model.order.OrdersFilterV;
import com.sos.joc.utilities.resource.ICyclicOrdersResource;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
import com.sos.schema.JsonValidator;

@Path("utilities")
public class CyclicOrdersImpl extends JOCOrderResourceImpl implements ICyclicOrdersResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CyclicOrdersImpl.class);
    private static final String API_CALL = "./utilities/cyclic_orders";

    @Override
    public JOCDefaultResponse postCyclicOrders(String accessToken, byte[] filterBytes) {
        LOGGER.debug("reading list of cyclic orders");
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrdersFilterV.class);
            OrdersFilterV ordersFilterV = Globals.objectMapper.readValue(filterBytes, OrdersFilterV.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(ordersFilterV.getControllerId(), getControllerPermissions(ordersFilterV
                    .getControllerId(), accessToken).getOrders().getView());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            setSettings();
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
        }
    }

    private void addCyclicOrderIds(Collection<String> orderIds, String orderId, OrdersFilterV ordersFilterV) throws SOSHibernateException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(ordersFilterV.getControllerId());
            filter.setOrderId(orderId);

            List<DBItemDailyPlanOrder> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);

            if (listOfPlannedOrders.size() == 1) {
                DBItemDailyPlanOrder dbItemDailyPlanOrder = listOfPlannedOrders.get(0);
                if (dbItemDailyPlanOrder.getStartMode() == 1) {

                    FilterDailyPlannedOrders filterCyclic = new FilterDailyPlannedOrders();
                    filterCyclic.setControllerId(ordersFilterV.getControllerId());
                    filterCyclic.setRepeatInterval(dbItemDailyPlanOrder.getRepeatInterval());
                    filterCyclic.setPeriodBegin(dbItemDailyPlanOrder.getPeriodBegin());
                    filterCyclic.setPeriodEnd(dbItemDailyPlanOrder.getPeriodEnd());
                    filterCyclic.setWorkflowName(dbItemDailyPlanOrder.getWorkflowName());
                    filterCyclic.setScheduleName(dbItemDailyPlanOrder.getScheduleName());
                    filterCyclic.setDailyPlanDate(dbItemDailyPlanOrder.getDailyPlanDate(settings.getTimeZone()), settings.getTimeZone(), settings
                            .getPeriodBegin());

                    List<DBItemDailyPlanOrder> listOfPlannedCyclicOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filterCyclic, 0);
                    for (DBItemDailyPlanOrder dbItemDailyPlanOrders : listOfPlannedCyclicOrders) {
                        if (!dbItemDailyPlanOrders.getOrderId().equals(orderId)) {
                            orderIds.add(dbItemDailyPlanOrders.getOrderId());
                        }
                    }
                }
            } else {
                throw new DBMissingDataException("Expected one record for order-id " + filter.getOrderId());
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
