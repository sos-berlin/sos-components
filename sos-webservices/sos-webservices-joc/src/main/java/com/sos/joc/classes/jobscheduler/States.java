package com.sos.joc.classes.jobscheduler;

import com.sos.jobscheduler.model.cluster.ClusterType;
import com.sos.joc.model.jobscheduler.ClusterNodeState;
import com.sos.joc.model.jobscheduler.ClusterNodeStateText;
import com.sos.joc.model.jobscheduler.ClusterState;
import com.sos.joc.model.jobscheduler.ComponentState;
import com.sos.joc.model.jobscheduler.ComponentStateText;
import com.sos.joc.model.jobscheduler.ConnectionState;
import com.sos.joc.model.jobscheduler.ConnectionStateText;

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
            componentState.setSeverity(3);
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
            connectionState.setSeverity(3);
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
            clusterNodeState.setSeverity(3);
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
            clusterState.setSeverity(3);
            clusterState.set_text("ClusterUnknown");
            return clusterState;
        }
        clusterState.set_text(state.value());
        switch (state) {
        case CLUSTER_COUPLED:
            clusterState.setSeverity(0);
            break;
        case CLUSTER_FAILED_OVER:
            clusterState.setSeverity(1);
            break;
        case CLUSTER_SWITCHED_OVER:
            clusterState.setSeverity(1);
            break;
        case CLUSTER_PASSIVE_LOST:
            clusterState.setSeverity(1);
            break;
        case CLUSTER_NODES_APPOINTED:
            clusterState.setSeverity(1);
            break;
        case CLUSTER_PREPARED_TO_BE_COUPLED:
            clusterState.setSeverity(2);
            break;
        case CLUSTER_EMPTY:
            clusterState.setSeverity(2);
            break;
        case CLUSTER_SOLE:
            clusterState.setSeverity(1);
            break;
        }
        return clusterState;
    }
}
