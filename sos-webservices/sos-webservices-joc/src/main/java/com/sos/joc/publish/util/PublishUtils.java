package com.sos.joc.publish.util;

import java.io.ByteArrayOutputStream;
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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
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
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
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
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.exceptions.JocSignatureVerificationException;
import com.sos.joc.exceptions.JocSosHibernateException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.exceptions.JocUnsupportedKeyTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.pgp.JocKeyAlgorythm;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.pgp.JocKeyType;
import com.sos.joc.model.publish.JSDeploymentState;
import com.sos.joc.model.publish.JSObject;
import com.sos.joc.model.publish.OperationType;
import com.sos.joc.model.publish.Signature;
import com.sos.joc.model.publish.SignaturePath;
import com.sos.joc.publish.common.JSObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpDownloadMapper;

import io.vavr.control.Either;
import js7.base.crypt.SignedString;
import js7.base.problem.Problem;
import js7.data.agent.AgentRefPath;
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
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PRIVATE.value(), keyPair.getPrivateKey(), keyPair.getCertificate(), account, secLvl);
            } else if (keyPair.getPublicKey() != null && keyPair.getCertificate() != null) {
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PUBLIC.value(), keyPair.getPublicKey(), keyPair.getCertificate(), account, secLvl);
            } else if (keyPair.getCertificate() != null) {
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PUBLIC.value(), keyPair.getCertificate(), account, secLvl);
            } else if (keyPair.getPublicKey() != null) {
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PUBLIC.value(), keyPair.getPublicKey(), account, secLvl);
            }
        }
    }

    public static void checkJocSecurityLevelAndStore(JocKeyPair keyPair, SOSHibernateSession hibernateSession, String account)
            throws SOSHibernateException, JocUnsupportedKeyTypeException, JocMissingRequiredParameterException {
        if (keyPair != null) {
            // Check forJocSecurityLevel commented, has to be introduced when the testing can be done with changing joc.properties
            if (keyPair.getPrivateKey() != null && Globals.getJocSecurityLevel().equals(JocSecurityLevel.MEDIUM)) {
                if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PUBLIC_KEY_HEADER) || keyPair.getPrivateKey().startsWith(
                        SOSPGPConstants.PUBLIC_PGP_KEY_HEADER) || keyPair.getPrivateKey().startsWith(SOSPGPConstants.PUBLIC_RSA_KEY_HEADER)) {
                    throw new JocUnsupportedKeyTypeException("Wrong key type. expected: private | received: public");
                }
                storeKey(keyPair, hibernateSession, account, JocSecurityLevel.MEDIUM);
            } else if (keyPair.getPublicKey() != null && Globals.getJocSecurityLevel().equals(JocSecurityLevel.HIGH)) {
                if (keyPair.getPublicKey().startsWith(SOSPGPConstants.PRIVATE_KEY_HEADER) || keyPair.getPublicKey().startsWith(
                        SOSPGPConstants.PRIVATE_PGP_KEY_HEADER) || keyPair.getPublicKey().startsWith(SOSPGPConstants.PRIVATE_RSA_KEY_HEADER)) {
                    throw new JocUnsupportedKeyTypeException("Wrong key type. expected: public | received: private");
                }
                storeKey(keyPair, hibernateSession, account, JocSecurityLevel.HIGH);
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

    public static void signDrafts(String versionId, String account, Set<DBItemInventoryConfiguration> unsignedDrafts, SOSHibernateSession session,
            JocSecurityLevel secLvl) throws JocMissingKeyException, JsonParseException, JsonMappingException, SOSHibernateException, IOException,
            PGPException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account, secLvl);
        signDrafts(versionId, account, unsignedDrafts, keyPair, session);
    }

    public static void signDrafts(String versionId, String account, Set<DBItemInventoryConfiguration> unsignedDrafts, JocKeyPair keyPair,
            SOSHibernateSession session) throws JocMissingKeyException, JsonParseException, JsonMappingException, SOSHibernateException, IOException,
            PGPException, NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        boolean isPGPKey = false;
        if (keyPair.getPrivateKey() == null || keyPair.getPrivateKey().isEmpty()) {
            throw new JocMissingKeyException("No private key found fo signing!");
        } else {
            if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER)) {
                isPGPKey = true;
            }
            for (DBItemInventoryConfiguration draft : unsignedDrafts) {
                updateVersionIdOnDraftObject(draft, versionId, session);
                if (isPGPKey) {
                    // TODO: uncomment when draft is refactored
                    // draft.setSignedContent(SignObject.signPGP(keyPair.getPrivateKey(), draft.getContent(), null));
                } else {
                    KeyPair kp = null;
                    if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_RSA_KEY_HEADER)) {
                        kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(keyPair.getPrivateKey());
                    } else {
                        kp = KeyUtil.getKeyPairFromPrivatKeyString(keyPair.getPrivateKey());
                    }
                    // TODO: uncomment when draft is refactored
                    // draft.setSignedContent(SignObject.signX509(kp.getPrivate(), draft.getContent()));
                }
            }
        }
    }

    public static Map<DBItemInventoryConfiguration, DBItemDepSignatures> getDraftsWithSignature(String versionId, String account,
            Set<DBItemInventoryConfiguration> unsignedDrafts, SOSHibernateSession session, JocSecurityLevel secLvl) throws JocMissingKeyException,
            JsonParseException, JsonMappingException, SOSHibernateException, IOException, PGPException, NoSuchAlgorithmException,
            InvalidKeySpecException, InvalidKeyException, SignatureException {
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
            InvalidKeySpecException, InvalidKeyException, SignatureException {
        boolean isPGPKey = false;
        Map<DBItemInventoryConfiguration, DBItemDepSignatures> signedDrafts = new HashMap<DBItemInventoryConfiguration, DBItemDepSignatures>();
        if (keyPair.getPrivateKey() == null || keyPair.getPrivateKey().isEmpty()) {
            throw new JocMissingKeyException("No private key found fo signing! - Please check your private key from the key management section in your profile.");
        } else {
            if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER)) {
                isPGPKey = true;
            }
            DBItemDepSignatures sig = null;
            for (DBItemInventoryConfiguration draft : unsignedDrafts) {
                updateVersionIdOnDraftObject(draft, versionId, session);
                if (isPGPKey) {
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

    public static Map<DBItemDeploymentHistory, DBItemDepSignatures> getDeploymentsWithSignature(String versionId, String account,
            Set<DBItemDeploymentHistory> depHistoryToRedeploy, SOSHibernateSession session, JocSecurityLevel secLvl) throws JocMissingKeyException,
            JsonParseException, JsonMappingException, SOSHibernateException, IOException, PGPException, NoSuchAlgorithmException,
            InvalidKeySpecException, InvalidKeyException, SignatureException {
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
        boolean isPGPKey = false;
        Map<DBItemDeploymentHistory, DBItemDepSignatures> signedReDeployable = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
        if (keyPair.getPrivateKey() == null || keyPair.getPrivateKey().isEmpty()) {
            throw new JocMissingKeyException("No private key found for signing! - Please check your private key from the key management section in your profile.");
        } else {
            if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER)) {
                isPGPKey = true;
            }
            DBItemDepSignatures sig = null;
            for (DBItemDeploymentHistory deployed : depHistoryToRedeploy) {
                updateVersionIdOnDeployedObject(deployed, versionId, session);
                if (isPGPKey) {
                    sig = new DBItemDepSignatures();
                    sig.setAccount(account);
                    sig.setInvConfigurationId(deployed.getId());
                    sig.setModified(Date.from(Instant.now()));
                    sig.setSignature(SignObject.signPGP(keyPair.getPrivateKey(), deployed.getContent(), null));
                    signedReDeployable.put(deployed, sig);
                } else {
                    KeyPair kp = null;
                    if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_RSA_KEY_HEADER)) {
                        kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(keyPair.getPrivateKey());
                    } else {
                        kp = KeyUtil.getKeyPairFromPrivatKeyString(keyPair.getPrivateKey());
                    }
                    sig = new DBItemDepSignatures();
                    sig.setAccount(account);
                    sig.setInvConfigurationId(deployed.getId());
                    sig.setModified(Date.from(Instant.now()));
                    sig.setSignature(SignObject.signX509(kp.getPrivate(), deployed.getContent()));
                    signedReDeployable.put(deployed, sig);
                }
                if (sig != null) {
                    session.save(sig);
                }
            }
        }
        return signedReDeployable;
    }

    public static Set<DBItemInventoryConfiguration> verifySignatures(String account, Set<DBItemInventoryConfiguration> signedDrafts,
            SOSHibernateSession session, JocSecurityLevel secLvl) throws SOSHibernateException, IOException, PGPException, InvalidKeyException,
            CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, JocMissingKeyException, SignatureException,
            NoSuchProviderException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account, secLvl);
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

    public static DBItemInventoryConfiguration verifySignature(String account, DBItemInventoryConfiguration signedDraft,
            DBItemDepSignatures draftSignature, SOSHibernateSession session, JocSecurityLevel secLvl) throws SOSHibernateException, IOException,
            PGPException, InvalidKeyException, CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, JocMissingKeyException,
            SignatureException, NoSuchProviderException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account, secLvl);
        if (keyPair != null) {
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
                if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER)) {
                    return verifyPGPSignature(account, signedDeployments, draftSignature, keyPair);
                } else {
                    return verifyRSASignature(signedDeployments, draftSignature, keyPair);
                }
            } else if (keyPair.getPublicKey() != null) {
                if (keyPair.getPublicKey().startsWith(SOSPGPConstants.PUBLIC_PGP_KEY_HEADER)) {
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
            JocKeyPair keyPair) throws SOSHibernateException, IOException, PGPException {
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
            // verified = VerifySignature.verifyPGP(publicKey, draft.getContent(), draft.getSignedContent());
            if (!verified) {
                LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", draft.getPath()));
            } else {
                verifiedDrafts.add(draft);
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
            LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDeployment.getPath()));
        } else {
            verifiedDeployment = signedDeployment;
        }
        return verifiedDeployment;
    }

    public static Set<DBItemInventoryConfiguration> verifyRSASignatures(Set<DBItemInventoryConfiguration> signedDrafts, JocKeyPair jocKeyPair)
            throws CertificateException, NoSuchAlgorithmException, InvalidKeySpecException, JocMissingKeyException, InvalidKeyException,
            SignatureException, NoSuchProviderException, IOException {
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
                // verified = VerifySignature.verifyX509(cert, draft.getContent(), draft.getSignedContent());
                if (!verified) {
                    LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", draft.getPath()));
                } else {
                    verifiedDrafts.add(draft);
                }
            }
        } else if (publicKey != null) {
            for (DBItemInventoryConfiguration draft : signedDrafts) {
                // TODO: uncomment when draft is refactored
                // verified = VerifySignature.verifyX509(publicKey, draft.getContent(), draft.getSignedContent());
                if (!verified) {
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
            if (!verified) {
                LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDraft.getPath()));
            } else {
                verifiedDraft = signedDraft;
            }
        } else if (publicKey != null) {
            verified = VerifySignature.verifyX509(publicKey, signedDraft.getContent(), draftSignature.getSignature());
            if (!verified) {
                LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDraft.getPath()));
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
            verified = VerifySignature.verifyX509(cert, signedDeployment.getContent(), deployedSignature.getSignature());
            if (!verified) {
                LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDeployment
                        .getPath()));
            } else {
                verifiedDeployment = signedDeployment;
            }
        } else if (publicKey != null) {
            verified = VerifySignature.verifyX509(publicKey, signedDeployment.getContent(), deployedSignature.getSignature());
            if (!verified) {
                LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDeployment
                        .getPath()));
            } else {
                verifiedDeployment = signedDeployment;
            }
        } else {
            throw new JocMissingKeyException("Neither PublicKey nor Certificate found for signature verification.");
        }
        return verifiedDeployment;
    }

    public static CompletableFuture<Either<Problem, Void>> updateRepoAddOrUpdate(
            String versionId, 
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> drafts, 
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, 
            String controllerId, 
            DBLayerDeploy dbLayer) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {

        Set<JUpdateRepoOperation> updateRepoOperations = new HashSet<JUpdateRepoOperation>();
        if (drafts != null) {
            for (DBItemInventoryConfiguration draft : drafts.keySet()) {
                if (draft != null) {
                    SignedString signedString = SignedString.of(draft.getContent(), "PGP", drafts.get(draft).getSignature());
                    JUpdateRepoOperation operation = JUpdateRepoOperation.addOrReplace(signedString);
                    updateRepoOperations.add(operation);
                }
            }
        }
        if (alreadyDeployed != null) {
            for (DBItemDeploymentHistory reDeploy : alreadyDeployed.keySet()) {
                if (reDeploy != null) {
                    SignedString signedString = SignedString.of(reDeploy.getContent(), "PGP", alreadyDeployed.get(reDeploy).getSignature());
                    JUpdateRepoOperation operation = JUpdateRepoOperation.addOrReplace(signedString);
                    updateRepoOperations.add(operation);
                }
            }
        }
        return ControllerApi.of(controllerId).updateRepo(VersionId.of(versionId), Flux.fromIterable(updateRepoOperations));
    }

    public static CompletableFuture<Either<Problem, Void>> updateRepoDelete(
            String versionId, 
            List<DBItemDeploymentHistory> alreadyDeployedtoDelete, 
            String controllerId, 
            DBLayerDeploy dbLayer) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {

        Set<JUpdateRepoOperation> updateRepoOperations = new HashSet<JUpdateRepoOperation>();
        if (alreadyDeployedtoDelete != null) {
            for (DBItemDeploymentHistory toDelete : alreadyDeployedtoDelete) {
                switch (DeployType.fromValue(toDelete.getType())) {
                case WORKFLOW:
                    updateRepoOperations.add(JUpdateRepoOperation.delete(WorkflowPath.of(toDelete.getPath())));
                    break;
                case AGENTREF:
                    updateRepoOperations.add(JUpdateRepoOperation.delete(AgentRefPath.of(toDelete.getPath())));
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
            String versionId, 
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> drafts, 
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed,
            List<DBItemDeploymentHistory> alreadyDeployedtoDelete, 
            String controllerId, 
            DBLayerDeploy dbLayer) 
                    throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {

        Set<JUpdateRepoOperation> updateRepoOperations = new HashSet<JUpdateRepoOperation>();
        if (drafts != null) {
            for (DBItemInventoryConfiguration draft : drafts.keySet()) {
                if (draft != null) {
                    SignedString signedString = SignedString.of(draft.getContent(), "PGP", drafts.get(draft).getSignature());
                    JUpdateRepoOperation operation = JUpdateRepoOperation.addOrReplace(signedString);
                    updateRepoOperations.add(operation);
                }
            }
        }
        if (alreadyDeployed != null) {
            for (DBItemDeploymentHistory reDeploy : alreadyDeployed.keySet()) {
                if (reDeploy != null) {
                    SignedString signedString = SignedString.of(reDeploy.getContent(), "PGP", alreadyDeployed.get(reDeploy).getSignature());
                    JUpdateRepoOperation operation = JUpdateRepoOperation.addOrReplace(signedString);
                    updateRepoOperations.add(operation);
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
                    updateRepoOperations.add(JUpdateRepoOperation.delete(AgentRefPath.of(toDelete.getPath())));
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
            deployed.setId(null);
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
        deployed.setId(null);
    }

    public static Set<DBItemDeploymentHistory> cloneInvConfigurationsToDepHistoryItems(
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> draftsWithSignature, String account, DBLayerDeploy dbLayerDeploy, String versionId,
            String controllerId, Date deploymentDate) {
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
                newDeployedObject.setType(PublishUtils.mapInventoryMetaConfigurationType(ConfigurationType.fromValue(draft.getType())).intValue());
                newDeployedObject.setCommitId(versionId);
                newDeployedObject.setContent(draft.getContent());
                newDeployedObject.setSignedContent(draftsWithSignature.get(draft).getSignature());
                newDeployedObject.setDeploymentDate(deploymentDate);
                newDeployedObject.setControllerInstanceId(controllerInstance.getId());
                newDeployedObject.setControllerId(controllerId);
                newDeployedObject.setInventoryConfigurationId(draft.getId());
                newDeployedObject.setOperation(OperationType.UPDATE.value());
                newDeployedObject.setState(JSDeploymentState.DEPLOYED.value());
                dbLayerDeploy.getSession().save(newDeployedObject);
                deployedObjects.add(newDeployedObject);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return deployedObjects;
    }

    public static Set<DBItemDeploymentHistory> cloneInvConfigurationsToDepHistoryItems(Map<DBItemInventoryConfiguration, JSObject> importedObjects,
            String account, DBLayerDeploy dbLayerDeploy, String controllerId, Date deploymentDate, String versionId) {
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
                newDeployedObject.setType(PublishUtils.mapInventoryMetaConfigurationType(ConfigurationType.fromValue(draft.getType())).intValue());
                newDeployedObject.setCommitId(versionId);
                newDeployedObject.setContent(draft.getContent());
                newDeployedObject.setSignedContent(importedObjects.get(draft).getSignedContent());
                newDeployedObject.setDeploymentDate(deploymentDate);
                newDeployedObject.setControllerInstanceId(controllerInstance.getId());
                newDeployedObject.setControllerId(controllerId);
                newDeployedObject.setInventoryConfigurationId(draft.getId());
                newDeployedObject.setOperation(OperationType.UPDATE.value());
                newDeployedObject.setState(JSDeploymentState.DEPLOYED.value());
                dbLayerDeploy.getSession().save(newDeployedObject);
                deployedObjects.add(newDeployedObject);
            }
        } catch (SOSHibernateException e) {
            throw new JocSosHibernateException(e);
        }
        return deployedObjects;
    }

    public static Set<DBItemDeploymentHistory> cloneDepHistoryItemsToRedeployed(
            Map<DBItemDeploymentHistory, DBItemDepSignatures> redeployedWithSignature, String account, DBLayerDeploy dbLayerDeploy, String versionId,
            String controllerId, Date deploymentDate) {
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
                redeployed.setState(JSDeploymentState.DEPLOYED.value());
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
                delete.setState(JSDeploymentState.DEPLOYED.value());
                delete.setDeletedDate(Date.from(Instant.now()));
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

    public static <T extends DBItem> void checkPathRenamingForUpdate(Set<T> verifiedObjects, String controllerId, DBLayerDeploy dbLayer)
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
                updateRepoDelete(versionId, alreadyDeployedToDelete, controllerId, dbLayer);
                Set<DBItemDeploymentHistory> deletedDeployItems = PublishUtils.updateDeletedDepHistory(alreadyDeployedToDelete, dbLayer);
                LOGGER.debug(String.format("%1$d item(s) deleted from controller '%2$s':", deletedDeployItems.size(), controllerId));
                deletedDeployItems.stream().map(path -> path.getPath()).forEach(path -> {
                    LOGGER.trace(String.format("Object '%1$s' deleted from controller '%2$s'", path, controllerId));
                });
                
                
            }
        }
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

    public static Signature verifyWorkflows(SOSHibernateSession hibernateSession, Set<SignaturePath> signaturePaths, Workflow workflow, String account)
            throws JocSignatureVerificationException, SOSHibernateException {
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
                    if (publicKey.startsWith(SOSPGPConstants.PUBLIC_PGP_KEY_HEADER)) {
                        verified = VerifySignature.verifyPGP(publicKey, 
                                om.writeValueAsString(workflow), signaturePath.getSignature().getSignatureString());
                    } else if (publicKey.startsWith(SOSPGPConstants.PUBLIC_RSA_KEY_HEADER) 
                            || publicKey.startsWith(SOSPGPConstants.PUBLIC_KEY_HEADER)) {
                        PublicKey pubKey = KeyUtil.getPublicKeyFromString(publicKey); 
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

    public static Signature verifyAgentRefs(SOSHibernateSession hibernateSession, Set<SignaturePath> signaturePaths, AgentRef agentRef, String account)
            throws JocSignatureVerificationException, SOSHibernateException {
        SignaturePath signaturePath = signaturePaths.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath()
                .equals(agentRef.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
        DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
        Boolean verified = null;
        try {
            if (signaturePath != null && signaturePath.getSignature() != null) {
                JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.HIGH);
                String publicKey = keyPair.getPublicKey();
                verified = VerifySignature.verifyPGP(publicKey, om.writeValueAsString(agentRef), signaturePath.getSignature().getSignatureString());
                if (!verified) {
                    LOGGER.debug(String.format("signature verification for agentRef %1$s was not successful!", agentRef.getPath()));
                } 
            }
        } catch (IOException | PGPException  e) {
            throw new JocSignatureVerificationException(e);
        }
        return signaturePath.getSignature();
    }

}