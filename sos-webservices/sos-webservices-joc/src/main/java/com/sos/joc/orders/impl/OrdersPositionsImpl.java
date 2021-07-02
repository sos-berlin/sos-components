package com.sos.joc.orders.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import com.sos.controller.model.workflow.WorkflowId;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.model.order.OrdersPositions;
import com.sos.joc.model.order.Positions;
import com.sos.joc.orders.resource.IOrdersPositions;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;

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
            
            JControllerState currentState = Proxy.of(controllerId).currentState();
            Stream<JOrder> orderStream = currentState.ordersBy(o -> orders.contains(o.id().string()));
            
            Map<Boolean, Set<JOrder>> suspendedOrFailedOrders = orderStream.collect(Collectors.groupingBy(o -> OrdersHelper.isSuspendedOrFailed(o),
                    Collectors.toSet()));
            
            if (!suspendedOrFailedOrders.containsKey(Boolean.TRUE)) {
                throw new JocBadRequestException("The orders are neither failed nor suspended");
            }
            
            orderStream = suspendedOrFailedOrders.getOrDefault(Boolean.TRUE, Collections.emptySet()).stream().filter(o -> canAdd(WorkflowPaths
                    .getPath(o.workflowId()), folderPermissions.getListOfFolders()));
            
            Map<JWorkflowId, Set<JOrder>> map = orderStream.collect(Collectors.groupingBy(o -> o.workflowId(), Collectors.toSet()));
            OrdersPositions entity = new OrdersPositions();
            
            if (map.isEmpty()) {
                throw new JocFolderPermissionsException("access denied");
            }
            
            if (map.size() > 1) {
                throw new JocBadRequestException("The orders must be from the same workflow. Found workflows are: " + map.keySet().toString());
            }
            
            JWorkflowId workflowId = map.keySet().iterator().next();
            Either<Problem, JWorkflow> e = currentState.repo().idToWorkflow(workflowId);
            ProblemHelper.throwProblemIfExist(e);
            WorkflowId w = new WorkflowId();
            w.setPath(workflowId.path().string());
            w.setVersionId(workflowId.versionId().string());
            entity.setWorkflowId(w);

            final Map<String, Integer> counterPerPos = new HashMap<>();
            final Set<Positions> pos = new HashSet<>();
            final List<String> orderIds = new ArrayList<>();
            map.get(workflowId).forEach(o -> {
                e.get().reachablePositions(o.workflowPosition().position()).stream().forEach(jPos -> {
                    Positions p = new Positions();
                    p.setPosition(jPos.toList());
                    p.setPositionString(jPos.toString());
                    pos.add(p);
                    orderIds.add(o.id().string());
                    counterPerPos.putIfAbsent(jPos.toString(), 0);
                    counterPerPos.computeIfPresent(jPos.toString(), (key, value) -> value + 1);
                });
            });

            entity.setOrderIds(orderIds);

            int countOrders = map.get(workflowId).size();
            Set<String> commonPos = counterPerPos.entrySet().stream().filter(entry -> entry.getValue() == countOrders).map(Map.Entry::getKey).collect(
                    Collectors.toSet());

            entity.setPositions(pos.stream().filter(p -> commonPos.contains(p.getPositionString())).collect(Collectors.toList()));
            if (entity.getPositions().isEmpty() && orderIds.size() > 1) {
                throw new JocBadRequestException("The orders " + orderIds.toString() + " don't have common allowed positions.");
            }

            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setSurveyDate(Date.from(currentState.instant()));
            
            if (suspendedOrFailedOrders.containsKey(Boolean.FALSE)) {
                String msg = suspendedOrFailedOrders.get(Boolean.FALSE).stream().map(o -> o.id().string()).collect(Collectors.joining("', '",
                        "Orders '", "' not failed or suspended"));
                ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
                //LOGGER.info(getJocError().printMetaInfo());
                //LOGGER.warn(msg);
                //getJocError().clearMetaInfo();
            }
            
            return JOCDefaultResponse.responseStatus200(entity);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
