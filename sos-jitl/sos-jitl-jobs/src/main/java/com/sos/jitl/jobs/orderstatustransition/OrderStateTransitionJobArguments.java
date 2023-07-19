package com.sos.jitl.jobs.orderstatustransition;

import java.util.ArrayList;
import java.util.List;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.commons.job.JobArgument;
import com.sos.commons.job.JobArguments;

public class OrderStateTransitionJobArguments extends JobArguments {

    private JobArgument<List<String>> workflowFolders = new JobArgument<>("workflow_folders", false,new ArrayList<String>());
    private JobArgument<List<String>> workflowSearchPatterns = new JobArgument<>("workflow_search_patterns", false,new ArrayList<String>());
    private JobArgument<List<String>> orderSearchPatterns = new JobArgument<>("order_search_patterns", false,new ArrayList<String>());
    private JobArgument<String> persistDuration = new JobArgument<>("persist_duration", false);
    private JobArgument<String> stateTransitionSource = new JobArgument<>("state_transition_source", true);
    private JobArgument<String> stateTransitionTarget = new JobArgument<>("state_transition_target", true);
    private JobArgument<String> controllerId = new JobArgument<>("controller_id", false);

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

    public List<String> getOrderSearchPatterns() {
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

    public String getStateTransitionSource() {
        return stateTransitionSource.getValue();
    }

    public void setStateTransitionSource(String stateTransitionSource) {
        this.stateTransitionSource.setValue(stateTransitionSource);
    }

    public String getStateTransitionTarget() {
        return stateTransitionTarget.getValue();
    }

    public void setStateTransitionTarget(String stateTransitionTarget) {
        this.stateTransitionTarget.setValue(stateTransitionTarget);
    }

    public String getControllerId() {
        return controllerId.getValue();
    }

    public void setControllerId(String controllerId) {
        this.controllerId.setValue(controllerId);
    }

}
