package com.sos.joc.task.impl;

import java.nio.charset.StandardCharsets;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.LogTaskContent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.job.RunningTaskLog;
import com.sos.joc.model.job.RunningTaskLogs;
import com.sos.joc.model.job.RunningTaskLogsFilter;
import com.sos.joc.model.job.TaskFilter;
import com.sos.joc.task.resource.ITaskLogResource;
import com.sos.schema.JsonValidator;

@Path("task")
public class TaskLogResourceImpl extends JOCResourceImpl implements ITaskLogResource {

    private static final String API_CALL_LOG = "./task/log";
    private static final String API_CALL_RUNNING = "./task/log/runnning";
    private static final String API_CALL_DOWNLOAD = "./task/log/download";
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("uuuu-MM-dd' 'HH:mm:ss.SSSZ");

    @Override
    public JOCDefaultResponse postTaskLog(String accessToken, byte[] filterBytes) {
        return execute(API_CALL_LOG, accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse postRollingTaskLog(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_RUNNING, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, RunningTaskLogsFilter.class);
            RunningTaskLogs taskLogs = Globals.objectMapper.readValue(filterBytes, RunningTaskLogs.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(taskLogs.getControllerId(), getPermissonsJocCockpit(taskLogs
                    .getControllerId(), accessToken).getJob().getView().isTaskLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            //checkRequiredParameter("tasks", taskLogs.getTasks());

            // TODO callables in several threads
            // Fake
            //RunningTaskLogs logs = new RunningTaskLogs();
            //List<RunningTaskLog> runningTasks = new ArrayList<RunningTaskLog>();
            String message = ZonedDateTime.now().format(formatter) + " [INFO] Running log is not yet implemented";
            for (RunningTaskLog runningTaskLog : taskLogs.getTasks()) {
                //RunningTaskLog runningTaskLog = new RunningTaskLog();
                runningTaskLog.setComplete(true);
                runningTaskLog.setEventId(null);
                runningTaskLog.setLog(message);
                //runningTasks.add(runningTaskLog);
            }
            //logs.setTasks(runningTasks);
            return JOCDefaultResponse.responseStatus200(taskLogs);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
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

            JOCDefaultResponse jocDefaultResponse = initPermissions(taskFilter.getControllerId(), getPermissonsJocCockpit(taskFilter
                    .getControllerId(), accessToken).getJob().getView().isTaskLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredParameter("taskId", taskFilter.getTaskId());
            LogTaskContent logTaskContent = new LogTaskContent(taskFilter);
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
