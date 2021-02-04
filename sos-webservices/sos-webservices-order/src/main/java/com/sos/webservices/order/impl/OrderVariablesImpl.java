package com.sos.webservices.order.impl;

import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.common.Variables;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.orders.DBItemDailyPlanVariables;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.dailyplan.DailyPlanSubmissionsFilter;
import com.sos.joc.model.order.OrderFilter;
import com.sos.js7.order.initiator.db.DBLayerOrderVariables;
import com.sos.js7.order.initiator.db.FilterOrderVariables;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.initiator.model.OrderVariables;
import com.sos.webservices.order.resource.IOrderVariablesResource;

@Path("orders")
public class OrderVariablesImpl extends JOCResourceImpl implements IOrderVariablesResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderVariablesImpl.class);
    private static final String API_CALL = "./orders/variables";

    @Override
    public JOCDefaultResponse postOrderVariables(String accessToken, byte[] filterBytes) {
        LOGGER.debug("list order variables");
        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DailyPlanSubmissionsFilter.class);
            OrderFilter orderFilter = Globals.objectMapper.readValue(filterBytes, OrderFilter.class);

            JOCDefaultResponse jocDefaultResponse = init(API_CALL, "", accessToken, orderFilter.getControllerId(), getPermissonsJocCockpit(
                    getControllerId(accessToken, orderFilter.getControllerId()), accessToken).getOrder().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("orderId", orderFilter.getOrderId());
            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerOrderVariables dbLayerOrderVariables = new DBLayerOrderVariables(sosHibernateSession);
            FilterOrderVariables filterOrderVariables = new FilterOrderVariables();

            filterOrderVariables.setOrderId(orderFilter.getOrderId());
            OrderVariables orderVariables = new OrderVariables();
            List<DBItemDailyPlanVariables> listOfOrderVariables = dbLayerOrderVariables.getOrderVariables(filterOrderVariables, 0);
            orderVariables.setDeliveryDate(new Date());
            Variables variables = new Variables();
            if (listOfOrderVariables != null) {
                for (DBItemDailyPlanVariables orderVariable : listOfOrderVariables) {
                    // TODO db should know datatype then cast value
                    variables.setAdditionalProperty(orderVariable.getVariableName(), orderVariable.getVariableValue());
                }
            }
            orderVariables.setVariables(variables);

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
