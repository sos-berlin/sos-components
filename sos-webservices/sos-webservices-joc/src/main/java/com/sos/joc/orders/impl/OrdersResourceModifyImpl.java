package com.sos.joc.orders.impl;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.common.Outcome;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.common.Variables;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.CheckedResumeOrdersPositions;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.common.JOCOrderResourceImpl;
import com.sos.joc.dailyplan.db.DBLayerDailyPlannedOrders;
import com.sos.joc.dailyplan.db.FilterDailyPlannedOrders;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocAccessDeniedException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.Position;
import com.sos.joc.orders.resource.IOrdersResourceModify;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.controller.ControllerCommand;
import js7.data.item.VersionedItemId;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.command.JSuspensionMode;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JHistoricOutcome;
import js7.data_for_java.order.JHistoryOperation;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;
import js7.proxy.javaapi.JControllerApi;
import scala.Function1;
import scala.collection.JavaConverters;

@Path("orders")
public class OrdersResourceModifyImpl extends JOCResourceImpl implements IOrdersResourceModify {

    private static final String API_CALL = "./orders";
    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersResourceModifyImpl.class);

    private enum Action {
        CANCEL, SUSPEND, RESUME, REMOVE_WHEN_TERMINATED, ANSWER_PROMPT
    }

    @Override
    public JOCDefaultResponse postOrdersSuspend(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.SUSPEND, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getSuspendResume();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postOrdersModify(Action.SUSPEND, modifyOrders);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postOrdersResume(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.RESUME, accessToken, filterBytes);
            boolean permSuspendResume = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getSuspendResume();
            boolean permResumeFailed = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getResumeFailed();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), permSuspendResume || permResumeFailed);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postResumeOrders(modifyOrders, permResumeFailed && !permSuspendResume);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postOrdersCancel(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.CANCEL, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getCancel();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postOrdersModify(Action.CANCEL, modifyOrders);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postOrdersConfirm(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.ANSWER_PROMPT, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getConfirm();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postOrdersModify(Action.ANSWER_PROMPT, modifyOrders);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postOrdersRemoveWhenTerminated(String accessToken, byte[] filterBytes) {
        try {
            ModifyOrders modifyOrders = initRequest(Action.REMOVE_WHEN_TERMINATED, accessToken, filterBytes);
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getView();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postOrdersModify(Action.REMOVE_WHEN_TERMINATED, modifyOrders);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    public void postOrdersModify(Action action, ModifyOrders modifyOrders) throws Exception {
        CategoryType category = CategoryType.CONTROLLER;
        String controllerId = modifyOrders.getControllerId();
        DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyOrders.getAuditLog(), controllerId, category);
        ZoneId zoneId = OrdersHelper.getDailyPlanTimeZone();

        Set<String> orders = modifyOrders.getOrderIds();
        List<WorkflowId> workflowIds = modifyOrders.getWorkflowIds();
        boolean withOrders = orders != null && !orders.isEmpty();
        boolean withFolderFilter = modifyOrders.getFolders() != null && !modifyOrders.getFolders().isEmpty();
        Set<Folder> permittedFolders = addPermittedFolder(modifyOrders.getFolders());

        JControllerState currentState = Proxy.of(controllerId).currentState();
        Instant surveyInstant = currentState.instant();
        long surveyDateMillis = surveyInstant.toEpochMilli();
        Stream<JOrder> orderStream = Stream.empty();

        final boolean withStatesFilter = modifyOrders.getStates() != null && !modifyOrders.getStates().isEmpty();
        final boolean lookingForBlocked = withStatesFilter && modifyOrders.getStates().contains(OrderStateText.BLOCKED);
        final boolean lookingForInProgress = withStatesFilter && modifyOrders.getStates().contains(OrderStateText.INPROGRESS);

        if (withOrders) {
            orderStream = currentState.ordersBy(o -> orders.contains(o.id().string()));
            // determine possibly fresh cyclic orders in case of CANCEL or SUSPEND
            if (Action.SUSPEND.equals(action) || Action.CANCEL.equals(action)) {
                // determine cyclic ids
                orderStream = Stream.concat(orderStream, cyclicFreshOrderIds(orders, currentState));
            }
        } else if (workflowIds != null && !workflowIds.isEmpty()) {
            Predicate<WorkflowId> versionNotEmpty = w -> w.getVersionId() != null && !w.getVersionId().isEmpty();
            Set<VersionedItemId<WorkflowPath>> workflowPaths = workflowIds.stream().filter(versionNotEmpty).map(w -> JWorkflowId.of(JocInventory
                    .pathToName(w.getPath()), w.getVersionId()).asScala()).collect(Collectors.toSet());
            Set<WorkflowPath> workflowPaths2 = workflowIds.stream().filter(w -> !versionNotEmpty.test(w)).map(w -> WorkflowPath.of(JocInventory
                    .pathToName(w.getPath()))).collect(Collectors.toSet());
            Function1<Order<Order.State>, Object> workflowFilter = o -> (workflowPaths.contains(o.workflowId()) || workflowPaths2.contains(o
                    .workflowId().path()));

            Function1<Order<Order.State>, Object> workflowStateFilter = getWorkflowStateFilter(modifyOrders, surveyDateMillis, workflowFilter,
                    zoneId);
            orderStream = currentState.ordersBy(workflowStateFilter).parallel();
            if (Action.SUSPEND.equals(action) || Action.CANCEL.equals(action)) {
                orderStream = orderStream.filter(getDateToFilter(modifyOrders, action));
            }
            orderStream = considerAdmissionOrders(orderStream, lookingForBlocked, lookingForInProgress, workflowStateFilter, currentState, zoneId);

        } else if (withFolderFilter && (permittedFolders == null || permittedFolders.isEmpty())) {
            // no permission
        } else {
            Set<VersionedItemId<WorkflowPath>> workflowIds2 = WorkflowsHelper.getWorkflowIdsFromFolders(controllerId, permittedFolders.stream()
                    .collect(Collectors.toList()), currentState, permittedFolders);
            if (workflowIds2 != null && !workflowIds2.isEmpty()) {
                Function1<Order<Order.State>, Object> workflowFilter = o -> workflowIds2.contains(o.workflowId());

                Function1<Order<Order.State>, Object> workflowStateFilter = getWorkflowStateFilter(modifyOrders, surveyDateMillis, workflowFilter,
                        zoneId);
                orderStream = currentState.ordersBy(workflowStateFilter).parallel();
                if (Action.SUSPEND.equals(action) || Action.CANCEL.equals(action)) {
                    orderStream = orderStream.filter(getDateToFilter(modifyOrders, action));
                }
                orderStream = considerAdmissionOrders(orderStream, lookingForBlocked, lookingForInProgress, workflowStateFilter, currentState,
                        zoneId);
            }
        }

        final Set<JOrder> jOrders = getJOrders(action, orderStream, controllerId, folderPermissions.getListOfFolders(), withOrders);

        if (!jOrders.isEmpty()) {
            command(currentState, action, modifyOrders, dbAuditLog, jOrders).thenAccept(
                    either -> {
                        ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                        if (either.isRight()) {
                            OrdersHelper.storeAuditLogDetailsFromJOrders(jOrders, dbAuditLog.getId(), controllerId).thenAccept(
                                    either2 -> ProblemHelper.postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
                        }
                    });
        } else {
            throwControllerObjectNotExistException(action);
        }
    }

    @SuppressWarnings("unchecked")
    public void postResumeOrders(ModifyOrders modifyOrders, boolean hasOnlyResumeFailedPermission) throws Exception {

        String controllerId = modifyOrders.getControllerId();
        DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyOrders.getAuditLog(), controllerId, CategoryType.CONTROLLER);

        JControllerState currentState = Proxy.of(controllerId).currentState();
        Instant surveyInstant = currentState.instant();
        long surveyDateMillis = surveyInstant.toEpochMilli();
        ZoneId zoneId = OrdersHelper.getDailyPlanTimeZone();

        Set<String> orders = modifyOrders.getOrderIds();
        List<WorkflowId> workflowIds = modifyOrders.getWorkflowIds();
        boolean withOrders = orders != null && !orders.isEmpty();
        boolean withFolderFilter = modifyOrders.getFolders() != null && !modifyOrders.getFolders().isEmpty();
        Set<Folder> permittedFolders = addPermittedFolder(modifyOrders.getFolders());
        
        if (withOrders) {
            //
            if (hasOnlyResumeFailedPermission) {
                if (currentState.ordersBy(o -> modifyOrders.getOrderIds().contains(o.id().string())).anyMatch(OrdersHelper::isNotFailed)) {
                    throw new JocAccessDeniedException("Resuming only for failed orders permitted");
                }
            }
        } else if (workflowIds != null && !workflowIds.isEmpty()) {
            Predicate<WorkflowId> versionNotEmpty = w -> w.getVersionId() != null && !w.getVersionId().isEmpty();
            Set<VersionedItemId<WorkflowPath>> workflowPaths = workflowIds.stream().filter(versionNotEmpty).map(w -> JWorkflowId.of(JocInventory
                    .pathToName(w.getPath()), w.getVersionId()).asScala()).collect(Collectors.toSet());
            Set<WorkflowPath> workflowPaths2 = workflowIds.stream().filter(w -> !versionNotEmpty.test(w)).map(w -> WorkflowPath.of(JocInventory
                    .pathToName(w.getPath()))).collect(Collectors.toSet());
            Function1<Order<Order.State>, Object> workflowFilter = o -> (workflowPaths.contains(o.workflowId()) || workflowPaths2.contains(o
                    .workflowId().path()));
            Stream<JOrder> jOrdersStream = currentState.ordersBy(getWorkflowStateFilter(modifyOrders, surveyDateMillis, workflowFilter, zoneId))
                    .parallel().filter(getDateToFilter(modifyOrders, Action.RESUME));
            if (hasOnlyResumeFailedPermission && jOrdersStream.anyMatch(OrdersHelper::isNotFailed)) {
                throw new JocAccessDeniedException("Resuming only for failed orders permitted");
            }
            orders = jOrdersStream.map(JOrder::id).map(OrderId::string).collect(Collectors.toSet());
        } else if (withFolderFilter && (permittedFolders == null || permittedFolders.isEmpty())) {
            // no permission
        } else {
            Set<VersionedItemId<WorkflowPath>> workflowIds2 = WorkflowsHelper.getWorkflowIdsFromFolders(controllerId, permittedFolders.stream()
                    .collect(Collectors.toList()), currentState, permittedFolders);
            if (workflowIds2 != null && !workflowIds2.isEmpty()) {
                Function1<Order<Order.State>, Object> workflowFilter = o -> workflowIds2.contains(o.workflowId());
                Stream<JOrder> jOrdersStream = currentState.ordersBy(getWorkflowStateFilter(modifyOrders, surveyDateMillis, workflowFilter, zoneId))
                        .parallel().filter(getDateToFilter(modifyOrders, Action.RESUME));
                if (hasOnlyResumeFailedPermission && jOrdersStream.anyMatch(OrdersHelper::isNotFailed)) {
                    throw new JocAccessDeniedException("Resuming only for failed orders allowed");
                }
                orders = jOrdersStream.map(JOrder::id).map(OrderId::string).collect(Collectors.toSet());
            }
        }

        if (orders == null || orders.isEmpty()) {
            return;
        }

        Optional<JPosition> positionOpt = Optional.empty();
        Optional<String> workflowPositionStringOpt = Optional.empty();

        // JOC-1453 consider labels
        Object positionObj = modifyOrders.getPosition();
        boolean withPosition = positionObj != null && !((positionObj instanceof List<?> && ((List<Object>) positionObj).isEmpty()));

        boolean withVariables = modifyOrders.getVariables() != null && modifyOrders.getVariables().getAdditionalProperties() != null && !modifyOrders
                .getVariables().getAdditionalProperties().isEmpty();

        CheckedResumeOrdersPositions cop = new CheckedResumeOrdersPositions().get(orders, currentState, folderPermissions.getListOfFolders());
        final Set<JOrder> jOrders = cop.getJOrders();
        List<JHistoryOperation> historyOperations = Collections.emptyList();
        Set<String> allowedPositions = cop.getPositions().stream().map(Position::getPositionString).collect(Collectors.toCollection(
                LinkedHashSet::new));

        if (withPosition) {
            if (!cop.isSingleOrder() && cop.getDisabledPositionChange() != null) {
                throw new JocBadRequestException(cop.getDisabledPositionChange().getMessage());
            }
            if (cop.isSingleOrder()) {
                List<Object> pos = null;
                if (positionObj instanceof String) {
                    SOSHibernateSession connection = null;
                    try {
                        JWorkflowId jWorkflowId = jOrders.iterator().next().workflowId();
                        Map<String, List<Object>> labelMap = Collections.emptyMap();

                        connection = Globals.createSosHibernateStatelessConnection(API_CALL);
                        DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(connection);
                        DeployedContent dbWorkflow = dbLayer.getDeployedInventory(controllerId, DeployType.WORKFLOW.intValue(), jWorkflowId.path()
                                .string(), jWorkflowId.versionId().string());
                        Globals.disconnect(connection);
                        connection = null;
                        if (dbWorkflow != null) {
                            com.sos.inventory.model.workflow.Workflow w = JocInventory.workflowContent2Workflow(dbWorkflow.getContent());
                            if (w != null) {
                                labelMap = WorkflowsHelper.getLabelToPositionsMap(w);
                            }
                        }
                        pos = OrdersHelper.getPosition(positionObj, labelMap);

                    } finally {
                        Globals.disconnect(connection);
                    }

                } else if (positionObj instanceof List<?>) {
                    pos = (List<Object>) positionObj;
                    if (pos.isEmpty()) {
                        pos = null;
                    }
                }
                if (pos != null) {
                    Either<Problem, JPosition> posFromList = JPosition.fromList(pos);
                    if (posFromList.isLeft()) {
                        ProblemHelper.throwProblemIfExist(posFromList);
                    }
                    positionOpt = Optional.of(posFromList.get());
                }
            }
            
            workflowPositionStringOpt = positionOpt.map(pos -> cop.orderPositionToWorkflowPosition(pos
                    .toString()));

            if (positionOpt.isPresent() && !allowedPositions.contains(workflowPositionStringOpt.get())) {
                if (cop.isSingleOrder() && (cop.getCurrentWorkflowPosition().toString().equals(positionOpt.get().toString()) || cop
                        .getCurrentOrderPosition().toString().equals(positionOpt.get().toString()))) {
                    positionOpt = Optional.empty();
                } else {
                    throw new JocBadRequestException("Disallowed position '" + workflowPositionStringOpt.get() + "'. Allowed positions are: "
                            + allowedPositions.toString());
                }
            }
        }

        if (withVariables) {
            if (!cop.isSingleOrder()) {
                throw new JocBadRequestException("Variables can only be set for resuming a single order.");
            } else if (cop.getVariablesNotSettable() == Boolean.TRUE) {
                //throw new JocBadRequestException("Variables can only be set if the order starts from the beginning in its scope.");
            }
        }

        if (!positionOpt.isPresent() && !withVariables) {
            command(currentState, Action.RESUME, modifyOrders, dbAuditLog, jOrders).thenAccept(
                    either -> {
                        ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                        if (either.isRight()) {
                            OrdersHelper.storeAuditLogDetailsFromJOrders(jOrders, dbAuditLog.getId(), controllerId).thenAccept(
                                    either2 -> ProblemHelper.postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
                        }
                    });
        } else if (cop.isSingleOrder()) {
            
            if (withVariables) {
                Set<String> allowedPositionsWithImplicitEnds = cop.getPositionsWithImplicitEnds().stream().map(Position::getPositionString).collect(
                        Collectors.toCollection(LinkedHashSet::new));
                int posIndex = getIndex(allowedPositionsWithImplicitEnds, workflowPositionStringOpt.orElse(""));
                int curPosIndex = getIndex(allowedPositionsWithImplicitEnds, cop.getCurrentWorkflowPosition().toString());
                boolean isNotFuturePosition = posIndex <= curPosIndex;
                
                Map<String, Object> vars = modifyOrders.getVariables().getAdditionalProperties();
                if (vars.containsKey("returnCode") && vars.get("returnCode") instanceof String && ((String) vars.get("returnCode")).matches(
                        "\\d+")) {
                    modifyOrders.getVariables().setAdditionalProperty("returnCode", Long.valueOf((String) vars.get("returnCode")));
                }

                // TODO for the time being: quick and dirty solution by replacing historicOutcome of previous position
                List<Object> prevPos = null;
                String prevPosString = null;
                if (isNotFuturePosition) {
                    
                    JOrder currentJOrder = jOrders.iterator().next();
                    List<JPosition> historicPositions = JavaConverters.asJava(currentJOrder.asScala().historicOutcomes()).stream().map(h -> JPosition
                            .apply(h.position())).collect(Collectors.toCollection(LinkedList::new));
                    List<String> historicWorkflowPositions = historicPositions.stream().map(JPosition::toString).map(p -> cop
                            .orderPositionToWorkflowPosition(p)).collect(Collectors.toCollection(LinkedList::new));
                    int lastIndexOfWorkflowPos =  historicWorkflowPositions.lastIndexOf(workflowPositionStringOpt.orElse(""));
                    if (lastIndexOfWorkflowPos > 0) {
                        JPosition jPos = historicPositions.get(lastIndexOfWorkflowPos - 1);
                        if (jPos != null) {
                            prevPos = jPos.toList();
                            prevPosString = jPos.toString();
                        }
                    } else {
//                        prevPos = cop.getCurrentOrderPosition().toList();
//                        prevPosString = cop.getCurrentOrderPosition().toString();
                    }
                    
                } else {
                    prevPos = cop.getCurrentOrderPosition().toList();
                    prevPosString = cop.getCurrentOrderPosition().toString();
                }
                if (prevPos != null) {
//                    final String prevPString = prevPosString;
//                    Optional<HistoricOutcome> hoOpt = cop.getHistoricOutcomes().stream().filter(ho -> JPosition.fromList(ho.getPosition()).get()
//                            .toString().equals(prevPString)).findFirst();
                    HistoricOutcome h = null;
                    for (HistoricOutcome ho : cop.getHistoricOutcomes()) {
                        if (JPosition.fromList(ho.getPosition()).get().toString().equals(prevPosString)) {
                            h = ho;
                        }
                    }
                    if (h != null) {
                        //HistoricOutcome h = hoOpt.get();
                        Variables v = h.getOutcome().getNamedValues();
                        if (v == null) {
                            v = new Variables();
                        }
                        v.setAdditionalProperties(modifyOrders.getVariables().getAdditionalProperties());
                        if ("Disrupted".equals(h.getOutcome().getTYPE())) {
                            String errMsg = null;
                            try {
                                errMsg = h.getOutcome().getReason().getProblem().getMessage();
                            } catch (Exception e) {
                                //
                            }
                            h = new HistoricOutcome(prevPos, new Outcome("Failed", errMsg, v, null, null));
                        } else {
                            h.getOutcome().setNamedValues(v);
                        }
                        historyOperations = getJHistoricOperations(h, getJocError(), "replace", null);
                    } else {
                        Variables v = new Variables();
                        v.setAdditionalProperties(modifyOrders.getVariables().getAdditionalProperties());
                        historyOperations = getJHistoricOperations(new HistoricOutcome(prevPos, new Outcome("Succeeded", null, v, null, null)),
                                getJocError(), "append", null);
                    }
                } else {
                    Variables v = new Variables();
                    v.setAdditionalProperties(modifyOrders.getVariables().getAdditionalProperties());
                    List<Object> posList = new LinkedList<>(cop.getCurrentOrderPosition().toList());
                    int prev = ((Integer) posList.remove(posList.size() - 1)) - 1;
                    posList.add(Math.max(0, prev));
                    historyOperations = getJHistoricOperations(new HistoricOutcome(posList, new Outcome("Succeeded", null, v, null, null)),
                            getJocError(), "insert", cop.getCurrentOrderPosition());
                }
            }
            
            Optional<JPosition> orderPositionOpt = cop.workflowPositionToOrderPosition(positionOpt, modifyOrders.getCycleEndTime());

            OrderId oId = jOrders.iterator().next().id();
            // TODO handle grouped fresh cyclicOrders
            Set<OrderId> orderIds = Collections.emptySet();
            if (OrdersHelper.isCyclicOrderId(oId.string())) {
                orderIds = cyclicFreshOrderIds(Collections.singleton(oId.string()), currentState).filter(OrdersHelper::isResumable).map(
                        JOrder::id).collect(Collectors.toSet());
            }
            if (orderIds.isEmpty()) {
                orderIds = Collections.singleton(oId);
            }

            for (OrderId orderId : orderIds) {
                ControllerApi.of(controllerId).resumeOrder(orderId, orderPositionOpt, historyOperations, true).thenAccept(either -> {
                    ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                    if (either.isRight()) {
                        OrdersHelper.storeAuditLogDetailsFromJOrders(jOrders, dbAuditLog.getId(), controllerId).thenAccept(either2 -> ProblemHelper
                                .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
                    }
                });
            }
        } else {
            for (JOrder jOrder : jOrders) {
                Optional<JPosition> orderPositionOpt = cop.workflowPositionToOrderPosition(positionOpt, jOrder, modifyOrders.getCycleEndTime());
                ControllerApi.of(controllerId).resumeOrder(jOrder.id(), orderPositionOpt, historyOperations, true).thenAccept(either -> {
                    ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                    if (either.isRight()) {
                        OrdersHelper.storeAuditLogDetailsFromJOrder(jOrder, dbAuditLog.getId(), controllerId).thenAccept(either2 -> ProblemHelper
                                .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
                    }
                });
            }
        }
    }

    private static List<JHistoryOperation> getJHistoricOperations(HistoricOutcome h, JocError err, String command, JPosition posBefore)
            throws JsonProcessingException {
        String json = Globals.objectMapper.writeValueAsString(h);
        Either<Problem, JHistoricOutcome> hoE = JHistoricOutcome.fromJson(json);
        if (hoE.isLeft()) {
            ProblemHelper.postProblemEventIfExist(hoE, null, err, null);
            // ProblemHelper.postProblemEventAsHintIfExist(API_CALL + "/resume", hoE, null, err, null);
            return Collections.emptyList();
        }
        if ("append".equals(command)) {
            return Collections.singletonList(JHistoryOperation.append(hoE.get().asScala()));
        } else if ("replace".equals(command)) {
            return Collections.singletonList(JHistoryOperation.replace(hoE.get().asScala()));
        } else {
            return Collections.singletonList(JHistoryOperation.insert(posBefore, hoE.get().asScala()));
        }
    }

    private static int getIndex(Set<? extends Object> set, Object value) {
        int result = 0;
        for (Object entry : set) {
            if (entry.equals(value)) {
                return result;
            }
            result++;
        }
        return result;
    }

    // private static List<HistoricOutcome> subList(List<HistoricOutcome> hOutcomes, String positionString, String curPositionString,
    // Set<String> allowedPositions) {
    // if (hOutcomes.isEmpty()) {
    // return hOutcomes;
    // }
    // String posString = positionString;
    // if (posString == null || posString.isEmpty()) {
    // posString = curPositionString;
    // }
    // List<HistoricOutcome> subList = new ArrayList<>();
    // for (HistoricOutcome hOutcome : hOutcomes) {
    // String hOutcomePos = JPosition.fromList(hOutcome.getPosition()).get().toString();
    // if (hOutcomePos.equals(posString)) {
    // break;
    // }
    // if (!allowedPositions.contains(hOutcomePos)) {
    // continue;
    // }
    // subList.add(hOutcome);
    // }
    // return subList;
    // }

    private void throwControllerObjectNotExistException(Action action) throws ControllerObjectNotExistException {
        switch (action) {
        case RESUME:
            throw new ControllerObjectNotExistException("No resumable orders found.");
        case ANSWER_PROMPT:
            throw new ControllerObjectNotExistException("No prompting orders found.");
        case SUSPEND:
            throw new ControllerObjectNotExistException("No suspendible orders found.");
        default:
            throw new ControllerObjectNotExistException("No orders found.");
        }

    }

    public Set<JOrder> getJOrders(Action action, Stream<JOrder> orderStream, String controllerId, boolean withPostProblem) {
        switch (action) {
        case RESUME:
            Map<Boolean, Set<JOrder>> resumableOrders = orderStream.collect(Collectors.groupingBy(OrdersHelper::isResumable,
                    Collectors.toSet()));
            postProblem(resumableOrders, controllerId, withPostProblem, "resumable");
            return resumableOrders.getOrDefault(Boolean.TRUE, Collections.emptySet());
        case ANSWER_PROMPT:
            Map<Boolean, Set<JOrder>> promptingOrders = orderStream.collect(Collectors.groupingBy(OrdersHelper::isPrompting, Collectors
                    .toSet()));
            postProblem(promptingOrders, controllerId, withPostProblem, "prompting");
            return promptingOrders.getOrDefault(Boolean.TRUE, Collections.emptySet());
        case SUSPEND:
            Map<Boolean, Set<JOrder>> suspendibleOrders = orderStream.collect(Collectors.groupingBy(OrdersHelper::isSuspendible,
                    Collectors.toSet()));
            postProblem(suspendibleOrders, controllerId, withPostProblem, "suspendible");
            return suspendibleOrders.getOrDefault(Boolean.TRUE, Collections.emptySet());
        default:
            return orderStream.collect(Collectors.toSet());
        }
    }

    private Problem getProblem(Set<JOrder> orders, String message) {
        return Problem.pure(orders.stream().map(o -> o.id().string()).collect(Collectors.joining("', '", "Orders '", "' are not " + message)));
    }

    private void postProblem(Map<Boolean, Set<JOrder>> orders, String controllerId, boolean withPostProblem, String message) {
        if (withPostProblem && orders.containsKey(Boolean.FALSE)) {
            ProblemHelper.postProblemEventAsHintIfExist(Either.left(getProblem(orders.get(Boolean.FALSE), message)), getAccessToken(), getJocError(),
                    controllerId);
        }
    }

    private Set<JOrder> getJOrders(Action action, Stream<JOrder> orderStream, String controllerId, Set<Folder> permittedFolders,
            boolean withPostProblem) {
        final Set<JOrder> jOrders = getJOrders(action, orderStream, controllerId, withPostProblem);
        return jOrders.stream().filter(o -> canAdd(WorkflowPaths.getPath(o.workflowId()), permittedFolders)).collect(Collectors.toSet());
    }

    public static Stream<JOrder> cyclicFreshOrderIds(Collection<String> orderIds, JControllerState currentState) {
        Stream<JOrder> cyclicOrderStream = Stream.empty();
        // determine cyclic ids
        Map<OrderId, JOrder> knownOrders = currentState.idToOrder();
        Set<String> freshCyclicIds = orderIds.stream().filter(s -> OrdersHelper.isCyclicOrderId(s)).map(s -> knownOrders.get(OrderId.of(s))).filter(
                Objects::nonNull).filter(o -> Order.Fresh$.class.isInstance(o.asScala().state())).map(o -> OrdersHelper.getCyclicOrderIdMainPart(o
                        .id().string())).collect(Collectors.toSet());
        if (!freshCyclicIds.isEmpty()) {
            cyclicOrderStream = currentState.ordersBy(JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), o -> freshCyclicIds
                    .contains(OrdersHelper.getCyclicOrderIdMainPart(o.id().string()))));
        }
        return cyclicOrderStream;
    }

    private CompletableFuture<Either<Problem, Void>> command(JControllerState currentState, Action action, ModifyOrders modifyOrders,
            DBItemJocAuditLog dbAuditLog, Set<JOrder> jOrders) throws SOSHibernateException {

        String controllerId = modifyOrders.getControllerId();
        Stream<OrderId> oIdsStream = jOrders.stream().map(JOrder::id);

        switch (action) {
        case CANCEL:
            if (modifyOrders.getDeep() == Boolean.TRUE) {
                oIdsStream = Stream.concat(oIdsStream, getChildren(currentState, jOrders));
            }
            Set<OrderId> oIds = oIdsStream.collect(Collectors.toSet());
            return OrdersHelper.cancelOrders(modifyOrders, oIds).thenApply(either -> {
                // TODO @uwe: This update must be removed when dailyplan service receives events for order state changes
                if (either.isRight()) {
                    updateDailyPlan(oIds, controllerId);
                }
                return either;
            });

        case RESUME:
            Stream<OrderId> cyclicOrders = cyclicFreshOrderIds(jOrders.stream().map(JOrder::id).map(OrderId::string).collect(Collectors.toSet()),
                    currentState).filter(OrdersHelper::isResumable).map(JOrder::id);
            return ControllerApi.of(controllerId).resumeOrders(Stream.concat(oIdsStream, cyclicOrders).collect(Collectors.toSet()), true);

        case SUSPEND:
            if (modifyOrders.getDeep() == Boolean.TRUE) {
                oIdsStream = Stream.concat(oIdsStream, getChildren(currentState, jOrders));
            }
            if (modifyOrders.getKill() == Boolean.TRUE) {
                return ControllerApi.of(controllerId).suspendOrders(oIdsStream.collect(Collectors.toSet()), JSuspensionMode.kill());
            } else {
                return ControllerApi.of(controllerId).suspendOrders(oIdsStream.collect(Collectors.toSet()));
            }

        case ANSWER_PROMPT:
            // No bulk operation in API
            JControllerApi api = ControllerApi.of(controllerId);
            oIdsStream.map(oId -> JControllerCommand.apply(new ControllerCommand.AnswerOrderPrompt(oId))).forEach(command -> api.executeCommand(
                    command).thenAccept(either -> ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId)));
            return CompletableFuture.supplyAsync(() -> Either.right(null));

        default: // case REMOVE_WHEN_TERMINATED
            return ControllerApi.of(controllerId).deleteOrdersWhenTerminated(oIdsStream.collect(Collectors.toSet()));
        }
    }
    
    private Stream<OrderId> getChildren(JControllerState currentState, Set<JOrder> jOrders) {
        String regex = jOrders.stream().map(JOrder::id).map(OrderId::string).map(str -> str + "|").map(Pattern::quote).collect(Collectors
                .joining("|", "(", ").*"));
        return currentState.ordersBy(o -> o.id().string().matches(regex)).map(JOrder::id);
    }

    private void updateDailyPlan(Set<OrderId> oIds, String controllerId) {
        try {
            // only for non-temporary and non-file orders
            LOGGER.debug("Cancel orders. Calling updateDailyPlan");
            updateDailyPlan(oIds.stream().map(OrderId::string).filter(s -> !s.matches(".*#(T|F|D)[0-9]+-.*")).collect(Collectors.toSet()));
        } catch (Exception e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), controllerId);
        }
    }

    private static void updateDailyPlan(Collection<String> orderIds) throws SOSHibernateException {
        SOSHibernateSession session = null;
        if (!orderIds.isEmpty()) {
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[updateDailyPlan][orderIds=%s]%s", orderIds.size(), String.join(",", orderIds)));
                }

                DailyPlanSettings settings = JOCOrderResourceImpl.getDailyPlanSettings();

                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.setOrderIds(orderIds);
                filter.setSubmitted(false);
                filter.setSortMode(null);
                filter.setOrderCriteria(null);

                session = Globals.createSosHibernateStatelessConnection(API_CALL + "/cancel(updateDailyPlan)");
                session.setAutoCommit(false);
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
                Globals.beginTransaction(session);
                dbLayer.setSubmitted(filter);
                Globals.commit(session);
                List<DBItemDailyPlanOrder> items = dbLayer.getDailyPlanList(filter, 0);
                Globals.disconnect(session);
                session = null;

                Set<String> days = new HashSet<String>();
                for (DBItemDailyPlanOrder item : items) {
                    String date = item.getDailyPlanDate(settings.getTimeZone(), settings.getPeriodBegin());
                    if (!days.contains(date)) {
                        days.add(date);
                        EventBus.getInstance().post(new DailyPlanEvent(item.getControllerId(), date));
                    }
                }

            } catch (Exception e) {
                Globals.rollback(session);
                throw e;
            } finally {
                Globals.disconnect(session);
            }
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug("[updateDailyPlan]No orderIds to be updated in daily plan");
            }
        }
    }

    private ModifyOrders initRequest(Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        initLogging(API_CALL + "/" + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validate(filterBytes, ModifyOrders.class);
        return Globals.objectMapper.readValue(filterBytes, ModifyOrders.class);
    }

    private static Predicate<JOrder> getDateToFilter(ModifyOrders modifyOrders, Action action) {
        Predicate<JOrder> dateToFilter = o -> true;
        if ((modifyOrders.getDateFrom() != null && !modifyOrders.getDateFrom().isEmpty()) || (modifyOrders.getDateTo() != null && !modifyOrders
                .getDateTo().isEmpty())) {
            Instant dateToInstant = JobSchedulerDate.getInstantFromDateStr(modifyOrders.getDateTo(), true, modifyOrders.getTimeZone());
            Instant dateFromInstant = JobSchedulerDate.getInstantFromDateStr(modifyOrders.getDateFrom(), false, modifyOrders.getTimeZone());
            if (dateFromInstant != null && dateToInstant != null && dateFromInstant.isAfter(dateToInstant)) {
                throw new JocBadRequestException("'dateFrom' must be older than 'dateTo'");
            }

            dateToFilter = o -> {
                Instant scheduledFor = OrdersHelper.getScheduledForInstant(o);
                if (scheduledFor != null) {
                    if (scheduledFor.toEpochMilli() == JobSchedulerDate.NEVER_MILLIS.longValue()) { // pending orders
                        return false;
                    }
                    if (dateToInstant != null && !scheduledFor.isBefore(dateToInstant)) {
                        return false;
                    }
                    if (dateFromInstant != null && scheduledFor.isBefore(dateFromInstant)) {
                        return false;
                    }
                    return true;
                }
                return true;
            };

            if (Action.SUSPEND.equals(action)) {
                Predicate<JOrder> actionFilter = o -> !o.asScala().isSuspended();
                return actionFilter.and(dateToFilter);
            } else if (Action.RESUME.equals(action)) {
                Predicate<JOrder> actionFilter = o -> OrdersHelper.isResumable(o);
                return actionFilter.and(dateToFilter);
            } else {
                return dateToFilter;
            }
        }
        return dateToFilter;
    }

    private static Function1<Order<Order.State>, Object> getWorkflowStateFilter(ModifyOrders modifyOrders, long surveyDateMillis,
            Function1<Order<Order.State>, Object> workflowFilter, ZoneId zoneId) {
        List<OrderStateText> states = modifyOrders.getStates();
        Function1<Order<Order.State>, Object> stateFilter = null;

        if (states != null && !states.isEmpty()) {

            final boolean lookingForBlocked = states.contains(OrderStateText.BLOCKED);
            final boolean lookingForPending = states.contains(OrderStateText.PENDING);
            final boolean lookingForScheduled = states.contains(OrderStateText.SCHEDULED);

            Function1<Order<Order.State>, Object> freshOrderFilter = null;

            Function1<Order<Order.State>, Object> finishedFilter = JOrderPredicates.or(JOrderPredicates.or(JOrderPredicates.byOrderState(
                    Order.Finished$.class), JOrderPredicates.byOrderState(Order.Cancelled$.class)), JOrderPredicates.byOrderState(
                            Order.ProcessingKilled$.class));
            Function1<Order<Order.State>, Object> suspendFilter = JOrderPredicates.and(o -> o.isSuspended(), JOrderPredicates.not(finishedFilter));
            Function1<Order<Order.State>, Object> notSuspendFilter = JOrderPredicates.not(suspendFilter);

            states.remove(OrderStateText.SCHEDULED);
            states.remove(OrderStateText.PENDING);
            states.remove(OrderStateText.BLOCKED);

            Map<OrderStateText, Set<Class<? extends Order.State>>> m = OrdersHelper.groupByStateClasses.entrySet().stream().collect(Collectors
                    .groupingBy(Map.Entry::getValue, Collectors.mapping(Map.Entry::getKey, Collectors.toSet())));
            Iterator<Function1<Order<Order.State>, Object>> stateFilters = states.stream().filter(s -> m.containsKey(s)).flatMap(s -> m.get(s)
                    .stream()).map(JOrderPredicates::byOrderState).iterator();

            if (stateFilters.hasNext()) {
                stateFilter = stateFilters.next();
                while (stateFilters.hasNext()) {
                    stateFilter = JOrderPredicates.or(stateFilter, stateFilters.next());
                }
            }

            if (states.contains(OrderStateText.SUSPENDED)) {
                if (stateFilter == null) {
                    stateFilter = suspendFilter;
                } else {
                    stateFilter = JOrderPredicates.or(suspendFilter, stateFilter);
                }
            } else {
                if (stateFilter != null) {
                    stateFilter = JOrderPredicates.and(notSuspendFilter, stateFilter);
                }
            }

            if (lookingForScheduled && !lookingForBlocked && !lookingForPending) {
                freshOrderFilter = o -> OrdersHelper.getScheduledForMillis(o, zoneId, surveyDateMillis) >= surveyDateMillis && o.scheduledFor().get()
                        .toEpochMilli() != JobSchedulerDate.NEVER_MILLIS;
            } else if (lookingForScheduled && lookingForBlocked && !lookingForPending) {
                freshOrderFilter = o -> o.scheduledFor().isEmpty() || (!o.scheduledFor().isEmpty() && o.scheduledFor().get()
                        .toEpochMilli() != JobSchedulerDate.NEVER_MILLIS);
            } else if (lookingForScheduled && !lookingForBlocked && lookingForPending) {
                freshOrderFilter = o -> OrdersHelper.getScheduledForMillis(o, zoneId, surveyDateMillis) >= surveyDateMillis;
            } else if (!lookingForScheduled && lookingForBlocked && !lookingForPending) {
                freshOrderFilter = o -> OrdersHelper.getScheduledForMillis(o, zoneId, surveyDateMillis) < surveyDateMillis;
            } else if (!lookingForScheduled && !lookingForBlocked && lookingForPending) {
                freshOrderFilter = o -> !o.scheduledFor().isEmpty() && o.scheduledFor().get().toEpochMilli() == JobSchedulerDate.NEVER_MILLIS;
            } else if (!lookingForScheduled && lookingForBlocked && lookingForPending) {
                freshOrderFilter = o -> OrdersHelper.getScheduledForMillis(o, zoneId, surveyDateMillis) < surveyDateMillis || o.scheduledFor().get()
                        .toEpochMilli() == JobSchedulerDate.NEVER_MILLIS;
            }

            if (freshOrderFilter != null) {
                freshOrderFilter = JOrderPredicates.and(JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), o -> !o
                        .isSuspended()), freshOrderFilter);
            } else if (lookingForScheduled && lookingForBlocked && lookingForPending) {
                freshOrderFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), o -> !o.isSuspended());
            }

            if (stateFilter == null) {
                if (freshOrderFilter != null) {
                    stateFilter = freshOrderFilter;
                }
            } else {
                if (freshOrderFilter != null) {
                    stateFilter = JOrderPredicates.or(stateFilter, freshOrderFilter);
                }
            }
        }
        if (stateFilter == null) {
            return workflowFilter;
        } else {
            return JOrderPredicates.and(workflowFilter, stateFilter);
        }
    }

    private static Stream<JOrder> considerAdmissionOrders(Stream<JOrder> orderStream, boolean lookingForBlocked, boolean lookingForInProgress,
            Function1<Order<Order.State>, Object> filter, JControllerState controllerState, ZoneId zoneId) {

        if (lookingForBlocked || lookingForInProgress) {
            long surveyDateMillis = controllerState.instant().toEpochMilli();
            Function1<Order<Order.State>, Object> blockedFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), o -> !o
                    .isSuspended() && OrdersHelper.getScheduledForMillis(o, zoneId, surveyDateMillis) < surveyDateMillis);

            Set<JOrder> blockedOrders = controllerState.ordersBy(JOrderPredicates.and(filter, blockedFilter)).collect(Collectors.toSet());
            ConcurrentMap<OrderId, JOrder> blockedOrders2 = OrdersHelper.getWaitingForAdmissionOrders(blockedOrders, controllerState);
            Set<OrderId> blockedOrderIds = blockedOrders2.keySet();
            if (lookingForBlocked && !lookingForInProgress) {
                orderStream = orderStream.filter(o -> !blockedOrderIds.contains(o.id()));
            } else if (!lookingForBlocked && lookingForInProgress) {
                orderStream = Stream.concat(orderStream, blockedOrders2.values().stream()).distinct();
            }
        }

        return orderStream;
    }

}