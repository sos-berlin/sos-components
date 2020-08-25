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
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

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
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.deployment.DBItemDepSignatures;
import com.sos.joc.db.deployment.DBItemDeploymentHistory;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.InventoryMeta;
import com.sos.joc.exceptions.JocMissingKeyException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocNotImplementedException;
import com.sos.joc.exceptions.JocUnsupportedKeyTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.pgp.JocKeyAlgorythm;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.pgp.JocKeyType;
import com.sos.joc.model.publish.JSDeploymentState;
import com.sos.joc.model.publish.OperationType;
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
    
    public static void storeKey(JocKeyPair keyPair, SOSHibernateSession hibernateSession, String account)  throws SOSHibernateException {
        DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
        if (keyPair != null) {
            if (keyPair.getPrivateKey() != null && keyPair.getCertificate() != null) {
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PRIVATE.value(), keyPair.getPrivateKey(), keyPair.getCertificate(), account);
            } else if (keyPair.getPublicKey() != null && keyPair.getCertificate() != null) {
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PUBLIC.value(), keyPair.getPublicKey(), keyPair.getCertificate(), account);
            } else if (keyPair.getPrivateKey() != null) {
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PRIVATE.value(), keyPair.getPrivateKey(), account);
            } else if (keyPair.getCertificate() != null) {
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PUBLIC.value(), keyPair.getCertificate(), account);
            }else if (keyPair.getPublicKey() != null) {
                dbLayerKeys.saveOrUpdateKey(JocKeyType.PUBLIC.value(), keyPair.getPublicKey(), account);
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
                updateVersionIdOnDraftObject(draft, versionId, session);
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
        if (keyPair != null) {
            return getDraftsWithSignature(versionId, account, unsignedDrafts, keyPair, session);
        } else {
            throw new JocMissingKeyException("No Key found for this account.");
        }
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
                updateVersionIdOnDraftObject(draft, versionId, session);
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
    
    public static Map<DBItemDeploymentHistory, DBItemDepSignatures> getDeploymentsWithSignature(String versionId, String account, 
            Set<DBItemDeploymentHistory> depHistoryToRedeploy, SOSHibernateSession session) throws JocMissingKeyException, JsonParseException, 
            JsonMappingException, SOSHibernateException, IOException, PGPException, NoSuchAlgorithmException, InvalidKeySpecException, 
            InvalidKeyException, SignatureException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account);
        if (keyPair != null) {
            return getDeploymentsWithSignature(versionId, account, depHistoryToRedeploy, keyPair, session);
        } else {
            throw new JocMissingKeyException("No Key found for this account.");
        }
    }
    
    public static Map<DBItemDeploymentHistory, DBItemDepSignatures> getDeploymentsWithSignature(
            String versionId, String account, Set<DBItemDeploymentHistory> depHistoryToRedeploy, JocKeyPair keyPair, SOSHibernateSession session)
                    throws JocMissingKeyException, JsonParseException, JsonMappingException, SOSHibernateException, IOException, PGPException,
                    NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
        boolean isPGPKey = false;
        Map<DBItemDeploymentHistory, DBItemDepSignatures> signedReDeployable = new HashMap<DBItemDeploymentHistory, DBItemDepSignatures>();
        if(keyPair.getPrivateKey() == null || keyPair.getPrivateKey().isEmpty()) {
            throw new JocMissingKeyException("No private key found for signing!");
        } else {
            if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER)) {
                isPGPKey = true;
            }
            DBItemDepSignatures sig = null;
            for (DBItemDeploymentHistory deployed : depHistoryToRedeploy) {
                updateVersionIdOnDeployedObject(deployed, versionId, session);
                if(isPGPKey) {
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

    public static DBItemDeploymentHistory verifySignature(
            String account, DBItemDeploymentHistory signedDeployments, DBItemDepSignatures draftSignature, SOSHibernateSession session)
            throws SOSHibernateException, IOException, PGPException, InvalidKeyException, CertificateException, NoSuchAlgorithmException,
            InvalidKeySpecException, JocMissingKeyException, SignatureException, NoSuchProviderException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account);
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
    
    public static DBItemDeploymentHistory verifyPGPSignature(
            String account, DBItemDeploymentHistory signedDeployment, DBItemDepSignatures deployedSignature, JocKeyPair keyPair)
            throws SOSHibernateException, IOException, PGPException {
        DBItemDeploymentHistory verifiedDeployment = null;
        String publicKey = null;
        if (keyPair.getPublicKey() == null) {
            publicKey = KeyUtil.extractPublicKey(keyPair.getPrivateKey());
        } else {
            publicKey = keyPair.getPublicKey();
        }
        Boolean verified = false;
        verified = VerifySignature.verifyPGP(publicKey, signedDeployment.getContent(), deployedSignature.getSignature());
        if(!verified) {
            LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDeployment.getPath()));
        } else {
            verifiedDeployment = signedDeployment;
        }
        return verifiedDeployment;
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
            if(!verified) {
                LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDeployment.getPath()));
            } else {
                verifiedDeployment =signedDeployment;
            }
        } else if (publicKey != null) {
            verified = VerifySignature.verifyX509(publicKey, signedDeployment.getContent(), deployedSignature.getSignature());
            if(!verified) {
                LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", signedDeployment.getPath()));
            } else {
                verifiedDeployment = signedDeployment;
            }
        } else {
            throw new JocMissingKeyException("Neither PublicKey nor Certificate found for signature verification.");
        }
        return verifiedDeployment;
    }

    public static Either<Problem, Void> updateRepo(
            String versionId, Map<DBItemInventoryConfiguration, DBItemDepSignatures> drafts, 
            Map<DBItemDeploymentHistory, DBItemDepSignatures> alreadyDeployed, List<DBItemDeploymentHistory> alreadyDeployedtoDelete,
            String controllerId, DBLayerDeploy dbLayer) throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException{
        
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
                switch(getDeployTypeFromOrdinal(toDelete.getObjectType())) {
                    case WORKFLOW:
                        updateRepoOperations.add(JUpdateRepoOperation.delete(WorkflowPath.of(toDelete.getPath())));
                        break;
                    case AGENT_REF:
                        updateRepoOperations.add(JUpdateRepoOperation.delete(AgentRefPath.of(toDelete.getPath())));
                        break;
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
        
        CompletableFuture<Either<Problem, Void>> future = 
                Proxy.of(controllerId).api().updateRepo(VersionId.of(versionId), Flux.fromIterable(updateRepoOperations));
        Either<Problem, Void> either = future.get(Globals.httpSocketTimeout, TimeUnit.SECONDS);
        return either;
//        if (either.isLeft()) {
//            
//            throw new JocUpdateRepoException(either.getLeft().message(), either.getLeft().throwable());
//        }
    }
    
    private static void updateVersionIdOnDraftObject(DBItemInventoryConfiguration draft, String versionId, SOSHibernateSession session)
            throws JsonParseException, JsonMappingException, IOException, SOSHibernateException, JocNotImplementedException {
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
            case CALENDAR:
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
        
        switch(DeployType.values()[deployed.getObjectType()]) {
            case WORKFLOW:
                Workflow workflow = om.readValue(deployed.getContent(), Workflow.class);
                workflow.setVersionId(versionId);
                deployed.setContent(om.writeValueAsString(workflow));
                deployed.setId(null);
                break;
            case AGENT_REF:
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
            Map<DBItemInventoryConfiguration, DBItemDepSignatures> draftsWithSignature, String account, 
            SOSHibernateSession hibernateSession, String versionId, Long controllerInstanceId, Date deploymentDate)
                    throws SOSHibernateException {
        DBItemInventoryJSInstance controllerInstance = hibernateSession.get(DBItemInventoryJSInstance.class, controllerInstanceId);
        Set<DBItemDeploymentHistory> deployedObjects = new HashSet<DBItemDeploymentHistory>();
        for (DBItemInventoryConfiguration draft : draftsWithSignature.keySet()) {
            DBItemDeploymentHistory newDeployedObject = new DBItemDeploymentHistory();
            newDeployedObject.setAccount(account);
            // TODO: get Version to set here
            newDeployedObject.setVersion(null);
            newDeployedObject.setPath(draft.getPath());
            newDeployedObject.setFolder(draft.getFolder());
            newDeployedObject.setObjectType(PublishUtils.mapInventoryMetaConfigurationType(
                    InventoryMeta.ConfigurationType.fromValue(draft.getType())).ordinal());
            newDeployedObject.setCommitId(versionId);
            newDeployedObject.setContent(draft.getContent());
            newDeployedObject.setSignedContent(draftsWithSignature.get(draft).getSignature());
            newDeployedObject.setDeploymentDate(deploymentDate);
            newDeployedObject.setControllerInstanceId(controllerInstanceId);
            newDeployedObject.setControllerId(controllerInstance.getSchedulerId());
            newDeployedObject.setInventoryConfigurationId(draft.getId());
            newDeployedObject.setOperation(OperationType.UPDATE.value());
            newDeployedObject.setState(JSDeploymentState.DEPLOYED.value());
            hibernateSession.save(newDeployedObject);
            hibernateSession.delete(draftsWithSignature.get(draft));
            deployedObjects.add(newDeployedObject);
        }
        return deployedObjects;
    }
    
    public static Set<DBItemDeploymentHistory> cloneDepHistoryItemsToRedeployed(
            Map<DBItemDeploymentHistory, DBItemDepSignatures> redeployedWithSignature, String account, 
            SOSHibernateSession hibernateSession, String versionId, Long controllerInstanceId, Date deploymentDate)
                    throws SOSHibernateException {
        DBItemInventoryJSInstance controllerInstance = hibernateSession.get(DBItemInventoryJSInstance.class, controllerInstanceId);
        Set<DBItemDeploymentHistory> deployedObjects = new HashSet<DBItemDeploymentHistory>();
        for (DBItemDeploymentHistory redeployed : redeployedWithSignature.keySet()) {
            redeployed.setSignedContent(redeployedWithSignature.get(redeployed).getSignature());
            redeployed.setId(null);
            redeployed.setAccount(account);
            // TODO: get Version to set here
            redeployed.setVersion(null);
            redeployed.setCommitId(versionId);
            redeployed.setControllerId(controllerInstance.getSchedulerId());
            redeployed.setControllerInstanceId(controllerInstanceId);
            redeployed.setDeploymentDate(deploymentDate);
            redeployed.setOperation(OperationType.UPDATE.value());
            redeployed.setState(JSDeploymentState.DEPLOYED.value());
            hibernateSession.save(redeployed);
            hibernateSession.delete(redeployedWithSignature.get(redeployed));
            deployedObjects.add(redeployed);
        }
        return deployedObjects;
    }
    
    public static Set<DBItemDeploymentHistory> updateDeletedDepHistory(
            List<DBItemDeploymentHistory> toDelete, DBLayerDeploy dbLayer) throws SOSHibernateException {
        Set<DBItemDeploymentHistory> deletedObjects = new HashSet<DBItemDeploymentHistory>();
        for (DBItemDeploymentHistory delete : toDelete) {
            delete.setId(null);
            delete.setOperation(OperationType.DELETE.value());
            delete.setState(JSDeploymentState.DEPLOYED.value());
            delete.setDeletedDate(Date.from(Instant.now()));
            delete.setDeploymentDate(Date.from(Instant.now()));
            dbLayer.getSession().save(delete);            
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

    public static DeployType mapInventoryMetaConfigurationType(InventoryMeta.ConfigurationType inventoryType) {
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
                return null;
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
                return null;
        }
    }
    
    public static <T extends DBItem> void checkPathRenamingForUpdate(Set<T> verifiedObjects, Long controllerInstanceId, DBLayerDeploy dbLayer)
            throws SOSException, IOException, InterruptedException, ExecutionException, TimeoutException {
        DBItemDeploymentHistory depHistory = null;
        DBItemInventoryConfiguration invConf = null;
        final String versionId = UUID.randomUUID().toString();
        // check first if a deploymentHistory item related to the configuration item exist
        DBItemInventoryJSInstance controller = dbLayer.getSession().get(DBItemInventoryJSInstance.class, controllerInstanceId);
        List<DBItemDeploymentHistory> alreadyDeployedToDelete = new ArrayList<DBItemDeploymentHistory>();
        for (T object : verifiedObjects) {
            if (DBItemInventoryConfiguration.class.isInstance(object)) {
                invConf = (DBItemInventoryConfiguration)object;
                depHistory = dbLayer.getLatestDepHistoryItem(invConf, controller.getSchedulerId());
                if (depHistory != null && OperationType.DELETE.equals(OperationType.fromValue(depHistory.getOperation()))) {
                    depHistory = null;
                }
            } else {
                depHistory = (DBItemDeploymentHistory)object;
                invConf = dbLayer.getSession().get(DBItemInventoryConfiguration.class, depHistory.getInventoryConfigurationId());
            }
            // if so, check if the paths of both are the same
            if (depHistory != null && !depHistory.getPath().equals(((DBItemInventoryConfiguration)object).getPath())) {
                // if not, delete the old deployed item via updateRepo before deploy of the new configuration
                depHistory.setCommitId(versionId);
                alreadyDeployedToDelete.add(depHistory);
                updateRepo(versionId, null, null, alreadyDeployedToDelete, controller.getSchedulerId(), dbLayer);
                Set<DBItemDeploymentHistory> deletedDeployItems = PublishUtils.updateDeletedDepHistory(alreadyDeployedToDelete, dbLayer);
            }
        }
    }
        
}