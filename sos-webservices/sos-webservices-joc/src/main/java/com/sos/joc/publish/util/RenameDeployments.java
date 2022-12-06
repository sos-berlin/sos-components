package com.sos.joc.publish.util;

import java.io.IOException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import com.sos.commons.exception.SOSException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.SignedItemsSpec;

public class RenameDeployments {

    public static void callUpdateItemsFor(DBLayerDeploy dbLayer, SignedItemsSpec signedItemsSpec, Set<DBItemDeploymentHistory> renamedOrigToDelete,
            String account, String commitId, String controllerId, String accessToken, JocError jocError, String wsIdentifier) throws SOSException,
            IOException, InterruptedException, ExecutionException, TimeoutException, CertificateException {

        if (signedItemsSpec.getVerifiedDeployables() != null && !signedItemsSpec.getVerifiedDeployables().isEmpty()) {

            // store new history entries and update inventory for update operation optimistically
            StoreDeployments.storeNewDepHistoryEntries(signedItemsSpec, account, commitId, controllerId, accessToken, jocError, dbLayer);
            DeleteDeployments.storeNewDepHistoryEntries(dbLayer, new ArrayList(renamedOrigToDelete), commitId);
            List<DBItemInventoryCertificate> caCertificates = dbLayer.getCaCertificates();
            boolean verified = false;
            String signerDN = null;
            X509Certificate cert = null;
            // call updateItems command via ControllerApi for the given controller

            switch (signedItemsSpec.getKeyPair().getKeyAlgorithm()) {
            case SOSKeyConstants.PGP_ALGORITHM_NAME:
                UpdateItemUtils.updateItemsAddOrDeletePGP(commitId, signedItemsSpec.getVerifiedDeployables(), renamedOrigToDelete, controllerId)
                        .thenAccept(either -> StoreDeployments.processAfterAdd(either, account, commitId, controllerId, accessToken, jocError,
                                wsIdentifier));
                break;
            case SOSKeyConstants.RSA_ALGORITHM_NAME:
                if (signedItemsSpec.getKeyPair().getCertificate() != null && !signedItemsSpec.getKeyPair().getCertificate().isEmpty()) {
                    cert = KeyUtil.getX509Certificate(signedItemsSpec.getKeyPair().getCertificate());
                }
                if (cert != null) {
                    verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                    if (verified) {
                        UpdateItemUtils.updateItemsAddOrDeleteX509Certificate(commitId, signedItemsSpec.getVerifiedDeployables(), renamedOrigToDelete,
                                controllerId, SOSKeyConstants.RSA_SIGNER_ALGORITHM, signedItemsSpec.getKeyPair().getCertificate()).thenAccept(
                                        either -> StoreDeployments.processAfterAdd(either, account, commitId, controllerId, accessToken, jocError,
                                                wsIdentifier));
                    } else {
                        signerDN = cert.getSubjectDN().getName();
                        UpdateItemUtils.updateItemsAddOrDeleteX509SignerDN(commitId, signedItemsSpec.getVerifiedDeployables(), renamedOrigToDelete,
                                controllerId, SOSKeyConstants.RSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> StoreDeployments.processAfterAdd(
                                        either, account, commitId, controllerId, accessToken, jocError, wsIdentifier));
                    }
                } else {
                    String message = "No certificate present! Items could not be deployed to controller.";
                    StoreDeployments.updateOptimisticEntriesIfFailed(signedItemsSpec.getVerifiedDeployables().keySet(), message, dbLayer);
                    throw new JocDeployException(message);
                }
                break;
            case SOSKeyConstants.ECDSA_ALGORITHM_NAME:
                cert = KeyUtil.getX509Certificate(signedItemsSpec.getKeyPair().getCertificate());
                if (cert != null) {
                    verified = PublishUtils.verifyCertificateAgainstCAs(cert, caCertificates);
                    if (verified) {
                        UpdateItemUtils.updateItemsAddOrDeleteX509Certificate(commitId, signedItemsSpec.getVerifiedDeployables(), renamedOrigToDelete,
                                controllerId, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signedItemsSpec.getKeyPair().getCertificate()).thenAccept(
                                        either -> StoreDeployments.processAfterAdd(either, account, commitId, controllerId, accessToken, jocError,
                                                wsIdentifier));
                    } else {
                        signerDN = cert.getSubjectDN().getName();
                        UpdateItemUtils.updateItemsAddOrDeleteX509SignerDN(commitId, signedItemsSpec.getVerifiedDeployables(), renamedOrigToDelete,
                                controllerId, SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, signerDN).thenAccept(either -> StoreDeployments.processAfterAdd(
                                        either, account, commitId, controllerId, accessToken, jocError, wsIdentifier));
                    }
                } else {
                    String message = "No certificate present! Items could not be deployed to controller.";
                    StoreDeployments.updateOptimisticEntriesIfFailed(signedItemsSpec.getVerifiedDeployables().keySet(), message, dbLayer);
                    throw new JocDeployException(message);
                }
                break;
            }
        }
    }
}
