package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.Path;

import com.sos.jobscheduler.model.cluster.ClusterState;
import com.sos.jobscheduler.model.cluster.ClusterType;
import com.sos.jobscheduler.model.command.ClusterSwitchOver;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.ModifyJobSchedulerClusterAudit;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceModifyJobSchedulerCluster;
import com.sos.joc.model.jobscheduler.UrlParameter;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;

@Path("jobscheduler")
public class JobSchedulerResourceModifyJobSchedulerClusterImpl extends JOCResourceImpl implements IJobSchedulerResourceModifyJobSchedulerCluster {

    private static String API_CALL = "./jobscheduler/cluster/switchover";

    @Override
    public JOCDefaultResponse postJobschedulerSwitchOver(String accessToken, byte[] filterBytes) {
        try {
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter urlParameter = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);

            // TODO permission
            boolean permission = getPermissonsJocCockpit(urlParameter.getJobschedulerId(), accessToken).getJS7ControllerCluster().getExecute()
                    .isTerminateFailSafe();

            JOCDefaultResponse jocDefaultResponse = init(API_CALL, urlParameter, accessToken, urlParameter.getJobschedulerId(), permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredComment(urlParameter.getAuditLog());
            ModifyJobSchedulerClusterAudit jobschedulerAudit = new ModifyJobSchedulerClusterAudit(urlParameter);
            logAuditMessage(jobschedulerAudit);

            // ask for cluster
            List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(urlParameter.getJobschedulerId());
            if (controllerInstances == null || controllerInstances.size() < 2) { // is not cluster
                throw new JobSchedulerBadRequestException("There is no cluster with the Id: " + urlParameter.getJobschedulerId());
            }

            ClusterState clusterState = Globals.objectMapper.readValue(Proxy.of(urlParameter.getJobschedulerId()).currentState().clusterState()
                    .toJson(), ClusterState.class);

            // ask for coupled
            if (clusterState == null || clusterState.getTYPE() != ClusterType.COUPLED) {
                throw new JobSchedulerBadRequestException("Switchover not available, because the cluster is not coupled");
            }

            // ask for active node is not necessary with ControllerApi
            Either<Problem, String> response = ControllerApi.of(urlParameter.getJobschedulerId()).executeCommandJson(Globals.objectMapper
                    .writeValueAsString(new ClusterSwitchOver())).get(Globals.httpSocketTimeout, TimeUnit.MICROSECONDS);
            ProblemHelper.throwProblemIfExist(response);

            storeAuditLogEntry(jobschedulerAudit);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

}
