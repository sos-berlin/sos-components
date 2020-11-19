package com.sos.joc.jobscheduler.impl;

import java.net.URI;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.jobscheduler.model.cluster.ClusterState;
import com.sos.jobscheduler.model.cluster.ClusterType;
import com.sos.jobscheduler.model.cluster.ClusterWatcher;
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
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceModifyJobSchedulerCluster;
import com.sos.joc.model.jobscheduler.UrlParameter;
import com.sos.schema.JsonValidator;

@Path("controller")
public class JobSchedulerResourceModifyJobSchedulerClusterImpl extends JOCResourceImpl implements IJobSchedulerResourceModifyJobSchedulerCluster {

    private static String API_CALL_SWITCHOVER = "./controller/cluster/switchover";
    private static String API_CALL_APPOINT_NODES = "./controller/cluster/appoint_nodes";

    @Override
    public JOCDefaultResponse postJobschedulerSwitchOver(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_SWITCHOVER, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter urlParameter = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);

            boolean permission = getPermissonsJocCockpit(urlParameter.getControllerId(), accessToken).getJS7ControllerCluster().getExecute()
                    .isSwitchOver();

            JOCDefaultResponse jocDefaultResponse = initPermissions(urlParameter.getControllerId(), permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredComment(urlParameter.getAuditLog());
            ModifyJobSchedulerClusterAudit jobschedulerAudit = new ModifyJobSchedulerClusterAudit(urlParameter);
            logAuditMessage(jobschedulerAudit);

            // ask for cluster
            List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(urlParameter.getControllerId());
            if (controllerInstances == null || controllerInstances.size() < 2) { // is not cluster
                throw new JobSchedulerBadRequestException("There is no cluster with the Id: " + urlParameter.getControllerId());
            }

            ClusterState clusterState = Globals.objectMapper.readValue(Proxy.of(urlParameter.getControllerId()).currentState().clusterState()
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
            ControllerApi.of(urlParameter.getControllerId()).executeCommandJson(Globals.objectMapper.writeValueAsString(new ClusterSwitchOver()))
                    .thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, getJocError(), urlParameter.getControllerId()));

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
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_APPOINT_NODES, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter urlParameter = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            // TODO permissions
            boolean permission = true; //getPermissonsJocCockpit(urlParameter.getControllerId(), accessToken).getJS7ControllerCluster().getExecute()
//                    .isSwitchOver();

            JOCDefaultResponse jocDefaultResponse = initPermissions(urlParameter.getControllerId(), permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredComment(urlParameter.getAuditLog());
            ModifyJobSchedulerClusterAudit jobschedulerAudit = new ModifyJobSchedulerClusterAudit(urlParameter);
            logAuditMessage(jobschedulerAudit);
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_APPOINT_NODES);
            appointNodes(urlParameter.getControllerId(), new InventoryAgentInstancesDBLayer(connection), getJocError());
            storeAuditLogEntry(jobschedulerAudit);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }
    
    public static void appointNodes(String controllerId, InventoryAgentInstancesDBLayer dbLayer, JocError jocError) throws DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            JobSchedulerConnectionRefusedException, JsonProcessingException, JobSchedulerBadRequestException {
        // ask for cluster
        List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(controllerId);
        if (controllerInstances == null || controllerInstances.size() < 2) { // is not cluster
            throw new JobSchedulerBadRequestException("There is no cluster configured with the Id: " + controllerId);
        }
        ClusterAppointNodes command = new ClusterAppointNodes();
        command.setActiveId("Primary");
        IdToUri idToUri = new IdToUri();
        for (DBItemInventoryJSInstance inst : controllerInstances) {
            idToUri.getAdditionalProperties().put(inst.getIsPrimary() ? "Primary" : "Standby", inst.getClusterUri());
        }
        command.setIdToUri(idToUri);
        List<DBItemInventoryAgentInstance> watchers = dbLayer.getEnabledClusterWatcherByControllerId(controllerId);
        if (watchers == null || watchers.isEmpty()) {
            throw new JobSchedulerBadRequestException("There must exist at least one Agent Cluster Watcher");
        }
        List<ClusterWatcher> cWatchers = watchers.stream().map(item -> {
            ClusterWatcher watcher = new ClusterWatcher();
            watcher.setUri(URI.create(item.getUri()));
            return watcher;
        }).distinct().collect(Collectors.toList());
        command.setClusterWatches(cWatchers);
        ControllerApi.of(controllerId).executeCommandJson(Globals.objectMapper.writeValueAsString(command)).thenAccept(e -> ProblemHelper
                .postProblemEventIfExist(e, jocError, controllerId));
    }

}
