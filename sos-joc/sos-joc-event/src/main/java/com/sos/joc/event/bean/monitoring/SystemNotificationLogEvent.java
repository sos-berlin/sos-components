package com.sos.joc.event.bean.monitoring;

import java.time.Instant;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SystemNotificationLogEvent extends MonitoringEvent {

    /*
     * long epochMillis instead Instant object?
     * product: Enum of CONTROLLER, AGENT
     * clusterId: controllerId or agentId
     * instanceId: switch(product) { case CONTROLLER: Primary or Backup, case AGENT: subagentId }
     * For standalone Agent: agentId == subagentId
     */
    public SystemNotificationLogEvent(String host, String product, String clusterId, String instanceId, String role, String level, Instant instant,
            String loggerName, String message, String stacktrace) {
        super(SystemNotificationLogEvent.class.getSimpleName(), null, null);
        putVariable("host", host);
        putVariable("product", product);
        putVariable("clusterId", clusterId);
        putVariable("instanceId", instanceId);
        putVariable("role", role);
        putVariable("level", level);
        putVariable("instant", instant);
        putVariable("loggerName", loggerName);
        putVariable("message", message);
        putVariable("stacktrace", stacktrace);
    }
    
    @JsonIgnore
    public String getHost() {
        return (String) getVariables().get("host");
    }

    @JsonIgnore
    public String getLevel() {
        return (String) getVariables().get("level");
    }

    @JsonIgnore
    public Instant getInstant() {
        return (Instant) getVariables().get("instant");
    }

    @JsonIgnore
    public String getRole() {
        return (String) getVariables().get("role");
    }

    @JsonIgnore
    public String getLoggerName() {
        return (String) getVariables().get("loggerName");
    }

    @JsonIgnore
    public String getProduct() {
        return (String) getVariables().get("product");
    }

    @JsonIgnore
    public String getClusterId() {
        return (String) getVariables().get("clusterId");
    }

    @JsonIgnore
    public String getInstanceId() {
        return (String) getVariables().get("instanceId");
    }

    @JsonIgnore
    public String getMessage() {
        return (String) getVariables().get("message");
    }

    @JsonIgnore
    public String getStacktrace() {
        return (String) getVariables().get("stacktrace");
    }
    
    @JsonIgnore
    public String getNotifier() {
        String product = getProduct();
        String instanceId = getInstanceId();
        String clusterId = getClusterId();
        String role = getRole();
        if ("AGENT".equals(product)) {
            // clusterId: agentId ; not yet supported
            // instanceId: subagentId
            // role: Primary or Backup
            if (instanceId == null || instanceId.isBlank()) {
                return "";
            } else {
//                if (clusterId != null && !clusterId.isBlank() && !clusterId.equals(instanceId)) {
//                    return instanceId + " (" + clusterId + ")";
//                } else {
//                    return instanceId;
//                }
                if (role == null || role.isBlank()) {
                    return instanceId;
                } else {
                    return instanceId + "(" + role + ")";
                }
            }
        } else if ("CONTROLLER".equals(product)) {
            // clusterId: controllerId
            // instanceId (or role): Primary or Backup
            if (clusterId == null || clusterId.isBlank()) {
                return "";
            } else {
                // only "Backup" is added because standalone Controller is "Primary" too. That's confusing.
                if (instanceId == null || instanceId.isBlank() || "Primary".equalsIgnoreCase(instanceId)) {
                    return clusterId;
                } else {
                    return clusterId + "(" + instanceId + ")";
                }
            }
        }
        return "";
    }

    @JsonIgnore
    public String toString() {
        String stacktrace = getStacktrace() != null ? getStacktrace() : "";
        String message = getMessage() != null ? getMessage() : "";
        return String.format("host:%s, product:%s, clusterId:%s, instanceId:%s, role:%s, level:%s, timestamp:%s, logger:%s, message:%s, thrown:%s",
                getHost(), getProduct(), getClusterId(), getInstanceId(), getRole(), getLevel(), getInstant().toString(), getLoggerName(), message,
                stacktrace);
    }
}
