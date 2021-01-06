package com.sos.joc.publish.impl;

import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.RedeployAudit;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.exceptions.BulkError;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.Err419;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.publish.RedeployFilter;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.resource.IRedeploy;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

import io.vavr.control.Either;
import js7.base.problem.Problem;

@Path("inventory/deployment")
public class RedeployImpl extends JOCResourceImpl implements IRedeploy {

    private static final String API_CALL = "./inventory/deployment/redeploy";
    private static final Logger LOGGER = LoggerFactory.getLogger(RedeployImpl.class);
    private DBLayerDeploy dbLayer = null;
    private boolean hasErrors = false;
    private List<Err419> listOfErrors = new ArrayList<Err419>();

    @Override
    public JOCDefaultResponse postDeploy(String xAccessToken, byte[] filter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, filter, xAccessToken);
            JsonValidator.validateFailFast(filter, RedeployFilter.class);
            RedeployFilter reDeployFilter = Globals.objectMapper.readValue(filter, RedeployFilter.class);
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", 
                    getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isDeploy());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            dbLayer = new DBLayerDeploy(hibernateSession);
            // get all available controller instances
            // process filter
            String controllerId = reDeployFilter.getControllerId();
            // read all objects provided in the filter from the database
            List<DBItemDeploymentHistory> reDeployables = dbLayer.getDeploymentsToRedeploy(reDeployFilter);

            final Date deploymentDate = Date.from(Instant.now());
            // all items will be signed or re-signed with current versionId
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.HIGH);
            // call updateRepo command via ControllerApi for given controllerId
            String signerDN = null;
            X509Certificate cert = null;
            Set<String> versionIds = reDeployables.stream().flatMap(item -> Stream.of(item.getCommitId())).collect(Collectors.toSet());
            for (String versionId : versionIds) {
                switch(keyPair.getKeyAlgorithm()) {
                case SOSKeyConstants.PGP_ALGORITHM_NAME:
                    PublishUtils.updateItemsAddOrUpdatePGP(
                            versionId,  
                            reDeployables.stream()
                                .map(item -> {
                                    if(item.getCommitId().equals(versionId)) {
                                        return item;
                                    }
                                    return null;
                                })
                                .collect(Collectors.toList()),
                            controllerId)
                        .thenAccept(either -> {
                            processAfterAdd(either, reDeployables, account, versionId, controllerId, deploymentDate, reDeployFilter);
                        }).get();
                    break;
                case SOSKeyConstants.RSA_ALGORITHM_NAME:
                    cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                    signerDN = cert.getSubjectDN().getName();
                    PublishUtils.updateItemsAddOrUpdateWithX509(
                            versionId,  
                            reDeployables.stream()
                                .map(item -> {
                                    if(item.getCommitId().equals(versionId)) {
                                        return item;
                                    }
                                    return null;
                                })
                                .collect(Collectors.toList()),
                            controllerId, 
                            SOSKeyConstants.RSA_SIGNER_ALGORITHM, 
                            signerDN)
                        .thenAccept(either -> {
                            processAfterAdd(either, reDeployables, account, versionId, controllerId, deploymentDate, reDeployFilter);
                        }).get();
                    break;
                case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                    cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                    signerDN = cert.getSubjectDN().getName();
                    PublishUtils.updateItemsAddOrUpdateWithX509(
                            versionId,  
                            reDeployables.stream()
                                .map(item -> {
                                    if(item.getCommitId().equals(versionId)) {
                                        return item;
                                    }
                                    return null;
                                })
                                .collect(Collectors.toList()),
                            controllerId, 
                            SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, 
                            signerDN)
                        .thenAccept(either -> {
                            processAfterAdd(either, reDeployables, account, versionId, controllerId, deploymentDate, reDeployFilter);
                        }).get();
                    break;
                }
            }
            versionIds.stream().peek(versionId -> {
            });
            if (hasErrors) {
                return JOCDefaultResponse.responseStatus419(listOfErrors);
            } else {
                return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
            }
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

    private void processAfterAdd (
            Either<Problem, Void> either, 
            List<DBItemDeploymentHistory> reDeployables,
            String account,
            String versionId,
            String controllerId,
            Date deploymentDate,
            RedeployFilter filter) {
        if (either.isRight()) {
            // no error occurred
            Set<DBItemDeploymentHistory> deployedObjects = 
                    PublishUtils.cloneDepHistoryItemsToRedeployed(reDeployables, account, dbLayer, controllerId, deploymentDate);
            LOGGER.info(String.format("Deploy to Controller \"%1$s\" was successful!", controllerId));
            createAuditLogForEach(deployedObjects, filter, controllerId, versionId);
        } else if (either.isLeft()) {
            // an error occurred
            String message = String.format(
                    "Response from Controller \"%1$s:\": %2$s", controllerId, either.getLeft().message());
            LOGGER.error(message);
            // updateRepo command is atomic, therefore all items are rejected
            List<DBItemDeploymentHistory> failedDeployUpdateItems = 
                    dbLayer.updateFailedDeploymentForUpdate(reDeployables, controllerId, account, either.getLeft().message());
            // if not successful the objects and the related controllerId have to be stored 
            // in a submissions table for reprocessing
            dbLayer.createSubmissionForFailedDeployments(failedDeployUpdateItems);
            hasErrors = true;
            if (either.getLeft().codeOrNull() != null) {
                listOfErrors.add(
                        new BulkError().get(new JocError(either.getLeft().codeOrNull().toString(), either.getLeft().message()), "/"));
            } else {
                listOfErrors.add(new BulkError().get(new JocError(either.getLeft().message()), "/"));
            }
        }
    }
    
    private void createAuditLogForEach(Collection<DBItemDeploymentHistory> depHistoryEntries, RedeployFilter filter, String controllerId,
            String commitId) {
        Set<RedeployAudit> audits = depHistoryEntries.stream().map(item -> {
                return new RedeployAudit(filter, 
                        controllerId, 
                        commitId, 
                        item.getId(), 
                        item.getPath(), 
                        String.format("object %1$s updated on controller %2$s", item.getPath(), controllerId));
        }).collect(Collectors.toSet());
        audits.stream().forEach(audit -> logAuditMessage(audit));
        audits.stream().forEach(audit -> storeAuditLogEntry(audit));
    }
    
}