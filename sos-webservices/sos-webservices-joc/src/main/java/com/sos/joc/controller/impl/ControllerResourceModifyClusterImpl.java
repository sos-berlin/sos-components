package com.sos.joc.controller.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.cluster.ClusterState;
import com.sos.controller.model.cluster.ClusterType;
import com.sos.controller.model.command.ClusterSwitchOver;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.ClusterWatch;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.controller.resource.IControllerResourceModifyCluster;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocServiceException;
import com.sos.joc.joc.impl.StateImpl;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.controller.UrlParameter;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("controller")
public class ControllerResourceModifyClusterImpl extends JOCResourceImpl implements IControllerResourceModifyCluster {

    private final static String API_CALL_SWITCHOVER = "./controller/cluster/switchover";
    private final static String API_CALL_APPOINT_NODES = "./controller/cluster/appoint_nodes";
    private final static String API_CALL_CONFIRM_LOSS_NODES = "./controller/cluster/confirm_node_loss";
    
    @Override
    public JOCDefaultResponse postClusterNodeSwitchOver(String accessToken, byte[] filterBytes) {
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
            storeAuditLog(urlParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);

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

            ControllerApi.of(controllerId).executeCommandJson(Globals.objectMapper.writeValueAsString(new ClusterSwitchOver()))
                    .thenAccept(e -> ProblemHelper.postProblemEventIfExist(e, getAccessToken(), getJocError(), controllerId));

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
    @Override
    public JOCDefaultResponse postClusterAppointNodes(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_APPOINT_NODES, filterBytes, accessToken);
            
            if (!StateImpl.isActive(API_CALL_APPOINT_NODES, null)) {
                throw new JocServiceException("Appointing the Controller cluster nodes is possible only in the active JOC node.");
            }
            
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter urlParameter = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            String controllerId = urlParameter.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(controllerId, accessToken)
                    .getSwitchOver());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(urlParameter.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL_APPOINT_NODES);
            ClusterWatch.getInstance().appointNodes(controllerId, ControllerApi.of(controllerId), new InventoryAgentInstancesDBLayer(connection),
                    accessToken, getJocError());

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
    
    @Override
    public JOCDefaultResponse postConfirmClusterNodeLoss(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL_CONFIRM_LOSS_NODES, filterBytes, accessToken);
            
            if (!StateImpl.isActive(API_CALL_CONFIRM_LOSS_NODES, null)) {
                throw new JocServiceException("Confirming the Controller cluster loss node is possible only in the active JOC node.");
            }
            
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter body = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            String controllerId = body.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getControllerPermissions(controllerId, accessToken).getSwitchOver());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            storeAuditLog(body.getAuditLog(), controllerId, CategoryType.CONTROLLER);
            ClusterWatch.getInstance().confirmNodeLoss(controllerId, getAccount());
            
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

}
