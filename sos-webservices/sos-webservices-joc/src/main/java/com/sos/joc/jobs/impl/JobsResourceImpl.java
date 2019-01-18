package com.sos.joc.jobs.impl;

import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobs.resource.IJobsResource;
import com.sos.joc.model.job.JobsFilter;
import com.sos.joc.model.job.JobsV;

@Path("jobs")
public class JobsResourceImpl extends JOCResourceImpl implements IJobsResource {

    private static final String API_CALL = "./jobs";

    @Override
    public JOCDefaultResponse postJobs(String xAccessToken, String accessToken, JobsFilter jobsFilter) throws Exception {
        return postJobs(getAccessToken(xAccessToken, accessToken), jobsFilter);
    }

    public JOCDefaultResponse postJobs(String accessToken, JobsFilter jobsFilter) throws Exception {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobsFilter, accessToken, jobsFilter.getJobschedulerId(), getPermissonsJocCockpit(
                    jobsFilter.getJobschedulerId(), accessToken).getJob().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JobsV entity = new JobsV();
            entity.setJobs(null);
            entity.setDeliveryDate(new Date());

            return JOCDefaultResponse.responseStatus200(entity);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}