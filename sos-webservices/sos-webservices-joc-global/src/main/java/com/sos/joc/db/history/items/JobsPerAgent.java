package com.sos.joc.db.history.items;

public class JobsPerAgent {
    
    private String agentId;
    private boolean error;
    private Long count;

    public JobsPerAgent(String agentId, boolean error, Long count) {
        this.agentId = agentId ;
        this.error = error;
        this.count = count;
    }

    public String getAgentId() {
        return agentId;
    }

    public boolean getError() {
        return error;
    }
    
    public Long getCount() {
        return count;
    }
}
