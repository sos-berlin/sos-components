
package com.sos.joc.classes.agent;

import java.util.Optional;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sos.controller.model.cluster.ClusterState;


public class AgentDirectorClusterState extends ClusterState {

    @JsonIgnore
    private String lostNodeId;
    
    @JsonIgnore
    private String lostNodeProblem;

    @JsonIgnore
    public String getLostNodeId() {
        return lostNodeId;
    }

    public void setLostNodeId(String lostNodeId) {
        this.lostNodeId = lostNodeId;
    }
    
    @JsonIgnore
    public Optional<String> getLostNodeProblem() {
        return lostNodeProblem == null ? Optional.empty() : Optional.of(lostNodeProblem);
    }

    public void setLostNodeProblem(String lostNodeProblem) {
        this.lostNodeProblem = lostNodeProblem;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        return super.equals(other);
    }

}
