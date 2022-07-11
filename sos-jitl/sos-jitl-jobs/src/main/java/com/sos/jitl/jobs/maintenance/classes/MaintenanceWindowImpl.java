package com.sos.jitl.jobs.maintenance.classes;

import com.sos.commons.exception.SOSException;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.jocapi.ApiExecutor;
import com.sos.jitl.jobs.jocapi.ApiResponse;
import com.sos.jitl.jobs.maintenance.MaintenanceWindowJobArguments;
import com.sos.jitl.jobs.maintenance.MaintenanceWindowJobArguments.StateValues;
import com.sos.joc.model.controller.Components;
import com.sos.joc.model.controller.Controller;
import com.sos.joc.model.joc.Cockpit;

public class MaintenanceWindowImpl {

    private MaintenanceWindowJobArguments args;
    private JobLogger logger;

    public MaintenanceWindowImpl(JobLogger logger, MaintenanceWindowJobArguments args) {
        this.args = args;
        this.logger = logger;
    }

    public void executeApiCall() throws Exception {

        ApiExecutor apiExecutor = new ApiExecutor(logger);
        apiExecutor.getClient().setConnectionTimeout(10000);
        String accessToken = null;
        ApiResponse apiResponse = null;
        try {
            if (args.getState() != null) {
                apiResponse = apiExecutor.login();
                if (apiResponse.getStatusCode() == 200) {
                    accessToken = apiResponse.getAccessToken();
                } else {
                    String message = apiResponse.getStatusCode() + " " + apiExecutor.getClient().getHttpResponse().getStatusLine().getReasonPhrase();
                    throw new SOSException(message);
                }
                MaintenanceWindowExecuter maintenanceWindowExecuter = new MaintenanceWindowExecuter(logger, apiExecutor);
                // ManageMaintenanceWindowProfile manageMaintenanceWindowProfile =
                // maintenanceWindowExecuter.getSettings(accessToken,args.getMaintenanceProfile());

                Components components = maintenanceWindowExecuter.getControllerClusterStatus(accessToken, args.getControllerId());
                if (args.getControllerHost() != null) {
                    for (Controller controller : components.getControllers()) {
                        if (args.getState() != null && controller.getHost().equals(args.getControllerHost()) && !controller.getClusterNodeState()
                                .get_text().value().equalsIgnoreCase(args.getState().name())) {
                            maintenanceWindowExecuter.switchOverController(accessToken, args.getControllerId());
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
                    maintenanceWindowExecuter.enOrDisableAgent(accessToken, args.getState().equals(StateValues.ACTIVE), args.getControllerId(), args
                            .getAgentIds());
                }
                if (args.getSubAgentIds() != null && args.getSubAgentIds().size() > 0) {
                    maintenanceWindowExecuter.enOrDisableSubAgent(accessToken, args.getState().equals(StateValues.ACTIVE), args.getControllerId(),
                            args.getSubAgentIds());
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
