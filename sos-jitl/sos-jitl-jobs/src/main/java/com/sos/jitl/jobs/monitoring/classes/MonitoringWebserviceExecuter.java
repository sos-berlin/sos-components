package com.sos.jitl.jobs.monitoring.classes;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.exception.SOSException;
import com.sos.jitl.jobs.common.Globals;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.jitl.jobs.jocapi.ApiExecutor;
import com.sos.jitl.jobs.jocapi.ApiResponse;
import com.sos.joc.model.agent.AgentV;
import com.sos.joc.model.agent.AgentsV;
import com.sos.joc.model.agent.ReadAgentsV;
import com.sos.joc.model.controller.ClusterNodeStateText;
import com.sos.joc.model.controller.Components;
import com.sos.joc.model.controller.Controller;
import com.sos.joc.model.controller.ControllerIdReq;
import com.sos.joc.model.controller.Controllers;
import com.sos.joc.model.controller.JobScheduler200;
import com.sos.joc.model.controller.JobSchedulerP;
import com.sos.joc.model.controller.JobSchedulerP200;
import com.sos.joc.model.jitl.monitoring.MonitoringControllerStatus;
import com.sos.joc.model.jitl.monitoring.MonitoringJocStatus;
import com.sos.joc.model.joc.Cockpit;
import com.sos.joc.model.order.OrdersFilterV;
import com.sos.joc.model.order.OrdersHistoricSummary;
import com.sos.joc.model.order.OrdersOverView;
import com.sos.joc.model.order.OrdersSnapshot;
import com.sos.joc.model.order.OrdersSummary;

public class MonitoringWebserviceExecuter {

    private ApiExecutor apiExecutor;
    private JobLogger logger;

    public MonitoringWebserviceExecuter(JobLogger logger, ApiExecutor apiExecutor) {
        super();
        this.apiExecutor = apiExecutor;
        this.logger = logger;
    }

    private Controller getVolatileControllerStatus(String body, String accessToken, String controllerId) throws Exception {

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/controller", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MonitoringErrorResponse monitoringErrorResponse = Globals.objectMapper.readValue(apiResponse.getResponseBody(),
                    MonitoringErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s", apiResponse.getStatusCode(), monitoringErrorResponse.getError()
                    .getMessage(), monitoringErrorResponse.getMessage()));
        }

        Globals.debug(logger, body);
        Globals.debug(logger, "answer=" + answer);
        JobScheduler200 volatileStatus = Globals.objectMapper.readValue(answer, JobScheduler200.class);

        return volatileStatus.getController();
    }

    private JobSchedulerP getPermanentControllerStatus(String body, String accessToken, String controllerId) throws Exception {

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/controller/p", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MonitoringErrorResponse monitoringErrorResponse = Globals.objectMapper.readValue(apiResponse.getResponseBody(),
                    MonitoringErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s", apiResponse.getStatusCode(), monitoringErrorResponse.getError()
                    .getMessage(), monitoringErrorResponse.getMessage()));
        }

        Globals.debug(logger, body);
        Globals.debug(logger, "answer=" + answer);
        JobSchedulerP200 permanentStatus = Globals.objectMapper.readValue(answer, JobSchedulerP200.class);

        return permanentStatus.getController();
    }

    private Controllers getPermanentControllersStatus(String body, String accessToken, String controllerId) throws Exception {

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/controllers/p", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MonitoringErrorResponse monitoringErrorResponse = Globals.objectMapper.readValue(apiResponse.getResponseBody(),
                    MonitoringErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s", apiResponse.getStatusCode(), monitoringErrorResponse.getError()
                    .getMessage(), monitoringErrorResponse.getMessage()));
        }

        Globals.debug(logger, body);
        Globals.debug(logger, "answer=" + answer);
        Controllers permanentStatus = Globals.objectMapper.readValue(answer, Controllers.class);

        return permanentStatus;
    }

    public MonitoringJocStatus getJS7JOCInstance(String accessToken, String controllerId) throws Exception {

        ControllerIdReq controllerIdReq = new ControllerIdReq();
        controllerIdReq.setControllerId(controllerId);
        String body = Globals.objectMapper.writeValueAsString(controllerIdReq);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/controller/components", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MonitoringErrorResponse monitoringErrorResponse = Globals.objectMapper.readValue(apiResponse.getResponseBody(),
                    MonitoringErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s", apiResponse.getStatusCode(), monitoringErrorResponse.getError()
                    .getMessage(), monitoringErrorResponse.getMessage()));
        }

        Globals.debug(logger, body);
        Globals.debug(logger, "answer=" + answer);
        Components components = Globals.objectMapper.readValue(answer, Components.class);

        MonitoringJocStatus monitoringJocStatus = new MonitoringJocStatus();
        for (Cockpit cockpit : components.getJocs()) {
            if (cockpit.getClusterNodeState() == null || cockpit.getClusterNodeState().get_text() == ClusterNodeStateText.active) {
                monitoringJocStatus.setActive(cockpit);
            } else {
                if (cockpit.getClusterNodeState().get_text() == ClusterNodeStateText.inactive) {
                    monitoringJocStatus.getPassive().add(cockpit);
                }
            }
        }

        return monitoringJocStatus;
    }

    public MonitoringControllerStatus getControllerStatus(String accessToken, String controllerId) throws Exception {
        ControllerIdReq controllerIdReq = new ControllerIdReq();
        controllerIdReq.setControllerId(controllerId);
        String body = Globals.objectMapper.writeValueAsString(controllerIdReq);

        JobSchedulerP permanentStatus = getPermanentControllerStatus(body, accessToken, controllerId);
        Controller volatileStatus = getVolatileControllerStatus(body, accessToken, controllerId);
        Controllers controllers = getPermanentControllersStatus(body, accessToken, controllerId);

        MonitoringControllerStatus monitoringControllerStatus = new MonitoringControllerStatus();
        monitoringControllerStatus.setVolatileStatus(volatileStatus);
        monitoringControllerStatus.setPermanentStatus(permanentStatus);

        for (Controller controller : controllers.getControllers()) {

            if (controller.getUrl().equals(volatileStatus.getUrl())) {
                if ((volatileStatus.getClusterNodeState() == null) || (volatileStatus.getClusterNodeState()
                        .get_text() == ClusterNodeStateText.active)) {
                    monitoringControllerStatus.setActive(controller);
                } else {
                    monitoringControllerStatus.setPassive(controller);
                }
            } else {
                if (volatileStatus.getClusterNodeState().get_text() == ClusterNodeStateText.active) {
                    monitoringControllerStatus.setActive(controller);
                } else {
                    monitoringControllerStatus.setPassive(controller);
                }
            }
        }

        return monitoringControllerStatus;
    }

    public List<AgentV> getJS7AgentStatus(String accessToken, String controllerId) throws JsonProcessingException, SOSException {

        ReadAgentsV readAgentsV = new ReadAgentsV();
        readAgentsV.setCompact(true);
        readAgentsV.setControllerId(controllerId);
        String body = Globals.objectMapper.writeValueAsString(readAgentsV);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/agents", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MonitoringErrorResponse monitoringErrorResponse = Globals.objectMapper.readValue(apiResponse.getResponseBody(),
                    MonitoringErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s", apiResponse.getStatusCode(), monitoringErrorResponse.getError()
                    .getMessage(), monitoringErrorResponse.getMessage()));
        }

        Globals.debug(logger, body);
        Globals.debug(logger, "answer=" + answer);
        AgentsV agentsV = Globals.objectMapper.readValue(answer, AgentsV.class);
        return agentsV.getAgents();

    }

    public OrdersSummary getJS7OrderSnapshot(String accessToken, String controllerId) throws JsonProcessingException, SOSException {
        OrdersFilterV ordersFilterV = new OrdersFilterV();
        ordersFilterV.setControllerId(controllerId);
        ordersFilterV.setDateTo("1d");
        String body = Globals.objectMapper.writeValueAsString(ordersFilterV);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/orders/overview/snapshot", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MonitoringErrorResponse monitoringErrorResponse = Globals.objectMapper.readValue(apiResponse.getResponseBody(),
                    MonitoringErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s", apiResponse.getStatusCode(), monitoringErrorResponse.getError()
                    .getMessage(), monitoringErrorResponse.getMessage()));
        }

        Globals.debug(logger, body);
        Globals.debug(logger, "answer=" + answer);
        OrdersSnapshot ordersSnapshot = Globals.objectMapper.readValue(answer, OrdersSnapshot.class);
        return ordersSnapshot.getOrders();
    }

    public OrdersHistoricSummary getJS7OrderSummary(String accessToken, String controllerId) throws JsonProcessingException, SOSException {
        OrdersFilterV ordersFilterV = new OrdersFilterV();
        ordersFilterV.setControllerId(controllerId);
        String body = Globals.objectMapper.writeValueAsString(ordersFilterV);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/orders/overview/snapshot", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MonitoringErrorResponse monitoringErrorResponse = Globals.objectMapper.readValue(apiResponse.getResponseBody(),
                    MonitoringErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s", apiResponse.getStatusCode(), monitoringErrorResponse.getError()
                    .getMessage(), monitoringErrorResponse.getMessage()));
        }

        Globals.debug(logger, body);
        Globals.debug(logger, "answer=" + answer);
        OrdersOverView ordersOverView = Globals.objectMapper.readValue(answer, OrdersOverView.class);
        return ordersOverView.getOrders();
    }

}
