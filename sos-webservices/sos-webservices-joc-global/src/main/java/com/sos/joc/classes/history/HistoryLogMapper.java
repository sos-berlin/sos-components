package com.sos.joc.classes.history;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.inventory.model.common.Variables;
import com.sos.joc.model.history.order.OrderLogEntry;
import com.sos.joc.model.history.order.OrderLogEntryInstruction;
import com.sos.joc.model.history.order.added.OrderAdded;
import com.sos.joc.model.history.order.notice.BaseNotice;

public class HistoryLogMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryLogMapper.class);

    // see HistoryModel.createOrderLogEntry
    public static String toString(OrderLogEntry entry) {
        if (entry == null) {
            return "";
        }
        List<String> info = new ArrayList<String>();
        if (!SOSString.isEmpty(entry.getOrderId())) {
            info.add("id=" + entry.getOrderId());
        }
        if (!SOSString.isEmpty(entry.getPosition())) {
            info.add("pos=" + entry.getPosition());
        }
        if (!SOSString.isEmpty(entry.getJob())) {
            info.add("Job=" + entry.getJob());
        }
        if (entry.getAgentDatetime() != null) {
            info.add(getAgent(entry));
        }
        if (entry.getError() != null) {
            info.add(getError(entry));
        }
        if (entry.getReturnCode() != null) {
            info.add("returnCode=" + entry.getReturnCode());
        }
        if (entry.getReturnMessage() != null) {
            info.add("returnMessage=" + entry.getReturnMessage());
        }
        if (entry.getArguments() != null && entry.getArguments().getAdditionalProperties().size() > 0) {
            info.add("arguments(" + toString(entry.getArguments()) + ")");
        } else if (entry.getReturnValues() != null && entry.getReturnValues().getAdditionalProperties().size() > 0) {
            info.add("returnValues(" + toString(entry.getReturnValues()) + ")");
        }
        if (entry.getLocks() != null && entry.getLocks().size() > 0) {
            info.add(getLocks(entry));
        } else if (entry.getConsumeNotices() != null) {
            info.add(getConsumeNotices(entry));
        } else if (entry.getExpectNotices() != null) {
            info.add(getExpectNotices(entry));
        } else if (entry.getPostNotice() != null) {
            info.add(getPostNotice(entry));
        } else if (entry.getRetrying() != null) {
            info.add(getRetrying(entry));
        } else if (entry.getCaught() != null) {
            info.add(getCaught(entry));
        } else if (entry.getMoved() != null) {
            info.add(getMoved(entry));
        } else if (entry.getStopped() != null) {
            info.add(getStopped(entry));
        } else if (entry.getResumed() != null) {
            info.add(toString(entry.getResumed(), false));
        } else if (entry.getOrderAdded() != null) {
            info.add(getOrderAdded(entry.getOrderAdded()));
        }
        String s = info.stream().filter(e -> !SOSString.isEmpty(e)).collect(Collectors.joining(", "));
        return String.format("%s [%-8s [%-15s %s", entry.getControllerDatetime(), entry.getLogLevel() + "]", entry.getLogEvent().value() + "]", s);
    }

    private static String getError(OrderLogEntry entry) {
        List<String> info = new ArrayList<String>();
        if (!SOSString.isEmpty(entry.getError().getErrorState())) {
            info.add("status=" + entry.getError().getErrorState());
        }
        if (!SOSString.isEmpty(entry.getError().getErrorCode())) {
            info.add("code=" + entry.getError().getErrorCode());
        }
        if (!SOSString.isEmpty(entry.getError().getErrorReason())) {
            info.add("reason=" + entry.getError().getErrorReason());
        }
        if (!SOSString.isEmpty(entry.getError().getErrorText())) {
            info.add("msg=" + entry.getError().getErrorText());
        }
        return info.stream().collect(Collectors.joining(", ", "Error(", ")"));
    }

    private static String getAgent(OrderLogEntry entry) {
        List<String> info = new ArrayList<String>();
        if (!SOSString.isEmpty(entry.getAgentUrl())) {
            info.add("url=" + entry.getAgentUrl());
        }
        // if (!SOSString.isEmpty(entry.getAgentId())) {
        // info.add("id=" + entry.getAgentId());
        // }
        if (!SOSString.isEmpty(entry.getAgentName())) {
            info.add("name=" + entry.getAgentName());
        }
        if (!SOSString.isEmpty(entry.getAgentDatetime())) {
            info.add("time=" + entry.getAgentDatetime());
        }
        return info.stream().collect(Collectors.joining(", ", "Agent(", ")"));
    }

    // TODO lock details
    private static String getLocks(OrderLogEntry entry) {
        StringBuilder sb = new StringBuilder();
        try {
            if (!SOSString.isEmpty(entry.getMsg())) {
                sb.append(entry.getMsg());
            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("[getLocks]%s", e.toString()), e);
        }
        return sb.toString();
    }

    private static String getConsumeNotices(OrderLogEntry entry) {
        StringBuilder sb = new StringBuilder();
        try {
            if (entry.getConsumeNotices().getConsuming() != null && entry.getConsumeNotices().getConsuming().size() > 0) {
                sb.append("Consuming, ");
                List<String> info = new ArrayList<String>();
                for (BaseNotice n : entry.getConsumeNotices().getConsuming()) {
                    info.add("ExpectNotice(board=" + n.getBoardName() + ", id=" + n.getId() + ")");
                }
                sb.append(String.join(", ", info));
            } else if (entry.getConsumeNotices().getConsumed() != null) {

            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("[getConsumeNotices]%s", e.toString()), e);
        }
        return sb.toString();
    }

    private static String getExpectNotices(OrderLogEntry entry) {
        StringBuilder sb = new StringBuilder();
        try {
            if (entry.getExpectNotices().getWaitingFor() != null && entry.getExpectNotices().getWaitingFor().size() > 0) {
                sb.append("Waiting for ");
                List<String> info = new ArrayList<String>();
                for (BaseNotice n : entry.getExpectNotices().getWaitingFor()) {
                    info.add("ExpectNotice(board=" + n.getBoardName() + ", id=" + n.getId() + ")");
                }
                sb.append(String.join(", ", info));
            } else if (entry.getExpectNotices().getConsumed() != null) {
                sb.append("ExpectNotices(");
                sb.append(entry.getExpectNotices().getConsumed());
                sb.append(")");
            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("[getExpectNotices]%s", e.toString()), e);
        }
        return sb.toString();
    }

    private static String getPostNotice(OrderLogEntry entry) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("PostNotice(");
            sb.append("board=" + entry.getPostNotice().getBoardName());
            sb.append(", id=" + entry.getPostNotice().getId());
            sb.append(", endOfLife=" + entry.getPostNotice().getEndOfLife());
            sb.append(")");
        } catch (Throwable e) {
            LOGGER.warn(String.format("[getPostNotice]%s", e.toString()), e);
        }
        return sb.toString();
    }

    private static String getRetrying(OrderLogEntry entry) {
        StringBuilder sb = new StringBuilder();
        try {
            if (entry.getRetrying().getDelayedUntil() != null) {
                sb.append("Retrying(");
                sb.append("delayedUntil=").append(entry.getRetrying().getDelayedUntil());
                sb.append(")");
            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("[getRetrying]%s", e.toString()), e);
        }
        return sb.toString();
    }

    private static String getCaught(OrderLogEntry entry) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("Caught(");
            sb.append("cause=").append(entry.getCaught().getCause());
            sb.append(")");
        } catch (Throwable e) {
            LOGGER.warn(String.format("[getCaught]%s", e.toString()), e);
        }
        return sb.toString();
    }

    private static String getMoved(OrderLogEntry entry) {
        StringBuilder sb = new StringBuilder();
        try {
            if (entry.getMoved().getSkipped() != null) {
                sb.append("Skipped(");
                sb.append(toString(entry.getMoved().getSkipped().getInstruction(), true));
                sb.append(", reason=" + entry.getMoved().getSkipped().getReason());
                sb.append("), ");
            }
            sb.append("Moved To(pos=").append(entry.getMoved().getTo().getPosition()).append(")");
        } catch (Throwable e) {
            LOGGER.warn(String.format("[getMoved]%s", e.toString()), e);
        }
        return sb.toString();
    }

    private static String getStopped(OrderLogEntry entry) {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append("Stopped(");
            sb.append(toString(entry.getStopped(), true));
            sb.append(")");
        } catch (Throwable e) {
            LOGGER.warn(String.format("[getStopped]%s", e.toString()), e);
        }
        return sb.toString();
    }

    private static String getOrderAdded(OrderAdded entry) {
        StringBuilder sb = new StringBuilder();
        try {
            List<String> info = new ArrayList<String>();
            if (!SOSString.isEmpty(entry.getOrderId())) {
                info.add("id=" + entry.getOrderId());
            }
            if (!SOSString.isEmpty(entry.getWorkflowPath())) {
                info.add("workflow=" + entry.getWorkflowPath());
            }
            if (entry.getArguments() != null && entry.getArguments().getAdditionalProperties().size() > 0) {
                info.add("arguments(" + toString(entry.getArguments()) + ")");
            }
            sb.append("OrderAdded(");
            sb.append(String.join(", ", info));
            sb.append(")");
        } catch (Throwable e) {
            LOGGER.warn(String.format("[getOrderAdded]%s", e.toString()), e);
        }
        return sb.toString();
    }

    private static String toString(OrderLogEntryInstruction in, boolean toLowerCase) {
        if (in == null) {
            return "";
        }
        if (toLowerCase) {
            return in.getInstruction() == null ? "job=" + in.getJob() : "instruction=" + in.getInstruction();
        }
        return in.getInstruction() == null ? "Job=" + in.getJob() : "Instruction=" + in.getInstruction();
    }

    private static String toString(Variables var) {
        StringBuilder sb = new StringBuilder();
        var.getAdditionalProperties().entrySet().forEach(e -> {
            if (e.getValue() instanceof Variables) {
                sb.append(e.getKey()).append("={").append(toString((Variables) e.getValue())).append("}");
            } else {
                sb.append(e.getKey()).append("=").append(e.getValue());
            }
            sb.append(",");
        });
        String r = sb.toString();
        return r.endsWith(",") ? r.substring(0, r.length() - 1) : r;
    }
}
