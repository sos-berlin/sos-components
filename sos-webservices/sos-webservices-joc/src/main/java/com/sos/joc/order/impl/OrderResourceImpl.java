package com.sos.joc.order.impl;

import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.orders.OrdersHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JobSchedulerObjectNotExistException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderFilter;
import com.sos.joc.model.order.OrderV;
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
        try {
            JsonValidator.validateFailFast(filterBytes, OrderFilter.class);
            OrderFilter orderFilter = Globals.objectMapper.readValue(filterBytes, OrderFilter.class);
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, orderFilter, accessToken, orderFilter.getJobschedulerId(), getPermissonsJocCockpit(
                    orderFilter.getJobschedulerId(), accessToken).getOrder().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkFolderPermissions(orderFilter.getWorkflow());
            JControllerState currentState = Proxy.of(orderFilter.getJobschedulerId()).currentState();
            Long surveyDateMillis = currentState.eventId() / 1000;
            Optional<JOrder> optional = currentState.idToOrder(OrderId.of(orderFilter.getOrderId()));
            
            if (optional.isPresent() && optional.get().workflowId().path().string().equals(orderFilter.getWorkflow())) {
                return JOCDefaultResponse.responseStatus200(OrdersHelper.mapJOrderToOrderV(optional.get(), orderFilter.getCompact(), surveyDateMillis,
                        true));
            } else {
                if (orderFilter.getSuppressNotExistException() != null && orderFilter.getSuppressNotExistException()) {
                    OrderV order = new OrderV();
                    order.setSurveyDate(Date.from(Instant.ofEpochMilli(surveyDateMillis)));
                    order.setDeliveryDate(Date.from(Instant.now()));
                    return JOCDefaultResponse.responseStatus200(order);
                } else {
                    throw new JobSchedulerObjectNotExistException(String.format("unknown Order '%s' in Workflow '%s'", orderFilter.getOrderId(),
                            orderFilter.getWorkflow()));
                }
            }

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
