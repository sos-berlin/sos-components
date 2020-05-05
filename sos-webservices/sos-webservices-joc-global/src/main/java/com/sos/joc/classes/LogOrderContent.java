package com.sos.joc.classes;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;

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

public class LogOrderContent {

    private Long historyId;
    private Long eventId = null;
    private String orderId;
    private Long unCompressedLength = null;

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
    
    
    
//    public Path writeGzipLogFile() throws SOSHibernateException, JocConfigurationException, DBOpenSessionException, IOException,
//            JobSchedulerObjectNotExistException, JocMissingRequiredParameterException, DBConnectionRefusedException, DBInvalidDataException {
//
//        if (historyId == null) {
//            throw new JocMissingRequiredParameterException("undefined 'historyId'");
//        }
//        Path path = writeGzipLogFileFromDB();
//        // TODO web service call for runnning logs
//        if (path == null) {
//            path = writeGzipLogFileFromHistoryService();
//        }
//        if (path == null) {
//            String msg = String.format("Order log with id %s is missing", historyId);
//            throw new JobSchedulerObjectNotExistException(msg);
//        }
//        return path;
//    }
    
//    private Date getDateFromEventId(String eventId) {
//        return Date.from(Instant.ofEpochMilli(Long.valueOf(eventId.substring(0, 13))));
//    }
//    
//    private OrderLogItem getOrderAddedEvent(DBItemOrder orderEvent) {
//        OrderLogItem logItem = new OrderLogItem();
//        logItem.setTimestamp(getDateFromEventId(orderEvent.getStartEventId()));
//        logItem.setOrderId(orderEvent.getOrderKey());
//        logItem.setLogLevel("DEBUG");
//        logItem.setLogEvent("OrderAdded");
//        logItem.setPosition(orderEvent.getStartWorkflowPosition());
//        return logItem;
//    }
//    
//    private OrderLogItem getOrderEndEvent(DBItemOrder orderEvent) {
//        OrderLogItem logItem = new OrderLogItem();
//        logItem.setTimestamp(getDateFromEventId(orderEvent.getEndEventId()));
//        logItem.setOrderId(orderEvent.getOrderKey());
//        logItem.setLogLevel("INFO");
//        switch (orderEvent.getStatus()) {
//        case "stopped":
//        case "failed":
//            logItem.setLogEvent("OrderFailed");
//            break;
//        case "finished":
//            logItem.setLogEvent("OrderFinished");
//            break;
//        case "cancelled":
//        case "canceled":
//            logItem.setLogEvent("OrderCanceled");
//            break;
//        default:
//            logItem.setLogEvent("OrderEnded");   
//        }
//        logItem.setPosition(orderEvent.getEndWorkflowPosition());
//        logItem.setError(orderEvent.getError());
//        if (orderEvent.getError()) {
//            logItem.setErrorStatus(orderEvent.getErrorStatus());
//            logItem.setErrorReason(orderEvent.getErrorReason());
//            logItem.setErrorText(orderEvent.getErrorText());
//        }
//        return logItem;
//    }
    
    private OrderLog getLogRollingFromHistoryService() {
        // TODO
        OrderLog orderLog = new OrderLog();
        OrderLogItem item = new OrderLogItem();
        item.setTimestamp(Date.from(Instant.now()));
        item.setLogEvent(LogEvent.ORDER_FINISHED);
        item.setLogLevel("INFO");
        item.setOrderId(orderId);
        OrderLogItemError err = new OrderLogItemError();
        err.setErrorText("Running log is not yet implemented");
        item.setError(err);
        orderLog.setComplete(true);
        orderLog.setLogEvents(Arrays.asList(item));
        return orderLog;
    }

    private OrderLog getLogFromHistoryService() {
        // TODO
        OrderLog orderLog = new OrderLog();
        OrderLogItem item = new OrderLogItem();
        item.setTimestamp(Date.from(Instant.now()));
        item.setLogEvent(LogEvent.ORDER_FINISHED);
        item.setLogLevel("INFO");
        item.setOrderId(orderId);
        OrderLogItemError err = new OrderLogItemError();
        err.setErrorText("Snapshot log is not yet implemented");
        item.setError(err);
        orderLog.setComplete(true);
        orderLog.setLogEvents(Arrays.asList(item));
        return orderLog;
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
                        //TODO orderLog.setLogEvents(Arrays.asList(Globals.objectMapper.readValue("[" + SOSStreamUnzip.unzip2String(historyDBItem.getFileContent()) + "]"
                        //        .replaceFirst("^\\[+", "[").replaceFirst("\\]+$", "]"), OrderLogItem[].class)));
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
    
//    public void getOrderLog() {
//        SOSHibernateSession connection = null;
//        try {
//            connection = Globals.createSosHibernateStatelessConnection("./order/log");
//            JobHistoryDBLayer dbLayer = new JobHistoryDBLayer(connection);
//            List<DBItemOrder> orderEvents = dbLayer.getOrdersOfMainOrder(historyId);
//            List<OrderLogItem> logItems = new ArrayList<OrderLogItem>();
//            
//            for (DBItemOrder orderEvent : orderEvents) {
//                if (orderEvent.getId() == historyId) {
//                    logItems.add(getOrderAddedEvent(orderEvent));
//                    if (orderEvent.getEndEventId() != null) {
//                        logItems.add(getOrderEndEvent(orderEvent));
//                    }
//                }
//            }
//            
//            ...to be continue
//            
//    }
    
    public OrderLog getOrderLog() throws JsonParseException, JsonMappingException, JocConfigurationException, DBOpenSessionException,
            SOSHibernateException, DBMissingDataException, IOException, JocMissingRequiredParameterException {
        if (historyId == null) {
            throw new JocMissingRequiredParameterException("undefined 'taskId'");
        }
        OrderLog orderLog = null;
        if (eventId != null) {
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
        if (orderLog != null) { //TODO
//            final InputStream inStream = new ByteArrayInputStream(compressedLog);
//            out = new StreamingOutput() {
//
//                @Override
//                public void write(OutputStream output) throws IOException {
//                    try {
//                        byte[] buffer = new byte[4096];
//                        int length;
//                        while ((length = inStream.read(buffer)) > 0) {
//                            output.write(buffer, 0, length);
//                        }
//                        output.flush();
//                    } finally {
//                        try {
//                            output.close();
//                        } catch (Exception e) {
//                        }
//                    }
//                }
//            };
        }
        if (out == null) {
            //throw new JobSchedulerInvalidResponseDataException(String.format("Order Log (Id:%d) not found", historyId));
            throw new JobSchedulerInvalidResponseDataException("Download log is not yet implemented");
        }

        return out;
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
