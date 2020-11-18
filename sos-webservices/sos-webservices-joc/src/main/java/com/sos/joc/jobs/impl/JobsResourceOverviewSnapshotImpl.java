package com.sos.joc.jobs.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobs.resource.IJobsResourceOverviewSnapshot;
import com.sos.joc.model.common.ControllerId;
import com.sos.joc.model.job.JobsSnapshot;
import com.sos.joc.model.job.JobsSummary;
import com.sos.schema.JsonValidator;

import js7.data.order.Order;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.order.JOrderPredicates;

@Path("jobs")
public class JobsResourceOverviewSnapshotImpl extends JOCResourceImpl implements IJobsResourceOverviewSnapshot {

    private static final String API_CALL = "./jobs/overview/snapshot";

    @Override
    public JOCDefaultResponse postJobsOverviewSnapshot(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ControllerId.class);
            ControllerId jobScheduler = Globals.objectMapper.readValue(filterBytes, ControllerId.class);
            
            JOCDefaultResponse jocDefaultResponse = initPermissions(jobScheduler.getControllerId(), getPermissonsJocCockpit(jobScheduler
                    .getControllerId(), accessToken).getJob().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JobsSummary jobs = new JobsSummary();
            jobs.setPending(0); // TODO setPending from database
            jobs.setRunning(0);
            
            JControllerState controllerState = Proxy.of(jobScheduler.getControllerId()).currentState();
            jobs.setRunning(controllerState.orderStateToCount(JOrderPredicates.byOrderState(Order.Processing$.class)).get(Order.Processing$.class));
            
            JobsSnapshot entity = new JobsSnapshot();
            entity.setSurveyDate(Date.from(Instant.ofEpochMilli(controllerState.eventId() / 1000)));
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
