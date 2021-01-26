package com.sos.joc.order.impl;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.exceptions.JobSchedulerObjectNotExistException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderFilter;
import com.sos.joc.order.resource.IOrderResource;
import com.sos.schema.JsonValidator;

import js7.data.order.OrderId;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.order.JOrder;

@Path("order")
public class OrderResourceImpl extends JOCResourceImpl implements IOrderResource {

    private static final String API_CALL = "./order";

    @Override
    public JOCDefaultResponse postOrder(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrderFilter.class);
            OrderFilter orderFilter = Globals.objectMapper.readValue(filterBytes, OrderFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(orderFilter.getControllerId(), getPermissonsJocCockpit(orderFilter
                    .getControllerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JControllerState currentState = Proxy.of(orderFilter.getControllerId()).currentState();
            Long surveyDateMillis = currentState.eventId() / 1000;
            Optional<JOrder> optional = currentState.idToOrder(OrderId.of(orderFilter.getOrderId()));
            
            if (optional.isPresent()) {
                JOrder jOrder = optional.get();
                session = Globals.createSosHibernateStatelessConnection(API_CALL);
                DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
                final Map<String, String> namePathMap = dbLayer.getNamePathMapping(Arrays.asList(jOrder.workflowId().path().toString()),
                        DeployType.WORKFLOW.intValue());
                //checkFolderPermissions(optional.get().workflowId().path().string()); is only a name
                return JOCDefaultResponse.responseStatus200(OrdersHelper.mapJOrderToOrderV(optional.get(), orderFilter.getCompact(), namePathMap, surveyDateMillis,
                        true));
            } else {
                throw new JobSchedulerObjectNotExistException(String.format("unknown Order '%s'", orderFilter.getOrderId()));
            }

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
