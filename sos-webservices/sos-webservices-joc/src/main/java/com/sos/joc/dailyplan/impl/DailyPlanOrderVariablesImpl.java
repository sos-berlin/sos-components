package com.sos.joc.dailyplan.impl;

import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebservicePaths;
import com.sos.joc.dailyplan.db.DBLayerOrderVariables;
import com.sos.joc.dailyplan.resource.IDailyPlanOrderVariablesResource;
import com.sos.joc.db.dailyplan.DBItemDailyPlanVariable;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.order.OrderFilter;
import com.sos.schema.JsonValidator;
import com.sos.webservices.order.initiator.model.OrderVariables;

import jakarta.ws.rs.Path;

@Path(WebservicePaths.DAILYPLAN)
public class DailyPlanOrderVariablesImpl extends JOCResourceImpl implements IDailyPlanOrderVariablesResource {

    @Override
    public JOCDefaultResponse postOrderVariables(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, OrderFilter.class);
            OrderFilter in = Globals.objectMapper.readValue(filterBytes, OrderFilter.class);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), getBasicControllerPermissions(in.getControllerId(), accessToken)
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
            } else {
                // TODO @ur throw exception
            }
            answer.setDeliveryDate(new Date());
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }

    }

}
