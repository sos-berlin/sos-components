package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.Path;

import com.sos.jobscheduler.model.cluster.ClusterState;
import com.sos.jobscheduler.model.cluster.ClusterType;
import com.sos.jobscheduler.model.cluster.IdToUri;
import com.sos.jobscheduler.model.command.ClusterAppointNodes;
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
import js7.base.web.Uri;
import js7.data.cluster.ClusterSetting.Watch;
import js7.data.node.NodeId;

@Path("jobscheduler")
public class JobSchedulerResourceModifyJobSchedulerClusterImpl extends JOCResourceImpl implements IJobSchedulerResourceModifyJobSchedulerCluster {

    private static String API_CALL_SWITCHOVER = "./jobscheduler/cluster/switchover";
    private static String API_CALL_APPOINT_NODES = "./jobscheduler/cluster/switchover";

    @Override
    public JOCDefaultResponse postJobschedulerSwitchOver(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_SWITCHOVER, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter urlParameter = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);

            boolean permission = getPermissonsJocCockpit(urlParameter.getJobschedulerId(), accessToken).getJS7ControllerCluster().getExecute()
                    .isSwitchOver();

            JOCDefaultResponse jocDefaultResponse = initPermissions(urlParameter.getJobschedulerId(), permission);
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
            if (clusterState == null || !ClusterType.COUPLED.equals(clusterState.getTYPE())) {
                throw new JobSchedulerBadRequestException("Switchover not available because the cluster is not coupled");
            }

            // ask for active node is not necessary with ControllerApi
//            try {
//                Either<Problem, String> response = ControllerApi.of(urlParameter.getJobschedulerId()).executeCommandJson(Globals.objectMapper
//                        .writeValueAsString(new ClusterSwitchOver())).get(Globals.httpSocketTimeout, TimeUnit.MILLISECONDS);
//                ProblemHelper.throwProblemIfExist(response);
//            } catch (TimeoutException e) {
//            }
            ControllerApi.of(urlParameter.getJobschedulerId()).executeCommandJson(Globals.objectMapper.writeValueAsString(new ClusterSwitchOver()))
                    .thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, urlParameter.getJobschedulerId()));

            storeAuditLogEntry(jobschedulerAudit);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse postJobschedulerAppointNodes(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_APPOINT_NODES, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter urlParameter = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            // TODO permissions
            boolean permission = getPermissonsJocCockpit(urlParameter.getJobschedulerId(), accessToken).getJS7ControllerCluster().getExecute()
                    .isSwitchOver();

            JOCDefaultResponse jocDefaultResponse = initPermissions(urlParameter.getJobschedulerId(), permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredComment(urlParameter.getAuditLog());
            ModifyJobSchedulerClusterAudit jobschedulerAudit = new ModifyJobSchedulerClusterAudit(urlParameter);
            logAuditMessage(jobschedulerAudit);

            // ask for cluster
            List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(urlParameter.getJobschedulerId());
            if (controllerInstances == null || controllerInstances.size() < 2) { // is not cluster
                throw new JobSchedulerBadRequestException("There is no cluster configured with the Id: " + urlParameter.getJobschedulerId());
            }

            ClusterAppointNodes command = new ClusterAppointNodes();
            command.setActiveId("Primary");
            IdToUri idToUri = new IdToUri();
            for (DBItemInventoryJSInstance inst : controllerInstances) {
                idToUri.getAdditionalProperties().put(inst.getIsPrimary() ? "Primary" : "StandBy", inst.getClusterUri());
            }
            command.setIdToUri(idToUri);
            command.setClusterWatches(null); // TODO Clusterwatcher from DB
            //ControllerApi.of(urlParameter.getJobschedulerId()).clusterAppointNodes(idToUri, activeId, clusterWatches)
            ControllerApi.of(urlParameter.getJobschedulerId()).executeCommandJson(Globals.objectMapper.writeValueAsString(command)).thenAccept(
                    e -> ProblemHelper.postProblemEventIfExist(e, urlParameter.getJobschedulerId()));
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
