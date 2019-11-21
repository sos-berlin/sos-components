package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceCluster;
import com.sos.joc.model.common.JobSchedulerId;
import com.sos.joc.model.jobscheduler.Cluster;
import com.sos.joc.model.jobscheduler.ClusterType;
import com.sos.joc.model.jobscheduler.Clusters;

@Path("jobscheduler")
public class JobSchedulerResourceClusterImpl extends JOCResourceImpl implements IJobSchedulerResourceCluster {

    private static final String API_CALL = "./jobscheduler/cluster";

    @Override
    public JOCDefaultResponse postJobschedulerCluster(String accessToken, JobSchedulerId jobSchedulerFilter) {
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, jobSchedulerFilter, accessToken, jobSchedulerFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(jobSchedulerFilter.getJobschedulerId(), accessToken).getJobschedulerMasterCluster().getView().isStatus()
                            || getPermissonsJocCockpit(jobSchedulerFilter.getJobschedulerId(), accessToken).getJobschedulerMaster().getView()
                                    .isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            Cluster cluster = new Cluster();
            cluster.setJobschedulerId(jobSchedulerFilter.getJobschedulerId());
            cluster.setSurveyDate(Date.from(Instant.now()));
            if (dbItemInventoryInstance.getIsCluster()) {
                cluster.set_type(ClusterType.PASSIVE);
            } else {
                cluster.set_type(ClusterType.STANDALONE);
            }
            Clusters entity = new Clusters();
            entity.setCluster(cluster);
            entity.setDeliveryDate(Date.from(Instant.now()));

            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }
}
