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
import java.util.OptionalLong;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
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
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
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
import js7.data.workflow.Workflow;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.instructions.Cycle;
import js7.data.workflow.instructions.ImplicitEnd;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JRepo;
import js7.data_for_java.workflow.JWorkflowId;
import js7.data_for_java.workflow.position.JPosition;

// record W(JWorkflowId id, boolean isCycle) {}

@Path("workflows")
public class WorkflowsTimeConsumption extends JOCResourceImpl implements IWorkflowsTimeConsumption {

    private static final String API_CALL = "./workflows/time_consumption";
    // Permissible dispersion factor; should value between 2.5 and 3.0 -> 2.5 finds more outliers than 3.0
    private static final double DISPERSION = 3.0;
    private static final Pattern CYCLE_STEP = Pattern.compile("0/cycle\\+.*?,i=(\\d+).*");
    private static final Predicate<String> CYCLE_STEP_FILTER = CYCLE_STEP.asMatchPredicate();

    record CycleWorkflowId(JWorkflowId id, boolean isCycle) {
    };

    record CycleDuration(Duration workflowDuration, long cycleSteps) {
    };

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
            AuthFolders authFolders = getPermittedFoldersByControllerPermissions(controllerId, getControllerPermissionsPredicate().getWorkflows()
                .getView());

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(getResponse(in, authFolders, session)));

        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }

    private static WorkflowTimeConsumptions getResponse(WorkflowIdsFilter in, AuthFolders authFolders, SOSHibernateSession session) throws Exception {

        final String controllerId = in.getControllerId();
        final JControllerState currentstate = Proxy.of(controllerId).currentState();
        final Instant surveyInstant = currentstate.instant();

        JRepo jRepo = currentstate.repo();
        Map<String, Set<CycleWorkflowId>> wIds = in.getWorkflowIds()
            .stream()
            .distinct()
            .peek(wId -> wId.setPath(JocInventory.pathToName(wId.getPath())))
            .map(wId -> {
                if (wId.getVersionId() == null || wId.getVersionId().isEmpty()) {
                    return jRepo.pathToCheckedWorkflow(WorkflowPath.of(wId.getPath()));
                } else {
                    return jRepo.idToCheckedWorkflow(JWorkflowId.of(wId.getPath(), wId.getVersionId()));
                }
            })
            .filter(Either::isRight)
            .map(Either::get)
            .filter(w -> canAdd(WorkflowPaths.getPath(w.id()), authFolders))
            .map(w -> new CycleWorkflowId(w.id(), isCycleWorkflow(w.asScala())))
            .collect(Collectors.groupingBy(cw -> cw.id().path().string(), Collectors.toSet()));

        HistoryFilter dbFilter = new HistoryFilter();
        dbFilter.setControllerIds(Collections.singleton(controllerId));
        dbFilter.setWorkflowNames(wIds.keySet());
        dbFilter.setMainOrder(true);
        dbFilter.setState(HistoryStateText.SUCCESSFUL);
        session = Globals.createSosHibernateStatelessConnection(API_CALL);
        JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(session, dbFilter);

        Map<CycleWorkflowId, List<CycleDuration>> durationsPerWorkflow = new HashMap<>();

        try (ScrollableResults<DBItemHistoryOrder> sr = dbLayer.getMainOrders()) {
            while (sr.next()) {
                DBItemHistoryOrder item = sr.get();
                JWorkflowId wId = JWorkflowId.of(item.getWorkflowName(), item.getWorkflowVersionId());
                Optional<CycleWorkflowId> cwo = Optional.ofNullable(wIds.get(item.getWorkflowName()))
                    .flatMap(cws -> cws.stream().filter(cw -> cw.id().equals(wId)).findAny());
                if (cwo.isEmpty()) {
                    continue;
                }
                CycleWorkflowId cw = cwo.get();
                // TODO Maybe continue if order is not dailyplan order?
                Either<Exception, CycleDuration> d = getDuration(item, cw, session);
                if (d.isRight()) {
                    durationsPerWorkflow.putIfAbsent(cw, new ArrayList<>());
                    durationsPerWorkflow.get(cw).add(d.get());
                }
            }
        }

        wIds.values().stream().flatMap(Set::stream).forEach(cw -> durationsPerWorkflow.putIfAbsent(cw, Collections.emptyList()));

        WorkflowTimeConsumptions response = new WorkflowTimeConsumptions();
        response.setDeliveryDate(Date.from(surveyInstant));
        response.setWorkflows(durationsPerWorkflow.entrySet().stream().map(WorkflowsTimeConsumption::getWorkflowStatistics).toList());
        return response;
    }

    @SuppressWarnings("unchecked")
    private static <T> Optional<T> tryCast(Class<T> clazz, Object object) {
        return clazz.isAssignableFrom(object.getClass()) ? Optional.of((T) object) : Optional.empty();
    }

    private static boolean isCycleWorkflow(Workflow w) {
        boolean workflowStartsWithCycle = tryCast(Cycle.class, w.instruction(JPosition.fromList(List.of(0)).get().asScala())).isPresent();
        boolean workflowEndsWithCycle = tryCast(ImplicitEnd.class, w.instruction(JPosition.fromList(List.of(1)).get().asScala())).isPresent();
        return workflowStartsWithCycle && workflowEndsWithCycle;
    }

    private static WorkflowTimeConsumption getWorkflowStatistics(Map.Entry<CycleWorkflowId, List<CycleDuration>> entry) {
        return getWorkflowStatistics(entry.getKey(), entry.getValue());
    }

    private static WorkflowTimeConsumption getWorkflowStatistics(CycleWorkflowId wId, List<CycleDuration> durations) {
        List<Duration> workflowDurations = durations.stream().map(CycleDuration::workflowDuration).toList();
        DoubleSummaryStatistics statistics = getStatistics(workflowDurations);
        WorkflowTimeConsumption workflowResponse = new WorkflowTimeConsumption();
        workflowResponse.setPath(WorkflowPaths.getPath(wId.id().path().string()));
        workflowResponse.setVersionId(wId.id().versionId().string());
        workflowResponse.setStatistics(statisticsToResponse(statistics));
        if (wId.isCycle()) {
            List<Duration> cycleDurations = durations.stream()
                .filter(cd -> cd.cycleSteps() > 0)
                .map(cd -> cd.workflowDuration().toMillis() / cd.cycleSteps())
                .map(Duration::ofMillis)
                .toList();
            DoubleSummaryStatistics cycleStatistics = getStatistics(cycleDurations);
            workflowResponse.setCycleStatistics(statisticsToResponse(cycleStatistics));
        }
        return workflowResponse;
    }

    private static final DoubleSummaryStatistics getStatistics(List<Duration> durations) {
        DoubleSummaryStatistics statistics = new DoubleSummaryStatistics(0, 0, 0, 0);
        if (!durations.isEmpty()) {
            double arithM = average(durations);
            double deviation = standardDeviation(durations, arithM);
            // outliers limit
            double dispersionLimit = deviation * DISPERSION;
            // Delete outliers
            statistics = durations.stream()
                .mapToDouble(Duration::toMillis)
                .filter(d -> d <= arithM + dispersionLimit)
                .filter(d -> d >= arithM - dispersionLimit)
                .summaryStatistics();
        }
        return statistics;
    }

    private static Either<Exception, CycleDuration> getDuration(DBItemHistoryOrder item, CycleWorkflowId cw, SOSHibernateSession session) {
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
            if (cw.isCycle()) {
                OrderLogEntry[] logEntries = Globals.objectMapper.readValue(historyDBItem.getFileContent(), OrderLogEntry[].class);
                if (Stream.of(logEntries).map(OrderLogEntry::getLogEvent).anyMatch(evt -> evt.isDisturbed())) {
                    return Either.left(new DBInvalidDataException("Order " + item.getOrderId() + " is disturbed (suspended/canceled/failed)"));
                }
                OptionalLong l = Stream.of(logEntries)
                    .map(OrderLogEntry::getPosition)
                    .filter(CYCLE_STEP_FILTER)
                    .map(s -> CYCLE_STEP.matcher(s).replaceFirst("$1"))
                    .mapToLong(Long::valueOf)
                    .max();
                if (l.isPresent()) {
                    return Either.right(new CycleDuration(Duration.between(item.getStartTime().toInstant(), item.getEndTime().toInstant()), l
                        .getAsLong()));
                }
            } else {
                if (Stream.of(Globals.objectMapper.readValue(historyDBItem.getFileContent(), OrderLogEntry[].class))
                    .map(OrderLogEntry::getLogEvent)
                    .anyMatch(evt -> evt.isDisturbed())) {
                    return Either.left(new DBInvalidDataException("Order " + item.getOrderId() + " is disturbed (suspended/canceled/failed)"));
                }
            }
            return Either.right(new CycleDuration(Duration.between(item.getStartTime().toInstant(), item.getEndTime().toInstant()), 0));
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
