package com.sos.joc.order.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.Obstacle;
import com.sos.joc.model.order.Obstacle200;
import com.sos.joc.model.order.ObstacleType;
import com.sos.joc.model.order.OrderFilter;
import com.sos.joc.order.resource.IOrderObstaclesResource;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.order.OrderId;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrderObstacle;

@Path("order")
public class OrderObstaclesResourceImpl extends JOCResourceImpl implements IOrderObstaclesResource {

    private static final String API_CALL = "./order/obstacles";

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
            OrderId oId = OrderId.of(orderFilter.getOrderId());
            Either<Problem, Set<JOrderObstacle>> obstaclesE = currentState.orderToObstacles(oId, surveyDateInstant);
            
            Obstacle200 entry = new Obstacle200();
            entry.setSurveyDate(Date.from(surveyDateInstant));
            entry.setOrderId(orderFilter.getOrderId());
            try {
                ProblemHelper.throwProblemIfExist(obstaclesE);

                entry.setObstacles(obstaclesE.get().stream().map(obstacle -> {
                    Obstacle ob = new Obstacle();
                    if (obstacle instanceof JOrderObstacle.WaitingForAdmission) {
                        ob.setType(ObstacleType.WaitingForAdmission);
                        ob.setUntil(Date.from(((JOrderObstacle.WaitingForAdmission) obstacle).until()));
                    } else if (obstacle instanceof JOrderObstacle.JobParallelismLimitReached) {
                        ob.setType(ObstacleType.JobParallelismLimitReached);
                    } else if (obstacle instanceof JOrderObstacle.WorkflowSuspended) {
                        ob.setType(ObstacleType.WorkflowIsSuspended);
                    } else {
                        return null;
                    }
                    return ob;
                }).filter(Objects::nonNull).collect(Collectors.toSet()));
                
            } catch (ControllerObjectNotExistException e) {
                Obstacle ob = new Obstacle();
                ob.setType(ObstacleType.OrderNotExisting);
                entry.setObstacles(Collections.singleton(ob));
            }
            
            entry.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(entry);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
