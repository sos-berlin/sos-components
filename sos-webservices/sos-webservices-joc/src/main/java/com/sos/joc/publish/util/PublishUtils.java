package com.sos.joc.publish.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import javax.ws.rs.core.StreamingOutput;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.sign.keys.sign.SignObject;
import com.sos.commons.sign.keys.verify.VerifySignature;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.job.Job;
import com.sos.jobscheduler.model.jobclass.JobClass;
import com.sos.jobscheduler.model.jobclass.JobClassEdit;
import com.sos.jobscheduler.model.jobclass.JobClassPublish;
import com.sos.jobscheduler.model.junction.Junction;
import com.sos.jobscheduler.model.junction.JunctionEdit;
import com.sos.jobscheduler.model.junction.JunctionPublish;
import com.sos.jobscheduler.model.lock.Lock;
import com.sos.jobscheduler.model.lock.LockEdit;
import com.sos.jobscheduler.model.lock.LockPublish;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.jobscheduler.model.workflow.WorkflowEdit;
import com.sos.jobscheduler.model.workflow.WorkflowPublish;
import com.sos.joc.Globals;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocImportException;
import com.sos.joc.exceptions.JocMissingKeyException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.exceptions.JocSignatureVerificationException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.calendar.Calendar;
import com.sos.joc.model.calendar.NonWorkingDaysCalendarEdit;
import com.sos.joc.model.calendar.WorkingDaysCalendarEdit;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.common.CalendarType;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.joc.JocMetaInfo;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.DeployablesFilter;
import com.sos.joc.model.publish.DeployablesValidFilter;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.ReleasablesFilter;
import com.sos.joc.model.publish.Signature;
import com.sos.joc.model.publish.SignaturePath;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.JocKeyType;
import com.sos.joc.publish.common.ConfigurationObjectFileExtension;
import com.sos.joc.publish.common.JSObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpDownloadMapper;
import com.sos.joc.publish.mapper.UpdateableWorkflowJobAgentName;
import com.sos.webservices.order.initiator.model.Schedule;
import com.sos.webservices.order.initiator.model.ScheduleEdit;

import io.vavr.control.Either;
import js7.base.crypt.SignedString;
import js7.base.crypt.SignerId;
import js7.base.problem.Problem;
import js7.data.item.VersionId;
import js7.data.workflow.WorkflowPath;
import js7.proxy.javaapi.data.item.JUpdateItemOperation;
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

    public static void storeKey(JocKeyPair keyPair, SOSHibernateSession hibernateSession, String account, JocSecurityLevel secLvl)
            throws SOSHibernateException {
        DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
        if (keyPair != null) {
            if (keyPair.getPrivateKey() != null) {
                dbLayerKeys.saveOrUpdateKey(
                        JocKeyType.PRIVATE.value(), 
                        keyPair.getPrivateKey(), 
                        keyPair.getCertificate(), 
                        account, 
                        secLvl, 
                        keyPair.getKeyAlgorithm());
            } else if (keyPair.getPrivateKey() == null && keyPair.getPublicKey() != null) {
                dbLayerKeys.saveOrUpdateKey(
                        JocKeyType.PUBLIC.value(), 
                        keyPair.getPublicKey(), 
                        keyPair.getCertificate(), 
                        account, 
                        secLvl, 
                        keyPair.getKeyAlgorithm());
            } else if (keyPair.getPrivateKey() == null && keyPair.getPublicKey() == null && keyPair.getCertificate() != null) {
                switch(secLvl) {
                case LOW:
                case MEDIUM:
                    dbLayerKeys.saveOrUpdateKey(
                            JocKeyType.PRIVATE.value(), 
                            keyPair.getCertificate(), 
                            account, 
                            secLvl, 
                            keyPair.getKeyAlgorithm());
                    break;
                case HIGH:
                    dbLayerKeys.saveOrUpdateKey(
                            JocKeyType.PUBLIC.value(), 
                            keyPair.getCertificate(), 
                            account, 
                            secLvl, 
                            keyPair.getKeyAlgorithm());
                }
            } 
        }
    }

    public static Map<DBItemInventoryConfiguration, DBItemDepSignatures> getDraftsWithSignature(String commitId, String account,
            Set<DBItemInventoryConfiguration> unsignedDrafts, Set<UpdateableWorkflowJobAgentName> updateableAgentNames, String controllerId, 
            SOSHibernateSession session, JocSecurityLevel secLvl) 
                throws JocMissingKeyException, JsonParseException, JsonMappingException, SOSHibernateException, IOException, PGPException, 
                NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, CertificateException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account, secLvl);
        if (keyPair != null) {
            return getDraftsWithSignature(commitId, account, unsignedDrafts, updateableAgentNames, keyPair, controllerId, session);
        } else {
            throw new JocMissingKeyException("No Key found for this account.");
        }
    }

    public static Map<DBItemInventoryConfiguration, DBItemDepSignatures> getDraftWithSignature(String versionId, String account,
            DBItemInventoryConfiguration unsignedDraft, Set<UpdateableWorkflowJobAgentName> updateableAgentNames, String controllerId,
            SOSHibernateSession session, JocSecurityLevel secLvl) 
                throws JocMissingKeyException, JsonParseException, JsonMappingException, SOSHibernateException, IOException, PGPException, 
                NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, CertificateException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account, secLvl);
        if (keyPair != null) {
            return getDraftWithSignature(versionId, account, unsignedDraft, updateableAgentNames, keyPair, controllerId, session);
        } else {
            throw new JocMissingKeyException("No Key found for this account.");
        }
    }

    public static Map<DBItemInventoryConfiguration, DBItemDepSignatures> getDraftsWithSignature(String commitId, String account,
            Set<DBItemInventoryConfiguration> unsignedDrafts, Set<UpdateableWorkflowJobAgentName> updateableAgentNames, JocKeyPair keyPair, 
            String controllerId, SOSHibernateSession session) 
                    throws JocMissingKeyException, JsonParseException, JsonMappingException, SOSHibernateException, IOException,
                    PGPException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, CertificateException {
        Map<DBItemInventoryConfiguration, DBItemDepSignatures> signedDrafts = new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
        if (keyPair.getPrivateKey() == null || keyPair.getPrivateKey().isEmpty()) {
            throw new JocMissingKeyException(
                    "No private key found for signing! - Please check your private key from the key management section in your profile.");
        } else {
            DBItemDepSignatures sig = null;
            Set<DBItemInventoryConfiguration> unsignedDraftsUpdated = unsignedDrafts.stream()
                    .map(item -> cloneDraftToUpdate(item)).collect(Collectors.toSet());
            for (DBItemInventoryConfiguration draft : unsignedDraftsUpdated) {
                updateVersionIdOnDraftObject(draft, commitId);
                // update agentName in Workflow jobs before signing agentName -> agentId
                if (draft.getTypeAsEnum().equals(ConfigurationType.WORKFLOW)) {
                    replaceAgentNameWithAgentId(draft, updateableAgentNames, controllerId);
                }
                if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                    sig = new DBItemDepSignatures();
                    sig.setAccount(account);
                    sig.setInvConfigurationId(draft.getId());
                    sig.setModified(Date.from(Instant.now()));
                    sig.setSignature(SignObject.signPGP(keyPair.getPrivateKey(), draft.getContent(), null));
                    signedDrafts.put(draft, sig);
                } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                    KeyPair kp = null;
                    if (keyPair.getPrivateKey().startsWith(SOSKeyConstants.PRIVATE_RSA_KEY_HEADER)) {
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
                } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                    KeyPair kp = KeyUtil.getKeyPairFromECDSAPrivatKeyString(keyPair.getPrivateKey());
                    sig = new DBItemDepSignatures();
                    sig.setAccount(account);
                    sig.setInvConfigurationId(draft.getId());
                    sig.setModified(Date.from(Instant.now()));
//                    X509Certificate cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                    sig.setSignature(SignObject.signX509(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, kp.getPrivate(), draft.getContent()));
                    signedDrafts.put(draft, sig);
                }
                if (sig != null) {
                    session.save(sig);
                }
            }
        }
        return signedDrafts;
    }

    public static Map<DBItemInventoryConfiguration, DBItemDepSignatures> getDraftWithSignature(String commitId, String account,
            DBItemInventoryConfiguration unsignedDraft, Set<UpdateableWorkflowJobAgentName> updateableAgentNames, JocKeyPair keyPair,
            String controllerId, SOSHibernateSession session) 
                    throws JocMissingKeyException, JsonParseException, JsonMappingException, SOSHibernateException, IOException,
                    PGPException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, CertificateException {
        Map<DBItemInventoryConfiguration, DBItemDepSignatures> signedDrafts = new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
        if (keyPair.getPrivateKey() == null || keyPair.getPrivateKey().isEmpty()) {
            throw new JocMissingKeyException(
                    "No private key found fo signing! - Please check your private key from the key management section in your profile.");
        } else {
            DBItemDepSignatures sig = null;
            DBItemInventoryConfiguration unsignedDraftUpdated = cloneDraftToUpdate(unsignedDraft);
            updateVersionIdOnDraftObject(unsignedDraftUpdated, commitId);
            // update agentName in Workflow jobs before signing agentName -> agentId
            if (unsignedDraft.getTypeAsEnum().equals(ConfigurationType.WORKFLOW)) {
                replaceAgentNameWithAgentId(unsignedDraftUpdated, updateableAgentNames, controllerId);
            }
            if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                sig = new DBItemDepSignatures();
                sig.setAccount(account);
                sig.setInvConfigurationId(unsignedDraftUpdated.getId());
                sig.setModified(Date.from(Instant.now()));
                sig.setSignature(SignObject.signPGP(keyPair.getPrivateKey(), unsignedDraftUpdated.getContent(), null));
                signedDrafts.put(unsignedDraftUpdated, sig);
            } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                KeyPair kp = null;
                if (keyPair.getPrivateKey().startsWith(SOSKeyConstants.PRIVATE_RSA_KEY_HEADER)) {
                    kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(keyPair.getPrivateKey());
                } else {
                    kp = KeyUtil.getKeyPairFromPrivatKeyString(keyPair.getPrivateKey());
                }
                sig = new DBItemDepSignatures();
                sig.setAccount(account);
                sig.setInvConfigurationId(unsignedDraftUpdated.getId());
                sig.setModified(Date.from(Instant.now()));
                sig.setSignature(SignObject.signX509(kp.getPrivate(), unsignedDraftUpdated.getContent()));
                signedDrafts.put(unsignedDraftUpdated, sig);
            } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                KeyPair kp = KeyUtil.getKeyPairFromECDSAPrivatKeyString(keyPair.getPrivateKey());
                sig = new DBItemDepSignatures();
                sig.setAccount(account);
                sig.setInvConfigurationId(unsignedDraftUpdated.getId());
                sig.setModified(Date.from(Instant.now()));
//                X509Certificate cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                sig.setSignature(SignObject.signX509(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, kp.getPrivate(), unsignedDraftUpdated.getContent()));
                signedDrafts.put(unsignedDraftUpdated, sig);
            }
            if (sig != null) {
                session.save(sig);
            }
        }
        return signedDrafts;
    }

    public static Map<DBItemDeploymentHistory, DBItemDepSignatures> getDeploymentsWithSignature(String commitId, String account,
            Set<DBItemDeploymentHistory> depHistoryToRedeploy, SOSHibernateSession session, JocSecurityLevel secLvl)
                    throws JocMissingKeyException, JsonParseException, JsonMappingException, SOSHibernateException, IOException, PGPException,
                    NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account, secLvl);
        if (keyPair != null) {
            return getDeploymentsWithSignature(commitId, account, depHistoryToRedeploy, keyPair, session);
        } else {
            throw new JocMissingKeyException("No Key found for this account.");
        }
    }

    public static Map<DBItemDeploymentHistory, DBItemDepSignatures> getDeploymentsWithSignature(String commitId, String account,
            Set<DBItemDeploymentHistory> depHistoryToRedeploy, JocKeyPair keyPair, SOSHibernateSession session) throws JocMissingKeyException,
            JsonParseException, JsonMappingException, SOSHibernateException, IOException, PGPException, NoSuchAlgorithmException,
            InvalidKeySpecException, InvalidKeyException, SignatureException {
        Map<DBItemDeploymentHistory, DBItemDepSignatures> signedReDeployable = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
        if (keyPair.getPrivateKey() == null || keyPair.getPrivateKey().isEmpty()) {
            throw new JocMissingKeyException(
                    "No private key found for signing! - Please check your private key from the key management section in your profile.");
        } else {
            DBItemDepSignatures sig = null;
            for (DBItemDeploymentHistory deployed : depHistoryToRedeploy) {
                updateVersionIdOnDeployedObject(deployed, commitId, session);
                if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                    sig = new DBItemDepSignatures();
                    sig.setAccount(account);
                    sig.setDepHistoryId(deployed.getId());
                    sig.setInvConfigurationId(deployed.getInventoryConfigurationId());
                    sig.setModified(Date.from(Instant.now()));
                    sig.setSignature(SignObject.signPGP(keyPair.getPrivateKey(), deployed.getContent(), null));
                    signedReDeployable.put(deployed, sig);
                } else {
                    KeyPair kp = null;
                    String signerAlgorithm = null;
                    if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                        kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(keyPair.getPrivateKey());
                        signerAlgorithm = SOSKeyConstants.RSA_SIGNER_ALGORITHM;
                    } else {
                        kp = KeyUtil.getKeyPairFromECDSAPrivatKeyString(keyPair.getPrivateKey());
                        signerAlgorithm = SOSKeyConstants.ECDSA_SIGNER_ALGORITHM;
                    }
                    sig = new DBItemDepSignatures();
                    sig.setAccount(account);
                    sig.setDepHistoryId(deployed.getId());
                    sig.setInvConfigurationId(deployed.getInventoryConfigurationId());
                    sig.setModified(Date.from(Instant.now()));
                    sig.setSignature(SignObject.signX509(signerAlgorithm, kp.getPrivate(), deployed.getContent()));
                    signedReDeployable.put(deployed, sig);
                }
                if (sig != null) {
                    session.save(sig);
                }
            }
        }
        return signedReDeployable;
    }

    public static DBItemInventoryConfiguration verifySignature(String account, DBItemInventoryConfiguration signedDraft,
            DBItemDepSignatures draftSignature, SOSHibernateSession session, JocSecurityLevel secLvl) throws SOSHibernateException, IOException,
            PGPException, InvalidKeyException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, JocMissingKeyException,
            SignatureException, NoSuchProviderException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account, secLvl);
        if (keyPair != null) {
            if (keyPair.getPrivateKey() != null) {
                if (keyPair.getPrivateKey().startsWith(SOSKeyConstants.PRIVATE_PGP_KEY_HEADER)) {
                    return verifyPGPSignature(account, signedDraft, draftSignature, keyPair);
                } else {
                    return verifyRSASignature(signedDraft, draftSignature, keyPair);
                }
            } else if (keyPair.getPublicKey() != null) {
                if (keyPair.getPublicKey().startsWith(SOSKeyConstants.PUBLIC_PGP_KEY_HEADER)) {
                    return verifyPGPSignature(account, signedDraft, draftSignature, keyPair);
                } else {
                    return verifyRSASignature(signedDraft, draftSignature, keyPair);
                }
            } else if (keyPair.getCertificate() != null) {
                return verifyRSASignature(signedDraft, draftSignature, keyPair);
            } else {
                throw new JocMissingKeyException(String.format("No key or certificate provided for the account \"%1$s\".", account));
            }
        } else {
            throw new JocMissingKeyException(String.format("No key or certificate provided for the account \"%1$s\".", account));
        }
    }

    public static DBItemDeploymentHistory verifySignature(String account, DBItemDeploymentHistory signedDeployments,
            DBItemDepSignatures draftSignature, SOSHibernateSession session, JocSecurityLevel secLvl) throws SOSHibernateException, IOException,
            PGPException, InvalidKeyException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, JocMissingKeyException,
            SignatureException, NoSuchProviderException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account, secLvl);
        if (keyPair != null) {
            if (keyPair.getPrivateKey() != null) {
                if (keyPair.getPrivateKey().startsWith(SOSKeyConstants.PRIVATE_PGP_KEY_HEADER)) {
                    return verifyPGPSignature(account, signedDeployments, draftSignature, keyPair);
                } else {
                    return verifyRSASignature(signedDeployments, draftSignature, keyPair);
                }
            } else if (keyPair.getPublicKey() != null) {
                if (keyPair.getPublicKey().startsWith(SOSKeyConstants.PUBLIC_PGP_KEY_HEADER)) {
                    return verifyPGPSignature(account, signedDeployments, draftSignature, keyPair);
                } else {
                    return verifyRSASignature(signedDeployments, draftSignature, keyPair);
                }
            } else if (keyPair.getCertificate() != null) {
                return verifyRSASignature(signedDeployments, draftSignature, keyPair);
            } else {
                throw new JocMissingKeyException(String.format("No key or certificate provided for the account \"%1$s\".", account));
            }
        } else {
            throw new JocMissingKeyException(String.format("No key or certificate provided for the account \"%1$s\".", account));
        }
    }

//    public static Set<DBItemInventoryConfiguration> verifyPGPSignatures(String account, Set<DBItemInventoryConfiguration> signedDrafts,
//            JocKeyPair keyPair, SOSHibernateSession connection) throws SOSHibernateException, IOException, PGPException {
//        Set<DBItemInventoryConfiguration> verifiedDrafts = new HashSet<DBItemInventoryConfiguration>();
//        String publicKey = null;
//        if (keyPair.getPublicKey() == null) {
//            publicKey = KeyUtil.extractPublicKey(keyPair.getPrivateKey());
//        } else {
//            publicKey = keyPair.getPublicKey();
//        }
//        Boolean verified = false;
//        for (DBItemInventoryConfiguration draft : signedDrafts) {
//            DBLayerDeploy dbLayer = new DBLayerDeploy(connection);
//            DBItemDepSignatures dbSignature = dbLayer.getSignature(draft.getId());
//            if(dbSignature != null) {
//                verified = VerifySignature.verifyPGP(publicKey, draft.getContent(), dbSignature.getSignature());
//                if (!verified) {
//                    LOGGER.trace(
//                            String.format("Signature of object %1$s could not be verified! Object will not be deployed.", draft.getPath()));
//                } else {
//                    verifiedDrafts.add(draft);
//                }
//            }
//        }
//        return verifiedDrafts;
//    }
//
    public static DBItemInventoryConfiguration verifyPGPSignature(String account, DBItemInventoryConfiguration signedDraft,
            DBItemDepSignatures draftSignature, JocKeyPair keyPair) throws SOSHibernateException, IOException, PGPException {
        DBItemInventoryConfiguration verifiedDraft = null;
        String publicKey = null;
        if (keyPair.getPublicKey() == null) {
            publicKey = KeyUtil.extractPublicKey(keyPair.getPrivateKey());
        } else {
            publicKey = keyPair.getPublicKey();
        }
        Boolean verified = false;
        verified = VerifySignature.verifyPGP(publicKey, signedDraft.getContent(), draftSignature.getSignature());
        if (!verified) {
            LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDraft.getPath()));
        } else {
            verifiedDraft = signedDraft;
        }
        return verifiedDraft;
    }

    public static DBItemDeploymentHistory verifyPGPSignature(String account, DBItemDeploymentHistory signedDeployment,
            DBItemDepSignatures deployedSignature, JocKeyPair keyPair) throws SOSHibernateException, IOException, PGPException {
        DBItemDeploymentHistory verifiedDeployment = null;
        String publicKey = null;
        if (keyPair.getPublicKey() == null) {
            publicKey = KeyUtil.extractPublicKey(keyPair.getPrivateKey());
        } else {
            publicKey = keyPair.getPublicKey();
        }
        Boolean verified = false;
        verified = VerifySignature.verifyPGP(publicKey, signedDeployment.getContent(), deployedSignature.getSignature());
        if (!verified) {
            LOGGER.trace(
                    String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDeployment.getPath()));
        } else {
            verifiedDeployment = signedDeployment;
        }
        return verifiedDeployment;
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
            publicKey = KeyUtil.getPublicKeyFromString(KeyUtil.decodePublicKeyString(jocKeyPair.getPublicKey()));
        }
        if (cert == null && publicKey == null) {
            KeyPair kp = null;
            if (jocKeyPair.getPrivateKey().startsWith(SOSKeyConstants.PRIVATE_RSA_KEY_HEADER)) {
                kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(jocKeyPair.getPrivateKey());
            } else {
                kp = KeyUtil.getKeyPairFromPrivatKeyString(jocKeyPair.getPrivateKey());
            }
            publicKey = kp.getPublic();
        }
        Boolean verified = false;
        if (cert != null) {
            verified = VerifySignature.verifyX509(cert, signedDraft.getContent(), draftSignature.getSignature());
            if (!verified) {
                LOGGER.trace(
                        String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDraft.getPath()));
            } else {
                verifiedDraft = signedDraft;
            }
        } else if (publicKey != null) {
            verified = VerifySignature.verifyX509(publicKey, signedDraft.getContent(), draftSignature.getSignature());
            if (!verified) {
                LOGGER.trace(
                        String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDraft.getPath()));
            } else {
                verifiedDraft = signedDraft;
            }
        } else {
            throw new JocMissingKeyException("Neither PublicKey nor Certificate found for signature verification.");
        }
        return verifiedDraft;
    }

    public static DBItemDeploymentHistory verifyRSASignature(DBItemDeploymentHistory signedDeployment, DBItemDepSignatures deployedSignature,
            JocKeyPair jocKeyPair) throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, JocMissingKeyException,
            InvalidKeyException, SignatureException, NoSuchProviderException, IOException {
        DBItemDeploymentHistory verifiedDeployment = null;
        Certificate cert = null;
        PublicKey publicKey = null;
        if (jocKeyPair.getCertificate() != null && !jocKeyPair.getCertificate().isEmpty()) {
            cert = KeyUtil.getCertificate(jocKeyPair.getCertificate());
        } else if (jocKeyPair.getPublicKey() != null && !jocKeyPair.getPublicKey().isEmpty()) {
            publicKey = KeyUtil.getPublicKeyFromString(KeyUtil.decodePublicKeyString(jocKeyPair.getPublicKey()));
        }
        if (cert == null && publicKey == null) {
            KeyPair kp = null;
            if (jocKeyPair.getPrivateKey().startsWith(SOSKeyConstants.PRIVATE_RSA_KEY_HEADER)) {
                kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(jocKeyPair.getPrivateKey());
            } else {
                kp = KeyUtil.getKeyPairFromPrivatKeyString(jocKeyPair.getPrivateKey());
            }
            publicKey = kp.getPublic();
        }
        Boolean verified = false;
        if (cert != null) {
            verified = VerifySignature.verifyX509(cert, signedDeployment.getContent(), deployedSignature.getSignature());
            if (!verified) {
                LOGGER.trace(String.format(
                        "Signature of object %1$s could not be verified! Object will not be deployed.", signedDeployment.getPath()));
            } else {
                verifiedDeployment = signedDeployment;
            }
        } else if (publicKey != null) {
            verified = VerifySignature.verifyX509(publicKey, signedDeployment.getContent(), deployedSignature.getSignature());
            if (!verified) {
                LOGGER.trace(String.format(
                        "Signature of object %1$s could not be verified! Object will not be deployed.", signedDeployment.getPath()));
            } else {
                verifiedDeployment = signedDeployment;
            }
        } else {
            throw new JocMissingKeyException("Neither PublicKey nor Certificate found for signature verification.");
        }
        return verifiedDeployment;
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdatePGP(
            String commitId,  Map<DBItemInventoryConfiguration, DBItemDepSignatures> drafts,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, String controllerId, DBLayerDeploy dbLayer)
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateRepoOperations = new HashSet<JUpdateItemOperation>();
        updateRepoOperations.addAll(
                drafts.keySet().stream().map(
                        item -> JUpdateItemOperation.addOrChange(SignedString.of(
                                item.getContent(),
                                SOSKeyConstants.PGP_ALGORITHM_NAME,
                                drafts.get(item).getSignature()))
                        ).collect(Collectors.toSet())
                );
        updateRepoOperations.addAll(
                alreadyDeployed.keySet().stream().map(
                        item -> JUpdateItemOperation.addOrChange(SignedString.of(
                                item.getContent(),
                                SOSKeyConstants.PGP_ALGORITHM_NAME,
                                alreadyDeployed.get(item).getSignature()))
                        ).collect(Collectors.toSet())
                );
        return ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId))),
                Flux.fromIterable(updateRepoOperations)));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdatePGP2(
            String commitId,  Map<JSObject, DBItemDepSignatures> drafts,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, String controllerId, DBLayerDeploy dbLayer)
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateRepoOperations = new HashSet<JUpdateItemOperation>();
        updateRepoOperations.addAll(
                drafts.keySet().stream().map(
                        item -> {
                            switch(item.getObjectType()) {
                                case WORKFLOW:
                                try {
                                    return JUpdateItemOperation.addOrChange(SignedString.of(
                                                    Globals.objectMapper.writeValueAsString(((WorkflowPublish)item).getContent()),
                                                    SOSKeyConstants.PGP_ALGORITHM_NAME,
                                                    drafts.get(item).getSignature()));
                                } catch (JsonProcessingException e) {}
                                case LOCK:
                                try {
                                    return JUpdateItemOperation.addOrChange(SignedString.of(
                                                    Globals.objectMapper.writeValueAsString(((LockPublish)item).getContent()),
                                                    SOSKeyConstants.PGP_ALGORITHM_NAME,
                                                    drafts.get(item).getSignature()));
                                } catch (JsonProcessingException e) {}
                                case JUNCTION:
                                try {
                                    return JUpdateItemOperation.addOrChange(SignedString.of(
                                                    Globals.objectMapper.writeValueAsString(((JunctionPublish)item).getContent()),
                                                    SOSKeyConstants.PGP_ALGORITHM_NAME,
                                                    drafts.get(item).getSignature()));
                                } catch (JsonProcessingException e) {}
                                case JOBCLASS:
                                try {
                                    return JUpdateItemOperation.addOrChange(SignedString.of(
                                                    Globals.objectMapper.writeValueAsString(((JobClassPublish)item).getContent()),
                                                    SOSKeyConstants.PGP_ALGORITHM_NAME,
                                                    drafts.get(item).getSignature()));
                                } catch (JsonProcessingException e) {}
                                default:
                                    return null;
                            }
                        }).filter(Objects::nonNull).collect(Collectors.toSet()));
        updateRepoOperations.addAll(
                alreadyDeployed.keySet().stream().map(
                        item -> JUpdateItemOperation.addOrChange(SignedString.of(
                                item.getContent(),
                                SOSKeyConstants.PGP_ALGORITHM_NAME,
                                alreadyDeployed.get(item).getSignature()))
                        ).collect(Collectors.toSet())
                );
        return ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId))),
                Flux.fromIterable(updateRepoOperations)));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdatePGP(
            String commitId,  List<DBItemDeploymentHistory> alreadyDeployed, String controllerId)
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateItems(Flux.concat(
                Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId))),
                Flux.fromIterable(alreadyDeployed).map(
                                item -> JUpdateItemOperation.addOrChange(SignedString.of(
                                        item.getContent(),
                                        SOSKeyConstants.PGP_ALGORITHM_NAME,
                                        item.getSignedContent()))
                                )
                ));
    }
    
    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdateWithX509(
            String commitId,  Map<DBItemInventoryConfiguration, DBItemDepSignatures> drafts,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, String controllerId, DBLayerDeploy dbLayer,
            String signatureAlgorithm, String signerDN)
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateRepoOperations = new HashSet<JUpdateItemOperation>();
        if (drafts != null) {
            updateRepoOperations.addAll(
                    drafts.keySet().stream().map(
                            item -> JUpdateItemOperation.addOrChange(SignedString.x509WithSignedId(
                                    item.getContent(),
                                    drafts.get(item).getSignature(),
                                    signatureAlgorithm,
                                    SignerId.of(signerDN)))
                            ).collect(Collectors.toSet())
                    );
        }
        if (alreadyDeployed != null) {
            updateRepoOperations.addAll(
                    alreadyDeployed.keySet().stream().map(
                            item -> JUpdateItemOperation.addOrChange(SignedString.x509WithSignedId(
                                    item.getContent(),
                                    alreadyDeployed.get(item).getSignature(),
                                    signatureAlgorithm,
                                    SignerId.of(signerDN)))
                            ).collect(Collectors.toSet())
                    );
        }
        return ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId))),
                Flux.fromIterable(updateRepoOperations)));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdateWithX509_2(
            String commitId,  Map<JSObject, DBItemDepSignatures> drafts,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, String controllerId, DBLayerDeploy dbLayer,
            String signatureAlgorithm, String signerDN)
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateRepoOperations = new HashSet<JUpdateItemOperation>();
        if (drafts != null) {
            updateRepoOperations.addAll(
                    drafts.keySet().stream().map(
                            item -> {
                            switch(item.getObjectType()) {
                                case WORKFLOW:
                                try {
                                    return JUpdateItemOperation.addOrChange(SignedString.x509WithSignedId(
                                                    om.writeValueAsString(((WorkflowPublish)item).getContent()),
                                                    drafts.get(item).getSignature(),
                                                    signatureAlgorithm,
                                                    SignerId.of(signerDN)));
                                } catch (JsonProcessingException e) {}
                                case LOCK:
                                try {
                                    return JUpdateItemOperation.addOrChange(SignedString.x509WithSignedId(
                                                    om.writeValueAsString(((LockPublish)item).getContent()),
                                                    drafts.get(item).getSignature(),
                                                    signatureAlgorithm,
                                                    SignerId.of(signerDN)));
                                } catch (JsonProcessingException e) {}
                                case JUNCTION:
                                try {
                                    return JUpdateItemOperation.addOrChange(SignedString.x509WithSignedId(
                                                    om.writeValueAsString(((JunctionPublish)item).getContent()),
                                                    drafts.get(item).getSignature(),
                                                    signatureAlgorithm,
                                                    SignerId.of(signerDN)));
                                } catch (JsonProcessingException e) {}
                                case JOBCLASS:
                                try {
                                    return JUpdateItemOperation.addOrChange(SignedString.x509WithSignedId(
                                                    om.writeValueAsString(((JobClassPublish)item).getContent()),
                                                    drafts.get(item).getSignature(),
                                                    signatureAlgorithm,
                                                    SignerId.of(signerDN)));
                                } catch (JsonProcessingException e) {}
                                default:
                                    return null;
                            }
                        }).filter(Objects::nonNull).collect(Collectors.toSet())
                    );
        }
        if (alreadyDeployed != null) {
            updateRepoOperations.addAll(
                    alreadyDeployed.keySet().stream().map(
                            item -> JUpdateItemOperation.addOrChange(SignedString.x509WithSignedId(
                                    item.getContent(),
                                    alreadyDeployed.get(item).getSignature(),
                                    signatureAlgorithm,
                                    SignerId.of(signerDN)))
                            ).collect(Collectors.toSet())
                    );
        }
        return ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId))),
                Flux.fromIterable(updateRepoOperations)));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdateWithX509(
            String commitId,  List<DBItemDeploymentHistory> alreadyDeployed, String controllerId, String signatureAlgorithm, String signerDN)
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> uro = alreadyDeployed.stream().map(
                item -> JUpdateItemOperation.addOrChange(SignedString.x509WithSignedId(
                        item.getContent(),
                        item.getSignedContent(),
                        signatureAlgorithm,
                        SignerId.of(signerDN)))
                ).collect(Collectors.toSet());
        return ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId))),
                Flux.fromIterable(uro)));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsDelete(String commitId,
            List<DBItemDeploymentHistory> alreadyDeployedtoDelete, String controllerId, DBLayerDeploy dbLayer, String keyAlgorithm)
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        if ("RSA".equals(keyAlgorithm) || "ECDSA".equals(keyAlgorithm)) {
            keyAlgorithm = "X509";
        }
        Set<JUpdateItemOperation> updateRepoOperations = new HashSet<JUpdateItemOperation>();
        if (alreadyDeployedtoDelete != null) {
            for (DBItemDeploymentHistory toDelete : alreadyDeployedtoDelete) {
                switch (DeployType.fromValue(toDelete.getType())) {
                case WORKFLOW:
                    updateRepoOperations.add(JUpdateItemOperation.deleteItem(WorkflowPath.of(toDelete.getPath())));
                    break;
                case JOBCLASS:
                    // TODO:
                case LOCK:
                    // TODO:
                case JUNCTION:
                    // TODO:
                    throw new JocNotImplementedException();
                default:
                    break;
                }
            }
        }
        return ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.just(JUpdateItemOperation.addVersion(VersionId.of(commitId))),
                Flux.fromIterable(updateRepoOperations)));
    }

    private static void updateVersionIdOnDraftObject(DBItemInventoryConfiguration draft, String commitId)
            throws JsonParseException, JsonMappingException, IOException, JocNotImplementedException {
        switch (ConfigurationType.fromValue(draft.getType())) {
        case WORKFLOW:
            Workflow workflow = om.readValue(draft.getContent(), Workflow.class);
            workflow.setVersionId(commitId);
            draft.setContent(om.writeValueAsString(workflow));
            break;
        case LOCK:
            // TODO: locks and other objects
        case WORKINGDAYSCALENDAR:
        case NONWORKINGDAYSCALENDAR:
        case FOLDER:
        case JOBCLASS:
        case JUNCTION:
        case SCHEDULE:
        default:
            throw new JocNotImplementedException();
        }
    }

    private static void updateVersionIdOnDeployedObject(DBItemDeploymentHistory deployed, String commitId, SOSHibernateSession session)
            throws JsonParseException, JsonMappingException, IOException, SOSHibernateException, JocNotImplementedException {

        switch (DeployType.fromValue(deployed.getType())) {
        case WORKFLOW:
            Workflow workflow = om.readValue(deployed.getContent(), Workflow.class);
            workflow.setVersionId(commitId);
            deployed.setContent(om.writeValueAsString(workflow));
            break;
        case LOCK:
            // TODO: locks and other objects
        case JUNCTION:
        default:
            throw new JocNotImplementedException();
        }
    }
    
    public static Set<UpdateableWorkflowJobAgentName> getUpdateableAgentRefInWorkflowJobs(DBItemInventoryConfiguration item, 
            String controllerId, DBLayerDeploy dbLayer) {
        return getUpdateableAgentRefInWorkflowJobs(item.getContent(), 
                ConfigurationType.fromValue(item.getType()), controllerId, dbLayer);
    }

    public static Set<UpdateableWorkflowJobAgentName> getUpdateableAgentRefInWorkflowJobs(DBItemDeploymentHistory item,
            String controllerId, DBLayerDeploy dbLayer) {
        return getUpdateableAgentRefInWorkflowJobs(item.getInvContent(), 
                ConfigurationType.fromValue(item.getType()), controllerId, dbLayer);
    }

    public static Set<UpdateableWorkflowJobAgentName> getUpdateableAgentRefInWorkflowJobs(String json, ConfigurationType type,
            String controllerId, DBLayerDeploy dbLayer) {
        Set<UpdateableWorkflowJobAgentName> update = new HashSet<UpdateableWorkflowJobAgentName>();
        try {
            if (ConfigurationType.WORKFLOW.equals(type)) {
                Workflow workflow = om.readValue(json, Workflow.class);
                workflow.getJobs().getAdditionalProperties().keySet().stream().forEach(jobname -> {
                    Job job = workflow.getJobs().getAdditionalProperties().get(jobname);
                    String agentName = job.getAgentId();
                    String agentId = dbLayer.getAgentIdFromAgentName(agentName, controllerId, workflow.getPath(), jobname);
                    update.add(
                            new UpdateableWorkflowJobAgentName(workflow.getPath(), jobname, job.getAgentId(), agentId, controllerId));
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e);
        }
        return update;
    }

    public static Set<DBItemDeploymentHistory> cloneInvConfigurationsToDepHistoryItems(
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> draftsWithSignature, Set<UpdateableWorkflowJobAgentName> updateableAgentNames, 
            String account, DBLayerDeploy dbLayerDeploy, String commitId, String controllerId, Date deploymentDate)
                    throws JsonParseException, JsonMappingException, IOException {
        Set<DBItemDeploymentHistory> deployedObjects;
        try {
            DBItemInventoryJSInstance controllerInstance = dbLayerDeploy.getController(controllerId);
            deployedObjects = new HashSet<DBItemDeploymentHistory>();
            for (DBItemInventoryConfiguration draft : draftsWithSignature.keySet()) {
                DBItemDeploymentHistory newDeployedObject = new DBItemDeploymentHistory();
                newDeployedObject.setAccount(account);
                // TODO: get Version to set here
                newDeployedObject.setVersion(null);
                newDeployedObject.setPath(draft.getPath());
                newDeployedObject.setFolder(draft.getFolder());
                newDeployedObject.setType(
                        PublishUtils.mapConfigurationType(ConfigurationType.fromValue(draft.getType())).intValue());
                newDeployedObject.setCommitId(commitId);
                newDeployedObject.setContent(draft.getContent());
                newDeployedObject.setSignedContent(draftsWithSignature.get(draft).getSignature());
                if (updateableAgentNames != null && draft.getTypeAsEnum().equals(ConfigurationType.WORKFLOW)) {
                    newDeployedObject.setInvContent(getContentWithOrigAgentName(draft, updateableAgentNames, controllerId));
                } else {
                    // nothing was replaced in the original
                    newDeployedObject.setInvContent(draft.getContent());
                }
                newDeployedObject.setDeploymentDate(deploymentDate);
                newDeployedObject.setControllerInstanceId(controllerInstance.getId());
                newDeployedObject.setControllerId(controllerId);
                newDeployedObject.setInventoryConfigurationId(draft.getId());
                newDeployedObject.setOperation(OperationType.UPDATE.value());
                newDeployedObject.setState(DeploymentState.DEPLOYED.value());
                dbLayerDeploy.getSession().save(newDeployedObject);
                deployedObjects.add(newDeployedObject);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return deployedObjects;
    }

    public static Set<DBItemDeploymentHistory> cloneInvConfigurationsToDepHistoryItems(
            Map<JSObject, DBItemDepSignatures> draftsWithSignature, String account, DBLayerDeploy dbLayerDeploy, String commitId,
            String controllerId, Date deploymentDate) throws JsonParseException, JsonMappingException, IOException {
        Set<DBItemDeploymentHistory> deployedObjects;
        try {
            DBItemInventoryJSInstance controllerInstance = dbLayerDeploy.getController(controllerId);
            deployedObjects = new HashSet<DBItemDeploymentHistory>();
            for (JSObject draft : draftsWithSignature.keySet()) {
                DBItemDeploymentHistory newDeployedObject = new DBItemDeploymentHistory();
                DBItemInventoryConfiguration original = dbLayerDeploy.getConfiguration(draft.getPath(), draft.getObjectType().intValue());
                newDeployedObject.setAccount(account);
                // TODO: get Version to set here
                newDeployedObject.setVersion(null);
                newDeployedObject.setPath(original.getPath());
                newDeployedObject.setFolder(original.getFolder());
                newDeployedObject.setType(draft.getObjectType().intValue());
                newDeployedObject.setCommitId(commitId);
                switch (draft.getObjectType()) {
                case WORKFLOW:
                    String workflow = Globals.objectMapper.writeValueAsString(((WorkflowPublish)draft).getContent());
                    newDeployedObject.setContent(workflow);
                    break;
                case LOCK:
                    String lock = Globals.objectMapper.writeValueAsString(((WorkflowPublish)draft).getContent());
                    newDeployedObject.setContent(lock);
                    break;
                case JUNCTION:
                    String junction = Globals.objectMapper.writeValueAsString(((WorkflowPublish)draft).getContent());
                    newDeployedObject.setContent(junction);
                    break;
                case JOBCLASS:
                    String jobclass = Globals.objectMapper.writeValueAsString(((WorkflowPublish)draft).getContent());
                    newDeployedObject.setContent(jobclass);
                    break;
                }
                newDeployedObject.setSignedContent(draftsWithSignature.get(draft).getSignature());
                newDeployedObject.setInvContent(original.getContent());
                newDeployedObject.setDeploymentDate(deploymentDate);
                newDeployedObject.setControllerInstanceId(controllerInstance.getId());
                newDeployedObject.setControllerId(controllerId);
                newDeployedObject.setInventoryConfigurationId(original.getId());
                newDeployedObject.setOperation(OperationType.UPDATE.value());
                newDeployedObject.setState(DeploymentState.DEPLOYED.value());
                dbLayerDeploy.getSession().save(newDeployedObject);
                deployedObjects.add(newDeployedObject);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return deployedObjects;
    }

//    public static Set<DBItemDeploymentHistory> cloneInvConfigurationsToDepHistoryItems(
//            Map<DBItemInventoryConfiguration, JSObject> importedObjects, String account, DBLayerDeploy dbLayerDeploy, String controllerId,
//            Date deploymentDate, String commitId) {
//        Set<DBItemDeploymentHistory> deployedObjects = new HashSet<DBItemDeploymentHistory>();
//        try {
//            DBItemInventoryJSInstance controllerInstance = dbLayerDeploy.getController(controllerId);
//            for (DBItemInventoryConfiguration draft : importedObjects.keySet()) {
//                DBItemDeploymentHistory newDeployedObject = new DBItemDeploymentHistory();
//                newDeployedObject.setAccount(account);
//                // TODO: get Version to set here
//                newDeployedObject.setVersion(null);
//                newDeployedObject.setPath(draft.getPath());
//                newDeployedObject.setFolder(draft.getFolder());
//                newDeployedObject.setType(
//                        PublishUtils.mapInventoryMetaConfigurationType(ConfigurationType.fromValue(draft.getType())).intValue());
//                newDeployedObject.setCommitId(commitId);
//                newDeployedObject.setContent(draft.getContent());
//                newDeployedObject.setSignedContent(importedObjects.get(draft).getSignedContent());
//                newDeployedObject.setDeploymentDate(deploymentDate);
//                newDeployedObject.setControllerInstanceId(controllerInstance.getId());
//                newDeployedObject.setControllerId(controllerId);
//                newDeployedObject.setInventoryConfigurationId(draft.getId());
//                newDeployedObject.setOperation(OperationType.UPDATE.value());
//                newDeployedObject.setState(DeploymentState.DEPLOYED.value());
//                dbLayerDeploy.getSession().save(newDeployedObject);
//                deployedObjects.add(newDeployedObject);
//            }
//        } catch (SOSHibernateException e) {
//            throw new JocSosHibernateException(e);
//        }
//        return deployedObjects;
//    }
//
    public static Set<DBItemDeploymentHistory> cloneDepHistoryItemsToRedeployed(
            Map<DBItemDeploymentHistory, DBItemDepSignatures> redeployedWithSignature, String account, DBLayerDeploy dbLayerDeploy,
            String commitId, String controllerId, Date deploymentDate) {
        Set<DBItemDeploymentHistory> deployedObjects;
        try {
            DBItemInventoryJSInstance controllerInstance = dbLayerDeploy.getController(controllerId);
            deployedObjects = new HashSet<DBItemDeploymentHistory>();
            for (DBItemDeploymentHistory redeployed : redeployedWithSignature.keySet()) {
                redeployed.setSignedContent(redeployedWithSignature.get(redeployed).getSignature());
                redeployed.setId(null);
                redeployed.setAccount(account);
                // TODO: get Version to set here
                redeployed.setVersion(null);
                redeployed.setCommitId(commitId);
                redeployed.setControllerId(controllerId);
                redeployed.setControllerInstanceId(controllerInstance.getId());
                redeployed.setDeploymentDate(deploymentDate);
                redeployed.setOperation(OperationType.UPDATE.value());
                redeployed.setState(DeploymentState.DEPLOYED.value());
                dbLayerDeploy.getSession().save(redeployed);
                deployedObjects.add(redeployed);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return deployedObjects;
    }

    public static Set<DBItemDeploymentHistory> cloneDepHistoryItemsToRedeployed(
            List<DBItemDeploymentHistory> redeployedItems, String account, DBLayerDeploy dbLayerDeploy, String controllerId, Date deploymentDate) {
        Set<DBItemDeploymentHistory> deployedObjects;
        try {
            DBItemInventoryJSInstance controllerInstance = dbLayerDeploy.getController(controllerId);
            deployedObjects = new HashSet<DBItemDeploymentHistory>();
            for (DBItemDeploymentHistory redeployed : redeployedItems) {
                redeployed.setId(null);
                redeployed.setAccount(account);
                redeployed.setControllerId(controllerId);
                redeployed.setControllerInstanceId(controllerInstance.getId());
                redeployed.setDeploymentDate(deploymentDate);
                redeployed.setOperation(OperationType.UPDATE.value());
                redeployed.setState(DeploymentState.DEPLOYED.value());
                dbLayerDeploy.getSession().save(redeployed);
                deployedObjects.add(redeployed);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return deployedObjects;
    }

    public static Set<DBItemDeploymentHistory> updateDeletedDepHistory(List<DBItemDeploymentHistory> toDelete, DBLayerDeploy dbLayer) {
        Set<DBItemDeploymentHistory> deletedObjects = new HashSet<DBItemDeploymentHistory>();
        try {
            for (DBItemDeploymentHistory delete : toDelete) {
                delete.setId(null);
                delete.setInventoryConfigurationId(dbLayer.getInventoryConfigurationIdByPathAndType(delete.getPath(), delete.getType()));
                delete.setOperation(OperationType.DELETE.value());
                delete.setState(DeploymentState.DEPLOYED.value());
                delete.setDeleteDate(Date.from(Instant.now()));
                delete.setDeploymentDate(Date.from(Instant.now()));
                dbLayer.getSession().save(delete);
                deletedObjects.add(delete);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return deletedObjects;
    }

    public static void prepareNextInvConfigGeneration(Set<DBItemInventoryConfiguration> drafts, 
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames, String controllerId, SOSHibernateSession hibernateSession) {
        try {
            for (DBItemInventoryConfiguration draft : drafts) {
                draft.setDeployed(true);
                draft.setModified(Date.from(Instant.now()));
                // update agentName with original in Workflow jobs before updating  agentId -> agentName
                if (updateableAgentNames != null && draft.getTypeAsEnum().equals(ConfigurationType.WORKFLOW)) {
                    replaceAgentIdWithOrigAgentName(draft, updateableAgentNames, controllerId);
                }
                hibernateSession.update(draft);
            }
        } catch (SOSHibernateException | IOException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public static void prepareNextInvConfigGeneration(Set<JSObject> drafts, 
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames, String controllerId, DBLayerDeploy dbLayer) {
        try {
            for (JSObject draft : drafts) {
                DBItemInventoryConfiguration configuration = dbLayer.getConfiguration(draft.getPath(), mapDeployType(draft.getObjectType()));
                configuration.setDeployed(true);
                configuration.setModified(Date.from(Instant.now()));
                // update agentName with original in Workflow jobs before updating  agentId -> agentName
                if (updateableAgentNames != null && draft.getObjectType().equals(DeployType.WORKFLOW)) {
                    replaceAgentIdWithOrigAgentName(configuration, updateableAgentNames, controllerId);
                }
                dbLayer.getSession().update(configuration);
            }
        } catch (SOSHibernateException | IOException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public static DeployType mapConfigurationType(ConfigurationType inventoryType) {
        switch (inventoryType) {
        case WORKFLOW:
            return DeployType.WORKFLOW;
        case LOCK:
            return DeployType.LOCK;
        case JUNCTION:
            return DeployType.JUNCTION;
        case JOBCLASS:
            return DeployType.JOBCLASS;
        default:
            return DeployType.WORKFLOW;
        }
    }

    public static ConfigurationType mapDeployType(DeployType deployType) {
        switch (deployType) {
        case WORKFLOW:
            return ConfigurationType.WORKFLOW;
        case LOCK:
            return ConfigurationType.LOCK;
        case JUNCTION:
            return ConfigurationType.JUNCTION;
        case JOBCLASS:
            return ConfigurationType.JOBCLASS;
        default:
            return ConfigurationType.WORKFLOW;
        }
    }

    public static <T extends DBItem> List<DBItemDeploymentHistory> checkPathRenamingForUpdate(
            Set<T> verifiedObjects, String controllerId, DBLayerDeploy dbLayer, String keyAlgorithm)
            throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        DBItemDeploymentHistory depHistory = null;
        DBItemInventoryConfiguration invConf = null;
        final String versionId = UUID.randomUUID().toString();
        // check first if a deploymentHistory item related to the configuration item exist
        List<DBItemDeploymentHistory> alreadyDeployedToDelete = new ArrayList<DBItemDeploymentHistory>();
        for (T object : verifiedObjects) {
            if (DBItemInventoryConfiguration.class.isInstance(object)) {
                invConf = (DBItemInventoryConfiguration) object;
                depHistory = dbLayer.getLatestDepHistoryItem(invConf, controllerId);
                // if operation of latest history item was 'delete', no need to delete again 
                if (depHistory != null && OperationType.DELETE.equals(OperationType.fromValue(depHistory.getOperation()))) {
                    depHistory = null;
                }
            } else {
                depHistory = (DBItemDeploymentHistory) object;
                invConf = dbLayer.getSession().get(DBItemInventoryConfiguration.class, depHistory.getInventoryConfigurationId());
            }
            // if so, check if the paths of both are the same
            if (depHistory != null && !depHistory.getPath().equals(invConf.getPath())) {
                // if not, delete the old deployed item via updateRepo before deploy of the new configuration
                depHistory.setCommitId(versionId);
                alreadyDeployedToDelete.add(depHistory);
            } 
        }
        return alreadyDeployedToDelete;
    }

    public static Map<ConfigurationObject, SignaturePath> readZipFileContentWithSignatures(InputStream inputStream)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException, 
            JocConfigurationException, DBOpenSessionException {
        Set<ConfigurationObject> objects = new HashSet<ConfigurationObject>();
        Set<SignaturePath> signaturePaths = new HashSet<SignaturePath>();
        Map<ConfigurationObject, SignaturePath> objectsWithSignature = new HashMap<ConfigurationObject, SignaturePath>();
        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(inputStream);
            ZipEntry entry = null;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
                ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
                byte[] binBuffer = new byte[8192];
                int binRead = 0;
                while ((binRead = zipStream.read(binBuffer, 0, 8192)) >= 0) {
                    outBuffer.write(binBuffer, 0, binRead);
                }
                // process deployables only
                SignaturePath signaturePath = new SignaturePath();
                Signature signature = new Signature();
                if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
                    WorkflowEdit workflowEdit = new WorkflowEdit();
                    Workflow workflow = om.readValue(outBuffer.toString(), Workflow.class);
                    if (checkObjectNotEmpty(workflow)) {
                        workflowEdit.setConfiguration(workflow);
                    } else {
                        throw new JocImportException(String.format("Workflow with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(), "")));
                    }
                    if (workflowEdit.getConfiguration().getPath() != null) {
                        workflowEdit.setPath(workflowEdit.getConfiguration().getPath());
                    } else {
                        workflowEdit.setPath(("/" + entryName).replace(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(), ""));
                        workflowEdit.getConfiguration().setPath(workflowEdit.getPath());
                    }
                    workflowEdit.setObjectType(ConfigurationType.WORKFLOW);
                    
                    objects.add(workflowEdit);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_PGP_SIGNATURE_FILE_EXTENSION.value())) {
                    signaturePath.setObjectPath(("/" + entryName).replace(JSObjectFileExtension.WORKFLOW_PGP_SIGNATURE_FILE_EXTENSION.value(), ""));
                    signature.setSignatureString(outBuffer.toString());
                    signaturePath.setSignature(signature);
                    signaturePaths.add(signaturePath);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_X509_SIGNATURE_FILE_EXTENSION.value())) {
                    signaturePath.setObjectPath(("/" + entryName).replace(JSObjectFileExtension.WORKFLOW_X509_SIGNATURE_FILE_EXTENSION.value(), ""));
                    signature.setSignatureString(outBuffer.toString());
                    signaturePath.setSignature(signature);
                    signaturePaths.add(signaturePath);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.LOCK_FILE_EXTENSION.value())) {
                    LockEdit lockEdit = new LockEdit();
                    Lock lock = om.readValue(outBuffer.toString(), Lock.class);
                    if (checkObjectNotEmpty(lock)) {
                        lockEdit.setConfiguration(lock);
                    } else {
                        throw new JocImportException(String.format("Lock with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.LOCK_FILE_EXTENSION.value(), "")));
                    }
                    if (lockEdit.getConfiguration().getPath() != null ) {
                        lockEdit.setPath(lockEdit.getConfiguration().getPath());
                    } else {
                        lockEdit.setPath(("/" + entryName).replace(JSObjectFileExtension.LOCK_FILE_EXTENSION.value(), ""));
                        lockEdit.getConfiguration().setPath(lockEdit.getPath());
                    }
                    lockEdit.setObjectType(ConfigurationType.LOCK);
                    objects.add(lockEdit);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.JUNCTION_FILE_EXTENSION.value())) {
                    JunctionEdit junctionEdit = new JunctionEdit();
                    Junction junction = om.readValue(outBuffer.toString(), Junction.class);
                    if (checkObjectNotEmpty(junction)) {
                        junctionEdit.setConfiguration(junction);
                    } else {
                        throw new JocImportException(String.format("Junction with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.JUNCTION_FILE_EXTENSION.value(), "")));
                    }
                    if (junctionEdit.getConfiguration().getPath() != null ) {
                        junctionEdit.setPath(junctionEdit.getConfiguration().getPath());
                    } else {
                        junctionEdit.setPath(("/" + entryName).replace(JSObjectFileExtension.JUNCTION_FILE_EXTENSION.value(), ""));
                        junctionEdit.getConfiguration().setPath(junctionEdit.getPath());
                    }
                    junctionEdit.setObjectType(ConfigurationType.JUNCTION);
                    objects.add(junctionEdit);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.JOBCLASS_FILE_EXTENSION.value())) {
                    JobClassEdit jobClassEdit = new JobClassEdit();
                    JobClass jobClass = om.readValue(outBuffer.toString(), JobClass.class);
                    if (checkObjectNotEmpty(jobClass)) {
                        jobClassEdit.setConfiguration(jobClass);
                    } else {
                        throw new JocImportException(String.format("JobClass with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(), "")));
                    }
                    if (jobClassEdit.getConfiguration().getPath() != null ) {
                        jobClassEdit.setPath(jobClassEdit.getConfiguration().getPath());
                    } else {
                        jobClassEdit.setPath(("/" + entryName).replace(JSObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(), ""));
                        jobClassEdit.getConfiguration().setPath(jobClassEdit.getPath());
                    }
                    jobClassEdit.setObjectType(ConfigurationType.JOBCLASS);
                    objects.add(jobClassEdit);
                } 
            }
            objects.stream().forEach(item -> {
                objectsWithSignature.put(item, signaturePaths.stream()
                        .filter(item2 -> item2.getObjectPath().equals(item.getConfiguration().getPath())).findFirst().get());
            });            
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {}
            }
        }
        return objectsWithSignature;
    }

    public static Set<ConfigurationObject> readZipFileContent(InputStream inputStream)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException, 
            JocConfigurationException, DBOpenSessionException {
        Set<ConfigurationObject> objects = new HashSet<ConfigurationObject>();
        ZipInputStream zipStream = null;
        try {
            zipStream = new ZipInputStream(inputStream);
            ZipEntry entry = null;
            while ((entry = zipStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
                ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
                byte[] binBuffer = new byte[8192];
                int binRead = 0;
                while ((binRead = zipStream.read(binBuffer, 0, 8192)) >= 0) {
                    outBuffer.write(binBuffer, 0, binRead);
                }
                // process deployables and releaseables
                if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
                    WorkflowEdit workflowEdit = new WorkflowEdit();
                    Workflow workflow = om.readValue(outBuffer.toString(), Workflow.class);
                    if (checkObjectNotEmpty(workflow)) {
                        workflowEdit.setConfiguration(workflow);
                    } else {
                        throw new JocImportException(String.format("Workflow with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(), "")));
                    }
                    if (workflowEdit.getConfiguration().getPath() != null) {
                        workflowEdit.setPath(workflowEdit.getConfiguration().getPath());
                    } else {
                        workflowEdit.setPath(("/" + entryName).replace(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(), ""));
                        workflowEdit.getConfiguration().setPath(workflowEdit.getPath());
                    }
                    workflowEdit.setObjectType(ConfigurationType.WORKFLOW);
                    objects.add(workflowEdit);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.LOCK_FILE_EXTENSION.value())) {
                    LockEdit lockEdit = new LockEdit();
                    Lock lock = om.readValue(outBuffer.toString(), Lock.class);
                    if (checkObjectNotEmpty(lock)) {
                        lockEdit.setConfiguration(lock);
                    } else {
                        throw new JocImportException(String.format("Lock with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.LOCK_FILE_EXTENSION.value(), "")));
                    }
                    if (lockEdit.getConfiguration().getPath() != null ) {
                        lockEdit.setPath(lockEdit.getConfiguration().getPath());
                    } else {
                        lockEdit.setPath(("/" + entryName).replace(JSObjectFileExtension.LOCK_FILE_EXTENSION.value(), ""));
                        lockEdit.getConfiguration().setPath(lockEdit.getPath());
                    }
                    lockEdit.setObjectType(ConfigurationType.LOCK);
                    objects.add(lockEdit);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.JUNCTION_FILE_EXTENSION.value())) {
                    JunctionEdit junctionEdit = new JunctionEdit();
                    Junction junction = om.readValue(outBuffer.toString(), Junction.class);
                    if (checkObjectNotEmpty(junction)) {
                        junctionEdit.setConfiguration(junction);
                    } else {
                        throw new JocImportException(String.format("Junction with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.JUNCTION_FILE_EXTENSION.value(), "")));
                    }
                    if (junctionEdit.getConfiguration().getPath() != null ) {
                        junctionEdit.setPath(junctionEdit.getConfiguration().getPath());
                    } else {
                        junctionEdit.setPath(("/" + entryName).replace(JSObjectFileExtension.JUNCTION_FILE_EXTENSION.value(), ""));
                        junctionEdit.getConfiguration().setPath(junctionEdit.getPath());
                    }
                    junctionEdit.setObjectType(ConfigurationType.JUNCTION);
                    objects.add(junctionEdit);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.JOBCLASS_FILE_EXTENSION.value())) {
                    JobClassEdit jobClassEdit = new JobClassEdit();
                    JobClass jobClass = om.readValue(outBuffer.toString(), JobClass.class);
                    if (checkObjectNotEmpty(jobClass)) {
                        jobClassEdit.setConfiguration(jobClass);
                    } else {
                        throw new JocImportException(String.format("JobClass with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(), "")));
                    }
                    if (jobClassEdit.getConfiguration().getPath() != null ) {
                        jobClassEdit.setPath(jobClassEdit.getConfiguration().getPath());
                    } else {
                        jobClassEdit.setPath(("/" + entryName).replace(JSObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(), ""));
                        jobClassEdit.getConfiguration().setPath(jobClassEdit.getPath());
                    }
                    jobClassEdit.setObjectType(ConfigurationType.JOBCLASS);
                    objects.add(jobClassEdit);
                } else if (("/" + entryName).endsWith(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.value())) {
                    ScheduleEdit scheduleEdit = new ScheduleEdit();
                    Schedule schedule = om.readValue(outBuffer.toString(), Schedule.class);
                    if (checkObjectNotEmpty(schedule)) {
                        scheduleEdit.setConfiguration(schedule);
                    } else {
                        throw new JocImportException(String.format("Schedule with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.value(), "")));
                    }
                    if (scheduleEdit.getConfiguration().getPath() != null ) {
                        scheduleEdit.setPath(scheduleEdit.getConfiguration().getPath());
                    } else {
                        scheduleEdit.setPath(("/" + entryName).replace(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.value(), ""));
                        scheduleEdit.getConfiguration().setPath(scheduleEdit.getPath());
                    }
                    scheduleEdit.setObjectType(ConfigurationType.SCHEDULE);
                    objects.add(scheduleEdit);
                } else if (("/" + entryName).endsWith(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value())) {
                    Calendar cal = om.readValue(outBuffer.toString(), Calendar.class);
                    if (checkObjectNotEmpty(cal)) {
                        if (CalendarType.WORKINGDAYSCALENDAR.equals(cal.getType())) {
                            WorkingDaysCalendarEdit wdcEdit = new WorkingDaysCalendarEdit();
                            wdcEdit.setConfiguration(cal);
                            if (wdcEdit.getConfiguration().getPath() != null ) {
                                wdcEdit.setPath(wdcEdit.getConfiguration().getPath());
                            } else {
                                wdcEdit.setPath(("/" + entryName).replace(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value(), ""));
                                wdcEdit.getConfiguration().setPath(wdcEdit.getPath());
                            }
                            wdcEdit.setObjectType(ConfigurationType.WORKINGDAYSCALENDAR);
                            objects.add(wdcEdit);
                        } else if (CalendarType.WORKINGDAYSCALENDAR.equals(cal.getType())) {
                            NonWorkingDaysCalendarEdit nwdcEdit = new NonWorkingDaysCalendarEdit();
                            nwdcEdit.setConfiguration(cal);
                            if (nwdcEdit.getConfiguration().getPath() != null ) {
                                nwdcEdit.setPath(nwdcEdit.getConfiguration().getPath());
                            } else {
                                nwdcEdit.setPath(("/" + entryName).replace(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value(), ""));
                                nwdcEdit.getConfiguration().setPath(nwdcEdit.getPath());
                            }
                            nwdcEdit.setObjectType(ConfigurationType.NONWORKINGDAYSCALENDAR);
                            objects.add(nwdcEdit);
                        }
                    } else {
                        throw new JocImportException(String.format("Calendar with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value(), "")));
                    }
                }
            }
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {}
            }
        }
        return objects;
    }

    public static Map<ConfigurationObject, SignaturePath> readTarGzipFileContentWithSignatures(InputStream inputStream) 
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException, 
            JocConfigurationException, DBOpenSessionException {
        Set<ConfigurationObject> objects = new HashSet<ConfigurationObject>();
        Set<SignaturePath> signaturePaths = new HashSet<SignaturePath>();
        Map<ConfigurationObject, SignaturePath> objectsWithSignature = new HashMap<ConfigurationObject, SignaturePath>();
        GZIPInputStream gzipInputStream = null;
        TarArchiveInputStream tarArchiveInputStream = null;
        try {
            gzipInputStream = new GZIPInputStream(inputStream);
            tarArchiveInputStream = new TarArchiveInputStream(gzipInputStream);
            ArchiveEntry entry = null;
            while ((entry = tarArchiveInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
                ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
                byte[] binBuffer = new byte[8192];
                int binRead = 0;
                while ((binRead = tarArchiveInputStream.read(binBuffer, 0, 8192)) >= 0) {
                    outBuffer.write(binBuffer, 0, binRead);
                }
                // process deployables only
                SignaturePath signaturePath = new SignaturePath();
                Signature signature = new Signature();
                if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
                    WorkflowEdit workflowEdit = new WorkflowEdit();
                    Workflow workflow = om.readValue(outBuffer.toString(), Workflow.class);
                    if (checkObjectNotEmpty(workflow)) {
                        workflowEdit.setConfiguration(workflow);
                    } else {
                        throw new JocImportException(String.format("Workflow with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(), "")));
                    }
                    workflowEdit.setPath(workflowEdit.getConfiguration().getPath());
                    workflowEdit.setObjectType(ConfigurationType.WORKFLOW);
                    objects.add(workflowEdit);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_PGP_SIGNATURE_FILE_EXTENSION.value())) {
                    signaturePath.setObjectPath(("/" + entryName).replace(JSObjectFileExtension.WORKFLOW_PGP_SIGNATURE_FILE_EXTENSION.value(), ""));
                    signature.setSignatureString(outBuffer.toString());
                    signaturePath.setSignature(signature);
                    signaturePaths.add(signaturePath);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_X509_SIGNATURE_FILE_EXTENSION.value())) {
                    signaturePath.setObjectPath(("/" + entryName).replace(JSObjectFileExtension.WORKFLOW_X509_SIGNATURE_FILE_EXTENSION.value(), ""));
                    signature.setSignatureString(outBuffer.toString());
                    signaturePath.setSignature(signature);
                    signaturePaths.add(signaturePath);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.LOCK_FILE_EXTENSION.value())) {
                    LockEdit lockEdit = new LockEdit();
                    Lock lock = om.readValue(outBuffer.toString(), Lock.class);
                    if (checkObjectNotEmpty(lock)) {
                        lockEdit.setConfiguration(lock);
                    } else {
                        throw new JocImportException(String.format("Lock with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.LOCK_FILE_EXTENSION.value(), "")));
                    }
                    lockEdit.setPath(lockEdit.getConfiguration().getPath());
                    lockEdit.setObjectType(ConfigurationType.LOCK);
                    objects.add(lockEdit);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.JUNCTION_FILE_EXTENSION.value())) {
                    JunctionEdit junctionEdit = new JunctionEdit();
                    Junction junction = om.readValue(outBuffer.toString(), Junction.class);
                    if (checkObjectNotEmpty(junction)) {
                        junctionEdit.setConfiguration(junction);
                    } else {
                        throw new JocImportException(String.format("Junction with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.JUNCTION_FILE_EXTENSION.value(), "")));
                    }
                    junctionEdit.setPath(junctionEdit.getConfiguration().getPath());
                    junctionEdit.setObjectType(ConfigurationType.JUNCTION);
                    objects.add(junctionEdit);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.JOBCLASS_FILE_EXTENSION.value())) {
                    JobClassEdit jobClassEdit = new JobClassEdit();
                    JobClass jobClass = om.readValue(outBuffer.toString(), JobClass.class);
                    if (checkObjectNotEmpty(jobClass)) {
                        jobClassEdit.setConfiguration(jobClass);
                    } else {
                        throw new JocImportException(String.format("JobClass with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(), "")));
                    }
                    jobClassEdit.setPath(jobClassEdit.getConfiguration().getPath());
                    jobClassEdit.setObjectType(ConfigurationType.JOBCLASS);
                    objects.add(jobClassEdit);
                }
            }
            objects.stream().forEach(item -> {
                objectsWithSignature.put(item, signaturePaths.stream()
                        .filter(item2 -> item2.getObjectPath().equals(item.getConfiguration().getPath())).findFirst().get());
            });            
        } finally {
            try {
                if (tarArchiveInputStream != null) {
                    tarArchiveInputStream.close();
                }
                if (gzipInputStream != null) {
                    gzipInputStream.close();
                }
            } catch (Exception e) {}
        }
        return objectsWithSignature;
    }

    public static Set<ConfigurationObject> readTarGzipFileContent(InputStream inputStream) 
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException, 
            JocConfigurationException, DBOpenSessionException {
        Set<ConfigurationObject> objects = new HashSet<ConfigurationObject>();
        GZIPInputStream gzipInputStream = null;
        TarArchiveInputStream tarArchiveInputStream = null;
        try {
            gzipInputStream = new GZIPInputStream(inputStream);
            tarArchiveInputStream = new TarArchiveInputStream(gzipInputStream);
            ArchiveEntry entry = null;
            while ((entry = tarArchiveInputStream.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }
                String entryName = entry.getName().replace('\\', '/');
                ByteArrayOutputStream outBuffer = new ByteArrayOutputStream();
                byte[] binBuffer = new byte[8192];
                int binRead = 0;
                while ((binRead = tarArchiveInputStream.read(binBuffer, 0, 8192)) >= 0) {
                    outBuffer.write(binBuffer, 0, binRead);
                }
                // process deployables and releaseables
                if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
                    WorkflowEdit workflowEdit = new WorkflowEdit();
                    Workflow workflow = om.readValue(outBuffer.toString(), Workflow.class);
                    if (checkObjectNotEmpty(workflow)) {
                        workflowEdit.setConfiguration(workflow);
                    } else {
                        throw new JocImportException(String.format("Workflow with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(), "")));
                    }
                    workflowEdit.setPath(workflowEdit.getConfiguration().getPath());
                    workflowEdit.setObjectType(ConfigurationType.WORKFLOW);
                    objects.add(workflowEdit);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.LOCK_FILE_EXTENSION.value())) {
                    LockEdit lockEdit = new LockEdit();
                    Lock lock = om.readValue(outBuffer.toString(), Lock.class);
                    if (checkObjectNotEmpty(lock)) {
                        lockEdit.setConfiguration(lock);
                    } else {
                        throw new JocImportException(String.format("Lock with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.LOCK_FILE_EXTENSION.value(), "")));
                    }
                    lockEdit.setPath(lockEdit.getConfiguration().getPath());
                    lockEdit.setObjectType(ConfigurationType.LOCK);
                    objects.add(lockEdit);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.JUNCTION_FILE_EXTENSION.value())) {
                    JunctionEdit junctionEdit = new JunctionEdit();
                    Junction junction = om.readValue(outBuffer.toString(), Junction.class);
                    if (checkObjectNotEmpty(junction)) {
                        junctionEdit.setConfiguration(junction);
                    } else {
                        throw new JocImportException(String.format("Junction with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.JUNCTION_FILE_EXTENSION.value(), "")));
                    }
                    junctionEdit.setPath(junctionEdit.getConfiguration().getPath());
                    junctionEdit.setObjectType(ConfigurationType.JUNCTION);
                    objects.add(junctionEdit);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.JOBCLASS_FILE_EXTENSION.value())) {
                    JobClassEdit jobClassEdit = new JobClassEdit();
                    JobClass jobClass = om.readValue(outBuffer.toString(), JobClass.class);
                    if (checkObjectNotEmpty(jobClass)) {
                        jobClassEdit.setConfiguration(jobClass);
                    } else {
                        throw new JocImportException(String.format("JobClass with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(JSObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(), "")));
                    }
                    jobClassEdit.setPath(jobClassEdit.getConfiguration().getPath());
                    jobClassEdit.setObjectType(ConfigurationType.JOBCLASS);
                    objects.add(jobClassEdit);
                } else if (("/" + entryName).endsWith(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.value())) {
                    ScheduleEdit scheduleEdit = new ScheduleEdit();
                    Schedule schedule = om.readValue(outBuffer.toString(), Schedule.class);
                    if (checkObjectNotEmpty(schedule)) {
                        scheduleEdit.setConfiguration(schedule);
                    } else {
                        throw new JocImportException(String.format("Schedule with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.value(), "")));
                    }
                    scheduleEdit.setPath(scheduleEdit.getConfiguration().getPath());
                    scheduleEdit.setObjectType(ConfigurationType.SCHEDULE);
                    objects.add(scheduleEdit);
                } else if (("/" + entryName).endsWith(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value())) {
                    Calendar cal = om.readValue(outBuffer.toString(), Calendar.class);
                    if (checkObjectNotEmpty(cal)) {
                        if (CalendarType.WORKINGDAYSCALENDAR.equals(cal.getType())) {
                            WorkingDaysCalendarEdit wdcEdit = new WorkingDaysCalendarEdit();
                            wdcEdit.setConfiguration(cal);
                            if (wdcEdit.getConfiguration().getPath() != null ) {
                                wdcEdit.setPath(wdcEdit.getConfiguration().getPath());
                            } else {
                                wdcEdit.setPath(("/" + entryName).replace(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value(), ""));
                                wdcEdit.getConfiguration().setPath(wdcEdit.getPath());
                            }
                            wdcEdit.setObjectType(ConfigurationType.WORKINGDAYSCALENDAR);
                            objects.add(wdcEdit);
                        } else if (CalendarType.WORKINGDAYSCALENDAR.equals(cal.getType())) {
                            NonWorkingDaysCalendarEdit nwdcEdit = new NonWorkingDaysCalendarEdit();
                            nwdcEdit.setConfiguration(cal);
                            if (nwdcEdit.getConfiguration().getPath() != null ) {
                                nwdcEdit.setPath(nwdcEdit.getConfiguration().getPath());
                            } else {
                                nwdcEdit.setPath(("/" + entryName).replace(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value(), ""));
                                nwdcEdit.getConfiguration().setPath(nwdcEdit.getPath());
                            }
                            nwdcEdit.setObjectType(ConfigurationType.NONWORKINGDAYSCALENDAR);
                            objects.add(nwdcEdit);
                        }
                    } else {
                        throw new JocImportException(String.format("Calendar with path %1$s not imported. Object values could not be mapped.", 
                                ("/" + entryName).replace(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value(), "")));
                    }
                }
            }
        } finally {
            try {
                if (tarArchiveInputStream != null) {
                    tarArchiveInputStream.close();
                }
                if (gzipInputStream != null) {
                    gzipInputStream.close();
                }
            } catch (Exception e) {}
        }
        return objects;
    }

    public static StreamingOutput writeZipFile (Set<JSObject> deployables, Set<ConfigurationObject> releasables, 
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames,String commitId, String controllerId, DBLayerDeploy dbLayer) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException {
                ZipOutputStream zipOut = null;
                try {
                    zipOut = new ZipOutputStream(new BufferedOutputStream(output), StandardCharsets.UTF_8);
                    String content = null;
                    if (deployables != null && !deployables.isEmpty()) {
                        for (JSObject deployable : deployables) {
                            String extension = null;
                            switch (deployable.getObjectType()) {
                            case WORKFLOW:
                                extension = JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                                Workflow workflow = (Workflow) deployable.getContent();
                                // determine agent names to be replaced
                                workflow.setVersionId(commitId);
                                if (controllerId != null && updateableAgentNames != null) {
                                    replaceAgentNameWithAgentId(workflow, updateableAgentNames, controllerId);
                                }
                                content = om.writeValueAsString(workflow);
                                break;
                            case LOCK:
                                extension = JSObjectFileExtension.LOCK_FILE_EXTENSION.toString();
                                Lock lock = (Lock) deployable.getContent();
                                lock.setVersionId(commitId);
                                content = om.writeValueAsString(lock);
                                break;
                            case JUNCTION:
                                extension = JSObjectFileExtension.JUNCTION_FILE_EXTENSION.toString();
                                Junction junction = (Junction) deployable.getContent();
                                junction.setVersionId(commitId);
                                content = om.writeValueAsString(junction);
                                break;
                            case JOBCLASS:
                                extension = JSObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString();
                                JobClass jobClass = (JobClass) deployable.getContent();
                                jobClass.setVersionId(commitId);
                                content = om.writeValueAsString(jobClass);
                                break;
                            }
                            String zipEntryName = deployable.getPath().substring(1).concat(extension);
                            ZipEntry entry = new ZipEntry(zipEntryName);
                            zipOut.putNextEntry(entry);
                            zipOut.write(content.getBytes());
                            zipOut.closeEntry();
                        } 
                    }
                    if (releasables != null && !releasables.isEmpty()) {
                        for (ConfigurationObject releasable : releasables) {
                            // process releasable objects
                            String extension = null;
                            switch (releasable.getObjectType()) {
                            case SCHEDULE:
                                extension = ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.toString();
                                break;
                            case WORKINGDAYSCALENDAR:
                            case NONWORKINGDAYSCALENDAR:
                                extension = ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString();
                                break;
                            default:
                                break;
                            }
                            if (extension != null) {
                                content = om.writeValueAsString(releasable.getConfiguration());
                                String zipEntryName = releasable.getPath().substring(1).concat(extension);
                                ZipEntry entry = new ZipEntry(zipEntryName);
                                zipOut.putNextEntry(entry);
                                zipOut.write(content.getBytes());
                                zipOut.closeEntry();
                            }
                        } 
                    }
                    JocMetaInfo jocMetaInfo = getJocMetaInfo();
                    if (!isJocMetaInfoNullOrEmpty(jocMetaInfo)) {
                        String zipEntryName = "meta_inf";
                        ZipEntry entry = new ZipEntry(zipEntryName);
                        zipOut.putNextEntry(entry);
                        zipOut.write(om.writeValueAsBytes(jocMetaInfo));
                        zipOut.closeEntry();
                    }
                    zipOut.flush();
                } finally {
                    if (zipOut != null) {
                        try {
                            zipOut.close();
                        } catch (Exception e) {}
                    }
                }
            }
        };
        return streamingOutput;
    }
    
    public static StreamingOutput writeTarGzipFile (Set<JSObject> deployables, Set<ConfigurationObject> releasables,
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames, String commitId,  String controllerId, DBLayerDeploy dbLayer) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException {
                GZIPOutputStream gzipOut = null;
                TarArchiveOutputStream tarOut = null;
                BufferedOutputStream bOut = null;
                try {
                    bOut = new BufferedOutputStream(output);
                    gzipOut = new GZIPOutputStream(bOut);
                    tarOut = new TarArchiveOutputStream(gzipOut);
                    String content = null;
                    if (deployables != null && !deployables.isEmpty()) {
                        for (JSObject deployable : deployables) {
                            String extension = null;
                            switch (deployable.getObjectType()) {
                            case WORKFLOW:
                                extension = JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                                Workflow workflow = (Workflow) deployable.getContent();
                                workflow.setVersionId(commitId);
                                if (controllerId != null && updateableAgentNames != null) {
                                    replaceAgentNameWithAgentId(workflow, updateableAgentNames, controllerId);
                                }
                                content = om.writeValueAsString(workflow);
                                break;
                            case LOCK:
                                extension = JSObjectFileExtension.LOCK_FILE_EXTENSION.toString();
                                Lock lock = (Lock) deployable.getContent();
                                lock.setVersionId(commitId);
                                content = om.writeValueAsString(lock);
                                break;
                            case JUNCTION:
                                extension = JSObjectFileExtension.JUNCTION_FILE_EXTENSION.toString();
                                Junction junction = (Junction) deployable.getContent();
                                junction.setVersionId(commitId);
                                content = om.writeValueAsString(junction);
                                break;
                            case JOBCLASS:
                                extension = JSObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString();
                                JobClass jobClass = (JobClass) deployable.getContent();
                                jobClass.setVersionId(commitId);
                                content = om.writeValueAsString(jobClass);
                                break;
                            }
                            String zipEntryName = deployable.getPath().substring(1).concat(extension);
                            TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
                            byte[] contentBytes = content.getBytes();
                            entry.setSize(contentBytes.length);
                            tarOut.putArchiveEntry(entry);
                            tarOut.write(contentBytes);
                            tarOut.closeArchiveEntry();
                        } 
                    }
                    if (releasables != null && !releasables.isEmpty()) {
                        for (ConfigurationObject releasable : releasables) {
                            // process releasable objects
                            String extension = null;
                            switch (releasable.getObjectType()) {
                            case SCHEDULE:
                                extension = ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.toString();
                                break;
                            case WORKINGDAYSCALENDAR:
                            case NONWORKINGDAYSCALENDAR:
                                extension = ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.toString();
                                break;
                            default:
                                break;
                            }
                            if (extension != null) {
                                content = om.writeValueAsString(releasable.getConfiguration());
                                String zipEntryName = releasable.getPath().substring(1).concat(extension);
                                TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
                                byte[] contentBytes = content.getBytes();
                                entry.setSize(contentBytes.length);
                                tarOut.putArchiveEntry(entry);
                                tarOut.write(contentBytes);
                                tarOut.closeArchiveEntry();
                            }
                        } 
                    }
                    JocMetaInfo jocMetaInfo = getJocMetaInfo();
                    if (!isJocMetaInfoNullOrEmpty(jocMetaInfo)) {
                        String zipEntryName = "meta_inf";
                        TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
                        byte[] jocMetaInfoBytes = om.writeValueAsBytes(jocMetaInfo);
                        entry.setSize(jocMetaInfoBytes.length);
                        tarOut.putArchiveEntry(entry);
                        tarOut.write(jocMetaInfoBytes);
                        tarOut.closeArchiveEntry();
                    }
                    tarOut.flush();
                } finally {
                    if (tarOut != null) {
                        try {
                            tarOut.finish();
                            tarOut.close();
                        } catch (Exception e) {}
                    }
                    if (gzipOut != null) {
                        try {
                            gzipOut.flush();
                            gzipOut.close();
                        } catch (Exception e) {}
                    }
                    if (bOut != null) {
                        try {
                            bOut.flush();
                            bOut.close();
                        } catch (Exception e) {}
                    }

                }
                
            }
        };
        return streamingOutput;
    }
    
    public static boolean verifyDeployable(SOSHibernateSession hibernateSession, SignaturePath signaturePath, ConfigurationObject deployable,
            String account) throws JocSignatureVerificationException, SOSHibernateException {
        DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
        boolean verified = false;
        try {
            if (signaturePath != null && signaturePath.getSignature() != null) {
                JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.HIGH);
                String publicKey = keyPair.getPublicKey();
                if (keyPair.getCertificate() != null && !keyPair.getCertificate().isEmpty()) {
                    Certificate certificate = KeyUtil.getCertificate(keyPair.getCertificate());
                    verified = VerifySignature.verifyX509(certificate, 
                            om.writeValueAsString(deployable), signaturePath.getSignature().getSignatureString());                    
                } else if (publicKey != null && !publicKey.isEmpty()) {
                    if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                        verified = VerifySignature.verifyPGP(publicKey, 
                                om.writeValueAsString(deployable), signaturePath.getSignature().getSignatureString());
                    } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                        PublicKey pubKey = KeyUtil.getPublicKeyFromString(KeyUtil.decodePublicKeyString(publicKey)); 
                        verified = VerifySignature.verifyX509(pubKey, 
                                om.writeValueAsString(deployable), signaturePath.getSignature().getSignatureString());
                    } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                        PublicKey pubKey = KeyUtil.getECDSAPublicKeyFromString(publicKey); 
                        verified = VerifySignature.verifyX509(pubKey, 
                                om.writeValueAsString(deployable), signaturePath.getSignature().getSignatureString());
                    }
                }
                if (!verified) {
                    LOGGER.debug(String.format("signature verification for deployable %1$s was not successful!", deployable.getPath()));
                    return verified;
                } 
            }
        } catch (IOException | PGPException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException 
                | SignatureException | CertificateException | NoSuchProviderException  e) {
            throw new JocSignatureVerificationException(e);
        }
        return verified;
    }

    public static Signature verifyWorkflows(SOSHibernateSession hibernateSession, Set<SignaturePath> signaturePaths, Workflow workflow,
            String account) throws JocSignatureVerificationException, SOSHibernateException {
        SignaturePath signaturePath = signaturePaths.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath()
                .equals(workflow.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
        DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
        Boolean verified = null;
        try {
            if (signaturePath != null && signaturePath.getSignature() != null) {
                JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.HIGH);
                String publicKey = keyPair.getPublicKey();
                if (keyPair.getCertificate() != null && !keyPair.getCertificate().isEmpty()) {
                    Certificate certificate = KeyUtil.getCertificate(keyPair.getCertificate());
                    verified = VerifySignature.verifyX509(certificate, 
                            om.writeValueAsString(workflow), signaturePath.getSignature().getSignatureString());                    
                } else if (publicKey != null && !publicKey.isEmpty()) {
                    if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                        verified = VerifySignature.verifyPGP(publicKey, 
                                om.writeValueAsString(workflow), signaturePath.getSignature().getSignatureString());
                    } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                        PublicKey pubKey = KeyUtil.getPublicKeyFromString(KeyUtil.decodePublicKeyString(publicKey)); 
                        verified = VerifySignature.verifyX509(pubKey, 
                                om.writeValueAsString(workflow), signaturePath.getSignature().getSignatureString());
                    } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                        PublicKey pubKey = KeyUtil.getECDSAPublicKeyFromString(publicKey); 
                        verified = VerifySignature.verifyX509(pubKey, 
                                om.writeValueAsString(workflow), signaturePath.getSignature().getSignatureString());
                    }
                }
                if (!verified) {
                    LOGGER.debug(String.format("signature verification for workflow %1$s was not successful!", workflow.getPath()));
                    return null;
                } 
            }
        } catch (IOException | PGPException | NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException 
                | SignatureException | CertificateException | NoSuchProviderException  e) {
            throw new JocSignatureVerificationException(e);
        }
        return signaturePath.getSignature();
    }

    public static boolean jocKeyPairNotEmpty (JocKeyPair keyPair) {
        boolean checkNotEmpty = false;
        if(keyPair != null) {
            if(keyPair.getPrivateKey() != null && !keyPair.getPrivateKey().isEmpty()) {
                checkNotEmpty = true;
            } else if (keyPair.getPrivateKey() != null && keyPair.getPrivateKey().isEmpty()) {
                checkNotEmpty = false;
            } else if (keyPair.getPrivateKey() == null) {
                checkNotEmpty = false;
            } 
            if (checkNotEmpty) {
                return checkNotEmpty;
            } else {
                if (keyPair.getPublicKey() != null) {
                    checkNotEmpty = true;
                } else if (keyPair.getPublicKey() == null && keyPair.getCertificate() != null) {
                    checkNotEmpty = true;
                } else if (keyPair.getPublicKey() == null && keyPair.getCertificate() == null) {
                    checkNotEmpty = false;
                } else if ((keyPair.getPublicKey() != null && keyPair.getPublicKey().isEmpty()) 
                        && (keyPair.getCertificate() != null && keyPair.getCertificate().isEmpty())) {
                    checkNotEmpty = false;
                }
                return checkNotEmpty;
            }
        } else {
            checkNotEmpty = false;
        }
        return checkNotEmpty;
    }
    
    public static Set<Path> updateSetOfPathsWithParents(Set<Path> paths) {
        final Set<Path> pathWithParents = new HashSet<Path>();
        pathWithParents.addAll(paths.stream().flatMap(path -> getPathWithParents(path).stream()).collect(Collectors.toSet()));
        return pathWithParents;
    }
    
    private static Set<Path> getPathWithParents (Path path) {
        Set<Path> pathsWithParents = new HashSet<Path>();
        pathsWithParents.add(path);
        Iterator<Path> pathsIter = path.iterator();
        String folder = null;
        while (pathsIter.hasNext()) {
            if (folder == null) {
                folder = "/" + pathsIter.next().toString();
            } else {
                folder += "/" + pathsIter.next().toString();
            }
            pathsWithParents.add(Paths.get(folder));
        }
        return pathsWithParents; 
    }
    
    private static void replaceAgentNameWithAgentId(DBItemInventoryConfiguration draft, Set<UpdateableWorkflowJobAgentName> updateableAgentNames,
            String controllerId) throws JsonParseException, JsonMappingException, IOException {
        Workflow workflow = om.readValue(draft.getContent(), Workflow.class);
        Set<UpdateableWorkflowJobAgentName> filteredUpdateables = updateableAgentNames.stream()
                .filter(item -> item.getWorkflowPath().equals(draft.getPath())).collect(Collectors.toSet());
        workflow.getJobs().getAdditionalProperties().keySet().stream().forEach(jobname -> {
            Job job = workflow.getJobs().getAdditionalProperties().get(jobname);
            job.setAgentId(filteredUpdateables.stream()
                    .filter(item -> item.getJobName().equals(jobname) && controllerId.equals(item.getControllerId()))
                    .findFirst().get().getAgentId());
        });
        draft.setContent(om.writeValueAsString(workflow));
    }

    private static void replaceAgentNameWithAgentId(Workflow workflow, Set<UpdateableWorkflowJobAgentName> updateableAgentNames,
            String controllerId) throws JsonParseException, JsonMappingException, IOException {
        Set<UpdateableWorkflowJobAgentName> filteredUpdateables = updateableAgentNames.stream()
                .filter(item -> item.getWorkflowPath().equals(workflow.getPath())).collect(Collectors.toSet());
        workflow.getJobs().getAdditionalProperties().keySet().stream().forEach(jobname -> {
            Job job = workflow.getJobs().getAdditionalProperties().get(jobname);
            job.setAgentId(filteredUpdateables.stream()
                    .filter(item -> item.getJobName().equals(jobname) && controllerId.equals(item.getControllerId()))
                    .findFirst().get().getAgentId());
        });
    }

    private static void replaceAgentIdWithOrigAgentName(DBItemInventoryConfiguration draft, 
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames, String controllerId)
            throws JsonParseException, JsonMappingException, IOException {
        draft.setContent(getContentWithOrigAgentName(draft, updateableAgentNames, controllerId));
    }
    
    private static String getContentWithOrigAgentName(DBItemInventoryConfiguration draft, 
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames, String controllerId)
            throws JsonParseException, JsonMappingException, IOException {
        Workflow workflow = om.readValue(draft.getContent(), Workflow.class);
        Set<UpdateableWorkflowJobAgentName> filteredUpdateables = updateableAgentNames.stream()
                .filter(item -> item.getWorkflowPath().equals(draft.getPath()) && controllerId.equals(item.getControllerId()))
                .collect(Collectors.toSet());
        workflow.getJobs().getAdditionalProperties().keySet().stream().forEach(jobname -> {
            Job job = workflow.getJobs().getAdditionalProperties().get(jobname);
            job.setAgentId(filteredUpdateables.stream().filter(item -> item.getJobName().equals(jobname)).findFirst().get().getAgentName());
        });
        return om.writeValueAsString(workflow);
    }
    
    public static String getValueAsStringWithleadingZeros(Integer i, int length) {
        if (i.toString().length() >= length) {
            return i.toString();
        } else {
            return String.format("%0" + (length-i.toString().length()) + "d%s", 0, i.toString());
        }
   }

    private static DBItemInventoryConfiguration cloneDraftToUpdate(DBItemInventoryConfiguration unsignedDraft) {
        DBItemInventoryConfiguration unsignedDraftUpdated = new DBItemInventoryConfiguration();
        unsignedDraftUpdated.setAuditLogId(unsignedDraft.getAuditLogId());
        unsignedDraftUpdated.setContent(unsignedDraft.getContent());
        unsignedDraftUpdated.setCreated(unsignedDraft.getCreated());
        unsignedDraftUpdated.setDeleted(unsignedDraft.getDeleted());
        unsignedDraftUpdated.setDeployed(unsignedDraft.getDeployed());
        unsignedDraftUpdated.setDocumentationId(unsignedDraft.getDocumentationId());
        unsignedDraftUpdated.setFolder(unsignedDraft.getFolder());
        unsignedDraftUpdated.setId(unsignedDraft.getId());
        unsignedDraftUpdated.setModified(unsignedDraft.getModified());
        unsignedDraftUpdated.setName(unsignedDraft.getName());
        unsignedDraftUpdated.setPath(unsignedDraft.getPath());
        unsignedDraftUpdated.setReleased(unsignedDraft.getReleased());
        unsignedDraftUpdated.setTitle(unsignedDraft.getTitle());
        unsignedDraftUpdated.setType(unsignedDraft.getType());
        unsignedDraftUpdated.setValid(unsignedDraft.getValid());
        return unsignedDraftUpdated;
    }

    public static Set<DBItemDeploymentHistory> getLatestDepHistoryEntriesActiveForFolder(Config folder, DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        entries.addAll(dbLayer.getLatestDepHistoryItemsFromFolder(
                folder.getConfiguration().getPath()));
        return entries.stream()
                .filter(item -> item.getOperation().equals(OperationType.UPDATE.value())).collect(Collectors.toSet());
    }
    
    public static Set<DBItemDeploymentHistory> getLatestDepHistoryEntriesActiveForFolder(Config folder, String controllerId,
            DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        entries.addAll(dbLayer.getLatestDepHistoryItemsFromFolder(
                folder.getConfiguration().getPath(), controllerId));
        return entries.stream()
                .filter(item -> item.getOperation().equals(OperationType.UPDATE.value())).collect(Collectors.toSet());
    }
    
    public static Set<DBItemDeploymentHistory> getLatestDepHistoryEntriesActiveForFolders(List<Config> foldersToDelete, DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        foldersToDelete.stream()
            .map(item -> item.getConfiguration().getPath())
            .forEach(item -> entries.addAll(dbLayer.getLatestDepHistoryItemsFromFolder(item)));
        return entries.stream()
                .filter(item -> item.getOperation().equals(OperationType.UPDATE.value())).collect(Collectors.toSet());
    }
    
    public static Set<DBItemDeploymentHistory> getLatestDepHistoryEntriesActiveForFolders(List<Config> foldersToDelete, String controllerId,
            DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        foldersToDelete.stream()
            .map(item -> item.getConfiguration().getPath())
            .forEach(item -> entries.addAll(dbLayer.getLatestDepHistoryItemsFromFolder(item, controllerId)));
        return entries.stream()
                .filter(item -> item.getOperation().equals(OperationType.UPDATE.value())).collect(Collectors.toSet());
    }
 
    public static Set<DBItemInventoryConfiguration> getDeployableInventoryConfigurationsfromFolders(List<Configuration> folders, DBLayerDeploy dbLayer) {
        List<DBItemInventoryConfiguration> entries = new ArrayList<DBItemInventoryConfiguration>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getDeployableInventoryConfigurationsByFolder(item.getPath(), item.getRecursive())));
        return entries.stream().collect(Collectors.toSet());
    }
    
    public static Set<DBItemInventoryConfiguration> getValidDeployableInventoryConfigurationsfromFolders(List<Configuration> folders, DBLayerDeploy dbLayer) {
        List<DBItemInventoryConfiguration> entries = new ArrayList<DBItemInventoryConfiguration>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getValidDeployableInventoryConfigurationsByFolder(item.getPath(), item.getRecursive())));
        return entries.stream().collect(Collectors.toSet());
    }
    
    public static Set<DBItemInventoryConfiguration> getReleasableInventoryConfigurationsfromFolders(List<Configuration> folders, DBLayerDeploy dbLayer) {
        List<DBItemInventoryConfiguration> entries = new ArrayList<DBItemInventoryConfiguration>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getReleasableInventoryConfigurationsByFolder(item.getPath(), item.getRecursive())));
        return entries.stream().collect(Collectors.toSet());
    }
    
    public static Set<DBItemInventoryConfiguration> getValidReleasableInventoryConfigurationsfromFolders(List<Configuration> folders, DBLayerDeploy dbLayer) {
        List<DBItemInventoryConfiguration> entries = new ArrayList<DBItemInventoryConfiguration>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getValidReleasableInventoryConfigurationsByFolder(item.getPath(), item.getRecursive())));
        return entries.stream().collect(Collectors.toSet());
    }
    
    public static Set<DBItemInventoryReleasedConfiguration> getReleasedInventoryConfigurationsfromFolders(List<Configuration> folders, 
            DBLayerDeploy dbLayer) {
        List<DBItemInventoryReleasedConfiguration> entries = new ArrayList<DBItemInventoryReleasedConfiguration>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getReleasedInventoryConfigurationsByFolder(item.getPath(), item.getRecursive())));
        return entries.stream().collect(Collectors.toSet());
    }
    
    public static Set<DBItemDeploymentHistory> getLatestActiveDepHistoryEntriesFromFolders(List<Configuration> folders, DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getLatestDepHistoryItemsFromFolder(item.getPath(), item.getRecursive())));
        return entries.stream()
                .filter(item -> item.getOperation().equals(OperationType.UPDATE.value())).collect(Collectors.toSet());
    }
    
    public static Set<DBItemDeploymentHistory> getLatestDepHistoryEntriesDeleteForFolder(Config folder, String controllerId,
            DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        entries.addAll(dbLayer.getLatestDepHistoryItemsFromFolder(folder.getConfiguration().getPath(), controllerId));
        return entries.stream().filter(item -> item.getOperation().equals(OperationType.DELETE.value())).collect(Collectors.toSet());
    }
    
    public static Set<DBItemDeploymentHistory> getLatestDepHistoryEntriesDeleteForFolders(List<Config> foldersToDelete, String controllerId,
            DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        foldersToDelete.stream()
            .map(item -> item.getConfiguration().getPath())
            .forEach(item -> entries.addAll(dbLayer.getLatestDepHistoryItemsFromFolder(item, controllerId)));
        return entries.stream().filter(item -> item.getOperation().equals(OperationType.DELETE.value())).collect(Collectors.toSet());
    }
    
    public static Set<JSObject> getDeployableObjectsFromDB(DeployablesFilter filter, DBLayerDeploy dbLayer) 
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, 
            IOException, SOSHibernateException {
        return getDeployableObjectsFromDB(filter, dbLayer, null);
    }

    public static Set<JSObject> getDeployableObjectsFromDB(DeployablesFilter filter, DBLayerDeploy dbLayer, String commitId) 
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, 
            IOException, SOSHibernateException {
        Set<JSObject> allObjects = new HashSet<JSObject>();
        if (filter != null) {
            if (filter.getDeployConfigurations() != null && !filter.getDeployConfigurations().isEmpty()) {
                List<Configuration> depFolders = filter.getDeployConfigurations().stream()
                        .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                        .map(item -> item.getConfiguration())
                        .collect(Collectors.toList());
                Set<DBItemDeploymentHistory> allItems = new HashSet<DBItemDeploymentHistory>();
                if (depFolders != null && !depFolders.isEmpty()) {
                    allItems.addAll(getLatestActiveDepHistoryEntriesFromFolders(depFolders, dbLayer));
                }
                List<DBItemDeploymentHistory> deploymentDbItems = dbLayer.getFilteredDeployments(filter);
                if (deploymentDbItems != null && !deploymentDbItems.isEmpty()) {
                    allItems.addAll(deploymentDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream()
                        .filter(Objects::nonNull)
                        .filter(item -> !item.getType().equals(ConfigurationType.FOLDER.intValue()))
                        .forEach(item -> {
                            if (commitId != null) {
                                dbLayer.storeCommitIdForLaterUsage(item, commitId);
                            }
                            allObjects.add(getJSObjectFromDBItem(item, commitId));
                        });
                }
            }
            if (filter.getDraftConfigurations() != null && !filter.getDraftConfigurations().isEmpty()) {
                List<Configuration> draftFolders = filter.getDraftConfigurations().stream()
                        .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                        .map(item -> item.getConfiguration())
                        .collect(Collectors.toList());
                Set<DBItemInventoryConfiguration> allItems = new HashSet<DBItemInventoryConfiguration>();
                if (draftFolders != null && !draftFolders.isEmpty()) {
                    allItems.addAll(getDeployableInventoryConfigurationsfromFolders(draftFolders, dbLayer));
                }
                List<DBItemInventoryConfiguration> configurationDbItems = dbLayer.getFilteredDeployableConfigurations(filter);
                if (configurationDbItems != null && !configurationDbItems.isEmpty()) {
                    allItems.addAll(configurationDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream()
                        .filter(Objects::nonNull)
                        .filter(item -> !item.getTypeAsEnum().equals(ConfigurationType.FOLDER))
                        .forEach(item -> {
                            if (commitId != null) {
                                dbLayer.storeCommitIdForLaterUsage(item, commitId);
                            }
                            allObjects.add(mapInvConfigToJSObject(item));
                        });
                }
            } 
        }
        return allObjects;
    }
    
    public static Set<JSObject> getDeployableObjectsFromDB(DeployablesValidFilter filter, DBLayerDeploy dbLayer, String commitId) 
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, 
            IOException, SOSHibernateException {
        Set<JSObject> allObjects = new HashSet<JSObject>();
        if (filter != null) {
            if (filter.getDeployConfigurations() != null && !filter.getDeployConfigurations().isEmpty()) {
                List<Configuration> depFolders = filter.getDeployConfigurations().stream()
                        .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                        .map(item -> item.getConfiguration())
                        .collect(Collectors.toList());
                Set<DBItemDeploymentHistory> allItems = new HashSet<DBItemDeploymentHistory>();
                if (depFolders != null && !depFolders.isEmpty()) {
                    allItems.addAll(getLatestActiveDepHistoryEntriesFromFolders(depFolders, dbLayer));
                }
                List<DBItemDeploymentHistory> deploymentDbItems = dbLayer.getFilteredDeployments(filter);
                if (deploymentDbItems != null && !deploymentDbItems.isEmpty()) {
                    allItems.addAll(deploymentDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream()
                        .filter(Objects::nonNull)
                        .filter(item -> !item.getType().equals(ConfigurationType.FOLDER.intValue()))
                        .forEach(item -> {
                            if (commitId != null) {
                                dbLayer.storeCommitIdForLaterUsage(item, commitId);
                            }
                            allObjects.add(getJSObjectFromDBItem(item, commitId));
                        });
                }
            }
            if (filter.getDraftConfigurations() != null && !filter.getDraftConfigurations().isEmpty()) {
                List<Configuration> draftFolders = filter.getDraftConfigurations().stream()
                        .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                        .map(item -> item.getConfiguration())
                        .collect(Collectors.toList());
                Set<DBItemInventoryConfiguration> allItems = new HashSet<DBItemInventoryConfiguration>();
                if (draftFolders != null && !draftFolders.isEmpty()) {
                    allItems.addAll(getDeployableInventoryConfigurationsfromFolders(draftFolders, dbLayer));
                }
                List<DBItemInventoryConfiguration> configurationDbItems = dbLayer.getFilteredDeployableConfigurations(filter);
                if (configurationDbItems != null && !configurationDbItems.isEmpty()) {
                    allItems.addAll(configurationDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream()
                        .filter(Objects::nonNull)
                        .filter(item -> !item.getTypeAsEnum().equals(ConfigurationType.FOLDER))
                        .forEach(item -> {
                            if (commitId != null) {
                                dbLayer.storeCommitIdForLaterUsage(item, commitId);
                            }
                            allObjects.add(mapInvConfigToJSObject(item));
                        });
                }
            } 
        }
        return allObjects;
    }
    
    public static Set<ConfigurationObject> getReleasableObjectsFromDB(ReleasablesFilter filter, DBLayerDeploy dbLayer) 
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, 
            IOException, SOSHibernateException {
        Set<ConfigurationObject> allObjects = new HashSet<ConfigurationObject>();
        if (filter != null) {
            if (filter.getReleasedConfigurations() != null && !filter.getReleasedConfigurations().isEmpty()) {
                List<Configuration> releasedFolders = filter.getReleasedConfigurations().stream()
                        .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                        .map(item -> item.getConfiguration())
                        .collect(Collectors.toList());
                Set<DBItemInventoryReleasedConfiguration> allItems = new HashSet<DBItemInventoryReleasedConfiguration>();
                if (releasedFolders != null && !releasedFolders.isEmpty()) {
                    allItems.addAll(getReleasedInventoryConfigurationsfromFolders(releasedFolders, dbLayer));
                }
                List<DBItemInventoryReleasedConfiguration> configurationDbItems = dbLayer.getFilteredReleasedConfigurations(filter);
                if (configurationDbItems != null && !configurationDbItems.isEmpty()) {
                    allItems.addAll(configurationDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream()
                        .filter(Objects::nonNull)
                        .filter(item -> !item.getTypeAsEnum().equals(ConfigurationType.FOLDER))
                        .forEach(item -> allObjects.add(getConfigurationObjectFromDBItem(item)));
                }
            }
            if (filter.getDraftConfigurations() != null && !filter.getDraftConfigurations().isEmpty()) {
                List<Configuration> draftFolders = filter.getDraftConfigurations().stream()
                        .filter(item -> item.getConfiguration().getObjectType().equals(ConfigurationType.FOLDER))
                        .map(item -> item.getConfiguration())
                        .collect(Collectors.toList());
                Set<DBItemInventoryConfiguration> allItems = new HashSet<DBItemInventoryConfiguration>();
                if (draftFolders != null && !draftFolders.isEmpty()) {
                    allItems.addAll(getReleasableInventoryConfigurationsfromFolders(draftFolders, dbLayer));
                }
                List<DBItemInventoryConfiguration> configurationDbItems = dbLayer.getFilteredReleasableConfigurations(filter);
                if (configurationDbItems != null && !configurationDbItems.isEmpty()) {
                    allItems.addAll(configurationDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream()
                    .filter(Objects::nonNull)
                    .filter(item -> !item.getTypeAsEnum().equals(ConfigurationType.FOLDER))
                    .forEach(item -> allObjects.add(getConfigurationObjectFromDBItem(item)));
                }
            } 
        }
        return allObjects;
    }
    
    private static JSObject mapInvConfigToJSObject (DBItemInventoryConfiguration item) {
        return mapInvConfigToJSObject(item, null);
    }
    
    private static JSObject mapInvConfigToJSObject (DBItemInventoryConfiguration item, String commitId) {
        try {
            JSObject jsObject = new JSObject();
//            jsObject.setId(item.getId());
            jsObject.setPath(item.getPath());
            jsObject.setObjectType(PublishUtils.mapConfigurationType(ConfigurationType.fromValue(item.getType())));
            switch (jsObject.getObjectType()) {
            case WORKFLOW:
                Workflow workflow = om.readValue(item.getContent().getBytes(), Workflow.class);
                if (commitId != null) {
                    workflow.setVersionId(commitId);
                }
                jsObject.setContent(workflow);
                break;
            case LOCK:
                Lock lock = om.readValue(item.getContent().getBytes(), Lock.class);
                if (commitId != null) {
                    lock.setVersionId(commitId);
                }
                jsObject.setContent(lock);
                break;
            case JUNCTION:
                Junction junction = om.readValue(item.getContent().getBytes(), Junction.class);
                if (commitId != null) {
                    junction.setVersionId(commitId);
                }
                jsObject.setContent(junction);
                break;
            case JOBCLASS:
                JobClass jobClass = om.readValue(item.getContent().getBytes(), JobClass.class);
                if (commitId != null) {
                    jobClass.setVersionId(commitId);
                }
                jsObject.setContent(jobClass);
                break;
            }
            jsObject.setAccount(Globals.defaultProfileAccount);
            // TODO: setVersion
//        jsObject.setVersion(item.getVersion());
            jsObject.setModified(item.getModified());
            return jsObject;
        } catch (IOException e) {
            throw new JocException(e);
        }
    }

    private static JSObject getJSObjectFromDBItem (DBItemDeploymentHistory item) {
        return getJSObjectFromDBItem(item, null);
    }

    private static JSObject getJSObjectFromDBItem (DBItemDeploymentHistory item, String commitId) {
        try {
            JSObject jsObject = new JSObject();
//            jsObject.setId(item.getId());
            jsObject.setPath(item.getPath());
            jsObject.setObjectType(DeployType.fromValue(item.getType()));
            switch (jsObject.getObjectType()) {
            case WORKFLOW:
                Workflow workflow = om.readValue(item.getInvContent().getBytes(), Workflow.class);
                if (commitId != null) {
                    workflow.setVersionId(commitId);
                }
                jsObject.setContent(workflow);
                break;
            case JOBCLASS:
                JobClass jobClass = om.readValue(item.getInvContent().getBytes(), JobClass.class);
                if (commitId != null) {
                    jobClass.setVersionId(commitId);
                }
                jsObject.setContent(jobClass);
                break;
            case LOCK:
                Lock lock = om.readValue(item.getInvContent().getBytes(), Lock.class);
                if (commitId != null) {
                    lock.setVersionId(commitId);
                }
                jsObject.setContent(lock);
                break;
            case JUNCTION:
                Junction junction = om.readValue(item.getInvContent().getBytes(), Junction.class);
                if (commitId != null) {
                    junction.setVersionId(commitId);
                }
                jsObject.setContent(junction);
                break;
            }
            jsObject.setVersion(item.getVersion());
            jsObject.setAccount(Globals.defaultProfileAccount);
            return jsObject;
        } catch (IOException e) {
            throw new JocException(e);
        }
    }
    
    private static ConfigurationObject getConfigurationObjectFromDBItem(DBItemInventoryConfiguration item) {
        try {
            ConfigurationObject configuration = new ConfigurationObject();
//            configuration.setId(item.getId());
            configuration.setPath(item.getPath());
            configuration.setObjectType(ConfigurationType.fromValue(item.getType()));
            switch (configuration.getObjectType()) {
            case WORKINGDAYSCALENDAR:
            case NONWORKINGDAYSCALENDAR:
                Calendar calendar = om.readValue(item.getContent().getBytes(), Calendar.class);
                configuration.setConfiguration(calendar);
                break;
            case SCHEDULE:
                Schedule schedule = om.readValue(item.getContent(), Schedule.class);
                configuration.setConfiguration(schedule);
                break;
            default:
                break;
            }
            return configuration;
        } catch (IOException e) {
            throw new JocException(e);
        }
    }

    private static ConfigurationObject getConfigurationObjectFromDBItem(DBItemInventoryReleasedConfiguration item) {
        try {
            ConfigurationObject configuration = new ConfigurationObject();
//            configuration.setId(item.getId());
            configuration.setPath(item.getPath());
            configuration.setObjectType(ConfigurationType.fromValue(item.getType()));
            switch (configuration.getObjectType()) {
            case WORKINGDAYSCALENDAR:
            case NONWORKINGDAYSCALENDAR:
                Calendar calendar = om.readValue(item.getContent().getBytes(), Calendar.class);
                configuration.setConfiguration(calendar);
                break;
            case SCHEDULE:
                Schedule schedule = om.readValue(item.getContent(), Schedule.class);
                configuration.setConfiguration(schedule);
                break;
            default:
                break;
            }
            return configuration;
        } catch (IOException e) {
            throw new JocException(e);
        }
    }

    private static boolean checkObjectNotEmpty (Workflow workflow) {
        if (workflow.getDocumentationId() == null 
                && workflow.getInstructions() == null 
                && workflow.getJobs() == null
                && workflow.getPath() == null
                && workflow.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }
    
    private static boolean checkObjectNotEmpty (Junction junction) {
        if (junction.getDocumentationId() == null
                && junction.getLifetime() == null
                && junction.getOrderId() == null
                && junction.getPath() == null
                && junction.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }
    
    private static boolean checkObjectNotEmpty (JobClass jobClass) {
        if (jobClass.getDocumentationId() == null 
                && jobClass.getMaxProcesses() == null
                && jobClass.getPath() == null
                && jobClass.getPriority() == null
                && jobClass.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }
    
    private static boolean checkObjectNotEmpty (Lock lock) {
        if (lock.getDocumentationId() == null 
                && lock.getCapacity() == null
                && lock.getPath() == null
                && lock.getMaxNonExclusive() == null
                && lock.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }
    
    private static boolean checkObjectNotEmpty (Schedule schedule) {
        if (schedule.getDocumentationId() == null 
                && schedule.getPlanOrderAutomatically() == null
                && schedule.getPath() == null
                && schedule.getCalendars() == null
                && schedule.getWorkflowPath() == null
                && schedule.getSubmitOrderToControllerWhenPlanned() == null
                && schedule.getNonWorkingCalendars() == null
                && schedule.getVariables() == null) {
            return false;
        } else {
            return true;
        }
    }
    
    private static boolean checkObjectNotEmpty (Calendar calendar) {
        if (calendar.getDocumentationId() == null 
                && calendar.getExcludes() == null
                && calendar.getPath() == null
                && calendar.getFrom() == null
                && calendar.getIncludes() == null
                && calendar.getName() == null 
                && calendar.getTo() == null 
                && calendar.getType() == null) {
            return false;
        } else {
            return true;
        }
    }
    
    private static JocMetaInfo getJocMetaInfo() {
        Properties jocProperties = Globals.sosCockpitProperties.getProperties();
        JocMetaInfo jocMetaInfo = new JocMetaInfo();
        if (jocProperties.containsKey("joc_version")) {
            jocMetaInfo.setJocVersion(jocProperties.getProperty("joc_version"));
        }
        if (jocProperties.containsKey("inventory_schema_version")) {
            jocMetaInfo.setInventorySchemaVersion(jocProperties.getProperty("inventory_schema_version"));
        }
        if(jocProperties.containsKey("api_version")) {
            jocMetaInfo.setApiVersion(jocProperties.getProperty("api_version"));
        }
        return jocMetaInfo;
    }

    private static boolean isJocMetaInfoNullOrEmpty (JocMetaInfo jocMetaInfo) {
        if (jocMetaInfo == null ||
                ((jocMetaInfo.getJocVersion() == null || jocMetaInfo.getJocVersion().isEmpty())
                        && (jocMetaInfo.getInventorySchemaVersion() == null || jocMetaInfo.getInventorySchemaVersion().isEmpty())
                        && (jocMetaInfo.getApiVersion() == null || jocMetaInfo.getApiVersion().isEmpty()))) {
            return true;
        } else {
            return false;
        }
    }
}