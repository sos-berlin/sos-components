package com.sos.webservices.order.impl;

import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.orders.DBItemDailyPlanVariable;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderFilter;
import com.sos.js7.order.initiator.db.DBLayerOrderVariables;
import com.sos.js7.order.initiator.db.FilterOrderVariables;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.initiator.model.OrderVariables;
import com.sos.webservices.order.resource.IOrderVariablesResource;

@Path("daily_plan")
public class OrderVariablesImpl extends JOCResourceImpl implements IOrderVariablesResource {

    private static final String API_CALL = "./dailyplan/order/variables";

    @Override
    public JOCDefaultResponse postOrderVariables(String accessToken, byte[] filterBytes) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrderFilter.class);
            OrderFilter orderFilter = Globals.objectMapper.readValue(filterBytes, OrderFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(orderFilter.getControllerId(), getControllerPermissions(orderFilter
                    .getControllerId(), accessToken).getOrders().getView());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);

            DBLayerOrderVariables dbLayerOrderVariables = new DBLayerOrderVariables(sosHibernateSession);
            FilterOrderVariables filterOrderVariables = new FilterOrderVariables();

            filterOrderVariables.setOrderId(orderFilter.getOrderId());
            OrderVariables orderVariables = new OrderVariables();
            List<DBItemDailyPlanVariable> listOfOrderVariables = dbLayerOrderVariables.getOrderVariables(filterOrderVariables, 0);
            Variables variables = new Variables();
            if (listOfOrderVariables != null && listOfOrderVariables.size() > 0 && listOfOrderVariables.get(0).getVariableValue() != null) {
                variables = Globals.objectMapper.readValue(listOfOrderVariables.get(0).getVariableValue(), Variables.class);
                /*
                 * for (DBItemDailyPlanVariables orderVariable : listOfOrderVariables) { switch (VariableType.fromValue(orderVariable.getVariableType())) { case
                 * STRING: variables.setAdditionalProperty(orderVariable.getVariableName(), orderVariable.getVariableValue()); break; case INTEGER:
                 * variables.setAdditionalProperty(orderVariable.getVariableName(), Integer.parseInt(orderVariable.getVariableValue())); break; case DOUBLE:
                 * variables.setAdditionalProperty(orderVariable.getVariableName(), Double.parseDouble(orderVariable.getVariableValue())); break; case
                 * BIGDECIMAL: variables.setAdditionalProperty(orderVariable.getVariableName(), new BigDecimal(orderVariable.getVariableValue().replaceAll(",",
                 * ""))); break; case BOOLEAN: variables.setAdditionalProperty(orderVariable.getVariableName(),
                 * Boolean.parseBoolean(orderVariable.getVariableValue())); break; } }
                 */
            } else {
                // TODO @ur throw exception
            }
            orderVariables.setVariables(variables);
            orderVariables.setDeliveryDate(new Date());

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(orderVariables));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }

    }

}
