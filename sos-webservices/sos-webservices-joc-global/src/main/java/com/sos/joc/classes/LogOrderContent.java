package com.sos.joc.classes;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.StreamingOutput;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.history.DBItemLog;
import com.sos.jobscheduler.db.history.DBItemOrder;
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

    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mm:ss.SSSZ");
    private Long historyId;
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
        item.setMasterDatetime(ZonedDateTime.now().format(formatter));
        item.setLogEvent(LogEvent.OrderFinished);
        item.setLogLevel("INFO");
        item.setOrderId(orderId);
        item.setPosition("...");
        OrderLogItemError err = new OrderLogItemError();
        err.setErrorText("Running log is not yet implemented");
        err.setErrorState("failed");
        err.setErrorReason("Running log is not yet implemented");
        err.setErrorCode("99");
        item.setError(err);
        orderLog.setComplete(true);
        orderLog.setLogEvents(Arrays.asList(item));
        return orderLog;
    }

    private OrderLog getLogFromHistoryService() {
        // TODO
        OrderLog orderLog = new OrderLog();
        OrderLogItem item = new OrderLogItem();
        item.setMasterDatetime(ZonedDateTime.now().format(formatter));
        item.setLogEvent(LogEvent.OrderFinished);
        item.setLogLevel("INFO");
        item.setOrderId(orderId);
        item.setPosition("...");
        OrderLogItemError err = new OrderLogItemError();
        err.setErrorText("Snapshot log is not yet implemented");
        err.setErrorReason("Snapshot log is not yet implemented");
        err.setErrorState("failed");
        err.setErrorCode("99");
        item.setError(err);
        orderLog.setComplete(true);
        orderLog.setLogEvents(Arrays.asList(item));
        unCompressedLength = orderLog.toString().length() * 1L;
        return orderLog;
    }

    private DBItemOrder getDBItemOrder() throws JocConfigurationException, DBOpenSessionException, SOSHibernateException, DBMissingDataException {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection("./order/log");
            DBItemOrder historyOrderItem = connection.get(DBItemOrder.class, historyId);
            if (historyOrderItem == null) {
                throw new DBMissingDataException(String.format("Order (Id:%d) not found", historyId));
            } else if (historyOrderItem.getMainParentId() != historyId) {
                historyId = historyOrderItem.getMainParentId();
                historyOrderItem = connection.get(DBItemOrder.class, historyId);
                if (historyOrderItem == null) {
                    throw new DBMissingDataException(String.format("MainOrder (Id:%d) not found", historyId));
                }
            }
            orderId = historyOrderItem.getOrderKey();
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
            DBItemOrder historyOrderItem = connection.get(DBItemOrder.class, historyId);
            if (historyOrderItem == null) {
                throw new DBMissingDataException(String.format("Order (Id:%d) not found", historyId));
            } else if (historyOrderItem.getMainParentId() != historyId) {
                historyId = historyOrderItem.getMainParentId();
                historyOrderItem = connection.get(DBItemOrder.class, historyId);
                if (historyOrderItem == null) {
                    throw new DBMissingDataException(String.format("MainOrder (Id:%d) not found", historyId));
                }
            }
            orderId = historyOrderItem.getOrderKey();
            if (historyOrderItem.getLogId() == 0L) {
                if (historyOrderItem.getEndTime() == null) {
                    // Order is running
                    return null;
                } else {
                    throw new DBMissingDataException(String.format("The log of the order %s (history id:%d) doesn't found", orderId, historyId));
                }
            } else {
                DBItemLog historyDBItem = connection.get(DBItemLog.class, historyOrderItem.getLogId());
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
                                    while ((length = inStream.read(buffer)) > 0) {
                                        output.write(buffer, 0, length);
                                    }
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
        // "masterDatetime [logLevel][logEvent] id:orderId, pos:position"
        // and further optional additions
        // " Agent( agentDatetime path:agentPath, url:agentUrl ), Job:job, returncode:returncode"
        // " [error.errorState] code:error.errorCode, reason:error.errorReason, msg:error.errorText
        List<String> info = new ArrayList<String>();
        String agent = "";
        if ((item.getAgentDatetime() != null && !item.getAgentDatetime().isEmpty()) || (item.getAgentPath() != null && !item.getAgentPath().isEmpty())
                || (item.getAgentUrl() != null && !item.getAgentUrl().isEmpty())) {
            String prefix = ", Agent( ";
            if (item.getAgentDatetime() != null && !item.getAgentDatetime().isEmpty()) {
                prefix += item.getAgentDatetime() + " ";
            }
            if (item.getAgentPath() != null && !item.getAgentPath().isEmpty()) {
                info.add("path:" + item.getAgentPath());
            }
            if (item.getAgentUrl() != null && !item.getAgentUrl().isEmpty()) {
                info.add("url:" + item.getAgentUrl());
            }
            agent = info.stream().collect(Collectors.joining(", ", prefix, " )"));
        }
        String job = "";
        if (item.getJob() != null && !item.getJob().isEmpty()) {
            job = ", Job:" + item.getJob();
        }
        String rc = "";
        if (item.getReturnCode() != null) {
            rc = ", returnCode:" + item.getReturnCode();
        }
        String error = "";
        if (item.getError() != null) {
            OrderLogItemError err = item.getError();
            if (err.getErrorState() != null && !err.getErrorState().isEmpty()) {
                error = " [" + err.getErrorState() + "]";
            } else {
                error = " [Error]";
            }
            if ((err.getErrorCode() != null && !err.getErrorCode().isEmpty()) || (err.getErrorReason() != null && !err.getErrorReason().isEmpty())
                    || (err.getErrorText() != null && !err.getErrorText().isEmpty())) {
                info.clear();
                if (err.getErrorCode() != null && !err.getErrorCode().isEmpty()) {
                    info.add("code:" + err.getErrorCode());
                }
                if (err.getErrorReason() != null && !err.getErrorReason().isEmpty()) {
                    info.add("reason:" + err.getErrorReason());
                }
                if (err.getErrorText() != null && !err.getErrorText().isEmpty()) {
                    info.add("msg:" + err.getErrorText());
                }
                error += info.stream().collect(Collectors.joining(", ", " ", ""));
            }
        }
        String logline = String.format("%s [%s][%s] id:%s, pos:%s%s%s%s%s%n", item.getMasterDatetime(), item.getLogLevel(), item.getLogEvent()
                .value(), item.getOrderId(), item.getPosition(), agent, job, rc, error);
        return new ByteArrayInputStream(logline.getBytes(StandardCharsets.UTF_8));
    }

}
