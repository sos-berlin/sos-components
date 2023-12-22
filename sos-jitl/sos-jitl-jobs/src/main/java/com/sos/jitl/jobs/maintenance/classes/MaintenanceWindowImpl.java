package com.sos.jitl.jobs.maintenance.classes;

import java.util.Map;

import com.sos.jitl.jobs.maintenance.MaintenanceWindowJobArguments;
import com.sos.jitl.jobs.maintenance.MaintenanceWindowJobArguments.StateValues;
import com.sos.joc.model.controller.Components;
import com.sos.joc.model.controller.Controller;
import com.sos.joc.model.joc.Cockpit;
import com.sos.js7.job.DetailValue;
import com.sos.js7.job.OrderProcessStep;
import com.sos.js7.job.OrderProcessStepLogger;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class MaintenanceWindowImpl {

    private MaintenanceWindowJobArguments args;
    private Map<String, DetailValue> jobResources;
    private OrderProcessStepLogger logger;
    private String controllerId;

    public MaintenanceWindowImpl(OrderProcessStep<MaintenanceWindowJobArguments> step) {
        this.args = step.getDeclaredArguments();
        this.jobResources = step.getJobResourcesArgumentsAsNameDetailValueMap();
        this.logger = step.getLogger();
    }

    public void executeApiCall() throws Exception {

        ApiExecutor apiExecutor = new ApiExecutor(logger);
        apiExecutor.setJobResources(jobResources);

        String accessToken = null;
        ApiResponse apiResponse = null;
        try {
            if (args.getState() != null) {
                apiResponse = apiExecutor.login();

                if (apiExecutor.getClient() != null) {
                    apiExecutor.getClient().setConnectionTimeout(10000);
                }

                if (apiResponse.getStatusCode() == 200) {
                    accessToken = apiResponse.getAccessToken();
                }

                MaintenanceWindowExecuter maintenanceWindowExecuter = new MaintenanceWindowExecuter(logger, apiExecutor);
            
                String controllerId = maintenanceWindowExecuter.getControllerid(accessToken, args.getControllerId());
                if (controllerId == null || controllerId.isEmpty()) {
                    controllerId = this.controllerId;
                }

                Components components = maintenanceWindowExecuter.getControllerClusterStatus(accessToken, controllerId);
                if (args.getControllerHost() != null) {
                    for (Controller controller : components.getControllers()) {
                        if (args.getState() != null && controller.getHost().equals(args.getControllerHost()) && !controller.getClusterNodeState()
                                .get_text().value().equalsIgnoreCase(args.getState().name())) {
                            maintenanceWindowExecuter.switchOverController(accessToken, controllerId);
                            break;
                        }
                    }
                }

                if (args.getJocHost() != null) {
                    for (Cockpit cockpit : components.getJocs()) {
                        if (args.getState().equals(StateValues.ACTIVE) && cockpit.getHost().equals(args.getJocHost()) && !cockpit
                                .getClusterNodeState().get_text().value().equalsIgnoreCase(args.getState().name())) {
                            maintenanceWindowExecuter.switchOverJoc(accessToken, cockpit.getMemberId());
                            break;
                        }
                        if (args.getState().equals(StateValues.INACTIVE) && cockpit.getHost().equals(args.getJocHost()) && !cockpit
                                .getClusterNodeState().get_text().value().equalsIgnoreCase(args.getState().name())) {
                            for (Cockpit cockpit2 : components.getJocs()) {
                                if (!cockpit2.getHost().equals(args.getJocHost())) {
                                    maintenanceWindowExecuter.switchOverJoc(accessToken, cockpit2.getMemberId());
                                    break;
                                }

                            }
                        }
                    }
                }

                if (args.getAgentIds() != null && args.getAgentIds().size() > 0) {
                    maintenanceWindowExecuter.enOrDisableAgent(accessToken, args.getState().equals(StateValues.ACTIVE), controllerId, args
                            .getAgentIds());
                }
                if (args.getSubAgentIds() != null && args.getSubAgentIds().size() > 0) {
                    maintenanceWindowExecuter.enOrDisableSubAgent(accessToken, args.getState().equals(StateValues.ACTIVE), controllerId, args
                            .getSubAgentIds());
                }
            }

        } finally {
            if (accessToken != null) {
                apiExecutor.logout(accessToken);
            }
            apiExecutor.close();

        }
    }
}
