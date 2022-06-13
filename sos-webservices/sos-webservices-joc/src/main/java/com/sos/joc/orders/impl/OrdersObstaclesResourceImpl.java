package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.Obstacle;
import com.sos.joc.model.order.ObstacleType;
import com.sos.joc.model.order.Obstacles;
import com.sos.joc.model.order.Obstacles200;
import com.sos.joc.model.order.OrdersFilterV;
import com.sos.joc.orders.resource.IOrdersObstaclesResource;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowControlState;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderObstacle;
import scala.collection.JavaConverters;

@Path("orders")
public class OrdersObstaclesResourceImpl extends JOCResourceImpl implements IOrdersObstaclesResource {

    private static final String API_CALL = "./orders/obstacles";

    @Override
    public JOCDefaultResponse postOrders(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrdersFilterV.class);
            OrdersFilterV ordersFilter = Globals.objectMapper.readValue(filterBytes, OrdersFilterV.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(ordersFilter.getControllerId(), getControllerPermissions(ordersFilter
                    .getControllerId(), accessToken).getOrders().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JControllerState currentState = Proxy.of(ordersFilter.getControllerId()).currentState();
            Instant surveyDateInstant = currentState.instant();
            Set<OrderId> orderIds = null;
            if (ordersFilter.getOrderIds() == null || ordersFilter.getOrderIds().isEmpty()) {
                orderIds = currentState.orderIds();
            } else {
                orderIds = ordersFilter.getOrderIds().stream().map(o -> OrderId.of(o)).collect(Collectors.toSet());
            }
            Either<Problem, Map<OrderId, Set<JOrderObstacle>>> obstaclesE = currentState.ordersToObstacles(orderIds, surveyDateInstant);
            ProblemHelper.throwProblemIfExist(obstaclesE);

            Map<WorkflowPath, WorkflowControlState> controlStates = JavaConverters.asJava(currentState.asScala().pathToWorkflowControlState_());

            Obstacles200 entry = new Obstacles200();
            entry.setSurveyDate(Date.from(surveyDateInstant));
            List<Obstacles> obstaclesSet = new ArrayList<>();
            
            for (Map.Entry<OrderId, Set<JOrderObstacle>> obstacles : obstaclesE.get().entrySet()) {
                Obstacles obs = new Obstacles();
                obs.setOrderId(obstacles.getKey().string());
                Set<Obstacle> obstacleSet = new HashSet<>();
                
                JOrder order = currentState.idToOrder().get(obstacles.getKey());
                if (order != null) {
                    WorkflowControlState controlState = controlStates.get(order.workflowId().path());
                    if (controlState != null && controlState.workflowControl().suspended()) {
                        Obstacle ob = new Obstacle();
                        ob.setType(ObstacleType.WorkflowIsSuspended);
                    }
                }
                for (JOrderObstacle obstacle : obstacles.getValue()) {
                    Obstacle ob = new Obstacle();
                    if (obstacle instanceof JOrderObstacle.WaitingForAdmission) {
                        ob.setType(ObstacleType.WaitingForAdmission);
                        ob.setUntil(Date.from(((JOrderObstacle.WaitingForAdmission) obstacle).until()));
                        obstacleSet.add(ob);
                    } else if (obstacle instanceof JOrderObstacle.JobParallelismLimitReached) {
                        ob.setType(ObstacleType.JobParallelismLimitReached);
                        obstacleSet.add(ob);
                    }
                }
                if (!obstacleSet.isEmpty()) {
                    obs.setObstacles(obstacleSet);
                    obstaclesSet.add(obs);
                }
            }
            
            entry.setOrders(obstaclesSet);
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
