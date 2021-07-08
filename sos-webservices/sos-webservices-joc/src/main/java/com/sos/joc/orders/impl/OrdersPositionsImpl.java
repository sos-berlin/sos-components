package com.sos.joc.orders.impl;

import java.util.Set;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.order.CheckedOrdersPositions;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.orders.resource.IOrdersPositions;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;

@Path("orders")
public class OrdersPositionsImpl extends JOCResourceImpl implements IOrdersPositions {

    private static final String API_CALL = "./orders/resume/positions";

    @Override
    public JOCDefaultResponse postOrderPositions(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ModifyOrders.class);
            ModifyOrders ordersFilter = Globals.objectMapper.readValue(filterBytes, ModifyOrders.class);
            String controllerId = ordersFilter.getControllerId();
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getOrders()
                    .getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            Set<String> orders = ordersFilter.getOrderIds();
            checkRequiredParameter("orderIds", orders);

            CheckedOrdersPositions entity = new CheckedOrdersPositions().get(orders, Proxy.of(controllerId).currentState(), folderPermissions
                    .getListOfFolders());

            if (entity.hasNotSuspendedOrFailedOrders()) {
                String msg = entity.getNotSuspendedOrFailedOrdersMessage();
                ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
            }
            
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
