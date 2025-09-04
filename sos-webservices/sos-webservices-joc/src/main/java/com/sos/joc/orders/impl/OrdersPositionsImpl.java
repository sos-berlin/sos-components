package com.sos.joc.orders.impl;

import java.util.Set;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.order.CheckedAddOrdersPositions;
import com.sos.joc.classes.order.CheckedResumeOrdersPositions;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.order.OrderIdsFilter;
import com.sos.joc.model.workflow.WorkflowFilter;
import com.sos.joc.orders.resource.IOrdersPositions;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("orders")
public class OrdersPositionsImpl extends JOCResourceImpl implements IOrdersPositions {

    private static final String API_CALL_RESUME = "./orders/resume/positions";
    private static final String API_CALL_ADD = "./orders/add/positions";

    @Override
    public JOCDefaultResponse resumeOrderPositions(String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(API_CALL_RESUME, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validate(filterBytes, OrderIdsFilter.class);
            OrderIdsFilter ordersFilter = Globals.objectMapper.readValue(filterBytes, OrderIdsFilter.class);
            String controllerId = ordersFilter.getControllerId();
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getBasicControllerPermissions(controllerId, accessToken).getOrders()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            Set<String> orders = ordersFilter.getOrderIds();

            CheckedResumeOrdersPositions entity = new CheckedResumeOrdersPositions().get(orders, Proxy.of(controllerId).currentState(), folderPermissions
                    .getListOfFolders());

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));

        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    @Override
    public JOCDefaultResponse addOrderPositions(String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(API_CALL_ADD, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validate(filterBytes, WorkflowFilter.class);
            WorkflowFilter workflowFilter = Globals.objectMapper.readValue(filterBytes, WorkflowFilter.class);
            String controllerId = workflowFilter.getControllerId();

            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getBasicControllerPermissions(controllerId, accessToken).getOrders()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            CheckedAddOrdersPositions entity = new CheckedAddOrdersPositions().get(workflowFilter.getWorkflowId(), controllerId, Proxy.of(
                    controllerId).currentState(), folderPermissions.getListOfFolders());

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));

        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
