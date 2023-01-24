package com.sos.joc.classes.proxy;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.cluster.ClusterSetting;
import com.sos.controller.model.cluster.ClusterState;
import com.sos.controller.model.cluster.ClusterWatcher;
import com.sos.controller.model.cluster.IdToUri;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.db.cluster.JocInstancesDBLayer;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;

import js7.base.web.Uri;
import js7.data.cluster.ClusterSetting.Watch;
import js7.data.cluster.ClusterWatchId;
import js7.data.node.NodeId;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;

public class ClusterWatch {
    
    private static ClusterWatch instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterWatch.class);
    private String clusterId = null;
    private String memberId = null;
    private volatile ConcurrentMap<String, CompletableFuture<Void>> startedWatches = new ConcurrentHashMap<>();
    
    private ClusterWatch() {
        EventBus.getInstance().register(this);
        clusterId = Globals.getClusterId() + "#" + Globals.getOrdering();
        memberId = Globals.getMemberId();
    }
    
    public static synchronized ClusterWatch getInstance() {
        if (instance == null) {
            instance = new ClusterWatch();
        }
        return instance;
    }
    
    @Subscribe({ ActiveClusterChangedEvent.class })
    public void listenEvent(ActiveClusterChangedEvent evt) {
        LOGGER.info("[ClusterWatch] memberId = " + memberId);
        LOGGER.info("[ClusterWatch] current watches: " + startedWatches.keySet().toString());
        LOGGER.info("[ClusterWatch] receive event: " + evt.toString());
        if (evt.getNewClusterMemberId() != null && evt.getOldClusterMemberId() != null) {
            if (memberId.equals(evt.getOldClusterMemberId())) {
                //stop for all controllerIds
                LOGGER.info("[ClusterWatch] try to stop watches");
                startedWatches.keySet().forEach(controllerId -> stop(controllerId));
            } else if (memberId.equals(evt.getNewClusterMemberId())) {
                //start for all controllerIds
                LOGGER.info("[ClusterWatch] try to start watches");
                SOSHibernateSession sosHibernateSession = null;
                try {
                    sosHibernateSession = Globals.createSosHibernateStatelessConnection("ClusterWatch");
                    ConcurrentMap<String, List<DBItemInventoryJSInstance>> controllerDbInstances = new InventoryInstancesDBLayer(sosHibernateSession)
                            .getInventoryInstances().stream().collect(Collectors.groupingByConcurrent(DBItemInventoryJSInstance::getControllerId));
                    Globals.disconnect(sosHibernateSession);
                    controllerDbInstances.forEach((controllerId, dbItems) -> Proxies.getInstance().updateProxies(dbItems));
                    
                    Proxies.getControllerDbInstances().keySet().stream().filter(c -> !controllerDbInstances.containsKey(c)).forEach(c -> Proxies
                            .getInstance().removeProxies(c));
                } catch (Exception e) {
                    LOGGER.error("", e);
                } finally {
                    Globals.disconnect(sosHibernateSession);
                }
                LOGGER.info("[ClusterWatch] started watches: " + startedWatches.keySet().toString());
            }
        }
    }
    
    public void appointNodes(String controllerId, JControllerProxy proxy, InventoryAgentInstancesDBLayer dbLayer, String accessToken, JocError jocError)
            throws DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ControllerConnectionRefusedException, JsonProcessingException, JocBadRequestException, ControllerConnectionResetException, ExecutionException {
        // ask for cluster
        List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(controllerId);
        if (controllerInstances == null || controllerInstances.size() < 2) { // is not cluster
            throw new JocBadRequestException("There is no Controller cluster configured with the Id: " + controllerId);
        }
        
        List<String> agentWatches = getClusterWatchers(controllerId, dbLayer);
        // dry run: if (agentWatches.isEmpty()) {
        if (agentWatches.isEmpty()) {
            start(proxy.api(), controllerId, true, dbLayer);
        } else {
            stop(controllerId);
        }
        //}
           
        ClusterState cState = getCurrentClusterState(controllerId, proxy);

        NodeId primeId = NodeId.of("Primary");
        Map<NodeId, Uri> idToUri = new HashMap<>();
        IdToUri itu = new IdToUri();
        
        boolean changed = false;
        for (DBItemInventoryJSInstance inst : controllerInstances) {
            if (inst.getIsPrimary()) {
                if (!changed) {
                    changed = !inst.getClusterUri().equals(cState.getSetting().getIdToUri().getAdditionalProperties().get("Primary"));
                }
                idToUri.put(primeId, Uri.of(inst.getClusterUri())); 
                itu.setAdditionalProperty("Primary", inst.getClusterUri());
            } else {
                if (!changed) {
                    changed = !inst.getClusterUri().equals(cState.getSetting().getIdToUri().getAdditionalProperties().get("Backup"));
                }
                idToUri.put(NodeId.of("Backup"), Uri.of(inst.getClusterUri())); 
                itu.setAdditionalProperty("Backup", inst.getClusterUri());
            }
        }
        
        if (!changed) {
            changed = agentWatches.size() != cState.getSetting().getClusterWatches().size();
        }
        if (!changed && !cState.getSetting().getClusterWatches().isEmpty()) {
            cState.getSetting().getClusterWatches().removeIf(w -> agentWatches.contains(w.getUri().toString()));
            changed = !cState.getSetting().getClusterWatches().isEmpty();
        }
        
        NodeId activeId = cState.getSetting().getActiveId() != null ? NodeId.of(cState.getSetting().getActiveId()) : primeId;
        if (changed) {
            try {
                LOGGER.info("[ClusterWatch] Appoint nodes for '" + controllerId + "': " + Globals.objectMapper.writeValueAsString(new ClusterSetting(
                        itu, activeId.string(), agentWatches.stream().map(s -> new ClusterWatcher(URI.create(s))).collect(Collectors.toList()),
                        null)));
            } catch (Exception e) {
                LOGGER.info("[ClusterWatch] Appoint nodes for '" + controllerId + "'");
            }
            
            proxy.api().clusterAppointNodes(idToUri, activeId, agentWatches.stream().map(item -> new Watch(Uri.of(item))).collect(Collectors
                    .toList())).thenAccept(e -> {
                        ProblemHelper.postProblemEventIfExist(e, accessToken, jocError, null); 
                        if (e.isLeft()) {
                            if (jocError == null) {
                                LOGGER.warn(ProblemHelper.getErrorMessage(e.getLeft()));
                            } else {
                                ProblemHelper.postProblemEventIfExist(e, accessToken, jocError, null); 
                            }
                        } else {
                            LOGGER.info("[ClusterWatch] Appointing cluster nodes for '" + controllerId + "' was successful");
                        }
                    });
        } else {
            LOGGER.info("[ClusterWatch] Cluster nodes of '" + controllerId + "' are already appointed.");
        }
    }
    
    private Stream<String> jocIsClusterWatch(String controllerId, InventoryAgentInstancesDBLayer dbLayer) {
        // determine in DB which controllerId don't use Agent Cluster Watch
        SOSHibernateSession sosHibernateSession = null;
        try {
            if (dbLayer == null) {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection("ClusterWatch");
                dbLayer = new InventoryAgentInstancesDBLayer(sosHibernateSession);
            }
            return dbLayer.getControllerIdsWithoutAgentWatch(controllerId);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void start(JControllerApi controllerApi, String controllerId, boolean checkWatchByJoc, InventoryAgentInstancesDBLayer dbLayer) {
        LOGGER.info("[ClusterWatch] try to start for '" + controllerId + "'");
        LOGGER.info("[ClusterWatch] current Watches: " + startedWatches.keySet().toString());
        boolean clusterWatchByJoc = !checkWatchByJoc;
        boolean jocIsActive = true;
        if (checkWatchByJoc) {
            // dry run
            clusterWatchByJoc = jocIsClusterWatch(controllerId, dbLayer).count() == 1L;
            // clusterWatchByJoc = true;
            jocIsActive = jocInstanceIsActive(dbLayer);
        }
        if (clusterWatchByJoc && jocIsActive) {
            if (isAlive(controllerId)) {
                LOGGER.info("[ClusterWatch] cluster watch by JOC is still running for '" + controllerId + "'");
                // throw new JocServiceException("[ClusterWatch] " + controllerId + " is still running.");
            } else {
                try {
                    LOGGER.info("[ClusterWatch] start cluster watch by JOC for '" + controllerId + "'");
                    if (controllerApi == null) {
                        controllerApi = ControllerApi.of(controllerId);
                    }
                    CompletableFuture<Void> started = controllerApi.runClusterWatch(ClusterWatchId.of(clusterId));
                    // dry run
//                    CompletableFuture<Void> started = CompletableFuture.runAsync(() -> {
//                        LOGGER.info("[ClusterWatch] starting for " + controllerId);
//                    });
                    startedWatches.put(controllerId, started);
                } catch (Exception e) {
                    LOGGER.error("[ClusterWatch] start cluster watch by JOC for '" + controllerId + "' failed", e);
                }
            }
        } else {
            if (clusterWatchByJoc) {
                LOGGER.info("[ClusterWatch] '" + controllerId + "' is watched by Agent");
            }
        }
    }
    
    private CompletableFuture<Void> stop(String controllerId) {
        if (isAlive(controllerId)) {
            try {
                return ControllerApi.of(controllerId).stopClusterWatch().thenAccept(v -> {
                    if (startedWatches.get(controllerId).isDone()) {
                        startedWatches.remove(controllerId);
                        LOGGER.info("[ClusterWatch] cluster watch by JOC is stopped for '" + controllerId + "'");
                    } else {
                        LOGGER.error("[ClusterWatch] stop cluster watch by JOC for '" + controllerId + "' is called but cluster watch is still running.");
                        //throw new JocServiceException("[ClusterWatch] stop for " + controllerId + " is called but Cluster Watch is still running.");
                    }
                });
                /* dry run
                return CompletableFuture.runAsync(() -> {
                    LOGGER.info("[ClusterWatch] stopping for " + controllerId);
                }).thenAccept(v -> {
                    if (startedWatches.get(controllerId).isDone()) {
                        startedWatches.remove(controllerId);
                        LOGGER.info("[ClusterWatch] stopped for " + controllerId);
                    } else {
                        LOGGER.info("[ClusterWatch] stop for " + controllerId + " is called but Cluster Watch is still running.");
                        //throw new JocServiceException("[ClusterWatch] stop for " + controllerId + " is called but Cluster Watch is still running.");
                    }
                });
                */
            } catch (Exception e) {
                LOGGER.error("[ClusterWatch] stopping cluster watch by JOC for '" + controllerId + "' failed", e);
            }
        } else {
            LOGGER.info("[ClusterWatch] cluster watch by JOC for '" + controllerId + "' is already stopped");
        }
        return new CompletableFuture<Void>();
    }
    
    private boolean isAlive(String controllerId) {
        boolean isAlive = false;
        if (startedWatches.containsKey(controllerId)) {
            if (startedWatches.get(controllerId).isDone()) {
                // already stopped
                /* dry run 
                 * startedWatches.remove(controllerId);
                 * isAlive = true;
                 */
                startedWatches.remove(controllerId);
            } else {
                isAlive = true;
            }
        }
        return isAlive;
    }
    
    private boolean jocInstanceIsActive(InventoryAgentInstancesDBLayer dbLayer) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            DBItemJocInstance activeInstance = null;
            if (dbLayer == null) {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection("JocIsActive");
                activeInstance =  new JocInstancesDBLayer(sosHibernateSession).getActiveInstance();
            } else {
                activeInstance =  new JocInstancesDBLayer(dbLayer.getSession()).getActiveInstance();
            }
            if (activeInstance != null && memberId.equals(activeInstance.getMemberId())) {
                LOGGER.info("[ClusterWatch] JOC instance is active");
                return true;
            }
            LOGGER.info("[ClusterWatch] JOC instance is inactive");
            return false;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }
    
    private static ClusterState getCurrentClusterState(String controllerId, JControllerProxy proxy) {
        String clusterState = proxy.currentState().clusterState().toJson();
        LOGGER.info("[ClusterWatch] Current cluster state for '" + controllerId + "': " + clusterState);
        ClusterState cState;
        try {
            cState = Globals.objectMapper.readValue(clusterState, ClusterState.class);
        } catch (Exception e) {
            cState = new ClusterState();
        }
        if (cState.getSetting() == null) {
            cState.setSetting(new ClusterSetting()); 
        }
        if (cState.getSetting().getClusterWatches() == null) {
            cState.getSetting().setClusterWatches(Collections.emptyList()); 
        }
        if (cState.getSetting().getIdToUri() == null) {
            cState.getSetting().setIdToUri(new IdToUri());
        }
        return cState;
    }
    
    private static List<String> getClusterWatchers(String controllerId, InventoryAgentInstancesDBLayer dbLayer) throws JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            if (dbLayer == null) {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection("GetClusterWatchers");
                dbLayer = new InventoryAgentInstancesDBLayer(sosHibernateSession);
            }
            List<String> w = dbLayer.getUrisOfClusterWatcherByControllerId(controllerId);
            if (w.isEmpty()) {
                LOGGER.info(String.format("No Agent Cluster Watcher is configured for '" + controllerId + "'"));
            } else {
                LOGGER.info(String.format("Agent Cluster Watchers of '%s': %s", controllerId, w.toString()));
            }
            return w;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

}
