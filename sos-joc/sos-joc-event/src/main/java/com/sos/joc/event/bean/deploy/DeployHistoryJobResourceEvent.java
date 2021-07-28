package com.sos.joc.event.bean.deploy;

public class DeployHistoryJobResourceEvent extends DeployHistoryEvent {

    /** No args constructor for use in serialization */
    public DeployHistoryJobResourceEvent() {
    }

    public DeployHistoryJobResourceEvent(String controllerId, String name, String commitId, String path, Integer objectType) {
        super(DeployHistoryJobResourceEvent.class.getSimpleName(), controllerId, name, commitId, path, objectType);
    }

}
