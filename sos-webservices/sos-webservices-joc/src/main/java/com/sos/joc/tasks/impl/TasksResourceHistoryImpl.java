package com.sos.joc.tasks.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.TaskHistory;
import com.sos.joc.tasks.resource.ITasksResourceHistory;

@Path("tasks")
public class TasksResourceHistoryImpl extends JOCResourceImpl implements ITasksResourceHistory {

    private static final String API_CALL = "./tasks/history";

    @Override
    public JOCDefaultResponse postTasksHistory(String xAccessToken, String accessToken, JobsFilter jobsFilter) throws Exception {
        return postTasksHistory(getAccessToken(xAccessToken, accessToken), jobsFilter);
    }

    public JOCDefaultResponse postTasksHistory(String accessToken, JobsFilter jobsFilter) throws Exception {
        try {
            if (jobsFilter.getJobschedulerId() == null) {
                jobsFilter.setJobschedulerId("");
            }
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobsFilter, accessToken, jobsFilter.getJobschedulerId(), getPermissonsJocCockpit(
                    jobsFilter.getJobschedulerId(), accessToken).getHistory().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            TaskHistory entity = new TaskHistory();
            entity.setDeliveryDate(new Date());
            entity.setHistory(null);

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
