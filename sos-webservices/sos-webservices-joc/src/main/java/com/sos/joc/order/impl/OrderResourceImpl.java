package com.sos.joc.order.impl;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderFilter;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.OrderV;
import com.sos.joc.order.resource.IOrderResource;
import com.sos.schema.JsonValidator;

import js7.data.order.OrderId;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;

@Path("order")
public class OrderResourceImpl extends JOCResourceImpl implements IOrderResource {

    private static final String API_CALL = "./order";
    private final List<OrderStateText> orderStateWithRequirements = Arrays.asList(OrderStateText.PENDING, OrderStateText.SCHEDULED,
            OrderStateText.BLOCKED);

    @Override
    public JOCDefaultResponse postOrder(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrderFilter.class);
            OrderFilter orderFilter = Globals.objectMapper.readValue(filterBytes, OrderFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(orderFilter.getControllerId(), getControllerPermissions(orderFilter
                    .getControllerId(), accessToken).getOrders().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JControllerState currentState = Proxy.of(orderFilter.getControllerId()).currentState();
            Instant surveyDateInstant = currentState.instant();
            Optional<JOrder> optional = currentState.idToOrder(OrderId.of(orderFilter.getOrderId()));

            if (optional.isPresent()) {
                JOrder jOrder = optional.get();
                OrderV o = OrdersHelper.mapJOrderToOrderV(jOrder, orderFilter.getCompact(), folderPermissions.getListOfFolders(), Collections
                        .singletonMap(jOrder.workflowId(), OrdersHelper.getFinalParameters(jOrder.workflowId(), currentState)), surveyDateInstant
                                .toEpochMilli());
                checkFolderPermissions(o.getWorkflowId().getPath());
                if (orderStateWithRequirements.contains(o.getState().get_text())) {
                    o.setRequirements(OrdersHelper.getRequirements(jOrder, currentState));
                }
                o.setSurveyDate(Date.from(surveyDateInstant));
                o.setDeliveryDate(Date.from(Instant.now()));
                return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(o));
            } else {
                throw new ControllerObjectNotExistException(String.format("unknown Order '%s'", orderFilter.getOrderId()));
            }

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
