package com.sos.joc.event.bean.cluster;

import org.apache.commons.lang3.builder.ToStringBuilder;

public class NewJocAddedEvent extends ClusterEvent {


    /**
     * No args constructor for use in serialization
     * 
     */
    public NewJocAddedEvent() {
        super();
    }

    /**
     * @param key
     * @param jobschedulerId
     * @param variables
     */
    public NewJocAddedEvent(Long jocId, String clusterId, String memberId, Integer ordering) {
        super();
        putVariable("jocId", jocId);
        putVariable("clusterId", clusterId);
        putVariable("memberId", memberId);
        putVariable("ordering", ordering);
    }
    
    public Long getJocId() {
        return (Long) getVariables().get("jocId");
    }
    
    public String getClusterId() {
        return (String) getVariables().get("clusterId");
    }
    
    public String getMemberId() {
        return (String) getVariables().get("memberId");
    }
    
    public Integer getOrdering() {
        return (Integer) getVariables().get("ordering");
    }
    
    @Override
    public String toString() {
        return new ToStringBuilder(this).appendSuper(super.toString()).append("jocId", getJocId().toString()).append("clusterId", getClusterId())
                .append("memberId", getMemberId()).append("ordering", getOrdering().toString()).toString();
    }

}
