package com.sos.joc.orders.impl;

import java.util.Set;

import jakarta.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.order.CheckedAddOrdersPositions;
import com.sos.joc.classes.order.CheckedResumeOrdersPositions;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.model.workflow.WorkflowFilter;
import com.sos.joc.orders.resource.IOrdersPositions;
import com.sos.schema.JsonValidator;

@Path("orders")
public class OrdersPositionsImpl extends JOCResourceImpl implements IOrdersPositions {

    private static final String API_CALL_RESUME = "./orders/resume/positions";
    private static final String API_CALL_ADD = "./orders/add/positions";

    @Override
    public JOCDefaultResponse resumeOrderPositions(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_RESUME, filterBytes, accessToken);
            JsonValidator.validate(filterBytes, ModifyOrders.class);
            ModifyOrders ordersFilter = Globals.objectMapper.readValue(filterBytes, ModifyOrders.class);
            String controllerId = ordersFilter.getControllerId();
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getOrders()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            Set<String> orders = ordersFilter.getOrderIds();
            checkRequiredParameter("orderIds", orders);

            CheckedResumeOrdersPositions entity = new CheckedResumeOrdersPositions().get(orders, Proxy.of(controllerId).currentState(), folderPermissions
                    .getListOfFolders());

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse addOrderPositions(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_ADD, filterBytes, accessToken);
            JsonValidator.validate(filterBytes, WorkflowFilter.class);
            WorkflowFilter workflowFilter = Globals.objectMapper.readValue(filterBytes, WorkflowFilter.class);
            String controllerId = workflowFilter.getControllerId();

            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getOrders()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            CheckedAddOrdersPositions entity = new CheckedAddOrdersPositions().get(workflowFilter.getWorkflowId(), Proxy.of(controllerId)
                    .currentState(), folderPermissions.getListOfFolders());

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
