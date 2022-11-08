package com.sos.joc.classes.logs;

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
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.GZIPOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.auth.classes.SOSAuthFolderPermissions;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSPath;
import com.sos.controller.model.event.EventType;
import com.sos.joc.Globals;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.classes.history.HistoryLogMapper;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.exceptions.ControllerInvalidResponseDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.history.order.OrderLogEntry;
import com.sos.joc.model.history.order.OrderLogEntryError;
import com.sos.joc.model.history.order.OrderLogEntryLogLevel;
import com.sos.joc.model.order.OrderLog;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.StreamingOutput;

public class LogOrderContent {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogOrderContent.class);
    private static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mm:ss.SSSZ");
    private static byte[] newlineBytes = { '\r', '\n' };
    private static String newlineString = "\r\n";
    private Long historyId;
    private Long mainParentHistoryId;
    private String orderId;
    private Long unCompressedLength = null;
    private final SOSAuthFolderPermissions folderPermissions;

    public LogOrderContent(Long historyId, SOSAuthFolderPermissions folderPermissions) {
        this.historyId = historyId;
        this.folderPermissions = folderPermissions;
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

    private OrderLog getLogSnapshotFromHistoryService() {
        OrderLog orderLog = new OrderLog();
        orderLog.setHistoryId(historyId);
        orderLog.setComplete(true);
        orderLog.setEventId(null);
        try {
            Path logFile = Paths.get("logs", "history", mainParentHistoryId.toString(), historyId + ".log");
            if (Files.exists(logFile)) {
                LOGGER.debug(String.format("[%s]Log file found", logFile));
                orderLog.setLogEvents(Arrays.asList(Globals.objectMapper.readValue(SOSPath.readFile(logFile, Collectors.joining(",", "[", "]")),
                        OrderLogEntry[].class)));
                unCompressedLength = Files.size(logFile);
                int numOfLogEvents = orderLog.getLogEvents().size();
                boolean isIncomplete = false;
                if (numOfLogEvents > 0) {
                    EventType lastEvt = orderLog.getLogEvents().get(numOfLogEvents - 1).getLogEvent();
                    if (!RunningOrderLogs.completeTypes.contains(lastEvt)) {
                        isIncomplete = true;
                    }
                } else {
                    isIncomplete = true;
                }
                if (isIncomplete) {
                    orderLog.setComplete(false);
                    orderLog.setEventId(Instant.now().toEpochMilli());
                    RunningOrderLogs.getInstance().subscribe(historyId);
                }
                return orderLog;
            } else {
                LOGGER.debug(String.format("[%s]Log file not found. Try to read from db...", logFile));
                // only for the rare moment that the file is deleted and now in the database
                OrderLog oLog = getLogFromDb();
                if (oLog != null) {
                    return oLog;
                }
            }
        } catch (Exception e) {
            LOGGER.warn(e.toString());
        }

        OrderLogEntry item = new OrderLogEntry();
        item.setOrderId(orderId);
        item.setControllerDatetime(ZonedDateTime.now().format(formatter));
        item.setLogEvent(EventType.OrderBroken);
        item.setLogLevel(OrderLogEntryLogLevel.INFO);
        item.setPosition("0");
        OrderLogEntryError err = new OrderLogEntryError();
        err.setErrorReason(null);
        err.setErrorState(".");
        if (JocClusterService.getInstance().isRunning()) {
            err.setErrorText("Couldn't find the snapshot log");
        } else {
            err.setErrorText("Standby JOC Cockpit instance has no access to the snapshot log");
        }
        // err.setErrorState("Failed");
        // err.setErrorCode("99");
        item.setError(err);
        orderLog.setLogEvents(Arrays.asList(item));
        unCompressedLength = orderLog.toString().length() * 1L;
        return orderLog;
    }

    private OrderLog getLogFromDb() throws JocConfigurationException, DBOpenSessionException, SOSHibernateException, DBMissingDataException,
            JsonParseException, JsonMappingException, IOException {
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection("./order/log");
            DBItemHistoryOrder historyOrderItem = connection.get(DBItemHistoryOrder.class, historyId);
            if (historyOrderItem == null) {
                throw new DBMissingDataException(String.format("Couldn't find the Order (Id:%d)", historyId));
            }// else if (historyOrderItem.getMainParentId() != historyId) {
             // historyId = historyOrderItem.getMainParentId();
             // historyOrderItem = connection.get(DBItemHistoryOrder.class, historyId);
             // if (historyOrderItem == null) {
             // throw new DBMissingDataException(String.format("Couldn't find the MainOrder (Id:%d)", historyId));
             // }
             // }
            if (!folderPermissions.isPermittedForFolder(historyOrderItem.getWorkflowFolder())) {
                throw new JocFolderPermissionsException("folder access denied: " + historyOrderItem.getWorkflowFolder());
            }
            mainParentHistoryId = historyOrderItem.getMainParentId();
            orderId = historyOrderItem.getOrderId();
            if (historyOrderItem.getLogId() == 0L) {
                if (historyOrderItem.getEndTime() == null) {
                    // Order is running
                    return null;
                } else {
                    throw new DBMissingDataException(String.format("Couldn't find the log of the order %s (history id:%d)", orderId, historyId));
                }
            } else {
                DBItemHistoryLog historyDBItem = connection.get(DBItemHistoryLog.class, historyOrderItem.getLogId());
                if (historyDBItem == null) {
                    throw new DBMissingDataException(String.format("Couldn't find the log of the order %s (history id:%d)", orderId, historyId));
                } else {
                    unCompressedLength = historyDBItem.getFileSizeUncomressed();
                    if (!historyDBItem.fileContentIsNull()) {
                        OrderLog orderLog = new OrderLog();
                        orderLog.setComplete(true);
                        orderLog.setLogEvents(Arrays.asList(Globals.objectMapper.readValue(historyDBItem.getFileContent(), OrderLogEntry[].class)));
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
        OrderLog orderLog = getLogFromDb();
        if (orderLog == null) {
            orderLog = getLogSnapshotFromHistoryService();
        }
        // TODO later part of Robert's history
        if (orderLog.getLogEvents() != null) {
            orderLog.setLogEvents(orderLog.getLogEvents().stream().map(item -> getMappedLogItem(item)).collect(Collectors.toList()));

            // set complete true if Order only added -> no running log expected
            if (orderLog.getLogEvents().size() == 1 && EventType.OrderAdded.equals(orderLog.getLogEvents().get(0).getLogEvent())) {
                orderLog.setComplete(true);
                orderLog.setEventId(null);
            }
        }

        return orderLog;
    }

    public StreamingOutput getStreamOutput() throws JocMissingRequiredParameterException, JocConfigurationException, DBOpenSessionException,
            SOSHibernateException, DBMissingDataException, ControllerInvalidResponseDataException, JsonParseException, JsonMappingException,
            IOException {
        if (historyId == null) {
            throw new JocMissingRequiredParameterException("undefined 'taskId'");
        }
        StreamingOutput out = null;
        OrderLog orderLog = null;
        orderLog = getLogFromDb();
        if (orderLog == null) {
            orderLog = getLogSnapshotFromHistoryService();
        }
        if (orderLog != null) {
            final List<OrderLogEntry> logItems = orderLog.getLogEvents();
            out = new StreamingOutput() {

                @Override
                public void write(OutputStream outstream) throws IOException, WebApplicationException {
                    InputStream inStream = null;
                    GZIPOutputStream output = new GZIPOutputStream(outstream);
                    try {
                        byte[] buffer = new byte[4096];
                        int length;
                        for (OrderLogEntry i : logItems) {
                            inStream = getLogLine(i);
                            while ((length = inStream.read(buffer)) > 0) {
                                output.write(buffer, 0, length);
                            }
                            if (i.getLogEvent() == EventType.OrderProcessingStarted) {
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
            throw new ControllerInvalidResponseDataException(String.format("Couldn't find the Order Log (Id:%d)", historyId));
        }

        return out;
    }

    public static ByteArrayInputStream getLogLine(OrderLogEntry item) {
        // "masterDatetime [logLevel] [logEvent] id:orderId, pos:position"
        // and further optional additions
        // " ,Job=job, Agent (url=agentUrl, id=agentId, time=agentDatetime), Job=job"
        // " ,Error (status=error.errorState, code=error.errorCode, reason=error.errorReason, msg=error.errorText), returncode=returncode
        item = getMappedLogItem(item);
        String logline = HistoryLogMapper.toString(item) + newlineString;
        return new ByteArrayInputStream(logline.getBytes(StandardCharsets.UTF_8));
    }

    protected static OrderLogEntry getMappedLogItem(OrderLogEntry item) {
        // if (item.getError() != null) {
        // item.setLogLevel(OrderLogEntryLogLevel.ERROR);
        // } else if (item.getLogEvent() == EventType.OrderProcessed) {
        // item.setLogLevel(OrderLogEntryLogLevel.SUCCESS);
        // }
        if (item.getOrderId() != null && item.getOrderId().contains("/")) {
            item.setOrderId(item.getOrderId().replaceFirst("^[^/]+/", ""));
        }
        return item;
    }

}
