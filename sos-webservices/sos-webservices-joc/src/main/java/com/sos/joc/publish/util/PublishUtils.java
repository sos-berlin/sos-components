package com.sos.joc.publish.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.UriBuilderException;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.pgp.SOSPGPConstants;
import com.sos.commons.sign.pgp.key.KeyUtil;
import com.sos.commons.sign.pgp.sign.SignObject;
import com.sos.commons.sign.pgp.verify.VerifySignature;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.agent.DeleteAgentRef;
import com.sos.jobscheduler.model.command.UpdateRepo;
import com.sos.jobscheduler.model.deploy.DeleteObject;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.DeleteWorkflow;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryMeta;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingKeyException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedKeyTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.pgp.JocKeyAlgorythm;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.pgp.JocKeyType;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.Signature;
import com.sos.joc.model.publish.SignedObject;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpDownloadMapper;

import io.vavr.control.Either;
import js7.base.crypt.GenericSignature;
import js7.base.crypt.SignedString;
import js7.base.problem.Problem;
import js7.data.agent.AgentRefPath;
import js7.data.item.VersionId;
import js7.data.workflow.WorkflowPath;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.data.JUpdateRepoOperation;
import reactor.core.publisher.Flux;

public abstract class PublishUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishUtils.class);
    private static ObjectMapper om = UpDownloadMapper.initiateObjectMapper();

    public static String getExtensionFromFilename(String filename) {
        String extension = filename;
        if (filename == null) {
            return "";
        }
        if (extension.contains(".")) {
            extension = extension.replaceFirst(".*\\.([^\\.]+)$", "$1");
        } else {
            extension = "";
        }
        return extension.toLowerCase();
    }

    public static void inputStream2OutputStream(InputStream inStream, OutputStream outStream) throws IOException {
        int bytesRead;
        byte[] buf = new byte[1024];
        while ((bytesRead = inStream.read(buf)) > 0) {
            outStream.write(buf, 0, bytesRead);
        }
        inStream.close();
        outStream.close();
    }
    
    public static void storeKey(JocKeyPair keyPair, SOSHibernateSession hibernateSession, String account)  throws SOSHibernateException {
        DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
        if (keyPair != null) {
            if (keyPair.getPrivateKey() != null && keyPair.getCertificate() != null) {
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PRIVATE.ordinal(), keyPair.getPrivateKey(), keyPair.getCertificate(), account);
            } else if (keyPair.getPublicKey() != null && keyPair.getCertificate() != null) {
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PUBLIC.ordinal(), keyPair.getPublicKey(), keyPair.getCertificate(), account);
            } else if (keyPair.getPrivateKey() != null) {
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PRIVATE.ordinal(), keyPair.getPrivateKey(), account);
            } else if (keyPair.getCertificate() != null) {
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PUBLIC.ordinal(), keyPair.getCertificate(), account);
            }else if (keyPair.getPublicKey() != null) {
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PUBLIC.ordinal(), keyPair.getPublicKey(), account);
            } 
        }
    }

    public static void checkJocSecurityLevelAndStore (JocKeyPair keyPair, SOSHibernateSession hibernateSession, String account) 
            throws SOSHibernateException, JocUnsupportedKeyTypeException, JocMissingRequiredParameterException {
        if (keyPair != null) {
            //Check forJocSecurityLevel commented, has to be introduced when the testing can be done with changing joc.properties
            if (keyPair.getPrivateKey() != null && Globals.getJocSecurityLevel().equals(JocSecurityLevel.MEDIUM)) {
                if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PUBLIC_KEY_HEADER) 
                        || keyPair.getPrivateKey().startsWith(SOSPGPConstants.PUBLIC_PGP_KEY_HEADER)
                        || keyPair.getPrivateKey().startsWith(SOSPGPConstants.PUBLIC_RSA_KEY_HEADER)) {
                    throw new JocUnsupportedKeyTypeException("Wrong key type. expected: private | received: public");
                }
                storeKey(keyPair, hibernateSession, account);
            } else if (keyPair.getPublicKey() != null && Globals.getJocSecurityLevel().equals(JocSecurityLevel.HIGH)) {
                if (keyPair.getPublicKey().startsWith(SOSPGPConstants.PRIVATE_KEY_HEADER) 
                        || keyPair.getPublicKey().startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER)
                        || keyPair.getPublicKey().startsWith(SOSPGPConstants.PRIVATE_RSA_KEY_HEADER)) {
                    throw new JocUnsupportedKeyTypeException("Wrong key type. expected: public | received: private");
                }
                storeKey(keyPair, hibernateSession, account);
            } else if (keyPair.getPublicKey() != null && !Globals.getJocSecurityLevel().equals(JocSecurityLevel.HIGH)) {
                throw new JocUnsupportedKeyTypeException("Wrong key type. expected: private | received: public");
            } else if (keyPair.getPrivateKey() != null && Globals.getJocSecurityLevel().equals(JocSecurityLevel.HIGH)) {
                throw new JocUnsupportedKeyTypeException("Wrong key type. expected: public | received: private");
            } else if (Globals.getJocSecurityLevel().equals(JocSecurityLevel.LOW)) {
                LOGGER.info("JOC Security Level is low, no key will be stored");
            }
        } else {
            throw new JocMissingRequiredParameterException("no key was provided with the request.");
        }
    }

    public static void signDrafts(String versionId, String account, Set<DBItemInventoryConfiguration> unsignedDrafts, SOSHibernateSession session)
            throws JocMissingKeyException, JsonParseException, JsonMappingException, SOSHibernateException, IOException, PGPException,
            NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account);
        signDrafts(versionId, account, unsignedDrafts, keyPair, session);
    }
    
    public static void signDrafts(
            String versionId, String account, Set<DBItemInventoryConfiguration> unsignedDrafts, JocKeyPair keyPair, SOSHibernateSession session)
                    throws JocMissingKeyException, JsonParseException, JsonMappingException, SOSHibernateException, IOException, PGPException,
                    NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        boolean isPGPKey = false;
        if(keyPair.getPrivateKey() == null || keyPair.getPrivateKey().isEmpty()) {
            throw new JocMissingKeyException("No private key found fo signing!");
        } else {
            if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER)) {
                isPGPKey = true;
            }
            for (DBItemInventoryConfiguration draft : unsignedDrafts) {
                updateVersionIdOnObject(draft, versionId, session);
                if(isPGPKey) {
                    // TODO: uncomment when draft is refactored
//                    draft.setSignedContent(SignObject.signPGP(keyPair.getPrivateKey(), draft.getContent(), null));
                } else {
                    KeyPair kp = null;
                    if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_RSA_KEY_HEADER)) {
                        kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(keyPair.getPrivateKey());
                    } else {
                        kp = KeyUtil.getKeyPairFromPrivatKeyString(keyPair.getPrivateKey());
                    }
                    // TODO: uncomment when draft is refactored
//                    draft.setSignedContent(SignObject.signX509(kp.getPrivate(), draft.getContent()));
                }
            }
        }
    }
    
    public static Map<DBItemInventoryConfiguration, DBItemDepSignatures> getDraftsWithSignature(String versionId, String account, 
            Set<DBItemInventoryConfiguration> unsignedDrafts, SOSHibernateSession session) throws JocMissingKeyException, JsonParseException, 
            JsonMappingException, SOSHibernateException, IOException, PGPException, NoSuchAlgorithmException, InvalidKeySpecException, 
            InvalidKeyException, SignatureException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account);
        return getDraftsWithSignature(versionId, account, unsignedDrafts, keyPair, session);
    }
    
    public static Map<DBItemInventoryConfiguration, DBItemDepSignatures> getDraftsWithSignature(
            String versionId, String account, Set<DBItemInventoryConfiguration> unsignedDrafts, JocKeyPair keyPair, SOSHibernateSession session)
                    throws JocMissingKeyException, JsonParseException, JsonMappingException, SOSHibernateException, IOException, PGPException,
                    NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        boolean isPGPKey = false;
        Map<DBItemInventoryConfiguration, DBItemDepSignatures> signedDrafts = new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
        if(keyPair.getPrivateKey() == null || keyPair.getPrivateKey().isEmpty()) {
            throw new JocMissingKeyException("No private key found fo signing!");
        } else {
            if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER)) {
                isPGPKey = true;
            }
            DBItemDepSignatures sig = null;
            for (DBItemInventoryConfiguration draft : unsignedDrafts) {
                updateVersionIdOnObject(draft, versionId, session);
                if(isPGPKey) {
                    sig = new DBItemDepSignatures();
                    sig.setAccount(account);
                    sig.setInvConfigurationId(draft.getId());
                    sig.setModified(Date.from(Instant.now()));
                    sig.setSignature(SignObject.signPGP(keyPair.getPrivateKey(), draft.getContent(), null));
                    signedDrafts.put(draft, sig);
                } else {
                    KeyPair kp = null;
                    if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_RSA_KEY_HEADER)) {
                        kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(keyPair.getPrivateKey());
                    } else {
                        kp = KeyUtil.getKeyPairFromPrivatKeyString(keyPair.getPrivateKey());
                    }
                    sig = new DBItemDepSignatures();
                    sig.setAccount(account);
                    sig.setInvConfigurationId(draft.getId());
                    sig.setModified(Date.from(Instant.now()));
                    sig.setSignature(SignObject.signX509(kp.getPrivate(), draft.getContent()));
                    signedDrafts.put(draft, sig);
                }
                if (sig != null) {
                    session.save(sig);
                }
            }
        }
        return signedDrafts;
    }
    
    public static Set<DBItemInventoryConfiguration> verifySignatures(
            String account, Set<DBItemInventoryConfiguration> signedDrafts, SOSHibernateSession session)
            throws SOSHibernateException, IOException, PGPException, InvalidKeyException, CertificateException, NoSuchAlgorithmException,
            InvalidKeySpecException, JocMissingKeyException, SignatureException, NoSuchProviderException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account);
        if (keyPair.getPrivateKey() != null) {
            if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER)) {
                return verifyPGPSignatures(account, signedDrafts, keyPair);
            } else {
                return verifyRSASignatures(signedDrafts, keyPair);
            }
        } else if (keyPair.getPublicKey() != null) {
            if (keyPair.getPublicKey().startsWith(SOSPGPConstants.PUBLIC_PGP_KEY_HEADER)) {
                return verifyPGPSignatures(account, signedDrafts, keyPair);
            } else {
                return verifyRSASignatures(signedDrafts, keyPair);
            }
        } else if (keyPair.getCertificate() != null) {
            return verifyRSASignatures(signedDrafts, keyPair);
        } else {
            throw new JocMissingKeyException(String.format("No key or certificate provide for the account \"%1$s\".", account));
        }
    }

    public static DBItemInventoryConfiguration verifySignature(
            String account, DBItemInventoryConfiguration signedDraft, DBItemDepSignatures draftSignature, SOSHibernateSession session)
            throws SOSHibernateException, IOException, PGPException, InvalidKeyException, CertificateException, NoSuchAlgorithmException,
            InvalidKeySpecException, JocMissingKeyException, SignatureException, NoSuchProviderException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account);
        if (keyPair.getPrivateKey() != null) {
            if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER)) {
                return verifyPGPSignature(account, signedDraft, draftSignature, keyPair);
            } else {
                return verifyRSASignature(signedDraft, draftSignature, keyPair);
            }
        } else if (keyPair.getPublicKey() != null) {
            if (keyPair.getPublicKey().startsWith(SOSPGPConstants.PUBLIC_PGP_KEY_HEADER)) {
                return verifyPGPSignature(account, signedDraft, draftSignature, keyPair);
            } else {
                return verifyRSASignature(signedDraft, draftSignature, keyPair);
            }
        } else if (keyPair.getCertificate() != null) {
            return verifyRSASignature(signedDraft, draftSignature, keyPair);
        } else {
            throw new JocMissingKeyException(String.format("No key or certificate provide for the account \"%1$s\".", account));
        }
    }

    public static Set<DBItemInventoryConfiguration> verifyPGPSignatures(
            String account, Set<DBItemInventoryConfiguration> signedDrafts, JocKeyPair keyPair)
            throws SOSHibernateException, IOException, PGPException {
        Set<DBItemInventoryConfiguration> verifiedDrafts = new HashSet<DBItemInventoryConfiguration>();
        String publicKey = null;
        if (keyPair.getPublicKey() == null) {
            publicKey = KeyUtil.extractPublicKey(keyPair.getPrivateKey());
        } else {
            publicKey = keyPair.getPublicKey();
        }
        Boolean verified = false;
        for (DBItemInventoryConfiguration draft : signedDrafts) {
            // TODO: uncomment when draft is refactored
//            verified = VerifySignature.verifyPGP(publicKey, draft.getContent(), draft.getSignedContent());
            if(!verified) {
                LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", draft.getPath()));
            } else {
                verifiedDrafts.add(draft);
            }
        }
        return verifiedDrafts;
    }
    
    public static DBItemInventoryConfiguration verifyPGPSignature(
            String account, DBItemInventoryConfiguration signedDraft, DBItemDepSignatures draftSignature, JocKeyPair keyPair)
            throws SOSHibernateException, IOException, PGPException {
        DBItemInventoryConfiguration verifiedDraft = null;
        String publicKey = null;
        if (keyPair.getPublicKey() == null) {
            publicKey = KeyUtil.extractPublicKey(keyPair.getPrivateKey());
        } else {
            publicKey = keyPair.getPublicKey();
        }
        Boolean verified = false;
        verified = VerifySignature.verifyPGP(publicKey, signedDraft.getContent(), draftSignature.getSignature());
        if(!verified) {
            LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDraft.getPath()));
        } else {
            verifiedDraft = signedDraft;
        }
        return verifiedDraft;
    }
    
    public static Set<DBItemInventoryConfiguration> verifyRSASignatures(Set<DBItemInventoryConfiguration> signedDrafts, JocKeyPair jocKeyPair)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, JocMissingKeyException,
            InvalidKeyException, SignatureException, NoSuchProviderException, IOException {
        Set<DBItemInventoryConfiguration> verifiedDrafts = new HashSet<DBItemInventoryConfiguration>();
        Certificate cert = null;
        PublicKey publicKey = null;
        if (jocKeyPair.getCertificate() != null && !jocKeyPair.getCertificate().isEmpty()) {
            cert = KeyUtil.getCertificate(jocKeyPair.getCertificate());
        } else if (jocKeyPair.getPublicKey() != null && !jocKeyPair.getPublicKey().isEmpty()) {
            publicKey = KeyUtil.getPublicKeyFromString(jocKeyPair.getPublicKey());
        }
        if (cert == null && publicKey == null) {
            KeyPair kp = null;
            if (jocKeyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_RSA_KEY_HEADER)) {
                kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(jocKeyPair.getPrivateKey());
            } else {
                kp = KeyUtil.getKeyPairFromPrivatKeyString(jocKeyPair.getPrivateKey());
            }
            publicKey = kp.getPublic();
        }
        Boolean verified = false;
        if (cert != null) {
            for (DBItemInventoryConfiguration draft : signedDrafts) {
                // TODO: uncomment when draft is refactored
//                verified = VerifySignature.verifyX509(cert, draft.getContent(), draft.getSignedContent());
                if(!verified) {
                    LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", draft.getPath()));
                } else {
                    verifiedDrafts.add(draft);
                }
            }
        } else if (publicKey != null) {
            for (DBItemInventoryConfiguration draft : signedDrafts) {
                // TODO: uncomment when draft is refactored
//                verified = VerifySignature.verifyX509(publicKey, draft.getContent(), draft.getSignedContent());
                if(!verified) {
                    LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", draft.getPath()));
                } else {
                    verifiedDrafts.add(draft);
                }
            }
        } else {
            throw new JocMissingKeyException("Neither PublicKey nor Certificate found for signature verification.");
        }
        return verifiedDrafts;
    }

    public static DBItemInventoryConfiguration verifyRSASignature(DBItemInventoryConfiguration signedDraft, DBItemDepSignatures draftSignature,
            JocKeyPair jocKeyPair) throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, JocMissingKeyException,
            InvalidKeyException, SignatureException, NoSuchProviderException, IOException {
        DBItemInventoryConfiguration verifiedDraft = null;
        Certificate cert = null;
        PublicKey publicKey = null;
        if (jocKeyPair.getCertificate() != null && !jocKeyPair.getCertificate().isEmpty()) {
            cert = KeyUtil.getCertificate(jocKeyPair.getCertificate());
        } else if (jocKeyPair.getPublicKey() != null && !jocKeyPair.getPublicKey().isEmpty()) {
            publicKey = KeyUtil.getPublicKeyFromString(jocKeyPair.getPublicKey());
        }
        if (cert == null && publicKey == null) {
            KeyPair kp = null;
            if (jocKeyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_RSA_KEY_HEADER)) {
                kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(jocKeyPair.getPrivateKey());
            } else {
                kp = KeyUtil.getKeyPairFromPrivatKeyString(jocKeyPair.getPrivateKey());
            }
            publicKey = kp.getPublic();
        }
        Boolean verified = false;
        if (cert != null) {
            verified = VerifySignature.verifyX509(cert, signedDraft.getContent(), draftSignature.getSignature());
            if(!verified) {
                LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDraft.getPath()));
            } else {
                verifiedDraft =signedDraft;
            }
        } else if (publicKey != null) {
            verified = VerifySignature.verifyX509(publicKey, signedDraft.getContent(), draftSignature.getSignature());
            if(!verified) {
                LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDraft.getPath()));
            } else {
                verifiedDraft = signedDraft;
            }
        } else {
            throw new JocMissingKeyException("Neither PublicKey nor Certificate found for signature verification.");
        }
        return verifiedDraft;
    }

//    public static void updateRepo(
//            String versionId, Set<DBItemInventoryConfiguration> drafts, List<DBItemDeploymentHistory> alreadyDeployedToDelete,
//            String masterUrl, String masterJobschedulerId)
//            throws IllegalArgumentException, UriBuilderException, SOSException, JocException, IOException {
//        UpdateRepo updateRepo = new UpdateRepo();
//        updateRepo.setVersionId(versionId);
//        for (DBItemInventoryConfiguration draft : drafts) {
//            // TODO: uncomment when draft is refactored
//            SignedObject signedObject = new SignedObject();
////            signedObject.setString(draft.getContent());
//            Signature signature = new Signature();
////            signature.setSignatureString(draft.getSignedContent());
//            signedObject.setSignature(signature);
//            updateRepo.getChange().add(signedObject);
//        }
//        for (DBItemDeploymentHistory toDelete : alreadyDeployedToDelete) {
//            DeleteObject deletedObject = null;
//            switch(InventoryMeta.ConfigurationType.fromValue(toDelete.getType())) {
//                case WORKFLOW:
//                    deletedObject = new DeleteWorkflow(toDelete.getPath());
//                    break;
//                case AGENTCLUSTER:
//                    deletedObject = new DeleteAgentRef(toDelete.getPath());
//                    break;
//                case LOCK:
//                    // TODO: locks and other objects
//                    break;
//                case CALENDAR:
//                case FOLDER:
//                case JOBCLASS:
//                case JUNCTION:
//                case ORDER:
//                default:
//                    break;
//            }
//            updateRepo.getDelete().add(deletedObject);
//            
//        }
//        JOCJsonCommand command = new JOCJsonCommand();
//        command.setUriBuilderForCommands(masterUrl);
//        command.setAllowAllHostnameVerifier(false);
//        command.addHeader("Accept", "application/json");
//        command.addHeader("Content-Type", "application/json");
//        String updateRepoCommandBody = om.writeValueAsString(updateRepo);
//        LOGGER.debug(updateRepoCommandBody);
//        String response = command.getJsonStringFromPost(updateRepoCommandBody);
//    }
//    
    public static void updateRepo(
            String versionId, Map<DBItemInventoryConfiguration, DBItemDepSignatures> drafts,
            List<DBItemDeploymentHistory> alreadyDeployed,
            List<DBItemDeploymentHistory> alreadyDeployedtoDelete,
            String masterUrl, String masterJobschedulerId)
            throws IllegalArgumentException, UriBuilderException, SOSException, JocException, IOException {
        UpdateRepo updateRepo = new UpdateRepo();
        updateRepo.setVersionId(versionId);
        if (updateRepo.getChange() == null) {
            updateRepo.setChange(new ArrayList<SignedObject>());
        }
        if (updateRepo.getDelete() == null) {
            updateRepo.setDelete(new ArrayList<DeleteObject>());
        }
//        Set<JUpdateRepoOperation> updateRepoOperations = new HashSet<JUpdateRepoOperation>();
        for (DBItemInventoryConfiguration draft : drafts.keySet()) {
            if (draft != null) {
                // TODO: uncomment when draft is refactored
                SignedObject signedObject = new SignedObject();
                signedObject.setString(draft.getContent());
                Signature signature = new Signature();
                signature.setSignatureString(drafts.get(draft).getSignature());
                signedObject.setSignature(signature);
                updateRepo.getChange().add(signedObject);
//                GenericSignature sig = new GenericSignature("PGP", drafts.get(draft).getSignature());
//                SignedString signedString = new SignedString(draft.getContent(), sig);
//                JUpdateRepoOperation operation = JUpdateRepoOperation.addOrReplace(signedString);
//                updateRepoOperations.add(operation);
            }
        }
        for (DBItemDeploymentHistory toUpdate : alreadyDeployed) {
            SignedObject signedObject = new SignedObject();
            signedObject.setString(toUpdate.getContent());
            Signature signature = new Signature();
            signature.setSignatureString(toUpdate.getSignedContent());
            signedObject.setSignature(signature);
            updateRepo.getChange().add(signedObject);
//            updateRepoOperations.add(JUpdateRepoOperation.addOrReplace(SignedString.of(toUpdate.getContent(), "PGP", toUpdate.getSignedContent())));
        }
        for (DBItemDeploymentHistory toDelete : alreadyDeployedtoDelete) {
            DeleteObject deletedObject = null;
            switch(getDeployTypeFromOrdinal(toDelete.getObjectType())) {
                case WORKFLOW:
                    deletedObject = new DeleteWorkflow(toDelete.getPath());
//                    updateRepoOperations.add(JUpdateRepoOperation.delete(WorkflowPath.of(toDelete.getPath())));
                    break;
                case AGENT_REF:
                    deletedObject = new DeleteAgentRef(toDelete.getPath());
//                    updateRepoOperations.add(JUpdateRepoOperation.delete(AgentRefPath.of(toDelete.getPath())));
                    break;
                case LOCK:
                    // TODO:
                    break;
                case JUNCTION:
                    // TODO:
                    break;
                default:
                    break;
            }
            updateRepo.getDelete().add(deletedObject);
        }
//        try {
//            CompletableFuture<Either<Problem, Void>> future =  Proxy.of(masterUrl).api().updateRepo(VersionId.of(versionId), Flux.fromIterable(updateRepoOperations));
//            future.thenRun(new Runnable() {
//                @Override
//                public void run() {
//                    dbLayer.updateDeployedItems();
//                    // TODO Auto-generated method stub
//                }
//            });
//        } catch (ExecutionException | RuntimeException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
        JOCJsonCommand command = new JOCJsonCommand();
        command.setUriBuilderForCommands(masterUrl);
        command.setAllowAllHostnameVerifier(false);
        command.addHeader("Accept", "application/json");
        command.addHeader("Content-Type", "application/json");
        String updateRepoCommandBody = om.writeValueAsString(updateRepo);
        LOGGER.debug(updateRepoCommandBody);
        String response = command.getJsonStringFromPost(updateRepoCommandBody);
    }
    
    private static void updateVersionIdOnObject(DBItemInventoryConfiguration draft, String versionId, SOSHibernateSession session)
            throws JsonParseException, JsonMappingException, IOException, SOSHibernateException {
        switch(InventoryMeta.ConfigurationType.fromValue(draft.getType())) {
            case WORKFLOW:
                Workflow workflow = om.readValue(draft.getContent(), Workflow.class);
                workflow.setVersionId(versionId);
                draft.setContent(om.writeValueAsString(workflow));
                break;
            case AGENTCLUSTER:
                AgentRef agentRef = om.readValue(draft.getContent(), AgentRef.class);
                agentRef.setVersionId(versionId);
                draft.setContent(om.writeValueAsString(agentRef));
                break;
            case LOCK:
                // TODO: locks and other objects
                break;
        }
        session.update(draft);
    }

    public static Set<DBItemDeploymentHistory> cloneInvCfgsToDepHistory(
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> draftsWithSignature, String account, SOSHibernateSession hibernateSession,
            String versionId, Long controllerId, Date deploymentDate) throws SOSHibernateException {
        Set<DBItemDeploymentHistory> deployedObjects = new HashSet<DBItemDeploymentHistory>();
        for (DBItemInventoryConfiguration draft : draftsWithSignature.keySet()) {
            DBItemDeploymentHistory newDeployedObject = new DBItemDeploymentHistory();
            newDeployedObject.setAccount(account);
            // TODO: get Version to set here
            newDeployedObject.setVersion(null);
            newDeployedObject.setPath(draft.getPath());
            newDeployedObject.setObjectType(
                    PublishUtils.mapInventoyMetaConfigurationType(InventoryMeta.ConfigurationType.fromValue(draft.getType())).ordinal());
            newDeployedObject.setCommitId(versionId);
            newDeployedObject.setContent(draft.getContent());
            newDeployedObject.setSignedContent(draftsWithSignature.get(draft).getSignature());
            newDeployedObject.setDeploymentDate(deploymentDate);
            newDeployedObject.setControllerId(controllerId);
            newDeployedObject.setInventoryConfigurationId(draft.getId());
            newDeployedObject.setOperation(OperationType.UPDATE.value());
            hibernateSession.save(newDeployedObject);
            deployedObjects.add(newDeployedObject);
        }
        return deployedObjects;
    }
    
    public static Set<DBItemDeploymentHistory> updateDeletedDepHistory(
            List<DBItemDeploymentHistory> toDelete, DBLayerDeploy dbLayer) throws SOSHibernateException {
        Set<DBItemDeploymentHistory> deletedObjects = new HashSet<DBItemDeploymentHistory>();
        for (DBItemDeploymentHistory delete : toDelete) {
            delete.setOperation(OperationType.DELETE.value());
            delete.setDeletedDate(Date.from(Instant.now()));
            dbLayer.getSession().update(delete);            
            deletedObjects.add(delete);
        }
        return deletedObjects;
    }
    
    public static void prepareNextInvConfigGeneration(Set<DBItemInventoryConfiguration> drafts, SOSHibernateSession hibernateSession)
            throws SOSHibernateException {
        for (DBItemInventoryConfiguration draft : drafts) {
            draft.setDeployed(true);
            draft.setModified(Date.from(Instant.now()));
            hibernateSession.update(draft);
        }
    }
    
    public static JocKeyAlgorythm getKeyAlgorythm(JocKeyPair keyPair) {
        if (keyPair.getPrivateKey() != null) {
            if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER)) {
                return JocKeyAlgorythm.PGP;
            } else {
                return JocKeyAlgorythm.RSA;
            }
        } else if (keyPair.getPublicKey() != null && keyPair.getCertificate() == null) {
            if (keyPair.getPublicKey().startsWith(SOSPGPConstants.PUBLIC_PGP_KEY_HEADER)) {
                return JocKeyAlgorythm.PGP;
            } else {
                return JocKeyAlgorythm.RSA;
            }
        } else if (keyPair.getPublicKey() != null && keyPair.getCertificate() != null) {
            return JocKeyAlgorythm.RSA;
        }
        // DEFAULT
        return JocKeyAlgorythm.RSA;
    }

    public static JocKeyAlgorythm getKeyAlgorythm(String key) {
        if (key.startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER) || key.startsWith(SOSPGPConstants.PUBLIC_PGP_KEY_HEADER)) {
            return JocKeyAlgorythm.PGP;
        } else {
            return JocKeyAlgorythm.RSA;
        }
    }
    
    public static DeployType getDeployTypeFromOrdinal (Integer ordinal) {
        return DeployType.values()[ordinal];
    }

    public static DeployType mapInventoyMetaConfigurationType(InventoryMeta.ConfigurationType inventoryType) {
        switch(inventoryType) {
            case WORKFLOW:
                return DeployType.WORKFLOW;
            case AGENTCLUSTER:
                return DeployType.AGENT_REF;
            case LOCK:
                return DeployType.LOCK;
            case JUNCTION:
                return DeployType.JUNCTION;
            default:
                return DeployType.WORKFLOW;
        }
    }
    
    public static InventoryMeta.ConfigurationType mapDeployType (DeployType deployType) {
        switch(deployType) {
            case WORKFLOW:
                return InventoryMeta.ConfigurationType.WORKFLOW;
            case AGENT_REF:
                return InventoryMeta.ConfigurationType.AGENTCLUSTER;
            case LOCK:
                return InventoryMeta.ConfigurationType.LOCK;
            case JUNCTION:
                return InventoryMeta.ConfigurationType.JUNCTION;
            default:
                return InventoryMeta.ConfigurationType.WORKFLOW;
        }
    }
    
}