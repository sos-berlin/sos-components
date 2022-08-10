package com.sos.jitl.jobs.monitoring.classes;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import com.sos.commons.exception.SOSException;
import com.sos.jitl.jobs.common.JobLogger;
import com.sos.joc.model.agent.AgentV;
import com.sos.joc.model.controller.Controller;
import com.sos.joc.model.controller.Role;
import com.sos.joc.model.jitl.monitoring.MonitoringStatus;
import com.sos.joc.model.joc.Cockpit;
import com.sos.joc.model.joc.ControllerConnectionState;

public class MonitoringChecker {

    private JobLogger logger;

    public MonitoringChecker(JobLogger logger) {
        super();
        this.logger = logger;
    }

    private MonitoringCheckReturn checkControllerStatus(Controller controller) {
        MonitoringCheckReturn monitoringCheckReturn = new MonitoringCheckReturn();
        monitoringCheckReturn.setSuccess(true);

        if (controller.getConnectionState() == null) {
            monitoringCheckReturn.setErrorMessage("check-ControllerStatus: empty connection status");
        } else {
            if (controller.getConnectionState().getSeverity() != 0) {
                monitoringCheckReturn.setErrorMessage("check-ControllerStatus: unhealthy connection status: " + controller.getConnectionState()
                        .get_text() + ":" + controller.getConnectionState().getSeverity());
            }
        }

        if (controller.getComponentState() == null) {
            monitoringCheckReturn.setErrorMessage("check-ControllerStatus: empty component status");
        } else {
            if (controller.getComponentState().getSeverity() != 0) {
                monitoringCheckReturn.setErrorMessage("check-ControllerStatus: unhealthy component status: " + controller.getComponentState()
                        .get_text() + ":" + controller.getComponentState().getSeverity());
            }
        }

        if (controller.getRole() == null) {
            monitoringCheckReturn.setErrorMessage("check-ControllerStatus: Controller not assigned a role");
        }

        boolean isCluster = (controller.getRole() != null && controller.getRole() != Role.STANDALONE);

        if (isCluster) {
            if (controller.getClusterNodeState() == null) {
                monitoringCheckReturn.setErrorMessage("check-ControllerStatus: empty cluster node state");
            } else {
                if (controller.getClusterNodeState().getSeverity() != 0) {
                    monitoringCheckReturn.setErrorMessage("check-ControllerStatus: unhealthy cluster node state " + controller.getClusterNodeState()
                            .get_text() + ":" + controller.getClusterNodeState().getSeverity());
                }
            }

            if (!controller.getIsCoupled()) {
                monitoringCheckReturn.setErrorMessage("check-ControllerStatus: cluster not coupled");
            }
        }

        return monitoringCheckReturn;

    }

    private MonitoringCheckReturn checkAgentStatus(List<AgentV> listOfAgents) {
        MonitoringCheckReturn monitoringCheckReturn = new MonitoringCheckReturn();
        monitoringCheckReturn.setSuccess(true);

        for (AgentV agentV : listOfAgents) {
            if (agentV.getState() == null) {
                monitoringCheckReturn.setErrorMessage(
                        "check-AgentStatus: unhealthy Agent status '$($status.state._text)', severity '$($status.state.severity)' for Agent ID '$($status.agentId)', Agent Name '$($status.agentName)': $($status.errorMessage)");
            } else {
                if (agentV.getState().getSeverity() != 0) {
                    monitoringCheckReturn.setErrorMessage("check-AgentStatus: unhealthy Agent status " + agentV.getState().get_text() + ", severity "
                            + agentV.getState().getSeverity() + " for Agent ID " + agentV.getAgentId() + ", Agent Name " + agentV.getAgentName() + ":"
                            + agentV.getErrorMessage());
                }
            }
        }
        return monitoringCheckReturn;
    }

    private MonitoringCheckReturn checkJOCStatus(Cockpit cockpit) {
        MonitoringCheckReturn monitoringCheckReturn = new MonitoringCheckReturn();
        monitoringCheckReturn.setSuccess(true);

        if (cockpit.getConnectionState() == null) {
            monitoringCheckReturn.setErrorMessage("check-JOCStatus: empty connection status");
        } else {
            if (cockpit.getConnectionState().getSeverity() != 0) {
                monitoringCheckReturn.setErrorMessage("check-JOCStatus: unhealthy connection status " + cockpit.getConnectionState().get_text() + ":"
                        + cockpit.getConnectionState().getSeverity());
            }
        }

        if (cockpit.getComponentState() == null) {
            monitoringCheckReturn.setErrorMessage("check-JOCStatus: empty component status");
        } else {
            if (cockpit.getComponentState().getSeverity() != 0) {
                monitoringCheckReturn.setErrorMessage("check-JOCStatus: unhealthy component status " + cockpit.getComponentState().get_text() + ":"
                        + cockpit.getComponentState().getSeverity());
            }
        }

        boolean isCluster = (cockpit.getClusterNodeState() != null);

        if (isCluster) {
            if (cockpit.getClusterNodeState().getSeverity() != 0) {
                monitoringCheckReturn.setErrorMessage("check-JOCStatus: unhealthy cluster node state " + cockpit.getClusterNodeState().get_text()
                        + ":" + cockpit.getClusterNodeState().getSeverity());
            }
        }

        if (cockpit.getControllerConnectionStates() == null) {
            for (ControllerConnectionState controllerConnectionState : cockpit.getControllerConnectionStates()) {
                if (controllerConnectionState.getState().getSeverity() != 0) {
                    monitoringCheckReturn.setErrorMessage("check-JOCStatus: unhealthy Controller connection status controller"
                            + controllerConnectionState.getState().get_text() + " for role " + controllerConnectionState.getRole() + ":"
                            + controllerConnectionState.getState().getSeverity());
                }
            }
        }

        return monitoringCheckReturn;
    }

    public MonitoringCheckReturn doCheck(MonitoringStatus monitoringStatus, String reportFilename, String monitorReportDate, String mailSmtpFrom)
            throws SOSException {

        MonitoringCheckReturn monitoringCheckReturn = new MonitoringCheckReturn();
        monitoringCheckReturn.setSuccess(true);

        if (!Files.exists(Paths.get(reportFilename))) {
            throw new SOSException("sosMonitor: report file missing: " + reportFilename);
        }

        if (monitorReportDate == null) {
            throw new SOSException("sosMonitor: monitorReportDate is null");
        }

        Globals.log(logger, "monitor report date: " + monitorReportDate);
        Globals.log(logger, "monitor report file: " + reportFilename);

        try {
            if (monitoringStatus.getControllerStatus() == null || monitoringStatus.getControllerStatus().getVolatileStatus() == null) {
                monitoringCheckReturn.setErrorMessage("sosMonitor: empty Controller status");
                monitoringCheckReturn.setSuccess(false);
            } else {
                MonitoringCheckReturn _monitoringCheckReturn = new MonitoringCheckReturn();
                _monitoringCheckReturn = this.checkControllerStatus(monitoringStatus.getControllerStatus().getVolatileStatus());
                monitoringCheckReturn.setSuccess(monitoringCheckReturn.isSuccess() && _monitoringCheckReturn.isSuccess());
                monitoringCheckReturn.onErrorSetMessage(_monitoringCheckReturn);
            }

            if (monitoringStatus.getAgentStatus() == null || monitoringStatus.getAgentStatus().size() == 0) {
                monitoringCheckReturn.setErrorMessage("sosMonitor: empty Agent status");
                monitoringCheckReturn.setSuccess(false);
            } else {
                MonitoringCheckReturn _monitoringCheckReturn = new MonitoringCheckReturn();
                _monitoringCheckReturn = this.checkAgentStatus(monitoringStatus.getAgentStatus());
                monitoringCheckReturn.setSuccess(monitoringCheckReturn.isSuccess() && _monitoringCheckReturn.isSuccess());
                monitoringCheckReturn.onErrorSetMessage(_monitoringCheckReturn);
            }

            if (monitoringStatus.getJocStatus() == null) {
                monitoringCheckReturn.setErrorMessage("sosMonitor: empty JOC Cockpit status");
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
            if (monitoringCheckReturn.isSuccess()) {
                monitoringCheckReturn.setSubject("JS7 Monitor: Notice from: " + mailSmtpFrom + " at: " + monitorReportDate);
                monitoringCheckReturn.setBody(monitoringCheckReturn.getSubject());
            } else {
                monitoringCheckReturn.setSubject("JS7 Monitor: Alert from: " + mailSmtpFrom + " at: " + monitorReportDate);
                monitoringCheckReturn.setBody(monitoringCheckReturn.getSubject() + "\n" + monitoringCheckReturn.getMessage());
            }
            return monitoringCheckReturn;

        } catch (Exception e) {
            Globals.error(logger, "", e);
            throw e;
        }

    }
}
