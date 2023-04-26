package com.sos.joc.history.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.controller.model.event.EventType;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;
import com.sos.joc.history.controller.proxy.fatevent.AFatEventOrderBase;
import com.sos.joc.history.controller.proxy.fatevent.AFatEventOrderLocks;
import com.sos.joc.history.controller.proxy.fatevent.AFatEventOrderNotice;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderCaught;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderCaught.FatEventOrderCaughtCause;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderMoved;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderPrompted;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderRetrying;
import com.sos.joc.history.controller.proxy.fatevent.FatForkedChild;
import com.sos.joc.history.controller.proxy.fatevent.FatInstruction;
import com.sos.joc.history.controller.proxy.fatevent.FatOutcome;
import com.sos.joc.model.history.order.OrderLogEntryLogLevel;
import com.sos.joc.model.history.order.caught.Caught;
import com.sos.joc.model.history.order.caught.CaughtCause;
import com.sos.joc.model.order.OrderStateText;

public class LogEntry {

    private OrderLogEntryLogLevel logLevel;
    private final EventType eventType;
    private final Date controllerDatetime;
    private final Date agentDatetime;
    private String orderId = ".";
    private Long historyOrderMainParentId = Long.valueOf(0);
    private Long historyOrderId = Long.valueOf(0);
    private Long historyOrderStepId = Long.valueOf(0);
    private String position;
    private String jobName = ".";
    private String agentTimezone = null;
    private String agentId = ".";
    private String agentName = ".";
    private String agentUri = ".";
    private String subagentClusterId = null;
    private String info;
    private Integer state;
    private boolean error;
    private String errorState;
    private String errorReason;
    private String errorCode;
    private String errorText;
    private Integer returnCode;
    private String returnMessage;
    private List<OrderLock> orderLocks;
    private AFatEventOrderNotice orderNotice;
    private Date delayedUntil;
    private Caught caught;
    private FatEventOrderMoved orderMoved;
    private FatInstruction instruction;
    private Variables arguments;
    private String question;
    private boolean isOrderStarted;

    public LogEntry(OrderLogEntryLogLevel level, EventType type, Date controllerDate, Date agentDate) {
        logLevel = level;
        eventType = type;
        controllerDatetime = controllerDate;
        agentDatetime = agentDate;
        isOrderStarted = true;
    }

    public void onOrder(CachedOrder co, String position) {
        onOrder(co, position, null);
    }

    public void onOrderBase(CachedOrder co, String position, AFatEventOrderBase eo) {
        onOrder(co, position, null);
        switch (eo.getType()) {
        case OrderRetrying:
            delayedUntil = ((FatEventOrderRetrying) eo).getDelayedUntil();
            break;
        case OrderCaught:
            caught = new Caught();
            caught.setCause(getCaughtCause(((FatEventOrderCaught) eo).getCause()));
            break;
        case OrderPrompted:
            question = ((FatEventOrderPrompted) eo).getQuestion();
            break;
        default:
            break;
        }
    }

    private CaughtCause getCaughtCause(FatEventOrderCaughtCause cause) {
        if (cause == null) {
            return CaughtCause.Unknown;
        }
        switch (cause) {
        case Retry:
            return CaughtCause.Retry;
        case TryInstruction:
            return CaughtCause.TryInstruction;
        default:
            return CaughtCause.Unknown;
        }
    }

    public void onOrder(CachedOrder co, String workflowPosition, List<FatForkedChild> childs) {
        orderId = co.getOrderId();
        historyOrderMainParentId = co.getMainParentId();
        historyOrderId = co.getId();
        position = getPosition(workflowPosition);
        info = co.getOrderId();
    }

    public void onNotStartedOrder(String orderId, String position) {
        this.orderId = orderId;
        this.historyOrderMainParentId = 0L;
        this.historyOrderId = 0L;
        this.position = getPosition(position);
        this.info = orderId;
        this.isOrderStarted = false;
    }

    private String getPosition(String position) {
        return SOSString.isEmpty(position) ? "0" : position;
    }

    public void onOrderLock(CachedOrder co, AFatEventOrderLocks eo) {
        onOrder(co, eo.getPosition(), null);
        orderLocks = eo.getOrderLocks();
    }

    public void onOrderNotice(CachedOrder co, AFatEventOrderNotice eo) {
        onOrder(co, eo.getPosition(), null);
        orderNotice = eo;
    }

    public void onOrderMoved(CachedOrder co, FatEventOrderMoved eo) {
        if (co == null) {
            onNotStartedOrder(eo.getOrderId(), eo.getPosition());
        } else {
            onOrder(co, eo.getPosition(), null);
        }
        orderMoved = eo;
    }

    public void setError(String state, CachedOrder co) {
        error = true;
        errorState = state == null ? null : state.toLowerCase();
        if (co.getLastStepError() != null) {
            errorReason = co.getLastStepError().getReason();
            errorCode = co.getLastStepError().getCode();
            errorText = co.getLastStepError().getText();
        }
    }

    public void setError(String state, FatOutcome outcome) {
        error = true;
        errorState = state == null ? null : state.toLowerCase();
        errorReason = outcome.getErrorReason();
        errorCode = outcome.getErrorCode();
        errorText = outcome.getErrorMessage();
    }

    public void onOrderJoined(CachedOrder co, FatOutcome outcome, String workflowPosition, List<String> childs) {
        orderId = co.getOrderId();
        historyOrderMainParentId = co.getMainParentId();
        historyOrderId = co.getId();
        position = workflowPosition;
        info = String.join(", ", childs);

        if (outcome != null) {
            returnCode = outcome.getReturnCode();
            if (outcome.isFailed()) {
                setError(OrderStateText.FAILED.value(), outcome);
                co.setLastStepError(this);
            }
        }
    }

    public void setError(CachedOrder co, FatOutcome outcome) {
        if (outcome.getReturnCode() == null && SOSString.isEmpty(outcome.getErrorMessage()) && co.getLastStepError() != null) {
            setError(OrderStateText.FAILED.value(), co);
            setReturnCode(co.getLastStepError().getReturnCode());
            if (outcome.getErrorReason() != null) {
                errorReason = outcome.getErrorReason();
            }
        } else {
            setError(OrderStateText.FAILED.value(), outcome);
        }
    }

    public void onOrderStep(CachedOrderStep cos) {
        onOrderStep(cos, null);
    }

    public void onOrderStep(CachedOrderStep cos, String entryInfo) {
        orderId = cos.getOrderId();
        historyOrderMainParentId = cos.getHistoryOrderMainParentId();
        historyOrderId = cos.getHistoryOrderId();
        historyOrderStepId = cos.getId();
        position = cos.getWorkflowPosition();
        jobName = cos.getJobName();
        agentTimezone = cos.getAgentTimezone();
        agentId = cos.getAgentId();
        agentName = cos.getAgentName();
        agentUri = cos.getAgentUri();
        subagentClusterId = cos.getSubagentClusterId();
        StringBuilder sb;
        switch (eventType) {
        case OrderProcessingStarted:
            String add = subagentClusterId == null ? "" : ", subagentClusterId=" + subagentClusterId;
            info = String.format("[Start] Job=%s, Agent (url=%s, id=%s, name=%s%s)", jobName, agentUri, agentId, agentName, add);
            return;
        case OrderProcessed:
            returnCode = cos.getReturnCode();
            sb = new StringBuilder("[End]");
            if (error) {
                sb.append(" [Error]");
            } else {
                sb.append(" [Success]");
            }
            if (returnCode != null) {
                sb.append(" returnCode=").append(returnCode);
            }
            if (error) {
                List<String> errorInfo = new ArrayList<String>();
                if (errorState != null) {
                    errorInfo.add("errorState=" + errorState);
                }
                if (errorCode != null) {
                    errorInfo.add("code=" + errorCode);
                }
                if (errorReason != null) {
                    errorInfo.add("reason=" + errorReason);
                }
                if (errorText != null) {
                    errorInfo.add("msg=" + errorText);
                }
                if (errorInfo.size() > 0) {
                    sb.append(", ").append(String.join(", ", errorInfo));
                }
            }
            info = sb.toString();
            return;
        default:
            info = entryInfo;
            break;
        }

    }

    public void setLogLevel(OrderLogEntryLogLevel val) {
        logLevel = val;
    }

    public OrderLogEntryLogLevel getLogLevel() {
        return logLevel;
    }

    public EventType getEventType() {
        return eventType;
    }

    public String getOrderId() {
        return orderId;
    }

    public Long getHistoryOrderMainParentId() {
        return historyOrderMainParentId;
    }

    public Long getHistoryOrderId() {
        return historyOrderId;
    }

    public Long getHistoryOrderStepId() {
        return historyOrderStepId;
    }

    public String getPosition() {
        return position;
    }

    public void setJobName(String val) {
        jobName = val;
    }

    public String getJobName() {
        return jobName;
    }

    public String getAgentTimezone() {
        return agentTimezone;
    }

    public void setAgentTimezone(String val) {
        agentTimezone = val;
    }

    public String getAgentUri() {
        return agentUri;
    }

    public String getAgentId() {
        return agentId;
    }

    public String getAgentName() {
        return agentName;
    }

    public String getSubagentClusterId() {
        return subagentClusterId;
    }

    public Date getControllerDatetime() {
        return controllerDatetime;
    }

    public Date getAgentDatetime() {
        return agentDatetime;
    }

    public String getInfo() {
        return info;
    }

    public void setState(Integer val) {
        state = val;
    }

    public Integer getState() {
        return state;
    }

    public boolean isError() {
        return error;
    }

    public String getErrorState() {
        return errorState;
    }

    public String getErrorReason() {
        return errorReason;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorText(String val) {
        if (SOSString.isEmpty(val)) {
            val = null;
        }
        errorText = val;
    }

    public String getErrorText() {
        return errorText;
    }

    public void setReturnCode(Integer val) {
        returnCode = val;
    }

    public boolean isNullOrDefaultReturnCode() {
        return returnCode == null || returnCode.equals(0);
    }

    public Integer getReturnCode() {
        return returnCode;
    }

    public String getReturnMessage() {
        return returnMessage;
    }

    public void setReturnMessage(String val) {
        returnMessage = val;
    }

    public List<OrderLock> getOrderLocks() {
        return orderLocks;
    }

    public AFatEventOrderNotice getOrderNotice() {
        return orderNotice;
    }

    public Date getDelayedUntil() {
        return delayedUntil;
    }

    public Caught getCaught() {
        return caught;
    }

    public FatEventOrderMoved getOrderMoved() {
        return orderMoved;
    }

    public void setArguments(Variables val) {
        arguments = val;
    }

    public Variables getArguments() {
        return arguments;
    }

    public boolean isOrderStarted() {
        return isOrderStarted;
    }

    public void setInstruction(FatInstruction val) {
        instruction = val;
    }

    public FatInstruction getInstruction() {
        return instruction;
    }

    public String getQuestion() {
        return question;
    }
}
