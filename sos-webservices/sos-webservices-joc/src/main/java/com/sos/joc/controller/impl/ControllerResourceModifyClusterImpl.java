package com.sos.joc.controller.impl;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.cluster.ClusterState;
import com.sos.controller.model.cluster.ClusterType;
import com.sos.controller.model.command.ClusterSwitchOver;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.audit.ModifyJobSchedulerClusterAudit;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.controller.resource.IControllerResourceModifyCluster;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.controller.UrlParameter;
import com.sos.schema.JsonValidator;

import js7.base.web.Uri;
import js7.data.node.NodeId;

@Path("controller")
public class ControllerResourceModifyClusterImpl extends JOCResourceImpl implements IControllerResourceModifyCluster {

    private static String API_CALL_SWITCHOVER = "./controller/cluster/switchover";
    private static String API_CALL_APPOINT_NODES = "./controller/cluster/appoint_nodes";

    @Override
    public JOCDefaultResponse postJobschedulerSwitchOver(String accessToken, byte[] filterBytes) {
        try {
            initLogging(API_CALL_SWITCHOVER, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter urlParameter = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            String controllerId = urlParameter.getControllerId();

            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken)
                    .getSwitchOver());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredComment(urlParameter.getAuditLog());
            ModifyJobSchedulerClusterAudit jobschedulerAudit = new ModifyJobSchedulerClusterAudit(urlParameter);
            logAuditMessage(jobschedulerAudit);

            // ask for cluster
            List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(controllerId);
            if (controllerInstances != null && controllerInstances.size() < 2) { // is not cluster
                throw new JocBadRequestException("There is no cluster with the Id: " + controllerId);
            }

            ClusterState clusterState = Globals.objectMapper.readValue(Proxy.of(controllerId).currentState().clusterState().toJson(),
                    ClusterState.class);

            // ask for coupled
            if (clusterState == null || !ClusterType.COUPLED.equals(clusterState.getTYPE())) {
                throw new JocBadRequestException("Switchover is not available because the cluster is not coupled");
            }

            // ask for active node is not necessary with ControllerApi
//            try {
//                Either<Problem, String> response = ControllerApi.of(controllerId).executeCommandJson(Globals.objectMapper
//                        .writeValueAsString(new ClusterSwitchOver())).get(Globals.httpSocketTimeout, TimeUnit.MILLISECONDS);
//                ProblemHelper.throwProblemIfExist(response);
//            } catch (TimeoutException e) {
//            }
            ControllerApi.of(controllerId).executeCommandJson(Globals.objectMapper.writeValueAsString(new ClusterSwitchOver()))
                    .thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, getAccessToken(), getJocError(), controllerId));

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
            String controllerId = urlParameter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken)
                    .getSwitchOver());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredComment(urlParameter.getAuditLog());
            urlParameter.setWithFailover(null);
            ModifyJobSchedulerClusterAudit jobschedulerAudit = new ModifyJobSchedulerClusterAudit(urlParameter);
            logAuditMessage(jobschedulerAudit);
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_APPOINT_NODES);
            appointNodes(controllerId, new InventoryAgentInstancesDBLayer(connection), accessToken, getJocError());
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
    
    public static void appointNodes(String controllerId, InventoryAgentInstancesDBLayer dbLayer, String accessToken, JocError jocError)
            throws DBMissingDataException, JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ControllerConnectionRefusedException, JsonProcessingException, JocBadRequestException {
        // ask for cluster
        List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(controllerId);
        if (controllerInstances == null || controllerInstances.size() < 2) { // is not cluster
            throw new JocBadRequestException("There is no cluster configured with the Id: " + controllerId);
        }
//        ClusterAppointNodes command = new ClusterAppointNodes();
//        command.setActiveId("Primary");
//        IdToUri idToUri = new IdToUri();
//        for (DBItemInventoryJSInstance inst : controllerInstances) {
//            idToUri.getAdditionalProperties().put(inst.getIsPrimary() ? "Primary" : "Backup", inst.getClusterUri());
//        }
//        command.setIdToUri(idToUri);
//        List<String> watchers = dbLayer.getUrisOfEnabledClusterWatcherByControllerId(controllerId);
//        if (watchers == null || watchers.isEmpty()) {
//            throw new JobSchedulerBadRequestException("There must exist at least one Agent Cluster Watcher");
//        }
//        List<ClusterWatcher> cWatchers = watchers.stream().map(item -> {
//            ClusterWatcher watcher = new ClusterWatcher();
//            watcher.setUri(URI.create(item));
//            return watcher;
//        }).distinct().collect(Collectors.toList());
//        command.setClusterWatches(cWatchers);
//
//        ControllerApi.of(controllerId).executeCommandJson(Globals.objectMapper.writeValueAsString(command)).thenAccept(e -> ProblemHelper
//                .postProblemEventIfExist(e, jocError, controllerId));

        NodeId activeId = NodeId.of("Primary");
        Map<NodeId, Uri> idToUri = new HashMap<>();
        for (DBItemInventoryJSInstance inst : controllerInstances) {
            idToUri.put(inst.getIsPrimary() ? activeId : NodeId.of("Backup"), Uri.of(inst.getClusterUri()));
        }
        ControllerApi.of(controllerId).clusterAppointNodes(idToUri, activeId, Proxies.getClusterWatchers(controllerId, dbLayer)).thenAccept(
                e -> ProblemHelper.postProblemEventIfExist(e, accessToken, jocError, controllerId));
    }

}
