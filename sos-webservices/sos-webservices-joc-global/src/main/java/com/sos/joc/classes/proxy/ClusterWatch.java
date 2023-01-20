package com.sos.joc.classes.proxy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.db.cluster.JocInstancesDBLayer;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;

import js7.base.web.Uri;
import js7.data.cluster.ClusterSetting.Watch;
import js7.data.node.NodeId;
import js7.proxy.javaapi.JControllerApi;

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
        LOGGER.info("[ClusterWatch] current Watches: " + startedWatches.toString());
        if (evt.getNewClusterMemberId() != null && evt.getOldClusterMemberId() != null) {
            if (memberId.equals(evt.getOldClusterMemberId())) {
                //stop for all controllerIds
                LOGGER.info("[ClusterWatch] try to stop Watches: " + startedWatches.toString());
                startedWatches.keySet().forEach(controllerId -> stop(controllerId));
            } else if (memberId.equals(evt.getNewClusterMemberId())) {
                //start for all controllerIds
                jocIsClusterWatch().forEach(controllerId -> start(controllerId));
                LOGGER.info("[ClusterWatch] started Watches: " + startedWatches.toString());
            }
        }
    }
    
    private Stream<String> jocIsClusterWatch() {
        return jocIsClusterWatch(null);
    }
    
    private Stream<String> jocIsClusterWatch(String controllerId) {
        // determine in DB which controllerId don't use Agent Cluster Watch
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("ClusterWatch");
            return new InventoryAgentInstancesDBLayer(sosHibernateSession).getControllerIdsWithoutAgentWatch(controllerId);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    protected void start(JControllerApi controllerApi, String controllerId, boolean checkWatchByJoc) {
        LOGGER.info("[ClusterWatch] try to start for " + controllerId);
        LOGGER.info("[ClusterWatch] current Watches: " + startedWatches.toString());
        boolean clusterWatchByJoc = !checkWatchByJoc;
        if (checkWatchByJoc) {
            /* dry run
            clusterWatchByJoc = jocIsClusterWatch(controllerId).count() == 1L && jocInstanceIsActive();
            */
            clusterWatchByJoc = jocInstanceIsActive();
        }
        if (clusterWatchByJoc) {
            if (isAlive(controllerId)) {
                LOGGER.info("[ClusterWatch] is still running for " + controllerId);
                // throw new JocServiceException("[ClusterWatch] " + controllerId + " is still running.");
            } else {
                try {
                    LOGGER.info("[ClusterWatch] start for " + controllerId);
                    if (controllerApi == null) {
                        controllerApi = ControllerApi.of(controllerId);
                    }
                    // dry run CompletableFuture<Void> started = controllerApi.startClusterWatch(ClusterWatchId.of(clusterId)).get();
                    CompletableFuture<Void> started = CompletableFuture.runAsync(() -> {
                        LOGGER.info("[ClusterWatch] starting for " + controllerId);
                    });
                    startedWatches.put(controllerId, started);
                } catch (Exception e) {
                    LOGGER.error("[ClusterWatch] starting for " + controllerId + " failed", e);
                }
            }
        } else {
            LOGGER.info("[ClusterWatch] " + controllerId + " is watched by Agent");
        }
    }

    private void start(String controllerId) {
        start(null, controllerId, false);
    }
    
    private CompletableFuture<Void> stop(String controllerId) {
        if (isAlive(controllerId)) {
            try {
                /* dry run 
                return ControllerApi.of(controllerId).stopClusterWatch().thenAccept(v -> {
                    if (startedWatches.get(controllerId).isDone()) {
                        startedWatches.remove(controllerId);
                        LOGGER.info("[ClusterWatch] stopped for " + controllerId);
                    } else {
                        throw new JocServiceException("[ClusterWatch] stop for " + controllerId + " is called but Cluster Watch is still running.");
                    }
                }); */
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
            } catch (Exception e) {
                LOGGER.error("[ClusterWatch] starting for " + controllerId + " failed", e);
            }
        }
        return new CompletableFuture<Void>();
    }
    
    private boolean isAlive(String controllerId) {
        boolean isAlive = false;
        if (startedWatches.containsKey(controllerId)) {
            if (startedWatches.get(controllerId).isDone()) {
                // already stopped
                startedWatches.remove(controllerId);
            } else {
                isAlive = true;
            }
        }
        return isAlive;
    }
    
    private boolean jocInstanceIsActive() {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("ClusterWatch");
            DBItemJocInstance activeInstance =  new JocInstancesDBLayer(sosHibernateSession).getActiveInstance();
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
    
    public void appointNodes(String controllerId, InventoryAgentInstancesDBLayer dbLayer, String accessToken, JocError jocError)
            throws DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ControllerConnectionRefusedException, JsonProcessingException, JocBadRequestException {
        // ask for cluster
        List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(controllerId);
        if (controllerInstances == null || controllerInstances.size() < 2) { // is not cluster
            throw new JocBadRequestException("There is no cluster configured with the Id: " + controllerId);
        }
        
        List<Watch> agentWatches = getClusterWatchers(controllerId, dbLayer);
        JControllerApi controllerApi = ControllerApi.of(controllerId);
        // dry run: if (agentWatches.isEmpty()) {
            start(controllerApi, controllerId, true);
        //}

        NodeId activeId = NodeId.of("Primary");
        Map<NodeId, Uri> idToUri = new HashMap<>();
        for (DBItemInventoryJSInstance inst : controllerInstances) {
            idToUri.put(inst.getIsPrimary() ? activeId : NodeId.of("Backup"), Uri.of(inst.getClusterUri()));
        }
        controllerApi.clusterAppointNodes(idToUri, activeId, agentWatches).thenAccept(
                e -> ProblemHelper.postProblemEventIfExist(e, accessToken, jocError, null));
    }
    
    public static List<Watch> getClusterWatchers(String controllerId, InventoryAgentInstancesDBLayer dbLayer) throws JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBMissingDataException, DBConnectionRefusedException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            if (dbLayer == null) {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection("GetClusterWatchers");
                dbLayer = new InventoryAgentInstancesDBLayer(sosHibernateSession);
            }
            List<String> w = dbLayer.getUrisOfClusterWatcherByControllerId(controllerId);
            if (w.isEmpty()) {
                // throw new DBMissingDataException("No Cluster Watcher is configured for Controller '" + controllerId + "'");
                LOGGER.info(String.format("No Cluster Watcher is configured for Controller '" + controllerId + "'"));
            } else {
                LOGGER.info(String.format("Cluster Watchers of '%s': %s", controllerId, w.toString()));
                // return w.stream().map(item -> new Watch(Uri.of(item))).collect(Collectors.toList());
            }
            return w.stream().map(item -> new Watch(Uri.of(item))).collect(Collectors.toList());
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
}

}
