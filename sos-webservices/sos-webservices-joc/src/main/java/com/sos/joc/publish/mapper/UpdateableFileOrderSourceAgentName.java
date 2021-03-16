package com.sos.joc.publish.mapper;


public class UpdateableFileOrderSourceAgentName {

    private String fileOrderSourceId;
    private String agentName;
    private String agentId;
    private String controllerId;
    
    
    public UpdateableFileOrderSourceAgentName (String fileOrderSourceId, String agentName, String agentId, String controllerId) {
        this.fileOrderSourceId = fileOrderSourceId;
        this.agentName = agentName;
        this.agentId = agentId;
        this.controllerId = controllerId;
    }
    
    public String getFileOrderSourceId() {
        return fileOrderSourceId;
    }
    
    public String getAgentName() {
        return agentName;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
    public String getControllerId() {
        return controllerId;
    }
    
}
