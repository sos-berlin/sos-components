package com.sos.joc.workflow.impl;

import java.io.IOException;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.sos.controller.model.order.OrderModeType;
import com.sos.inventory.model.instruction.Instruction;
import com.sos.inventory.model.instruction.InstructionStateText;
import com.sos.inventory.model.instruction.NamedJob;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JobSchedulerDate;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.ControllerObjectNotExistException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.order.ModifyOrders;
import com.sos.joc.model.workflow.ModifyWorkflowLabels;
import com.sos.joc.workflow.resource.IWorkflowLabelsModify;
import com.sos.schema.JsonValidator;
import com.sos.schema.exception.SOSJsonSchemaException;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.base.problem.Problem;
import js7.data.controller.ControllerCommand.Response;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.data.workflow.Workflow;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.WorkflowPathControl;
import js7.data.workflow.position.Label;
import js7.data_for_java.controller.JControllerCommand;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.order.JFreshOrder;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.order.JOrderPredicates;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.position.JPositionOrLabel;
import js7.proxy.javaapi.JControllerProxy;
import reactor.core.publisher.Flux;
import scala.Function1;
import scala.collection.JavaConverters;

@Path("workflow")
public class WorkflowLabelsModifyImpl extends JOCResourceImpl implements IWorkflowLabelsModify {

    private static final String API_CALL = "./workflow/";

    private enum Action {
        SKIP, UNSKIP
    }
    
    private class WorkflowJobs {
        private WorkflowPath workflowPath;
        private Set<String> labels;
        
        public WorkflowJobs(ModifyWorkflowLabels workflowJob) {
            workflowPath = WorkflowPath.of(JocInventory.pathToName(workflowJob.getWorkflowPath()));
            labels = workflowJob.getLabels().stream().collect(Collectors.toSet());
        }
        
        public WorkflowPath getWorkflowPath() {
            return workflowPath;
        }
        
        public String getPath() {
            return workflowPath.string();
        }
        
        public Set<String> getLabels() {
            return labels;
        }
        
        public Map<Label, Boolean> getLabelsForSkipOrUnSkip(Boolean action) {
            Map<Label, Boolean> m = new HashMap<>(labels.size());
            labels.forEach(l -> m.put(Label.fromString(l), action));
            return m;
        }
    }
    
    @Override
    public JOCDefaultResponse skipWorkflows(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflowLabels modifyWorkflow = initRequest(Action.SKIP, accessToken, filterBytes);
            boolean perm = hasPermission(modifyWorkflow.getControllerId(), accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyWorkflow.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowJobsModify(Action.SKIP, modifyWorkflow);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse unskipWorkflows(String accessToken, byte[] filterBytes) {
        try {
            ModifyWorkflowLabels modifyWorkflow = initRequest(Action.UNSKIP, accessToken, filterBytes);
            boolean perm = hasPermission(modifyWorkflow.getControllerId(), accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions(modifyWorkflow.getControllerId(), perm);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            postWorkflowJobsModify(Action.UNSKIP, modifyWorkflow);
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void postWorkflowJobsModify(Action action, ModifyWorkflowLabels modifyWorkflow) throws Exception {

        String controllerId = modifyWorkflow.getControllerId();
        DBItemJocAuditLog dbAuditLog = storeAuditLog(modifyWorkflow.getAuditLog(), controllerId, CategoryType.CONTROLLER);
        JControllerProxy proxy = Proxy.of(controllerId);
        JControllerState currentState = proxy.currentState();

        WorkflowJobs wj = new WorkflowJobs(modifyWorkflow);
        checkFolderPermissions(WorkflowPaths.getPath(wj.getPath()));
        
        Either<Problem, JWorkflow> workflowV = currentState.repo().pathToCheckedWorkflow(wj.getWorkflowPath());
        if (workflowV == null || workflowV.isLeft()) {
            throw new ControllerObjectNotExistException("Couldn't find the the Workflow '" + wj.getPath() + "'.");
        }

        JWorkflow jWorkflow = workflowV.get();
        
        Optional<NamedJob> nj = checkWorkflow(action, wj, currentState, jWorkflow, new ArrayList<>(modifyWorkflow.getLabels()));
        CompletableFuture<Boolean> future = command(controllerId, action, wj, dbAuditLog);
        
        if (action.equals(Action.UNSKIP) && firstJobWasSkippedAndWillBeUnskipped(nj, modifyWorkflow)) { // JOC-1561 Reassigning orders
            future.thenAccept(bool -> {
                if (bool) {
                    // check if job is unskipped now in a loop of 10 seconds otherwise abort action
                    int i = 0;
                    JControllerState newCurrentState = proxy.currentState();
                    while (i < 10) {
                        Optional<WorkflowPathControl> controlState = WorkflowsHelper.getWorkflowPathControl(newCurrentState, wj.getWorkflowPath(),
                                false);
                        if (controlState.isPresent()) {
                            Set<String> skippedLabels = WorkflowsHelper.getSkippedLabels(controlState, false);
                            if (!skippedLabels.contains(nj.get().getLabel())) {
                                break;
                            }
                        }
                        try {
                            TimeUnit.SECONDS.sleep(1L);
                        } catch (InterruptedException e) {
                            //
                        }
                        newCurrentState = proxy.currentState();
                        i++;
                    }
                    if (i == 10) {
                        // publish warning TODO only if fresh orders exist
                        ProblemHelper.postMessageAsHintIfExist("Reassigning orders not possible", getAccessToken(), getJocError(), controllerId);
                    } else {
                        // collect fresh orders that are scheduled in future
                        ZoneId zoneId = OrdersHelper.getDailyPlanTimeZone();
                        Instant surveyDateInstant = newCurrentState.instant();
                        Long surveyDateMillis = surveyDateInstant.toEpochMilli();
                        Function1<Order<Order.State>, Object> scheduledFreshOrderFilter = o -> OrdersHelper.getScheduledForMillis(o, zoneId,
                                surveyDateMillis) >= surveyDateMillis && o.scheduledFor().get().toEpochMilli() != JobSchedulerDate.NEVER_MILLIS;
                        scheduledFreshOrderFilter = JOrderPredicates.and(o -> o.workflowId().equals(jWorkflow.id().asScala()), JOrderPredicates.and(
                                JOrderPredicates.and(JOrderPredicates.byOrderState(Order.Fresh$.class), o -> !o.isSuspended()),
                                scheduledFreshOrderFilter));
                        
                        // map collected orders to fresh orders
                        Function<JOrder, JFreshOrder> mapper = o -> JFreshOrder.of(o.id(), o.workflowId().path(), o.scheduledFor(), o.arguments(), o
                                .asScala().deleteWhenTerminated(), o.asScala().forceJobAdmission(), Optional.empty(), JavaConverters.asJava(o
                                        .asScala().stopPositions()).stream().map(JPositionOrLabel::apply).collect(Collectors.toSet()));
                        
                        Map<OrderId, JFreshOrder> freshOrders = newCurrentState.ordersBy(scheduledFreshOrderFilter).collect(Collectors.toMap(
                                JOrder::id, mapper));

                        ModifyOrders modifyOrders = new ModifyOrders();
                        modifyOrders.setControllerId(controllerId);
                        modifyOrders.setOrderType(OrderModeType.FRESH_ONLY);
                        
                        proxy.api().deleteOrdersWhenTerminated(freshOrders.keySet()).thenAccept(either -> {
                            ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
                            if (either.isRight()) {
                                OrdersHelper.cancelOrders(proxy.api(), modifyOrders, freshOrders.keySet()).thenAccept(either2 -> {
                                    ProblemHelper.postProblemEventIfExist(either2, getAccessToken(), getJocError(), controllerId);
                                    if (either2.isRight()) {
                                        // check if really all orders are cancelled. Only then the same orderId can be used for addOrders(...)
                                        try {
                                            for (int j = 0; j < 10; j++) {
                                                try {
                                                    TimeUnit.SECONDS.sleep(1L);
                                                    if (j < 9 && !proxy.currentState().orderIds().stream().anyMatch(o -> freshOrders.keySet().contains(o))) {
                                                        // all orders are cancelled
                                                        break;
                                                    }
                                                    if (j == 9) {
                                                        Set<OrderId> oIds = proxy.currentState().orderIds();
                                                        oIds.retainAll(freshOrders.keySet());
                                                        if (oIds.isEmpty()) {
                                                            // all orders are cancelled
                                                            break;
                                                        }
                                                        // addOrders not possible for retained oIds because not cancelled
                                                        // throw Problem
                                                        if (!oIds.isEmpty()) {
                                                            oIds.forEach(o -> freshOrders.remove(o));
                                                            Either<Problem, Void> e = Either.left(Problem.pure("The Orders " + oIds.toString()
                                                                    + " cannot be reassigned because they could not be deleted before within 10 seconds."));
                                                            ProblemHelper.postProblemEventIfExist(e, getAccessToken(), getJocError(), controllerId);
                                                        }
                                                    }
                                                } catch (Exception e) {
                                                    //
                                                }
                                            }
                                        } catch (Exception e) {
                                            //
                                        }
                                        
                                        proxy.api().addOrders(Flux.fromIterable(freshOrders.values())).thenAccept(either3 -> {
                                            ProblemHelper.postProblemEventIfExist(either3, getAccessToken(), getJocError(), controllerId);
//                                            if (either3.isRight()) {
//                                                storeAuditLogDetails(auditLogDetails, auditlogId).thenAccept(either5 -> ProblemHelper.postExceptionEventIfExist(
//                                                        either5, getAccessToken(), getJocError(), controllerId));
//                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                }
            });
        }
    }
    
    private boolean firstJobWasSkippedAndWillBeUnskipped(Optional<NamedJob> nj, ModifyWorkflowLabels modifyWorkflow) {
        if (nj.isPresent()) {
            NamedJob j = nj.get();
            if (modifyWorkflow.getLabels().contains(j.getLabel()) && j.getState() != null && InstructionStateText.SKIPPED.equals(j.getState()
                    .get_text())) {
                return true;
            }
        }
        return false;
    }
    
    private Optional<NamedJob> checkWorkflow(Action action, WorkflowJobs wj, JControllerState currentState, JWorkflow jWorkflow, List<String> requestedLabels) throws IOException {
        
        Set<String> knownLabels = new HashSet<>();
        Workflow wV = jWorkflow.asScala();
        requestedLabels.forEach(l -> {
            if (wV.labelToPosition(Label.fromString(l)).isRight()) {
                knownLabels.add(l);
            }
        });
        
        // check labels of older workflow versions
        WorkflowsHelper.oldJWorkflowIds(currentState).filter(wId -> wj.getPath().equals(wId.path().string())).map(wId -> currentState.repo()
                .idToCheckedWorkflow(wId)).filter(Either::isRight).map(Either::get).map(JWorkflow::asScala).forEach(w -> {
                    requestedLabels.forEach(l -> {
                        if (w.labelToPosition(Label.fromString(l)).isRight()) {
                            knownLabels.add(l);
                        }
                    });
                });

        wj.getLabels().retainAll(knownLabels);
        requestedLabels.removeAll(wj.getLabels());

        if (!requestedLabels.isEmpty()) {
            if (requestedLabels.size() == 1) {
                throw new ControllerObjectNotExistException("Couldn't find a Job instruction with the label '" + requestedLabels.get(0) + "' in the Workflow '" + wj.getPath()
                        + "'.");
            }
            throw new ControllerObjectNotExistException("Couldn't find a Job instructions with the labels " + requestedLabels.toString() + " in the workflow '" + wj.getPath()
                    + "'.");
        }

        Optional<NamedJob> nj = Optional.empty();
        Optional<WorkflowPathControl> controlState = WorkflowsHelper.getWorkflowPathControl(currentState, wj.getWorkflowPath(), false);
        if (controlState.isPresent()) {
            Set<String> skippedLabels = WorkflowsHelper.getSkippedLabels(controlState, false);
            
            switch (action) {
            case SKIP:
                wj.getLabels().removeAll(skippedLabels);
                if (wj.getLabels().isEmpty()) { // TODO or maybe better always raise an exception?
                    throw new JocBadRequestException("All requested labels are already skipped.");
                }
                break;
            case UNSKIP:
                wj.getLabels().retainAll(skippedLabels);
                if (wj.getLabels().isEmpty()) { // TODO or maybe better always raise an exception?
                    throw new JocBadRequestException("None of the requested labels are skipped.");
                }
                JsonNode node = Globals.objectMapper.readTree(jWorkflow.toJson());
                Instruction firstInstruction = Globals.objectMapper.reader().forType(new TypeReference<Instruction>() {}).readValue(node.get("instructions").get(0));
                nj = WorkflowsHelper.getFirstJob(firstInstruction, skippedLabels);
                break;
            }
        }
        
        return nj;
    }
    
    private ModifyWorkflowLabels initRequest(Action action, String accessToken, byte[] filterBytes) throws SOSJsonSchemaException, IOException {
        initLogging(API_CALL + action.name().toLowerCase(), filterBytes, accessToken);
        JsonValidator.validateFailFast(filterBytes, ModifyWorkflowLabels.class);
        return Globals.objectMapper.readValue(filterBytes, ModifyWorkflowLabels.class);
    }

    private CompletableFuture<Boolean> command(String controllerId, Action action, WorkflowJobs wj, DBItemJocAuditLog dbAuditLog) {
        JControllerCommand commmand = JControllerCommand.controlWorkflowPath(wj.getWorkflowPath(), Optional.empty(), wj.getLabelsForSkipOrUnSkip(
                Action.SKIP.equals(action)));
        // LOGGER.info(action.name().toLowerCase() + "-command: " + commmand.toJson());
        return ControllerApi.of(controllerId).executeCommand(commmand).thenApply(either -> thenAcceptHandler(either, controllerId, wj, dbAuditLog));
    }

    private boolean thenAcceptHandler(Either<Problem, Response> either, String controllerId, WorkflowJobs wj, DBItemJocAuditLog dbAuditLog) {
        ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), controllerId);
        if (either.isRight()) {
            WorkflowsHelper.storeAuditLogDetailsFromWorkflowPath(wj.getWorkflowPath(), dbAuditLog, controllerId).thenAccept(either2 -> ProblemHelper
                    .postExceptionEventIfExist(either2, getAccessToken(), getJocError(), controllerId));
            return true;
        }
        return false;
    }
    
    private boolean hasPermission(String controllerId, String accessToken) {
        return getControllerPermissions(controllerId, accessToken).getOrders().getManagePositions();
    }
}