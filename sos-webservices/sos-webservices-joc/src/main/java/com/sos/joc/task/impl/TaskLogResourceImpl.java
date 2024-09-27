package com.sos.joc.task.impl;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.logs.LogTaskContent;
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

import jakarta.ws.rs.Path;

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

            JOCDefaultResponse jocDefaultResponse = initPermissions(taskLog.getControllerId(), getControllerPermissions(taskLog.getControllerId(),
                    accessToken).getOrders().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Long started = Instant.now().toEpochMilli();

            taskId = taskLog.getTaskId();
            taskLog.setComplete(false);
            taskLog.setLog(null);

            RunningTaskLogs r = RunningTaskLogs.getInstance();
            if (r.isBeforeLastLogAPICall(accessToken, taskId, taskLog.getEventId(), started, "start")) {
                disable(taskLog);
            } else {
                RunningTaskLogs.Mode mode = r.hasEvents(accessToken, taskLog.getEventId(), taskId);
                if (LOGGER.isDebugEnabled()) {
                    LOGGER.debug("[postRollingTaskLog][taskId=" + taskId + "][eventId=" + taskLog.getEventId() + "]mode=" + mode.name());
                }

                switch (mode) {
                case TRUE:
                    try {
                        TimeUnit.MILLISECONDS.sleep(500);
                    } catch (InterruptedException e1) {
                    }
                case COMPLETE:
                    taskLog = r.getRunningTaskLog(accessToken, taskLog);
                    if (r.isBeforeLastLogAPICall(accessToken, taskId, taskLog.getEventId(), started, "mode=complete")) {
                        disable(taskLog);
                    } else {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug("  [after]eventId=" + taskLog.getEventId() + ",complete=" + taskLog.getComplete());
                        }
                    }
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
                        taskLog = r.getRunningTaskLog(accessToken, taskLog);
                        // additional check due to waiting time and possibly changed taskLog.getEventId()
                        if (r.isBeforeLastLogAPICall(accessToken, taskId, taskLog.getEventId(), started, "mode=false")) {
                            disable(taskLog);
                        } else {
                            if (LOGGER.isDebugEnabled()) {
                                LOGGER.debug("  [after]eventId=" + taskLog.getEventId() + ",complete=" + taskLog.getComplete());
                            }
                        }
                    }
                    break;
                case BROKEN:
                    taskLog.setComplete(true); // to avoid endless calls
                    break;
                }
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
        // LOGGER.debug("tasklog event received with taskId '" + evt.getHistoryOrderStepId() + "', expected taskId '" + taskId + "'");
        if (taskId != null && taskId.longValue() == evt.getHistoryOrderStepId()) {
            eventArrived.set(true);
            complete.set(evt.getComplete() == Boolean.TRUE);
            signalEvent();
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

            LogTaskContent logTaskContent = new LogTaskContent(taskFilter, folderPermissions, accessToken);
            switch (apiCall) {
            case API_CALL_LOG:
                RunningTaskLogs.getInstance().registerLastLogAPICall(accessToken, taskFilter.getTaskId());
                return JOCDefaultResponse.responsePlainStatus200(logTaskContent.getStreamOutput(false), logTaskContent.getHeaders());
            default:  // API_CALL_DOWNLOAD:
                return JOCDefaultResponse.responseOctetStreamDownloadStatus200(logTaskContent.getStreamOutput(true), logTaskContent
                        .getDownloadFilename(), logTaskContent.getUnCompressedLength());
            }
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private synchronized void signalEvent() {
        try {
            // LOGGER.debug("signalEvent: " + (condition != null));
            if (condition != null && lock.tryLock(2L, TimeUnit.SECONDS)) { // with timeout
                try {
                    // LOGGER.debug("signalEvent: signalAll" + condition.hashCode());
                    condition.signalAll();
                } finally {
                    try {
                        lock.unlock();
                    } catch (IllegalMonitorStateException e) {
                        LOGGER.info("IllegalMonitorStateException at unlock lock after signal");
                    }
                }
            } else {
                LOGGER.info("signalEvent failed");
            }
        } catch (InterruptedException e) {
            LOGGER.info("signalEvent: " + e.toString());
        }
    }

    private void waitingForEvents(long maxDelay) {
        try {
            if (condition != null && lock.tryLock(200L, TimeUnit.MILLISECONDS)) { // with timeout
                try {
                    // LOGGER.debug("waitingForEvents: await " + condition.hashCode());
                    condition.await(maxDelay, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e1) {
                } finally {
                    try {
                        lock.unlock();
                    } catch (IllegalMonitorStateException e) {
                        LOGGER.info("IllegalMonitorStateException at unlock lock after await");
                    }
                }
            }
        } catch (InterruptedException e) {
        }
    }

    private void disable(RunningTaskLog taskLog) {
        taskLog.setComplete(true);
        taskLog.setLog(null);
    }
}
