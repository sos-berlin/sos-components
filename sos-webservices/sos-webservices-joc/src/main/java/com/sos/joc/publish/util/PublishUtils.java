package com.sos.joc.publish.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.UriBuilderException;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.db.deployment.DBItemDeployedConfiguration;
import com.sos.joc.db.inventory.deprecated.DBItemInventoryConfiguration;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.agent.DeleteAgentRef;
import com.sos.jobscheduler.model.command.UpdateRepo;
import com.sos.jobscheduler.model.deploy.DeleteObject;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.workflow.DeleteWorkflow;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCJsonCommand;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingKeyException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedKeyTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.pgp.JocKeyType;
import com.sos.joc.model.pgp.JocKeyAlgorythm;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.publish.Signature;
import com.sos.joc.model.publish.SignedObject;
import com.sos.commons.sign.pgp.SOSPGPConstants;
import com.sos.commons.sign.pgp.key.KeyUtil;
import com.sos.commons.sign.pgp.sign.SignObject;
import com.sos.commons.sign.pgp.verify.VerifySignature;

public abstract class PublishUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishUtils.class);

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
            throws SOSHibernateException, JocMissingKeyException, IOException, PGPException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account);
        signDrafts(versionId, account, unsignedDrafts, keyPair, session);
    }
    
    public static void signDraftsDefault(String versionId, String account, Set<DBItemInventoryConfiguration> unsignedDrafts, SOSHibernateSession session)
            throws SOSHibernateException, JocMissingKeyException, IOException, PGPException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getDefaultKeyPair(account);
        signDrafts(versionId, account, unsignedDrafts, keyPair, session);
    }
    
    public static void signDrafts(
            String versionId, String account, Set<DBItemInventoryConfiguration> unsignedDrafts, JocKeyPair keyPair, SOSHibernateSession session)
            throws SOSHibernateException, JocMissingKeyException, IOException, PGPException {
        if(keyPair.getPrivateKey() == null || keyPair.getPrivateKey().isEmpty()) {
            throw new JocMissingKeyException("No private key found fo signing!");
        } else {
            for (DBItemInventoryConfiguration draft : unsignedDrafts) {
                updateVersionIdOnObject(draft, versionId, session);
                draft.setSignedContent(SignObject.sign(keyPair.getPrivateKey(), draft.getContent(), null));
            }
        }
    }
    
    public static Set<DBItemInventoryConfiguration> verifySignatures(
            String account, Set<DBItemInventoryConfiguration> signedDrafts, SOSHibernateSession session)
            throws SOSHibernateException, IOException, PGPException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getKeyPair(account);
        return verifySignatures(account, signedDrafts, keyPair);
    }

    public static Set<DBItemInventoryConfiguration> verifySignaturesDefault(
            String account, Set<DBItemInventoryConfiguration> signedDrafts, SOSHibernateSession session)
            throws SOSHibernateException, IOException, PGPException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        JocKeyPair keyPair = dbLayer.getDefaultKeyPair(account);
        return verifySignatures(account, signedDrafts, keyPair);
    }

    public static Set<DBItemInventoryConfiguration> verifySignatures(
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
            verified = VerifySignature.verify(publicKey, draft.getContent(), draft.getSignedContent());
            if(!verified) {
                LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", draft.getPath()));
            } else {
                verifiedDrafts.add(draft);
            }
        }
        return verifiedDrafts;
    }

    public static void updateRepo(
            String versionId, Set<DBItemInventoryConfiguration> drafts, List<DBItemInventoryConfiguration> draftsToDelete,
            String masterUrl, String masterJobschedulerId)
            throws IllegalArgumentException, UriBuilderException, SOSException, JocException, IOException {
        UpdateRepo updateRepo = new UpdateRepo();
        updateRepo.setVersionId(versionId);
        for (DBItemInventoryConfiguration draft : drafts) {
            SignedObject signedObject = new SignedObject();
            signedObject.setString(draft.getContent());
            Signature signature = new Signature();
            signature.setSignatureString(draft.getSignedContent());
            signedObject.setSignature(signature);
            updateRepo.getChange().add(signedObject);
        }
        for (DBItemInventoryConfiguration draftToDelete : draftsToDelete) {
            DeleteObject deletedObject = null;
            switch(DeployType.fromValue(draftToDelete.getObjectType())) {
                case WORKFLOW:
                    deletedObject = new DeleteWorkflow(draftToDelete.getPath());
                    break;
                case AGENT_REF:
                    deletedObject = new DeleteAgentRef(draftToDelete.getPath());
                    break;
                case LOCK:
                    // TODO: locks and other objects
                    break;
            }
            updateRepo.getDelete().add(deletedObject);
            
        }
        JOCJsonCommand command = new JOCJsonCommand();
        command.setUriBuilderForCommands(masterUrl);
        command.setAllowAllHostnameVerifier(false);
        command.addHeader("Accept", "application/json");
        command.addHeader("Content-Type", "application/json");
        String updateRepoCommandBody = Globals.objectMapper.writeValueAsString(updateRepo);
        LOGGER.debug(updateRepoCommandBody);
        String response = command.getJsonStringFromPost(updateRepoCommandBody);
    }
    
    private static void updateVersionIdOnObject(DBItemInventoryConfiguration draft, String versionId, SOSHibernateSession session)
            throws JsonParseException, JsonMappingException, IOException, SOSHibernateException {
        switch(DeployType.fromValue(draft.getObjectType())) {
            case WORKFLOW:
                Workflow workflow = Globals.objectMapper.readValue(draft.getContent(), Workflow.class);
                workflow.setVersionId(versionId);
                draft.setContent(Globals.objectMapper.writeValueAsString(workflow));
                break;
            case AGENT_REF:
                AgentRef agentRef = Globals.objectMapper.readValue(draft.getContent(), AgentRef.class);
                agentRef.setVersionId(versionId);
                draft.setContent(Globals.objectMapper.writeValueAsString(agentRef));
                break;
            case LOCK:
                // TODO: locks and other objects
                break;
        }
        session.update(draft);
    }

    public static Set<DBItemDeployedConfiguration> cloneInvCfgsToDepCfgs(
            Set<DBItemInventoryConfiguration> drafts, String account, SOSHibernateSession hibernateSession) throws SOSHibernateException {
        Set<DBItemDeployedConfiguration> deployedObjects = new HashSet<DBItemDeployedConfiguration>();
        for (DBItemInventoryConfiguration draft : drafts) {
            DBItemDeployedConfiguration newDeployedObject = new DBItemDeployedConfiguration();
            newDeployedObject.setEditAccount(draft.getEditAccount());
            newDeployedObject.setPublishAccount(account);
            newDeployedObject.setVersion(draft.getVersion());
            newDeployedObject.setParentVersion(draft.getParentVersion());
            newDeployedObject.setPath(draft.getPath());
            newDeployedObject.setFolder(draft.getFolder());
            newDeployedObject.setUri(draft.getUri());
            newDeployedObject.setObjectType(draft.getObjectType());
            newDeployedObject.setVersionId(draft.getVersionId());
            newDeployedObject.setContent(draft.getContent());
            newDeployedObject.setSignedContent(draft.getSignedContent());
            newDeployedObject.setComment(draft.getComment());
            newDeployedObject.setModified(Date.from(Instant.now()));
            hibernateSession.save(newDeployedObject);
            deployedObjects.add(newDeployedObject);
        }
        return deployedObjects;
    }
    
    public static void prepareNextInvCfgGeneration(Set<DBItemInventoryConfiguration> drafts, SOSHibernateSession hibernateSession)
            throws SOSHibernateException {
        for (DBItemInventoryConfiguration draft : drafts) {
            draft.setSignedContent(null);
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
}
