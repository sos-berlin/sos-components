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
import java.util.Arrays;
import java.util.List;
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
                public void write(OutputStream outstream) throws IOException, WebApplicationException  {
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
        // dateTime [logLevel][logEvent][orderId][position]...
        StringBuilder s = new StringBuilder();
        s.append(String.format("%s [%s][%s][%s][%s]", item.getMasterDatetime(), item.getLogLevel(), item.getLogEvent().value(), item.getOrderId(),
                item.getPosition()));
        if (item.getAgentDatetime() != null && !item.getAgentDatetime().isEmpty()) {
            s.append("[").append(item.getAgentDatetime()).append("]");
        }
        if (item.getAgentPath() != null && !item.getAgentPath().isEmpty()) {
            s.append("[").append(item.getAgentPath()).append("]");
        }
        if (item.getAgentUrl() != null && !item.getAgentUrl().isEmpty()) {
            s.append("[").append(item.getAgentUrl()).append("]");
        }
        if (item.getJob() != null && !item.getJob().isEmpty()) {
            s.append("[").append(item.getJob()).append("]");
        }
        if (item.getReturnCode() != null) {
            s.append("[").append(item.getReturnCode()).append("]");
        }
        if (item.getError() != null) {
            OrderLogItemError err = item.getError();
            if (err.getErrorState() == null) {
                s.append("[success]");
            } else {
                s.append("[").append(err.getErrorState());
                if (err.getErrorCode() != null) {
                    s.append(", ").append(err.getErrorCode());
                }
                if (err.getErrorReason() != null) {
                    s.append(", ").append(err.getErrorReason());
                }
                if (err.getErrorText() != null) {
                    s.append(", ").append(err.getErrorText());
                }
                s.append("]");
            }
        }
        s.append("\n");
        return new ByteArrayInputStream(s.toString().getBytes(StandardCharsets.UTF_8));
    }

//    private Path writeGzipLogFileFromDB() throws SOSHibernateException, IOException, JocConfigurationException, DBOpenSessionException,
//            DBConnectionRefusedException, DBInvalidDataException {
//        SOSHibernateSession connection = null;
//        Path path = null;
//        try {
//            connection = Globals.createSosHibernateStatelessConnection("./order/log");
//            JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(connection);
//            List<Long> logIds = dbLayer.getLogIdsFromOrder(historyId);
//            if (logIds == null || logIds.isEmpty()) {
//                return null;
//            } else {
//                for (Long logId : logIds) {
//                    DBItemLog historyDBItem = connection.get(DBItemLog.class, logId);
//                    if (historyDBItem == null) {
//                        continue;
//                    } else {
//                        if (path == null) {
//                            path = historyDBItem.writeGzipLogFile(String.format(prefix, historyId));
//                        } else {
//                            historyDBItem.writeGzipLogFile(path, true);
//                        }
//                    }
//                }
//                if (logIds.size() > 1) {
//                    //TODO test if path is merged gzip
//                    Path path2 = Files.createTempFile(String.format(prefix, historyId), null);
//                    boolean unMerged = mergedGzipToFile(path, path2);
//                    Files.deleteIfExists(path);
//                    path = unMerged ? path2 : null;
//                }
//                return path;
//            }
//        } finally {
//            Globals.disconnect(connection);
//        }
//    }
//    
//    private boolean mergedGzipToFile(Path source, Path target) throws IOException {
//        if (source == null || target == null) {
//            return false;
//        }
//        InputStream is = null;
//        OutputStream out = null;
//        try {
//            is = new GzipCompressorInputStream(Files.newInputStream(source), true);
//            out = new GZIPOutputStream(Files.newOutputStream(target));
//            byte[] buffer = new byte[4096];
//            int length;
//            while ((length = is.read(buffer)) > 0) {
//                out.write(buffer, 0, length);
//            }
//            out.flush();
//            return true;
//        } finally {
//            try {
//                if (is != null) {
//                    is.close();
//                    is = null;
//                }
//            } catch (IOException e) {
//            }
//            try {
//                if (out != null) {
//                    out.close();
//                    out = null;
//                }
//            } catch (IOException e) {
//            }
//        }
//    }
//
//    
//
//    private Path writeGzipLogFileFromHistoryService() {
//        // TODO Auto-generated method stub
//        return null;
//    }
}
