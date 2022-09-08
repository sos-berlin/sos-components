package com.sos.joc.publish.impl;

import java.io.IOException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.common.SyncStateHelper;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.RedeployFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.SignedItemsSpec;
import com.sos.joc.publish.mapper.UpdateableFileOrderSourceAgentName;
import com.sos.joc.publish.mapper.UpdateableWorkflowJobAgentName;
import com.sos.joc.publish.resource.IRedeploy;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.joc.publish.util.StoreDeployments;
import com.sos.schema.JsonValidator;
import com.sos.sign.model.fileordersource.FileOrderSource;

import js7.data_for_java.controller.JControllerState;

@Path("inventory/deployment")
public class RedeployImpl extends JOCResourceImpl implements IRedeploy {

    private static final String API_CALL_REDEPLOY = "./inventory/deployment/redeploy";
    private static final String API_CALL_SYNC = "./inventory/deployment/synchronize";
    
    @Override
    public JOCDefaultResponse postRedeploy(String xAccessToken, byte[] filter) {
        return deploy(xAccessToken, filter, API_CALL_REDEPLOY);
    }
    
    @Override
    public JOCDefaultResponse postSync(String xAccessToken, byte[] filter) {
        return deploy(xAccessToken, filter, API_CALL_SYNC);
    }

    public JOCDefaultResponse deploy(String xAccessToken, byte[] filter, String action) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(action, filter, xAccessToken);
            JsonValidator.validateFailFast(filter, RedeployFilter.class);
            RedeployFilter redeployFilter = Globals.objectMapper.readValue(filter, RedeployFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getInventory().getDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            DBItemJocAuditLog dbAuditlog = storeAuditLog(redeployFilter.getAuditLog(), CategoryType.DEPLOYMENT);

            String account = ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());
            hibernateSession = Globals.createSosHibernateStatelessConnection(action);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            String controllerId = redeployFilter.getControllerId();
            // get all latest active history objects from the database for the provided controllerId and folder from the filter
            List<DBItemDeploymentHistory> latest = dbLayer.getLatestDepHistoryItemsFromFolder(redeployFilter.getFolder(), controllerId, 
                    redeployFilter.getRecursive());
            
            // all items will be resigned with a new commitId
            final String commitId = UUID.randomUUID().toString();
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.LOW);

            List<DBItemDeploymentHistory> unsignedRedeployables = null;
            if (latest != null) {
                Stream<DBItemDeploymentHistory> latestStream = latest.stream().filter(item -> OperationType.DELETE.value() != item.getOperation());
                if (API_CALL_SYNC.equals(action)) {
                    // filter latest with only "not in sync" objects
                    final JControllerState currentstate = SyncStateHelper.getControllerState(controllerId, xAccessToken, getJocError());
                    latestStream = latestStream.filter(item -> SyncStateHelper.isNotInSync(currentstate, item.getName(), item.getType()));
                }
                final Map<String, String> releasedScripts = dbLayer.getReleasedScripts();
                unsignedRedeployables = latestStream.peek(item -> {
    				try {
                        item.writeUpdateableContent(JsonConverter.readAsConvertedDeployObject(item.getPath(), item.getInvContent(),
                                StoreDeployments.CLASS_MAPPING.get(item.getType()), commitId, releasedScripts));
					} catch (IOException e) {
						throw new JocException(e);
					}
				}).collect(Collectors.toList());
            }
            // preparations
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames = new HashSet<UpdateableWorkflowJobAgentName>();
            Set<UpdateableFileOrderSourceAgentName> updateableAgentNamesFileOrderSources = new HashSet<UpdateableFileOrderSourceAgentName>();
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedRedeployables = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();

            if (unsignedRedeployables != null && !unsignedRedeployables.isEmpty()) {
//                PublishUtils.updatePathWithNameInContent(unsignedRedeployables);
            	
                unsignedRedeployables.stream().filter(item -> ConfigurationType.WORKFLOW.equals(ConfigurationType.fromValue(item.getType()))).forEach(
                        item -> updateableAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(item, controllerId, dbLayer)));
                unsignedRedeployables.stream().filter(item -> ConfigurationType.FILEORDERSOURCE.equals(ConfigurationType.fromValue(item.getType()))).forEach(
                        item -> { 
                            UpdateableFileOrderSourceAgentName update = PublishUtils.getUpdateableAgentRefInFileOrderSource(item, controllerId, dbLayer);
                        	updateableAgentNamesFileOrderSources.add(update);
                            try {
                                ((FileOrderSource)item.readUpdateableContent()).setAgentPath(update.getAgentId());
                                updateableAgentNamesFileOrderSources.add(update);
                            } catch (Exception e) {}
                        });

                verifiedRedeployables.putAll(PublishUtils.getDraftsWithSignature(
                		commitId, account, unsignedRedeployables, updateableAgentNames, keyPair, controllerId, hibernateSession));
            }
            if (verifiedRedeployables != null && !verifiedRedeployables.isEmpty()) {
                SignedItemsSpec signedItemsSpec = new SignedItemsSpec(keyPair, verifiedRedeployables, updateableAgentNames, updateableAgentNamesFileOrderSources,
                		dbAuditlog.getId());
                StoreDeployments.storeNewDepHistoryEntriesForRedeploy(signedItemsSpec, account, commitId, controllerId, getAccessToken(), getJocError(), dbLayer);
                // call updateItems command via ControllerApi for given controllers
                StoreDeployments.callUpdateItemsFor(dbLayer, signedItemsSpec, account, commitId, controllerId, getAccessToken(), getJocError(), action);
            }
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}