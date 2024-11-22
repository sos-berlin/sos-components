package com.sos.joc.history.helper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.sos.commons.util.SOSString;
import com.sos.controller.model.event.EventType;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.history.controller.model.HistoryModel;
import com.sos.joc.history.controller.proxy.HistoryEventEntry.HistoryOrder.OrderLock;
import com.sos.joc.history.controller.proxy.fatevent.AFatEventOrderBase;
import com.sos.joc.history.controller.proxy.fatevent.AFatEventOrderLocks;
import com.sos.joc.history.controller.proxy.fatevent.AFatEventOrderNotice;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderAttached;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderCaught;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderCaught.FatEventOrderCaughtCause;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderCyclingPrepared;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderMoved;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderOrderAdded;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderPrompted;
import com.sos.joc.history.controller.proxy.fatevent.FatEventOrderRetrying;
import com.sos.joc.history.controller.proxy.fatevent.FatInstruction;
import com.sos.joc.history.controller.proxy.fatevent.FatOutcome;
import com.sos.joc.history.controller.proxy.fatevent.FatPosition;
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
    private String positionOriginalIfDiff;
    private String jobName = ".";
    private String label;
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
    private FatEventOrderAttached orderAttached;
    private FatEventOrderCyclingPrepared orderCyclingPrepared;
    private FatEventOrderOrderAdded orderOrderAdded;
    private FatInstruction instruction;
    private Variables arguments;
    private String question;
    private boolean isOrderStarted;

    public LogEntry(OrderLogEntryLogLevel level, EventType type, Date controllerDate, Date agentDate) {
        this.logLevel = level;
        this.eventType = type;
        this.controllerDatetime = controllerDate;
        this.agentDatetime = agentDate;
        this.isOrderStarted = true;
    }

    public void onOrder(CachedOrder co, String workflowPosition, FatPosition position) {
        this.orderId = co.getOrderId();
        this.historyOrderMainParentId = co.getMainParentId();
        this.historyOrderId = co.getId();
        this.position = workflowPosition;
        this.positionOriginalIfDiff = position == null ? null : position.getOrigIfDiff();
        this.info = co.getOrderId();
    }

    public void onOrder(CachedOrder co, FatPosition position) {
        this.orderId = co.getOrderId();
        this.historyOrderMainParentId = co.getMainParentId();
        this.historyOrderId = co.getId();
        if (position == null) {
            this.position = co.getWorkflowPosition();
            this.positionOriginalIfDiff = null;
        } else {
            this.position = position.getValue();
            this.positionOriginalIfDiff = position.getOrigIfDiff();
        }
        this.info = co.getOrderId();
    }

    public void onOrderBase(CachedOrder co, AFatEventOrderBase eo) {
        onOrder(co, eo.getPosition());
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
        case OrderCyclingPrepared:
            orderCyclingPrepared = (FatEventOrderCyclingPrepared) eo;
            break;
        case OrderOrderAdded:
            orderOrderAdded = (FatEventOrderOrderAdded) eo;
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

    public void onNotStartedOrder(String orderId, FatPosition position) {
        this.orderId = orderId;
        this.historyOrderMainParentId = 0L;
        this.historyOrderId = 0L;
        this.position = HistoryModel.getRequiredPosition(position);
        this.positionOriginalIfDiff = position == null ? null : position.getOrigIfDiff();
        this.info = orderId;
        this.isOrderStarted = false;
    }

    public void onOrderLock(CachedOrder co, AFatEventOrderLocks eo) {
        onOrder(co, eo.getPosition());
        this.orderLocks = eo.getOrderLocks();
    }

    public void onOrderNotice(CachedOrder co, AFatEventOrderNotice eo) {
        onOrder(co, eo.getPosition());
        this.orderNotice = eo;
    }

    public void onOrderMoved(CachedOrder co, FatEventOrderMoved eo) {
        if (co == null) {
            onNotStartedOrder(eo.getOrderId(), eo.getPosition());
        } else {
            onOrder(co, eo.getPosition());
        }
        this.orderMoved = eo;
    }

    public void onOrderAttached(CachedOrder co, FatEventOrderAttached eo) {
        if (co == null) {
            onNotStartedOrder(eo.getOrderId(), eo.getPosition());
        } else {
            onOrder(co, eo.getPosition());
        }
        this.orderAttached = eo;
    }

    public void setError(String state, CachedOrder co) {
        this.error = true;
        this.errorState = state == null ? null : state.toLowerCase();
        if (co.getLastStepError() != null) {
            this.errorReason = co.getLastStepError().getReason();
            this.errorCode = co.getLastStepError().getCode();
            this.errorText = co.getLastStepError().getText();
        }
    }

    public void setError(String state, FatOutcome outcome) {
        this.error = true;
        this.errorState = state == null ? null : state.toLowerCase();
        this.errorReason = outcome.getErrorReason();
        this.errorCode = outcome.getErrorCode();
        this.errorText = outcome.getErrorMessage();
    }

    public void onOrderJoined(CachedOrder co, FatOutcome outcome, FatPosition position, List<String> childs) {
        this.orderId = co.getOrderId();
        this.historyOrderMainParentId = co.getMainParentId();
        this.historyOrderId = co.getId();
        this.position = HistoryModel.getRequiredPosition(position);
        this.positionOriginalIfDiff = position == null ? null : position.getOrigIfDiff();
        this.info = String.join(", ", childs);

        if (outcome != null) {
            this.returnCode = outcome.getReturnCode();
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
                this.errorReason = outcome.getErrorReason();
            }
        } else {
            setError(OrderStateText.FAILED.value(), outcome);
        }
    }

    public void onOrderStep(CachedOrderStep cos, FatPosition position) {
        onOrderStep(cos, position, null);
    }

    public void onOrderStep(CachedOrderStep cos, FatPosition position, String entryInfo) {
        this.orderId = cos.getOrderId();
        this.historyOrderMainParentId = cos.getHistoryOrderMainParentId();
        this.historyOrderId = cos.getHistoryOrderId();
        this.historyOrderStepId = cos.getId();
        this.position = cos.getWorkflowPosition();
        this.positionOriginalIfDiff = position == null ? null : position.getOrigIfDiff();
        this.jobName = cos.getJobName();
        this.label = cos.getJobLabel();
        this.agentTimezone = cos.getAgentTimezone();
        this.agentId = cos.getAgentId();
        this.agentName = cos.getAgentName();
        this.agentUri = cos.getAgentUri();
        this.subagentClusterId = cos.getSubagentClusterId();
        StringBuilder sb;
        switch (eventType) {
        case OrderProcessingStarted:
            String agentAdd = subagentClusterId == null ? "" : ", subagentClusterId=" + subagentClusterId;
            this.info = String.format("[Start] Job=%s, label=%s, Agent(url=%s, id=%s, name=%s%s)", jobName, label, agentUri, agentId, agentName,
                    agentAdd);
            return;
        case OrderProcessed:
            this.returnCode = cos.getReturnCode();
            sb = new StringBuilder("[End]");
            if (this.error) {
                sb.append(" [Error]");
            } else {
                sb.append(" [Success]");
            }
            if (this.returnCode != null) {
                sb.append(" returnCode=").append(this.returnCode);
            }
            if (this.error) {
                List<String> errorInfo = new ArrayList<String>();
                if (this.errorState != null) {
                    errorInfo.add("errorState=" + this.errorState);
                }
                if (this.errorCode != null) {
                    errorInfo.add("code=" + this.errorCode);
                }
                if (this.errorReason != null) {
                    errorInfo.add("reason=" + this.errorReason);
                }
                if (this.errorText != null) {
                    errorInfo.add("msg=" + this.errorText);
                }
                if (errorInfo.size() > 0) {
                    sb.append(", ").append(String.join(", ", errorInfo));
                }
            }
            this.info = sb.toString();
            return;
        default:
            this.info = entryInfo;
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

    public String getPositionOriginalIfDiff() {
        return positionOriginalIfDiff;
    }

    public void setJobName(String val) {
        jobName = val;
    }

    public String getJobName() {
        return jobName;
    }

    public String getLabel() {
        return label;
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

    public FatEventOrderAttached getOrderAttached() {
        return orderAttached;
    }

    public FatEventOrderCyclingPrepared getOrderCyclingPrepared() {
        return orderCyclingPrepared;
    }

    public FatEventOrderOrderAdded getOrderOrderAdded() {
        return orderOrderAdded;
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
