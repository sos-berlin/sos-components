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
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.commons.sign.keys.sign.SignObject;
import com.sos.commons.sign.keys.verify.VerifySignature;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.classes.proxy.ControllerApi;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocMissingKeyException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.exceptions.JocSignatureVerificationException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.pgp.JocKeyType;
import com.sos.joc.model.publish.DeploymentState;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.Signature;
import com.sos.joc.model.publish.SignaturePath;
import com.sos.joc.publish.common.JSObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpDownloadMapper;

import io.vavr.control.Either;
import js7.base.crypt.SignedString;
import js7.base.crypt.SignerId;
import js7.base.problem.Problem;
import js7.data.item.VersionId;
import js7.data.workflow.WorkflowPath;
import js7.proxy.javaapi.data.item.JUpdateRepoOperation;
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

    public static Map<DBItemInventoryConfiguration, DBItemDepSignatures> getDraftsWithSignature(String versionId, String account,
            Set<DBItemInventoryConfiguration> unsignedDrafts, SOSHibernateSession session, JocSecurityLevel secLvl) 
                throws JocMissingKeyException, JsonParseException, JsonMappingException, SOSHibernateException, IOException, PGPException, 
                NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException, CertificateException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account, secLvl);
        if (keyPair != null) {
            return getDraftsWithSignature(versionId, account, unsignedDrafts, keyPair, session);
        } else {
            throw new JocMissingKeyException("No Key found for this account.");
        }
    }

    public static Map<DBItemInventoryConfiguration, DBItemDepSignatures> getDraftsWithSignature(String versionId, String account,
            Set<DBItemInventoryConfiguration> unsignedDrafts, JocKeyPair keyPair, SOSHibernateSession session) throws JocMissingKeyException,
            JsonParseException, JsonMappingException, SOSHibernateException, IOException, PGPException, NoSuchAlgorithmException,
            InvalidKeySpecException, InvalidKeyException, SignatureException, CertificateException {
        boolean isPGPKey = false;
        Map<DBItemInventoryConfiguration, DBItemDepSignatures> signedDrafts = new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
        if (keyPair.getPrivateKey() == null || keyPair.getPrivateKey().isEmpty()) {
            throw new JocMissingKeyException(
                    "No private key found fo signing! - Please check your private key from the key management section in your profile.");
        } else {
            DBItemDepSignatures sig = null;
            for (DBItemInventoryConfiguration draft : unsignedDrafts) {
                updateVersionIdOnDraftObject(draft, versionId, session);
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
                    X509Certificate cert = KeyUtil.getX509Certificate(keyPair.getCertificate());
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

    public static Map<DBItemDeploymentHistory, DBItemDepSignatures> getDeploymentsWithSignature(String versionId, String account,
            Set<DBItemDeploymentHistory> depHistoryToRedeploy, SOSHibernateSession session, JocSecurityLevel secLvl)
                    throws JocMissingKeyException, JsonParseException, JsonMappingException, SOSHibernateException, IOException, PGPException,
                    NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account, secLvl);
        if (keyPair != null) {
            return getDeploymentsWithSignature(versionId, account, depHistoryToRedeploy, keyPair, session);
        } else {
            throw new JocMissingKeyException("No Key found for this account.");
        }
    }

    public static Map<DBItemDeploymentHistory, DBItemDepSignatures> getDeploymentsWithSignature(String versionId, String account,
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
                updateVersionIdOnDeployedObject(deployed, versionId, session);
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

    public static Set<DBItemInventoryConfiguration> verifyPGPSignatures(String account, Set<DBItemInventoryConfiguration> signedDrafts,
            JocKeyPair keyPair, SOSHibernateSession connection) throws SOSHibernateException, IOException, PGPException {
        Set<DBItemInventoryConfiguration> verifiedDrafts = new HashSet<DBItemInventoryConfiguration>();
        String publicKey = null;
        if (keyPair.getPublicKey() == null) {
            publicKey = KeyUtil.extractPublicKey(keyPair.getPrivateKey());
        } else {
            publicKey = keyPair.getPublicKey();
        }
        Boolean verified = false;
        for (DBItemInventoryConfiguration draft : signedDrafts) {
            DBLayerDeploy dbLayer = new DBLayerDeploy(connection);
            DBItemDepSignatures dbSignature = dbLayer.getSignature(draft.getId());
            if(dbSignature != null) {
                verified = VerifySignature.verifyPGP(publicKey, draft.getContent(), dbSignature.getSignature());
                if (!verified) {
                    LOGGER.trace(
                            String.format("Signature of object %1$s could not be verified! Object will not be deployed.", draft.getPath()));
                } else {
                    verifiedDrafts.add(draft);
                }
            }
        }
        return verifiedDrafts;
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

    public static CompletableFuture<Either<Problem, Void>> updateRepoAddOrUpdatePGP(
            String versionId,  Map<DBItemInventoryConfiguration, DBItemDepSignatures> drafts, 
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, String controllerId, DBLayerDeploy dbLayer) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateRepoOperation> updateRepoOperations = new HashSet<JUpdateRepoOperation>();
        updateRepoOperations.addAll(
                drafts.keySet().stream().map(
                        item -> JUpdateRepoOperation.addOrReplace(SignedString.of(
                                item.getContent(), 
                                SOSKeyConstants.PGP_ALGORITHM_NAME, 
                                drafts.get(item).getSignature()))
                        ).collect(Collectors.toSet())
                );
        updateRepoOperations.addAll(
                alreadyDeployed.keySet().stream().map(
                        item -> JUpdateRepoOperation.addOrReplace(SignedString.of(
                                item.getContent(), 
                                SOSKeyConstants.PGP_ALGORITHM_NAME, 
                                alreadyDeployed.get(item).getSignature()))
                        ).collect(Collectors.toSet())
                );
        return ControllerApi.of(controllerId).updateRepo(VersionId.of(versionId), Flux.fromIterable(updateRepoOperations));
    }

    public static CompletableFuture<Either<Problem, Void>> updateRepoAddOrUpdatePGP(
            String versionId,  List<DBItemDeploymentHistory> alreadyDeployed, String controllerId) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateRepo(
                VersionId.of(versionId), 
                Flux.fromIterable(
                        alreadyDeployed.stream().map(
                                item -> JUpdateRepoOperation.addOrReplace(SignedString.of(
                                        item.getContent(),
                                        SOSKeyConstants.PGP_ALGORITHM_NAME,
                                        item.getSignedContent()))
                                ).collect(Collectors.toSet())
                        )
                );
    }

    public static CompletableFuture<Either<Problem, Void>> updateRepoAddOrUpdateWithX509(
            String versionId,  Map<DBItemInventoryConfiguration, DBItemDepSignatures> drafts, 
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, String controllerId, DBLayerDeploy dbLayer,
            String signatureAlgorithm, String signerDN) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        Set<JUpdateRepoOperation> updateRepoOperations = new HashSet<JUpdateRepoOperation>();
        updateRepoOperations.addAll(
                drafts.keySet().stream().map(
                        item -> JUpdateRepoOperation.addOrReplace(SignedString.x509WithSignedId(
                                item.getContent(), 
                                drafts.get(item).getSignature(), 
                                signatureAlgorithm, 
                                SignerId.of(signerDN)))
                        ).collect(Collectors.toSet())
                );
        updateRepoOperations.addAll(
                alreadyDeployed.keySet().stream().map(
                        item -> JUpdateRepoOperation.addOrReplace(SignedString.x509WithSignedId(
                                item.getContent(), 
                                alreadyDeployed.get(item).getSignature(), 
                                signatureAlgorithm, 
                                SignerId.of(signerDN)))
                        ).collect(Collectors.toSet())
                );
        return ControllerApi.of(controllerId).updateRepo(VersionId.of(versionId), Flux.fromIterable(updateRepoOperations));
    }

    public static CompletableFuture<Either<Problem, Void>> updateRepoAddOrUpdateWithX509(
            String versionId,  List<DBItemDeploymentHistory> alreadyDeployed, String controllerId, String signatureAlgorithm, String signerDN) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        return ControllerApi.of(controllerId).updateRepo(
                VersionId.of(versionId), 
                Flux.fromIterable(
                        alreadyDeployed.stream().map(
                                item -> JUpdateRepoOperation.addOrReplace(SignedString.x509WithSignedId(
                                        item.getContent(), 
                                        item.getSignedContent(), 
                                        signatureAlgorithm, 
                                        SignerId.of(signerDN)))
                                ).collect(Collectors.toSet())
                        )
                );
    }

    public static CompletableFuture<Either<Problem, Void>> updateRepoDelete(String versionId,
            List<DBItemDeploymentHistory> alreadyDeployedtoDelete, String controllerId, DBLayerDeploy dbLayer, String keyAlgorithm) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        if ("RSA".equals(keyAlgorithm) || "ECDSA".equals(keyAlgorithm)) {
            keyAlgorithm = "X509";
        }
        Set<JUpdateRepoOperation> updateRepoOperations = new HashSet<JUpdateRepoOperation>();
        if (alreadyDeployedtoDelete != null) {
            for (DBItemDeploymentHistory toDelete : alreadyDeployedtoDelete) {
                switch (DeployType.fromValue(toDelete.getType())) {
                case WORKFLOW:
                    updateRepoOperations.add(JUpdateRepoOperation.delete(WorkflowPath.of(toDelete.getPath())));
                    break;
                case AGENTREF:
                    //updateRepoOperations.add(JUpdateRepoOperation.delete(AgentName.of(toDelete.getPath())));
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
        return ControllerApi.of(controllerId).updateRepo(VersionId.of(versionId), Flux.fromIterable(updateRepoOperations));
    }

    public static CompletableFuture<Either<Problem, Void>> updateRepoAddUpdateDeleteDelete(
            String versionId, Map<DBItemInventoryConfiguration, DBItemDepSignatures> drafts, 
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, List<DBItemDeploymentHistory> alreadyDeployedtoDelete, 
            String controllerId, DBLayerDeploy dbLayer, String keyAlgorithm) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        if ("RSA".equals(keyAlgorithm) || "ECDSA".equals(keyAlgorithm)) {
            keyAlgorithm = "X509";
        }
        Set<JUpdateRepoOperation> updateRepoOperations = new HashSet<JUpdateRepoOperation>();
        if (drafts != null) {
            for (DBItemInventoryConfiguration draft : drafts.keySet()) {
                if (draft != null) {
                    updateRepoOperations.add(JUpdateRepoOperation.addOrReplace(SignedString.of(
                            draft.getContent(), keyAlgorithm, drafts.get(draft).getSignature())));
                }
            }
        }
        if (alreadyDeployed != null) {
            for (DBItemDeploymentHistory reDeploy : alreadyDeployed.keySet()) {
                if (reDeploy != null) {
                    updateRepoOperations.add(JUpdateRepoOperation.addOrReplace(SignedString.of(
                            reDeploy.getContent(), keyAlgorithm, alreadyDeployed.get(reDeploy).getSignature())));
                }
            }
        }
        if (alreadyDeployedtoDelete != null) {
            for (DBItemDeploymentHistory toDelete : alreadyDeployedtoDelete) {
                switch (DeployType.fromValue(toDelete.getType())) {
                case WORKFLOW:
                    updateRepoOperations.add(JUpdateRepoOperation.delete(WorkflowPath.of(toDelete.getPath())));
                    break;
                case AGENTREF:
                    //updateRepoOperations.add(JUpdateRepoOperation.delete(AgentName.of(toDelete.getPath())));
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
        return ControllerApi.of(controllerId).updateRepo(VersionId.of(versionId), Flux.fromIterable(updateRepoOperations));
    }

    private static void updateVersionIdOnDraftObject(DBItemInventoryConfiguration draft, String versionId, SOSHibernateSession session)
            throws JsonParseException, JsonMappingException, IOException, SOSHibernateException, JocNotImplementedException {
        switch (ConfigurationType.fromValue(draft.getType())) {
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
        case WORKINGDAYSCALENDAR:
        case NONWORKINGDAYSCALENDAR:
        case FOLDER:
        case JOBCLASS:
        case JUNCTION:
        case ORDER:
        default:
            throw new JocNotImplementedException();
        }
        session.update(draft);
    }

    private static void updateVersionIdOnDeployedObject(DBItemDeploymentHistory deployed, String versionId, SOSHibernateSession session)
            throws JsonParseException, JsonMappingException, IOException, SOSHibernateException, JocNotImplementedException {

        switch (DeployType.fromValue(deployed.getType())) {
        case WORKFLOW:
            Workflow workflow = om.readValue(deployed.getContent(), Workflow.class);
            workflow.setVersionId(versionId);
            deployed.setContent(om.writeValueAsString(workflow));
            break;
        case AGENTREF:
            AgentRef agentRef = om.readValue(deployed.getContent(), AgentRef.class);
            agentRef.setVersionId(versionId);
            deployed.setContent(om.writeValueAsString(agentRef));
            break;
        case LOCK:
            // TODO: locks and other objects
        case JUNCTION:
        default:
            throw new JocNotImplementedException();
        }
    }

    public static Set<DBItemDeploymentHistory> cloneInvConfigurationsToDepHistoryItems(
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> draftsWithSignature, String account, DBLayerDeploy dbLayerDeploy,
            String versionId, String controllerId, Date deploymentDate) {
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
                        PublishUtils.mapInventoryMetaConfigurationType(ConfigurationType.fromValue(draft.getType())).intValue());
                newDeployedObject.setCommitId(versionId);
                newDeployedObject.setContent(draft.getContent());
                newDeployedObject.setSignedContent(draftsWithSignature.get(draft).getSignature());
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
            Map<DBItemInventoryConfiguration, JSObject> importedObjects, String account, DBLayerDeploy dbLayerDeploy, String controllerId,
            Date deploymentDate, String versionId) {
        Set<DBItemDeploymentHistory> deployedObjects = new HashSet<DBItemDeploymentHistory>();
        try {
            DBItemInventoryJSInstance controllerInstance = dbLayerDeploy.getController(controllerId);
            for (DBItemInventoryConfiguration draft : importedObjects.keySet()) {
                DBItemDeploymentHistory newDeployedObject = new DBItemDeploymentHistory();
                newDeployedObject.setAccount(account);
                // TODO: get Version to set here
                newDeployedObject.setVersion(null);
                newDeployedObject.setPath(draft.getPath());
                newDeployedObject.setFolder(draft.getFolder());
                newDeployedObject.setType(
                        PublishUtils.mapInventoryMetaConfigurationType(ConfigurationType.fromValue(draft.getType())).intValue());
                newDeployedObject.setCommitId(versionId);
                newDeployedObject.setContent(draft.getContent());
                newDeployedObject.setSignedContent(importedObjects.get(draft).getSignedContent());
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

    public static Set<DBItemDeploymentHistory> cloneDepHistoryItemsToRedeployed(
            Map<DBItemDeploymentHistory, DBItemDepSignatures> redeployedWithSignature, String account, DBLayerDeploy dbLayerDeploy,
            String versionId, String controllerId, Date deploymentDate) {
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
                redeployed.setCommitId(versionId);
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

    public static void prepareNextInvConfigGeneration(Set<DBItemInventoryConfiguration> drafts, SOSHibernateSession hibernateSession) {
        try {
            for (DBItemInventoryConfiguration draft : drafts) {
                draft.setDeployed(true);
                draft.setModified(Date.from(Instant.now()));
                hibernateSession.update(draft);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
    }

    public static DeployType mapInventoryMetaConfigurationType(ConfigurationType inventoryType) {
        switch (inventoryType) {
        case WORKFLOW:
            return DeployType.WORKFLOW;
        case AGENTCLUSTER:
            return DeployType.AGENTREF;
        case LOCK:
            return DeployType.LOCK;
        case JUNCTION:
            return DeployType.JUNCTION;
        default:
            return null;
        }
    }

    public static ConfigurationType mapDeployType(DeployType deployType) {
        switch (deployType) {
        case WORKFLOW:
            return ConfigurationType.WORKFLOW;
        case AGENTREF:
            return ConfigurationType.AGENTCLUSTER;
        case LOCK:
            return ConfigurationType.LOCK;
        case JUNCTION:
            return ConfigurationType.JUNCTION;
        default:
            return null;
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

    public static Set<SignaturePath> readZipFileContent(InputStream inputStream, Set<Workflow> workflows, Set<AgentRef> agentRefs
            /* , Set<Lock> locks */) throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException,
            IOException, JocUnsupportedFileTypeException, JocConfigurationException, DBOpenSessionException {
        Set<SignaturePath> signaturePaths = new HashSet<SignaturePath>();
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
                SignaturePath signaturePath = new SignaturePath();
                Signature signature = new Signature();
                if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
                    workflows.add(om.readValue(outBuffer.toString(), Workflow.class));
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.value())) {
                    if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.value())) {
                        signaturePath.setObjectPath("/" + entryName.substring(0, entryName.indexOf(
                                JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.value())));
                        signature.setSignatureString(outBuffer.toString());
                        signaturePath.setSignature(signature);
                        signaturePaths.add(signaturePath);
                    }
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.AGENT_REF_FILE_EXTENSION.value())) {
                    agentRefs.add(om.readValue(outBuffer.toString(), AgentRef.class));
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.AGENT_REF_SIGNATURE_FILE_EXTENSION.value())) {
                    signaturePath.setObjectPath("/" + entryName.substring(0, entryName.indexOf(
                            JSObjectFileExtension.AGENT_REF_SIGNATURE_FILE_EXTENSION.value())));
                    signature.setSignatureString(outBuffer.toString());
                    signaturePath.setSignature(signature);
                    signaturePaths.add(signaturePath);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.LOCK_FILE_EXTENSION.value())) {
                    // TODO: add processing for Locks, when Locks are ready
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.LOCK_SIGNATURE_FILE_EXTENSION.value())) {
                    // TODO: add processing for Locks, when Locks are ready
                }
            }
        } finally {
            if (zipStream != null) {
                try {
                    zipStream.close();
                } catch (IOException e) {}
            }
        }
        return signaturePaths;
    }

    public static Set<SignaturePath> readTarGzipFileContent(InputStream inputStream, Set<Workflow> workflows, Set<AgentRef> agentRefs
            /* , Set<Lock> locks */) throws DBConnectionRefusedException, DBInvalidDataException, SOSHibernateException,
            IOException, JocUnsupportedFileTypeException, JocConfigurationException, DBOpenSessionException {
        Set<SignaturePath> signaturePaths = new HashSet<SignaturePath>();
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
                SignaturePath signaturePath = new SignaturePath();
                Signature signature = new Signature();
                if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.value())) {
                    workflows.add(om.readValue(outBuffer.toString(), Workflow.class));
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.value())) {
                    if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.value())) {
                        signaturePath.setObjectPath("/" + entryName.substring(0, entryName.indexOf(
                                JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.value())));
                        signature.setSignatureString(outBuffer.toString());
                        signaturePath.setSignature(signature);
                        signaturePaths.add(signaturePath);
                    }
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.AGENT_REF_FILE_EXTENSION.value())) {
                    agentRefs.add(om.readValue(outBuffer.toString(), AgentRef.class));
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.AGENT_REF_SIGNATURE_FILE_EXTENSION.value())) {
                    signaturePath.setObjectPath("/" + entryName.substring(0, entryName.indexOf(
                            JSObjectFileExtension.AGENT_REF_SIGNATURE_FILE_EXTENSION.value())));
                    signature.setSignatureString(outBuffer.toString());
                    signaturePath.setSignature(signature);
                    signaturePaths.add(signaturePath);
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.LOCK_FILE_EXTENSION.value())) {
                    // TODO: add processing for Locks, when Locks are ready
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.LOCK_SIGNATURE_FILE_EXTENSION.value())) {
                    // TODO: add processing for Locks, when Locks are ready
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
        return signaturePaths;
    }

    public static StreamingOutput writeZipFile (Set<JSObject> jsObjects, String versionId) {
        StreamingOutput streamingOutput = new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException {
                ZipOutputStream zipOut = null;
                try {
                    zipOut = new ZipOutputStream(new BufferedOutputStream(output), StandardCharsets.UTF_8);
                    String content = null;
                    for (JSObject jsObject : jsObjects) {
                        String extension = null;
                        String signatureExtension = null;
                        switch(jsObject.getObjectType()) {
                        case WORKFLOW : 
                            extension = JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                            signatureExtension = JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.toString();
                            Workflow workflow = (Workflow)jsObject.getContent();
                            workflow.setVersionId(versionId);
                            content = om.writeValueAsString(workflow);
                            break;
                        case AGENTREF :
                            extension = JSObjectFileExtension.AGENT_REF_FILE_EXTENSION.toString();
                            signatureExtension = JSObjectFileExtension.AGENT_REF_SIGNATURE_FILE_EXTENSION.toString();
                            AgentRef agentRef = (AgentRef)jsObject.getContent();
                            agentRef.setVersionId(versionId);
                            content = om.writeValueAsString(agentRef);
                            break;
                        case LOCK :
                            extension = JSObjectFileExtension.LOCK_FILE_EXTENSION.toString();
                            signatureExtension = JSObjectFileExtension.LOCK_SIGNATURE_FILE_EXTENSION.toString();
                            // TODO:
//                            content = om.writeValueAsString((Lock)jsObject.getContent());
                            break;
                        case JUNCTION :
                            extension = JSObjectFileExtension.JUNCTION_FILE_EXTENSION.toString();
                            signatureExtension = JSObjectFileExtension.JUNCTION_SIGNATURE_FILE_EXTENSION.toString();
                            // TODO:
//                            content = om.writeValueAsString((Junction)jsObject.getContent());
                            break;
                        default:
                            extension = JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                            signatureExtension = JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.toString();
                        }
                        String zipEntryName = jsObject.getPath().substring(1).concat(extension); 
                        ZipEntry entry = new ZipEntry(zipEntryName);
                        zipOut.putNextEntry(entry);
                        zipOut.write(content.getBytes());
                        zipOut.closeEntry();
                        if (jsObject.getSignedContent() != null && !jsObject.getSignedContent().isEmpty()) {
                            String signatureZipEntryName = jsObject.getPath().substring(1).concat(signatureExtension);
                            ZipEntry signatureEntry = new ZipEntry(signatureZipEntryName);
                            zipOut.putNextEntry(signatureEntry);
                            zipOut.write(jsObject.getSignedContent().getBytes());
                            zipOut.closeEntry();
                        }
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
    
    public static StreamingOutput writeTarGzipFile (Set<JSObject> jsObjects, String versionId) {
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
                    for (JSObject jsObject : jsObjects) {
                        String extension = null;
                        String signatureExtension = null;
                        switch(jsObject.getObjectType()) {
                        case WORKFLOW : 
                            extension = JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                            signatureExtension = JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.toString();
                            Workflow workflow = (Workflow)jsObject.getContent();
                            workflow.setVersionId(versionId);
                            content = om.writeValueAsString(workflow);
                            break;
                        case AGENTREF :
                            extension = JSObjectFileExtension.AGENT_REF_FILE_EXTENSION.toString();
                            signatureExtension = JSObjectFileExtension.AGENT_REF_SIGNATURE_FILE_EXTENSION.toString();
                            AgentRef agentRef = (AgentRef)jsObject.getContent();
                            agentRef.setVersionId(versionId);
                            content = om.writeValueAsString(agentRef);
                            break;
                        case LOCK :
                            extension = JSObjectFileExtension.LOCK_FILE_EXTENSION.toString();
                            signatureExtension = JSObjectFileExtension.LOCK_SIGNATURE_FILE_EXTENSION.toString();
                            // TODO:
//                            content = om.writeValueAsString((Lock)jsObject.getContent());
                            break;
                        case JUNCTION :
                            extension = JSObjectFileExtension.JUNCTION_FILE_EXTENSION.toString();
                            signatureExtension = JSObjectFileExtension.JUNCTION_SIGNATURE_FILE_EXTENSION.toString();
                            // TODO:
//                            content = om.writeValueAsString((Junction)jsObject.getContent());
                            break;
                        default:
                            extension = JSObjectFileExtension.WORKFLOW_FILE_EXTENSION.toString();
                            signatureExtension = JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.toString();
                        }
                        String zipEntryName = jsObject.getPath().substring(1).concat(extension); 
                        TarArchiveEntry entry = new TarArchiveEntry(zipEntryName);
                        byte[] contentBytes = content.getBytes();
                        entry.setSize(contentBytes.length);
                        tarOut.putArchiveEntry(entry);
                        tarOut.write(contentBytes);
                        tarOut.closeArchiveEntry();
                        if (jsObject.getSignedContent() != null && !jsObject.getSignedContent().isEmpty()) {
                            String signatureZipEntryName = jsObject.getPath().substring(1).concat(signatureExtension);
                            TarArchiveEntry signatureEntry = new TarArchiveEntry(signatureZipEntryName);
                            tarOut.putArchiveEntry(signatureEntry);
                            tarOut.write(jsObject.getSignedContent().getBytes());
                            tarOut.closeArchiveEntry();
                        }
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

    public static Signature verifyAgentRefs(SOSHibernateSession hibernateSession, Set<SignaturePath> signaturePaths, AgentRef agentRef,
            String account) throws JocSignatureVerificationException, SOSHibernateException {
        SignaturePath signaturePath = signaturePaths.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath()
                .equals(agentRef.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
        DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
        Boolean verified = null;
        try {
            if (signaturePath != null && signaturePath.getSignature() != null) {
                JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.HIGH);
                String publicKey = keyPair.getPublicKey();
                verified = VerifySignature.verifyPGP(publicKey, om.writeValueAsString(agentRef), 
                        signaturePath.getSignature().getSignatureString());
                if (!verified) {
                    LOGGER.debug(String.format("signature verification for agentRef %1$s was not successful!", agentRef.getPath()));
                } 
            }
        } catch (IOException | PGPException  e) {
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
    
}