package com.sos.joc.lock.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.inventory.model.lock.Lock;
import com.sos.joc.Globals;
import com.sos.joc.classes.OrdersHelper;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.db.deploy.items.DeployedContent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.lock.common.LockEntry;
import com.sos.joc.model.lock.common.LockOrder;
import com.sos.joc.model.lock.common.LockWorkflow;
import com.sos.joc.model.lock.common.WorkflowLock;
import com.sos.joc.model.lock.common.WorkflowLockType;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.lock.Acquired;
import js7.data.lock.LockId;
import js7.data.lock.LockState;
import js7.data.order.OrderId;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.lock.JLockState;
import js7.proxy.javaapi.data.order.JOrder;
import scala.collection.JavaConverters;

public class LockEntryHelper {

    private final String controllerId;

    public LockEntryHelper(String controllerId) {
        this.controllerId = controllerId;
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(LockEntryHelper.class);

    public LockEntry getLockEntry(JControllerState controllerState, DeployedContent dc, String lockPath) throws Exception {
        if (dc == null || dc.getContent() == null || dc.getContent().isEmpty()) {
            throw new DBMissingDataException(String.format("Lock '%s' doesn't exist", lockPath));
        }
        int acquiredLockCount = 0;
        int ordersHoldingLocksCount = 0;
        int ordersWaitingForLocksCount = 0;
        Map<String, LockWorkflow> workflows = new HashMap<String, LockWorkflow>();

        Lock item = Globals.objectMapper.readValue(dc.getContent(), Lock.class);
        JLockState jLockState = getFromEither(controllerState.idToLockState(LockId.of(item.getId())), "LockId=" + item.getId());

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

            for (OrderId orderId : jLockState.orderIds()) {
                ordersHoldingLocksCount++;
                JOrder jo = getFromEither(controllerState.idToCheckedOrder(orderId), "OrderId=" + orderId.string());
                if (jo != null) {
                    LockOrder lo = new LockOrder();
                    lo.setOrder(OrdersHelper.mapJOrderToOrderV(jo, false, null, null, false));
                    lo.setLock(getWorkflowLock(sharedAcquired, item.getId(), orderId.string()));

                    LockWorkflow lw = getLockWorkflow(workflows, lo);
                    if (lw.getOrdersHoldingLocks() == null) {
                        lw.setOrdersHoldingLocks(new ArrayList<LockOrder>());
                    }
                    lw.getOrdersHoldingLocks().add(lo);
                }
            }
            for (OrderId orderId : jLockState.queuedOrderIds()) {
                ordersWaitingForLocksCount++;
                JOrder jo = getFromEither(controllerState.idToCheckedOrder(orderId), "OrderId=" + orderId.string());
                if (jo != null) {
                    LockOrder lo = new LockOrder();
                    lo.setOrder(OrdersHelper.mapJOrderToOrderV(jo, false, null, null, false));
                    lo.setLock(getWorkflowLock(item.getId(), WorkflowLockType.UNKNOWN));

                    LockWorkflow lw = getLockWorkflow(workflows, lo);
                    if (lw.getOrdersWaitingForLocks() == null) {
                        lw.setOrdersWaitingForLocks(new ArrayList<LockOrder>());
                    }
                    lw.getOrdersWaitingForLocks().add(lo);
                }
            }

        }
        item.setPath(dc.getPath());

        LockEntry entry = new LockEntry();
        entry.setAcquiredLockCount(acquiredLockCount);
        entry.setOrdersHoldingLocksCount(ordersHoldingLocksCount);
        entry.setOrdersWaitingForLocksCount(ordersWaitingForLocksCount);
        entry.setLock(item);
        entry.setWorkflows(workflows.values().stream().collect(Collectors.toList()));
        return entry;
    }

    private LockWorkflow getLockWorkflow(Map<String, LockWorkflow> workflows, LockOrder lo) {
        String workflowName = lo.getOrder().getWorkflowId().getPath();
        LockWorkflow lw = null;
        if (!workflows.containsKey(workflowName)) {
            lw = new LockWorkflow();
            SOSHibernateSession session = null;
            try {
                session = Globals.createSosHibernateStatelessConnection("getLockWorkflow");
                DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);

                Set<String> names = new HashSet<String>();
                names.add(workflowName);
                final Map<String, String> namePathMap = dbLayer.getNamePathMapping(controllerId, names, DeployType.WORKFLOW.intValue());
                if (namePathMap == null || !namePathMap.containsKey(workflowName)) {
                    lw.setPath(workflowName);
                } else {
                    lw.setPath(namePathMap.get(workflowName));
                }

            } catch (Throwable e) {
                lw.setPath(workflowName);
                LOGGER.error(e.toString(), e);
            } finally {
                if (session != null) {
                    session.close();
                }
            }
            lw.setVersionId(lo.getOrder().getWorkflowId().getVersionId());
            workflows.put(workflowName, lw);
        } else {
            lw = workflows.get(workflowName);
        }
        try {
            lo.getOrder().getWorkflowId().setPath(lw.getPath());
        } catch (Throwable e) {
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

    private WorkflowLock getWorkflowLock(String lockId, WorkflowLockType type) {
        WorkflowLock l = new WorkflowLock();
        l.setId(lockId);
        l.setType(type);
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
