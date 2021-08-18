package com.sos.joc.publish.util;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
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
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
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
import org.apache.commons.lang.builder.EqualsBuilder;
import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.sign.keys.sign.SignObject;
import com.sos.commons.sign.keys.verify.VerifySignature;
import com.sos.inventory.model.schedule.Schedule;
import com.sos.inventory.model.calendar.Calendar;
import com.sos.inventory.model.calendar.CalendarType;
import com.sos.inventory.model.deploy.DeployType;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.inventory.JsonSerializer;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryCertificate;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.deploy.DeployHistoryJobResourceEvent;
import com.sos.joc.event.bean.deploy.DeployHistoryWorkflowEvent;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocDeployException;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocImportException;
import com.sos.joc.exceptions.JocKeyNotParseableException;
import com.sos.joc.exceptions.JocMissingKeyException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.Version;
import com.sos.joc.model.calendar.NonWorkingDaysCalendarEdit;
import com.sos.joc.model.calendar.WorkingDaysCalendarEdit;
import com.sos.joc.model.common.IDeployObject;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.ConfigurationObject;
import com.sos.joc.model.inventory.board.BoardEdit;
import com.sos.joc.model.inventory.board.BoardPublish;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.inventory.fileordersource.FileOrderSourceEdit;
import com.sos.joc.model.inventory.fileordersource.FileOrderSourcePublish;
import com.sos.joc.model.inventory.jobclass.JobClassEdit;
import com.sos.joc.model.inventory.jobclass.JobClassPublish;
import com.sos.joc.model.inventory.jobresource.JobResourceEdit;
import com.sos.joc.model.inventory.jobresource.JobResourcePublish;
import com.sos.joc.model.inventory.lock.LockEdit;
import com.sos.joc.model.inventory.lock.LockPublish;
import com.sos.joc.model.inventory.workflow.WorkflowEdit;
import com.sos.joc.model.inventory.workflow.WorkflowPublish;
import com.sos.joc.model.joc.JocMetaInfo;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.ControllerObject;
import com.sos.joc.model.publish.DeployablesFilter;
import com.sos.joc.model.publish.DeployablesValidFilter;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.ReleasablesFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.JocKeyType;
import com.sos.joc.model.sign.Signature;
import com.sos.joc.model.sign.SignaturePath;
import com.sos.joc.publish.common.ConfigurationObjectFileExtension;
import com.sos.joc.publish.common.ControllerObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpdateableFileOrderSourceAgentName;
import com.sos.joc.publish.mapper.UpdateableWorkflowJobAgentName;
import com.sos.sign.model.board.Board;
import com.sos.sign.model.fileordersource.FileOrderSource;
import com.sos.sign.model.job.Job;
import com.sos.sign.model.jobclass.JobClass;
import com.sos.sign.model.jobresource.JobResource;
import com.sos.sign.model.lock.Lock;
import com.sos.sign.model.workflow.Workflow;
import com.sos.webservices.order.initiator.model.ScheduleEdit;

import io.vavr.control.Either;
import js7.base.crypt.SignedString;
import js7.base.crypt.SignerId;
import js7.base.problem.Problem;
import js7.data.agent.AgentPath;
import js7.data.board.BoardPath;
import js7.data.item.VersionId;
import js7.data.lock.LockPath;
import js7.data.orderwatch.OrderWatchPath;
import js7.data.workflow.WorkflowPath;
import js7.data_for_java.board.JBoard;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.lock.JLock;
import js7.data_for_java.orderwatch.JFileWatch;
import js7.data_for_java.value.JExpression;
import reactor.core.publisher.Flux;

public abstract class PublishUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(PublishUtils.class);
    private static final String JOC_META_INFO_FILENAME = "meta_inf";

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
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PRIVATE.value(), keyPair.getPrivateKey(), keyPair.getCertificate(), account, secLvl, keyPair
                        .getKeyAlgorithm());
            } else if (keyPair.getPrivateKey() == null && keyPair.getPublicKey() != null) {
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PUBLIC.value(), keyPair.getPublicKey(), keyPair.getCertificate(), account, secLvl, keyPair
                        .getKeyAlgorithm());
            } else if (keyPair.getPrivateKey() == null && keyPair.getPublicKey() == null && keyPair.getCertificate() != null) {
                switch (secLvl) {
                case LOW:
                case MEDIUM:
                    dbLayerKeys.saveOrUpdateKey(JocKeyType.PRIVATE.value(), keyPair.getCertificate(), account, secLvl, keyPair.getKeyAlgorithm());
                    break;
                case HIGH:
                    dbLayerKeys.saveOrUpdateKey(JocKeyType.PUBLIC.value(), keyPair.getCertificate(), account, secLvl, keyPair.getKeyAlgorithm());
                }
            }
        }
    }

    public static void storeAuthCA(JocKeyPair keyPair, SOSHibernateSession hibernateSession) throws SOSHibernateException {
        DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
        if (keyPair != null) {
            dbLayerKeys.saveOrUpdateKey(JocKeyType.CA.value(), keyPair.getPrivateKey(), keyPair.getCertificate(), "", JocSecurityLevel.LOW,
                    SOSKeyConstants.ECDSA_ALGORITHM_NAME);
        }
    }

    public static Map<DBItemDeploymentHistory, DBItemDepSignatures> getDraftsWithSignature(String commitId, String account,
            List<DBItemDeploymentHistory> unsignedDeployments, Set<UpdateableWorkflowJobAgentName> updateableAgentNames, JocKeyPair keyPair,
            String controllerId, SOSHibernateSession session) throws JocMissingKeyException, JsonParseException, JsonMappingException,
            SOSHibernateException, IOException, PGPException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException,
            SignatureException, CertificateException {
        Map<DBItemDeploymentHistory, DBItemDepSignatures> signedDrafts = new HashMap<>();
        if (keyPair.getPrivateKey() == null || keyPair.getPrivateKey().isEmpty()) {
            throw new JocMissingKeyException(
                    "No private key found for signing! - Please check your private key from the key management section in your profile.");
        } else {
            DBItemDepSignatures sig = null;
            for (DBItemDeploymentHistory deployed : unsignedDeployments) {
                updateVersionId(deployed, commitId);
                updatePath(deployed);
                // update agentName in Workflow jobs before signing agentName -> agentId
                if (deployed.getType() == ConfigurationType.WORKFLOW.intValue()) {
                    replaceAgentNameWithAgentId(deployed, updateableAgentNames, controllerId);
                }
                deployed.setContent(JsonSerializer.serializeAsString(deployed.readUpdateableContent()));
                if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                    if (deployed.getType() == DeployType.WORKFLOW.intValue() || deployed.getType() == DeployType.JOBRESOURCE.intValue()) {
                        sig = new DBItemDepSignatures();
                        sig.setSignature(SignObject.signPGP(keyPair.getPrivateKey(), deployed.getContent(), null));
                    }
                } else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                    KeyPair kp = null;
                    if (keyPair.getPrivateKey().startsWith(SOSKeyConstants.PRIVATE_RSA_KEY_HEADER)) {
                        kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(keyPair.getPrivateKey());
                    } else {
                        kp = KeyUtil.getKeyPairFromPrivatKeyString(keyPair.getPrivateKey());
                    }
                    if (deployed.getType() == DeployType.WORKFLOW.intValue() || deployed.getType() == ConfigurationType.JOBRESOURCE.intValue()) {
                        sig = new DBItemDepSignatures();
                        sig.setSignature(SignObject.signX509(kp.getPrivate(), deployed.getContent()));
                    }
                } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(keyPair.getKeyAlgorithm())) {
                    KeyPair kp = KeyUtil.getKeyPairFromECDSAPrivatKeyString(keyPair.getPrivateKey());
                    // X509Certificate cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
                    if (deployed.getType() == DeployType.WORKFLOW.intValue() || deployed.getType() == ConfigurationType.JOBRESOURCE.intValue()) {
                        sig = new DBItemDepSignatures();
                        sig.setSignature(SignObject.signX509(SOSKeyConstants.ECDSA_SIGNER_ALGORITHM, kp.getPrivate(), deployed.getContent()));
                    }
                }
                if (sig != null) {
                    sig.setAccount(account);
                    sig.setDepHistoryId(deployed.getId());
                    sig.setInvConfigurationId(deployed.getInventoryConfigurationId());
                    sig.setModified(Date.from(Instant.now()));
                    session.save(sig);
                } else {
                    deployed.setSignedContent(".");
                }
                signedDrafts.put(deployed, sig);
            }
        }
        return signedDrafts;
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
            LOGGER.trace(String.format("Signature of object with name %1$s could not be verified! Object will not be deployed.", signedDraft
                    .getName()));
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
            LOGGER.trace(String.format("Signature of object with name %1$s could not be verified! Object will not be deployed.", signedDeployment
                    .getName()));
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
                LOGGER.trace(String.format("Signature of object with name %1$s could not be verified! Object will not be deployed.", signedDraft
                        .getName()));
            } else {
                verifiedDraft = signedDraft;
            }
        } else if (publicKey != null) {
            verified = VerifySignature.verifyX509(publicKey, signedDraft.getContent(), draftSignature.getSignature());
            if (!verified) {
                LOGGER.trace(String.format("Signature of object with name %1$s could not be verified! Object will not be deployed.", signedDraft
                        .getName()));
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
                LOGGER.trace(String.format("Signature of object with name %1$s could not be verified! Object will not be deployed.", signedDeployment
                        .getName()));
            } else {
                verifiedDeployment = signedDeployment;
            }
        } else if (publicKey != null) {
            verified = VerifySignature.verifyX509(publicKey, signedDeployment.getContent(), deployedSignature.getSignature());
            if (!verified) {
                LOGGER.trace(String.format("Signature of object with name %1$s could not be verified! Object will not be deployed.", signedDeployment
                        .getName()));
            } else {
                verifiedDeployment = signedDeployment;
            }
        } else {
            throw new JocMissingKeyException("Neither PublicKey nor Certificate found for signature verification.");
        }
        return verifiedDeployment;
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdatePGP(String commitId,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, String controllerId, DBLayerDeploy dbLayer) throws SOSException,
            IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateItemOperationsSimple = new HashSet<JUpdateItemOperation>();
        Set<JUpdateItemOperation> updateItemOperationsSigned = new HashSet<JUpdateItemOperation>();
        if (alreadyDeployed != null) {
            updateItemOperationsSigned.addAll(alreadyDeployed.entrySet().stream().filter(item -> item.getKey().getType() == DeployType.WORKFLOW
                    .intValue()).map(item -> {
                        LOGGER.debug("JSON send to controller: ");
                        try {
                            String json = JsonSerializer.serializeAsString(item.getKey().readUpdateableContent());
                            LOGGER.debug(json);
                            return JUpdateItemOperation.addOrChangeSigned(SignedString.of(json, SOSKeyConstants.PGP_ALGORITHM_NAME, item.getValue()
                                    .getSignature()));
                        } catch (JsonProcessingException e) {
                            return null;
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toSet()));
            updateItemOperationsSigned.addAll(alreadyDeployed.entrySet().stream().filter(item -> item.getKey().getType() == DeployType.JOBRESOURCE
                    .intValue()).map(item -> {
                        LOGGER.debug("JSON send to controller: ");
                        try {
                            String json = JsonSerializer.serializeAsString(item.getKey().readUpdateableContent());
                            LOGGER.debug(json);
                            return JUpdateItemOperation.addOrChangeSigned(SignedString.of(json, SOSKeyConstants.PGP_ALGORITHM_NAME, item.getValue()
                                    .getSignature()));
                        } catch (JsonProcessingException e) {
                            return null;
                        }
                    }).collect(Collectors.toSet()));
            updateItemOperationsSimple.addAll(alreadyDeployed.keySet().stream().filter(item -> item.getType() == ConfigurationType.LOCK.intValue())
                    .map(item -> {
                        try {
                            Lock lock = (Lock) item.readUpdateableContent();
                            lock.setPath(Paths.get(item.getPath()).getFileName().toString());
                            return JUpdateItemOperation.addOrChangeSimple(getJLock(lock));
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toSet()));
            updateItemOperationsSimple.addAll(alreadyDeployed.keySet().stream().filter(item -> item.getType() == ConfigurationType.FILEORDERSOURCE
                    .intValue()).map(item -> {
                        try {
                            FileOrderSource fileOrderSource = (FileOrderSource) item.readUpdateableContent();
                            fileOrderSource.setPath(Paths.get(item.getPath()).getFileName().toString());
                            return JUpdateItemOperation.addOrChangeSimple(getJFileWatch(fileOrderSource));
                        } catch (JocDeployException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toSet()));
            // Board
            updateItemOperationsSimple.addAll(alreadyDeployed.keySet().stream().filter(item -> item.getType() == DeployType.NOTICEBOARD.intValue()).map(
                    item -> {
                        try {
                            Board board = (Board) item.readUpdateableContent();
                            if (board.getPath() == null) {
                                board.setPath(Paths.get(item.getPath()).getFileName().toString());
                            }
                            return JUpdateItemOperation.addOrChangeSimple(getJBoard(board));
                        } catch (JocDeployException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toSet()));
        }
        return ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.fromIterable(updateItemOperationsSimple), Flux.just(JUpdateItemOperation
                .addVersion(VersionId.of(commitId))), Flux.fromIterable(updateItemOperationsSigned)));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdatePGPFromImport(String commitId,
            Map<ControllerObject, DBItemDepSignatures> drafts, String controllerId, DBLayerDeploy dbLayer) throws SOSException, IOException,
            InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateItemsOperationsSigned = new HashSet<JUpdateItemOperation>();
        Set<JUpdateItemOperation> updateItemsOperationsSimple = new HashSet<JUpdateItemOperation>();
        if (drafts != null) {
            updateItemsOperationsSigned.addAll(drafts.entrySet().stream().filter(item -> item.getKey().getObjectType().equals(DeployType.WORKFLOW))
                    .map(item -> {
                        try {
                            LOGGER.debug("JSON send to controller: ");
                            String json = JsonSerializer.serializeAsString(item.getKey().getContent());
                            LOGGER.debug(json);
                            return JUpdateItemOperation.addOrChangeSigned(SignedString.of(json, SOSKeyConstants.PGP_ALGORITHM_NAME, item.getValue()
                                    .getSignature()));
                        } catch (JsonProcessingException e1) {
                            throw new JocDeployException(e1);
                        }
                    }).collect(Collectors.toSet()));
            updateItemsOperationsSigned.addAll(drafts.entrySet().stream().filter(item -> item.getKey().getObjectType().equals(DeployType.JOBRESOURCE))
                    .map(item -> {
                        try {
                            LOGGER.debug("JSON send to controller: ");
                            String json = JsonSerializer.serializeAsString(item.getKey().getContent());
                            LOGGER.debug(json);
                            return JUpdateItemOperation.addOrChangeSigned(SignedString.of(json, SOSKeyConstants.PGP_ALGORITHM_NAME, item.getValue()
                                    .getSignature()));
                        } catch (JsonProcessingException e1) {
                            throw new JocDeployException(e1);
                        }
                    }).collect(Collectors.toSet()));
            updateItemsOperationsSimple.addAll(drafts.keySet().stream().filter(item -> item.getObjectType().equals(DeployType.LOCK)).map(item -> {
                try {
                    Lock lock = (Lock) item.getContent();
                    lock.setPath(Paths.get(item.getPath()).getFileName().toString());
                    return JUpdateItemOperation.addOrChangeSimple(getJLock(lock));
                } catch (Exception e) {
                    throw new JocDeployException(e);
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet()));
            updateItemsOperationsSimple.addAll(drafts.keySet().stream().filter(item -> item.getObjectType().equals(DeployType.FILEORDERSOURCE)).map(
                    item -> {
                        try {
                            FileOrderSource fileOrderSource = (FileOrderSource) item.getContent();
                            if (fileOrderSource.getPath() == null) {
                                fileOrderSource.setPath(Paths.get(item.getPath()).getFileName().toString());
                            }
                            return JUpdateItemOperation.addOrChangeSimple(getJFileWatch(fileOrderSource));
                        } catch (JocDeployException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toSet()));
            // Board
            updateItemsOperationsSimple.addAll(drafts.keySet().stream().filter(item -> item.getObjectType().equals(DeployType.NOTICEBOARD)).map(item -> {
                try {
                    Board board = (Board) item.getContent();
                    if (board.getPath() == null) {
                        board.setPath(Paths.get(item.getPath()).getFileName().toString());
                    }
                    return JUpdateItemOperation.addOrChangeSimple(getJBoard(board));
                } catch (JocDeployException e) {
                    throw e;
                } catch (Exception e) {
                    throw new JocDeployException(e);
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet()));
        }
        return ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.fromIterable(updateItemsOperationsSimple), Flux.just(JUpdateItemOperation
                .addVersion(VersionId.of(commitId))), Flux.fromIterable(updateItemsOperationsSigned)));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdateWithX509Certificate(String commitId,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, String controllerId, DBLayerDeploy dbLayer, String signatureAlgorithm,
            String certificate) throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateRepoOperationsSigned = new HashSet<JUpdateItemOperation>();
        Set<JUpdateItemOperation> updateRepoOperationsSimple = new HashSet<JUpdateItemOperation>();
        if (alreadyDeployed != null) {
            // workflows
            updateRepoOperationsSigned.addAll(alreadyDeployed.entrySet().stream().filter(item -> item.getKey().getType() == DeployType.WORKFLOW
                    .intValue()).map(item -> {
                        try {
                            return JUpdateItemOperation.addOrChangeSigned(getSignedStringWithCertificate(JsonSerializer.serializeAsString(item
                                    .getKey().readUpdateableContent()), item.getValue().getSignature(), signatureAlgorithm, certificate));
                        } catch (IOException e) {
                            return null;
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toSet()));
            // job resources
            updateRepoOperationsSigned.addAll(alreadyDeployed.entrySet().stream().filter(item -> item.getKey().getType() == DeployType.JOBRESOURCE
                    .intValue()).map(item -> {
                        try {
                            return JUpdateItemOperation.addOrChangeSigned(getSignedStringWithCertificate(JsonSerializer.serializeAsString(item
                                    .getKey().readUpdateableContent()), item.getValue().getSignature(), signatureAlgorithm, certificate));
                        } catch (IOException e) {
                            return null;
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toSet()));
            // locks
            updateRepoOperationsSimple.addAll(alreadyDeployed.keySet().stream().filter(item -> item.getType() == DeployType.LOCK.intValue()).map(
                    item -> {
                        try {
                            Lock lock = (Lock) item.readUpdateableContent();
                            lock.setPath(Paths.get(item.getPath()).getFileName().toString());
                            return JUpdateItemOperation.addOrChangeSimple(getJLock(lock));
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
            // file order sources
            updateRepoOperationsSimple.addAll(alreadyDeployed.keySet().stream().filter(item -> item.getType() == DeployType.FILEORDERSOURCE
                    .intValue()).map(item -> {
                        try {
                            FileOrderSource fileOrderSource = (FileOrderSource) item.readUpdateableContent();
                            if (fileOrderSource.getPath() == null) {
                                fileOrderSource.setPath(Paths.get(item.getPath()).getFileName().toString());
                            }
                            return JUpdateItemOperation.addOrChangeSimple(getJFileWatch(fileOrderSource));
                        } catch (JocDeployException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
            // Board
            updateRepoOperationsSimple.addAll(alreadyDeployed.keySet().stream().filter(item -> item.getType() == DeployType.NOTICEBOARD.intValue()).map(
                    item -> {
                        try {
                            Board board = (Board) item.readUpdateableContent();
                            if (board.getPath() == null) {
                                board.setPath(Paths.get(item.getPath()).getFileName().toString());
                            }
                            return JUpdateItemOperation.addOrChangeSimple(getJBoard(board));
                        } catch (JocDeployException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
            // TODO: when implemented in controller
            // job classes
            // TODO: when implemented in controller
        }
        return ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.fromIterable(updateRepoOperationsSimple), Flux.just(JUpdateItemOperation
                .addVersion(VersionId.of(commitId))), Flux.fromIterable(updateRepoOperationsSigned)));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdateWithX509SignerDN(String commitId,
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, String controllerId, DBLayerDeploy dbLayer, String signatureAlgorithm,
            String signerDN) throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateRepoOperationsSigned = new HashSet<JUpdateItemOperation>();
        Set<JUpdateItemOperation> updateRepoOperationsSimple = new HashSet<JUpdateItemOperation>();
        if (alreadyDeployed != null) {
            // workflows
            updateRepoOperationsSigned.addAll(alreadyDeployed.entrySet().stream().filter(item -> item.getKey().getType() == DeployType.WORKFLOW
                    .intValue()).map(item -> {
                        try {
                            return JUpdateItemOperation.addOrChangeSigned(getSignedStringWithSignerDN(JsonSerializer.serializeAsString(item.getKey()
                                    .readUpdateableContent()), item.getValue().getSignature(), signatureAlgorithm, signerDN));
                        } catch (IOException e) {
                            return null;
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toSet()));
            // job resources
            updateRepoOperationsSigned.addAll(alreadyDeployed.entrySet().stream().filter(item -> item.getKey().getType() == DeployType.JOBRESOURCE
                    .intValue()).map(item -> {
                        try {
                            return JUpdateItemOperation.addOrChangeSigned(getSignedStringWithSignerDN(JsonSerializer.serializeAsString(item.getKey()
                                    .readUpdateableContent()), item.getValue().getSignature(), signatureAlgorithm, signerDN));
                        } catch (IOException e) {
                            return null;
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toSet()));
            // locks
            updateRepoOperationsSimple.addAll(alreadyDeployed.keySet().stream().filter(item -> item.getType() == DeployType.LOCK.intValue()).map(
                    item -> {
                        try {
                            Lock lock = (Lock) item.readUpdateableContent();
                            lock.setPath(Paths.get(item.getPath()).getFileName().toString());
                            return JUpdateItemOperation.addOrChangeSimple(getJLock(lock));
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
            // file order sources
            updateRepoOperationsSimple.addAll(alreadyDeployed.keySet().stream().filter(item -> item.getType() == DeployType.FILEORDERSOURCE
                    .intValue()).map(item -> {
                        try {
                            FileOrderSource fileOrderSource = (FileOrderSource) item.readUpdateableContent();
                            fileOrderSource.setPath(item.getName());
                            return JUpdateItemOperation.addOrChangeSimple(getJFileWatch(fileOrderSource));
                        } catch (JocDeployException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
            // Board
            updateRepoOperationsSimple.addAll(alreadyDeployed.keySet().stream().filter(item -> item.getType() == DeployType.NOTICEBOARD.intValue()).map(
                    item -> {
                        try {
                            Board board = (Board) item.readUpdateableContent();
                            if (board.getPath() == null) {
                                board.setPath(Paths.get(item.getPath()).getFileName().toString());
                            }
                            return JUpdateItemOperation.addOrChangeSimple(getJBoard(board));
                        } catch (JocDeployException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
            // job classes
            // TODO: when implemented in controller
        }
        return ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.fromIterable(updateRepoOperationsSimple), Flux.just(JUpdateItemOperation
                .addVersion(VersionId.of(commitId))), Flux.fromIterable(updateRepoOperationsSigned)));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdateWithX509CertificateFromImport(String commitId,
            Map<ControllerObject, DBItemDepSignatures> drafts, String controllerId, DBLayerDeploy dbLayer, String signatureAlgorithm,
            String certificate) throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateItemsOperationsSigned = new HashSet<JUpdateItemOperation>();
        Set<JUpdateItemOperation> updateItemsOperationsSimple = new HashSet<JUpdateItemOperation>();
        if (drafts != null) {
            // workflows
            updateItemsOperationsSigned.addAll(drafts.entrySet().stream().filter(item -> item.getKey().getObjectType().equals(DeployType.WORKFLOW))
                    .map(item -> {
                        try {
                            return JUpdateItemOperation.addOrChangeSigned(getSignedStringWithCertificate(JsonSerializer.serializeAsString(item
                                    .getKey().getContent()), item.getValue().getSignature(), signatureAlgorithm, certificate));
                        } catch (IOException e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
            // job resources
            updateItemsOperationsSigned.addAll(drafts.keySet().stream().filter(item -> item.getObjectType().equals(DeployType.JOBRESOURCE)).map(
                    item -> {
                        try {
                            return JUpdateItemOperation.addOrChangeSigned(getSignedStringWithCertificate(JsonSerializer.serializeAsString(item
                                    .getContent()), drafts.get(item).getSignature(), signatureAlgorithm, certificate));
                        } catch (JsonProcessingException e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
            // locks
            updateItemsOperationsSimple.addAll(drafts.keySet().stream().filter(item -> item.getObjectType().equals(ConfigurationType.LOCK)).map(
                    item -> {
                        try {
                            Lock lock = (Lock) item.getContent();
                            lock.setPath(Paths.get(item.getPath()).getFileName().toString());
                            return JUpdateItemOperation.addOrChangeSimple(getJLock(lock));
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
            // file order sources
            updateItemsOperationsSimple.addAll(drafts.keySet().stream().filter(item -> item.getObjectType().equals(ConfigurationType.FILEORDERSOURCE))
                    .map(item -> {
                        try {
                            FileOrderSource fileOrderSource = (FileOrderSource) item.getContent();
                            fileOrderSource.setPath(Paths.get(item.getPath()).getFileName().toString());
                            return JUpdateItemOperation.addOrChangeSimple(getJFileWatch(fileOrderSource));
                        } catch (JocDeployException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
            // Board
            updateItemsOperationsSimple.addAll(drafts.keySet().stream().filter(item -> item.getObjectType().equals(DeployType.NOTICEBOARD)).map(item -> {
                try {
                    Board board = (Board) item.getContent();
                    board.setPath(Paths.get(item.getPath()).getFileName().toString());
                    return JUpdateItemOperation.addOrChangeSimple(getJBoard(board));
                } catch (JocDeployException e) {
                    throw e;
                } catch (Exception e) {
                    throw new JocDeployException(e);
                }
            }).collect(Collectors.toSet()));
            // job classes
            // TODO: when implemented in controller
        }
        return ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.fromIterable(updateItemsOperationsSimple), Flux.just(JUpdateItemOperation
                .addVersion(VersionId.of(commitId))), Flux.fromIterable(updateItemsOperationsSigned)));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsAddOrUpdateWithX509SignerDNFromImport(String commitId,
            Map<ControllerObject, DBItemDepSignatures> drafts, String controllerId, DBLayerDeploy dbLayer, String signatureAlgorithm, String signerDN)
            throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateItemOperation> updateItemsOperationsSigned = new HashSet<JUpdateItemOperation>();
        Set<JUpdateItemOperation> updateItemsOperationsSimple = new HashSet<JUpdateItemOperation>();
        if (drafts != null) {
            // workflows
            updateItemsOperationsSigned.addAll(drafts.entrySet().stream().filter(item -> item.getKey().getObjectType().equals(DeployType.WORKFLOW))
                    .map(item -> {
                        try {
                            return JUpdateItemOperation.addOrChangeSigned(getSignedStringWithSignerDN(JsonSerializer.serializeAsString(item.getKey()
                                    .getContent()), item.getValue().getSignature(), signatureAlgorithm, signerDN));
                        } catch (JsonProcessingException e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
            // job resources
            updateItemsOperationsSigned.addAll(drafts.keySet().stream().filter(item -> item.getObjectType().equals(DeployType.JOBRESOURCE)).map(
                    item -> {
                        try {
                            return JUpdateItemOperation.addOrChangeSigned(getSignedStringWithSignerDN(JsonSerializer.serializeAsString(item
                                    .getContent()), drafts.get(item).getSignature(), signatureAlgorithm, signerDN));
                        } catch (JsonProcessingException e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
            // locks
            updateItemsOperationsSimple.addAll(drafts.keySet().stream().filter(item -> item.getObjectType().equals(DeployType.LOCK)).map(item -> {
                try {
                    Lock lock = (Lock) item.getContent();
                    lock.setPath(Paths.get(item.getPath()).getFileName().toString());
                    return JUpdateItemOperation.addOrChangeSimple(getJLock(lock));
                } catch (Exception e) {
                    throw new JocDeployException(e);
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet()));
            // file order sources
            updateItemsOperationsSimple.addAll(drafts.keySet().stream().filter(item -> item.getObjectType().equals(DeployType.FILEORDERSOURCE)).map(
                    item -> {
                        try {
                            FileOrderSource fileOrderSource = (FileOrderSource) item.getContent();
                            if (fileOrderSource.getPath() == null) {
                                fileOrderSource.setPath(Paths.get(item.getPath()).getFileName().toString());
                            }
                            return JUpdateItemOperation.addOrChangeSimple(getJFileWatch(fileOrderSource));
                        } catch (JocDeployException e) {
                            throw e;
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).filter(Objects::nonNull).collect(Collectors.toSet()));
            // Board
            updateItemsOperationsSimple.addAll(drafts.keySet().stream().filter(item -> item.getObjectType().equals(DeployType.NOTICEBOARD)).map(item -> {
                try {
                    Board board = (Board) item.getContent();
                    if (board.getPath() == null) {
                        board.setPath(Paths.get(item.getPath()).getFileName().toString());
                    }
                    return JUpdateItemOperation.addOrChangeSimple(getJBoard(board));
                } catch (JocDeployException e) {
                    throw e;
                } catch (Exception e) {
                    throw new JocDeployException(e);
                }
            }).filter(Objects::nonNull).collect(Collectors.toSet()));
            // job classes
            // TODO: when implemented in controller
        }
        return ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.fromIterable(updateItemsOperationsSimple), Flux.just(JUpdateItemOperation
                .addVersion(VersionId.of(commitId))), Flux.fromIterable(updateItemsOperationsSigned)));
    }

    public static CompletableFuture<Either<Problem, Void>> updateItemsDelete(String commitId, List<DBItemDeploymentHistory> alreadyDeployedtoDelete,
            String controllerId) {
        // keyAlgorithm obsolete
        Set<JUpdateItemOperation> updateItemOperationsSigned = new HashSet<JUpdateItemOperation>();
        Set<JUpdateItemOperation> updateItemOperationsSimple = new HashSet<JUpdateItemOperation>();
        if (alreadyDeployedtoDelete != null) {
            updateItemOperationsSigned.addAll(alreadyDeployedtoDelete.stream().filter(item -> item.getType() == DeployType.WORKFLOW.intValue()).map(
                    item -> JUpdateItemOperation.deleteVersioned(WorkflowPath.of(item.getName()))).filter(Objects::nonNull).collect(Collectors
                            .toSet()));
            updateItemOperationsSigned.addAll(alreadyDeployedtoDelete.stream().filter(item -> item.getType() == DeployType.JOBRESOURCE.intValue())
                    .map(item -> JUpdateItemOperation.deleteVersioned(WorkflowPath.of(item.getName()))).filter(Objects::nonNull).collect(Collectors
                            .toSet()));
            updateItemOperationsSimple.addAll(alreadyDeployedtoDelete.stream().filter(item -> item.getType() == DeployType.LOCK.intValue()).map(
                    item -> {
                        try {
                            Lock lock = Globals.objectMapper.readValue(item.getContent(), Lock.class);
                            lock.setPath(Paths.get(item.getPath()).getFileName().toString());
                            return JUpdateItemOperation.deleteSimple(LockPath.of(lock.getPath()));
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
            updateItemOperationsSimple.addAll(alreadyDeployedtoDelete.stream().filter(item -> item.getType() == DeployType.FILEORDERSOURCE.intValue())
                    .map(item -> {
                        try {
                            FileOrderSource fileOrderSource = Globals.objectMapper.readValue(item.getContent(), FileOrderSource.class);
                            fileOrderSource.setPath(Paths.get(item.getPath()).getFileName().toString());
                            return JUpdateItemOperation.deleteSimple(OrderWatchPath.of(fileOrderSource.getPath()));
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
            updateItemOperationsSimple.addAll(alreadyDeployedtoDelete.stream().filter(item -> item.getType() == DeployType.NOTICEBOARD.intValue()).map(
                    item -> {
                        try {
                            Board board = Globals.objectMapper.readValue(item.getContent(), Board.class);
                            board.setPath(Paths.get(item.getPath()).getFileName().toString());
                            return JUpdateItemOperation.deleteSimple(BoardPath.of(board.getPath()));
                        } catch (Exception e) {
                            throw new JocDeployException(e);
                        }
                    }).collect(Collectors.toSet()));
        }
        return ControllerApi.of(controllerId).updateItems(Flux.concat(Flux.fromIterable(updateItemOperationsSimple), Flux.just(JUpdateItemOperation
                .addVersion(VersionId.of(commitId))), Flux.fromIterable(updateItemOperationsSigned)));
    }

    private static void updateVersionId(DBItemDeploymentHistory draft, String commitId) {
        if (ConfigurationType.WORKFLOW.intValue() == draft.getType()) {
            ((Workflow) draft.readUpdateableContent()).setVersionId(commitId);
        }
    }

    public static Set<UpdateableWorkflowJobAgentName> getUpdateableAgentRefInWorkflowJobs(DBItemDeploymentHistory item, String controllerId,
            DBLayerDeploy dbLayer) {
        return getUpdateableAgentRefInWorkflowJobs(item.getPath(), item.readUpdateableContent(), item.getType(), controllerId, dbLayer);
    }

    public static Set<UpdateableWorkflowJobAgentName> getUpdateableAgentRefInWorkflowJobs(String path, IDeployObject deployObject, Integer type,
            String controllerId, DBLayerDeploy dbLayer) {
        Set<UpdateableWorkflowJobAgentName> update = new HashSet<UpdateableWorkflowJobAgentName>();
        if (ConfigurationType.WORKFLOW.intValue() == type) {
            Workflow workflow = (Workflow) deployObject;
            workflow.getJobs().getAdditionalProperties().keySet().stream().forEach(jobname -> {
                Job job = workflow.getJobs().getAdditionalProperties().get(jobname);
                String agentNameOrAlias = job.getAgentPath();
                String agentId = dbLayer.getAgentIdFromAgentName(agentNameOrAlias, controllerId, path, jobname);
                update.add(new UpdateableWorkflowJobAgentName(path, jobname, job.getAgentPath(), agentId, controllerId));
            });
        }
        return update;
    }

    public static UpdateableFileOrderSourceAgentName getUpdateableAgentRefInFileOrderSource(DBItemDeploymentHistory item, String controllerId,
            DBLayerDeploy dbLayer) {
        return getUpdateableAgentRefInFileOrderSource(item.getName(), item.readUpdateableContent(), controllerId, dbLayer);
    }

    public static UpdateableFileOrderSourceAgentName getUpdateableAgentRefInFileOrderSource(String fileOrderSourceId, IDeployObject deployObject,
            String controllerId, DBLayerDeploy dbLayer) {
        UpdateableFileOrderSourceAgentName update = null;
        FileOrderSource fileOrderSource = (FileOrderSource) deployObject;
        String agentNameOrAlias = fileOrderSource.getAgentPath();
        String agentId = dbLayer.getAgentIdFromAgentName(agentNameOrAlias, controllerId);
        update = new UpdateableFileOrderSourceAgentName(fileOrderSourceId, agentNameOrAlias, agentId, controllerId);
        return update;
    }

    public static Set<DBItemDeploymentHistory> cloneInvConfigurationsToDepHistoryItems(Map<ControllerObject, DBItemDepSignatures> draftsWithSignature,
            String account, DBLayerDeploy dbLayerDeploy, String commitId, String controllerId, Date deploymentDate, Long auditlogId)
            throws JsonParseException, JsonMappingException, IOException {
        Set<DBItemDeploymentHistory> deployedObjects;
        try {
            DBItemInventoryJSInstance controllerInstance = dbLayerDeploy.getController(controllerId);
            deployedObjects = new HashSet<DBItemDeploymentHistory>();
            for (ControllerObject draft : draftsWithSignature.keySet()) {
                DBItemDeploymentHistory newDeployedObject = new DBItemDeploymentHistory();
                newDeployedObject.setAccount(account);
                // TODO: get Version to set here
                newDeployedObject.setVersion(null);
                newDeployedObject.setType(draft.getObjectType().intValue());
                newDeployedObject.setCommitId(commitId);
                newDeployedObject.setAuditlogId(auditlogId);
                DBItemInventoryConfiguration original = null;
                switch (draft.getObjectType()) {
                case WORKFLOW:
                    String workflow = Globals.objectMapper.writeValueAsString(((WorkflowPublish) draft).getContent());
                    newDeployedObject.setContent(workflow);
                    if (draft.getPath() != null) {
                        original = dbLayerDeploy.getConfigurationByPath(draft.getPath(), ConfigurationType.WORKFLOW.intValue());
                    } else {
                        original = dbLayerDeploy.getConfigurationByPath(((WorkflowPublish) draft).getContent().getPath(), ConfigurationType.WORKFLOW
                                .intValue());
                    }
                    if (original.getPath() != null && !original.getPath().isEmpty()) {
                        newDeployedObject.setPath(original.getPath());
                    }
                    if (original.getName() != null && !original.getName().isEmpty()) {
                        newDeployedObject.setName(original.getName());
                    } else {
                        newDeployedObject.setName(Paths.get(original.getPath()).getFileName().toString());
                    }
                    newDeployedObject.setFolder(original.getFolder());
                    newDeployedObject.setInvContent(original.getContent());
                    newDeployedObject.setInventoryConfigurationId(original.getId());
                    break;
                case JOBRESOURCE:
                    String jobResource = Globals.objectMapper.writeValueAsString(((JobResourcePublish) draft).getContent());
                    newDeployedObject.setContent(jobResource);
                    if (draft.getPath() != null) {
                        original = dbLayerDeploy.getConfigurationByPath(draft.getPath(), ConfigurationType.JOBRESOURCE.intValue());
                    }
                    newDeployedObject.setPath(original.getPath());
                    if (original.getName() != null && !original.getName().isEmpty()) {
                        newDeployedObject.setName(original.getName());
                    } else {
                        newDeployedObject.setName(Paths.get(original.getPath()).getFileName().toString());
                    }
                    newDeployedObject.setFolder(original.getFolder());
                    newDeployedObject.setInvContent(original.getContent());
                    newDeployedObject.setInventoryConfigurationId(original.getId());
                    break;
                case LOCK:
                    String lock = Globals.objectMapper.writeValueAsString(((LockPublish) draft).getContent());
                    newDeployedObject.setContent(lock);
                    if (draft.getPath() != null) {
                        original = dbLayerDeploy.getConfigurationByPath(draft.getPath(), ConfigurationType.LOCK.intValue());
                    }
                    newDeployedObject.setPath(original.getPath());
                    if (original.getName() != null && !original.getName().isEmpty()) {
                        newDeployedObject.setName(original.getName());
                    } else {
                        newDeployedObject.setName(Paths.get(original.getPath()).getFileName().toString());
                    }
                    newDeployedObject.setFolder(original.getFolder());
                    newDeployedObject.setInvContent(original.getContent());
                    newDeployedObject.setInventoryConfigurationId(original.getId());
                    break;
                case FILEORDERSOURCE:
                    String fileOrderSource = Globals.objectMapper.writeValueAsString(((FileOrderSourcePublish) draft).getContent());
                    newDeployedObject.setContent(fileOrderSource);
                    if (draft.getPath() != null) {
                        original = dbLayerDeploy.getConfigurationByPath(draft.getPath(), ConfigurationType.FILEORDERSOURCE.intValue());
                    }
                    newDeployedObject.setPath(original.getPath());
                    if (original.getName() != null && !original.getName().isEmpty()) {
                        newDeployedObject.setName(original.getName());
                    } else {
                        newDeployedObject.setName(Paths.get(original.getPath()).getFileName().toString());
                    }
                    newDeployedObject.setFolder(original.getFolder());
                    newDeployedObject.setInvContent(original.getContent());
                    newDeployedObject.setInventoryConfigurationId(original.getId());
                    break;
                case NOTICEBOARD:
                    String board = Globals.objectMapper.writeValueAsString(((BoardPublish) draft).getContent());
                    newDeployedObject.setContent(board);
                    if (draft.getPath() != null) {
                        original = dbLayerDeploy.getConfigurationByPath(draft.getPath(), ConfigurationType.NOTICEBOARD.intValue());
                    } else {
                        original = dbLayerDeploy.getConfigurationByPath(((BoardPublish) draft).getContent().getPath(),
                                ConfigurationType.NOTICEBOARD.intValue());
                    }
                    newDeployedObject.setPath(original.getPath());
                    if (original.getName() != null && !original.getName().isEmpty()) {
                        newDeployedObject.setName(original.getName());
                    } else {
                        newDeployedObject.setName(Paths.get(original.getPath()).getFileName().toString());
                    }
                    newDeployedObject.setFolder(original.getFolder());
                    newDeployedObject.setInvContent(original.getContent());
                    newDeployedObject.setInventoryConfigurationId(original.getId());
                    break;
                case JOBCLASS:
                    String jobclass = Globals.objectMapper.writeValueAsString(((JobClassPublish) draft).getContent());
                    newDeployedObject.setContent(jobclass);
                    if (draft.getPath() != null) {
                        original = dbLayerDeploy.getConfigurationByPath(draft.getPath(), ConfigurationType.JOBCLASS.intValue());
                    } else {
                        original = dbLayerDeploy.getConfigurationByPath(((JobClassPublish) draft).getContent().getPath(), ConfigurationType.JOBCLASS
                                .intValue());
                    }
                    newDeployedObject.setPath(original.getPath());
                    if (original.getName() != null && !original.getName().isEmpty()) {
                        newDeployedObject.setName(original.getName());
                    } else {
                        newDeployedObject.setName(Paths.get(original.getPath()).getFileName().toString());
                    }
                    newDeployedObject.setFolder(original.getFolder());
                    newDeployedObject.setInvContent(original.getContent());
                    newDeployedObject.setInventoryConfigurationId(original.getId());
                    break;
                }
                newDeployedObject.setSignedContent(draftsWithSignature.get(draft).getSignature());
                if (newDeployedObject.getSignedContent() == null || newDeployedObject.getSignedContent().isEmpty()) {
                    newDeployedObject.setSignedContent(".");
                }
                newDeployedObject.setDeploymentDate(deploymentDate);
                newDeployedObject.setControllerInstanceId(controllerInstance.getId());
                newDeployedObject.setControllerId(controllerId);
                newDeployedObject.setOperation(OperationType.UPDATE.value());
                newDeployedObject.setState(DeploymentState.DEPLOYED.value());
                dbLayerDeploy.getSession().save(newDeployedObject);
                postDeployHistoryEvent(newDeployedObject);
                DBItemDepSignatures signature = draftsWithSignature.get(draft);
                if (signature != null) {
                    signature.setDepHistoryId(newDeployedObject.getId());
                    dbLayerDeploy.getSession().update(signature);
                }
                deployedObjects.add(newDeployedObject);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return deployedObjects;
    }

    public static DBItemDeploymentHistory cloneDepHistoryItemsToNewEntry(DBItemDeploymentHistory depHistoryItem, DBItemDepSignatures depSignatureItem,
            String account, DBLayerDeploy dbLayerDeploy, String commitId, String controllerId, Date deploymentDate, Long auditlogId) {
        try {
            if (depHistoryItem.getId() != null) {
                if (depSignatureItem != null) {
                    // signed item
                    depHistoryItem.setSignedContent(depSignatureItem.getSignature());
                }

                // Metode tries to change commitId and ControllerId but it can face into Constraint Violation
                boolean constraintViolation = new EqualsBuilder().append(commitId, depHistoryItem.getCommitId()).append(controllerId, depHistoryItem
                        .getControllerId()).isEquals();

                if (!constraintViolation) {
                    depHistoryItem.setId(null);
                }
                depHistoryItem.setAccount(account);
                // TODO: get Version to set here
                depHistoryItem.setVersion(null);
                depHistoryItem.setContent(JsonSerializer.serializeAsString(depHistoryItem.readUpdateableContent()));
                depHistoryItem.setCommitId(commitId);
                depHistoryItem.setControllerId(controllerId);
                DBItemInventoryJSInstance controllerInstance = dbLayerDeploy.getController(controllerId); // TODO obsolete or not?
                depHistoryItem.setControllerInstanceId(controllerInstance.getId());
                depHistoryItem.setDeploymentDate(deploymentDate);
                depHistoryItem.setOperation(OperationType.UPDATE.value());
                depHistoryItem.setState(DeploymentState.DEPLOYED.value());
                depHistoryItem.setAuditlogId(auditlogId);
                if(depHistoryItem.getSignedContent() == null || depHistoryItem.getSignedContent().isEmpty()) {
                    depHistoryItem.setSignedContent(".");
                }
                if (!constraintViolation) {
                    dbLayerDeploy.getSession().save(depHistoryItem);
                } else {
                    dbLayerDeploy.getSession().update(depHistoryItem);
                }
                postDeployHistoryEvent(depHistoryItem);
                if (depSignatureItem != null) {
                    depSignatureItem.setDepHistoryId(depHistoryItem.getId());
                    dbLayerDeploy.getSession().update(depSignatureItem);
                }
            }
        } catch (IOException e) {
            throw new JocException(e);
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return depHistoryItem;
    }

    public static Set<DBItemDeploymentHistory> cloneDepHistoryItemsToNewEntries(
            Map<DBItemDeploymentHistory, DBItemDepSignatures> deployedWithSignature, String account, DBLayerDeploy dbLayerDeploy, String commitId,
            String controllerId, Date deploymentDate, Long auditlogId) {
        Set<DBItemDeploymentHistory> deployedObjects = new HashSet<DBItemDeploymentHistory>();
        for (DBItemDeploymentHistory deployed : deployedWithSignature.keySet()) {
            if (deployed.getId() != null) {
                DBItemDepSignatures signature = deployedWithSignature.get(deployed);
                deployedObjects.add(cloneDepHistoryItemsToNewEntry(deployed, signature, account, dbLayerDeploy, commitId, controllerId,
                        deploymentDate, auditlogId));
            }
        }
        return deployedObjects;
    }

    public static Set<DBItemDeploymentHistory> cloneDepHistoryItemsToRedeployed(List<DBItemDeploymentHistory> redeployedItems, String account,
            DBLayerDeploy dbLayerDeploy, String controllerId, Date deploymentDate) {
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
                if (redeployed.getSignedContent() == null || redeployed.getSignedContent().isEmpty()) {
                    redeployed.setSignedContent(".");
                }
                dbLayerDeploy.getSession().save(redeployed);
                postDeployHistoryEvent(redeployed);
                deployedObjects.add(redeployed);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return deployedObjects;
    }

    public static Set<DBItemDeploymentHistory> updateDeletedDepHistory(List<DBItemDeploymentHistory> toDelete, DBLayerDeploy dbLayer, String commitId,
            boolean withTrash) {
        Set<DBItemDeploymentHistory> deletedObjects = new HashSet<DBItemDeploymentHistory>();
        InventoryDBLayer invDBLayer = new InventoryDBLayer(dbLayer.getSession());
        try {
            if (toDelete != null) {
                for (DBItemDeploymentHistory delete : toDelete) {
                    delete.setId(null);
                    delete.setCommitId(commitId);
                    delete.setOperation(OperationType.DELETE.value());
                    delete.setState(DeploymentState.DEPLOYED.value());
                    delete.setDeleteDate(Date.from(Instant.now()));
                    delete.setDeploymentDate(Date.from(Instant.now()));
                    if (delete.getSignedContent() == null || delete.getSignedContent().isEmpty()) {
                        delete.setSignedContent(".");
                    }
                    dbLayer.getSession().save(delete);
                    deletedObjects.add(delete);
                    if (withTrash) {
                        DBItemInventoryConfiguration orig = dbLayer.getInventoryConfigurationByNameAndType(delete.getName(), delete.getType());
                        if (orig != null) {
                            JocInventory.deleteInventoryConfigurationAndPutToTrash(orig, invDBLayer);
                        }
                    }
                }
            }

        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return deletedObjects;
    }

    public static Set<DBItemDeploymentHistory> updateDeletedDepHistoryAndPutToTrash(List<DBItemDeploymentHistory> toDelete, DBLayerDeploy dbLayer,
            String commitId) {
        return updateDeletedDepHistory(toDelete, dbLayer, commitId, true);
    }

    public static void prepareNextInvConfigGeneration(final Set<DBItemInventoryConfiguration> drafts, SOSHibernateSession hibernateSession) {
        // don´t touch the drafts as it would result in TamperedWithSignedMessage Error
        try {
            for (DBItemInventoryConfiguration draft : drafts) {
                DBItemInventoryConfiguration toUpdate = hibernateSession.get(DBItemInventoryConfiguration.class, draft.getId());
                toUpdate.setDeployed(true);
                toUpdate.setModified(Date.from(Instant.now()));
                hibernateSession.update(toUpdate);
                JocInventory.postEvent(draft.getFolder());
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public static void prepareNextInvConfigGeneration(Set<ControllerObject> drafts, String controllerId, DBLayerDeploy dbLayer) {
        try {
            for (ControllerObject draft : drafts) {
                DBItemInventoryConfiguration configuration = dbLayer.getConfigurationByPath(draft.getPath(), ConfigurationType.fromValue(draft
                        .getObjectType().intValue()));
                configuration.setDeployed(true);
                configuration.setModified(Date.from(Instant.now()));
                dbLayer.getSession().update(configuration);
                JocInventory.postEvent(configuration.getFolder());
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public static <T extends DBItem> List<DBItemDeploymentHistory> checkRenamingForUpdate(Set<T> verifiedObjects, String controllerId,
            DBLayerDeploy dbLayer, String keyAlgorithm) throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        DBItemDeploymentHistory depHistory = null;
        DBItemInventoryConfiguration invConf = null;
        final String versionId = UUID.randomUUID().toString();
        // check first if a deploymentHistory item related to the configuration item exist
        List<DBItemDeploymentHistory> alreadyDeployedToDelete = new ArrayList<DBItemDeploymentHistory>();
        DBItemDeploymentHistory latestDepHistory = null;
        for (T object : verifiedObjects) {
            if (DBItemInventoryConfiguration.class.isInstance(object)) {
                invConf = (DBItemInventoryConfiguration) object;
                depHistory = dbLayer.getLatestDepHistoryItem(invConf, controllerId);
                // if operation of latest history item was 'delete', no need to delete again
                if (depHistory != null && OperationType.DELETE.equals(OperationType.fromValue(depHistory.getOperation()))) {
                    depHistory = null;
                }
                // if so, check if the paths of both are the same
                if (depHistory != null && invConf != null && !depHistory.getName().equals(latestDepHistory.getName())) {
                    // if not, delete the old deployed item via updateRepo before deploy of the new configuration
                    depHistory.setCommitId(versionId);
                    alreadyDeployedToDelete.add(depHistory);
                }
            } else {
                depHistory = (DBItemDeploymentHistory) object;
                latestDepHistory = dbLayer.getLatestDepHistoryItem(depHistory.getInventoryConfigurationId(), controllerId);
                // if so, check if the paths of both are the same
                if (depHistory != null && latestDepHistory != null && latestDepHistory.getOperation() != 1 && !depHistory.getName().equals(latestDepHistory.getName())) {
                    // if not, delete the old deployed item via updateRepo before deploy of the new configuration
                    depHistory.setCommitId(versionId);
                    alreadyDeployedToDelete.add(latestDepHistory);
                }
            }
        }
        return alreadyDeployedToDelete;
    }

    public static Map<ControllerObject, SignaturePath> readZipFileContentWithSignatures(InputStream inputStream, JocMetaInfo jocMetaInfo)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException,
            JocConfigurationException, DBOpenSessionException {
        Set<ControllerObject> objects = new HashSet<ControllerObject>();
        Set<SignaturePath> signaturePaths = new HashSet<SignaturePath>();
        Map<ControllerObject, SignaturePath> objectsWithSignature = new HashMap<ControllerObject, SignaturePath>();
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
                // process JOC meta info file
                if (entryName.equals(JOC_META_INFO_FILENAME)) {
                    JocMetaInfo fromFile = Globals.prettyPrintObjectMapper.readValue(outBuffer.toString(), JocMetaInfo.class);
                    if (!isJocMetaInfoNullOrEmpty(fromFile)) {
                        jocMetaInfo.setJocVersion(fromFile.getJocVersion());
                        jocMetaInfo.setInventorySchemaVersion(fromFile.getInventorySchemaVersion());
                        jocMetaInfo.setApiVersion(fromFile.getApiVersion());
                    }
                }
                // process deployables only
                ControllerObject fromArchive = createControllerObjectFromArchiveFileEntry(outBuffer, entryName);
                if (fromArchive != null) {
                    objects.add(fromArchive);
                }
                SignaturePath signaturePath = createSignatureFromArchiveFileEntry(outBuffer, entryName);
                if (signaturePath != null) {
                    signaturePaths.add(signaturePath);
                }
            }
            objects.stream().forEach(item -> {
                objectsWithSignature.put(item, signaturePaths.stream().filter(item2 -> item2.getObjectPath().equals(item.getPath())).findFirst()
                        .get());
            });
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                }
            }
        }
        return objectsWithSignature;
    }

    public static Set<ConfigurationObject> readZipFileContent(InputStream inputStream, JocMetaInfo jocMetaInfo) throws DBConnectionRefusedException,
            DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException, JocConfigurationException,
            DBOpenSessionException {
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
                // process JOC meta info file
                if (entryName.equals(JOC_META_INFO_FILENAME)) {
                    JocMetaInfo fromFile = Globals.prettyPrintObjectMapper.readValue(outBuffer.toString(), JocMetaInfo.class);
                    if (!isJocMetaInfoNullOrEmpty(fromFile)) {
                        jocMetaInfo.setJocVersion(fromFile.getJocVersion());
                        jocMetaInfo.setInventorySchemaVersion(fromFile.getInventorySchemaVersion());
                        jocMetaInfo.setApiVersion(fromFile.getApiVersion());
                    }
                }
                ConfigurationObject fromArchive = createConfigurationObjectFromArchiveFileEntry(outBuffer, entryName);
                if (fromArchive != null) {
                    objects.add(fromArchive);
                }
            }
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {
                }
            }
        }
        return objects;
    }

    public static Map<ControllerObject, SignaturePath> readTarGzipFileContentWithSignatures(InputStream inputStream, JocMetaInfo jocMetaInfo)
            throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException, IOException, JocUnsupportedFileTypeException,
            JocConfigurationException, DBOpenSessionException {
        Set<ControllerObject> objects = new HashSet<ControllerObject>();
        Set<SignaturePath> signaturePaths = new HashSet<SignaturePath>();
        Map<ControllerObject, SignaturePath> objectsWithSignature = new HashMap<ControllerObject, SignaturePath>();
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
                // process JOC meta info file
                if (entryName.equals(JOC_META_INFO_FILENAME)) {
                    JocMetaInfo fromFile = Globals.prettyPrintObjectMapper.readValue(outBuffer.toString(), JocMetaInfo.class);
                    if (!isJocMetaInfoNullOrEmpty(fromFile)) {
                        jocMetaInfo.setJocVersion(fromFile.getJocVersion());
                        jocMetaInfo.setInventorySchemaVersion(fromFile.getInventorySchemaVersion());
                        jocMetaInfo.setApiVersion(fromFile.getApiVersion());
                    }
                }
                // process deployables only
                ControllerObject fromArchive = createControllerObjectFromArchiveFileEntry(outBuffer, entryName);
                if (fromArchive != null) {
                    objects.add(fromArchive);
                }
                SignaturePath signaturePath = createSignatureFromArchiveFileEntry(outBuffer, entryName);
                if (signaturePath != null) {
                    signaturePaths.add(signaturePath);
                }
            }
            objects.stream().forEach(item -> {
                objectsWithSignature.put(item, signaturePaths.stream().filter(item2 -> item2.getObjectPath().equals(item.getPath())).findFirst()
                        .get());
            });
        } finally {
            try {
                if (tarArchiveInputStream != null) {
                    tarArchiveInputStream.close();
                }
                if (gzipInputStream != null) {
                    gzipInputStream.close();
                }
            } catch (Exception e) {
            }
        }
        return objectsWithSignature;
    }

    public static Set<ConfigurationObject> readTarGzipFileContent(InputStream inputStream, JocMetaInfo jocMetaInfo)
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
                // process JOC meta info file
                if (entryName.equals(JOC_META_INFO_FILENAME)) {
                    JocMetaInfo fromFile = Globals.prettyPrintObjectMapper.readValue(outBuffer.toString(), JocMetaInfo.class);
                    if (!isJocMetaInfoNullOrEmpty(fromFile)) {
                        jocMetaInfo.setJocVersion(fromFile.getJocVersion());
                        jocMetaInfo.setInventorySchemaVersion(fromFile.getInventorySchemaVersion());
                        jocMetaInfo.setApiVersion(fromFile.getApiVersion());
                    }
                }
                ConfigurationObject fromArchive = createConfigurationObjectFromArchiveFileEntry(outBuffer, entryName);
                if (fromArchive != null) {
                    objects.add(fromArchive);
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
            } catch (Exception e) {
            }
        }
        return objects;
    }

    private static ControllerObject createControllerObjectFromArchiveFileEntry(ByteArrayOutputStream outBuffer, String entryName)
            throws JsonParseException, JsonMappingException, IOException {
        if (entryName.endsWith(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
            WorkflowPublish workflowPublish = new WorkflowPublish();
            Workflow workflow = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()), Workflow.class);
            if (checkObjectNotEmpty(workflow)) {
                workflowPublish.setContent(workflow);
            } else {
                throw new JocImportException(String.format("Workflow with path %1$s not imported. Object values could not be mapped.", Globals
                        .normalizePath("/" + entryName.replace(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(), ""))));
            }
            workflowPublish.setPath(Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(),
                    "")));
            workflowPublish.setObjectType(DeployType.WORKFLOW);
            return workflowPublish;
        } else if (entryName.endsWith(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.value())) {
            JobResourcePublish jobResourcePublish = new JobResourcePublish();
            JobResource jobResource = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()), JobResource.class);
            if (checkObjectNotEmpty(jobResource)) {
                jobResourcePublish.setContent(jobResource);
            } else {
                throw new JocImportException(String.format("JobResource with path %1$s not imported. Object values could not be mapped.", Globals
                        .normalizePath("/" + entryName.replace(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.value(), ""))));
            }
            jobResourcePublish.setPath(Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.value(),
                    "")));
            jobResourcePublish.setObjectType(DeployType.JOBRESOURCE);
            return jobResourcePublish;
        } else if (entryName.endsWith(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.value())) {
            LockPublish lockPublish = new LockPublish();
            Lock lock = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()), Lock.class);
            if (checkObjectNotEmpty(lock)) {
                lockPublish.setContent(lock);
            } else {
                throw new JocImportException(String.format("Lock with path %1$s not imported. Object values could not be mapped.", Globals
                        .normalizePath("/" + entryName.replace(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.value(), ""))));
            }
            lockPublish.setPath(Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.value(), "")));
            lockPublish.setObjectType(DeployType.LOCK);
            return lockPublish;
        } else if (entryName.endsWith(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.value())) {
            BoardPublish boardPublish = new BoardPublish();
            Board board = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()), Board.class);
            if (checkObjectNotEmpty(board)) {
                boardPublish.setContent(board);
            } else {
                throw new JocImportException(String.format("Board with path %1$s not imported. Object values could not be mapped.", Globals
                        .normalizePath("/" + entryName.replace(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.value(), ""))));
            }
            boardPublish.setPath(Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.value(), "")));
            boardPublish.setObjectType(DeployType.NOTICEBOARD);
            return boardPublish;
        } else if (entryName.endsWith(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.value())) {
            JobClassPublish jobClassPublish = new JobClassPublish();
            JobClass jobClass = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()), JobClass.class);
            if (checkObjectNotEmpty(jobClass)) {
                jobClassPublish.setContent(jobClass);
            } else {
                throw new JocImportException(String.format("JobClass with path %1$s not imported. Object values could not be mapped.", Globals
                        .normalizePath("/" + entryName.replace(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(), ""))));
            }
            jobClassPublish.setPath(Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(),
                    "")));
            jobClassPublish.setObjectType(DeployType.JOBCLASS);
            return jobClassPublish;
        } else if (entryName.endsWith(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.value())) {
            FileOrderSourcePublish fileOrderSourcePublish = new FileOrderSourcePublish();
            FileOrderSource fileOrderSource = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()),
                    FileOrderSource.class);
            if (checkObjectNotEmpty(fileOrderSource)) {
                fileOrderSourcePublish.setContent(fileOrderSource);
            } else {
                throw new JocImportException(String.format("FileOrderSource with path %1$s not imported. Object values could not be mapped.", Globals
                        .normalizePath("/" + entryName.replace(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.value(), ""))));
            }
            fileOrderSourcePublish.setPath(Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION
                    .value(), "")));
            fileOrderSourcePublish.setObjectType(DeployType.FILEORDERSOURCE);
            return fileOrderSourcePublish;
        }
        return null;
    }

    private static SignaturePath createSignatureFromArchiveFileEntry(ByteArrayOutputStream outBuffer, String entryName) throws JsonParseException,
            JsonMappingException, IOException {
        SignaturePath signaturePath = new SignaturePath();
        Signature signature = new Signature();
        if (entryName.endsWith(ControllerObjectFileExtension.WORKFLOW_PGP_SIGNATURE_FILE_EXTENSION.value())) {
            signaturePath.setObjectPath(Globals.normalizePath("/" + entryName.replace(
                    ControllerObjectFileExtension.WORKFLOW_PGP_SIGNATURE_FILE_EXTENSION.value(), "")));
            signature.setSignatureString(outBuffer.toString());
            signaturePath.setSignature(signature);
            return signaturePath;
        } else if (entryName.endsWith(ControllerObjectFileExtension.WORKFLOW_X509_SIGNATURE_FILE_EXTENSION.value())) {
            signaturePath.setObjectPath(Globals.normalizePath("/" + entryName.replace(
                    ControllerObjectFileExtension.WORKFLOW_X509_SIGNATURE_FILE_EXTENSION.value(), "")));
            signature.setSignatureString(outBuffer.toString());
            signaturePath.setSignature(signature);
            return signaturePath;
        } else if (entryName.endsWith(ControllerObjectFileExtension.JOBRESOURCE_PGP_SIGNATURE_FILE_EXTENSION.value())) {
            signaturePath.setObjectPath(Globals.normalizePath("/" + entryName.replace(
                    ControllerObjectFileExtension.JOBRESOURCE_PGP_SIGNATURE_FILE_EXTENSION.value(), "")));
            signature.setSignatureString(outBuffer.toString());
            signaturePath.setSignature(signature);
            return signaturePath;
        } else if (entryName.endsWith(ControllerObjectFileExtension.JOBRESOURCE_X509_SIGNATURE_FILE_EXTENSION.value())) {
            signaturePath.setObjectPath(Globals.normalizePath("/" + entryName.replace(
                    ControllerObjectFileExtension.JOBRESOURCE_X509_SIGNATURE_FILE_EXTENSION.value(), "")));
            signature.setSignatureString(outBuffer.toString());
            signaturePath.setSignature(signature);
            return signaturePath;
        }
        return null;
    }

    private static ConfigurationObject createConfigurationObjectFromArchiveFileEntry(ByteArrayOutputStream outBuffer, String entryName)
            throws JsonParseException, JsonMappingException, IOException {
        // process deployables and releaseables
        if (entryName.endsWith(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.value(), ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            WorkflowEdit workflowEdit = new WorkflowEdit();
            com.sos.inventory.model.workflow.Workflow workflow = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()),
                    com.sos.inventory.model.workflow.Workflow.class);
            workflow = JsonSerializer.emptyValuesToNull(workflow);
            if (checkObjectNotEmpty(workflow)) {
                workflowEdit.setConfiguration(workflow);
            } else {
                throw new JocImportException(String.format("Workflow with path %1$s not imported. Object values could not be mapped.",
                        normalizedPath));
            }
            workflowEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            workflowEdit.setPath(normalizedPath);
            workflowEdit.setObjectType(ConfigurationType.WORKFLOW);
            return workflowEdit;
        } else if (entryName.endsWith(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.value(),
                    ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            JobResourceEdit jobResourceEdit = new JobResourceEdit();
            com.sos.inventory.model.jobresource.JobResource jobResource = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8
                    .displayName()), com.sos.inventory.model.jobresource.JobResource.class);
            if (checkObjectNotEmpty(jobResource)) {
                jobResourceEdit.setConfiguration(jobResource);
            } else {
                throw new JocImportException(String.format("JobResource with path %1$s not imported. Object values could not be mapped.",
                        normalizedPath));
            }
            jobResourceEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            jobResourceEdit.setPath(normalizedPath);
            jobResourceEdit.setObjectType(ConfigurationType.JOBRESOURCE);
            return jobResourceEdit;
        } else if (entryName.endsWith(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.LOCK_FILE_EXTENSION.value(), ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            LockEdit lockEdit = new LockEdit();
            com.sos.inventory.model.lock.Lock lock = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()),
                    com.sos.inventory.model.lock.Lock.class);
            if (checkObjectNotEmpty(lock)) {
                lockEdit.setConfiguration(lock);
            } else {
                throw new JocImportException(String.format("Lock with path %1$s not imported. Object values could not be mapped.", normalizedPath));
            }
            lockEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            lockEdit.setPath(normalizedPath);
            lockEdit.setObjectType(ConfigurationType.LOCK);
            return lockEdit;
        } else if (entryName.endsWith(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.value(), ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            BoardEdit boardEdit = new BoardEdit();
            com.sos.inventory.model.board.Board board = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()),
                    com.sos.inventory.model.board.Board.class);
            if (checkObjectNotEmpty(board)) {
                boardEdit.setConfiguration(board);
            } else {
                throw new JocImportException(String.format("Board with path %1$s not imported. Object values could not be mapped.", normalizedPath));
            }
            boardEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            boardEdit.setPath(normalizedPath);
            boardEdit.setObjectType(ConfigurationType.NOTICEBOARD);
            return boardEdit;
        } else if (entryName.endsWith(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.value(), ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            JobClassEdit jobClassEdit = new JobClassEdit();
            com.sos.inventory.model.jobclass.JobClass jobClass = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8
                    .displayName()), com.sos.inventory.model.jobclass.JobClass.class);
            if (checkObjectNotEmpty(jobClass)) {
                jobClassEdit.setConfiguration(jobClass);
            } else {
                throw new JocImportException(String.format("JobClass with path %1$s not imported. Object values could not be mapped.",
                        normalizedPath));
            }
            jobClassEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            jobClassEdit.setPath(normalizedPath);
            jobClassEdit.setObjectType(ConfigurationType.JOBCLASS);
            return jobClassEdit;
        } else if (entryName.endsWith(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION
                    .value(), ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            FileOrderSourceEdit fileOrderSourceEdit = new FileOrderSourceEdit();
            com.sos.inventory.model.fileordersource.FileOrderSource fileOrderSource = Globals.objectMapper.readValue(outBuffer.toString(
                    StandardCharsets.UTF_8.displayName()), com.sos.inventory.model.fileordersource.FileOrderSource.class);
            if (checkObjectNotEmpty(fileOrderSource)) {
                fileOrderSourceEdit.setConfiguration(fileOrderSource);
            } else {
                throw new JocImportException(String.format("FileOrderSource with path %1$s not imported. Object values could not be mapped.",
                        normalizedPath));
            }
            fileOrderSourceEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            fileOrderSourceEdit.setPath(normalizedPath);
            fileOrderSourceEdit.setObjectType(ConfigurationType.FILEORDERSOURCE);
            return fileOrderSourceEdit;
        } else if (entryName.endsWith(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ConfigurationObjectFileExtension.SCHEDULE_FILE_EXTENSION.value(),
                    ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            ScheduleEdit scheduleEdit = new ScheduleEdit();
            Schedule schedule = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()), Schedule.class);
            if (checkObjectNotEmpty(schedule)) {
                scheduleEdit.setConfiguration(schedule);
            } else {
                throw new JocImportException(String.format("Schedule with path %1$s not imported. Object values could not be mapped.",
                        normalizedPath));
            }
            scheduleEdit.setName(Paths.get(normalizedPath).getFileName().toString());
            scheduleEdit.setPath(normalizedPath);
            scheduleEdit.setObjectType(ConfigurationType.SCHEDULE);
            return scheduleEdit;
        } else if (entryName.endsWith(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value())) {
            String normalizedPath = Globals.normalizePath("/" + entryName.replace(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value(),
                    ""));
            if (normalizedPath.startsWith("//")) {
                normalizedPath = normalizedPath.substring(1);
            }
            Calendar cal = Globals.objectMapper.readValue(outBuffer.toString(StandardCharsets.UTF_8.displayName()), Calendar.class);
            if (checkObjectNotEmpty(cal)) {
                if (CalendarType.WORKINGDAYSCALENDAR.equals(cal.getType())) {
                    WorkingDaysCalendarEdit wdcEdit = new WorkingDaysCalendarEdit();
                    wdcEdit.setConfiguration(cal);
                    wdcEdit.setName(Paths.get(normalizedPath).getFileName().toString());
                    wdcEdit.setPath(normalizedPath);
                    wdcEdit.setObjectType(ConfigurationType.WORKINGDAYSCALENDAR);
                    return wdcEdit;
                } else if (CalendarType.WORKINGDAYSCALENDAR.equals(cal.getType())) {
                    NonWorkingDaysCalendarEdit nwdcEdit = new NonWorkingDaysCalendarEdit();
                    nwdcEdit.setConfiguration(cal);
                    nwdcEdit.setName(Paths.get(normalizedPath).getFileName().toString());
                    nwdcEdit.setPath(normalizedPath);
                    nwdcEdit.setObjectType(ConfigurationType.NONWORKINGDAYSCALENDAR);
                    return nwdcEdit;
                }
            } else {
                throw new JocImportException(String.format("Calendar with path %1$s not imported. Object values could not be mapped.", ("/"
                        + entryName).replace(ConfigurationObjectFileExtension.CALENDAR_FILE_EXTENSION.value(), "")));
            }
        }
        return null;
    }

    public static StreamingOutput writeZipFileForSigning(Set<ControllerObject> deployables, Set<ConfigurationObject> releasables,
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames, Set<UpdateableFileOrderSourceAgentName> updateableFOSAgentNames,
            String commitId, String controllerId, DBLayerDeploy dbLayer, Version jocVersion, Version apiVersion, Version inventoryVersion) {
        StreamingOutput streamingOutput = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException {
                ZipOutputStream zipOut = null;
                try {
                    zipOut = new ZipOutputStream(new BufferedOutputStream(output), StandardCharsets.UTF_8);
                    byte[] contentBytes = null;
                    if (deployables != null && !deployables.isEmpty()) {
                        for (ControllerObject deployable : deployables) {
                            String extension = null;
                            switch (deployable.getObjectType()) {
                            case WORKFLOW:
                                extension = ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                                Workflow workflow = (Workflow) deployable.getContent();
                                workflow.setVersionId(commitId);
                                // determine agent names to be replaced
                                if (controllerId != null && updateableAgentNames != null) {
                                    replaceAgentNameWithAgentId(workflow, updateableAgentNames, controllerId);
                                }
                                workflow.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(workflow);
                                break;
                            case JOBRESOURCE:
                                extension = ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.toString();
                                JobResource jobResource = (JobResource) deployable.getContent();
                                jobResource.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(jobResource);
                                break;
                            case LOCK:
                                extension = ControllerObjectFileExtension.LOCK_FILE_EXTENSION.toString();
                                Lock lock = (Lock) deployable.getContent();
                                lock.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(lock);
                                break;
                            case NOTICEBOARD:
                                extension = ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.toString();
                                Board board = (Board) deployable.getContent();
                                board.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(board);
                                break;
                            case JOBCLASS:
                                extension = ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString();
                                JobClass jobClass = (JobClass) deployable.getContent();
                                contentBytes = JsonSerializer.serializeAsBytes(jobClass);
                                break;
                            case FILEORDERSOURCE:
                                extension = ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString();
                                FileOrderSource fileOrderSource = (FileOrderSource) deployable.getContent();
                                // determine agent names to be replaced
                                if (controllerId != null && updateableAgentNames != null) {
                                    replaceAgentNameWithAgentId(fileOrderSource, updateableFOSAgentNames, controllerId);
                                }
                                contentBytes = JsonSerializer.serializeAsBytes(fileOrderSource);
                                break;
                            }
                            String zipEntryName = deployable.getPath().substring(1).concat(extension);
                            ZipEntry entry = new ZipEntry(zipEntryName);
                            zipOut.putNextEntry(entry);
                            zipOut.write(contentBytes);
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
                                contentBytes = Globals.objectMapper.writeValueAsBytes(releasable.getConfiguration());
                                String zipEntryName = releasable.getPath().substring(1).concat(extension);
                                ZipEntry entry = new ZipEntry(zipEntryName);
                                zipOut.putNextEntry(entry);
                                zipOut.write(contentBytes);
                                zipOut.closeEntry();
                            }
                        }
                    }
                    JocMetaInfo jocMetaInfo = createJocMetaInfo(jocVersion, apiVersion, inventoryVersion);
                    if (!isJocMetaInfoNullOrEmpty(jocMetaInfo)) {
                        String zipEntryName = JOC_META_INFO_FILENAME;
                        ZipEntry entry = new ZipEntry(zipEntryName);
                        zipOut.putNextEntry(entry);
                        zipOut.write(Globals.prettyPrintObjectMapper.writeValueAsBytes(jocMetaInfo));
                        zipOut.closeEntry();
                    }
                    zipOut.flush();
                } finally {
                    if (zipOut != null) {
                        try {
                            zipOut.close();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        };
        return streamingOutput;
    }

    public static StreamingOutput writeZipFileShallow(Set<ConfigurationObject> deployables, Set<ConfigurationObject> releasables,
            DBLayerDeploy dbLayer, Version jocVersion, Version apiVersion, Version inventoryVersion) {
        StreamingOutput streamingOutput = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException {
                ZipOutputStream zipOut = null;
                try {
                    zipOut = new ZipOutputStream(new BufferedOutputStream(output), StandardCharsets.UTF_8);
                    String content = null;
                    if (deployables != null && !deployables.isEmpty()) {
                        for (ConfigurationObject deployable : deployables) {
                            String extension = null;
                            switch (deployable.getObjectType()) {
                            case WORKFLOW:
                                extension = ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                                break;
                            case JOBRESOURCE:
                                extension = ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.toString();
                                break;
                            case LOCK:
                                extension = ControllerObjectFileExtension.LOCK_FILE_EXTENSION.toString();
                                break;
                            case NOTICEBOARD:
                                extension = ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.toString();
                                break;
                            case JOBCLASS:
                                extension = ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString();
                                break;
                            case FILEORDERSOURCE:
                                extension = ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString();
                                break;
                            default:
                                break;
                            }
                            if (extension != null) {
                                content = Globals.prettyPrintObjectMapper.writeValueAsString(deployable.getConfiguration());
                                String zipEntryName = deployable.getPath().substring(1).concat(extension);
                                ZipEntry entry = new ZipEntry(zipEntryName);
                                zipOut.putNextEntry(entry);
                                zipOut.write(content.getBytes());
                                zipOut.closeEntry();
                            }
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
                                content = Globals.prettyPrintObjectMapper.writeValueAsString(releasable.getConfiguration());
                                String zipEntryName = releasable.getPath().substring(1).concat(extension);
                                ZipEntry entry = new ZipEntry(zipEntryName);
                                zipOut.putNextEntry(entry);
                                zipOut.write(content.getBytes());
                                zipOut.closeEntry();
                            }
                        }
                    }
                    JocMetaInfo jocMetaInfo = createJocMetaInfo(jocVersion, apiVersion, inventoryVersion);
                    if (!isJocMetaInfoNullOrEmpty(jocMetaInfo)) {
                        String zipEntryName = JOC_META_INFO_FILENAME;
                        ZipEntry entry = new ZipEntry(zipEntryName);
                        zipOut.putNextEntry(entry);
                        zipOut.write(Globals.prettyPrintObjectMapper.writeValueAsBytes(jocMetaInfo));
                        zipOut.closeEntry();
                    }
                    zipOut.flush();
                } finally {
                    if (zipOut != null) {
                        try {
                            zipOut.close();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        };
        return streamingOutput;
    }

    public static StreamingOutput writeTarGzipFileForSigning(Set<ControllerObject> deployables, Set<ConfigurationObject> releasables,
            Set<UpdateableWorkflowJobAgentName> updateableAgentNames, Set<UpdateableFileOrderSourceAgentName> updateableFOSAgentNames,
            String commitId, String controllerId, DBLayerDeploy dbLayer, Version jocVersion, Version apiVersion, Version inventoryVersion) {
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
                    byte[] contentBytes = null;
                    if (deployables != null && !deployables.isEmpty()) {
                        for (ControllerObject deployable : deployables) {
                            String extension = null;
                            switch (deployable.getObjectType()) {
                            case WORKFLOW:
                                extension = ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                                Workflow workflow = (Workflow) deployable.getContent();
                                workflow.setVersionId(commitId);
                                if (controllerId != null && updateableAgentNames != null) {
                                    replaceAgentNameWithAgentId(workflow, updateableAgentNames, controllerId);
                                }
                                workflow.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                // workflow.setPath(deployable.getPath());
                                contentBytes = JsonSerializer.serializeAsBytes(workflow);
                                break;
                            case JOBRESOURCE:
                                extension = ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.toString();
                                JobResource jobResource = (JobResource) deployable.getContent();
                                jobResource.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(jobResource);
                                break;
                            case LOCK:
                                extension = ControllerObjectFileExtension.LOCK_FILE_EXTENSION.toString();
                                Lock lock = (Lock) deployable.getContent();
                                lock.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(lock);
                                break;
                            case NOTICEBOARD:
                                extension = ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.toString();
                                Board board = (Board) deployable.getContent();
                                board.setPath(Paths.get(deployable.getPath()).getFileName().toString());
                                contentBytes = JsonSerializer.serializeAsBytes(board);
                                break;
                            case JOBCLASS:
                                extension = ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString();
                                JobClass jobClass = (JobClass) deployable.getContent();
                                contentBytes = JsonSerializer.serializeAsBytes(jobClass);
                                break;
                            case FILEORDERSOURCE:
                                extension = ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString();
                                FileOrderSource fileOrderSource = (FileOrderSource) deployable.getContent();
                                // determine agent names to be replaced
                                if (controllerId != null && updateableAgentNames != null) {
                                    replaceAgentNameWithAgentId(fileOrderSource, updateableFOSAgentNames, controllerId);
                                }
                                contentBytes = JsonSerializer.serializeAsBytes(fileOrderSource);
                                break;
                            }
                            String zipEntryName = deployable.getPath().substring(1).concat(extension);
                            TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
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
                                contentBytes = Globals.objectMapper.writeValueAsBytes(releasable.getConfiguration());
                                String zipEntryName = releasable.getPath().substring(1).concat(extension);
                                TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
                                entry.setSize(contentBytes.length);
                                tarOut.putArchiveEntry(entry);
                                tarOut.write(contentBytes);
                                tarOut.closeArchiveEntry();
                            }
                        }
                    }
                    JocMetaInfo jocMetaInfo = createJocMetaInfo(jocVersion, apiVersion, inventoryVersion);
                    if (!isJocMetaInfoNullOrEmpty(jocMetaInfo)) {
                        String zipEntryName = JOC_META_INFO_FILENAME;
                        TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
                        byte[] jocMetaInfoBytes = Globals.prettyPrintObjectMapper.writeValueAsBytes(jocMetaInfo);
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
                        } catch (Exception e) {
                        }
                    }
                    if (gzipOut != null) {
                        try {
                            gzipOut.flush();
                            gzipOut.close();
                        } catch (Exception e) {
                        }
                    }
                    if (bOut != null) {
                        try {
                            bOut.flush();
                            bOut.close();
                        } catch (Exception e) {
                        }
                    }

                }

            }
        };
        return streamingOutput;
    }

    public static StreamingOutput writeTarGzipFileShallow(Set<ConfigurationObject> deployables, Set<ConfigurationObject> releasables,
            DBLayerDeploy dbLayer, Version jocVersion, Version apiVersion, Version inventoryVersion) {
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
                        for (ConfigurationObject deployable : deployables) {
                            String extension = null;
                            switch (deployable.getObjectType()) {
                            case WORKFLOW:
                                extension = ControllerObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                                break;
                            case JOBRESOURCE:
                                extension = ControllerObjectFileExtension.JOBRESOURCE_FILE_EXTENSION.toString();
                                break;
                            case LOCK:
                                extension = ControllerObjectFileExtension.LOCK_FILE_EXTENSION.toString();
                                break;
                            case NOTICEBOARD:
                                extension = ControllerObjectFileExtension.NOTICEBOARD_FILE_EXTENSION.toString();
                                break;
                            case JOBCLASS:
                                extension = ControllerObjectFileExtension.JOBCLASS_FILE_EXTENSION.toString();
                                break;
                            case FILEORDERSOURCE:
                                extension = ControllerObjectFileExtension.FILEORDERSOURCE_FILE_EXTENSION.toString();
                                break;
                            default:
                                break;
                            }
                            if (extension != null) {
                                content = Globals.prettyPrintObjectMapper.writeValueAsString(deployable.getConfiguration());
                                String zipEntryName = deployable.getPath().substring(1).concat(extension);
                                TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
                                byte[] contentBytes = content.getBytes();
                                entry.setSize(contentBytes.length);
                                tarOut.putArchiveEntry(entry);
                                tarOut.write(contentBytes);
                                tarOut.closeArchiveEntry();
                            }
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
                                content = Globals.prettyPrintObjectMapper.writeValueAsString(releasable.getConfiguration());
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
                    JocMetaInfo jocMetaInfo = createJocMetaInfo(jocVersion, apiVersion, inventoryVersion);
                    if (!isJocMetaInfoNullOrEmpty(jocMetaInfo)) {
                        String zipEntryName = JOC_META_INFO_FILENAME;
                        TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
                        byte[] jocMetaInfoBytes = Globals.prettyPrintObjectMapper.writeValueAsBytes(jocMetaInfo);
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
                        } catch (Exception e) {
                        }
                    }
                    if (gzipOut != null) {
                        try {
                            gzipOut.flush();
                            gzipOut.close();
                        } catch (Exception e) {
                        }
                    }
                    if (bOut != null) {
                        try {
                            bOut.flush();
                            bOut.close();
                        } catch (Exception e) {
                        }
                    }

                }

            }
        };
        return streamingOutput;
    }

    public static boolean jocKeyPairNotEmpty(JocKeyPair keyPair) {
        boolean checkNotEmpty = false;
        if (keyPair != null) {
            if (keyPair.getPrivateKey() != null && !keyPair.getPrivateKey().isEmpty()) {
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
                } else if ((keyPair.getPublicKey() != null && keyPair.getPublicKey().isEmpty()) && (keyPair.getCertificate() != null && keyPair
                        .getCertificate().isEmpty())) {
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

    private static Set<Path> getPathWithParents(Path path) {
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

    private static void replaceAgentNameWithAgentId(DBItemDeploymentHistory deployed, Set<UpdateableWorkflowJobAgentName> updateableAgentNames,
            String controllerId) throws JsonParseException, JsonMappingException, IOException {
        Workflow workflow = (Workflow) deployed.readUpdateableContent();
        Set<UpdateableWorkflowJobAgentName> filteredUpdateables = updateableAgentNames.stream().filter(item -> item.getWorkflowPath().equals(deployed
                .getPath())).collect(Collectors.toSet());
        workflow.getJobs().getAdditionalProperties().keySet().stream().forEach(jobname -> {
            Job job = workflow.getJobs().getAdditionalProperties().get(jobname);
            job.setAgentPath(filteredUpdateables.stream().filter(item -> item.getJobName().equals(jobname) && controllerId.equals(item
                    .getControllerId())).findFirst().get().getAgentId());
        });
    }

    private static void replaceAgentNameWithAgentId(Workflow workflow, Set<UpdateableWorkflowJobAgentName> updateableAgentNames, String controllerId)
            throws JsonParseException, JsonMappingException, IOException {
        Set<UpdateableWorkflowJobAgentName> filteredUpdateables = updateableAgentNames.stream().filter(item -> item.getWorkflowPath().equals(workflow
                .getPath())).collect(Collectors.toSet());
        workflow.getJobs().getAdditionalProperties().keySet().stream().forEach(jobname -> {
            Job job = workflow.getJobs().getAdditionalProperties().get(jobname);
            job.setAgentPath(filteredUpdateables.stream().filter(item -> item.getJobName().equals(jobname) && controllerId.equals(item
                    .getControllerId())).findFirst().get().getAgentId());
        });
    }

    private static void replaceAgentNameWithAgentId(FileOrderSource fileOrderSource, Set<UpdateableFileOrderSourceAgentName> updateableFOSAgentNames,
            String controllerId) throws JsonParseException, JsonMappingException, IOException {
        Set<UpdateableFileOrderSourceAgentName> filteredUpdateables = updateableFOSAgentNames.stream().filter(item -> item.getFileOrderSourceId()
                .equals(fileOrderSource.getPath())).collect(Collectors.toSet());
        fileOrderSource.setAgentPath(filteredUpdateables.stream().filter(item -> controllerId.equals(item.getControllerId())).findFirst().get()
                .getAgentId());
    }

    public static String getValueAsStringWithleadingZeros(Integer i, int length) {
        if (i.toString().length() >= length) {
            return i.toString();
        } else {
            return String.format("%0" + (length - i.toString().length()) + "d%s", 0, i.toString());
        }
    }

    public static Set<DBItemDeploymentHistory> getLatestDepHistoryEntriesActiveForFolder(Config folder, DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        entries.addAll(dbLayer.getLatestDepHistoryItemsFromFolder(folder.getConfiguration().getPath()));
        return entries.stream().filter(item -> item.getOperation().equals(OperationType.UPDATE.value())).collect(Collectors.toSet());
    }

    public static Set<DBItemDeploymentHistory> getLatestDepHistoryEntriesActiveForFolder(Configuration folder, DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        entries.addAll(dbLayer.getLatestDepHistoryItemsFromFolder(folder.getPath()));
        return entries.stream().filter(item -> item.getOperation().equals(OperationType.UPDATE.value())).collect(Collectors.toSet());
    }

    public static Set<DBItemDeploymentHistory> getLatestDepHistoryEntriesActiveForFolder(Config folder, String controllerId, DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        entries.addAll(dbLayer.getLatestDepHistoryItemsFromFolder(folder.getConfiguration().getPath(), controllerId));
        return entries.stream().filter(item -> item.getOperation().equals(OperationType.UPDATE.value())).collect(Collectors.toSet());
    }

    public static Set<DBItemDeploymentHistory> getLatestDepHistoryEntriesActiveForFolders(List<Config> foldersToDelete, DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        foldersToDelete.stream().map(item -> item.getConfiguration().getPath()).forEach(item -> entries.addAll(dbLayer
                .getLatestDepHistoryItemsFromFolder(item)));
        return entries.stream().filter(item -> item.getOperation().equals(OperationType.UPDATE.value())).collect(Collectors.toSet());
    }

    public static Set<DBItemDeploymentHistory> getLatestDepHistoryEntriesActiveForFolders(List<Config> foldersToDelete, String controllerId,
            DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        foldersToDelete.stream().map(item -> item.getConfiguration().getPath()).forEach(item -> entries.addAll(dbLayer
                .getLatestDepHistoryItemsFromFolder(item, controllerId)));
        return entries.stream().filter(item -> item.getOperation().equals(OperationType.UPDATE.value())).collect(Collectors.toSet());
    }

    public static Set<DBItemInventoryConfiguration> getDeployableInventoryConfigurationsfromFolders(List<Configuration> folders,
            DBLayerDeploy dbLayer) {
        List<DBItemInventoryConfiguration> entries = new ArrayList<DBItemInventoryConfiguration>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getDeployableInventoryConfigurationsByFolderWithoutDeployed(item.getPath(), item
                .getRecursive())));
        return entries.stream().collect(Collectors.toSet());
    }

    public static Set<DBItemInventoryConfiguration> getValidDeployableInventoryConfigurationsfromFolders(List<Configuration> folders,
            DBLayerDeploy dbLayer) {
        List<DBItemInventoryConfiguration> entries = new ArrayList<DBItemInventoryConfiguration>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getValidDeployableInventoryConfigurationsByFolder(item.getPath(), item
                .getRecursive())));
        return entries.stream().collect(Collectors.toSet());
    }

    public static Set<DBItemInventoryConfiguration> getValidDeployableDraftInventoryConfigurationsfromFolders(List<Configuration> folders,
            DBLayerDeploy dbLayer) {
        List<DBItemInventoryConfiguration> entries = new ArrayList<DBItemInventoryConfiguration>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getValidDeployableDraftInventoryConfigurationsByFolder(item.getPath(), item
                .getRecursive())));
        return entries.stream().collect(Collectors.toSet());
    }

    public static Set<DBItemInventoryConfiguration> getReleasableInventoryConfigurationsWithoutReleasedfromFolders(List<Configuration> folders,
            DBLayerDeploy dbLayer) {
        List<DBItemInventoryConfiguration> entries = new ArrayList<DBItemInventoryConfiguration>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getReleasableInventoryConfigurationsByFolderWithoutReleased(item.getPath(), item
                .getRecursive())));
        return entries.stream().collect(Collectors.toSet());
    }

    public static Set<DBItemInventoryConfiguration> getValidReleasableInventoryConfigurationsfromFolders(List<Configuration> folders,
            DBLayerDeploy dbLayer) {
        List<DBItemInventoryConfiguration> entries = new ArrayList<DBItemInventoryConfiguration>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getValidReleasableInventoryConfigurationsByFolderWithoutReleased(item.getPath(), item
                .getRecursive())));
        return entries.stream().collect(Collectors.toSet());
    }

    public static Set<DBItemInventoryReleasedConfiguration> getReleasedInventoryConfigurationsfromFoldersWithoutDrafts(List<Configuration> folders,
            DBLayerDeploy dbLayer) {
        List<DBItemInventoryReleasedConfiguration> entries = new ArrayList<DBItemInventoryReleasedConfiguration>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getReleasedInventoryConfigurationsByFolder(item.getPath(), item.getRecursive())));
        Set<DBItemInventoryReleasedConfiguration> allReleased = entries.stream().filter(item -> {
            DBItemInventoryConfiguration dbItem = dbLayer.getConfigurationByName(item.getName(), item.getType());
            if (dbItem != null && item.getPath().equals(dbItem.getPath())) {
                return true;
            } else {
                return false;
            }
        }).filter(item -> {
            if (item.getName() == null || item.getName().isEmpty()) {
                LOGGER.debug(String.format("No name found for item with path: %1$s ", item.getPath()));
                String name = Paths.get(item.getPath()).getFileName().toString();
                item.setName(name);
                LOGGER.debug(String.format("Item name set to: %1$s ", item.getName()));
            }
            Boolean released = dbLayer.getInventoryConfigurationReleasedByNameAndType(item.getName(), item.getType());
            if (released == null) {
                // released item does not exist in current configuration
                // decision: ignore item as only objects from released configurations with existing current configuration are relevant
                return false;
            } else {
                return released;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        return allReleased;
    }

    public static Set<DBItemDeploymentHistory> getLatestActiveDepHistoryEntriesFromFolders(List<Configuration> folders, DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        folders.stream().forEach(item -> entries.addAll(dbLayer.getLatestDepHistoryItemsFromFolder(item.getPath(), item.getRecursive())));
        return entries.stream().filter(item -> !OperationType.DELETE.equals(OperationType.fromValue(item.getOperation()))).filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    public static Set<DBItemDeploymentHistory> getLatestActiveDepHistoryEntriesWithoutDraftsFromFolders(List<Configuration> folders,
            DBLayerDeploy dbLayer) {
        Set<DBItemDeploymentHistory> allLatest = getLatestActiveDepHistoryEntriesFromFolders(folders, dbLayer);
        // filter duplicates, if history item with same name but different folder exists
        allLatest = allLatest.stream().filter(item -> {
            DBItemInventoryConfiguration dbItem = dbLayer.getConfigurationByName(item.getName(), item.getType());
            if (dbItem != null && item.getPath().equals(dbItem.getPath())) {
                return true;
            } else {
                return false;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        return allLatest.stream().filter(item -> {
            if (item.getName() == null || item.getName().isEmpty()) {
                LOGGER.debug(String.format("No name found for item with path: %1$s ", item.getPath()));
                String name = Paths.get(item.getPath()).getFileName().toString();
                item.setName(name);
                LOGGER.debug(String.format("Item name set to: %1$s ", item.getName()));
            }
            Boolean deployed = dbLayer.getInventoryConfigurationDeployedByNameAndType(item.getName(), item.getType());
            if (deployed == null) {
                // history item does not exist in current configuration
                // decision: ignore item as only objects from history with existing current configuration are relevant
                return false;
            } else {
                return deployed;
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
    }

    public static Set<DBItemDeploymentHistory> getLatestDepHistoryEntriesDeleteForFolder(Config folder, String controllerId, DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        entries.addAll(dbLayer.getLatestDepHistoryItemsFromFolder(folder.getConfiguration().getPath(), controllerId));
        return entries.stream().filter(item -> item.getOperation().equals(OperationType.DELETE.value())).collect(Collectors.toSet());
    }

    public static Set<DBItemDeploymentHistory> getLatestDepHistoryEntriesDeleteForFolders(List<Config> foldersToDelete, String controllerId,
            DBLayerDeploy dbLayer) {
        List<DBItemDeploymentHistory> entries = new ArrayList<DBItemDeploymentHistory>();
        foldersToDelete.stream().map(item -> item.getConfiguration().getPath()).forEach(item -> entries.addAll(dbLayer
                .getLatestDepHistoryItemsFromFolder(item, controllerId)));
        return entries.stream().filter(item -> item.getOperation().equals(OperationType.DELETE.value())).collect(Collectors.toSet());
    }

    public static Set<ControllerObject> getDeployableControllerObjectsFromDB(DeployablesValidFilter filter, DBLayerDeploy dbLayer)
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, IOException,
            SOSHibernateException {
        return getDeployableControllerObjectsFromDB(filter, dbLayer, null);
    }

    public static Set<ControllerObject> getDeployableControllerObjectsFromDB(DeployablesValidFilter filter, DBLayerDeploy dbLayer, String commitId)
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, IOException,
            SOSHibernateException {
        Set<ControllerObject> allObjects = new HashSet<ControllerObject>();
        if (filter != null) {
            if (filter.getDeployConfigurations() != null && !filter.getDeployConfigurations().isEmpty()) {
                List<Configuration> depFolders = filter.getDeployConfigurations().stream().filter(item -> item.getConfiguration().getObjectType()
                        .equals(ConfigurationType.FOLDER)).map(item -> item.getConfiguration()).collect(Collectors.toList());
                Set<DBItemDeploymentHistory> allItems = new HashSet<DBItemDeploymentHistory>();
                if (depFolders != null && !depFolders.isEmpty()) {
                    allItems.addAll(getLatestActiveDepHistoryEntriesWithoutDraftsFromFolders(depFolders, dbLayer));
                }
                List<DBItemDeploymentHistory> deploymentDbItems = dbLayer.getFilteredDeployments(filter);
                if (deploymentDbItems != null && !deploymentDbItems.isEmpty()) {
                    allItems.addAll(deploymentDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream().filter(Objects::nonNull).filter(item -> !item.getType().equals(ConfigurationType.FOLDER.intValue())).forEach(
                            item -> {
                                if (commitId != null) {
                                    dbLayer.storeCommitIdForLaterUsage(item, commitId);
                                }
                                allObjects.add(getContollerObjectFromDBItem(item, commitId));
                            });
                }
            }
            if (filter.getDraftConfigurations() != null && !filter.getDraftConfigurations().isEmpty()) {
                List<Configuration> draftFolders = filter.getDraftConfigurations().stream().filter(item -> item.getConfiguration().getObjectType()
                        .equals(ConfigurationType.FOLDER)).map(item -> item.getConfiguration()).collect(Collectors.toList());
                Set<DBItemInventoryConfiguration> allItems = new HashSet<DBItemInventoryConfiguration>();
                if (draftFolders != null && !draftFolders.isEmpty()) {
                    allItems.addAll(getDeployableInventoryConfigurationsfromFolders(draftFolders, dbLayer));
                }
                List<DBItemInventoryConfiguration> configurationDbItems = dbLayer.getFilteredDeployableConfigurations(filter);
                if (configurationDbItems != null && !configurationDbItems.isEmpty()) {
                    allItems.addAll(configurationDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream().filter(Objects::nonNull).filter(item -> !item.getTypeAsEnum().equals(ConfigurationType.FOLDER)).forEach(
                            item -> {
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

    public static Set<ConfigurationObject> getDeployableConfigurationObjectsFromDB(DeployablesFilter filter, DBLayerDeploy dbLayer)
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, IOException,
            SOSHibernateException {
        return getDeployableConfigurationObjectsFromDB(filter, dbLayer, null);
    }

    public static Set<ConfigurationObject> getDeployableConfigurationObjectsFromDB(DeployablesFilter filter, DBLayerDeploy dbLayer, String commitId)
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, IOException,
            SOSHibernateException {
        Set<ConfigurationObject> configurations = new HashSet<ConfigurationObject>();
        if (filter != null) {
            if (filter.getDeployConfigurations() != null && !filter.getDeployConfigurations().isEmpty()) {
                List<Configuration> depFolders = filter.getDeployConfigurations().stream().filter(item -> item.getConfiguration().getObjectType()
                        .equals(ConfigurationType.FOLDER)).map(item -> item.getConfiguration()).collect(Collectors.toList());
                Set<DBItemDeploymentHistory> allItems = new HashSet<DBItemDeploymentHistory>();
                if (depFolders != null && !depFolders.isEmpty()) {
                    allItems.addAll(getLatestActiveDepHistoryEntriesFromFolders(depFolders, dbLayer));
                }
                List<DBItemDeploymentHistory> deploymentDbItems = dbLayer.getFilteredDeployments(filter);
                if (deploymentDbItems != null && !deploymentDbItems.isEmpty()) {
                    allItems.addAll(deploymentDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream().filter(Objects::nonNull).filter(item -> !item.getType().equals(ConfigurationType.FOLDER.intValue())).forEach(
                            item -> {
                                if (commitId != null) {
                                    dbLayer.storeCommitIdForLaterUsage(item, commitId);
                                }
                                configurations.add(getConfigurationObjectFromDBItem(item, commitId));
                            });
                }
            }
            if (filter.getDraftConfigurations() != null && !filter.getDraftConfigurations().isEmpty()) {
                List<Configuration> draftFolders = filter.getDraftConfigurations().stream().filter(item -> item.getConfiguration().getObjectType()
                        .equals(ConfigurationType.FOLDER)).map(item -> item.getConfiguration()).collect(Collectors.toList());
                Set<DBItemInventoryConfiguration> allItems = new HashSet<DBItemInventoryConfiguration>();
                if (draftFolders != null && !draftFolders.isEmpty()) {
                    allItems.addAll(getDeployableInventoryConfigurationsfromFolders(draftFolders, dbLayer));
                }
                List<DBItemInventoryConfiguration> configurationDbItems = dbLayer.getFilteredDeployableConfigurations(filter);
                if (configurationDbItems != null && !configurationDbItems.isEmpty()) {
                    allItems.addAll(configurationDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream().filter(Objects::nonNull).filter(item -> !item.getTypeAsEnum().equals(ConfigurationType.FOLDER)).forEach(
                            item -> {
                                boolean alreadyPresent = false;
                                for (ConfigurationObject config : configurations) {
                                    if (item.getName().equals(config.getName()) && item.getTypeAsEnum().equals(config.getObjectType())) {
                                        alreadyPresent = true;
                                        break;
                                    }
                                }
                                if (!alreadyPresent) {
                                    configurations.add(getConfigurationObjectFromDBItem(item));
                                }
                            });
                }
            }
        }
        return configurations;
    }

    public static Set<ConfigurationObject> getReleasableObjectsFromDB(ReleasablesFilter filter, DBLayerDeploy dbLayer)
            throws DBConnectionRefusedException, DBInvalidDataException, JocMissingRequiredParameterException, DBMissingDataException, IOException,
            SOSHibernateException {
        Map<String, ConfigurationObject> allObjectsMap = new HashMap<String, ConfigurationObject>();
        if (filter != null) {
            if (filter.getReleasedConfigurations() != null && !filter.getReleasedConfigurations().isEmpty()) {
                List<Configuration> releasedFolders = filter.getReleasedConfigurations().stream().filter(item -> item.getConfiguration()
                        .getObjectType().equals(ConfigurationType.FOLDER)).map(item -> item.getConfiguration()).collect(Collectors.toList());
                Set<DBItemInventoryReleasedConfiguration> allItems = new HashSet<DBItemInventoryReleasedConfiguration>();
                if (releasedFolders != null && !releasedFolders.isEmpty()) {
                    allItems.addAll(getReleasedInventoryConfigurationsfromFoldersWithoutDrafts(releasedFolders, dbLayer));
                }
                List<DBItemInventoryReleasedConfiguration> configurationDbItems = dbLayer.getFilteredReleasedConfigurations(filter);
                if (configurationDbItems != null && !configurationDbItems.isEmpty()) {
                    allItems.addAll(configurationDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream().filter(Objects::nonNull).filter(item -> !item.getTypeAsEnum().equals(ConfigurationType.FOLDER)).forEach(
                            item -> allObjectsMap.put(item.getName(), getConfigurationObjectFromDBItem(item)));
                }
            }
            if (filter.getDraftConfigurations() != null && !filter.getDraftConfigurations().isEmpty()) {
                List<Configuration> draftFolders = filter.getDraftConfigurations().stream().filter(item -> item.getConfiguration().getObjectType()
                        .equals(ConfigurationType.FOLDER)).map(item -> item.getConfiguration()).collect(Collectors.toList());
                Set<DBItemInventoryConfiguration> allItems = new HashSet<DBItemInventoryConfiguration>();
                if (draftFolders != null && !draftFolders.isEmpty()) {
                    allItems.addAll(getReleasableInventoryConfigurationsWithoutReleasedfromFolders(draftFolders, dbLayer));
                }
                List<DBItemInventoryConfiguration> configurationDbItems = dbLayer.getFilteredReleasableConfigurations(filter);
                if (configurationDbItems != null && !configurationDbItems.isEmpty()) {
                    allItems.addAll(configurationDbItems);
                }
                if (!allItems.isEmpty()) {
                    allItems.stream().filter(Objects::nonNull).filter(item -> !item.getTypeAsEnum().equals(ConfigurationType.FOLDER)).forEach(
                            item -> {
                                if (!allObjectsMap.containsKey(item.getName())) {
                                    allObjectsMap.put(item.getName(), getConfigurationObjectFromDBItem(item));
                                }
                            });
                }
            }
        }
        Set<ConfigurationObject> withoutDuplicates = new HashSet<ConfigurationObject>(allObjectsMap.values());
        return withoutDuplicates;
    }

    private static ControllerObject mapInvConfigToJSObject(DBItemInventoryConfiguration item) {
        return mapInvConfigToJSObject(item, null);
    }

    private static ControllerObject mapInvConfigToJSObject(DBItemInventoryConfiguration item, String commitId) {
        try {
            ControllerObject jsObject = new ControllerObject();
            // jsObject.setId(item.getId());
            jsObject.setPath(item.getPath());
            jsObject.setObjectType(DeployType.fromValue(item.getType()));
            switch (jsObject.getObjectType()) {
            case WORKFLOW:
                Workflow workflow = JsonConverter.readAsConvertedWorkflow(item.getContent());
                //Workflow workflow = Globals.objectMapper.readValue(item.getContent().getBytes(), Workflow.class);
                if (commitId != null) {
                    workflow.setVersionId(commitId);
                }
                jsObject.setContent(workflow);
                break;
            case JOBRESOURCE:
                JobResource jobResource = Globals.objectMapper.readValue(item.getContent().getBytes(), JobResource.class);
                jsObject.setContent(jobResource);
                break;
            case LOCK:
                Lock lock = Globals.objectMapper.readValue(item.getContent().getBytes(), Lock.class);
                jsObject.setContent(lock);
                break;
            case NOTICEBOARD:
                Board board = Globals.objectMapper.readValue(item.getContent().getBytes(), Board.class);
                jsObject.setContent(board);
                break;
            case JOBCLASS:
                JobClass jobClass = Globals.objectMapper.readValue(item.getContent().getBytes(), JobClass.class);
                jsObject.setContent(jobClass);
                break;
            case FILEORDERSOURCE:
                FileOrderSource fileOrderSource = Globals.objectMapper.readValue(item.getContent(), FileOrderSource.class);
                jsObject.setContent(fileOrderSource);
                break;
            }
            jsObject.setAccount(ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc()));
            // TODO: setVersion
            // jsObject.setVersion(item.getVersion());
            jsObject.setModified(item.getModified());
            return jsObject;
        } catch (IOException e) {
            throw new JocException(e);
        }
    }

    private static ControllerObject getContollerObjectFromDBItem(DBItemDeploymentHistory item, String commitId) {
        try {
            ControllerObject jsObject = new ControllerObject();
            jsObject.setPath(item.getPath());
            jsObject.setObjectType(DeployType.fromValue(item.getType()));
            switch (jsObject.getObjectType()) {
            case WORKFLOW:
                Workflow workflow = JsonConverter.readAsConvertedWorkflow(item.getInvContent());
                //Workflow workflow = Globals.objectMapper.readValue(item.getInvContent().getBytes(), Workflow.class);
                if (commitId != null) {
                    workflow.setVersionId(commitId);
                }
                jsObject.setContent(workflow);
                break;
            case JOBRESOURCE:
                JobResource jobResource = Globals.objectMapper.readValue(item.getInvContent().getBytes(), JobResource.class);
                jsObject.setContent(jobResource);
                break;
            case JOBCLASS:
                JobClass jobClass = Globals.objectMapper.readValue(item.getInvContent().getBytes(), JobClass.class);
                jsObject.setContent(jobClass);
                break;
            case LOCK:
                Lock lock = Globals.objectMapper.readValue(item.getInvContent().getBytes(), Lock.class);
                jsObject.setContent(lock);
                break;
            case NOTICEBOARD:
                Board board = Globals.objectMapper.readValue(item.getInvContent().getBytes(), Board.class);
                jsObject.setContent(board);
                break;
            case FILEORDERSOURCE:
                FileOrderSource fileOrderSource = Globals.objectMapper.readValue(item.getInvContent().getBytes(), FileOrderSource.class);
                jsObject.setContent(fileOrderSource);
                break;
            }
            jsObject.setVersion(item.getVersion());
            jsObject.setAccount(ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc()));
            return jsObject;
        } catch (IOException e) {
            throw new JocException(e);
        }
    }

    private static ConfigurationObject getConfigurationObjectFromDBItem(DBItemDeploymentHistory item, String commitId) {
        try {
            ConfigurationObject configurationObject = new ConfigurationObject();
            // jsObject.setId(item.getId());
            configurationObject.setPath(item.getPath());
            configurationObject.setName(item.getName());
            configurationObject.setObjectType(ConfigurationType.fromValue(item.getType()));
            switch (configurationObject.getObjectType()) {
            case WORKFLOW:
                com.sos.inventory.model.workflow.Workflow workflow = Globals.objectMapper.readValue(item.getInvContent().getBytes(),
                        com.sos.inventory.model.workflow.Workflow.class);
                configurationObject.setConfiguration(workflow);
                break;
            case JOBRESOURCE:
                com.sos.inventory.model.jobresource.JobResource jobResource = Globals.objectMapper.readValue(item.getInvContent().getBytes(),
                        com.sos.inventory.model.jobresource.JobResource.class);
                configurationObject.setConfiguration(jobResource);
                break;
            case JOBCLASS:
                com.sos.inventory.model.jobclass.JobClass jobClass = Globals.objectMapper.readValue(item.getInvContent().getBytes(),
                        com.sos.inventory.model.jobclass.JobClass.class);
                configurationObject.setConfiguration(jobClass);
                break;
            case LOCK:
                com.sos.inventory.model.lock.Lock lock = Globals.objectMapper.readValue(item.getInvContent().getBytes(),
                        com.sos.inventory.model.lock.Lock.class);
                configurationObject.setConfiguration(lock);
                break;
            case NOTICEBOARD:
                com.sos.inventory.model.board.Board board = Globals.objectMapper.readValue(item.getInvContent().getBytes(),
                        com.sos.inventory.model.board.Board.class);
                configurationObject.setConfiguration(board);
                break;
            case FILEORDERSOURCE:
                com.sos.inventory.model.fileordersource.FileOrderSource fileOrderSource = Globals.objectMapper.readValue(item.getInvContent()
                        .getBytes(), com.sos.inventory.model.fileordersource.FileOrderSource.class);
                configurationObject.setConfiguration(fileOrderSource);
                break;
            default:
                break;
            }
            // configurationObject.setVersion(item.getVersion());
            // configurationObject.setAccount(Globals.getConfigurationGlobalsJoc().getDefaultProfileAccount().getValue());
            return configurationObject;
        } catch (IOException e) {
            throw new JocException(e);
        }
    }

    private static ConfigurationObject getConfigurationObjectFromDBItem(DBItemInventoryConfiguration item) {
        try {
            ConfigurationObject configuration = new ConfigurationObject();
            // configuration.setId(item.getId());
            configuration.setPath(item.getPath());
            configuration.setObjectType(ConfigurationType.fromValue(item.getType()));
            switch (configuration.getObjectType()) {
            case WORKFLOW:
                com.sos.inventory.model.workflow.Workflow workflow = Globals.objectMapper.readValue(item.getContent().getBytes(),
                        com.sos.inventory.model.workflow.Workflow.class);
                configuration.setConfiguration(workflow);
                break;
            case JOBRESOURCE:
                com.sos.inventory.model.jobresource.JobResource jobResource = Globals.objectMapper.readValue(item.getContent().getBytes(),
                        com.sos.inventory.model.jobresource.JobResource.class);
                configuration.setConfiguration(jobResource);
                break;
            case LOCK:
                com.sos.inventory.model.lock.Lock lock = Globals.objectMapper.readValue(item.getContent().getBytes(),
                        com.sos.inventory.model.lock.Lock.class);
                configuration.setConfiguration(lock);
                break;
            case FILEORDERSOURCE:
                com.sos.inventory.model.fileordersource.FileOrderSource fileOrderSource = Globals.objectMapper.readValue(item.getContent().getBytes(),
                        com.sos.inventory.model.fileordersource.FileOrderSource.class);
                configuration.setConfiguration(fileOrderSource);
                break;
            case SCHEDULE:
                Schedule schedule = Globals.objectMapper.readValue(item.getContent(), Schedule.class);
                configuration.setConfiguration(schedule);
                break;
            case WORKINGDAYSCALENDAR:
            case NONWORKINGDAYSCALENDAR:
                Calendar calendar = Globals.objectMapper.readValue(item.getContent().getBytes(), Calendar.class);
                configuration.setConfiguration(calendar);
                break;
            case JOBCLASS:
                com.sos.inventory.model.jobclass.JobClass jobClass = Globals.objectMapper.readValue(item.getContent().getBytes(),
                        com.sos.inventory.model.jobclass.JobClass.class);
                configuration.setConfiguration(jobClass);
                break;
            case NOTICEBOARD:
                com.sos.inventory.model.board.Board board = Globals.objectMapper.readValue(item.getContent().getBytes(),
                        com.sos.inventory.model.board.Board.class);
                configuration.setConfiguration(board);
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
            // configuration.setId(item.getId());
            configuration.setPath(item.getPath());
            configuration.setObjectType(ConfigurationType.fromValue(item.getType()));
            switch (configuration.getObjectType()) {
            case WORKINGDAYSCALENDAR:
            case NONWORKINGDAYSCALENDAR:
                Calendar calendar = Globals.objectMapper.readValue(item.getContent().getBytes(), Calendar.class);
                configuration.setConfiguration(calendar);
                break;
            case SCHEDULE:
                Schedule schedule = Globals.objectMapper.readValue(item.getContent(), Schedule.class);
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

    private static boolean checkObjectNotEmpty(Workflow workflow) {
        if (workflow != null && workflow.getInstructions() == null && workflow.getJobs() == null && workflow.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(com.sos.inventory.model.workflow.Workflow workflow) {
        if (workflow != null && workflow.getDocumentationName() == null && workflow.getInstructions() == null && workflow.getJobs() == null
                && workflow.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(JobResource jobResource) {
        if (jobResource != null && jobResource.getEnv() == null && jobResource.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(com.sos.inventory.model.jobresource.JobResource jobResource) {
        if (jobResource != null && jobResource.getDocumentationName() == null && jobResource.getEnv() == null && jobResource.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(Board board) {
        if (board != null && board.getEndOfLife() == null && board.getExpectOrderToNoticeId() == null && board.getPostOrderToNoticeId() == null
                && board.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(com.sos.inventory.model.board.Board board) {
        if (board != null && board.getDocumentationName() == null && board.getEndOfLife() == null && board.getExpectOrderToNoticeId() == null && board
                .getPostOrderToNoticeId() == null && board.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(JobClass jobClass) {
        if (jobClass != null && jobClass.getMaxProcesses() == null && jobClass.getPriority() == null && jobClass.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(com.sos.inventory.model.jobclass.JobClass jobClass) {
        if (jobClass != null && jobClass.getDocumentationName() == null && jobClass.getMaxProcesses() == null && jobClass.getPriority() == null
                && jobClass.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(FileOrderSource fileOrderSource) {
        if (fileOrderSource != null && fileOrderSource.getAgentPath() == null && fileOrderSource.getDelay() == null && fileOrderSource
                .getTYPE() == null && fileOrderSource.getPattern() == null && fileOrderSource.getWorkflowPath() == null && fileOrderSource
                        .getDirectory() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(com.sos.inventory.model.fileordersource.FileOrderSource fileOrderSource) {
        if (fileOrderSource != null && fileOrderSource.getDocumentationName() == null && fileOrderSource.getAgentName() == null && fileOrderSource
                .getDelay() == null && fileOrderSource.getTYPE() == null && fileOrderSource.getPattern() == null && fileOrderSource
                        .getWorkflowName() == null && fileOrderSource.getDirectory() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(Lock lock) {
        if (lock != null && lock.getLimit() == null && lock.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(com.sos.inventory.model.lock.Lock lock) {
        if (lock != null && lock.getDocumentationName() == null && lock.getLimit() == null && lock.getTYPE() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(Schedule schedule) {
        if (schedule != null && schedule.getDocumentationName() == null && schedule.getPlanOrderAutomatically() == null && schedule.getPath() == null
                && schedule.getCalendars() == null && schedule.getWorkflowPath() == null && schedule.getSubmitOrderToControllerWhenPlanned() == null
                && schedule.getNonWorkingCalendars() == null && schedule.getVariableSets() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static boolean checkObjectNotEmpty(Calendar calendar) {
        if (calendar != null && calendar.getDocumentationName() == null && calendar.getExcludes() == null && calendar.getPath() == null && calendar
                .getFrom() == null && calendar.getIncludes() == null && calendar.getName() == null && calendar.getTo() == null && calendar
                        .getType() == null) {
            return false;
        } else {
            return true;
        }
    }

    private static JocMetaInfo createJocMetaInfo(Version jocVersion, Version apiVersion, Version inventoryVersion) {
        JocMetaInfo jocMetaInfo = new JocMetaInfo();
        if (jocVersion != null) {
            jocMetaInfo.setJocVersion(jocVersion.getVersion());
        }
        if (inventoryVersion != null) {
            jocMetaInfo.setInventorySchemaVersion(inventoryVersion.getVersion());
        }
        if (apiVersion != null) {
            jocMetaInfo.setApiVersion(apiVersion.getVersion());
        }
        return jocMetaInfo;
    }

    public static boolean isJocMetaInfoNullOrEmpty(JocMetaInfo jocMetaInfo) {
        if (jocMetaInfo == null || ((jocMetaInfo.getJocVersion() == null || jocMetaInfo.getJocVersion().isEmpty()) && (jocMetaInfo
                .getInventorySchemaVersion() == null || jocMetaInfo.getInventorySchemaVersion().isEmpty()) && (jocMetaInfo.getApiVersion() == null
                        || jocMetaInfo.getApiVersion().isEmpty()))) {
            return true;
        } else {
            return false;
        }
    }

    public static Version readVersion(InputStream stream, String path) throws JocException {
        try {
            if (stream != null) {
                return Globals.objectMapper.readValue(stream, Version.class);
            } else {
                return null;
            }
        } catch (JocException e) {
            throw e;
        } catch (Exception e) {
            throw new JocException(new JocError("JOC-002", String.format("Error while reading %1$s from classpath: ", path)), e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
            }
        }
    }

    public static List<Configuration> handleFolders1(List<Configuration> foldersIn, DBLayerDeploy dbLayer) {
        return foldersIn.stream().flatMap(item -> {
            List<DBItemInventoryConfiguration> dbItems = dbLayer.getInvConfigurationFolders(item.getPath(), true);
            return dbItems.stream().map(dbItem -> {
                Configuration configuration = new Configuration();
                configuration.setPath(dbItem.getPath());
                configuration.setObjectType(ConfigurationType.FOLDER);
                return configuration;
            }).filter(Objects::nonNull);
        }).collect(Collectors.toList());
    }

    public static List<Config> handleFolders(List<Config> foldersIn, DBLayerDeploy dbLayer) {
        return foldersIn.stream().flatMap(item -> {
            List<DBItemInventoryConfiguration> dbItems = dbLayer.getInvConfigurationFolders(item.getConfiguration().getPath(), true);
            return dbItems.stream().map(dbItem -> {
                Config config = new Config();
                Configuration configuration = new Configuration();
                configuration.setPath(dbItem.getPath());
                configuration.setObjectType(ConfigurationType.FOLDER);
                config.setConfiguration(configuration);
                return config;
            }).filter(Objects::nonNull);
        }).collect(Collectors.toList());
    }

    public static boolean verifyCertificateAgainstCAs(X509Certificate cert, List<DBItemInventoryCertificate> caCertDBItems) {
        Set<X509Certificate> caCerts = caCertDBItems.stream().map(item -> {
            try {
                return KeyUtil.getX509Certificate(item.getPem());
            } catch (CertificateException | UnsupportedEncodingException e) {
                throw new JocKeyNotParseableException(e);
            }
        }).filter(Objects::nonNull).collect(Collectors.toSet());
        for (X509Certificate caCert : caCerts) {
            try {
                cert.verify(caCert.getPublicKey());
                return true;
            } catch (Exception e) {
                // Do nothing if verification fails, as an exception here only indicates that the verification failed
            }
        }
        return false;
    }

    private static JFileWatch getJFileWatch(FileOrderSource fileOrderSource) throws JocDeployException {
        Long delay = fileOrderSource.getDelay() == null ? 2L : fileOrderSource.getDelay();
        Either<Problem, JFileWatch> fileWatch = JFileWatch.checked(OrderWatchPath.of(fileOrderSource.getPath()), WorkflowPath.of(fileOrderSource
                .getWorkflowPath()), AgentPath.of(fileOrderSource.getAgentPath()), Paths.get(fileOrderSource.getDirectory()),
                getFileOrderSourcePattern(fileOrderSource), getFileOrderIdPattern(fileOrderSource), Duration.ofSeconds(delay));
        if (fileWatch.isLeft()) {
            throw new JocDeployException(fileWatch.getLeft().toString());
        } else {
            return fileWatch.get();
        }
    }

    private static JLock getJLock(Lock lock) {
        return JLock.of(LockPath.of(lock.getPath()), lock.getLimit());
    }

    private static JBoard getJBoard(Board board) {
        // JBoard(Board(boardPath, toNotice.asScala, readingOrderToNoticeId.asScala, endOfLife.asScala))
        JExpression toNoticeExpression = null;
        JExpression readingOrderToNoticeIdExpression = null;
        JExpression endOfLifeExpression = null;
        Either<Problem, JExpression> toNoticeEither = JExpression.parse(board.getPostOrderToNoticeId());
        if (toNoticeEither.isLeft()) {
            throw new JocDeployException(toNoticeEither.getLeft().toString());
        } else {
            toNoticeExpression = toNoticeEither.get();
        }
        Either<Problem, JExpression> readingOrderToNoticeIdEither = JExpression.parse(board.getExpectOrderToNoticeId());
        if (readingOrderToNoticeIdEither.isLeft()) {
            throw new JocDeployException(readingOrderToNoticeIdEither.getLeft().toString());
        } else {
            readingOrderToNoticeIdExpression = readingOrderToNoticeIdEither.get();
        }
        Either<Problem, JExpression> endOfLifeEither = JExpression.parse(board.getEndOfLife());
        if (endOfLifeEither.isLeft()) {
            throw new JocDeployException(endOfLifeEither.getLeft().toString());
        } else {
            endOfLifeExpression = endOfLifeEither.get();
        }
        return JBoard.of(BoardPath.of(board.getPath()), toNoticeExpression, readingOrderToNoticeIdExpression, endOfLifeExpression);
    }

    private static SignedString getSignedStringWithCertificate(String jsonContent, String signature, String signatureAlgorithm, String certificate) {
        LOGGER.debug("JSON send to controller: ");
        LOGGER.debug(jsonContent);
        return SignedString.x509WithCertificate(jsonContent, signature, signatureAlgorithm, certificate);
    }

    private static SignedString getSignedStringWithSignerDN(String jsonContent, String signature, String signatureAlgorithm, String signerDN) {
        LOGGER.debug("JSON send to controller: ");
        LOGGER.debug(jsonContent);
        return SignedString.x509WithSignedId(jsonContent, signature, signatureAlgorithm, SignerId.of(signerDN));
    }

    private static Optional<String> getFileOrderSourcePattern(FileOrderSource fileOrderSource) {
        if (fileOrderSource.getPattern() == null || fileOrderSource.getPattern().isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(fileOrderSource.getPattern());
    }

    private static Optional<String> getFileOrderIdPattern(FileOrderSource fileOrderSource) {
        String idPattern = "'#' ++ now(format='yyyy-MM-dd', timezone='%s') ++ \"#F$epochSecond-$orderWatchPath:$0\"";
        String timeZone = fileOrderSource.getTimeZone();
        if (timeZone == null || timeZone.isEmpty()) {
            timeZone = "Etc/UTC";
        }
        fileOrderSource.setTimeZone(null);
        return Optional.of(String.format(idPattern, timeZone));
    }

    public static void postDeployHistoryEvent(DBItemDeploymentHistory dbItem) {
        if (DeployType.WORKFLOW.intValue() == dbItem.getType()) {
            EventBus.getInstance().post(new DeployHistoryWorkflowEvent(dbItem.getControllerId(), dbItem.getName(), dbItem.getCommitId(), dbItem
                    .getPath(), ConfigurationType.WORKFLOW.intValue()));
        } else if (DeployType.JOBRESOURCE.intValue() == dbItem.getType()) {
            EventBus.getInstance().post(new DeployHistoryJobResourceEvent(dbItem.getControllerId(), dbItem.getName(), dbItem.getCommitId(), dbItem
                    .getPath(), ConfigurationType.JOBRESOURCE.intValue()));
        }
    }

    public static DBItemDeploymentHistory cloneInvCfgToDepHistory(DBItemInventoryConfiguration cfg, String account, String controllerId,
            String commitId, Long auditLogId) {
        DBItemDeploymentHistory newItem = new DBItemDeploymentHistory();
        newItem.setAccount(account);
        newItem.setAuditlogId(auditLogId);
        newItem.setControllerId(controllerId);
        newItem.setControllerInstanceId(0L);
        newItem.setFolder(cfg.getFolder());
        newItem.setInvContent(cfg.getContent());
        newItem.setInventoryConfigurationId(cfg.getId());
        newItem.setName(cfg.getName());
        newItem.setPath(cfg.getPath());
        newItem.setType(cfg.getType());
        newItem.setTitle(cfg.getTitle());
        // TODO: type mapping
        try {
            newItem.writeUpdateableContent((IDeployObject) JsonConverter.readAsConvertedDeployObject(cfg.getContent(), StoreDeployments.CLASS_MAPPING
                    .get(cfg.getType()), commitId));
//            newItem.writeUpdateableContent((IDeployObject) Globals.objectMapper.readValue(cfg.getContent(), StoreDeployments.CLASS_MAPPING.get(cfg
//                    .getType())));
        } catch (IOException e) {
            throw new JocException(e);
        }
        newItem.setCommitId(commitId);
        return newItem;
    }

    private static void updatePath(DBItemDeploymentHistory deployed) {
        try {
            if (deployed.getType() == DeployType.WORKFLOW.intValue()) {
                ((Workflow) deployed.readUpdateableContent()).setPath(Paths.get(deployed.getPath()).getFileName().toString());
            } else if (deployed.getType() == DeployType.JOBRESOURCE.intValue()) {
                ((JobResource) deployed.readUpdateableContent()).setPath(deployed.getName());
            } else if (deployed.getType() == DeployType.LOCK.intValue()) {
                ((Lock) deployed.readUpdateableContent()).setPath(deployed.getName());
            } else if (deployed.getType() == DeployType.FILEORDERSOURCE.intValue()) {
                ((FileOrderSource) deployed.readUpdateableContent()).setPath(deployed.getName());
            } else if (deployed.getType() == DeployType.NOTICEBOARD.intValue()) {
                ((Board) deployed.readUpdateableContent()).setPath(deployed.getName());
            } else if (deployed.getType() == DeployType.JOBCLASS.intValue()) {
                ((JobClass) deployed.readUpdateableContent()).setPath(deployed.getName());
            }
            deployed.setContent(JsonSerializer.serializeAsString(deployed.readUpdateableContent()));
        } catch (JsonProcessingException e) {
        }
    }
}