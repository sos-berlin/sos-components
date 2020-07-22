package com.sos.joc.publish.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.deployment.DBItemDeployedConfiguration;
import com.sos.joc.db.deployment.DBItemDeployedConfigurationHistory;
import com.sos.joc.db.inventory.deprecated.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.JobSchedulerBadRequestException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.JobSchedulerId;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.JSConfigurationState;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IDeploy;
import com.sos.joc.publish.util.PublishUtils;

@Path("publish")
public class DeployImpl extends JOCResourceImpl implements IDeploy {

    private static final String API_CALL = "./publish/deploy";
    private static final Logger LOGGER = LoggerFactory.getLogger(DeployImpl.class);
    private DBLayerDeploy dbLayer = null;

    @Override
    public JOCDefaultResponse postDeploy(String xAccessToken, DeployFilter deployFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        Boolean deployHasErrors = false;
        Map<String, String> mastersWithDeployErrors = new HashMap<String,String>();
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, deployFilter, xAccessToken, "",
                    /* getPermissonsJocCockpit("", xAccessToken).getPublish().isDeploy() */
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            List<JobSchedulerId> jsMasters = deployFilter.getSchedulers();
            List<String> schedulerIds = new ArrayList<String>();
            schedulerIds.addAll(jsMasters.stream().map(masterId -> masterId.getJobschedulerId()).collect(Collectors.toList()));

            // read all objects provided in the filter from the database
            List<DBItemInventoryConfiguration> toUpdate = dbLayer.getFilteredInventoryConfigurations(deployFilter.getUpdate());
            List<DBItemInventoryConfiguration> toDelete = dbLayer.getFilteredInventoryConfigurations(deployFilter.getDelete());
            List<DBItemInventoryJSInstance> masters = dbLayer.getMasters(schedulerIds);
            JocSecurityLevel jocSecLvl = Globals.getJocSecurityLevel();
            Set<DBItemInventoryConfiguration> signedDrafts = new HashSet<DBItemInventoryConfiguration>();
            signedDrafts.addAll(toUpdate.stream().filter(draft -> draft.getSignedContent() != null && !draft.getSignedContent().isEmpty()).collect(
                    Collectors.toSet()));
            Set<DBItemInventoryConfiguration> unsignedDrafts = new HashSet<DBItemInventoryConfiguration>();
            unsignedDrafts.addAll(toUpdate.stream().filter(draft -> draft.getSignedContent() == null || draft.getSignedContent().isEmpty()).collect(
                    Collectors.toSet()));
            Set<DBItemInventoryConfiguration> verifiedDrafts = null;
            // versionId on objects will be removed
            // versionId on command stays
            // Clarify: Keep UUID as versionId?
            String versionId = UUID.randomUUID().toString();
            // only signed objects will be processed
            // existing signatures of objects are verified
            verifiedDrafts = PublishUtils.verifySignatures(account, signedDrafts, hibernateSession);
            // TODO: temporary impl already signed objects have to get a versionId corresponding to the versionId of the command
            // TODO: below code to set versionId on already signed objects has to be removed in the future
            verifiedDrafts = verifiedDrafts.stream()
                    .filter(verifiedDraft -> verifiedDraft.getVersionId() == null || !versionId.equals(verifiedDraft.getVersionId()))
                    .map(verifiedDraft -> {
                        verifiedDraft.setVersionId(versionId);
                        return verifiedDraft;
                    }).collect(Collectors.toSet());
            // call UpdateRepo for all provided JobScheduler Masters
            JSConfigurationState deployConfigurationState = null;
            for (DBItemInventoryJSInstance master : masters) {
                try {
                    PublishUtils.updateRepo(versionId, verifiedDrafts, toDelete, master.getUri(), master.getSchedulerId());
                    deployConfigurationState = JSConfigurationState.DEPLOYED_SUCCESSFULLY;
                    for (DBItemInventoryConfiguration verifiedDraft : verifiedDrafts) {
                        hibernateSession.update(verifiedDraft);
                    }
                    Set<DBItemDeployedConfiguration> deployedObjects = PublishUtils.cloneInvCfgsToDepCfgs(verifiedDrafts, account, hibernateSession);
                    PublishUtils.prepareNextInvCfgGeneration(verifiedDrafts, hibernateSession);
                    updateSuccessfulConfigurationMappings(master, account, deployedObjects, toDelete, deployConfigurationState); 
                    LOGGER.info(String.format("Deploy to Master \"%1$s\" with Url '%2$s' was successful!",
                            master.getSchedulerId(), master.getUri()));
                    // if updateRepo was not successful most possibly a problem with the keys occurred
                    // therefore the drafts should not be updated with the given signature
                } catch (JobSchedulerBadRequestException e) {
                    LOGGER.error(e.getError().getCode());
                    LOGGER.error(String.format("Response from Master \"%1$s:\" with Url '%2$s':", master.getSchedulerId(), master.getUri()));
                    LOGGER.error(String.format("%1$s", e.getError().getMessage()));
                    deployConfigurationState = JSConfigurationState.NOT_DEPLOYED;
                    updateFailedConfigurationMappings(master, account, deployConfigurationState); 
                    deployHasErrors = true;
                    mastersWithDeployErrors.put(master.getSchedulerId(), e.getError().getMessage());
                    continue;
                } catch (JobSchedulerConnectionRefusedException e) {
                    String errorMessage = String.format("Connection to Master \"%1$s\" with Url '%2$s' failed! Objects not deployed!",
                            master.getSchedulerId(), master.getUri());
                    LOGGER.error(errorMessage);
                    deployConfigurationState = JSConfigurationState.NOT_DEPLOYED;
                    updateFailedConfigurationMappings(master, account, deployConfigurationState); 
                    deployHasErrors = true;
                    mastersWithDeployErrors.put(master.getSchedulerId(), errorMessage);
                    continue;
                } 
            }
            if (deployHasErrors) {
                String[] metaInfos = new String[mastersWithDeployErrors.size()];
                int index = 0;
                for(String key : mastersWithDeployErrors.keySet()) {
                    metaInfos[index] = String.format("%1$s: %2$s", key, mastersWithDeployErrors.get(key));
                    index++;
                }
                
                JocError error = new JocError("JOC-419", "Deploy was not successful on Master(s): ", metaInfos);
                throw new JocDeployException(error);
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

    
    private void updateSuccessfulConfigurationMappings(DBItemInventoryJSInstance master, String account, Set<DBItemDeployedConfiguration> deployedObjects,
            List<DBItemInventoryConfiguration> toDelete, JSConfigurationState state) throws SOSHibernateException, DBConnectionRefusedException, DBInvalidDataException {
        DBItemDeployedConfigurationHistory configuration = dbLayer.getLatestSuccessfulConfigurationHistory(master.getSchedulerId());
        dbLayer.updateSuccessfulJSMasterConfiguration(master.getSchedulerId(), account, configuration, deployedObjects, toDelete, state);
    }

    private void updateFailedConfigurationMappings(DBItemInventoryJSInstance master, String account, JSConfigurationState state) throws SOSHibernateException {
        DBItemDeployedConfigurationHistory configuration = dbLayer.getLatestSuccessfulConfigurationHistory(master.getSchedulerId());
        dbLayer.updateFailedJSMasterConfiguration(master.getSchedulerId(), account, configuration, state);
    }

}
