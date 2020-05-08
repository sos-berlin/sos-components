package com.sos.jobscheduler.event.master.cluster.bean;

import java.util.LinkedHashMap;

import com.sos.jobscheduler.event.master.EventMeta.ClusterEventSeq;

public class ClusterEvent {

    private ClusterEventSeq type;
    private String activeId;
    private LinkedHashMap<String, String> idToUri;

    public ClusterEventSeq getType() {
        return type;
    }

    public void setType(ClusterEventSeq val) {
        type = val;
    }

    public String getActiveId() {
        return activeId;
    }

    public void setActiveId(String val) {
        activeId = val;
    }

    public LinkedHashMap<String, String> getIdToUri() {
        return idToUri;
    }

    public void setIdToUri(LinkedHashMap<String, String> val) {
        idToUri = val;
    }

    public String getActiveClusterUri() {
        if (idToUri != null && activeId != null) {
            return idToUri.get(activeId);
        }
        return null;
    }
}
