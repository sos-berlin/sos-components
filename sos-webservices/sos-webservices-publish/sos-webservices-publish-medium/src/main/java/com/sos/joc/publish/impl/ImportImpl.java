package com.sos.joc.publish.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
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
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Path;

import org.bouncycastle.openpgp.PGPException;
import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.pgp.SOSPGPConstants;
import com.sos.commons.sign.pgp.key.KeyUtil;
import com.sos.commons.sign.pgp.verify.VerifySignature;
import com.sos.jobscheduler.model.agent.AgentRef;
import com.sos.jobscheduler.model.agent.AgentRefPublish;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.jobscheduler.model.workflow.WorkflowPublish;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ImportAudit;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocSignatureVerificationException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.publish.ImportFilter;
import com.sos.joc.model.publish.Signature;
import com.sos.joc.model.publish.SignaturePath;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpDownloadMapper;
import com.sos.joc.publish.resource.IImportResource;
import com.sos.joc.publish.util.PublishUtils;

@Path("publish")
public class ImportImpl extends JOCResourceImpl implements IImportResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImportImpl.class);
    private static final String API_CALL = "./publish/import";
    private SOSHibernateSession connection = null;
    private Set<Workflow> workflows = new HashSet<Workflow>();
    private Set<AgentRef> agentRefs = new HashSet<AgentRef>();
    private Set<SignaturePath> signaturePaths = new HashSet<SignaturePath>();
    private ObjectMapper om = UpDownloadMapper.initiateObjectMapper();

    @Override
	public JOCDefaultResponse postImportConfiguration(String xAccessToken, 
			FormDataBodyPart body, 
			String timeSpent,
			String ticketLink,
			boolean updateRepo,
			String comment) throws Exception {
        AuditParams auditLog = new AuditParams();
        auditLog.setComment(comment);
        auditLog.setTicketLink(ticketLink);
        try {
            auditLog.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {}
		return postImportConfiguration(xAccessToken, body, auditLog);
	}

	private JOCDefaultResponse postImportConfiguration(String xAccessToken, FormDataBodyPart body,
			AuditParams auditLog) throws Exception {
        InputStream stream = null;
        String uploadFileName = null;
        SOSHibernateSession hibernateSession = null;
        try {
            ImportFilter filter = new ImportFilter();
            filter.setAuditLog(auditLog);
            if (body != null) {
                uploadFileName = URLDecoder.decode(body.getContentDisposition().getFileName(), "UTF-8");
            } else {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            }
             // copy&paste Permission, has to be changed to the correct permission for upload 
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filter, xAccessToken, "",
                    getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isImport());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            stream = body.getEntityAs(InputStream.class);
            final String mediaSubType = body.getMediaType().getSubtype().replaceFirst("^x-", "");
            ImportAudit importAudit = new ImportAudit(filter);
            logAuditMessage(importAudit);

//            Set<Lock> locks = new HashSet<Lock>();
            
            // process uploaded archive
            if (mediaSubType.contains("zip") && !mediaSubType.contains("gzip")) {
                signaturePaths = PublishUtils.readZipFileContent(stream, workflows, agentRefs);
            } else if (mediaSubType.contains("tgz") || mediaSubType.contains("tar.gz") || mediaSubType.contains("gzip")) {
                signaturePaths = PublishUtils.readTarGzipFileContent(stream, workflows, agentRefs);
            } else {
            	throw new JocUnsupportedFileTypeException(
            	        String.format("The file %1$s to be uploaded must have one of the formats zip, tar.gz or tgz!", uploadFileName));
            }
            // process signature verification and save or update objects
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBItemJocAuditLog dbItemAuditLog = storeAuditLogEntry(importAudit);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            for (Workflow workflow : workflows) {
                WorkflowPublish wfEdit = new WorkflowPublish();
                wfEdit.setContent(workflow);
                if (!signaturePaths.isEmpty()) {
                    Signature signature = verifyWorkflows(hibernateSession, workflow, account);
                    if (signature != null) {
                        wfEdit.setSignedContent(signature.getSignatureString());
                    } 
                }
                dbLayer.saveOrUpdateInventoryConfiguration(workflow.getPath(), wfEdit, workflow.getTYPE(), account, dbItemAuditLog.getId());
            }
            for (AgentRef agentRef : agentRefs) {
                AgentRefPublish arEdit = new AgentRefPublish();
                arEdit.setContent(agentRef);
                if (!signaturePaths.isEmpty()) {
                    Signature signature = verifyAgentRefs(hibernateSession, agentRef, account);
                    if (signature != null) {
                        arEdit.setSignedContent(signature.getSignatureString());
                    } 
                }
                dbLayer.saveOrUpdateInventoryConfiguration(agentRef.getPath(), arEdit, agentRef.getTYPE(), account, dbItemAuditLog.getId());
            }
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
            }
        }
	}

    private Signature verifyWorkflows(SOSHibernateSession hibernateSession, Workflow workflow, String account)
            throws JocSignatureVerificationException, SOSHibernateException {
        SignaturePath signaturePath = signaturePaths.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath()
                .equals(workflow.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
        DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
        Boolean verified = null;
        try {
            if (signaturePath != null && signaturePath.getSignature() != null) {
                JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.MEDIUM);
                if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER)) {
                    String publicKey = KeyUtil.extractPublicKey(keyPair.getPrivateKey());
                    verified = VerifySignature.verifyPGP(publicKey, 
                            om.writeValueAsString(workflow), signaturePath.getSignature().getSignatureString());                    
                } else if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_RSA_KEY_HEADER)) {
                    if (keyPair.getCertificate() != null && !keyPair.getCertificate().isEmpty()) {
                        Certificate certificate = KeyUtil.getX509Certificate(keyPair.getCertificate());
                        verified = VerifySignature.verifyX509(certificate, 
                                om.writeValueAsString(workflow), signaturePath.getSignature().getSignatureString());
                    } else if (keyPair.getPublicKey() != null && !keyPair.getPublicKey().isEmpty()) {
                        KeyPair kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(keyPair.getPrivateKey());
                        PublicKey publicKey = KeyUtil.getPublicKeyFromString(keyPair.getPublicKey());
                        verified = VerifySignature.verifyX509(publicKey, 
                                om.writeValueAsString(workflow), signaturePath.getSignature().getSignatureString());
                        verified = VerifySignature.verifyX509(kp.getPublic(), 
                                om.writeValueAsString(workflow), signaturePath.getSignature().getSignatureString());
                    } else {
                        KeyPair kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(keyPair.getPrivateKey());
                        verified = VerifySignature.verifyX509(kp.getPublic(), 
                                om.writeValueAsString(workflow), signaturePath.getSignature().getSignatureString());
                    }
                } else if (keyPair.getPrivateKey().startsWith(SOSPGPConstants.PRIVATE_KEY_HEADER)) {
                    if (keyPair.getCertificate() != null && !keyPair.getCertificate().isEmpty()) {
                        Certificate certificate = KeyUtil.getCertificate(keyPair.getCertificate());
                        verified = VerifySignature.verifyX509(certificate, 
                                om.writeValueAsString(workflow), signaturePath.getSignature().getSignatureString());
                    } else if (keyPair.getPublicKey() != null && !keyPair.getPublicKey().isEmpty()) {
                        PublicKey publicKey = KeyUtil.getPublicKeyFromString(keyPair.getPublicKey());
                        verified = VerifySignature.verifyX509(publicKey, 
                                om.writeValueAsString(workflow), signaturePath.getSignature().getSignatureString());
                    } else {
                        KeyPair kp = KeyUtil.getKeyPairFromPrivatKeyString(keyPair.getPrivateKey());
                        verified = VerifySignature.verifyX509(kp.getPublic(), 
                                om.writeValueAsString(workflow), signaturePath.getSignature().getSignatureString());
                    }
                }
                if (!verified) {
                    LOGGER.debug(String.format("signature verification for workflow %1$s was not successful!", workflow.getPath()));
                } 
            }
        } catch (IOException | PGPException | CertificateException | InvalidKeyException | NoSuchAlgorithmException | SignatureException 
                | NoSuchProviderException | InvalidKeySpecException  e) {
            throw new JocSignatureVerificationException(e);
        }
        return signaturePath.getSignature();
    }

    private Signature verifyAgentRefs(SOSHibernateSession hibernateSession, AgentRef agentRef, String account)
            throws JocSignatureVerificationException, SOSHibernateException {
        SignaturePath signaturePath = signaturePaths.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath()
                .equals(agentRef.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
        DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
        Boolean verified = null;
        try {
            if (signaturePath != null && signaturePath.getSignature() != null) {
                JocKeyPair keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.MEDIUM);
                String publicKey = KeyUtil.extractPublicKey(keyPair.getPrivateKey());
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
