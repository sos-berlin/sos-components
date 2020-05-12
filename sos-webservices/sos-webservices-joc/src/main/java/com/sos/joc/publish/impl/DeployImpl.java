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
import javax.ws.rs.core.UriBuilderException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.inventory.DBItemJSConfiguration;
import com.sos.jobscheduler.db.inventory.DBItemJSDraftObject;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
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
            List<DBItemJSDraftObject> drafts = dbLayer.getFilteredJobSchedulerDraftObjects(deployFilter.getUpdate());
            List<DBItemJSDraftObject> toDelete = dbLayer.getFilteredJobSchedulerDraftObjects(deployFilter.getDelete());
            // Where in the db is the information about the Urls of the masters? -> sos_js_scheduler_instances
            List<DBItemInventoryInstance> masters = dbLayer.getMasters(schedulerIds);
            JocSecurityLevel jocSecLvl = Globals.getJocSecurityLevel();
            Set<DBItemJSDraftObject> signedDrafts = new HashSet<DBItemJSDraftObject>();
            signedDrafts.addAll(drafts.stream().filter(draft -> draft.getSignedContent() != null && !draft.getSignedContent().isEmpty()).collect(
                    Collectors.toSet()));
            Set<DBItemJSDraftObject> unsignedDrafts = new HashSet<DBItemJSDraftObject>();
            unsignedDrafts.addAll(drafts.stream().filter(draft -> draft.getSignedContent() == null || draft.getSignedContent().isEmpty()).collect(
                    Collectors.toSet()));
            Set<DBItemJSDraftObject> verifiedDrafts = null;
            String versionId = UUID.randomUUID().toString().substring(0,19);
            switch (jocSecLvl) {
            case HIGH:
                // only signed objects are allowed
                // existing signatures of objects are verified
                verifiedDrafts = PublishUtils.verifySignatures(account, signedDrafts, hibernateSession);
                break;
            case MEDIUM:
                // signed and unsigned objects are allowed
                // existing signatures of objects are verified
                verifiedDrafts = PublishUtils.verifySignatures(account, signedDrafts, hibernateSession);
                // unsigned objects are signed with the user private key automatically
                PublishUtils.signDrafts(versionId, account, unsignedDrafts, hibernateSession);
                verifiedDrafts.addAll(unsignedDrafts);
                break;
            case LOW:
                // signed and unsigned objects are allowed
                // existing signatures of objects are verified
                verifiedDrafts = PublishUtils.verifySignaturesDefault(account, signedDrafts, hibernateSession);
                // unsigned objects are signed with the default key automatically
                PublishUtils.signDraftsDefault(versionId, account, unsignedDrafts, hibernateSession);
                verifiedDrafts.addAll(unsignedDrafts);
                break;
            }
            // call UpdateRepo for all provided JobScheduler Masters
            JSConfigurationState deployConfigurationState = null;
            for (DBItemInventoryInstance master : masters) {
                try {
                    PublishUtils.updateRepo(versionId, verifiedDrafts, toDelete, master.getUri(), master.getSchedulerId());
                    deployConfigurationState = JSConfigurationState.DEPLOYED_SUCCESSFULLY;
                    // TODO:
                    // if update Repo was successful save updated drafts 
                    for (DBItemJSDraftObject verifiedDraft : verifiedDrafts) {
                        hibernateSession.update(verifiedDraft);
                    }
                    // if updateRepo was not successful most possibly a problem with the keys occurred
                    // therefore the drafts should not be updated with the given signature
                } catch (JobSchedulerBadRequestException e) {
                    LOGGER.error(e.getError().getCode());
                    LOGGER.error(String.format("Response from Master \"%1$s:\" with Url '%2$s':", master.getSchedulerId(), master.getUri()));
                    LOGGER.error(String.format("%1$s", e.getError().getMessage()));
                    deployConfigurationState = JSConfigurationState.NOT_DEPLOYED;
                    deployHasErrors = true;
                    mastersWithDeployErrors.put(master.getSchedulerId(), e.getError().getMessage());
                } catch (JobSchedulerConnectionRefusedException e) {
                    String errorMessage = String.format("Connection to Master \"%1$s\" with Url '%2$s' failed! Objects not deployed!",
                            master.getSchedulerId(), master.getUri());
                    LOGGER.error(errorMessage);
                    deployConfigurationState = JSConfigurationState.NOT_DEPLOYED;
                    deployHasErrors = true;
                    mastersWithDeployErrors.put(master.getSchedulerId(), errorMessage);
                } 
                // TODO:
                // update mapping table for JSObject -> JobScheduler Master relation
                updateConfigurationMappings(master, account, verifiedDrafts, toDelete, deployConfigurationState); 
            }
            if (deployHasErrors) {
                
                String[] metaInfos = new String[mastersWithDeployErrors.size()];
                int index = 0;
                for(String key : mastersWithDeployErrors.keySet()) {
                    metaInfos[index] = String.format("%1$s: %2$s", key, mastersWithDeployErrors.get(key));
                    index++;
                }
                JocError error = new JocError("JOC-415", "Deploy was not successful on Master(s): ", metaInfos);
                throw new JocDeployException(error);
            }
            // update the existing draft object
            // * new commitHash (property versionId)
            // * clear signature
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

    
    private void updateConfigurationMappings(DBItemInventoryInstance master, String account, Set<DBItemJSDraftObject> verifiedDrafts,
            List<DBItemJSDraftObject> toDelete, JSConfigurationState state) throws SOSHibernateException {
        DBItemJSConfiguration configuration = dbLayer.getLatestSuccessfulConfiguration(master.getSchedulerId());
        dbLayer.updateJSMasterConfiguration(master.getSchedulerId(), account, configuration, verifiedDrafts, toDelete, state);
    }

}
