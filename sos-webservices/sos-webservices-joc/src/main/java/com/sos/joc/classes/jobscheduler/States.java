package com.sos.joc.classes.jobscheduler;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.controller.model.cluster.ClusterType;
import com.sos.joc.Globals;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.model.controller.ClusterNodeState;
import com.sos.joc.model.controller.ClusterNodeStateText;
import com.sos.joc.model.controller.ClusterState;
import com.sos.joc.model.controller.ComponentState;
import com.sos.joc.model.controller.ComponentStateText;
import com.sos.joc.model.controller.ConnectionState;
import com.sos.joc.model.controller.ConnectionStateText;

import js7.proxy.javaapi.data.cluster.JClusterState;

public class States {
    
    public static ComponentState getComponentState(ComponentStateText state) {
        ComponentState componentState = new ComponentState();
        componentState.set_text(state);
        switch (state) {
        case operational:
            componentState.setSeverity(0);
            break;
        case limited:
            componentState.setSeverity(1);
            break;
        case inoperable:
            componentState.setSeverity(2);
            break;
        case unknown:
            componentState.setSeverity(2);
            break;
        }
        return componentState;
    }
    
    public static ConnectionState getConnectionState(ConnectionStateText state) {
        ConnectionState connectionState = new ConnectionState();
        connectionState.set_text(state);
        switch (state) {
        case established:
            connectionState.setSeverity(0);
            break;
        case unstable:
            connectionState.setSeverity(1);
            break;
        case unreachable:
            connectionState.setSeverity(2);
            break;
        case unknown:
            connectionState.setSeverity(2);
            break;
        }
        return connectionState;
    }
    
    public static ClusterNodeState getClusterNodeState(Boolean isActive, boolean isCluster) {
        if (!isCluster) {
           return null; 
        }
        ClusterNodeState clusterNodeState = new ClusterNodeState();
        if (isActive == null) {
            clusterNodeState.setSeverity(2);
            clusterNodeState.set_text(ClusterNodeStateText.unknown);
        } else if (isActive) {
            clusterNodeState.setSeverity(0);
            clusterNodeState.set_text(ClusterNodeStateText.active);
        } else {
            clusterNodeState.setSeverity(1);
            clusterNodeState.set_text(ClusterNodeStateText.inactive);
        }
        return clusterNodeState;
    }
    
    public static ClusterState getClusterState(ClusterType state) {
        ClusterState clusterState = new ClusterState();
        if (state == null) {
            clusterState.setSeverity(2);
            clusterState.set_text("ClusterUnknown");
            return clusterState;
        }
        clusterState.set_text("Cluster" + state.value());
        switch (state) {
        case COUPLED:
            clusterState.setSeverity(0);
            break;
        case FAILED_OVER:
        case SWITCHED_OVER:
        case PASSIVE_LOST:
        case NODES_APPOINTED:
            clusterState.setSeverity(1);
            break;
        case ACTIVE_SHUT_DOWN:
        case PREPARED_TO_BE_COUPLED:
        case EMPTY:
            clusterState.setSeverity(2);
            break;
        }
        return clusterState;
    }
    
    public static DBItemInventoryJSInstance getActiveControllerNode(List<DBItemInventoryJSInstance> controllerInstances, JClusterState jClusterState)
            throws JsonParseException, JsonMappingException, IOException {
        DBItemInventoryJSInstance controllerInstance = null;
        if (controllerInstances.size() > 1) { // is cluster
            controllerInstance = getActiveControllerNode(controllerInstances, Globals.objectMapper.readValue(jClusterState.toJson(),
                    com.sos.controller.model.cluster.ClusterState.class));
        } else { // is standalone
            controllerInstance = controllerInstances.get(0);
        }
        return controllerInstance;
    }

    public static DBItemInventoryJSInstance getActiveControllerNode(List<DBItemInventoryJSInstance> controllerInstances,
            com.sos.controller.model.cluster.ClusterState clusterState) {
        DBItemInventoryJSInstance controllerInstance = null;
        if (clusterState != null) {
            switch (clusterState.getTYPE()) {
            case EMPTY:
                break;
            default:
                final String activeClusterUri = clusterState.getSetting().getIdToUri().getAdditionalProperties().get(clusterState.getSetting().getActiveId());
                Predicate<DBItemInventoryJSInstance> predicate = i -> activeClusterUri.equalsIgnoreCase(i.getClusterUri()) || activeClusterUri
                        .equalsIgnoreCase(i.getUri());
                Optional<DBItemInventoryJSInstance> o = controllerInstances.stream().filter(predicate).findAny();
                if (o.isPresent()) {
                    controllerInstance = o.get();
                }
                break;
            }
        }
        if (controllerInstance == null) {
            Optional<DBItemInventoryJSInstance> o = controllerInstances.stream().filter(i -> i.getIsPrimary()).findAny();
            if (o.isPresent()) {
                controllerInstance = o.get();
            } else {
                controllerInstance = controllerInstances.get(0);
            }
        }
        return controllerInstance;
    }
}
