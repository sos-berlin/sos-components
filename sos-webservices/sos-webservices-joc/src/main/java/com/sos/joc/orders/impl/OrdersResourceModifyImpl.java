package com.sos.joc.orders.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.common.Outcome;
import com.sos.controller.model.order.OrderModeType;
import com.sos.controller.model.workflow.HistoricOutcome;
import com.sos.controller.model.workflow.WorkflowId;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.CheckedOrdersPositions;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.db.dailyplan.DBItemDailyPlanOrder;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.dailyplan.DailyPlanEvent;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.order.CancelDailyPlanOrders;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.model.order.Positions;
import com.sos.joc.orders.resource.IOrdersResourceModify;
import com.sos.js7.order.initiator.DailyPlanSettings;
import com.sos.js7.order.initiator.classes.GlobalSettingsReader;
import com.sos.js7.order.initiator.db.DBLayerDailyPlannedOrders;
import com.sos.js7.order.initiator.db.FilterDailyPlannedOrders;
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
        CANCEL, CANCEL_DAILYPLAN, SUSPEND, RESUME, REMOVE_WHEN_TERMINATED, ANSWER_PROMPT
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
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getSuspendResume();
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postResumeOrders(modifyOrders);
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
            boolean perm = getControllerPermissions(modifyOrders.getControllerId(), accessToken).getOrders().getCancel();
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
    public JOCDefaultResponse postOrdersDailyPlanCancel(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL + "/dailyplan/cancel", filterBytes, accessToken);
            JsonValidator.validate(filterBytes, CancelDailyPlanOrders.class);
            CancelDailyPlanOrders cancelDailyPlanOrders = Globals.objectMapper.readValue(filterBytes, CancelDailyPlanOrders.class);

            boolean perm = getControllerPermissions(cancelDailyPlanOrders.getControllerId(), accessToken).getOrders().getCancel();
            JOCDefaultResponse jocDefaultResponse = initPermissions(cancelDailyPlanOrders.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            addSubmittedOrderIdsFromDailyplanDate(cancelDailyPlanOrders);
            ModifyOrders modifyOrders = new ModifyOrders();
            modifyOrders.setControllerId(cancelDailyPlanOrders.getControllerId());
            modifyOrders.setKill(false);
            modifyOrders.setOrderIds(cancelDailyPlanOrders.getOrderIds());
            modifyOrders.setOrderType(OrderModeType.FRESH_ONLY);
            modifyOrders.setAuditLog(cancelDailyPlanOrders.getAuditLog());

            postOrdersModify(Action.CANCEL_DAILYPLAN, modifyOrders);
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

    private DBItemDailyPlanOrder addCyclicOrderIds(List<String> orderIds, String orderId, String controllerId,
            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders) throws SOSHibernateException {
        DailyPlanSettings settings;
        if (Globals.configurationGlobals == null) {
            settings = new DailyPlanSettings();
            settings.setTimeZone("Etc/UTC");
            settings.setPeriodBegin("00:00");
            LOGGER.warn("Could not read settings. Using defaults");
        } else {
            GlobalSettingsReader reader = new GlobalSettingsReader();
            AConfigurationSection section = Globals.configurationGlobals.getConfigurationSection(DefaultSections.dailyplan);
            settings = reader.getSettings(section);
        }

        return dbLayerDailyPlannedOrders.addCyclicOrderIds(orderIds, orderId, controllerId, settings.getTimeZone(), settings.getPeriodBegin());
    }

    private void updateUnknownOrders(String controllerId, Set<String> orders, Set<JOrder> jOrders) throws SOSHibernateException {
        List<String> listOfOrderIds = new ArrayList<String>();
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(API_CALL + "/modify updateUnknownOrders");
            DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(session);

            FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
            filter.setControllerId(controllerId);

            for (String orderId : orders) {
                filter.setOrderId(orderId);
                if (dbLayerDailyPlannedOrders.getUniqueDailyPlan(filter) != null) {
                    listOfOrderIds.add(orderId);
                }
            }
            Globals.disconnect(session);
            session = null; // to avoid nested openSessions

            for (String orderId : orders) {
                addCyclicOrderIds(listOfOrderIds, orderId, controllerId, dbLayerDailyPlannedOrders);
            }

            Set<String> orderIds = jOrders.stream().map(o -> o.id().string()).collect(Collectors.toSet());
            listOfOrderIds.removeAll(orderIds);
            updateDailyPlan("updateUnknownOrders", listOfOrderIds);
        } finally {
            Globals.disconnect(session);
        }
    }

    public void postOrdersModify(Action action, ModifyOrders modifyOrders) throws Exception {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[postOrdersModify]action=%s", action));
        }
        CategoryType category = CategoryType.CONTROLLER;
        if (Action.CANCEL_DAILYPLAN.equals(action)) {
            category = CategoryType.DAILYPLAN;
        }
        String controllerId = modifyOrders.getControllerId();
        DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyOrders.getAuditLog(), controllerId, category);

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
            if (Action.SUSPEND.equals(action) || Action.CANCEL.equals(action) || Action.CANCEL_DAILYPLAN.equals(action)) {
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
            
            Function1<Order<Order.State>, Object> workflowStateFilter = getWorkflowStateFilter(modifyOrders, surveyDateMillis, workflowFilter);
            orderStream = currentState.ordersBy(workflowStateFilter).parallel().filter(getDateToFilter(modifyOrders, surveyInstant));
            orderStream = considerAdmissionOrders(orderStream, lookingForBlocked, lookingForInProgress, workflowStateFilter, currentState);
            
        } else if (withFolderFilter && (permittedFolders == null || permittedFolders.isEmpty())) {
            // no permission
        } else if (withFolderFilter && permittedFolders != null && !permittedFolders.isEmpty()) {
            Set<VersionedItemId<WorkflowPath>> workflowIds2 = WorkflowsHelper.getWorkflowIdsFromFolders(controllerId, permittedFolders.stream()
                    .collect(Collectors.toList()), currentState, permittedFolders);
            if (workflowIds2 != null && !workflowIds2.isEmpty()) {
                Function1<Order<Order.State>, Object> workflowFilter = o -> workflowIds2.contains(o.workflowId());
                
                Function1<Order<Order.State>, Object> workflowStateFilter = getWorkflowStateFilter(modifyOrders, surveyDateMillis, workflowFilter);
                orderStream = currentState.ordersBy(workflowStateFilter).parallel().filter(getDateToFilter(modifyOrders, surveyInstant));
                orderStream = considerAdmissionOrders(orderStream, lookingForBlocked, lookingForInProgress, workflowStateFilter, currentState);
            }
        }

        final Set<JOrder> jOrders = getJOrders(action, orderStream, controllerId, folderPermissions.getListOfFolders(), withOrders);

        if (Action.CANCEL_DAILYPLAN.equals(action)) {
            updateUnknownOrders(controllerId, orders, jOrders);
        }
        if (!jOrders.isEmpty() || Action.CANCEL_DAILYPLAN.equals(action)) {
            command(currentState, action, modifyOrders, dbAuditLog, jOrders.stream().map(JOrder::id).collect(Collectors.toSet())).thenAccept(
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

    public void postResumeOrders(ModifyOrders modifyOrders) throws Exception {

        String controllerId = modifyOrders.getControllerId();
        DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyOrders.getAuditLog(), controllerId, CategoryType.CONTROLLER);

        JControllerState currentState = Proxy.of(controllerId).currentState();
        Instant surveyInstant = currentState.instant();
        long surveyDateMillis = surveyInstant.toEpochMilli(); 

        Set<String> orders = modifyOrders.getOrderIds();
        List<WorkflowId> workflowIds = modifyOrders.getWorkflowIds();
        boolean withOrders = orders != null && !orders.isEmpty();
        boolean withFolderFilter = modifyOrders.getFolders() != null && !modifyOrders.getFolders().isEmpty();
        Set<Folder> permittedFolders = addPermittedFolder(modifyOrders.getFolders());

        if (withOrders) {
            //
        } else if (workflowIds != null && !workflowIds.isEmpty()) {
            Predicate<WorkflowId> versionNotEmpty = w -> w.getVersionId() != null && !w.getVersionId().isEmpty();
            Set<VersionedItemId<WorkflowPath>> workflowPaths = workflowIds.stream().filter(versionNotEmpty).map(w -> JWorkflowId.of(JocInventory
                    .pathToName(w.getPath()), w.getVersionId()).asScala()).collect(Collectors.toSet());
            Set<WorkflowPath> workflowPaths2 = workflowIds.stream().filter(w -> !versionNotEmpty.test(w)).map(w -> WorkflowPath.of(JocInventory
                    .pathToName(w.getPath()))).collect(Collectors.toSet());
            Function1<Order<Order.State>, Object> workflowFilter = o -> (workflowPaths.contains(o.workflowId()) || workflowPaths2.contains(o
                    .workflowId().path()));
            orders = currentState.ordersBy(getWorkflowStateFilter(modifyOrders, surveyDateMillis, workflowFilter)).parallel().filter(getDateToFilter(
                    modifyOrders, surveyInstant)).map(JOrder::id).map(OrderId::string).collect(Collectors.toSet());
        } else if (withFolderFilter && (permittedFolders == null || permittedFolders.isEmpty())) {
            // no permission
        } else if (withFolderFilter && permittedFolders != null && !permittedFolders.isEmpty()) {
            Set<VersionedItemId<WorkflowPath>> workflowIds2 = WorkflowsHelper.getWorkflowIdsFromFolders(controllerId, permittedFolders.stream()
                    .collect(Collectors.toList()), currentState, permittedFolders);
            if (workflowIds2 != null && !workflowIds2.isEmpty()) {
                Function1<Order<Order.State>, Object> workflowFilter = o -> workflowIds2.contains(o.workflowId());
                orders = currentState.ordersBy(getWorkflowStateFilter(modifyOrders, surveyDateMillis, workflowFilter)).parallel().filter(getDateToFilter(
                        modifyOrders, surveyInstant)).map(JOrder::id).map(OrderId::string).collect(Collectors.toSet());
            }
        }

        if (orders == null || orders.isEmpty()) {
            return;
        }

        Optional<JPosition> positionOpt = Optional.empty();
        if (modifyOrders.getPosition() != null && !modifyOrders.getPosition().isEmpty()) {
            Either<Problem, JPosition> posFromList = JPosition.fromList(modifyOrders.getPosition());
            if (posFromList.isLeft()) {
                ProblemHelper.throwProblemIfExist(posFromList);
            }
            positionOpt = Optional.of(posFromList.get());
        }

        boolean withVariables = modifyOrders.getVariables() != null && modifyOrders.getVariables().getAdditionalProperties() != null && !modifyOrders
                .getVariables().getAdditionalProperties().isEmpty();

        CheckedOrdersPositions cop = new CheckedOrdersPositions().get(orders, currentState, folderPermissions.getListOfFolders());
        final Set<JOrder> jOrders = cop.getJOrders();
        List<JHistoryOperation> historyOperations = Collections.emptyList();
        Set<String> allowedPositions = cop.getPositions().stream().map(Positions::getPositionString).collect(Collectors.toCollection(
                LinkedHashSet::new));

        if (positionOpt.isPresent()) {
            if (!cop.isSingleOrder() && cop.getDisabledPositionChange() != null) {
                throw new JocBadRequestException(cop.getDisabledPositionChange().getMessage());
            }

            if (!allowedPositions.contains(positionOpt.get().toString())) {
                if (cop.isSingleOrder() && cop.getCurrentPosition().toString().equals(positionOpt.get().toString())) {
                    positionOpt = Optional.empty();
                } else {
                    throw new JocBadRequestException("Disallowed position '" + positionOpt.get().toString() + "'. Allowed positions are: "
                            + allowedPositions.toString());
                }
            }
        }

        if (withVariables) {
            if (!cop.isSingleOrder()) {
                throw new JocBadRequestException("Variables can only be set for resuming a single order.");
            } else if (cop.getVariablesNotSettable() == Boolean.TRUE) {
                throw new JocBadRequestException("Variables can only be set if the order starts from the beginning in its scope.");
            }
        }

        if (!positionOpt.isPresent() && !withVariables) {
            command(currentState, Action.RESUME, modifyOrders, dbAuditLog, jOrders.stream().map(JOrder::id).collect(Collectors.toSet())).thenAccept(
                    either -> {
                        ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                        if (either.isRight()) {
                            OrdersHelper.storeAuditLogDetailsFromJOrders(jOrders, dbAuditLog.getId(), controllerId).thenAccept(either2 -> ProblemHelper
                                    .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
                        }
                    });
        } else if (cop.isSingleOrder()) {

            if (withVariables) {
                Set<String> allowedPositionsWithImplicitEnds = cop.getPositionsWithImplicitEnds().stream().map(Positions::getPositionString).collect(
                        Collectors.toCollection(LinkedHashSet::new));
                final String positionString = positionOpt.isPresent() ? positionOpt.get().toString() : "";
                boolean isNotFuturePosition = true;
                if (positionOpt.isPresent()) {
                    int posIndex = getIndex(allowedPositionsWithImplicitEnds, positionString);
                    int curPosIndex = getIndex(allowedPositionsWithImplicitEnds, cop.getCurrentPosition().toString());
                    isNotFuturePosition = posIndex <= curPosIndex;
                }

                // TODO for the time being: quick and dirty solution by replacing historicOutcome of previous allowed position
                List<Object> prevPos = null;
                String prevPosString = null;
                if (isNotFuturePosition) {
                    JOrder currentJOrder = jOrders.iterator().next();
                    Set<String> historicPositions = JavaConverters.asJava(currentJOrder.asScala().historicOutcomes()).stream().map(h -> JPosition
                            .apply(h.position())).map(p -> p.toString()).collect(Collectors.toCollection(LinkedHashSet::new));
                    Positions prevP = getPrevious(historicPositions, cop.getPositionsWithImplicitEnds(), positionString);
                    if (prevP != null) {
                        prevPos = prevP.getPosition();
                        prevPosString = prevP.getPositionString();
                    }
                } else {
                    prevPos = cop.getCurrentPosition().toList();
                    prevPosString = cop.getCurrentPosition().toString();
                }
                if (prevPos != null) {
                    final String prevPString = prevPosString;
                    Optional<HistoricOutcome> hoOpt = cop.getHistoricOutcomes().stream().filter(ho -> JPosition.fromList(ho.getPosition()).get()
                            .toString().equals(prevPString)).findFirst();
                    if (hoOpt.isPresent()) {
                        HistoricOutcome h = hoOpt.get();
                        Variables v = h.getOutcome().getNamedValues();
                        if (v == null) {
                            v = new Variables();
                        }
                        v.setAdditionalProperties(modifyOrders.getVariables().getAdditionalProperties());
                        h.getOutcome().setNamedValues(v);

                        String json = Globals.objectMapper.writeValueAsString(h);
                        JHistoricOutcome jH = JHistoricOutcome.fromJson(json).get();
                        historyOperations = Collections.singletonList(JHistoryOperation.replace(jH.asScala()));
                    } else {
                        Variables v = new Variables();
                        v.setAdditionalProperties(modifyOrders.getVariables().getAdditionalProperties());
                        HistoricOutcome h = new HistoricOutcome(prevPos, new Outcome("Succeeded", v, null));

                        String json = Globals.objectMapper.writeValueAsString(h);
                        JHistoricOutcome jH = JHistoricOutcome.fromJson(json).get();
                        historyOperations = Collections.singletonList(JHistoryOperation.insert(JPosition.fromList(prevPos).get(), jH.asScala()));
                    }
                }
            }

            ControllerApi.of(controllerId).resumeOrder(jOrders.iterator().next().id(), positionOpt, historyOperations).thenAccept(either -> {
                ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                if (either.isRight()) {
                    OrdersHelper.storeAuditLogDetailsFromJOrders(jOrders, dbAuditLog.getId(), controllerId).thenAccept(either2 -> ProblemHelper
                            .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
                }
            });
        } else {
            for (JOrder jOrder : jOrders) {
                ControllerApi.of(controllerId).resumeOrder(jOrder.id(), positionOpt, historyOperations).thenAccept(either -> {
                    ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                    if (either.isRight()) {
                        OrdersHelper.storeAuditLogDetailsFromJOrder(jOrder, dbAuditLog.getId(), controllerId).thenAccept(either2 -> ProblemHelper
                                .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
                    }
                });
            }
        }

        if (cop.hasNotSuspendedOrFailedOrders()) {
            String msg = cop.getNotSuspendedOrFailedOrdersMessage();
            ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
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

    private static Positions getPrevious(Set<String> historicPositions, Set<Positions> allowedPositions, String value) {
        Positions result = null;
        for (Positions entry : allowedPositions) {
            if (entry.getPositionString().equals(value)) {
                break;
            }
            if (historicPositions.contains(entry.getPositionString())) {
                result = entry;
            }
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
            throw new ControllerObjectNotExistException("No failed or suspended orders found.");
        case ANSWER_PROMPT:
            throw new ControllerObjectNotExistException("No prompting orders found.");
        default:
            throw new ControllerObjectNotExistException("No orders found.");
        }

    }

    private Set<JOrder> getJOrders(Action action, Stream<JOrder> orderStream, String controllerId, boolean withPostProblem) {
        switch (action) {
        case RESUME:
            Map<Boolean, Set<JOrder>> suspendedOrFailedOrders = orderStream.collect(Collectors.groupingBy(o -> OrdersHelper.isSuspendedOrFailed(o),
                    Collectors.toSet()));
            if (suspendedOrFailedOrders.containsKey(Boolean.FALSE)) {
                String msg = suspendedOrFailedOrders.get(Boolean.FALSE).stream().map(o -> o.id().string()).collect(Collectors.joining("', '",
                        "Orders '", "' not failed or suspended"));
                if (withPostProblem) {
                    ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
                }
            }
            return suspendedOrFailedOrders.getOrDefault(Boolean.TRUE, Collections.emptySet());
        case ANSWER_PROMPT:
            Map<Boolean, Set<JOrder>> promptingOrders = orderStream.collect(Collectors.groupingBy(o -> OrdersHelper.isPrompting(o), Collectors
                    .toSet()));
            if (promptingOrders.containsKey(Boolean.FALSE)) {
                String msg = promptingOrders.get(Boolean.FALSE).stream().map(o -> o.id().string()).collect(Collectors.joining("', '", "Orders '",
                        "' not prompting"));
                if (withPostProblem) {
                    ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
                }
            }
            return promptingOrders.getOrDefault(Boolean.TRUE, Collections.emptySet());
        case CANCEL_DAILYPLAN:
            Map<Boolean, Set<JOrder>> freshOrders = orderStream.collect(Collectors.groupingBy(o -> OrdersHelper.isPendingOrScheduledOrBlocked(o),
                    Collectors.toSet()));
            if (freshOrders.containsKey(Boolean.FALSE)) {
                String msg = freshOrders.get(Boolean.FALSE).stream().map(o -> o.id().string()).collect(Collectors.joining("', '", "Orders '",
                        "' not pending, scheduled or blocked"));
                if (withPostProblem) {
                    ProblemHelper.postProblemEventAsHintIfExist(Either.left(Problem.pure(msg)), getAccessToken(), getJocError(), controllerId);
                }
            }
            return freshOrders.getOrDefault(Boolean.TRUE, Collections.emptySet());
        default:
            return orderStream.collect(Collectors.toSet());
        }
    }

    private Set<JOrder> getJOrders(Action action, Stream<JOrder> orderStream, String controllerId, Set<Folder> permittedFolders,
            boolean withPostProblem) {
        final Set<JOrder> jOrders = getJOrders(action, orderStream, controllerId, withPostProblem);
        return jOrders.stream().filter(o -> canAdd(WorkflowPaths.getPath(o.workflowId()), permittedFolders)).collect(Collectors.toSet());
    }

    private static Stream<JOrder> cyclicFreshOrderIds(Collection<String> orderIds, JControllerState currentState) {
        Stream<JOrder> cyclicOrderStream = Stream.empty();
        // determine cyclic ids
        Set<String> freshCyclicIds = orderIds.stream().filter(s -> OrdersHelper.isCyclicOrderId(s)).map(s -> currentState.idToOrder(OrderId.of(s)))
                .filter(Optional::isPresent).map(Optional::get).filter(o -> Order.Fresh$.class.isInstance(o.asScala().state())).map(o -> OrdersHelper
                        .getCyclicOrderIdMainPart(o.id().string())).collect(Collectors.toSet());
        if (!freshCyclicIds.isEmpty()) {
            cyclicOrderStream = currentState.ordersBy(JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), o -> freshCyclicIds
                    .contains(OrdersHelper.getCyclicOrderIdMainPart(o.id().string()))));
        }
        return cyclicOrderStream;
    }

    private CompletableFuture<Either<Problem, Void>> command(JControllerState currentState, Action action, ModifyOrders modifyOrders,
            DBItemJocAuditLog dbAuditLog, Set<OrderId> oIds) throws SOSHibernateException {

        String controllerId = modifyOrders.getControllerId();

        switch (action) {
        case CANCEL_DAILYPLAN:
            if (modifyOrders.getOrderIds() != null) {
                Set<String> orders = modifyOrders.getOrderIds().stream().filter(s -> !s.matches(".*#(T|F|D)[0-9]+-.*")).collect(Collectors.toSet());
                orders.removeAll(oIds.stream().map(OrderId::string).collect(Collectors.toSet()));

                updateDailyPlan("command", orders);
            }

            if (oIds.isEmpty()) {
                return CompletableFuture.supplyAsync(() -> Either.right(null));
            } else {
                return OrdersHelper.cancelOrders(modifyOrders, oIds).thenApply(either -> {
                    if (either.isRight()) {
                        updateDailyPlan(oIds, controllerId);
                    }
                    return either;
                });
            }

        case CANCEL:
            return OrdersHelper.cancelOrders(modifyOrders, oIds).thenApply(either -> {
                // TODO @uwe: This update must be removed when dailyplan service receives events for order state changes
                if (either.isRight()) {
                    updateDailyPlan(oIds, controllerId);
                }
                return either;
            });

        case RESUME:
            return ControllerApi.of(controllerId).resumeOrders(oIds);

        case SUSPEND:
            if (modifyOrders.getKill() == Boolean.TRUE) {
                return ControllerApi.of(controllerId).suspendOrders(oIds, JSuspensionMode.kill());
            } else {
                return ControllerApi.of(controllerId).suspendOrders(oIds);
            }

        case ANSWER_PROMPT:
            // No bulk operation in API
            JControllerApi api = ControllerApi.of(controllerId);
            oIds.stream().map(oId -> JControllerCommand.apply(new ControllerCommand.AnswerOrderPrompt(oId))).forEach(command -> api.executeCommand(
                    command).thenAccept(either -> ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId)));
            return CompletableFuture.supplyAsync(() -> Either.right(null));

        default: // case REMOVE_WHEN_TERMINATED
            return ControllerApi.of(controllerId).deleteOrdersWhenTerminated(oIds);
        }
    }

    private void updateDailyPlan(Set<OrderId> oIds, String controllerId) {
        try {
            // only for non-temporary and non-file orders
            LOGGER.debug("Cancel orders. Calling updateDailyPlan");
            updateDailyPlan("updateDailyPlan", oIds.stream().map(OrderId::string).filter(s -> !s.matches(".*#(T|F|D)[0-9]+-.*")).collect(Collectors
                    .toSet()));
        } catch (Exception e) {
            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), controllerId);
        }
    }

    private static void updateDailyPlan(String caller, Collection<String> orderIds) throws SOSHibernateException {
        // SOSClassUtil.printStackTrace(true, LOGGER);
        SOSHibernateSession session = null;
        if (!orderIds.isEmpty()) {
            try {
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug(String.format("[updateDailyPlan][caller=%s][orderIds]%s", caller, String.join(",", orderIds)));
                }

                GlobalSettingsReader reader = new GlobalSettingsReader();
                DailyPlanSettings settings;
                if (Globals.configurationGlobals != null) {

                    AConfigurationSection configuration = Globals.configurationGlobals.getConfigurationSection(DefaultSections.dailyplan);
                    settings = reader.getSettings(configuration);
                } else {
                    settings = new DailyPlanSettings();
                    settings.setTimeZone("Etc/UTC");
                    settings.setPeriodBegin("00:00");
                }

                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.setListOfOrders(orderIds);
                filter.setSubmitted(false);

                session = Globals.createSosHibernateStatelessConnection(API_CALL + "/cancel");
                session.setAutoCommit(false);
                DBLayerDailyPlannedOrders dbLayer = new DBLayerDailyPlannedOrders(session);
                Globals.beginTransaction(session);
                dbLayer.setSubmitted(filter);
                Globals.commit(session);
                List<DBItemDailyPlanOrder> listOfOrders = dbLayer.getDailyPlanList(filter, 0);
                Globals.disconnect(session);
                session = null;

                Set<String> dailyPlanDays = new HashSet<String>();
                for (DBItemDailyPlanOrder order : listOfOrders) {
                    String dailyPlanDate = order.getDailyPlanDate(settings.getTimeZone());
                    if (!dailyPlanDays.contains(dailyPlanDate)) {
                        dailyPlanDays.add(dailyPlanDate);
                        EventBus.getInstance().post(new DailyPlanEvent(dailyPlanDate));
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
                LOGGER.debug(String.format("[updateDailyPlan][caller=%s]No orderIds to be updated in daily plan", caller));
            }
        }
    }

    private ModifyOrders initRequest(Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        initLogging(API_CALL + "/" + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validate(filterBytes, ModifyOrders.class);
        return Globals.objectMapper.readValue(filterBytes, ModifyOrders.class);
    }

    private void addSubmittedOrderIdsFromDailyplanDate(CancelDailyPlanOrders cancelDailyPlanOrders) throws Exception {
        if (cancelDailyPlanOrders.getDailyPlanDate() != null) {
            SOSHibernateSession sosHibernateSession = null;
            if (cancelDailyPlanOrders.getOrderIds() == null) {
                cancelDailyPlanOrders.setOrderIds(new LinkedHashSet<String>());
            }

            try {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
                sosHibernateSession.setAutoCommit(false);
                GlobalSettingsReader reader = new GlobalSettingsReader();
                DailyPlanSettings settings;
                if (Globals.configurationGlobals != null) {

                    AConfigurationSection configuration = Globals.configurationGlobals.getConfigurationSection(DefaultSections.dailyplan);
                    settings = reader.getSettings(configuration);
                } else {
                    settings = new DailyPlanSettings();
                    settings.setTimeZone("Etc/UTC");
                    settings.setPeriodBegin("00:00");
                }
                DBLayerDailyPlannedOrders dbLayerDailyPlannedOrders = new DBLayerDailyPlannedOrders(sosHibernateSession);

                FilterDailyPlannedOrders filter = new FilterDailyPlannedOrders();
                filter.setControllerId(cancelDailyPlanOrders.getControllerId());
                filter.setDailyPlanDate(cancelDailyPlanOrders.getDailyPlanDate(), settings.getTimeZone(), settings.getPeriodBegin());
                filter.setSubmitted(true);

                List<DBItemDailyPlanOrder> listOfPlannedOrders = dbLayerDailyPlannedOrders.getDailyPlanList(filter, 0);
                if (listOfPlannedOrders != null) {
                    cancelDailyPlanOrders.getOrderIds().addAll(listOfPlannedOrders.stream().map(DBItemDailyPlanOrder::getOrderId).collect(Collectors
                            .toSet()));
                }
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
        }
    }

    private static Predicate<JOrder> getDateToFilter(ModifyOrders modifyOrders, Instant surveyInstant) {
        Predicate<JOrder> dateToFilter = o -> true;
        if (modifyOrders.getDateTo() != null && !modifyOrders.getDateTo().isEmpty()) {
            String dateTo = modifyOrders.getDateTo();
            if ("0d".equals(dateTo)) {
                dateTo = "1d";
            }
            Instant dateToInstant = JobSchedulerDate.getInstantFromDateStr(dateTo, false, modifyOrders.getTimeZone());
            final Instant until = (dateToInstant.isBefore(surveyInstant)) ? surveyInstant : dateToInstant;
            dateToFilter = o -> {
                if (!o.asScala().isSuspended() && OrderStateText.SCHEDULED.equals(OrdersHelper.getGroupedState(o.asScala().state().getClass()))) {
                    if (o.scheduledFor().isPresent() && o.scheduledFor().get().isAfter(until)) {
                        if (o.scheduledFor().get().toEpochMilli() == JobSchedulerDate.NEVER_MILLIS.longValue()) {
                            return true;
                        }
                        return false;
                    }
                }
                return true;
            };
        }
        return dateToFilter;
    }

    private static Function1<Order<Order.State>, Object> getWorkflowStateFilter(ModifyOrders modifyOrders, long surveyDateMillis,
            Function1<Order<Order.State>, Object> workflowFilter) {
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
                freshOrderFilter = o -> o.scheduledFor().isEmpty() || (!o.scheduledFor().isEmpty() && o.scheduledFor().get()
                        .toEpochMilli() >= surveyDateMillis && o.scheduledFor().get().toEpochMilli() != JobSchedulerDate.NEVER_MILLIS);
            } else if (lookingForScheduled && lookingForBlocked && !lookingForPending) {
                freshOrderFilter = o -> o.scheduledFor().isEmpty() || (!o.scheduledFor().isEmpty() && o.scheduledFor().get()
                        .toEpochMilli() != JobSchedulerDate.NEVER_MILLIS);
            } else if (lookingForScheduled && !lookingForBlocked && lookingForPending) {
                freshOrderFilter = o -> o.scheduledFor().isEmpty() || (!o.scheduledFor().isEmpty() && o.scheduledFor().get()
                        .toEpochMilli() >= surveyDateMillis);
            } else if (!lookingForScheduled && lookingForBlocked && !lookingForPending) {
                freshOrderFilter = o -> !o.scheduledFor().isEmpty() && o.scheduledFor().get().toEpochMilli() < surveyDateMillis;
            } else if (!lookingForScheduled && !lookingForBlocked && lookingForPending) {
                freshOrderFilter = o -> !o.scheduledFor().isEmpty() && o.scheduledFor().get().toEpochMilli() == JobSchedulerDate.NEVER_MILLIS;
            } else if (!lookingForScheduled && lookingForBlocked && lookingForPending) {
                freshOrderFilter = o -> !o.scheduledFor().isEmpty() && (o.scheduledFor().get().toEpochMilli() < surveyDateMillis || o.scheduledFor()
                        .get().toEpochMilli() == JobSchedulerDate.NEVER_MILLIS);
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
            Function1<Order<Order.State>, Object> filter, JControllerState controllerState) {

        if (lookingForBlocked || lookingForInProgress) {
            long surveyDateMillis = controllerState.instant().toEpochMilli();
            Function1<Order<Order.State>, Object> blockedFilter = JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), o -> !o
                    .isSuspended() && !o.scheduledFor().isEmpty() && o.scheduledFor().get().toEpochMilli() < surveyDateMillis);

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