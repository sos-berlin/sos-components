package com.sos.joc.task.impl;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.LogTaskContent;
import com.sos.joc.classes.logs.RunningTaskLogs;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.history.HistoryOrderTaskLogArrived;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.job.RunningTaskLog;
import com.sos.joc.model.job.RunningTaskLogFilter;
import com.sos.joc.model.job.TaskFilter;
import com.sos.joc.task.resource.ITaskLogResource;
import com.sos.schema.JsonValidator;

@Path("task")
public class TaskLogResourceImpl extends JOCResourceImpl implements ITaskLogResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskLogResourceImpl.class);
    private static final String API_CALL_LOG = "./task/log";
    private static final String API_CALL_RUNNING = "./task/log/running";
    private static final String API_CALL_DOWNLOAD = "./task/log/download";
    private Lock lock = new ReentrantLock();
    private Condition condition = null;
    private Long taskId = null;
    private volatile AtomicBoolean complete = new AtomicBoolean(false);
    private volatile AtomicBoolean eventArrived = new AtomicBoolean(false);

    @Override
    public JOCDefaultResponse postTaskLog(String accessToken, byte[] filterBytes) {
        return execute(API_CALL_LOG, accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse postRollingTaskLog(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_RUNNING, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, RunningTaskLogFilter.class);
            RunningTaskLog taskLog = Globals.objectMapper.readValue(filterBytes, RunningTaskLog.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(taskLog.getControllerId(), getControllerPermissions(taskLog
                    .getControllerId(), accessToken).getOrders().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            taskId = taskLog.getTaskId();
            taskLog.setComplete(false);
            taskLog.setLog(null);
            
            RunningTaskLogs r = RunningTaskLogs.getInstance();
            RunningTaskLogs.Mode mode = r.hasEvents(taskLog.getEventId(), taskId);
            LOGGER.debug("taskId '" + taskId + "' has tasklogs: " + mode.name());
            switch (mode) {
            case TRUE:
                try {
                    TimeUnit.MILLISECONDS.sleep(500);
                } catch (InterruptedException e1) {
                }
            case COMPLETE:
                taskLog = r.getRunningTaskLog(taskLog);
                break;
            case FALSE:
                EventBus.getInstance().register(this);
                condition = lock.newCondition();
                waitingForEvents(TimeUnit.MINUTES.toMillis(1));
                if (eventArrived.get()) {
                    if (!complete.get()) {
                        try {
                            TimeUnit.MILLISECONDS.sleep(500);
                        } catch (InterruptedException e1) {
                        }
                    }
                    taskLog = r.getRunningTaskLog(taskLog);
                }
                break;
            }
            
            return JOCDefaultResponse.responseStatus200(taskLog);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            EventBus.getInstance().unRegister(this);
        }
    }
    
    @Subscribe({ HistoryOrderTaskLogArrived.class })
    public void createHistoryTaskEvent(HistoryOrderTaskLogArrived evt) {
        LOGGER.debug("tasklog event received");
        if (taskId != null && evt.getHistoryOrderStepId() == taskId) {
            eventArrived.set(true);
            complete.set(evt.getComplete() == Boolean.TRUE);
            signalEvent();
        }
    }
    
    private void waitingForEvents(long maxDelay) {
        try {
            if (condition != null && lock.tryLock(200L, TimeUnit.MILLISECONDS)) { // with timeout
                try {
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
            if (condition != null && lock.tryLock(2L, TimeUnit.SECONDS)) {
                try {
                    condition.signalAll();
                } finally {
                    try {
                        lock.unlock();
                    } catch (IllegalMonitorStateException e) {
                        LOGGER.warn("IllegalMonitorStateException at unlock lock after signal");
                    }
                }
            }
        } catch (InterruptedException e) {
        }
    }

    @Override
    public JOCDefaultResponse downloadTaskLog(String accessToken, String queryAccessToken, String controllerId, Long taskId) {
        if (accessToken == null) {
            accessToken = queryAccessToken;
        }
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (controllerId != null) {
            builder.add("controllerId", controllerId);
        }
        if (taskId != null) {
            builder.add("taskId", taskId);
        }
        return downloadTaskLog(accessToken, builder.build().toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public JOCDefaultResponse downloadTaskLog(String accessToken, byte[] filterBytes) {
        return execute(API_CALL_DOWNLOAD, accessToken, filterBytes);
    }

    public JOCDefaultResponse execute(String apiCall, String accessToken, byte[] filterBytes) {

        try {
            initLogging(apiCall, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, TaskFilter.class);
            TaskFilter taskFilter = Globals.objectMapper.readValue(filterBytes, TaskFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(taskFilter.getControllerId(), getControllerPermissions(taskFilter
                    .getControllerId(), accessToken).getOrders().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            LogTaskContent logTaskContent = new LogTaskContent(taskFilter, folderPermissions);
            switch (apiCall) {
            case API_CALL_LOG:
                return JOCDefaultResponse.responsePlainStatus200(logTaskContent.getStreamOutput(), logTaskContent.getHeaders());
            default:  // API_CALL_DOWNLOAD:
                return JOCDefaultResponse.responseOctetStreamDownloadStatus200(logTaskContent.getStreamOutput(), logTaskContent.getDownloadFilename(),
                        logTaskContent.getUnCompressedLength());
            }

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
