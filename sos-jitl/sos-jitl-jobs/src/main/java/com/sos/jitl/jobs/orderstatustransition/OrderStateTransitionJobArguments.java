package com.sos.jitl.jobs.orderstatustransition;

import java.util.List;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class OrderStateTransitionJobArguments extends JobArguments {

    private JobArgument<List<String>> workflowFolders = new JobArgument<List<String>>("workflow_folder", false);
    private JobArgument<List<String>> workflowSearchPatterns = new JobArgument<List<String>>("workflow_search_pattern", false);
    private JobArgument<List<String>> orderSearchPatterns = new JobArgument<List<String>>("order_search_pattern", false);
    private JobArgument<String> persistDuration = new JobArgument<String>("persist_duration", false);
    private JobArgument<List<String>> stateTransitionSources = new JobArgument<List<String>>("state_transition_source", false);
    private JobArgument<String> stateTransitionTarget = new JobArgument<String>("state_transition_target", false);

    public OrderStateTransitionJobArguments() {
        super(new SOSCredentialStoreArguments());
    }

    public List<String> getWorkflowFolders() {
        return workflowFolders.getValue();
    }

    public void setWorkflowFolders(List<String> workflowFolders) {
        this.workflowFolders.setValue(workflowFolders);
    }

    public List<String> getWorkflowSearchPattern() {
        return workflowSearchPatterns.getValue();
    }

    public void setWorkflowSearchPattern(List<String> workflowSearchPattern) {
        this.workflowSearchPatterns.setValue(workflowSearchPattern);
    }

    public List<String> getOorderSearchPatterns() {
        return orderSearchPatterns.getValue();
    }

    public void setOrderSearchPattern(List<String> orderSearchPatterns) {
        this.orderSearchPatterns.setValue(orderSearchPatterns);
    }

    public String getPersistDuration() {
        return persistDuration.getValue();
    }

    public void setPersistDuration(String persistDuration) {
        this.persistDuration.setValue(persistDuration);
    }

    public List<String> getStateTransitionSource() {
        return stateTransitionSources.getValue();
    }

    public void setStateTransitionSources(List<String> stateTransitionSource) {
        this.stateTransitionSources.setValue(stateTransitionSource);
    }

    public String getStateTransitionTarget() {
        return stateTransitionTarget.getValue();
    }

    public void setStateTransitionTarget(String stateTransitionTarget) {
        this.stateTransitionTarget.setValue(stateTransitionTarget);
    }

}
