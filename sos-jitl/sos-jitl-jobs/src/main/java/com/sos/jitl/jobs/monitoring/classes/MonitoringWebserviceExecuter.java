package com.sos.jitl.jobs.monitoring.classes;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.exception.SOSException;
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
import com.sos.js7.job.JobHelper;
import com.sos.js7.job.jocapi.ApiExecutor;
import com.sos.js7.job.jocapi.ApiResponse;

public class MonitoringWebserviceExecuter {

    private final ApiExecutor apiExecutor;

    public MonitoringWebserviceExecuter(ApiExecutor apiExecutor) {
        this.apiExecutor = apiExecutor;
    }

    private Controller getVolatileControllerStatus(String body, String accessToken, String controllerId) throws Exception {

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/controller", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MonitoringErrorResponse monitoringErrorResponse = JobHelper.OBJECT_MAPPER.readValue(apiResponse.getResponseBody(),
                    MonitoringErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s", apiResponse.getStatusCode(), monitoringErrorResponse.getError()
                    .getMessage(), monitoringErrorResponse.getMessage()));
        }
        if (apiExecutor.getLogger().isDebugEnabled()) {
            apiExecutor.getLogger().debug(body);
            apiExecutor.getLogger().debug("answer=%s", answer);
        }
        JobScheduler200 volatileStatus = JobHelper.OBJECT_MAPPER.readValue(answer, JobScheduler200.class);

        return volatileStatus.getController();
    }

    private JobSchedulerP getPermanentControllerStatus(String body, String accessToken, String controllerId) throws Exception {

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/controller/p", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MonitoringErrorResponse monitoringErrorResponse = JobHelper.OBJECT_MAPPER.readValue(apiResponse.getResponseBody(),
                    MonitoringErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s", apiResponse.getStatusCode(), monitoringErrorResponse.getError()
                    .getMessage(), monitoringErrorResponse.getMessage()));
        }
        if (apiExecutor.getLogger().isDebugEnabled()) {
            apiExecutor.getLogger().debug(body);
            apiExecutor.getLogger().debug("answer=%s", answer);
        }
        JobSchedulerP200 permanentStatus = JobHelper.OBJECT_MAPPER.readValue(answer, JobSchedulerP200.class);

        return permanentStatus.getController();
    }

    private Controllers getVolatileControllersStatus(String body, String accessToken, String controllerId) throws Exception {

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/controllers", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MonitoringErrorResponse monitoringErrorResponse = JobHelper.OBJECT_MAPPER.readValue(apiResponse.getResponseBody(),
                    MonitoringErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s", apiResponse.getStatusCode(), monitoringErrorResponse.getError()
                    .getMessage(), monitoringErrorResponse.getMessage()));
        }

        if (apiExecutor.getLogger().isDebugEnabled()) {
            apiExecutor.getLogger().debug(body);
            apiExecutor.getLogger().debug("answer=%s", answer);
        }
        Controllers permanentStatus = JobHelper.OBJECT_MAPPER.readValue(answer, Controllers.class);

        return permanentStatus;
    }

    public MonitoringJocStatus getJS7JOCInstance(String accessToken, String controllerId) throws Exception {

        ControllerIdReq controllerIdReq = new ControllerIdReq();
        controllerIdReq.setControllerId(controllerId);
        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(controllerIdReq);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/controller/components", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MonitoringErrorResponse monitoringErrorResponse = JobHelper.OBJECT_MAPPER.readValue(apiResponse.getResponseBody(),
                    MonitoringErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s", apiResponse.getStatusCode(), monitoringErrorResponse.getError()
                    .getMessage(), monitoringErrorResponse.getMessage()));
        }

        if (apiExecutor.getLogger().isDebugEnabled()) {
            apiExecutor.getLogger().debug(body);
            apiExecutor.getLogger().debug("answer=%s", answer);
        }

        Components components = JobHelper.OBJECT_MAPPER.readValue(answer, Components.class);

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
        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(controllerIdReq);

        JobSchedulerP permanentStatus = getPermanentControllerStatus(body, accessToken, controllerId);
        Controller volatileStatus = getVolatileControllerStatus(body, accessToken, controllerId);
        Controllers controllers = getVolatileControllersStatus(body, accessToken, controllerId);

        MonitoringControllerStatus monitoringControllerStatus = new MonitoringControllerStatus();
        monitoringControllerStatus.setVolatileStatus(volatileStatus);
        monitoringControllerStatus.setPermanentStatus(permanentStatus);

        for (Controller controller : controllers.getControllers()) {

            if (controller.getUrl().equals(volatileStatus.getUrl())) {
                if ((volatileStatus.getClusterNodeState() == null) || (volatileStatus.getClusterNodeState()
                        .get_text() == ClusterNodeStateText.active)) {
                    monitoringControllerStatus.setActive(controller);
                }
            } else {
                if ((controller.getClusterNodeState() != null) && (controller.getClusterNodeState().get_text() == ClusterNodeStateText.inactive)) {
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
        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(readAgentsV);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/agents", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MonitoringErrorResponse monitoringErrorResponse = JobHelper.OBJECT_MAPPER.readValue(apiResponse.getResponseBody(),
                    MonitoringErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s", apiResponse.getStatusCode(), monitoringErrorResponse.getError()
                    .getMessage(), monitoringErrorResponse.getMessage()));
        }

        if (apiExecutor.getLogger().isDebugEnabled()) {
            apiExecutor.getLogger().debug(body);
            apiExecutor.getLogger().debug("answer=%s", answer);
        }
        AgentsV agentsV = JobHelper.OBJECT_MAPPER.readValue(answer, AgentsV.class);
        return agentsV.getAgents();

    }

    public OrdersSummary getJS7OrderSnapshot(String accessToken, String controllerId) throws JsonProcessingException, SOSException {
        OrdersFilterV ordersFilterV = new OrdersFilterV();
        ordersFilterV.setControllerId(controllerId);
        ordersFilterV.setDateTo("1d");
        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(ordersFilterV);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/orders/overview/snapshot", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MonitoringErrorResponse monitoringErrorResponse = JobHelper.OBJECT_MAPPER.readValue(apiResponse.getResponseBody(),
                    MonitoringErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s", apiResponse.getStatusCode(), monitoringErrorResponse.getError()
                    .getMessage(), monitoringErrorResponse.getMessage()));
        }

        if (apiExecutor.getLogger().isDebugEnabled()) {
            apiExecutor.getLogger().debug(body);
            apiExecutor.getLogger().debug("answer=%s", answer);
        }
        OrdersSnapshot ordersSnapshot = JobHelper.OBJECT_MAPPER.readValue(answer, OrdersSnapshot.class);
        return ordersSnapshot.getOrders();
    }

    public OrdersHistoricSummary getJS7OrderSummary(String accessToken, String controllerId) throws JsonProcessingException, SOSException {
        OrdersFilterV ordersFilterV = new OrdersFilterV();
        ordersFilterV.setControllerId(controllerId);
        String body = JobHelper.OBJECT_MAPPER.writeValueAsString(ordersFilterV);

        ApiResponse apiResponse = apiExecutor.post(accessToken, "/joc/api/orders/overview/snapshot", body);
        String answer = null;
        if (apiResponse.getStatusCode() == 200) {
            answer = apiResponse.getResponseBody();
        } else {
            MonitoringErrorResponse monitoringErrorResponse = JobHelper.OBJECT_MAPPER.readValue(apiResponse.getResponseBody(),
                    MonitoringErrorResponse.class);
            throw new SOSException(String.format("Status Code: %s : Error: %s %s", apiResponse.getStatusCode(), monitoringErrorResponse.getError()
                    .getMessage(), monitoringErrorResponse.getMessage()));
        }

        if (apiExecutor.getLogger().isDebugEnabled()) {
            apiExecutor.getLogger().debug(body);
            apiExecutor.getLogger().debug("answer=%s", answer);
        }
        OrdersOverView ordersOverView = JobHelper.OBJECT_MAPPER.readValue(answer, OrdersOverView.class);
        return ordersOverView.getOrders();
    }

}
