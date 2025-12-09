package com.sos.joc.classes.agent;

import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.common.SyncState;
import com.sos.controller.model.common.SyncStateText;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventorySubAgentCluster;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocMissingLicenseException;
import com.sos.joc.model.inventory.common.ConfigurationType;

import js7.data.agent.AgentPath;
import js7.data.subagent.SubagentBundleId;
import js7.data.subagent.SubagentId;
import js7.data_for_java.controller.JControllerState;

public class AgentHelper {

    public static boolean testMode = false;
    private static String errMsg = "missing license for Agent cluster";

    public static boolean hasClusterLicense() {
        if (testMode) {
            return true;
        }
        return JocClusterService.getInstance().getCluster() != null && JocClusterService.getInstance().getCluster().getConfig().getClusterModeResult()
                .getUse();
    }

    public static void throwJocMissingLicenseException() throws JocMissingLicenseException {
        throwJocMissingLicenseException(errMsg);
    }

    public static void throwJocMissingLicenseException(String message) throws JocMissingLicenseException {
        if (!hasClusterLicense()) {
            if (message == null || message.isEmpty()) {
                message = errMsg;
            }
            throw new JocMissingLicenseException(message);
        }
    }

    public static Map<String, JControllerState> getCurrentStates(Collection<String> controllerIds) {
        if (controllerIds == null) {
            return Collections.emptyMap();
        }
        Map<String, JControllerState> result = new HashMap<>(controllerIds.size());
        controllerIds.stream().filter(Objects::nonNull).distinct().forEach(s -> result.put(s, getCurrentState(s)));
        return result;
    }

    private static JControllerState getCurrentState(String controllerId) {
        try {
            return Proxy.of(controllerId).currentState();
        } catch (Exception e1) {
            return null;
        }
    }

    public static Map<String, Set<String>> getSubagents(Collection<String> controllerIds, Map<String, JControllerState> currentStates) {
        if (controllerIds == null) {
            return Collections.emptyMap();
        }
        Map<String, Set<String>> result = new HashMap<>(controllerIds.size());
        controllerIds.stream().filter(Objects::nonNull).distinct().forEach(s -> result.put(s, getSubagents(currentStates.get(s))));
        return result;
    }

    private static Set<String> getSubagents(JControllerState currentState) {
        if (currentState == null) {
            return null;
        }
        try {
            return currentState.idToSubagentItem().keySet().stream().map(SubagentId::string).collect(Collectors.toSet());
        } catch (Exception e1) {
            return null;
        }
    }

    public static Map<String, Set<String>> getAgents(Collection<String> controllerIds, Map<String, JControllerState> currentStates) {
        if (controllerIds == null) {
            return Collections.emptyMap();
        }
        Map<String, Set<String>> result = new HashMap<>(controllerIds.size());
        controllerIds.stream().filter(Objects::nonNull).distinct().forEach(s -> result.put(s, getAgents(currentStates.get(s))));
        return result;
    }

    private static Set<String> getAgents(JControllerState currentState) {
        if (currentState == null) {
            return null;
        }
        try {
            return currentState.pathToAgentRef().keySet().stream().map(AgentPath::string).collect(Collectors.toSet());
        } catch (Exception e1) {
            return null;
        }
    }

    public static Map<String, Set<String>> getSubagentSelections(Collection<String> controllerIds, Map<String, JControllerState> currentStates) {
        if (controllerIds == null) {
            return Collections.emptyMap();
        }
        Map<String, Set<String>> result = new HashMap<>(controllerIds.size());
        controllerIds.stream().filter(Objects::nonNull).distinct().forEach(s -> result.put(s, getSubagentSelections(currentStates.get(s))));
        return result;
    }

    private static Set<String> getSubagentSelections(JControllerState currentState) {
        if (currentState == null) {
            return null;
        }
        try {
            return currentState.idToSubagentBundle().keySet().stream().map(SubagentBundleId::string).collect(Collectors.toSet());
        } catch (Exception e1) {
            return null;
        }
    }

    public static SyncState getSyncState(Set<String> agents, DBItemInventoryAgentInstance dbAgent) {
        return getSyncState(agents, dbAgent.getAgentId(), dbAgent.getDeployed());
    }

    public static SyncState getSyncState(Set<String> subagents, DBItemInventorySubAgentInstance dbSubAgent) {
        return getSyncState(subagents, dbSubAgent.getSubAgentId(), dbSubAgent.getDeployed());
    }

    public static SyncState getSyncState(Set<String> subagentSelections, DBItemInventorySubAgentCluster dbSubAgent) {
        return getSyncState(subagentSelections, dbSubAgent.getSubAgentClusterId(), dbSubAgent.getDeployed());
    }

    private static SyncState getSyncState(Set<String> objects, String identifier, Boolean deployed) {
        if (objects == null) {
            return SyncStateHelper.getState(SyncStateText.UNKNOWN);
        } else if (objects.contains(identifier)) {
            if (deployed) {
                return SyncStateHelper.getState(SyncStateText.IN_SYNC);
            } else {
                return SyncStateHelper.getState(SyncStateText.NOT_IN_SYNC);
            }
        } else {
            return SyncStateHelper.getState(SyncStateText.NOT_DEPLOYED);
        }
    }
    
    public static void validateWorkflowsByAgentNames(InventoryAgentInstancesDBLayer agentDbLayer, Set<String> agentNamesAndAliases,
            Set<String> missedAliases) throws SOSHibernateException {
        
        InventoryDBLayer invDbLayer = new InventoryDBLayer(agentDbLayer.getSession());
        Set<String> events = new HashSet<>();
        Set<String> visibleAgentNames = agentDbLayer.getVisibleAgentNames();
        
        if (missedAliases != null && !missedAliases.isEmpty()) {
            Set<DBItemInventoryConfiguration> workflowsByMissedAgentNames = invDbLayer.getUsedWorkflowsByAgentNames(missedAliases, true);
            workflowsByMissedAgentNames.stream().peek(w -> w.setDeployed(false)).forEach(w -> {
                if (w.getValid()) {
                    try {
                        Validator.validate(ConfigurationType.WORKFLOW, w.getContent().getBytes(StandardCharsets.UTF_8), invDbLayer, visibleAgentNames);
                    } catch (Exception e) {
                        w.setValid(false);
                    }
                }
                try {
                    events.add(w.getFolder());
                    invDbLayer.getSession().update(w);
                } catch (Exception e) {
                    //
                }
            });
        }

        if (agentNamesAndAliases != null && !agentNamesAndAliases.isEmpty()) {
            Set<DBItemInventoryConfiguration> invalidWorkflowsByAgentNames = invDbLayer.getUsedWorkflowsByAgentNames(agentNamesAndAliases, false);
//            List<Long> workflowInvIds = new ArrayList<>();
            if (!invalidWorkflowsByAgentNames.isEmpty()) {
                invalidWorkflowsByAgentNames.stream().filter(w -> {
                    try {
                        Validator.validate(ConfigurationType.WORKFLOW, w.getContent().getBytes(StandardCharsets.UTF_8), invDbLayer, visibleAgentNames);
                        return true;
                    } catch (Exception e) {
                        return false;
                    }
                }).peek(w -> w.setValid(true)).forEach(w -> {
                    try {
                        events.add(w.getFolder());
//                        if (JocInventory.isWorkflow(w.getType())) {
//                            workflowInvIds.add(w.getId());
//                        }
                        //JocInventory.updateConfiguration(invDbLayer, w);
                        invDbLayer.getSession().update(w);
                    } catch (Exception e) {
                        //
                    }
                });
            }
            // post event: InventoryTaggingUpdated
//            if (workflowInvIds != null && !workflowInvIds.isEmpty()) {
//                InventoryTagDBLayer dbTagLayer = new InventoryTagDBLayer(agentDbLayer.getSession());
//                dbTagLayer.getTags(workflowInvIds).stream().distinct().forEach(JocInventory::postTaggingEvent);
//            }
        }
        events.forEach(JocInventory::postEvent);
    }
    
    public static OptionalInt getProcessLimit(Integer processLimit) {
        return processLimit == null ? OptionalInt.empty() : OptionalInt.of(processLimit);
    }
}
