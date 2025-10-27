package com.sos.joc.classes.proxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.cluster.ClusterSetting;
import com.sos.controller.model.cluster.ClusterState;
import com.sos.controller.model.cluster.IdToUri;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.db.cluster.JocInstancesDBLayer;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;

import js7.base.web.Uri;
import js7.data.node.NodeId;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;

public class ClusterWatch {
    
    private static ClusterWatch instance;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClusterWatch.class);
    private String clusterId = null;
    private String memberId = null;
    private volatile ConcurrentMap<String, ClusterWatchServiceContext> startedWatches = new ConcurrentHashMap<>();
    private Map<String, String> urlMapper = null;
    protected static boolean onStart = true;
    
    private ClusterWatch() {
        EventBus.getInstance().register(this);
        clusterId = Globals.getJocId();
        memberId = Globals.getMemberId();
    }
    
    public static synchronized ClusterWatch getInstance() {
        if (instance == null) {
            instance = new ClusterWatch();
        }
        return instance;
    }
    
    public static void init(Map<String, String> val) {
        ClusterWatch.getInstance().setUrlMapper(val);
    }
    
    private void setUrlMapper(Map<String, String> val) {
        urlMapper = val;
    }
    
    @Subscribe({ ActiveClusterChangedEvent.class })
    public void listenEvent(ActiveClusterChangedEvent evt) {
        onStart = false;
        if (evt.getNewClusterMemberId() != null) {
            LOGGER.debug("[ClusterWatch] memberId = " + memberId);
            LOGGER.info("[ClusterWatch] current watched Controller clusters by " + toStringWithId() + ": " + startedWatches.keySet().toString());
            LOGGER.info("[ClusterWatch] receive event: " + evt.toString());
            
            if (memberId.equals(evt.getOldClusterMemberId())) {
                // stop for all controllerIds
                if (!startedWatches.isEmpty()) {
                    LOGGER.info("[ClusterWatch] try to stop watches");
                    startedWatches.keySet().forEach(this::stop);
                }
            } else if (memberId.equals(evt.getNewClusterMemberId())) {
                // start for all controllerIds
                SOSHibernateSession sosHibernateSession = null;
                try {
                    sosHibernateSession = Globals.createSosHibernateStatelessConnection("ClusterWatch");
                    
                    Stream<DBItemInventoryJSInstance> instancesStream = new InventoryInstancesDBLayer(sosHibernateSession).getInventoryInstances()
                            .stream();
                    if (urlMapper != null) {
                        instancesStream = instancesStream.peek(i -> i.setUri(urlMapper.getOrDefault(i.getUri(), i.getUri())));
                    }
                    ConcurrentMap<String, List<DBItemInventoryJSInstance>> controllerDbInstances = instancesStream.collect(Collectors
                            .groupingByConcurrent(DBItemInventoryJSInstance::getControllerId));
                    Globals.disconnect(sosHibernateSession);
                    if (controllerDbInstances.values().stream().anyMatch(l -> l.size() > 1)) {
                        LOGGER.info("[ClusterWatch] try to start watches");
                    } else {
                        LOGGER.info("[ClusterWatch] no Controller cluster registered.");
                    }
                    controllerDbInstances.forEach((controllerId, dbItems) -> ProxiesEdit.update(dbItems, true));

                    Proxies.getControllerDbInstances().keySet().stream().filter(c -> !controllerDbInstances.containsKey(c)).forEach(Proxies
                            .getInstance()::removeProxies);
                } catch (Exception e) {
                    LOGGER.error("", e);
                } finally {
                    Globals.disconnect(sosHibernateSession);
                }
            }
        }
    }
    
//    @Subscribe({ ProxyRestarted.class })
//    public void listenProxyRestartedEvent(ProxyRestarted evt) {
//        if (ProxyUser.JOC.getUser().equals(evt.getKey())) {
//            if (isWatched(evt.getControllerId())) {
//                startedWatches.get(evt.getControllerId()).stop();
//                appointNodes(evt.getControllerId(), ControllerApi.of(evt.getControllerId()));
//            }
//        }
//    }
    
    public void appointNodes(String controllerId, JControllerApi api, boolean force) throws DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, JocBadRequestException {
        appointNodes(controllerId, api, null, null, null, null, force);
    }

//    public void appointNodes(String controllerId, JControllerProxy proxy) throws DBMissingDataException, JocConfigurationException,
//            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException, JocBadRequestException {
//        appointNodes(controllerId, null, proxy, null, null, null);
//    }
    
    public void appointNodes(String controllerId, JControllerApi api, JControllerProxy proxy, JocInstancesDBLayer dbLayer, String accessToken,
            JocError jocError) throws DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, JocBadRequestException {
        appointNodes(controllerId, api, proxy, dbLayer, accessToken, jocError, false);
    }

    public void appointNodes(String controllerId, JControllerApi api, JControllerProxy proxy, JocInstancesDBLayer dbLayer, String accessToken,
            JocError jocError, boolean forceRestart) throws DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, JocBadRequestException {
        if (onStart) {
            //LOGGER.warn(toStringWithId() + " cluster service is not started: " + controllerId);
            return;
        }
        // ask for cluster
        List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(controllerId);
        if (controllerInstances == null || controllerInstances.size() < 2) { // is not cluster
            throw new JocBadRequestException("There is no Controller cluster configured with the Id: " + controllerId);
        }
        
        JControllerApi controllerApi = proxy == null ? api : proxy.api();
        
        String watchId = start(controllerApi, controllerId, true, dbLayer, forceRestart);
           
        ClusterState cState = getCurrentClusterState(controllerId, proxy);

        NodeId primeId = NodeId.of("Primary");
        Map<NodeId, Uri> idToUri = new HashMap<>();
        IdToUri itu = new IdToUri();
        
        boolean changed = false;
        for (DBItemInventoryJSInstance inst : controllerInstances) {
            String clusterUri = inst.getClusterUri() == null || inst.getClusterUri().isEmpty() ? inst.getUri() : inst.getClusterUri();
            if (inst.getIsPrimary()) {
                if (!changed) {
                    changed = !clusterUri.equals(cState.getSetting().getIdToUri().getAdditionalProperties().get("Primary"));
                }
                idToUri.put(primeId, Uri.of(clusterUri)); 
                itu.setAdditionalProperty("Primary", clusterUri);
            } else {
                if (!changed) {
                    changed = !clusterUri.equals(cState.getSetting().getIdToUri().getAdditionalProperties().get("Backup"));
                }
                idToUri.put(NodeId.of("Backup"), Uri.of(clusterUri)); 
                itu.setAdditionalProperty("Backup", clusterUri);
            }
        }
        
        NodeId activeId = cState.getSetting().getActiveId() != null ? NodeId.of(cState.getSetting().getActiveId()) : primeId;
        if (changed) {
            try {
                LOGGER.info("[ClusterWatch] Appoint Controller cluster nodes for '" + controllerId + "': " + Globals.objectMapper.writeValueAsString(
                        new ClusterSetting(itu, activeId.string(), watchId, null)));
            } catch (Exception e) {
                LOGGER.info("[ClusterWatch] Appoint Controller cluster nodes for '" + controllerId + "'");
            }

            controllerApi.clusterAppointNodes(idToUri, activeId).thenAccept(e -> {
                        if (e.isLeft()) {
                            if (jocError == null) {
                                LOGGER.warn(ProblemHelper.getErrorMessage(e.getLeft()));
                            } else {
                                ProblemHelper.postProblemEventIfExist(e, accessToken, jocError, null);
                            }
                        } else {
                            LOGGER.info("[ClusterWatch] Appointing Controller cluster nodes for '" + controllerId + "' was successful");
                        }
                    });
        } else {
            LOGGER.debug("[ClusterWatch] Controller cluster nodes of '" + controllerId + "' are already appointed.");
        }
    }
    
    public NodeId getClusterNodeLoss(String controllerId) {
        if (isWatched(controllerId)) {
            return startedWatches.get(controllerId).getClusterNodeLoss();
        }
        return null;
    }

    public boolean hasClusterNodeLoss(String controllerId) {
        return getClusterNodeLoss(controllerId) != null;
    }
    
    public void confirmNodeLoss(String controllerId, String confirmer, String accessToken, JocError jocError) throws JocBadRequestException {
        if (isWatched(controllerId)) {
            NodeId nodeId = startedWatches.get(controllerId).getClusterNodeLoss();
            if (nodeId != null) {
                startedWatches.get(controllerId).confirmNodeLoss(nodeId, confirmer, accessToken, jocError);
            } else {
                throw new JocBadRequestException("Couldn't determine loss node of Controller cluster '" + controllerId + "'."); 
            }
        } else {
            throw new JocBadRequestException("Controller cluster '" + controllerId + "' is not watched by " + toStringWithId() + "."); 
        }
    }
    
    public Optional<String> getAndCleanLastMessage(String controllerId) {
        if (isWatched(controllerId)) {
            return startedWatches.get(controllerId).getAndCleanLastMessage();
        }
        return Optional.empty();
    }
    
    protected void clear() {
        startedWatches.clear();
    }
    
    private String toStringWithId() {
        return "JOC (" + clusterId + ")";
    }

    private String start(JControllerApi controllerApi, String controllerId, boolean checkWatchByJoc, JocInstancesDBLayer dbLayer,
            boolean forceRestart) {
        // LOGGER.info("[ClusterWatch] try to start " + toStringWithId() + " as watcher for '" + controllerId + "'");
        boolean jocIsActive = true;
        if (checkWatchByJoc) {
            jocIsActive = jocInstanceIsActive(dbLayer);
        }
        if (jocIsActive) {
            if (!forceRestart && isWatched(controllerId)) {
                LOGGER.info("[ClusterWatch] Watcher by " + toStringWithId() + " is still running for '" + controllerId + "'");
                // throw new JocServiceException("[ClusterWatch] " + controllerId + " is still running.");
                return clusterId;
            } else {
                try {
                    Optional.ofNullable(startedWatches.get(controllerId)).ifPresent(cws -> cws.stop());
                    LOGGER.info("[ClusterWatch] start " + toStringWithId() + " as watcher for '" + controllerId + "'");
                    if (controllerApi == null) {
                        controllerApi = ControllerApi.of(controllerId);
                    }
                    startedWatches.put(controllerId, new ClusterWatchServiceContext(controllerId, clusterId, controllerApi));
                    //ClusterWatchService started = controllerApi.startClusterWatch(ClusterWatchId.of(clusterId), startEventbus()).get();
                    //CompletableFuture<Void> started = controllerApi.runClusterWatch(ClusterWatchId.of(clusterId));
                    //startedWatches.put(controllerId, started);
                    return clusterId;
                } catch (Exception e) {
                    LOGGER.error("[ClusterWatch] starting " + toStringWithId() + " as watcher for '" + controllerId + "' failed", e);
                }
            }
        }
        return null;
    }
    
    private void stop(String controllerId) {
        if (isWatched(controllerId)) {
            if (startedWatches.get(controllerId).stop()) {
                startedWatches.remove(controllerId);
            }
            
//            try {
//                startedWatches.get(controllerId).stop()
//                ControllerApi.of(controllerId).stopClusterWatch().get();
//                // ControllerApi.of(controllerId).stopClusterWatch().thenAccept(v -> {
////                if (startedWatches.get(controllerId).isDone()) {
////                    // startedWatches.remove(controllerId);
//                    LOGGER.info("[ClusterWatch] Watcher by " + toStringWithId() + " is stopped for '" + controllerId + "'");
////                } else {
////                    LOGGER.error("[ClusterWatch] stop " + toStringWithId() + " as watcher for '" + controllerId
////                            + "' is called but cluster watch is still running.");
//                    // throw new JocServiceException("[ClusterWatch] stop for " + controllerId + " is called but Cluster Watch is still running.");
////                }
//                startedWatches.remove(controllerId);
//                // });
//            } catch (Exception e) {
//                LOGGER.error("[ClusterWatch] stopping watcher by " + toStringWithId() + " for '" + controllerId + "' failed", e);
//            }
//        } else {
            // LOGGER.info("[ClusterWatch] Watcher by " + toStringWithId() + " for '" + controllerId + "' is already stopped");
        }
    }
    
    public boolean isWatched(String controllerId) {
        return startedWatches.containsKey(controllerId);
//        boolean isAlive = false;
//        if (startedWatches.containsKey(controllerId)) {
////            if (startedWatches.get(controllerId).isDone()) {
////                startedWatches.remove(controllerId);
////            } else {
//                isAlive = true;
////            }
//        }
//        return isAlive;
    }
    
    private boolean jocInstanceIsActive(JocInstancesDBLayer dbLayer) {
        SOSHibernateSession sosHibernateSession = null;
        try {
            DBItemJocInstance activeInstance = null;
            if (dbLayer == null) {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection("JocIsActive");
                activeInstance =  new JocInstancesDBLayer(sosHibernateSession).getActiveInstance();
            } else {
                activeInstance =  dbLayer.getActiveInstance();
            }
            if (activeInstance != null && memberId.equals(activeInstance.getMemberId())) {
//                if (activeInstance.getHeartBeat() != null) {
//                    Instant oneMinuteAgo = Instant.now().minusSeconds(TimeUnit.MINUTES.toSeconds(1));
//                    if (activeInstance.getHeartBeat().toInstant().isAfter(oneMinuteAgo)) {
                        LOGGER.info("[ClusterWatch] " + toStringWithId() + " instance is active");
                        return true;
//                    }
//                }
            }
            LOGGER.info("[ClusterWatch] " + toStringWithId() + " instance is inactive");
            return false;
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }
    
    private static ClusterState getCurrentClusterState(String controllerId, JControllerProxy proxy) {
        
        String clusterState = null;
        try {
            if (proxy == null) {
                proxy = Proxy.of(controllerId);
            }
            clusterState = proxy.currentState().clusterState().toJson();
            LOGGER.info("[ClusterWatch] Current Controller cluster state for '" + controllerId + "': " + clusterState);
        } catch (Exception e) {
            LOGGER.warn("", e);
        }
        
        ClusterState cState = new ClusterState();
        if (clusterState != null) {
            try {
                cState = Globals.objectMapper.readValue(clusterState, ClusterState.class);
            } catch (Exception e) {
                //
            }
        }
        if (cState.getSetting() == null) {
            cState.setSetting(new ClusterSetting());
        }
        if (cState.getSetting().getIdToUri() == null) {
            cState.getSetting().setIdToUri(new IdToUri());
        }
        return cState;
    }

}
