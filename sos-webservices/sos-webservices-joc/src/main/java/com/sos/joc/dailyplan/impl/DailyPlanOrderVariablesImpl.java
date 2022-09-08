package com.sos.joc.dailyplan.impl;

import java.util.Date;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.dailyplan.db.DBLayerOrderVariables;
import com.sos.joc.dailyplan.resource.IDailyPlanOrderVariablesResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderFilter;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.initiator.model.OrderVariables;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanOrderVariablesImpl extends JOCResourceImpl implements IDailyPlanOrderVariablesResource {

    @Override
    public JOCDefaultResponse postOrderVariables(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrderFilter.class);
            OrderFilter in = Globals.objectMapper.readValue(filterBytes, OrderFilter.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getControllerPermissions(in.getControllerId(), accessToken)
                    .getOrders().getView());
            if (response != null) {
                return response;
            }

            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DBLayerOrderVariables dbLayer = new DBLayerOrderVariables(session);
            DBItemDailyPlanVariable item = dbLayer.getOrderVariable(in.getControllerId(), in.getOrderId());
            session.close();
            session = null;

            OrderVariables answer = new OrderVariables();
            if (item != null && item.getVariableValue() != null) {
                answer.setVariables(Globals.objectMapper.readValue(item.getVariableValue(), Variables.class));
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
            answer.setDeliveryDate(new Date());
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }

    }

}
