package com.sos.joc.task.impl;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Path;
import javax.ws.rs.core.StreamingOutput;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.LogTaskContent;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.job.TaskFilter;
import com.sos.joc.task.resource.ITaskLogResource;
import com.sos.schema.JsonValidator;

@Path("task")
public class TaskLogResourceImpl extends JOCResourceImpl implements ITaskLogResource {

    private static final String API_CALL_LOG = "./task/log";
    private static final String API_CALL_ROLLING = "./task/log/rolling";
    private static final String API_CALL_DOWNLOAD = "./task/log/download";

    @Override
    public JOCDefaultResponse postTaskLog(String accessToken, byte[] filterBytes) {
        return execute(API_CALL_LOG, accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse postRollingTaskLog(String accessToken, byte[] filterBytes) {
        return execute(API_CALL_ROLLING, accessToken, filterBytes);
    }

    @Override
    public JOCDefaultResponse downloadTaskLog(String accessToken, String queryAccessToken, String jobschedulerId, Long taskId) {
        if (accessToken == null) {
            accessToken = queryAccessToken;
        }
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if (jobschedulerId != null) {
            builder.add("jobschedulerId", jobschedulerId);
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
            JsonValidator.validateFailFast(filterBytes, TaskFilter.class);
            TaskFilter taskFilter = Globals.objectMapper.readValue(filterBytes, TaskFilter.class);

            JOCDefaultResponse jocDefaultResponse = init(apiCall, taskFilter, accessToken, taskFilter.getJobschedulerId(), getPermissonsJocCockpit(
                    taskFilter.getJobschedulerId(), accessToken).getJob().getView().isTaskLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            checkRequiredParameter("taskId", taskFilter.getTaskId());
            if (API_CALL_ROLLING.equals(apiCall)) {
                checkRequiredParameter("eventId", taskFilter.getEventId());
            } else {
                taskFilter.setEventId(null);
            }
            LogTaskContent logTaskContent = new LogTaskContent(taskFilter);
            StreamingOutput stream = logTaskContent.getStreamOutput();
            if (API_CALL_DOWNLOAD.equals(apiCall)) {
                return JOCDefaultResponse.responseOctetStreamDownloadStatus200(stream, String.format("sos-%s-%d.task.log", URLEncoder.encode(
                        logTaskContent.getJobName(), StandardCharsets.UTF_8.name()), taskFilter.getTaskId()), logTaskContent.getUnCompressedLength());
            } else {
                return JOCDefaultResponse.responsePlainStatus200(stream, logTaskContent.getUnCompressedLength(), logTaskContent.isComplete());
            }

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
