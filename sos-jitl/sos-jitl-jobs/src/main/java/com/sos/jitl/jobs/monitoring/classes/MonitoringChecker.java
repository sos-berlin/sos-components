package com.sos.jitl.jobs.monitoring.classes;

import java.util.List;

import com.sos.commons.exception.SOSException;
import com.sos.jitl.jobs.common.Globals;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.joc.model.agent.AgentV;
import com.sos.joc.model.agent.SubagentV;
import com.sos.joc.model.controller.Controller;
import com.sos.joc.model.controller.Role;
import com.sos.joc.model.jitl.monitoring.MonitoringStatus;
import com.sos.joc.model.joc.Cockpit;
import com.sos.joc.model.joc.ControllerConnectionState;
import com.sos.joc.model.order.OrdersSummary;

public class MonitoringChecker {

    private JobLogger logger;
    private Integer count = 0;

    public MonitoringChecker(JobLogger logger) {
        super();
        this.logger = logger;
    }

    private MonitoringCheckReturn checkControllerStatus(Controller controller) {
        MonitoringCheckReturn monitoringCheckReturn = new MonitoringCheckReturn();
        monitoringCheckReturn.setSuccess(true);

        if (controller.getConnectionState() == null) {
            count += 1;
            monitoringCheckReturn.setErrorMessage(logger, "check-ControllerStatus: empty connection status");
        } else {
            if (controller.getConnectionState().getSeverity() != 0) {
                count += 1;
                monitoringCheckReturn.setErrorMessage(logger, "check-ControllerStatus: unhealthy connection status: " + controller
                        .getConnectionState().get_text() + ":" + controller.getConnectionState().getSeverity());
            }
        }

        if (controller.getComponentState() == null) {
            count += 1;
            monitoringCheckReturn.setErrorMessage(logger, "check-ControllerStatus: empty component status");
        } else {
            if (controller.getComponentState().getSeverity() != 0) {
                count += 1;
                monitoringCheckReturn.setErrorMessage(logger, "check-ControllerStatus: unhealthy component status: " + controller.getComponentState()
                        .get_text() + ":" + controller.getComponentState().getSeverity());
            }
        }

        if (controller.getRole() == null) {
            count += 1;
            monitoringCheckReturn.setErrorMessage(logger, "check-ControllerStatus: Controller not assigned a role");
        }

        boolean isCluster = (controller.getRole() != null && controller.getRole() != Role.STANDALONE);

        if (isCluster) {
            if (controller.getClusterNodeState() == null) {
                count += 1;
                monitoringCheckReturn.setErrorMessage(logger, "check-ControllerStatus: empty cluster node state");
            } else {
                if (controller.getClusterNodeState().getSeverity() != 0) {
                    count += 1;
                    monitoringCheckReturn.setErrorMessage(logger, "check-ControllerStatus: unhealthy cluster node state " + controller
                            .getClusterNodeState().get_text() + ":" + controller.getClusterNodeState().getSeverity());
                }
            }

            if (!controller.getIsCoupled()) {
                count += 1;
                monitoringCheckReturn.setErrorMessage(logger, "check-ControllerStatus: cluster not coupled");
            }
        }

        return monitoringCheckReturn;

    }

    private MonitoringCheckReturn checkAgentStatus(List<AgentV> listOfAgents) {
        MonitoringCheckReturn monitoringCheckReturn = new MonitoringCheckReturn();
        monitoringCheckReturn.setSuccess(true);

        for (AgentV agentV : listOfAgents) {

            if (!agentV.getDisabled()) {
                if (agentV.getState() == null && agentV.getHealthState() == null) {
                    count += 1;
                    monitoringCheckReturn.setErrorMessage(logger, "check-AgentStatus: empty health state");
                } else {
                    if (agentV.getHealthState() != null && agentV.getHealthState().getSeverity() != 0) {
                        count += 1;
                        monitoringCheckReturn.setErrorMessage(logger, "check-AgentStatus: unhealthy Agent health status " + agentV.getHealthState()
                                .get_text() + ", severity " + agentV.getHealthState().getSeverity() + " for Agent ID " + agentV.getAgentId()
                                + ", Agent Name " + agentV.getAgentName() + ":" + agentV.getErrorMessage());
                    }

                    if (agentV.getState() != null) {
                        if (agentV.getState().getSeverity() != 0) {
                            count += 1;
                            monitoringCheckReturn.setErrorMessage(logger, "check-AgentStatus: unhealthy Agent status " + agentV.getState().get_text()
                                    + ", severity " + agentV.getState().getSeverity() + " for Agent ID " + agentV.getAgentId() + ", Agent Name "
                                    + agentV.getAgentName() + ":" + agentV.getErrorMessage());
                        }
                    } else {
                        if (agentV.getSubagents() == null || agentV.getSubagents().size() == 0) {
                            count += 1;
                            monitoringCheckReturn.setErrorMessage(logger, "check-AgentStatus: empty agent status and could not find subagents");
                        } else {
                            checkSubAgentStatus(agentV.getSubagents());
                        }
                    }

                }
            }
        }
        return monitoringCheckReturn;
    }

    private MonitoringCheckReturn checkSubAgentStatus(List<SubagentV> listOfSubAgents) {
        MonitoringCheckReturn monitoringCheckReturn = new MonitoringCheckReturn();
        monitoringCheckReturn.setSuccess(true);

        for (SubagentV subagentV : listOfSubAgents) {

            if (!subagentV.getDisabled()) {
                if (subagentV.getState() == null) {
                    count += 1;
                    monitoringCheckReturn.setErrorMessage(logger, "check-AgentStatus: empty subagent status");
                } else {
                    if (subagentV.getState().getSeverity() != 0) {
                        count += 1;
                        monitoringCheckReturn.setErrorMessage(logger, "check-AgentStatus: unhealthy Agent status " + subagentV.getState().get_text()
                                + ", severity " + subagentV.getState().getSeverity() + " for Agent ID " + subagentV.getAgentId() + ", Agent Name "
                                + subagentV.getAgentName() + ":" + subagentV.getErrorMessage());
                    }
                }
            }
        }
        return monitoringCheckReturn;

    }

    private MonitoringCheckReturn checkOrderFailedStatus(OrdersSummary ordersSummary, MonitoringParameters monitoringParameters) {
        MonitoringCheckReturn monitoringCheckReturn = new MonitoringCheckReturn();
        monitoringCheckReturn.setSuccess(true);

        if (ordersSummary == null) {
            count += 1;
            monitoringCheckReturn.setErrorMessage(logger, "check-checkOrderFailedStatus: empty OrdersSummary");
        } else {
            if (ordersSummary.getFailed() > monitoringParameters.getMaxFailedOrders() && monitoringParameters.getMaxFailedOrders() > -1) {
                count += 1;
                monitoringCheckReturn.setErrorMessage(logger, "checkOrderFailedStatus: unhealthy orders status " + ordersSummary.getFailed()
                        + " failed orders ");
            }

        }
        return monitoringCheckReturn;

    }

    private MonitoringCheckReturn checkJOCStatus(Cockpit cockpit) {
        MonitoringCheckReturn monitoringCheckReturn = new MonitoringCheckReturn();
        monitoringCheckReturn.setSuccess(true);

        if (cockpit.getConnectionState() == null) {
            count += 1;
            monitoringCheckReturn.setErrorMessage(logger, "check-JOCStatus: empty connection status");
        } else {
            if (cockpit.getConnectionState().getSeverity() != 0) {
                count += 1;
                monitoringCheckReturn.setErrorMessage(logger, "check-JOCStatus: unhealthy connection status " + cockpit.getConnectionState()
                        .get_text() + ":" + cockpit.getConnectionState().getSeverity());
            }
        }

        if (cockpit.getComponentState() == null) {
            count += 1;
            monitoringCheckReturn.setErrorMessage(logger, "check-JOCStatus: empty component status");
        } else {
            if (cockpit.getComponentState().getSeverity() != 0) {
                count += 1;
                monitoringCheckReturn.setErrorMessage(logger, "check-JOCStatus: unhealthy component status " + cockpit.getComponentState().get_text()
                        + ":" + cockpit.getComponentState().getSeverity());
            }
        }

        boolean isCluster = (cockpit.getClusterNodeState() != null);

        if (isCluster) {
            if (cockpit.getClusterNodeState().getSeverity() != 0) {
                count += 1;
                monitoringCheckReturn.setErrorMessage(logger, "check-JOCStatus: unhealthy cluster node state " + cockpit.getClusterNodeState()
                        .get_text() + ":" + cockpit.getClusterNodeState().getSeverity());
            }
        }

        if (cockpit.getControllerConnectionStates() != null) {
            for (ControllerConnectionState controllerConnectionState : cockpit.getControllerConnectionStates()) {
                if (controllerConnectionState.getState().getSeverity() != 0) {
                    count += 1;
                    monitoringCheckReturn.setErrorMessage(logger, "check-JOCStatus: unhealthy Controller connection status controller"
                            + controllerConnectionState.getState().get_text() + " for role " + controllerConnectionState.getRole() + ":"
                            + controllerConnectionState.getState().getSeverity());
                }
            }
        }

        return monitoringCheckReturn;
    }

    public MonitoringCheckReturn doCheck(MonitoringStatus monitoringStatus, MonitoringParameters monitoringParameters, String from)
            throws SOSException {

        MonitoringCheckReturn monitoringCheckReturn = new MonitoringCheckReturn();
        monitoringCheckReturn.setSuccess(true);

        if (monitoringParameters.getMonitorFileReportDate() == null) {
            throw new SOSException("sosMonitor: monitorReportDate is null");
        }

        try {
            if (monitoringStatus.getControllerStatus() == null || monitoringStatus.getControllerStatus().getVolatileStatus() == null) {
                count += 1;
                monitoringCheckReturn.setErrorMessage(logger, "sosMonitor: empty Controller status");
                monitoringCheckReturn.setSuccess(false);
            } else {
                MonitoringCheckReturn _monitoringCheckReturn = new MonitoringCheckReturn();
                _monitoringCheckReturn = this.checkControllerStatus(monitoringStatus.getControllerStatus().getVolatileStatus());
                monitoringCheckReturn.setSuccess(monitoringCheckReturn.isSuccess() && _monitoringCheckReturn.isSuccess());
                monitoringCheckReturn.onErrorSetMessage(_monitoringCheckReturn);
            }
            {
                if (monitoringStatus.getAgentStatus() == null || monitoringStatus.getAgentStatus().size() == 0) {
                    count += 1;
                    monitoringCheckReturn.setErrorMessage(logger, "sosMonitor: empty Agent status");
                    monitoringCheckReturn.setSuccess(false);
                } else {
                    MonitoringCheckReturn _monitoringCheckReturn = new MonitoringCheckReturn();
                    _monitoringCheckReturn = this.checkAgentStatus(monitoringStatus.getAgentStatus());
                    monitoringCheckReturn.setSuccess(monitoringCheckReturn.isSuccess() && _monitoringCheckReturn.isSuccess());
                    monitoringCheckReturn.onErrorSetMessage(_monitoringCheckReturn);
                }
            }

            if (monitoringStatus.getJocStatus() == null) {
                count += 1;
                monitoringCheckReturn.setErrorMessage(logger, "sosMonitor: empty JOC Cockpit status");
                monitoringCheckReturn.setSuccess(false);
            } else {
                if (monitoringStatus.getJocStatus().getActive() != null) {
                    MonitoringCheckReturn _monitoringCheckReturn = new MonitoringCheckReturn();
                    _monitoringCheckReturn = this.checkJOCStatus(monitoringStatus.getJocStatus().getActive());
                    monitoringCheckReturn.setSuccess(monitoringCheckReturn.isSuccess() && _monitoringCheckReturn.isSuccess());
                    monitoringCheckReturn.onErrorSetMessage(_monitoringCheckReturn);
                } else {
                    for (Cockpit cockpit : monitoringStatus.getJocStatus().getPassive()) {
                        MonitoringCheckReturn _monitoringCheckReturn = new MonitoringCheckReturn();
                        _monitoringCheckReturn = this.checkJOCStatus(cockpit);
                        monitoringCheckReturn.setSuccess(monitoringCheckReturn.isSuccess() && _monitoringCheckReturn.isSuccess());
                        monitoringCheckReturn.onErrorSetMessage(_monitoringCheckReturn);
                    }
                }
            }

            MonitoringCheckReturn _monitoringCheckReturn = new MonitoringCheckReturn();
            _monitoringCheckReturn = this.checkOrderFailedStatus(monitoringStatus.getOrderSnapshot(), monitoringParameters);
            monitoringCheckReturn.setSuccess(monitoringCheckReturn.isSuccess() && _monitoringCheckReturn.isSuccess());
            monitoringCheckReturn.onErrorSetMessage(_monitoringCheckReturn);

            if (from == null) {
                from = "";
            }
            if (count == 0) {
                monitoringCheckReturn.setSubject("JS7 Monitor: Notice from: " + from + " at: " + monitoringParameters.getMonitorSubjectReportDate());
            } else {
                monitoringCheckReturn.setSubject("JS7 Monitor: Alert from: " + from + " at: " + monitoringParameters.getMonitorSubjectReportDate());
            }
            monitoringCheckReturn.setBody(monitoringCheckReturn.getSubject());
            monitoringCheckReturn.setCount(count);

            return monitoringCheckReturn;

        } catch (Exception e) {
            Globals.error(logger, "", e);
            throw e;
        }

    }
}
