package com.sos.joc.task.impl;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocNotImplemetedException;
import com.sos.joc.model.common.LogMime;
import com.sos.joc.model.job.TaskFilter;
import com.sos.joc.task.resource.ITaskLogResource;

@Path("task")
public class TaskLogResourceImpl extends JOCResourceImpl implements ITaskLogResource {

    private static final String API_CALL = "./task/log";
    private JocNotImplemetedException err = new JocNotImplemetedException(API_CALL + " is not yet implemented!");

    @Override
    public JOCDefaultResponse postTaskLog(String xAccessToken, String accessToken, TaskFilter taskFilter) throws Exception {
        return postTaskLog(getAccessToken(xAccessToken, accessToken), taskFilter);
    }

    public JOCDefaultResponse postTaskLog(String accessToken, TaskFilter taskFilter) throws Exception {
        return execute(API_CALL, accessToken, taskFilter);
    }

    @Override
    public JOCDefaultResponse getTaskLogHtml(String xAccessToken, String accessToken, String queryAccessToken, String jobschedulerId, String taskId,
            String filename) throws Exception {
        return getTaskLogHtml(getAccessToken(xAccessToken, accessToken), queryAccessToken, jobschedulerId, taskId, filename);
    }

    public JOCDefaultResponse getTaskLogHtml(String accessToken, String queryAccessToken, String jobschedulerId, String taskId, String filename)
            throws Exception {
        TaskFilter taskFilter = setTaskFilter(jobschedulerId, taskId, filename, LogMime.HTML);
        if (accessToken == null) {
            accessToken = queryAccessToken;
        }
        return execute(API_CALL + "/html", accessToken, taskFilter);
    }

    @Override
    public JOCDefaultResponse downloadTaskLog(String xAccessToken, String accessToken, String queryAccessToken, String jobschedulerId, String taskId,
            String filename) throws Exception {
        return downloadTaskLog(getAccessToken(xAccessToken, accessToken), queryAccessToken, jobschedulerId, taskId, filename);
    }

    public JOCDefaultResponse downloadTaskLog(String accessToken, String queryAccessToken, String jobschedulerId, String taskId, String filename)
            throws Exception {
        TaskFilter taskFilter = setTaskFilter(jobschedulerId, taskId, filename, LogMime.PLAIN);
        if (accessToken == null) {
            accessToken = queryAccessToken;
        }
        return downloadTaskLog(accessToken, taskFilter);
    }

    @Override
    public JOCDefaultResponse downloadTaskLog(String xAccessToken, String accessToken, TaskFilter taskFilter) throws Exception {
        return downloadTaskLog(getAccessToken(xAccessToken, accessToken), taskFilter);
    }

    public JOCDefaultResponse downloadTaskLog(String accessToken, TaskFilter taskFilter) throws Exception {
        return execute(API_CALL + "/download", accessToken, taskFilter);
    }

    @Override
    public JOCDefaultResponse getLogInfo(String xAccessToken, String accessToken, TaskFilter taskFilter) throws Exception {
        return getLogInfo(getAccessToken(xAccessToken, accessToken), taskFilter);
    }

    public JOCDefaultResponse getLogInfo(String accessToken, TaskFilter taskFilter) throws Exception {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL + "/info", taskFilter, accessToken, taskFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(taskFilter.getJobschedulerId(), accessToken).getJob().getView().isTaskLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            throw err;

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    public JOCDefaultResponse execute(String apiCall, String accessToken, TaskFilter taskFilter) {

        try {
            JOCDefaultResponse jocDefaultResponse = init(apiCall, taskFilter, accessToken, taskFilter.getJobschedulerId(), getPermissonsJocCockpit(
                    taskFilter.getJobschedulerId(), accessToken).getJob().getView().isTaskLog());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            throw err;

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            if ((API_CALL + "/html").equals(apiCall)) {
                return JOCDefaultResponse.responseHTMLStatusJSError(e);
            } else {
                return JOCDefaultResponse.responseStatusJSError(e);
            }
        } catch (Exception e) {
            if ((API_CALL + "/html").equals(apiCall)) {
                return JOCDefaultResponse.responseHTMLStatusJSError(e, getJocError());
            } else {
                return JOCDefaultResponse.responseStatusJSError(e, getJocError());
            }
        }
    }

    private TaskFilter setTaskFilter(String jobschedulerId, String taskId, String filename, LogMime mime) {
        TaskFilter taskFilter = new TaskFilter();
        taskFilter.setTaskId(taskId);
        taskFilter.setJobschedulerId(jobschedulerId);
        taskFilter.setMime(mime);
        taskFilter.setFilename(filename);
        return taskFilter;
    }

}
