package com.sos.joc.event.bean.cluster;

public class ActiveClusterChangedEvent extends ClusterEvent {

    private String newClusterMemberId;
    private String oldClusterMemberId;

    public String getNewClusterMemberId() {
        return newClusterMemberId;
    }

    public void setNewClusterMemberId(String val) {
        newClusterMemberId = val;
    }

    public String getOldClusterMemberId() {
        return oldClusterMemberId;
    }

    public void setOldClusterMemberId(String val) {
        oldClusterMemberId = val;
    }

}
