package com.sos.joc.event.bean.deploy;

public class DeployHistoryFileOrdersSourceEvent extends DeployHistoryEvent {

    /** No args constructor for use in serialization */
    public DeployHistoryFileOrdersSourceEvent() {
    }
    
    public DeployHistoryFileOrdersSourceEvent(String controllerId) {
        super(DeployHistoryFileOrdersSourceEvent.class.getSimpleName(), controllerId, null, null, null, null);
    }

    public DeployHistoryFileOrdersSourceEvent(String controllerId, String name, String commitId, String path, Integer objectType) {
        super(DeployHistoryFileOrdersSourceEvent.class.getSimpleName(), controllerId, name, commitId, path, objectType);
    }

}
