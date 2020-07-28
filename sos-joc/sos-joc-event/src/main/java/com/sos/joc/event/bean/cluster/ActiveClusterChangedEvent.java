package com.sos.joc.event.bean.cluster;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ActiveClusterChangedEvent extends ClusterEvent {

    @JsonProperty("newClusterMemberId")
    private String newClusterMemberId;
    @JsonProperty("oldClusterMemberId")
    private String oldClusterMemberId;
    
    /**
     * No args constructor for use in serialization
     * 
     */
    public ActiveClusterChangedEvent() {
        super();
    }

    /**
     * @param key
     * @param jobschedulerId
     * @param variables
     */
    public ActiveClusterChangedEvent(String newClusterMemberId, String oldClusterMemberId) {
        super();
        this.newClusterMemberId = newClusterMemberId;
        this.oldClusterMemberId = oldClusterMemberId;
    }
    
    @JsonProperty("newClusterMemberId")
    public String getNewClusterMemberId() {
        return newClusterMemberId;
    }

    @JsonProperty("newClusterMemberId")
    public void setNewClusterMemberId(String val) {
        newClusterMemberId = val;
    }

    @JsonProperty("oldClusterMemberId")
    public String getOldClusterMemberId() {
        return oldClusterMemberId;
    }

    @JsonProperty("oldClusterMemberId")
    public void setOldClusterMemberId(String val) {
        oldClusterMemberId = val;
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("newClusterMemberId", newClusterMemberId).append("oldClusterMemberId", oldClusterMemberId).toString();
    }

}
