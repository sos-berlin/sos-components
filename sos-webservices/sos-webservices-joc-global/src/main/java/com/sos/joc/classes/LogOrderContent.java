package com.sos.joc.classes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSPath;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerInvalidResponseDataException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.order.OrderLog;
import com.sos.joc.model.order.OrderLogItem;
import com.sos.joc.model.order.OrderLogItem.LogEvent;
import com.sos.joc.model.order.OrderLogItemError;
import com.sos.joc.model.order.OrderRunningLogFilter;

public class LogOrderContent {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogOrderContent.class);
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mm:ss.SSSZ");
    private static byte[] newlineBytes = { '\r', '\n' };
    private static String newlineString = "\r\n";
    private Long historyId;
    private Long mainParentHistoryId;
    private Long eventId = null;
    private String orderId;
    private Long unCompressedLength = null;

    public LogOrderContent(OrderRunningLogFilter runningLog) {
        this.historyId = runningLog.getHistoryId();
        this.eventId = runningLog.getEventId();
    }

    public LogOrderContent(Long historyId) {
        this.historyId = historyId;
    }

    public Long getUnCompressedLength() {
        return unCompressedLength;
    }

    public String getOrderId() {
        return orderId;
    }

    public String getDownloadFilename() throws UnsupportedEncodingException {
        return String.format("sos-%s-%d.order.log", URLEncoder.encode(orderId, StandardCharsets.UTF_8.name()), historyId);
    }

    private OrderLog getLogRollingFromHistoryService() {
        // TODO
        OrderLog orderLog = new OrderLog();
        OrderLogItem item = new OrderLogItem();
        item.setControllerDatetime(ZonedDateTime.now().format(formatter));
        item.setLogEvent(LogEvent.OrderBroken);
        item.setLogLevel("ERROR");
        // item.setOrderId(orderId);
        item.setPosition("...");
        OrderLogItemError err = new OrderLogItemError();
        err.setErrorText("Running log is not yet implemented");
        err.setErrorState("failed");
        err.setErrorCode("99");
        item.setError(err);
        orderLog.setComplete(true);
        orderLog.setLogEvents(Arrays.asList(item));
        return orderLog;
    }

    private OrderLog getLogFromHistoryService() {
        // TODO read joc.properties (history.propertis) to find logs/history folder
        OrderLog orderLog = new OrderLog();
        orderLog.setComplete(false);
        orderLog.setEventId(Instant.now().toEpochMilli() * 1000);
        try {
            Path orderLogLines = Paths.get("logs", "history", mainParentHistoryId.toString(), historyId + ".log");
            if (Files.exists(orderLogLines)) {
                orderLog.setLogEvents(Arrays.asList(Globals.objectMapper.readValue(SOSPath.readFile(orderLogLines, Collectors.joining(",", "[", "]")),
                        OrderLogItem[].class)));
                unCompressedLength = Files.size(orderLogLines);
                // no running log if OrderFailed or OrderSuspended etc. (later with events)
                int numOfLogEvents = orderLog.getLogEvents().size();
                if (numOfLogEvents > 0) {
                    List<LogEvent> evts = Arrays.asList(LogEvent.OrderCancelled, LogEvent.OrderBroken, LogEvent.OrderFailed,
                            LogEvent.OrderFailedinFork, LogEvent.OrderFinished, LogEvent.OrderSuspended);
                    LogEvent evt = orderLog.getLogEvents().get(numOfLogEvents - 1).getLogEvent();
                    if (evts.contains(evt)) {
                        orderLog.setComplete(true);
                        orderLog.setEventId(null);
                    }
                }
                return orderLog;
            } else {
                // only for the rare moment that the file is deleted and now in the database
                return getLogFromDb();
            }
        } catch (Exception e) {
            LOGGER.warn(e.toString());
            // TODO Auto-generated catch block
            // e.printStackTrace();
        }
        OrderLogItem item = new OrderLogItem();
        item.setOrderId(orderId);
        item.setControllerDatetime(ZonedDateTime.now().format(formatter));
        item.setLogEvent(LogEvent.OrderBroken);
        item.setLogLevel("INFO");
        item.setPosition("...");
        OrderLogItemError err = new OrderLogItemError();
        err.setErrorText("Snapshot log not found");
        err.setErrorReason("Current JOC Cockpit Node is on standby?");
        err.setErrorState("Failed");
        err.setErrorCode("99");
        item.setError(err);
        orderLog.setLogEvents(Arrays.asList(item));
        unCompressedLength = orderLog.toString().length() * 1L;
        return orderLog;
    }

    private DBItemHistoryOrder getDBItemOrder() throws JocConfigurationException, DBOpenSessionException, SOSHibernateException,
            DBMissingDataException {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection("./order/log");
            DBItemHistoryOrder historyOrderItem = connection.get(DBItemHistoryOrder.class, historyId);
            if (historyOrderItem == null) {
                throw new DBMissingDataException(String.format("Order (Id:%d) not found", historyId));
            }// else if (historyOrderItem.getMainParentId() != historyId) {
             // historyId = historyOrderItem.getMainParentId();
             // historyOrderItem = connection.get(DBItemHistoryOrder.class, historyId);
             // if (historyOrderItem == null) {
             // throw new DBMissingDataException(String.format("MainOrder (Id:%d) not found", historyId));
             // }
             // }
            mainParentHistoryId = historyOrderItem.getMainParentId();
            orderId = historyOrderItem.getOrderId();
            return historyOrderItem;
        } finally {
            Globals.disconnect(connection);
        }
    }

    private OrderLog getLogFromDb() throws JocConfigurationException, DBOpenSessionException, SOSHibernateException, DBMissingDataException,
            JsonParseException, JsonMappingException, IOException {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection("./order/log");
            DBItemHistoryOrder historyOrderItem = connection.get(DBItemHistoryOrder.class, historyId);
            if (historyOrderItem == null) {
                throw new DBMissingDataException(String.format("Order (Id:%d) not found", historyId));
            }// else if (historyOrderItem.getMainParentId() != historyId) {
             // historyId = historyOrderItem.getMainParentId();
             // historyOrderItem = connection.get(DBItemHistoryOrder.class, historyId);
             // if (historyOrderItem == null) {
             // throw new DBMissingDataException(String.format("MainOrder (Id:%d) not found", historyId));
             // }
             // }
            mainParentHistoryId = historyOrderItem.getMainParentId();
            orderId = historyOrderItem.getOrderId();
            if (historyOrderItem.getLogId() == 0L) {
                if (historyOrderItem.getEndTime() == null) {
                    // Order is running
                    return null;
                } else {
                    throw new DBMissingDataException(String.format("The log of the order %s (history id:%d) doesn't found", orderId, historyId));
                }
            } else {
                DBItemHistoryLog historyDBItem = connection.get(DBItemHistoryLog.class, historyOrderItem.getLogId());
                if (historyDBItem == null) {
                    throw new DBMissingDataException(String.format("The log of the order %s (history id:%d) doesn't found", orderId, historyId));
                } else {
                    unCompressedLength = historyDBItem.getFileSizeUncomressed();
                    if (!historyDBItem.fileContentIsNull()) {
                        OrderLog orderLog = new OrderLog();
                        orderLog.setComplete(true);
                        orderLog.setLogEvents(Arrays.asList(Globals.objectMapper.readValue(historyDBItem.getFileContent(), OrderLogItem[].class)));
                        return orderLog;
                    }
                    // Order is running
                    return null;
                }
            }
        } finally {
            Globals.disconnect(connection);
        }
    }

    public OrderLog getOrderLog() throws JsonParseException, JsonMappingException, JocConfigurationException, DBOpenSessionException,
            SOSHibernateException, DBMissingDataException, IOException, JocMissingRequiredParameterException {
        if (historyId == null) {
            throw new JocMissingRequiredParameterException("undefined 'historyId'");
        }
        OrderLog orderLog = null;
        if (eventId != null) {
            getDBItemOrder();
            orderLog = getLogRollingFromHistoryService();
        } else {
            orderLog = getLogFromDb();
            if (orderLog == null) {
                orderLog = getLogFromHistoryService();
            }
        }
        // TODO later part of Robert's history
        if (orderLog != null && orderLog.getLogEvents() != null) {
            orderLog.setLogEvents(orderLog.getLogEvents().stream().map(item -> getMappedLogItem(item)).collect(Collectors.toList()));

            // set complete true if Order only added -> no running log expected
            if (orderLog.getLogEvents().size() == 1 && LogEvent.OrderAdded.equals(orderLog.getLogEvents().get(0).getLogEvent())) {
                orderLog.setComplete(true);
                orderLog.setEventId(null);
            }
        }

        return orderLog;
    }

    public StreamingOutput getStreamOutput() throws JocMissingRequiredParameterException, JocConfigurationException, DBOpenSessionException,
            SOSHibernateException, DBMissingDataException, JobSchedulerInvalidResponseDataException, JsonParseException, JsonMappingException,
            IOException {
        if (historyId == null) {
            throw new JocMissingRequiredParameterException("undefined 'taskId'");
        }
        StreamingOutput out = null;
        OrderLog orderLog = null;
        orderLog = getLogFromDb();
        if (orderLog == null) {
            orderLog = getLogFromHistoryService();
        }
        if (orderLog != null) {
            final List<OrderLogItem> logItems = orderLog.getLogEvents();
            out = new StreamingOutput() {

                @Override
                public void write(OutputStream outstream) throws IOException, WebApplicationException {
                    InputStream inStream = null;
                    GZIPOutputStream output = new GZIPOutputStream(outstream);
                    try {
                        byte[] buffer = new byte[4096];
                        int length;
                        for (OrderLogItem i : logItems) {
                            inStream = getLogLine(i);
                            while ((length = inStream.read(buffer)) > 0) {
                                output.write(buffer, 0, length);
                            }
                            if (i.getLogEvent() == LogEvent.OrderProcessingStarted) {
                                // read tasklog
                                LogTaskContent logTaskContent = new LogTaskContent(i.getTaskId());
                                inStream = logTaskContent.getLogStream();
                                if (inStream != null) {
                                    output.write(newlineBytes);
                                    while ((length = inStream.read(buffer)) > 0) {
                                        output.write(buffer, 0, length);
                                    }
                                    output.write(newlineBytes);
                                }
                            }
                        }
                        output.flush();
                    } catch (IOException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new WebApplicationException(e);
                    } finally {
                        try {
                            output.close();
                        } catch (Exception e) {
                        }
                    }
                }
            };
        }
        if (out == null) {
            throw new JobSchedulerInvalidResponseDataException(String.format("Order Log (Id:%d) not found", historyId));
        }

        return out;
    }

    public static ByteArrayInputStream getLogLine(OrderLogItem item) {
        // "masterDatetime [logLevel] [logEvent] id:orderId, pos:position"
        // and further optional additions
        // " ,Job=job, Agent (url=agentUrl, id=agentId, time=agentDatetime), Job=job"
        // " ,Error (status=error.errorState, code=error.errorCode, reason=error.errorReason, msg=error.errorText), returncode=returncode
        item = getMappedLogItem(item);
        List<String> info = new ArrayList<String>();

        String agent = null;
        if (item.getLogEvent() == LogEvent.OrderProcessingStarted) {
            if (item.getAgentUrl() != null && !item.getAgentUrl().isEmpty()) {
                info.add("url=" + item.getAgentUrl());
            }
            if (item.getAgentPath() != null && !item.getAgentPath().isEmpty()) {
                info.add("id=" + item.getAgentPath());
            }
            if (item.getAgentPath() != null && !item.getAgentPath().isEmpty()) {
                info.add("time=" + item.getAgentDatetime());
            }
            if (!info.isEmpty()) {
                agent = info.stream().collect(Collectors.joining(", ", "Agent (", ")"));
            }
        }

        info.clear();
        String error = null;
        if (item.getError() != null) {
            OrderLogItemError err = item.getError();
            if (err.getErrorState() != null && !err.getErrorState().isEmpty()) {
                info.add("status=" + err.getErrorState());
            }
            if (err.getErrorCode() != null && !err.getErrorCode().isEmpty()) {
                info.add("code=" + err.getErrorCode());
            }
            if (err.getErrorReason() != null && !err.getErrorReason().isEmpty()) {
                info.add("reason=" + err.getErrorReason());
            }
            if (err.getErrorText() != null && !err.getErrorText().isEmpty()) {
                info.add("msg=" + err.getErrorText());
            }
            if (!info.isEmpty()) {
                error = info.stream().collect(Collectors.joining(", ", "Error (", ")"));
            }
        }

        info.clear();
        if (item.getOrderId() != null && !item.getOrderId().isEmpty()) {
            info.add("id=" + item.getOrderId());
        }
        if (item.getPosition() != null && !item.getPosition().isEmpty()) {
            info.add("pos=" + item.getPosition());
        }
        if (item.getJob() != null && !item.getJob().isEmpty()) {
            info.add("Job=" + item.getJob());
        }
        if (agent != null) {
            info.add(agent);
        }
        if (item.getReturnCode() != null) {
            info.add("returnCode=" + item.getReturnCode());
        }
        if (error != null) {
            info.add(error);
        }
        String loglineAdditionals = "";
        if (!info.isEmpty()) {
            loglineAdditionals = info.stream().collect(Collectors.joining(", "));
        }

        String logline = String.format("%s [%-8s [%-15s %s", item.getControllerDatetime(), item.getLogLevel() + "]", item.getLogEvent().value() + "]",
                loglineAdditionals) + newlineString;
        return new ByteArrayInputStream(logline.getBytes(StandardCharsets.UTF_8));
    }

    private static OrderLogItem getMappedLogItem(OrderLogItem item) {
        if (item.getError() != null) {
            item.setLogLevel("ERROR");
        } else if (item.getLogEvent() == LogEvent.OrderProcessed) {
            item.setLogLevel("SUCCESS");
        }
        if (item.getOrderId() != null && item.getOrderId().contains("/")) {
            item.setOrderId(item.getOrderId().replaceFirst("^[^/]+/", ""));
        }
        return item;
    }

}
