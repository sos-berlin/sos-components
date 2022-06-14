package com.sos.joc.order.impl;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.order.CheckedResumeOrdersPositions;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderVariablesFilter;
import com.sos.joc.order.resource.IOrderVariablesResource;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data_for_java.workflow.position.JPosition;

@Path("order")
public class OrderVariablesImpl extends JOCResourceImpl implements IOrderVariablesResource {

    private static final String API_CALL = "./order/variables";

    @Override
    public JOCDefaultResponse postVariables(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validate(filterBytes, OrderVariablesFilter.class);
            OrderVariablesFilter orderFilter = Globals.objectMapper.readValue(filterBytes, OrderVariablesFilter.class);
            String controllerId = orderFilter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getOrders().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            String position = null;
            if (orderFilter.getPosition() != null && !orderFilter.getPosition().isEmpty()) {
                Either<Problem, JPosition> jPos = JPosition.fromList(orderFilter.getPosition());
                ProblemHelper.throwProblemIfExist(jPos);
                position = jPos.get().toString();
            }

            CheckedResumeOrdersPositions cop = new CheckedResumeOrdersPositions();
            cop.get(orderFilter.getOrderId(), Proxy.of(controllerId).currentState(), folderPermissions.getListOfFolders(), position, false);
            cop.setOrderIds(null);
            cop.setDisabledPositionChange(null);
            cop.setPositions(null);

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(cop));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
