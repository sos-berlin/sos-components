package com.sos.joc.publish.impl;

import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
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

@Path("inventory/deployment")
public class RedeployImpl extends JOCResourceImpl implements IRedeploy {

    private static final String API_CALL = "./inventory/deployment/redeploy";
    private DBLayerDeploy dbLayer = null;

    @Override
    public JOCDefaultResponse postRedeploy(String xAccessToken, byte[] filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validateFailFast(filter, RedeployFilter.class);
            RedeployFilter redeployFilter = Globals.objectMapper.readValue(filter, RedeployFilter.class);

            JOCDefaultResponse jocDefaultResponse = 
                    initPermissions("", getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String account = Globals.getConfigurationGlobalsJoc().getDefaultProfileAccount().getValue();
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            String controllerId = redeployFilter.getControllerId();
            // get all latest active history objects from the database for the provided controllerId and folder from the filter
            List<DBItemDeploymentHistory> latest = dbLayer.getLatestDepHistoryItemsFromFolder(redeployFilter.getFolder(), controllerId, 
                    redeployFilter.getRecursive());
            // all items will be resigned with a new commitId
            final String commitId = UUID.randomUUID().toString();
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.LOW);

            Set<DBItemDeploymentHistory> unsignedRedeployables = null;
            if (latest != null) {
                unsignedRedeployables = new HashSet<DBItemDeploymentHistory>(latest);
            }
            // preparations
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames = new HashSet<UpdateableWorkflowJobAgentName>();
            Set<UpdateableFileOrderSourceAgentName> updateableAgentNamesFileOrderSources = new HashSet<UpdateableFileOrderSourceAgentName>();
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedRedeployables = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();

            if (unsignedRedeployables != null && !unsignedRedeployables.isEmpty()) {
                PublishUtils.updatePathWithNameInContent(unsignedRedeployables);
                unsignedRedeployables.stream().filter(item -> ConfigurationType.WORKFLOW.equals(ConfigurationType.fromValue(item.getType()))).forEach(
                        item -> updateableAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(item, controllerId, dbLayer)));
                unsignedRedeployables.stream().filter(item -> ConfigurationType.WORKFLOW.equals(ConfigurationType.fromValue(item.getType()))).forEach(
                        item -> updateableAgentNamesFileOrderSources.add(PublishUtils.getUpdateableAgentRefInFileOrderSource(item, controllerId, dbLayer)));

                verifiedRedeployables.putAll(PublishUtils.getDeploymentsWithSignature(commitId, account, unsignedRedeployables, hibernateSession,
                        JocSecurityLevel.MEDIUM));
            }
            if (verifiedRedeployables != null && !verifiedRedeployables.isEmpty()) {
                SignedItemsSpec spec = new SignedItemsSpec(keyPair, null, verifiedRedeployables, null, null);
                StoreDeployments.storeNewDepHistoryEntriesForRedeploy(spec, account, commitId, controllerId, getAccessToken(), 
                        getJocError(), dbLayer);
                // call updateItems command via ControllerApi for given controllers
                SignedItemsSpec signedItemsSpec = new SignedItemsSpec(keyPair, null, verifiedRedeployables, null, null);
                StoreDeployments.callUpdateItemsFor(dbLayer, signedItemsSpec, account, commitId, controllerId, getAccessToken(), getJocError(), API_CALL);
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