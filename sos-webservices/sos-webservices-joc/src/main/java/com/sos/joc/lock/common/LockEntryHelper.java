package com.sos.joc.lock.common;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.common.SyncStateText;
import com.sos.controller.model.lock.Lock;
import com.sos.joc.Globals;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.classes.workflow.WorkflowsHelper;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.model.lock.common.LockEntry;
import com.sos.joc.model.lock.common.LockOrder;
import com.sos.joc.model.lock.common.LockWorkflow;
import com.sos.joc.model.lock.common.WorkflowLock;
import com.sos.joc.model.lock.common.WorkflowLockType;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.lock.Acquired;
import js7.data.lock.LockPath;
import js7.data.lock.LockState;
import js7.data.order.OrderId;
import js7.data.workflow.Instruction;
import js7.data.workflow.WorkflowPath;
import js7.data.workflow.instructions.LockInstruction;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.lock.JLockState;
import js7.data_for_java.order.JOrder;
import js7.data_for_java.workflow.JWorkflow;
import js7.data_for_java.workflow.JWorkflowId;
import scala.collection.JavaConverters;
import scala.jdk.javaapi.OptionConverters;

public class LockEntryHelper {

    private final String controllerId;
    private final Boolean compact;
    private final Integer limit;
    private final ZoneId zoneId;
    private final SOSHibernateSession session;
    private final Boolean withWorkflowTagsDisplayed;

    public LockEntryHelper(String controllerId, Boolean compact, Integer limit, ZoneId zoneId, SOSHibernateSession session) {
        this.controllerId = controllerId;
        this.compact = compact;
        this.limit = limit;
        this.zoneId = zoneId;
        this.session = session;
        this.withWorkflowTagsDisplayed = WorkflowsHelper.withWorkflowTagsDisplayed();
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LockEntryHelper.class);

    public LockEntry getLockEntry(JControllerState controllerState, DeployedContent dc) throws Exception {
        int acquiredLockCount = 0;
        int ordersHoldingLocksCount = 0;
        int ordersWaitingForLocksCount = 0;
        SyncStateText stateText = SyncStateText.UNKNOWN;
        Map<JWorkflowId, LockWorkflow> workflows = new HashMap<>();

        Lock item = Globals.objectMapper.readValue(dc.getContent(), Lock.class);
        item.setPath(dc.getPath());
        item.setVersionDate(dc.getCreated());
        item.setVersion(null);
        String lockId = JocInventory.pathToName(dc.getPath());
        
        //JLockState jLockState = getFromEither(controllerState.idToLockState(LockId.of(lockId)), "LockId=" + lockId);
        JLockState jLockState = null;
        if (controllerState != null) {
            stateText = SyncStateText.NOT_IN_SYNC;
            jLockState = controllerState.pathToLockState().get(LockPath.of(lockId));
            if (jLockState != null) {
                stateText = SyncStateText.IN_SYNC;
            }
        }
        
        item.setState(SyncStateHelper.getState(stateText));

        if (jLockState != null) {
            LockState ls = jLockState.asScala();
            acquiredLockCount = ls.acquired().lockCount();

            Map<String, Integer> sharedAcquired = null;
            if (ls.acquired() instanceof Acquired.NonExclusive) {
                Acquired.NonExclusive a = (Acquired.NonExclusive) ls.acquired();
                Map<OrderId, Object> map = JavaConverters.asJava(a.orderToCount());
                if (map != null) {
                    sharedAcquired = new HashMap<String, Integer>();
                    for (Map.Entry<OrderId, Object> entry : map.entrySet()) {
                        try {
                            sharedAcquired.put(entry.getKey().string(), (Integer) entry.getValue());
                        } catch (Throwable e) {
                            LOGGER.warn(String.format("[%s][%s]%s", entry.getKey(), entry.getValue(), e.toString()), e);
                        }
                    }
                }
            }
            
            final Collection<OrderId> lockOrderIds = jLockState.orderIds();
            final Collection<OrderId> lockQueuedOrderIds = jLockState.queuedOrderIds();
            
            if (controllerState != null) {
                if (compact != Boolean.TRUE) {
                    Long surveyDateMillis = controllerState.instant().toEpochMilli();
                    ToLongFunction<JOrder> compareScheduleFor = OrdersHelper.getCompareScheduledFor(zoneId, surveyDateMillis);

                    List<JOrder> lockJOrders = controllerState.ordersBy(o -> lockOrderIds.contains(o.id())).sorted(Comparator.comparingLong(
                            compareScheduleFor).reversed()).collect(Collectors.toList());

                    List<JOrder> lockQueuedJOrders = controllerState.ordersBy(o -> lockQueuedOrderIds.contains(o.id())).sorted(Comparator
                            .comparingLong(compareScheduleFor).reversed()).collect(Collectors.toList());

                    ConcurrentMap<JWorkflowId, Collection<String>> finalParamsPerWorkflow = Stream.of(lockJOrders, lockQueuedJOrders).flatMap(l -> l
                            .stream()).map(JOrder::workflowId).distinct().collect(Collectors.toConcurrentMap(Function.identity(), w -> OrdersHelper
                                    .getFinalParameters(w, controllerState)));

                    Map<String, Set<String>> lockOrderTags = OrderTags.getTags(controllerState.asScala().controllerId().string(), lockJOrders,
                            session);
                    Map<String, Set<String>> lockQueuedOrderTags = OrderTags.getTags(controllerState.asScala().controllerId().string(),
                            lockQueuedJOrders, session);

                    for (JOrder jo : lockJOrders) {
                        ordersHoldingLocksCount++;
                        LockOrder lo = new LockOrder();
                        if (ordersHoldingLocksCount <= limit) {
                            lo.setOrder(OrdersHelper.mapJOrderToOrderV(jo, controllerState, true, lockOrderTags, finalParamsPerWorkflow,
                                    surveyDateMillis, zoneId));
                        }
                        lo.setLock(getWorkflowLock(sharedAcquired, lockId, jo.id().string()));

                        LockWorkflow lw = getLockWorkflow(workflows, jo.workflowId());
                        if (lw.getOrdersHoldingLocks() == null) {
                            lw.setOrdersHoldingLocks(new ArrayList<LockOrder>());
                        }
                        lw.getOrdersHoldingLocks().add(lo);
                    }

                    Map<JWorkflowId, WorkflowLock> queuedWorkflowLocks = new HashMap<>();
                    for (JOrder jo : lockQueuedJOrders) {
                        ordersWaitingForLocksCount++;
                        LockOrder lo = new LockOrder();
                        if (ordersWaitingForLocksCount <= limit) {
                            lo.setOrder(OrdersHelper.mapJOrderToOrderV(jo, controllerState, true, lockQueuedOrderTags, finalParamsPerWorkflow,
                                    surveyDateMillis, zoneId));
                        }
                        lo.setLock(getWorkflowLock(controllerState, jo, queuedWorkflowLocks, lockId));

                        LockWorkflow lw = getLockWorkflow(workflows, jo.workflowId());
                        if (lw.getOrdersWaitingForLocks() == null) {
                            lw.setOrdersWaitingForLocks(new ArrayList<LockOrder>());
                        }
                        lw.getOrdersWaitingForLocks().add(lo);
                    }
                } else {
                    ordersHoldingLocksCount = controllerState.ordersBy(o -> lockOrderIds.contains(o.id())).mapToInt(e -> 1).sum();
                    ordersWaitingForLocksCount = controllerState.ordersBy(o -> lockQueuedOrderIds.contains(o.id())).mapToInt(e -> 1).sum();
                }
            }

        }

        LockEntry entry = new LockEntry();
        entry.setAcquiredLockCount(acquiredLockCount);
        entry.setOrdersHoldingLocksCount(ordersHoldingLocksCount);
        entry.setOrdersWaitingForLocksCount(ordersWaitingForLocksCount);
        entry.setLock(item);
        entry.setWorkflows(workflows.values().stream().collect(Collectors.toList()));
        if (withWorkflowTagsDisplayed) {
            entry.setWorkflowTagsPerWorkflow(WorkflowsHelper.getTagsPerWorkflow(session, workflows.keySet().stream().map(JWorkflowId::path).map(
                    WorkflowPath::string)));
        }
        return entry;
    }

    private LockWorkflow getLockWorkflow(Map<JWorkflowId, LockWorkflow> workflows, JWorkflowId wId) {
        LockWorkflow lw = null;
        if (workflows.containsKey(wId)) {
            lw = workflows.get(wId);
        } else {
            lw = new LockWorkflow();
            lw.setPath(wId.path().string());
            lw.setVersionId(wId.versionId().string());
            workflows.put(wId, lw);
        }
        return lw;
    }

    private WorkflowLock getWorkflowLock(Map<String, Integer> shared, String lockId, String orderId) {
        WorkflowLock l = new WorkflowLock();
        l.setId(lockId);
        if (shared == null || !shared.containsKey(orderId)) {
            l.setType(WorkflowLockType.EXCLUSIVE);
        } else {
            l.setType(WorkflowLockType.SHARED);
            l.setCount(shared.get(orderId));
        }
        return l;
    }

    private WorkflowLock getWorkflowLock(JControllerState controllerState, JOrder jo, Map<JWorkflowId, WorkflowLock> queuedWorkflowLocks,
            String lockId) {

        if (queuedWorkflowLocks.containsKey(jo.workflowId())) {
            return queuedWorkflowLocks.get(jo.workflowId());
        }

        WorkflowLock l = new WorkflowLock();
        l.setId(lockId);
        try {
            JWorkflow jd = getFromEither(controllerState.repo().idToCheckedWorkflow(jo.workflowId()), "workflow=" + jo.workflowId().path().string())
                    .withPositions();
            Instruction in = jd.asScala().instruction(jo.workflowPosition().position().asScala());
            if (in != null && in instanceof LockInstruction) {
                JavaConverters.asJava(((LockInstruction) in).demands()).stream().filter(ld -> lockId.equals(ld.lockPath().string())).findAny()
                        .ifPresent(ld -> {
                            Optional<Object> c = OptionConverters.toJava(ld.count());
                            if (c != null && c.isPresent()) {
                                l.setType(WorkflowLockType.SHARED);
                                l.setCount((Integer) c.get());
                            } else {
                                l.setType(WorkflowLockType.EXCLUSIVE);
                            }
                        });
            }
        } catch (Throwable e) {
            LOGGER.error(String.format("[LockId=%s][%s]%s", lockId, jo.workflowId().path().string(), e.toString()), e);
        }
        if (l.getType() == null) {
            l.setType(WorkflowLockType.UNKNOWN);
        }
        queuedWorkflowLocks.put(jo.workflowId(), l);
        return l;
    }

    private <T> T getFromEither(Either<Problem, T> either, String msg) {
        if (either.isLeft()) {
            LOGGER.warn(String.format("[controller=%s][%s]%s", controllerId, msg, either.getLeft()));
            return null;
        }
        return either.get();
    }

}
