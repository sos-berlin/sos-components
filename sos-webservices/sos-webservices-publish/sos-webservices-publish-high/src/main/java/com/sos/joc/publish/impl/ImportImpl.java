package com.sos.joc.publish.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.security.InvalidKeyException;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import com.sos.jobscheduler.model.agent.AgentRefEdit;
import com.sos.jobscheduler.model.workflow.Workflow;
import com.sos.jobscheduler.model.workflow.WorkflowEdit;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ImportAudit;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocSignatureVerificationException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.keys.db.DBLayerKeys;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.publish.ImportFilter;
import com.sos.joc.model.publish.Signature;
import com.sos.joc.model.publish.SignaturePath;
import com.sos.joc.publish.common.JSObjectFileExtension;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.mapper.UpDownloadMapper;
import com.sos.joc.publish.resource.IImportResource;

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
            		/*getPermissonsJocCockpit("", xAccessToken).getPublish().isImport()*/
            		true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
            stream = body.getEntityAs(InputStream.class);
            final String mediaSubType = body.getMediaType().getSubtype().replaceFirst("^x-", "");
            ImportAudit importAudit = new ImportAudit(filter);
            logAuditMessage(importAudit);

            
            // process uploaded archive
            if (mediaSubType.contains("zip") && !mediaSubType.contains("gzip")) {
                readZipFileContent(stream, filter, workflows, agentRefs);
            } else if ((mediaSubType.contains("gz") || mediaSubType.contains("gzip")) && !mediaSubType.contains("tar.gz")) {
                // TODO:
//                readGzipFileContent(stream, filter);
            } else if (mediaSubType.contains("tgz") || mediaSubType.contains("tar.gz")) {
                // TODO:                
//                readTarGzipFileContent(stream, filter);
            } else {
            	throw new JocUnsupportedFileTypeException(String.format("The file %1$s to be uploaded must have the format zip!", uploadFileName)); 
            }
            // process signature verification and save or update objects
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBItemJocAuditLog dbItemAuditLog = storeAuditLogEntry(importAudit);
            DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
            for (Workflow workflow : workflows) {
                WorkflowEdit wfEdit = new WorkflowEdit();
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
                AgentRefEdit arEdit = new AgentRefEdit();
                arEdit.setContent(agentRef);
                if (!signaturePaths.isEmpty()) {
                    Signature signature = verifyAgentRefs(hibernateSession, agentRef, account);
                    if (signature != null) {
                        arEdit.setSignedContent(signature.getSignatureString());
                    } 
                }
                dbLayer.saveOrUpdateInventoryConfiguration(agentRef.getPath(), arEdit, agentRef.getTYPE(), account, dbItemAuditLog.getId());
            }
            storeAuditLogEntry(importAudit);
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

    private void readZipFileContent(InputStream inputStream, ImportFilter filter, Set<Workflow> workflows, Set<AgentRef> agentRefs/*, Set<Lock> locks*/) throws DBConnectionRefusedException, DBInvalidDataException,
            SOSHibernateException, IOException, JocUnsupportedFileTypeException, JocConfigurationException, DBOpenSessionException {
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
                } else if (("/"+entryName).endsWith(JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.value())) {
                    if (("/" + entryName).endsWith(JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.value())) {
                        signaturePath.setObjectPath("/" + entryName
                                .substring(0, entryName.indexOf(JSObjectFileExtension.WORKFLOW_SIGNATURE_FILE_EXTENSION.value())));
                        signature.setSignatureString(outBuffer.toString());
                        signaturePath.setSignature(signature);
                        signaturePaths.add(signaturePath);
                    }
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.AGENT_REF_FILE_EXTENSION.value())) {
                    agentRefs.add(om.readValue(outBuffer.toString(), AgentRef.class));
                } else if (("/" + entryName).endsWith(JSObjectFileExtension.AGENT_REF_SIGNATURE_FILE_EXTENSION.value())) {
                    signaturePath.setObjectPath("/" + entryName
                            .substring(0, entryName.indexOf(JSObjectFileExtension.AGENT_REF_SIGNATURE_FILE_EXTENSION.value())));
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
                } catch (IOException e) {
                }
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
                JocKeyPair keyPair = dbLayerKeys.getKeyPair(account);
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

    private Signature verifyAgentRefs(SOSHibernateSession hibernateSession, AgentRef agentRef, String account)
            throws JocSignatureVerificationException, SOSHibernateException {
        SignaturePath signaturePath = signaturePaths.stream().filter(signaturePathFromStream -> signaturePathFromStream.getObjectPath()
                .equals(agentRef.getPath())).map(signaturePathFromStream -> signaturePathFromStream).findFirst().get();
        DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
        Boolean verified = null;
        try {
            if (signaturePath != null && signaturePath.getSignature() != null) {
                JocKeyPair keyPair = dbLayerKeys.getKeyPair(account);
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
