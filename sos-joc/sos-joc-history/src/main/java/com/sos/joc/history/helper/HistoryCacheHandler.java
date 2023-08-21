package com.sos.joc.history.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.ExecutableJava;
import com.sos.inventory.model.job.ExecutableScript;
import com.sos.inventory.model.workflow.Workflow;
import com.sos.joc.classes.history.HistoryNotification;
import com.sos.joc.classes.inventory.search.WorkflowSearcher;
import com.sos.joc.classes.inventory.search.WorkflowSearcher.WorkflowJob;
import com.sos.joc.cluster.common.JocClusterUtil;
import com.sos.joc.db.history.DBItemHistoryAgent;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.history.controller.exception.model.HistoryModelOrderNotFoundException;
import com.sos.joc.history.controller.exception.model.HistoryModelOrderStepNotFoundException;
import com.sos.joc.history.controller.model.HistoryModel;
import com.sos.joc.history.db.DBLayerHistory;
import com.sos.joc.history.helper.CachedAgentCouplingFailed.AgentCouplingFailed;

public class HistoryCacheHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryCacheHandler.class);

    public static enum CacheType {
        order, orderWithChildOrders, orderStep
    };

    private static final String KEY_DELIMITER = "|||";

    private final String controllerId;

    private Map<String, CachedOrder> orders;
    private Map<String, CachedOrderStep> orderSteps;
    private Map<String, CachedAgent> agents;
    private Map<String, CachedWorkflow> workflows;
    private CachedAgentCouplingFailed agentsCouplingFailed;

    private String identifier;
    private boolean isDebugEnabled;

    public HistoryCacheHandler(String controllerId, String identifier) {
        this.controllerId = controllerId;

        init();
    }

    private void init() {
        orders = new HashMap<>();
        orderSteps = new HashMap<>();
        agents = new HashMap<>();
        workflows = new HashMap<>();
        agentsCouplingFailed = new CachedAgentCouplingFailed();
    }

    public void initLogLevels() {
        isDebugEnabled = LOGGER.isDebugEnabled();
    }

    public String getCachedSummary() {
        // TODO remove cached items - dependent of the created time
        int coSize = orders.size();
        int cosSize = orderSteps.size();
        // StringBuilder sb = new StringBuilder();
        // if (isDebugEnabled) {
        // LOGGER.debug(String.format("[cachedAgents=%s][cachedOrders=%s][cachedOrderSteps=%s]", cachedAgents.size(), coSize, cosSize));
        // }
        // if (coSize >= 1_000) {
        // sb.append("cachedOrders=").append(coSize);
        // } else {
        // if (isDebugEnabled && coSize > 0) {
        // // LOGGER.debug(SOSString.mapToString(cachedOrders, true));
        // }
        // }
        // if (cosSize >= 1_000) {
        // if (sb.length() > 0) {
        // sb.append(", ");
        // }
        // sb.append("cachedOrderSteps=").append(cosSize);
        // } else {
        // if (isDebugEnabled && cosSize > 0) {
        // // LOGGER.debug(SOSString.mapToString(cachedOrderSteps, true));
        // }
        // }
        // return sb.toString();
        return String.format("[cached workflows=%s,orders=%s,steps=%s]", workflows.size(), coSize, cosSize);
    }

    public boolean hasOrder(String orderId) {
        return orders.containsKey(orderId);
    }

    private CachedOrder getOrder(String orderId) {
        if (orders.containsKey(orderId)) {
            CachedOrder co = orders.get(orderId);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][cache][getOrder][%s]%s", identifier, orderId, SOSString.toString(co)));
            }
            return co;
        }
        return null;
    }

    public void addOrder(String orderId, CachedOrder co) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][cache][addOrder][%s]%s", identifier, orderId, SOSString.toString(co)));
        }
        orders.put(orderId, co);
    }

    public CachedOrder getOrderByConstraint(DBLayerHistory dbLayer, String constraintHash, String orderId, String constraintHashDetails)
            throws Exception {
        CachedOrder co = getOrder(orderId);
        if (co == null) {
            DBItemHistoryOrder item = dbLayer.getOrderByConstraint(constraintHash);
            if (item == null) {
                throw new HistoryModelOrderNotFoundException(controllerId, String.format("[%s][%s][%s]order not found", identifier, constraintHash,
                        constraintHashDetails));
            } else {
                addOrder(orderId, new CachedOrder(item));
            }
            co = getOrder(orderId);
        }
        if (co != null) {
            co.setLastUsage();
        }
        return co;
    }

    public CachedOrder getOrderByCurrentOrderId(DBLayerHistory dbLayer, String orderId, Long eventId) throws Exception {
        CachedOrder co = getOrder(orderId);
        if (co == null) {
            DBItemHistoryOrder item = dbLayer.getLastOrderByCurrentOrderId(controllerId, orderId);
            if (item == null) {
                throw new HistoryModelOrderNotFoundException(controllerId, String.format("[%s][%s][%s]order not found", identifier, orderId,
                        eventId));
            } else {
                addOrder(orderId, new CachedOrder(item));
            }
            co = getOrder(orderId);
        }
        if (co != null) {
            co.setLastUsage();
        }
        return co;
    }

    public void addOrderStep(String orderId, CachedOrderStep cos) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][cache][addOrderStep][%s]%s", identifier, orderId, SOSString.toString(cos)));
        }
        orderSteps.put(orderId, cos);
    }

    private CachedOrderStep getOrderStep(String orderId) {
        if (orderSteps.containsKey(orderId)) {
            CachedOrderStep co = orderSteps.get(orderId);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][cache][getOrderStep][%s]%s", identifier, orderId, SOSString.toString(co)));
            }
            return co;
        }
        return null;
    }

    public CachedOrderStep getOrderStepByConstraint(DBLayerHistory dbLayer, CachedAgent ca, String controllerTimezone, String constraintHash,
            String orderId, Long startEventId, String constraintHashDetails) throws Exception {
        DBItemHistoryOrderStep item = dbLayer.getOrderStepByConstraint(constraintHash);
        if (item == null) {
            throw new HistoryModelOrderStepNotFoundException(controllerId, String.format("[%s][%s][%s]order step not found", identifier, orderId,
                    constraintHashDetails));
        } else {
            if (ca == null) {
                LOGGER.info(String.format(
                        "[%s][agent not found]agent timezone can't be identified. set agent log timezone to controller timezone ...", item
                                .getAgentId()));
                addOrderStep(orderId, new CachedOrderStep(item, controllerTimezone));
            } else {
                addOrderStep(orderId, new CachedOrderStep(item, ca.getTimezone()));
            }
            return getOrderStep(orderId);
        }
    }

    public CachedOrderStep getOrderStepByOrder(DBLayerHistory dbLayer, CachedOrder co, String controllerTimezone, String workflowPosition)
            throws Exception {
        CachedOrderStep cos = getOrderStep(co.getOrderId());
        if (cos == null) {
            DBItemHistoryOrderStep item = dbLayer.getOrderStep(co.getCurrentHistoryOrderStepId());
            if (item != null && workflowPosition != null) {
                if (!item.getWorkflowPosition().equals(workflowPosition)) {
                    item = dbLayer.getOrderStepByWorkflowPosition(controllerId, co.getId(), workflowPosition);
                }
            }
            if (item == null) {
                throw new HistoryModelOrderStepNotFoundException(controllerId, String.format("[%s][%s][%s][%s]order step not found", identifier, co
                        .getOrderId(), co.getCurrentHistoryOrderStepId(), workflowPosition));
            } else {
                CachedAgent ca = getAgent(dbLayer, item.getAgentId(), controllerTimezone);
                cos = new CachedOrderStep(item, ca.getTimezone());
                addOrderStep(co.getOrderId(), cos);
            }
        }
        return cos;
    }

    public void addAgent(String agentId, CachedAgent ca) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][cache][addAgent][%s]%s", identifier, agentId, SOSString.toString(ca)));
        }
        agents.put(agentId, ca);
    }

    public void addAgentsCouplingFailed(String agentId, Long eventId, String message) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][cache][addAgentsCouplingFailed]%s", identifier, agentId));
        }
        agentsCouplingFailed.add(agentId, eventId, message);
    }

    public void removeAgentsCouplingFailed(String agentId) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][cache][removeAgentsCouplingFailed]%s", identifier, agentId));
        }
        agentsCouplingFailed.remove(agentId);
    }

    public AgentCouplingFailed getLastAgentCouplingFailed(String agentId, Long nextReadyEventId) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][cache][getLastAgentsCouplingFailed]%s", identifier, agentId));
        }
        return agentsCouplingFailed.getLast(agentId, nextReadyEventId);
    }

    public CachedAgent addAgentByReadyEventId(DBLayerHistory dbLayer, String agentId, Long readyEventId) {
        CachedAgent ca = null;
        try {
            ca = new CachedAgent(dbLayer.getAgentByReadyEventId(controllerId, readyEventId));
        } catch (Throwable e) {
            ca = getAgent(agentId);
        }
        addAgent(agentId, ca);
        return ca;
    }

    private CachedAgent getAgent(String agentId) {
        if (agents.containsKey(agentId)) {
            CachedAgent ca = agents.get(agentId);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][cache][getAgent][%s]%s", identifier, agentId, SOSString.toString(ca)));
            }
            return ca;
        }
        return null;
    }

    public CachedAgent getAgent(DBLayerHistory dbLayer, String agentId, String controllerTimezone) throws Exception {
        CachedAgent ca = getAgent(agentId);
        if (ca == null) {
            DBItemHistoryAgent item = dbLayer.getLastAgent(controllerId, agentId);
            if (item == null) {
                LOGGER.info(String.format("[%s][%s][%s]agent not found in the history. try to find in the agent instances...", identifier,
                        controllerId, agentId));
                DBItemInventoryAgentInstance inst = dbLayer.getAgentInstance(controllerId, agentId);
                // TODO read from controller API?
                if (inst == null) {
                    Date readyTime = new Date();
                    String uri = "http://localhost:4445";

                    LOGGER.info(String.format(
                            "[%s][%s][%s]agent not found in the agent instances. set agent timezone to controller timezone=%s, ready time=%s, uri=%s",
                            identifier, controllerId, agentId, controllerTimezone, HistoryModel.getDateAsString(readyTime), uri));

                    item = new DBItemHistoryAgent();
                    item.setReadyEventId(JocClusterUtil.getDateAsEventId(readyTime));
                    item.setControllerId(controllerId);
                    item.setAgentId(agentId);
                    item.setUri(uri);
                    item.setTimezone(HistoryUtil.getTimeZone("getAgent " + item.getAgentId(), controllerTimezone));
                    item.setReadyTime(readyTime);
                    item.setCreated(new Date());
                } else {
                    Date readyTime = inst.getStartedAt();
                    if (readyTime == null) {
                        readyTime = new Date();
                    }
                    LOGGER.info(String.format(
                            "[%s][%s][%s]agent found in the agent instances. set agent timezone to controller timezone=%s, ready time=%s", identifier,
                            controllerId, agentId, controllerTimezone, HistoryModel.getDateAsString(readyTime)));

                    item = new DBItemHistoryAgent();
                    item.setReadyEventId(JocClusterUtil.getDateAsEventId(readyTime));
                    item.setControllerId(controllerId);
                    item.setAgentId(agentId);
                    item.setUri(inst.getUri());
                    item.setTimezone(HistoryUtil.getTimeZone("getAgent " + item.getAgentId(), controllerTimezone));
                    item.setReadyTime(readyTime);
                    item.setCreated(new Date());
                }
                dbLayer.getSession().save(item);
            }

            ca = new CachedAgent(item);
            addAgent(agentId, ca);
        }
        return ca;
    }

    private void addWorkflow(String key, CachedWorkflow cw) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][cache][addWorkflow][%s]%s", identifier, key, SOSString.toString(cw)));
        }
        workflows.put(key, cw);
    }

    private CachedWorkflow getWorkflow(String workflowName, String workflowVersionId) {
        String key = getWorkflowKey(workflowName, workflowVersionId);
        if (workflows.containsKey(key)) {
            CachedWorkflow cw = workflows.get(key);
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][cache][getWorkflow][%s]%s", identifier, key, SOSString.toString(cw)));
            }
            return cw;
        }
        return null;
    }

    private String getWorkflowKey(String workflowName, String workflowVersionId) {
        return new StringBuilder(workflowName).append(KEY_DELIMITER).append(workflowVersionId).toString();
    }

    public CachedWorkflow getWorkflow(DBLayerHistory dbLayer, String workflowName, String workflowVersionId) throws Exception {
        // key workflowVersionId - trigger to select from the database
        CachedWorkflow cw = getWorkflow(workflowName, workflowVersionId);
        if (cw == null) {
            clearWorkflowCache(workflowName);
            String path = null;
            String title = null;
            List<CachedWorkflowParameter> orderPreparation = null;
            Map<String, CachedWorkflowJob> jobs = null;
            try {
                Object[] result = dbLayer.getDeployedWorkflow(controllerId, workflowName, workflowVersionId);
                if (result == null) {
                    throw new Exception("the deployed workflow could not be found in the database");
                }
                path = result[0].toString();
                Workflow w = getWorkflow(workflowName, workflowVersionId, result[1].toString());
                if (w != null) {
                    jobs = getWorkflowJobs(w, workflowName);
                    title = w.getTitle();
                    orderPreparation = getWorkflowOrderPreparation(w);
                }
            } catch (Throwable e) {
                LOGGER.warn(String.format("[workflowName=%s,workflowVersionId=%s][can't evaluate path]%s", workflowName, workflowVersionId, e
                        .toString()));
            }
            if (path == null) {
                path = "/" + workflowName;
            }
            cw = new CachedWorkflow(path, title, orderPreparation, jobs);
            addWorkflow(getWorkflowKey(workflowName, workflowVersionId), cw);
        }
        cw.setLastUsage();
        return cw;
    }

    private Map<String, CachedWorkflowJob> getWorkflowJobs(Workflow w, String workflowName) {
        WorkflowSearcher s = new WorkflowSearcher(w);
        Map<String, CachedWorkflowJob> map = new HashMap<>();
        for (WorkflowJob job : s.getJobs()) {
            String notification = null;
            if (!HistoryNotification.isJobMailNotificationEmpty(job.getJob().getNotification())) {
                try {
                    notification = HistoryUtil.toJsonString(job.getJob().getNotification());
                } catch (JsonProcessingException e) {
                    LOGGER.error(String.format("[workflow=%s][job=%s][error on read notification]%s", workflowName, job.getName(), e.toString()), e);
                }
            }
            map.put(job.getName(), new CachedWorkflowJob(job.getJob().getCriticality(), job.getJob().getTitle(), job.getJob().getAgentName(), job
                    .getJob().getSubagentClusterId(), job.getJob().getWarnIfLonger(), job.getJob().getWarnIfShorter(), getWarningReturnCodes(job
                            .getJob()), job.getJob().getWarnOnErrWritten(), notification));
        }
        return map;
    }

    private List<SortedSet<Integer>> getWarningReturnCodes(com.sos.inventory.model.job.Job job) {
        List<SortedSet<Integer>> result = null;
        if (job.getExecutable() != null && job.getExecutable().getTYPE() != null) {
            switch (job.getExecutable().getTYPE()) {
            case ShellScriptExecutable:
            case ScriptExecutable:
                ExecutableScript es = job.getExecutable().cast();
                if (es != null && es.getReturnCodeMeaning() != null) {
                    result = es.getReturnCodeMeaning().getNormalizedAsList();
                }
                break;
            case InternalExecutable:
                ExecutableJava ej = job.getExecutable().cast();
                if (ej != null && ej.getReturnCodeMeaning() != null) {
                    result = ej.getReturnCodeMeaning().getNormalizedAsList();
                }
                break;
            }
        }
        return result;
    }

    private List<CachedWorkflowParameter> getWorkflowOrderPreparation(Workflow w) {
        if (w.getOrderPreparation() == null || w.getOrderPreparation().getParameters() == null || w.getOrderPreparation().getParameters()
                .getAdditionalProperties() == null || w.getOrderPreparation().getParameters().getAdditionalProperties().size() == 0) {
            return null;
        }
        List<CachedWorkflowParameter> r = new ArrayList<>();
        w.getOrderPreparation().getParameters().getAdditionalProperties().forEach((name, param) -> {
            CachedWorkflowParameter cwp = new CachedWorkflowParameter(name, param);
            if (cwp.getValue() != null) {
                r.add(cwp);
            }
        });
        return r;
    }

    private Workflow getWorkflow(String workflowName, String workflowVersionId, String content) {
        try {
            return HistoryUtil.fromJsonString(content, Workflow.class);
        } catch (Throwable e) {
            LOGGER.warn(String.format("[workflowName=%s,workflowVersionId=%s][can't parse workflow]%s", workflowName, workflowVersionId, e
                    .toString()));
        }
        return null;
    }

    public void clear(CacheType cacheType, String orderId) {
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[%s][cache][clear][%s]%s", identifier, cacheType, orderId));
        }
        switch (cacheType) {
        case orderStep:
            orderSteps.entrySet().removeIf(entry -> entry.getKey().equals(orderId));
            break;
        case order:
            orders.entrySet().removeIf(entry -> entry.getKey().equals(orderId));
            clearOrderSteps(orderId);
            break;
        case orderWithChildOrders:
            orders.entrySet().removeIf(entry -> entry.getKey().startsWith(orderId));
            clearOrderSteps(orderId);
            break;
        default:
            break;
        }
    }

    private void clearOrderSteps(String orderId) {
        orderSteps.entrySet().removeIf(entry -> entry.getKey().startsWith(orderId));
    }

    public void clear(long currentSeconds, long olderThanSeconds) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(String.format("[%s][cache][clear]workflows/orders/steps older than %ss", identifier, olderThanSeconds));
        }
        workflows.entrySet().removeIf(e -> ((currentSeconds / 60) - e.getValue().getLastUsage()) >= (olderThanSeconds / 60));

        Set<String> orders2clear = orders.entrySet().stream().filter(e -> ((currentSeconds / 60) - e.getValue().getLastUsage()) >= (olderThanSeconds
                / 60)).map(e -> e.getKey()).collect(Collectors.toSet());
        if (orders2clear.size() > 0) {
            for (String orderId : orders2clear) {
                clear(CacheType.orderWithChildOrders, orderId);
            }
        }

    }

    private void clearWorkflowCache(String workflowName) {
        workflows.entrySet().removeIf(entry -> entry.getKey().startsWith(new StringBuilder(workflowName).append(KEY_DELIMITER).toString()));
    }

    public void setIdentifier(String val) {
        identifier = val;
    }

}
