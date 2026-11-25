package com.sos.joc.workflows.impl;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.DoubleSummaryStatistics;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.hibernate.ScrollableResults;

import com.sos.auth.records.AuthFolders;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.HistoryFilter;
import com.sos.joc.db.history.JobHistoryDBLayer;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.HistoryStateText;
import com.sos.joc.model.history.order.OrderLogEntry;
import com.sos.joc.model.workflow.WorkflowIdsFilter;
import com.sos.joc.model.workflow.WorkflowTimeConsumption;
import com.sos.joc.model.workflow.WorkflowTimeConsumptions;
import com.sos.joc.model.workflow.WorkflowTimeStatistics;
import com.sos.joc.workflows.resource.IWorkflowsTimeConsumption;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import jakarta.ws.rs.Path;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JRepo;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;

@Path("workflows")
public class WorkflowsTimeConsumption extends JOCResourceImpl implements IWorkflowsTimeConsumption {

    private static final String API_CALL = "./workflows/time_consumption";
    // Permissible dispersion factor; should value between 2.5 and 3.0 -> 2.5 finds more outliers than 3.0
    private static final double DISPERSION = 3.0;

    @Override
    public JOCDefaultResponse postTimeConsumption(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.DAILYPLAN);
            JsonValidator.validateFailFast(filterBytes, WorkflowIdsFilter.class);
            WorkflowIdsFilter in = Globals.objectMapper.readValue(filterBytes, WorkflowIdsFilter.class);
            String controllerId = in.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(getBasicControllerPermissions(controllerId).getWorkflows().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            AuthFolders authFolders = getPermittedFoldersByControllerPermissions(controllerId, getControllerPermissionsPredicate().
                    getWorkflows().getView());

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(getResponse(in, authFolders, session)));

        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }
    
    private static WorkflowTimeConsumptions getResponse(WorkflowIdsFilter in, AuthFolders authFolders, SOSHibernateSession session)
            throws ControllerConnectionResetException, ControllerConnectionRefusedException, DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, ExecutionException {

        final String controllerId = in.getControllerId();
        final JControllerState currentstate = Proxy.of(controllerId).currentState();
        final Instant surveyInstant = currentstate.instant();

        JRepo jRepo = currentstate.repo();
        Map<String, Set<JWorkflowId>> wIds = in.getWorkflowIds().stream().distinct().peek(wId -> wId.setPath(JocInventory.pathToName(wId.getPath())))
                .map(wId -> {
                    if (wId.getVersionId() == null || wId.getVersionId().isEmpty()) {
                        return jRepo.pathToCheckedWorkflow(WorkflowPath.of(wId.getPath()));
                    } else {
                        return jRepo.idToCheckedWorkflow(JWorkflowId.of(wId.getPath(), wId.getVersionId()));
                    }
                }).filter(Either::isRight).map(Either::get).map(JWorkflow::id).filter(w -> canAdd(WorkflowPaths.getPath(w), authFolders)).collect(
                        Collectors.groupingBy(w -> w.path().string(), Collectors.toSet()));

        HistoryFilter dbFilter = new HistoryFilter();
        dbFilter.setControllerIds(Collections.singleton(controllerId));
        dbFilter.setWorkflowNames(wIds.keySet());
        dbFilter.setMainOrder(true);
        dbFilter.setState(HistoryStateText.SUCCESSFUL);
        session = Globals.createSosHibernateStatelessConnection(API_CALL);
        JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(session, dbFilter);

        Map<JWorkflowId, List<Duration>> durationsPerWorkflow = new HashMap<>();

        try (ScrollableResults<DBItemHistoryOrder> sr = dbLayer.getMainOrders()) {
            while (sr.next()) {
                DBItemHistoryOrder item = sr.get();
                JWorkflowId wId = JWorkflowId.of(item.getWorkflowName(), item.getWorkflowVersionId());
                if (Optional.ofNullable(wIds.get(item.getWorkflowName())).filter(s -> s.contains(wId)).isEmpty()) {
                    continue;
                }
                // TODO Maybe continue if order is not dailyplan order?
                Either<Exception, Duration> d = getDuration(item, session);
                if (d.isRight()) {
                    durationsPerWorkflow.putIfAbsent(wId, new ArrayList<>());
                    durationsPerWorkflow.get(wId).add(d.get());
                }
            }
        }
        
        wIds.values().stream().flatMap(Set::stream).forEach(wId -> durationsPerWorkflow.putIfAbsent(wId, new ArrayList<>()));

        WorkflowTimeConsumptions response = new WorkflowTimeConsumptions();
        response.setDeliveryDate(Date.from(surveyInstant));
        response.setWorkflows(durationsPerWorkflow.entrySet().stream().map(WorkflowsTimeConsumption::getWorkflowStatistics).toList());
        return response;
    }
    
    private static WorkflowTimeConsumption getWorkflowStatistics(Map.Entry<JWorkflowId, List<Duration>> entry) {
        return getWorkflowStatistics(entry.getKey(), entry.getValue());
    }
    
    private static WorkflowTimeConsumption getWorkflowStatistics(JWorkflowId wId, List<Duration> durations) {
        DoubleSummaryStatistics statistics = new DoubleSummaryStatistics(0, 0, 0, 0);
        if (!durations.isEmpty()) {
            double arithM = average(durations);
            double deviation = standardDeviation(durations, arithM);
            // outliers limit
            double dispersionLimit = deviation * DISPERSION;
            // Delete outliers
            statistics = durations.stream().mapToDouble(Duration::toMillis).filter(d -> d <= arithM + dispersionLimit).filter(d -> d >= arithM
                    - dispersionLimit).summaryStatistics();
        }
        WorkflowTimeConsumption workflowResponse = new WorkflowTimeConsumption();
        workflowResponse.setPath(WorkflowPaths.getPath(wId.path().string()));
        workflowResponse.setVersionId(wId.versionId().string());
        workflowResponse.setStatistics(statisticsToResponse(statistics));
        return workflowResponse;
    }
    
    private static Either<Exception, Duration> getDuration(DBItemHistoryOrder item, SOSHibernateSession session) {
        if (item.getLogId() == 0L) {
            return Either.left(new DBInvalidDataException("Log of " + item.getOrderId() + " couldn't be found"));
        }
        if (item.getEndTime() == null) {
            return Either.left(new DBInvalidDataException("Undefined end time of " + item.getOrderId()));
        }
        if (item.getStartTime() == null) {
            return Either.left(new DBInvalidDataException("Undefined start time of " + item.getOrderId()));
        }
        DBItemHistoryLog historyDBItem;
        try {
            historyDBItem = session.get(DBItemHistoryLog.class, item.getLogId());
            if (historyDBItem == null) {
                return Either.left(new DBMissingDataException("Log of " + item.getOrderId() + " couldn't be found"));
            }
            if (historyDBItem.fileContentIsNull()) {
                return Either.left(new DBInvalidDataException("Empty log of " + item.getOrderId()));
            }
            if (Stream.of(Globals.objectMapper.readValue(historyDBItem.getFileContent(), OrderLogEntry[].class)).map(OrderLogEntry::getLogEvent)
                    .anyMatch(evt -> evt.isDisturbed())) {
                return Either.left(new DBInvalidDataException("Order " + item.getOrderId() + " is disturbed (suspended/canceled/failed)"));
            }
            return Either.right(Duration.between(item.getStartTime().toInstant(), item.getEndTime().toInstant()));
        } catch (SOSHibernateException e) {
            return Either.left(e);
        } catch (IOException e) {
            return Either.left(e);
        }
    }
    
    private static double average(List<Duration> durations) {
        return durations.stream().mapToDouble(Duration::toMillis).summaryStatistics().getAverage();
    }
    
    private static double standardDeviation(List<Duration> durations, final double average) {
        return Math.sqrt(durations.stream().mapToDouble(Duration::toMillis).map(l -> Math.pow(l - average, 2)).summaryStatistics().getAverage());
    }
    
    private static WorkflowTimeStatistics statisticsToResponse(DoubleSummaryStatistics statistics) {
        WorkflowTimeStatistics stats = new WorkflowTimeStatistics();
        stats.setCount(statistics.getCount());
        if (statistics.getCount() != 0l) {
            stats.setAverage(statistics.getAverage() / 1000);
            stats.setMax(statistics.getMax() / 1000);
            stats.setMin(statistics.getMin() / 1000);
        }
        return stats;
    }
}
