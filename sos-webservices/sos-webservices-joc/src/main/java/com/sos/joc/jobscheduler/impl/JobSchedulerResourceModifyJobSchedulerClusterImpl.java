package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.jobscheduler.model.cluster.ClusterState;
import com.sos.jobscheduler.model.cluster.ClusterType;
import com.sos.jobscheduler.model.command.ClusterSwitchOver;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ModifyJobSchedulerClusterAudit;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceModifyJobSchedulerCluster;
import com.sos.joc.model.jobscheduler.UrlParameter;
import com.sos.schema.JsonValidator;

@Path("jobscheduler")
public class JobSchedulerResourceModifyJobSchedulerClusterImpl extends JOCResourceImpl implements IJobSchedulerResourceModifyJobSchedulerCluster {

    private static String API_CALL = "./jobscheduler/cluster/switchover";

    @Override
    public JOCDefaultResponse postJobschedulerSwitchOver(String accessToken, byte[] filterBytes) {
        try {
            JsonValidator.validateFailFast(filterBytes, UrlParameter.class);
            UrlParameter urlParameter = Globals.objectMapper.readValue(filterBytes, UrlParameter.class);
            
            // TODO permission
            boolean permission = getPermissonsJocCockpit(urlParameter.getJobschedulerId(), accessToken).getJobschedulerMasterCluster().getExecute()
                    .isTerminateFailSafe();

            JOCDefaultResponse jocDefaultResponse = init(API_CALL, urlParameter, accessToken, urlParameter.getJobschedulerId(), permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            checkRequiredComment(urlParameter.getAuditLog());
            ModifyJobSchedulerClusterAudit jobschedulerAudit = new ModifyJobSchedulerClusterAudit(urlParameter);
            logAuditMessage(jobschedulerAudit);

            // Ask for cluster
            if (!dbItemInventoryInstance.getIsCluster()) {
                throw new JobSchedulerBadRequestException("There is no cluster with the Id: " + urlParameter.getJobschedulerId());
            }

            JOCJsonCommand jocJsonCommand = new JOCJsonCommand(this);
            jocJsonCommand.setAutoCloseHttpClient(false);
            jocJsonCommand.setUriBuilderForCluster();
            ClusterState clusterState = jocJsonCommand.getJsonObjectFromGet(ClusterState.class);
            // TODO usage of HTTP 400 responses
            /* HTTP 400 response if node inactive
             * {
             *   "TYPE": "Problem",
             *   "message": "This cluster node is not (yet) active",
             *   "code": "ClusterNodeIsNotActive"
             * }
             * HTTP 400 response if cluster not coupled ("code" missing)
             * {
             *   "TYPE": "Problem",
             *   "message": "Switchover is possible only for the active cluster node, but cluster state is: PassiveLost(Primary=http://master-2-0-primary:4444 active, Backup=http://master-2-0-backup:4444)"
             * }
             */

            // ask for coupled
            if (clusterState == null || clusterState.getTYPE() != ClusterType.COUPLED) {
                throw new JobSchedulerBadRequestException("Switchover not available, because the cluster is not coupled");
            }

            // ask for active node
            String activeClusterUri = clusterState.getIdToUri().getAdditionalProperties().get(clusterState.getActiveId());
            boolean isActive = activeClusterUri.equalsIgnoreCase(dbItemInventoryInstance.getClusterUri()) || activeClusterUri.equalsIgnoreCase(
                    dbItemInventoryInstance.getUri());

            String body = new ObjectMapper().writeValueAsString(new ClusterSwitchOver());
            if (isActive) {
                jocJsonCommand.setUriBuilderForCommands();
                jocJsonCommand.getJsonObjectFromPost(body);
                // TODO expected answer 200 { "TYPE": "Accepted" }
                jocJsonCommand.closeHttpClient();
            } else {
                jocJsonCommand.closeHttpClient();
                SOSHibernateSession connection = null;
                try {
                    connection = Globals.createSosHibernateStatelessConnection(API_CALL);
                    InventoryInstancesDBLayer instanceLayer = new InventoryInstancesDBLayer(connection);
                    DBItemInventoryJSInstance dbInstance = instanceLayer.getOtherClusterMember(urlParameter.getJobschedulerId(), dbItemInventoryInstance
                            .getUri());
                    isActive = activeClusterUri.equalsIgnoreCase(dbInstance.getClusterUri()) || activeClusterUri.equalsIgnoreCase(dbInstance
                            .getUri());
                    
                    if (isActive) {
                        jocJsonCommand.setUriBuilderForCommands(dbInstance.getUri());
                        jocJsonCommand.getJsonObjectFromPost(body);
                        // TODO expected answer 200 { "TYPE": "Accepted" }
                    } else {
                        throw new JobSchedulerBadRequestException("There is no active cluster node.");
                    }
                } finally {
                    Globals.disconnect(connection);
                }
            }

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
