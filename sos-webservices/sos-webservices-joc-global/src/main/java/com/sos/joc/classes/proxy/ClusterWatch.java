package com.sos.joc.classes.proxy;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.joc.exceptions.JocServiceException;

import js7.data.cluster.ClusterWatchId;

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
    public void createEvent(ActiveClusterChangedEvent evt) {
        if (evt.getNewClusterMemberId() != null && evt.getOldClusterMemberId() != null) {
            if (memberId.equals(evt.getOldClusterMemberId())) {
                //stop for all controllerIds
                startedWatches.keySet().forEach(controllerId -> stop(controllerId));
            } else if (memberId.equals(evt.getNewClusterMemberId())) {
                //start for all controllerIds
                jocIsClusterWatch().forEach(controllerId -> start(controllerId));
            }
        }
    }
    
    private Stream<String> jocIsClusterWatch() {
        // determine in DB which controllerId don't use Agent Cluster Watch
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("ClusterWatch");
            return new InventoryAgentInstancesDBLayer(sosHibernateSession).getControllerIdsWithoutAgentWatch();
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private void start(String controllerId) {
        if (isAlive(controllerId)) {
            throw new JocServiceException("[ClusterWatch] " + controllerId + " is still running.");
        }
        try {
            LOGGER.info("[ClusterWatch] started for " + controllerId);
            //dry run CompletableFuture<Void> started = ControllerApi.of(controllerId).startClusterWatch(ClusterWatchId.of(clusterId)).get();
            CompletableFuture<Void> started = CompletableFuture.runAsync(() -> {
                //do nothing
            });
            startedWatches.put(controllerId, started);
        } catch (Exception e) {
            LOGGER.error("[ClusterWatch] starting for " + controllerId + " failed", e);
        }
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
                    //do nothing
                }).thenAccept(v -> {
                    if (startedWatches.get(controllerId).isDone()) {
                        startedWatches.remove(controllerId);
                        LOGGER.info("[ClusterWatch] stopped for " + controllerId);
                    } else {
                        throw new JocServiceException("[ClusterWatch] stop for " + controllerId + " is called but Cluster Watch is still running.");
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

}
