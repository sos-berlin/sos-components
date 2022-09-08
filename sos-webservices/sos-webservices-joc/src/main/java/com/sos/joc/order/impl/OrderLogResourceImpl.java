package com.sos.joc.order.impl;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import jakarta.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.logs.LogOrderContent;
import com.sos.joc.classes.logs.RunningOrderLogs;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.history.HistoryOrderLogArrived;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.order.OrderHistoryFilter;
import com.sos.joc.model.order.OrderRunningLogFilter;
import com.sos.joc.model.order.RunningOrderLogEvents;
import com.sos.joc.order.resource.IOrderLogResource;
import com.sos.schema.JsonValidator;

@Path("order")
public class OrderLogResourceImpl extends JOCResourceImpl implements IOrderLogResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderLogResourceImpl.class);
    private static final String API_CALL = "./order/log";
    private static final String API_CALL_DOWNLOAD = "./order/log/download";
    private static final String API_CALL_RUNNING = "./order/log/running";
    private Lock lock = new ReentrantLock();
    private Condition condition = null;
    private Long historyId = null;
    private volatile AtomicBoolean complete = new AtomicBoolean(false);
    private volatile AtomicBoolean eventArrived = new AtomicBoolean(false);

    @Override
    public JOCDefaultResponse postOrderLog(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrderHistoryFilter.class);
            OrderHistoryFilter orderHistoryFilter = Globals.objectMapper.readValue(filterBytes, OrderHistoryFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(orderHistoryFilter.getControllerId(), getControllerPermissions(orderHistoryFilter
                    .getControllerId(), accessToken).getOrders().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            LogOrderContent logOrderContent = new LogOrderContent(orderHistoryFilter.getHistoryId(), folderPermissions);
            return JOCDefaultResponse.responseStatus200(logOrderContent.getOrderLog());
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse downloadOrderLog(String accessToken, String queryAccessToken, String controllerId, Long historyId) {
        if (accessToken == null) {
            accessToken = queryAccessToken;
        }
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (controllerId != null) {
            builder.add("controllerId", controllerId);
        }
        if (historyId != null) {
            builder.add("historyId", historyId);
        }
        return downloadOrderLog(accessToken, builder.build().toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public JOCDefaultResponse downloadOrderLog(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_DOWNLOAD, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrderHistoryFilter.class);
            OrderHistoryFilter orderHistoryFilter = Globals.objectMapper.readValue(filterBytes, OrderHistoryFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(orderHistoryFilter.getControllerId(), getControllerPermissions(orderHistoryFilter
                    .getControllerId(), accessToken).getOrders().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            LogOrderContent logOrderContent = new LogOrderContent(orderHistoryFilter.getHistoryId(), folderPermissions);
            return JOCDefaultResponse.responseOctetStreamDownloadStatus200(logOrderContent.getStreamOutput(), logOrderContent.getDownloadFilename(),
                    logOrderContent.getUnCompressedLength());
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    @Override
    public JOCDefaultResponse postRollingOrderLog(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_RUNNING, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, OrderRunningLogFilter.class);
            RunningOrderLogEvents orderLog = Globals.objectMapper.readValue(filterBytes, RunningOrderLogEvents.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(orderLog.getControllerId(), getControllerPermissions(orderLog
                    .getControllerId(), accessToken).getOrders().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            historyId = orderLog.getHistoryId();
            orderLog.setComplete(false);
            orderLog.setLogEvents(Collections.emptyList());
            
            RunningOrderLogs r = RunningOrderLogs.getInstance();
            RunningOrderLogs.Mode mode = r.hasEvents(orderLog.getEventId(), historyId);
            LOGGER.debug("historyId '" + historyId + "' has log events: " + mode.name());
            switch (mode) {
            case TRUE:
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e1) {
                }
            case COMPLETE:
                orderLog = r.getRunningOrderLog(orderLog);
                break;
            case FALSE:
                EventBus.getInstance().register(this);
                condition = lock.newCondition();
                waitingForEvents(TimeUnit.MINUTES.toMillis(1));
                LOGGER.debug("historyId '" + historyId + "' end of waiting events: event received? " + eventArrived.get() + ", complete? " + complete.get());
                if (eventArrived.get()) {
                    if (!complete.get()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(500);
                        } catch (InterruptedException e1) {
                        }
                    }
                    orderLog = r.getRunningOrderLog(orderLog);
                }
                break;
            case BROKEN:
                orderLog.setComplete(true); // to avoid endless calls
                break;
            }
            
            return JOCDefaultResponse.responseStatus200(orderLog);
            
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            EventBus.getInstance().unRegister(this);
        }
    }
    
    @Subscribe({ HistoryOrderLogArrived.class })
    public void createHistoryOrderEvent(HistoryOrderLogArrived evt) {
        LOGGER.debug("orderlog event received with historyId '" + evt.getHistoryOrderId() + "', expected historyId '" + historyId + "'");
        if (historyId != null && historyId.longValue() == evt.getHistoryOrderId()) {
            eventArrived.set(true);
            complete.set(evt.getComplete() == Boolean.TRUE);
            signalEvent();
        }
    }
    
    private void waitingForEvents(long maxDelay) {
        try {
            if (condition != null && lock.tryLock(200L, TimeUnit.MILLISECONDS)) { // with timeout
                try {
                    LOGGER.debug("waitingForEvents: await " + condition.hashCode());
                    condition.await(maxDelay, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e1) {
                } finally {
                    try {
                        lock.unlock();
                    } catch (IllegalMonitorStateException e) {
                        LOGGER.warn("IllegalMonitorStateException at unlock lock after await");
                    }
                }
            }
        } catch (InterruptedException e) {
        }
    }
    
    private synchronized void signalEvent() {
        try {
            LOGGER.debug("signalEvent: " + (condition != null));
            if (condition != null && lock.tryLock(2L, TimeUnit.SECONDS)) { // with timeout
                try {
                    LOGGER.debug("signalEvent: signalAll" + condition.hashCode());
                    condition.signalAll();
                } finally {
                    try {
                        lock.unlock();
                    } catch (IllegalMonitorStateException e) {
                        LOGGER.warn("IllegalMonitorStateException at unlock lock after signal");
                    }
                }
            } else {
                LOGGER.warn("signalEvent failed"); 
            }
        } catch (InterruptedException e) {
            LOGGER.warn("signalEvent: " + e.toString());
        }
    }

}
