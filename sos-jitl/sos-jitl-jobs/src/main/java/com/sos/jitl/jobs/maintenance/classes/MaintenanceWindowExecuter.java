package com.sos.jitl.jobs.maintenance.classes;

import java.util.List;

import com.sos.commons.exception.SOSException;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.jocapi.ApiExecutor;
import com.sos.jitl.jobs.jocapi.ApiResponse;
import com.sos.joc.model.agent.AgentsV;
import com.sos.joc.model.agent.DeployAgents;
import com.sos.joc.model.agent.ReadAgentsV;
import com.sos.joc.model.agent.SubAgentsCommand;
import com.sos.joc.model.cluster.ClusterSwitchMember;
import com.sos.joc.model.controller.Components;
import com.sos.joc.model.controller.ControllerIdReq;

public class MaintenanceWindowExecuter {

    private ApiExecutor apiExecutor;
    private JobLogger logger;

    public MaintenanceWindowExecuter(JobLogger logger, ApiExecutor apiExecutor) {
        super();
        this.apiExecutor = apiExecutor;
        this.logger = logger;
    }

    public Components getControllerClusterStatus(String accessToken, String controllerId) throws Exception {
        ControllerIdReq controllerIdReq = new ControllerIdReq();
        controllerIdReq.setControllerId(controllerId);

        String body = Globals.objectMapper.writeValueAsString(controllerIdReq);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/controller/components", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MaintenanceErrorResponse maintenanceErrorResponse = Globals.objectMapper.readValue(apiResponse.getResponseBody(),
                    MaintenanceErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s %s", apiResponse.getStatusCode(), maintenanceErrorResponse.getError()
                    .getMessage(), maintenanceErrorResponse.getMessage(), maintenanceErrorResponse.getRole()));
        }

        Globals.debug(logger, body);
        Globals.debug(logger, "answer=" + answer);
        Components components = Globals.objectMapper.readValue(answer, Components.class);

        return components;
    }

    public void switchOverController(String accessToken, String controllerId) throws Exception {
        ControllerIdReq controllerIdReq = new ControllerIdReq();
        controllerIdReq.setControllerId(controllerId);

        String body = Globals.objectMapper.writeValueAsString(controllerIdReq);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/controller/cluster/switchover", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MaintenanceErrorResponse maintenanceErrorResponse = Globals.objectMapper.readValue(apiResponse.getResponseBody(),
                    MaintenanceErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s %s", apiResponse.getStatusCode(), maintenanceErrorResponse.getError()
                    .getMessage(), maintenanceErrorResponse.getMessage(), maintenanceErrorResponse.getRole()));
        }
        Globals.debug(logger, body);
        Globals.debug(logger, "answer=" + answer);
    }

    public void switchOverJoc(String accessToken, String memberId) throws Exception {
        ClusterSwitchMember clusterSwitchMember = new ClusterSwitchMember();
        clusterSwitchMember.setMemberId(memberId);

        String body = Globals.objectMapper.writeValueAsString(clusterSwitchMember);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/joc/cluster/switch_member", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MaintenanceErrorResponse maintenanceErrorResponse = Globals.objectMapper.readValue(apiResponse.getResponseBody(),
                    MaintenanceErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s %s", apiResponse.getStatusCode(), maintenanceErrorResponse.getError()
                    .getMessage(), maintenanceErrorResponse.getMessage(), maintenanceErrorResponse.getRole()));
        }
        Globals.debug(logger, body);
        Globals.debug(logger, "answer=" + answer);
    }

    public void enOrDisableSubAgent(String accessToken, boolean enable, String controllerId, List<String> subagentIds) throws Exception {
        SubAgentsCommand subAgentsCommand = new SubAgentsCommand();
        subAgentsCommand.setControllerId(controllerId);
        subAgentsCommand.setSubagentIds(subagentIds);

        String body = Globals.objectMapper.writeValueAsString(subAgentsCommand);
        String answer = "";

        ApiResponse apiResponse = null;
        if (enable) {
            apiResponse = apiExecutor.post(accessToken, "/joc/api/agents/inventory/cluster/subagents/enable", body);
        } else {
            apiResponse = apiExecutor.post(accessToken, "/joc/api/agents/inventory/cluster/subagents/enable", body);
        }

        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MaintenanceErrorResponse maintenanceErrorResponse = Globals.objectMapper.readValue(apiResponse.getResponseBody(),
                    MaintenanceErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s %s", apiResponse.getStatusCode(), maintenanceErrorResponse.getError()
                    .getMessage(), maintenanceErrorResponse.getMessage(), maintenanceErrorResponse.getRole()));
        }

        Globals.debug(logger, body);
        Globals.debug(logger, "answer=" + answer);
    }

    public void enOrDisableAgent(String accessToken, boolean enable, String controllerId, List<String> agentIds) throws Exception {
        DeployAgents deployAgents = new DeployAgents();
        deployAgents.setControllerId(controllerId);
        deployAgents.setAgentIds(agentIds);

        String body = Globals.objectMapper.writeValueAsString(deployAgents);
        String answer = "";

        ApiResponse apiResponse = null;
        if (enable) {
            apiResponse = apiExecutor.post(accessToken, "/joc/api/agents/inventory/enable", body);
        } else {
            apiResponse = apiExecutor.post(accessToken, "/joc/api/agents/inventory/disable", body);
        }

        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MaintenanceErrorResponse maintenanceErrorResponse = Globals.objectMapper.readValue(apiResponse.getResponseBody(),
                    MaintenanceErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s %s", apiResponse.getStatusCode(), maintenanceErrorResponse.getError()
                    .getMessage(), maintenanceErrorResponse.getMessage(), maintenanceErrorResponse.getRole()));
        }
        Globals.debug(logger, body);
        Globals.debug(logger, "answer=" + answer);
    }

    public AgentsV getAgents(String accessToken, String controllerId) throws Exception {
        ReadAgentsV readAgentsV = new ReadAgentsV();
        readAgentsV.setControllerId(controllerId);
        readAgentsV.setOnlyVisibleAgents(false);

        String body = Globals.objectMapper.writeValueAsString(readAgentsV);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/agents", body);
        String answer = "";
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MaintenanceErrorResponse maintenanceErrorResponse = Globals.objectMapper.readValue(apiResponse.getResponseBody(),
                    MaintenanceErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s %s", apiResponse.getStatusCode(), maintenanceErrorResponse.getError()
                    .getMessage(), maintenanceErrorResponse.getMessage(), maintenanceErrorResponse.getRole()));
        }
        Globals.debug(logger, body);
        Globals.debug(logger, "answer=" + answer);
        AgentsV agentsV = Globals.objectMapper.readValue(answer, AgentsV.class);

        return agentsV;
    }

    /*
     * public ManageMaintenanceWindowProfile getSettings(String accessToken, String profile) throws Exception { Configuration configuration = new
     * Configuration(); configuration.setConfigurationType(ConfigurationType.SETTING); configuration.setObjectType("MAINTENANCE");
     * configuration.setShared(null); configuration.setId(0L); String body = Globals.objectMapper.writeValueAsString(configuration); String answer =
     * apiExecutor.post(accessToken, "/joc/api/configuration", body); Globals.debug(logger, body); Globals.debug(logger, "answer=" + answer); Configuration200
     * configuration200 = Globals.objectMapper.readValue(answer, Configuration200.class); ManageMaintenanceWindow manageMaintenanceWindow =
     * Globals.objectMapper.readValue(configuration200.getConfiguration().getConfigurationItem(), ManageMaintenanceWindow.class); for
     * (ManageMaintenanceWindowProfile manageMaintenanceWindowProfile : manageMaintenanceWindow.getManageMaintenanceWindowProfiles()) { if
     * (manageMaintenanceWindowProfile.getProfile().equals(profile)) { return manageMaintenanceWindowProfile; } } return null; }
     */

}
