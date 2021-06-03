package com.sos.joc.task.impl;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.LogTaskContent;
import com.sos.joc.classes.logs.RunningTaskLogs;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.job.RunningTaskLog;
import com.sos.joc.model.job.RunningTaskLogFilter;
import com.sos.joc.model.job.TaskFilter;
import com.sos.joc.task.resource.ITaskLogResource;
import com.sos.schema.JsonValidator;

@Path("task")
public class TaskLogResourceImpl extends JOCResourceImpl implements ITaskLogResource {

    private static final String API_CALL_LOG = "./task/log";
    private static final String API_CALL_RUNNING = "./task/log/running";
    private static final String API_CALL_DOWNLOAD = "./task/log/download";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mm:ss.SSSZ");

    @Override
    public JOCDefaultResponse postTaskLog(String accessToken, byte[] filterBytes) {
        return execute(API_CALL_LOG, accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse postRollingTaskLog(String accessToken, byte[] filterBytes) {
//        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL_RUNNING, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, RunningTaskLogFilter.class);
            RunningTaskLog taskLog = Globals.objectMapper.readValue(filterBytes, RunningTaskLog.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(taskLog.getControllerId(), getControllerPermissions(taskLog
                    .getControllerId(), accessToken).getOrders().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
//            session = Globals.createSosHibernateStatelessConnection(API_CALL_RUNNING);
//            if (taskLogs.getTasks().size() == 1) {
//                Long historyId = taskLogs.getTasks().get(0).getTaskId();
//                DBItemHistoryOrderStep historyOrderStepItem = session.get(DBItemHistoryOrderStep.class, historyId);
//                if (historyOrderStepItem == null) {
//                    throw new DBMissingDataException(String.format("Task (Id:%d) not found", historyId));
//                }
//                
//                historyOrderStepItem.getHistoryOrderId();
//                historyOrderStepItem.getOrderId()
//            }
            
//            CompletableFuture.supplyAsync(() -> {
//                Set<RunningTaskLog> s = null;
//                return s;
//            }).get(1, TimeUnit.MINUTES);
            
            RunningTaskLogs r = RunningTaskLogs.getInstance();
            RunningTaskLogs.Mode mode = r.hasEvents(taskLog.getEventId(), taskLog.getTaskId());
            if (RunningTaskLogs.Mode.IMMEDIATLY.equals(mode)) {
                taskLog = r.getRunningTaskLog(taskLog);
            }
            

            // TODO callables in several threads
            // Fake
//            RunningTaskLogs logs = new RunningTaskLogs();
//            List<RunningTaskLog> runningTasks = new ArrayList<RunningTaskLog>();
            TimeUnit.MINUTES.sleep(1);
            String message = ZonedDateTime.now().format(formatter) + " [INFO] Running log is not yet completly implemented";
            taskLog.setComplete(false);
            //taskLog.setEventId(null);
            taskLog.setLog(message);
//            for (RunningTaskLog runningTaskLog : taskLogs.getTasks()) {
//                runningTaskLog.setComplete(true);
//                runningTaskLog.setEventId(null);
//                runningTaskLog.setLog(message);
//                //runningTaskLog.setTaskId(taskId);
//                //runningTasks.add(runningTaskLog);
//            }
            //logs.setTasks(runningTasks);
            return JOCDefaultResponse.responseStatus200(taskLog);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
//        } finally {
//            Globals.disconnect(session);
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
