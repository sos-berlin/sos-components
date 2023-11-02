package com.sos.jitl.jobs.orderstatustransition;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.sos.commons.credentialstore.CredentialStoreArguments;
import com.sos.js7.job.JobArgument;
import com.sos.js7.job.JobArguments;

public class OrderStateTransitionJobArguments extends JobArguments {

    private JobArgument<List<String>> workflowFolders = new JobArgument<>("workflow_folders", false, new ArrayList<>(), (List<String>) null);
    private JobArgument<List<String>> workflowSearchPatterns = new JobArgument<>("workflow_search_patterns", false, new ArrayList<>(),
            (List<String>) null);
    private JobArgument<List<String>> orderSearchPatterns = new JobArgument<>("order_search_patterns", false, new ArrayList<>(), (List<String>) null);
    private JobArgument<String> persistDuration = new JobArgument<>("persist_duration", false);
    private JobArgument<String> states = new JobArgument<>("states", true,Collections.singletonList("state_transition_source"));
    private JobArgument<String> transition = new JobArgument<>("transition", true,  Collections.singletonList("state_transition_target"));
    private JobArgument<String> controllerId = new JobArgument<>("controller_id", false);
    private JobArgument<Integer> batchSize = new JobArgument<>("batch_size", false, 10000);

    public OrderStateTransitionJobArguments() {
        super(new CredentialStoreArguments());
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

    public String getStates() {
        return states.getValue();
    }

    public void setStates(String states) {
        this.states.setValue(states);
    }

    public String getTransition() {
        return transition.getValue();
    }

    public void setTransition(String transition) {
        this.transition.setValue(transition);
    }

    public String getControllerId() {
        return controllerId.getValue();
    }

    public void setControllerId(String controllerId) {
        this.controllerId.setValue(controllerId);
    }

    public int getBatchSize() {
        return batchSize.getValue();
    }

    public void setBatchSize(int batchSize) {
        this.batchSize.setValue(batchSize);
    }

}
