package com.sos.joc.publish.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.bouncycastle.openpgp.PGPException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.exception.SOSException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.httpclient.SOSRestApiClient;
import com.sos.jobscheduler.db.inventory.DBItemJSDraftObject;
import com.sos.jobscheduler.model.agent.DeleteAgentRef;
import com.sos.jobscheduler.model.command.UpdateRepo;
import com.sos.jobscheduler.model.deploy.DeleteObject;
import com.sos.jobscheduler.model.deploy.DeployType;
import com.sos.jobscheduler.model.deploy.Signature;
import com.sos.jobscheduler.model.deploy.SignatureType;
import com.sos.jobscheduler.model.deploy.SignedObject;
import com.sos.jobscheduler.model.workflow.DeleteWorkflow;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.JocMissingPGPKeyException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocUnsupportedKeyTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.pgp.JocPGPKeyType;
import com.sos.joc.model.pgp.SOSPGPKeyPair;
import com.sos.pgp.util.key.KeyUtil;
import com.sos.pgp.util.sign.SignObject;
import com.sos.pgp.util.verify.VerifySignature;

public abstract class PublishUtils {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(PublishUtils.class);
    private static final String PRIVATE_KEY_BLOCK_TITLESTART = "-----BEGIN PGP PRIVATE";
    private static final String PUBLIC_KEY_BLOCK_TITLESTART = "-----BEGIN PGP PUBLIC";

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
    
    private static void storeKey(SOSPGPKeyPair keyPair, SOSHibernateSession hibernateSession, String account)  throws SOSHibernateException {
        DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
        if (keyPair != null) {
            if (keyPair.getPrivateKey() != null) {
                dbLayerKeys.saveOrUpdateKey(JocPGPKeyType.PRIVATE.ordinal(), keyPair.getPrivateKey(), account);
            } else if (keyPair.getPublicKey() != null) {
                dbLayerKeys.saveOrUpdateKey(JocPGPKeyType.PUBLIC.ordinal(), keyPair.getPublicKey(), account);
            }
        }
    }

    public static void checkJocSecurityLevelAndStore (SOSPGPKeyPair keyPair, SOSHibernateSession hibernateSession, String account) 
            throws SOSHibernateException, JocUnsupportedKeyTypeException, JocMissingRequiredParameterException {
        if (keyPair != null) {
            //Check forJocSecurityLevel commented, has to be introduced when the testing can be done with changing joc.properties
            if (keyPair.getPrivateKey() != null && Globals.getJocSecurityLevel().equals(JocSecurityLevel.MEDIUM)) {
                if (keyPair.getPrivateKey().startsWith(PUBLIC_KEY_BLOCK_TITLESTART)) {
                    throw new JocUnsupportedKeyTypeException("Wrong key type. expected: private | received: public");
                }
                storeKey(keyPair, hibernateSession, account);
            } else if (keyPair.getPublicKey() != null && Globals.getJocSecurityLevel().equals(JocSecurityLevel.HIGH)) {
                if (keyPair.getPublicKey().startsWith(PRIVATE_KEY_BLOCK_TITLESTART)) {
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

    public static void signDrafts(String account, Set<DBItemJSDraftObject> unsignedDrafts, SOSHibernateSession session)
            throws SOSHibernateException, JocMissingPGPKeyException, IOException, PGPException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        SOSPGPKeyPair keyPair = dbLayer.getKeyPair(account);
        signDrafts(account, unsignedDrafts, keyPair);
    }
    
    public static void signDraftsDefault(String account, Set<DBItemJSDraftObject> unsignedDrafts, SOSHibernateSession session)
            throws SOSHibernateException, JocMissingPGPKeyException, IOException, PGPException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        SOSPGPKeyPair keyPair = dbLayer.getDefaultKeyPair();
        signDrafts(account, unsignedDrafts, keyPair);
    }
    
    public static void signDrafts(String account, Set<DBItemJSDraftObject> unsignedDrafts, SOSPGPKeyPair keyPair)
            throws SOSHibernateException, JocMissingPGPKeyException, IOException, PGPException {
        if(keyPair.getPrivateKey() == null || keyPair.getPrivateKey().isEmpty()) {
            throw new JocMissingPGPKeyException("No private PGP key found fo signing!");
        } else {
            for (DBItemJSDraftObject draft : unsignedDrafts) {
                draft.setSignedContent(SignObject.sign(keyPair.getPrivateKey(), draft.getContent(), null));
            }
        }
    }
    
    public static Set<DBItemJSDraftObject> verifySignatures(String account, Set<DBItemJSDraftObject> signedDrafts, SOSHibernateSession session)
            throws SOSHibernateException, IOException, PGPException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        SOSPGPKeyPair keyPair = dbLayer.getKeyPair(account);
        return verifySignatures(account, signedDrafts, keyPair);
    }

    public static Set<DBItemJSDraftObject> verifySignaturesDefault(String account, Set<DBItemJSDraftObject> signedDrafts, SOSHibernateSession session)
            throws SOSHibernateException, IOException, PGPException {
        DBLayerKeys dbLayer = new DBLayerKeys(session);
        SOSPGPKeyPair keyPair = dbLayer.getDefaultKeyPair();
        return verifySignatures(account, signedDrafts, keyPair);
    }

    public static Set<DBItemJSDraftObject> verifySignatures(String account, Set<DBItemJSDraftObject> signedDrafts, SOSPGPKeyPair keyPair)
            throws SOSHibernateException, IOException, PGPException {
        Set<DBItemJSDraftObject> verifiedDrafts = new HashSet<DBItemJSDraftObject>();
        String publicKey = null;
        if (keyPair.getPublicKey() == null) {
            publicKey = KeyUtil.extractPublicKey(keyPair.getPrivateKey());
        } else {
            publicKey = keyPair.getPublicKey();
        }
        Boolean verified = false;
        for (DBItemJSDraftObject draft : signedDrafts) {
            verified = VerifySignature.verify(publicKey, draft.getContent(), draft.getSignedContent());
            if(!verified) {
                LOGGER.trace(String.format("Signature of object %1$s could not be verified! Object will not be deployed.", draft.getPath()));
            } else {
                verifiedDrafts.add(draft);
            }
        }
        return verifiedDrafts;
    }

    public static void updateRepo(Set<DBItemJSDraftObject> drafts, List<DBItemJSDraftObject> draftsToDelete, String masterUrl)
            throws IllegalArgumentException, UriBuilderException, JsonProcessingException, SOSException {
        UpdateRepo updateRepo = new UpdateRepo();
        updateRepo.setVersionId("PUT_NEW_GENERATED_VERSION_ID_HERE");
        for (DBItemJSDraftObject draft : drafts) {
            SignedObject signedObject = new SignedObject();
            signedObject.setString(draft.getContent());
            Signature signature = new Signature();
            signature.setTYPE(SignatureType.PGP);
            signature.setSignatureString(draft.getSignedContent());
            signedObject.setSignature(signature);
            updateRepo.getChange().add(signedObject);
        }
        for (DBItemJSDraftObject draftToDelete : draftsToDelete) {
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
        SOSRestApiClient httpClient = new SOSRestApiClient();
        httpClient.setAllowAllHostnameVerifier(false);
        // Woher bekomm ich die BasicAuthorization des entsprechenden Masters?
        // Antwort OH 05.05.: gar nicht, wird entfernt
        // Stand 06.05.: bisher keine Klärung 
//        httpClient.setBasicAuthorization("VGVzdDp0ZXN0");
        httpClient.addHeader("Accept", "application/json");
        httpClient.addHeader("Content-Type", "application/json");
        // for each Master
        String response = httpClient.postRestService(UriBuilder.fromPath(masterUrl + "/master/api/command").build(),
                Globals.objectMapper.writeValueAsString(updateRepo));

    }
}
