package com.sos.jitl.jobs.maintenance.classes;

import java.util.List;

import com.sos.commons.exception.SOSException;
import com.sos.joc.model.agent.AgentsV;
import com.sos.joc.model.agent.DeployAgents;
import com.sos.joc.model.agent.ReadAgentsV;
import com.sos.joc.model.agent.SubAgentsCommand;
import com.sos.joc.model.cluster.ClusterSwitchMember;
import com.sos.joc.model.controller.Components;
import com.sos.joc.model.controller.Controller;
import com.sos.joc.model.controller.ControllerIdReq;
import com.sos.joc.model.controller.Controllers;
import com.sos.js7.job.JobHelper;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class MaintenanceWindowExecuter {

    private ApiExecutor apiExecutor;
    private OrderProcessStepLogger logger;

    public MaintenanceWindowExecuter(OrderProcessStepLogger logger, ApiExecutor apiExecutor) {
        super();
        this.apiExecutor = apiExecutor;
        this.logger = logger;
    }

    public String getControllerid(String accessToken, String controllerId) throws Exception {

        String defControllerId = "";
        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/controllers/p", "{}");
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MaintenanceErrorResponse maintenanceErrorResponse = JobHelper.OBJECT_MAPPER.readValue(apiResponse.getResponseBody(),
                    MaintenanceErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s %s", apiResponse.getStatusCode(), maintenanceErrorResponse.getError()
                    .getMessage(), maintenanceErrorResponse.getMessage(), maintenanceErrorResponse.getRole()));
        }

        logger.debug("answer=" + answer);
        Controllers controllers = JobHelper.OBJECT_MAPPER.readValue(answer, Controllers.class);
        for (Controller controller : controllers.getControllers()) {
            if (!defControllerId.isEmpty() & !defControllerId.equals(controller.getControllerId())) {
                defControllerId = "";
                break;
            }
            if (controller.getClusterUrl() != null && !controller.getClusterUrl().isEmpty() && defControllerId.isEmpty()) {
                defControllerId = controller.getControllerId();
            }
        }

        if (!defControllerId.isEmpty()) {
            logger.debug("ControllerId from Webservice");
            return defControllerId;
        } else {
            logger.debug("ControllerId from Arguments");
            return controllerId;
        }
    }

    public Components getControllerClusterStatus(String accessToken, String controllerId) throws Exception {
        ControllerIdReq controllerIdReq = new ControllerIdReq();
        controllerIdReq.setControllerId(controllerId);

        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(controllerIdReq);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/controller/components", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MaintenanceErrorResponse maintenanceErrorResponse = JobHelper.OBJECT_MAPPER.readValue(apiResponse.getResponseBody(),
                    MaintenanceErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s %s", apiResponse.getStatusCode(), maintenanceErrorResponse.getError()
                    .getMessage(), maintenanceErrorResponse.getMessage(), maintenanceErrorResponse.getRole()));
        }

        logger.debug(body);
        logger.debug("answer=" + answer);

        return JobHelper.OBJECT_MAPPER.readValue(answer, Components.class);
    }

    public void switchOverController(String accessToken, String controllerId) throws Exception {
        ControllerIdReq controllerIdReq = new ControllerIdReq();
        controllerIdReq.setControllerId(controllerId);

        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(controllerIdReq);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/controller/cluster/switchover", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MaintenanceErrorResponse maintenanceErrorResponse = JobHelper.OBJECT_MAPPER.readValue(apiResponse.getResponseBody(),
                    MaintenanceErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s %s", apiResponse.getStatusCode(), maintenanceErrorResponse.getError()
                    .getMessage(), maintenanceErrorResponse.getMessage(), maintenanceErrorResponse.getRole()));
        }
        logger.debug(body);
        logger.debug("answer=" + answer);
    }

    public void switchOverJoc(String accessToken, String memberId) throws Exception {
        ClusterSwitchMember clusterSwitchMember = new ClusterSwitchMember();
        clusterSwitchMember.setMemberId(memberId);

        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(clusterSwitchMember);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/joc/cluster/switch_member", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MaintenanceErrorResponse maintenanceErrorResponse = JobHelper.OBJECT_MAPPER.readValue(apiResponse.getResponseBody(),
                    MaintenanceErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s %s", apiResponse.getStatusCode(), maintenanceErrorResponse.getError()
                    .getMessage(), maintenanceErrorResponse.getMessage(), maintenanceErrorResponse.getRole()));
        }
        logger.debug(body);
        logger.debug("answer=" + answer);
    }

    public void enOrDisableSubAgent(String accessToken, boolean enable, String controllerId, List<String> subagentIds) throws Exception {
        SubAgentsCommand subAgentsCommand = new SubAgentsCommand();
        subAgentsCommand.setControllerId(controllerId);
        subAgentsCommand.setSubagentIds(subagentIds);

        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(subAgentsCommand);
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
            MaintenanceErrorResponse maintenanceErrorResponse = JobHelper.OBJECT_MAPPER.readValue(apiResponse.getResponseBody(),
                    MaintenanceErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s %s", apiResponse.getStatusCode(), maintenanceErrorResponse.getError()
                    .getMessage(), maintenanceErrorResponse.getMessage(), maintenanceErrorResponse.getRole()));
        }

        logger.debug(body);
        logger.debug("answer=" + answer);
    }

    public void enOrDisableAgent(String accessToken, boolean enable, String controllerId, List<String> agentIds) throws Exception {
        DeployAgents deployAgents = new DeployAgents();
        deployAgents.setControllerId(controllerId);
        deployAgents.setAgentIds(agentIds);

        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(deployAgents);
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
            MaintenanceErrorResponse maintenanceErrorResponse = JobHelper.OBJECT_MAPPER.readValue(apiResponse.getResponseBody(),
                    MaintenanceErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s %s", apiResponse.getStatusCode(), maintenanceErrorResponse.getError()
                    .getMessage(), maintenanceErrorResponse.getMessage(), maintenanceErrorResponse.getRole()));
        }
        logger.debug(body);
        logger.debug("answer=" + answer);
    }

    public AgentsV getAgents(String accessToken, String controllerId) throws Exception {
        ReadAgentsV readAgentsV = new ReadAgentsV();
        readAgentsV.setControllerId(controllerId);
        readAgentsV.setOnlyVisibleAgents(false);

        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(readAgentsV);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/agents", body);
        String answer = "";
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MaintenanceErrorResponse maintenanceErrorResponse = JobHelper.OBJECT_MAPPER.readValue(apiResponse.getResponseBody(),
                    MaintenanceErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s %s", apiResponse.getStatusCode(), maintenanceErrorResponse.getError()
                    .getMessage(), maintenanceErrorResponse.getMessage(), maintenanceErrorResponse.getRole()));
        }
        logger.debug(body);
        logger.debug("answer=" + answer);

        return JobHelper.OBJECT_MAPPER.readValue(answer, AgentsV.class);
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
