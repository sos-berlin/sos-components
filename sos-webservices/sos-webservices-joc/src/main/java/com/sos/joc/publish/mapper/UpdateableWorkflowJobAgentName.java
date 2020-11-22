package com.sos.joc.publish.mapper;

public class UpdateableWorkflowJobAgentName {

    private String workflowPath;
    private String jobName;
    private String agentName;
    private String agentId;
    
    
    public UpdateableWorkflowJobAgentName (String workflowPath, String jobName, String agentName, String agentId) {
        this.workflowPath = workflowPath;
        this.jobName = jobName;
        this.agentName = agentName;
        this.agentId = agentId;
    }
    
    public String getWorkflowPath() {
        return workflowPath;
    }
    
    public String getJobName() {
        return jobName;
    }

    public String getAgentName() {
        return agentName;
    }
    
    public String getAgentId() {
        return agentId;
    }
    
}
