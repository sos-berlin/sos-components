package com.sos.joc.jobs.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobs.resource.IJobsResourceOverviewSnapshot;
import com.sos.joc.model.common.JobSchedulerId;
import com.sos.joc.model.job.JobsSnapshot;
import com.sos.joc.model.job.JobsSummary;

@Path("jobs")
public class JobsResourceOverviewSnapshotImpl extends JOCResourceImpl implements IJobsResourceOverviewSnapshot {

    private static final String API_CALL = "./jobs/overview/snapshot";

    @Override
    public JOCDefaultResponse postJobsOverviewSnapshot(String accessToken, JobSchedulerId jobScheduler) throws Exception {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobScheduler, accessToken, jobScheduler.getJobschedulerId(),
                    getPermissonsJocCockpit(jobScheduler.getJobschedulerId(), accessToken).getJob().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JobsSummary jobs = new JobsSummary();
            jobs.setPending(0);
            jobs.setRunning(0);
            jobs.setStopped(0);
            jobs.setWaitingForResource(0);
            jobs.setTasks(0);

            JobsSnapshot entity = new JobsSnapshot();
            entity.setSurveyDate(Date.from(Instant.now()));
            entity.setJobs(jobs);
            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(entity);

        } catch (JobSchedulerConnectionResetException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatus434JSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

}
