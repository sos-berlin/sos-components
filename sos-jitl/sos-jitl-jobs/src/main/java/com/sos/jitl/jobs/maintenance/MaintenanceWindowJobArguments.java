package com.sos.jitl.jobs.maintenance;

import java.util.List;

import com.sos.commons.credentialstore.common.SOSCredentialStoreArguments;
import com.sos.jitl.jobs.common.JobArgument;
import com.sos.jitl.jobs.common.JobArguments;

public class MaintenanceWindowJobArguments extends JobArguments {

    public enum StateValues {
        ACTIVE, INACTIVE
    }

    private JobArgument<String> maintenanceProfile = new JobArgument<String>("maintenance_profile", false);

    private JobArgument<String> controllerId = new JobArgument<String>("controller_id", false);
    private JobArgument<String> controllerHost = new JobArgument<String>("controller_host", false);
    private JobArgument<StateValues> state = new JobArgument<StateValues>("state", false);
    private JobArgument<String> jocHost = new JobArgument<String>("joc_host", false);
    private JobArgument<List<String>> subAgentIds = new JobArgument<List<String>>("subagent_ids", false);
    private JobArgument<List<String>> agentIds = new JobArgument<List<String>>("agent_ids", false);

    public MaintenanceWindowJobArguments() {
        super(new SOSCredentialStoreArguments());
    }

    public String getMaintenanceProfile() {
        return maintenanceProfile.getValue();
    }

    public void setMaintenanceProfile(String maintenanceProfile) {
        this.maintenanceProfile.setValue(maintenanceProfile);
    }

    public String getControllerId() {
        return controllerId.getValue();
    }

    public void setControllerId(String controllerId) {
        this.controllerId.setValue(controllerId);
    }

    public String getControllerHost() {
        return controllerHost.getValue();
    }

    public void setControllerHost(String controllerHost) {
        this.controllerHost.setValue(controllerHost);
    }

    public StateValues getState() {
        return state.getValue();
    }

    public void setState(StateValues state) {
        this.state.setValue(state);
    }

    public String getJocHost() {
        return jocHost.getValue();
    }

    public void setJocHost(String jocHost) {
        this.jocHost.setValue(jocHost);
    }

    public List<String> getAgentIds() {
        return agentIds.getValue();
    }

    public void setAgentIds(List<String> agentIds) {
        this.agentIds.setValue(agentIds);
    }

    public List<String> getSubAgentIds() {
        return subAgentIds.getValue();
    }

    public void setSubAgentIds(List<String> subAgentIds) {
        this.subAgentIds.setValue(subAgentIds);
    }
}
