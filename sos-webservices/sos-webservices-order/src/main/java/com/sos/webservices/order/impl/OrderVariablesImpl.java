package com.sos.webservices.order.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.orders.DBItemDailyPlanVariables;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderFilter;
import com.sos.js7.order.initiator.db.DBLayerOrderVariables;
import com.sos.js7.order.initiator.db.FilterOrderVariables;
import com.sos.webservices.order.initiator.model.NameValuePair;
import com.sos.webservices.order.initiator.model.OrderVariables;
import com.sos.webservices.order.resource.IOrderVariablesResource;

@Path("orders")
public class OrderVariablesImpl extends JOCResourceImpl implements IOrderVariablesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupOrdersImpl.class);
    private static final String API_CALL = "./orders/variables";

    @Override
    public JOCDefaultResponse postOrderVariables(String xAccessToken, OrderFilter orderFilter) {
        LOGGER.debug("list orders");
        SOSHibernateSession sosHibernateSession = null;
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, "", xAccessToken, orderFilter.getJobschedulerId(), getPermissonsJocCockpit(
                    orderFilter.getJobschedulerId(), xAccessToken).getOrder().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("orderId", orderFilter.getOrderId());
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            
            DBLayerOrderVariables dbLayerOrderVariables = new DBLayerOrderVariables(sosHibernateSession);
            FilterOrderVariables filterOrderVariables = new FilterOrderVariables();
            
            
            filterOrderVariables.setPlannedOrderId(orderFilter.getOrderId());
            OrderVariables variables = new OrderVariables();
            List<DBItemDailyPlanVariables> listOfOrderVariables = dbLayerOrderVariables.getOrderVariables(filterOrderVariables, 0);
            variables.setDeliveryDate(new Date());
            variables.setVariables(new ArrayList<NameValuePair>());
            for (DBItemDailyPlanVariables orderVariable : listOfOrderVariables) {
                NameValuePair variable = new NameValuePair();
                variable.setName(orderVariable.getVariableName());
                variable.setValue(orderVariable.getVariableValue());
                variables.getVariables().add(variable);
            }

            return JOCDefaultResponse.responseStatus200(variables);

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
