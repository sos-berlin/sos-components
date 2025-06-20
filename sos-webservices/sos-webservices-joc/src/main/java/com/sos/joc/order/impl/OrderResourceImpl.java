package com.sos.joc.order.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.order.OrderFilter;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.order.resource.IOrderResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import js7.data.order.OrderId;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.workflow.JWorkflowId;

@Path("order")
public class OrderResourceImpl extends JOCResourceImpl implements IOrderResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderResourceImpl.class);
    private static final String API_CALL = "./order";
    private final List<OrderStateText> orderStateWithRequirements = Arrays.asList(OrderStateText.PENDING, OrderStateText.SCHEDULED,
            OrderStateText.BLOCKED);

    @Override
    public JOCDefaultResponse postOrder(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, OrderFilter.class);
            OrderFilter orderFilter = Globals.objectMapper.readValue(filterBytes, OrderFilter.class);
            String controllerId = orderFilter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getBasicControllerPermissions(controllerId, accessToken).getOrders()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JControllerState currentState = Proxy.of(controllerId).currentState();
            Instant surveyDateInstant = currentState.instant();
            JOrder jOrder = currentState.idToOrder().get(OrderId.of(orderFilter.getOrderId()));
            
            if (jOrder != null) {
                connection = Globals.createSosHibernateStatelessConnection(API_CALL);
                
                Map<String, Set<String>> orderTags = OrderTags.getTags(controllerId, Collections.singletonList(jOrder), connection);

                Map<List<Object>, String> positionToLabelsMap = getPositionToLabelsMap(controllerId, jOrder.workflowId());
                Set<OrderId> waitingOrders = OrdersHelper.getWaitingForAdmissionOrderIds(Collections.singleton(jOrder.id()), currentState);
                OrderV o = OrdersHelper.mapJOrderToOrderV(jOrder, currentState, orderFilter.getCompact(), folderPermissions.getListOfFolders(),
                        orderTags, waitingOrders, Collections.singletonMap(jOrder.workflowId(), OrdersHelper.getFinalParameters(jOrder.workflowId(),
                                currentState)), surveyDateInstant.toEpochMilli(), OrdersHelper.getDailyPlanTimeZone());
                checkFolderPermissions(o.getWorkflowId().getPath());
                o.setLabel(positionToLabelsMap.get(o.getPosition()));
                o.setHasChildOrders(currentState.orderIds().stream().map(OrderId::string).anyMatch(s -> s.startsWith(o.getOrderId() + "|")));
                if (orderStateWithRequirements.contains(o.getState().get_text())) {
                    o.setRequirements(OrdersHelper.getRequirements(jOrder, controllerId, new DeployedConfigurationDBLayer(connection)));
                    //o.setRequirements(OrdersHelper.getRequirements(jOrder, currentState));
                }
                o.setSurveyDate(Date.from(surveyDateInstant));
                o.setDeliveryDate(Date.from(Instant.now()));
                return responseStatus200(Globals.objectMapper.writeValueAsBytes(o));
            } else {
                throw new ControllerObjectNotExistException(String.format("unknown Order '%s'", orderFilter.getOrderId()));
            }

        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    private static Map<List<Object>, String> getPositionToLabelsMap(String controllerId, JWorkflowId workflowId) {
        try {
            return WorkflowsHelper.getPositionToLabelsMapFromDepHistory(controllerId, workflowId);
        } catch (Exception e) {
            LOGGER.warn("Cannot map order position to Workflow instruction label: ", e);
            return Collections.emptyMap();
        }
    }

}
