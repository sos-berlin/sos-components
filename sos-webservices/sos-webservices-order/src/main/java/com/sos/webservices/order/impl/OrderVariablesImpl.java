package com.sos.webservices.order.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.db.orders.DBItemDailyPlan;
import com.sos.jobscheduler.db.orders.DBItemDailyPlanVariables;
import com.sos.jobscheduler.model.common.Variables;
import com.sos.jobscheduler.model.order.OrderItem;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrdersFilter;
import com.sos.webservices.order.initiator.db.DBLayerDailyPlan;
import com.sos.webservices.order.initiator.db.DBLayerOrderVariables;
import com.sos.webservices.order.initiator.db.FilterDailyPlan;
import com.sos.webservices.order.initiator.db.FilterOrderVariables;
import com.sos.webservices.order.resource.IOrderVariablesResource;
import com.sos.webservices.order.classes.OrderHelper;

@Path("orders")
public class OrderVariablesImpl extends JOCResourceImpl implements IOrderVariablesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupOrdersImpl.class);
    private static final String API_CALL = "./orders/variables";

    @Override
    public JOCDefaultResponse postOrderVariables(String xAccessToken, OrdersFilter ordersFilter) {
        LOGGER.debug("cleanup orders");
        SOSHibernateSession sosHibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, "", xAccessToken, "scheduler_joc_cockpit", getPermissonsJocCockpit(
                    "scheduler_joc_cockpit", xAccessToken).getOrder().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerOrderVariables dbLayerOrderVariables = new DBLayerOrderVariables(sosHibernateSession);
            FilterOrderVariables filterOrderVariables = new FilterOrderVariables();
            filterOrderVariables.setPlannedOrderId(ordersFilter.getOrders().get(0).getOrderId());
            Variables variables = new Variables();
            List<DBItemDailyPlanVariables> listOfOrderVariables = dbLayerOrderVariables.getOrderVariables(filterOrderVariables, 0);
            for (DBItemDailyPlanVariables orderVariable : listOfOrderVariables) {
                variables.setAdditionalProperty(orderVariable.getVariableName(), orderVariable.getVariableValue());
            }

            return JOCDefaultResponse.responseHtmlStatus200(variables);

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
