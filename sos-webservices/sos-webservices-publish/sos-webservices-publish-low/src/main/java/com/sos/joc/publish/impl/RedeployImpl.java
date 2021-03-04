package com.sos.joc.publish.impl;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.RedeployFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpdateableWorkflowJobAgentName;
import com.sos.joc.publish.resource.IRedeploy;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.joc.publish.util.StoreDeployments;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;

@Path("inventory/deployment")
public class RedeployImpl extends JOCResourceImpl implements IRedeploy {

    private static final String API_CALL = "./inventory/deployment/redeploy";
    private static final Logger LOGGER = LoggerFactory.getLogger(RedeployImpl.class);
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
            String account = Globals.defaultProfileAccount;
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            String controllerId = redeployFilter.getControllerId();
            // get all latest active history objects from the database for the provided controllerId and folder from the filter
            List<DBItemDeploymentHistory> latest = dbLayer.getLatestDepHistoryItemsFromFolder(redeployFilter.getFolder(), controllerId, 
                    redeployFilter.getRecursive());
            // all items will be resigned with a new commitId
            final String commitId = UUID.randomUUID().toString();
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.MEDIUM);

            Set<DBItemDeploymentHistory> unsignedRedeployables = null;
            if (latest != null) {
                unsignedRedeployables = new HashSet<DBItemDeploymentHistory>(latest);
            }
            // preparations
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames = new HashSet<UpdateableWorkflowJobAgentName>();
            Map<DBItemDeploymentHistory, DBItemDepSignatures> verifiedRedeployables = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();

            if (unsignedRedeployables != null && !unsignedRedeployables.isEmpty()) {
                PublishUtils.updatePathWithNameInContent(unsignedRedeployables);
                unsignedRedeployables.stream().filter(item -> ConfigurationType.WORKFLOW.equals(ConfigurationType.fromValue(item.getType()))).forEach(
                        item -> updateableAgentNames.addAll(PublishUtils.getUpdateableAgentRefInWorkflowJobs(item, controllerId, dbLayer)));
                verifiedRedeployables.putAll(PublishUtils.getDeploymentsWithSignature(commitId, account, unsignedRedeployables, hibernateSession,
                        JocSecurityLevel.MEDIUM));
            }
            if (verifiedRedeployables != null && !verifiedRedeployables.isEmpty()) {
                // call updateItems command via ControllerApi for given controllers
                boolean verified = false;
                String signerDN = null;
                List<DBItemInventoryCertificate> caCertificates = dbLayer.getCaCertificates();
                X509Certificate cert = null;
                switch (keyPair.getKeyAlgorithm()) {
                case SOSKeyConstants.PGP_ALGORITHM_NAME:
                    PublishUtils.updateItemsAddOrUpdatePGP(commitId, null, verifiedRedeployables, controllerId, dbLayer).thenAccept(either -> {
                        processAfterAdd(either, account, commitId, controllerId, getAccessToken(), getJocError());
                    });
                    break;
                case SOSKeyConstants.RSA_ALGORITHM_NAME:
                    cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                    verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                    if (verified) {
                        PublishUtils.updateItemsAddOrUpdateWithX509Certificate(commitId, null, verifiedRedeployables, controllerId, dbLayer,
                                SOSKeyConstants.RSA_SIGNER_ALGORITHM, keyPair.getCertificate()).thenAccept(either -> {
                                    processAfterAdd(either, account, commitId, controllerId, getAccessToken(), getJocError());
                                });
                    } else {
                        signerDN = cert.getSubjectDN().getName();
                        PublishUtils.updateItemsAddOrUpdateWithX509SignerDN(commitId, null, verifiedRedeployables, controllerId, dbLayer,
                                SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> {
                                    processAfterAdd(either, account, commitId, controllerId, getAccessToken(), getJocError());
                                });
                    }
                    break;
                case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                    cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                    verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                    if (verified) {
                        PublishUtils.updateItemsAddOrUpdateWithX509Certificate(commitId, null, verifiedRedeployables, controllerId, dbLayer,
                                SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, keyPair.getCertificate()).thenAccept(either -> {
                                    processAfterAdd(either, account, commitId, controllerId, getAccessToken(), getJocError());
                                });
                    } else {
                        signerDN = cert.getSubjectDN().getName();
                        PublishUtils.updateItemsAddOrUpdateWithX509SignerDN(commitId, null, verifiedRedeployables, controllerId, dbLayer,
                                SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> {
                                    processAfterAdd(either, account, commitId, controllerId, getAccessToken(), getJocError());
                                });
                    }
                    break;
                }
            }
            StoreDeployments.storeNewDepHistoryEntriesForRedeploy(verifiedRedeployables, account, commitId, controllerId, getAccessToken(), 
                    getJocError(), dbLayer);
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

    private void processAfterAdd(
            Either<Problem, Void> either,
            String account, 
            String commitId, 
            String controllerId, 
            String accessToken,
            JocError jocError) {
        // First create a new db session as the session of the parent web service can already been closed
        SOSHibernateSession newHibernateSession = null;
        try {
            newHibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerDeploy dbLayer = new DBLayerDeploy(newHibernateSession);
            if (either.isLeft()) {
                // an error occurred
                String message = String.format("Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
                LOGGER.error(message);
                // updateRepo command is atomic, therefore all items are rejected
                // get all already optimistically stored entries for the commit
                List<DBItemDeploymentHistory> optimisticEntries = dbLayer.getDepHistory(commitId);
                // update all previously optimistically stored entries with the error message and change the state
                for(DBItemDeploymentHistory optimistic : optimisticEntries) {
                    optimistic.setErrorMessage(either.getLeft().message());
                    optimistic.setState(DeploymentState.NOT_DEPLOYED.value());
                    dbLayer.getSession().update(optimistic);
                    // update related inventory configuration to deployed=false 
                    DBItemInventoryConfiguration cfg = dbLayer.getConfiguration(optimistic.getInventoryConfigurationId());
                    cfg.setDeployed(false);
                    dbLayer.getSession().update(cfg);
                }
                // if not successful the objects and the related controllerId have to be stored 
                // in a submissions table for reprocessing
                dbLayer.createSubmissionForFailedDeployments(optimisticEntries);
                ProblemHelper.postProblemEventIfExist(either, accessToken, jocError, null);
                ProblemHelper.postProblemEventIfExist(either, getAccessToken(), getJocError(), null);
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            ProblemHelper.postExceptionEventIfExist(Either.left(e), getAccessToken(), getJocError(), null);
        } finally {
            Globals.disconnect(newHibernateSession);
        }
    }

}