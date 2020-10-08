package com.sos.joc.keys.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.pgp.SOSPGPConstants;
import com.sos.commons.sign.pgp.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.audit.ImportAudit;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocKeyNotValidException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.exceptions.JocUnsupportedKeyTypeException;
import com.sos.joc.keys.resource.IImportKey;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.publish.ImportFilter;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

@Path("publish")
public class ImportKeyImpl extends JOCResourceImpl implements IImportKey {

    private static final String API_CALL = "./publish/import_key";
    private SOSHibernateSession connection = null;

    @Override
    public JOCDefaultResponse postImportKey(
            String xAccessToken, FormDataBodyPart body, String timeSpent, String ticketLink, String comment, String importKeyFilter) throws Exception {
        AuditParams auditLog = new AuditParams();
        auditLog.setComment(comment);
        auditLog.setTicketLink(ticketLink);
        try {
            auditLog.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {
        }
        return postImportKey(xAccessToken, body, auditLog, importKeyFilter);
    }

    private JOCDefaultResponse postImportKey(String xAccessToken, FormDataBodyPart body, AuditParams auditLog, String importKeyFilter) throws Exception {
        JsonValidator.validateFailFast(importKeyFilter.getBytes(StandardCharsets.UTF_8), ImportFilter.class);
        ImportFilter filter = Globals.objectMapper.readValue(importKeyFilter, ImportFilter.class);
        InputStream stream = null;
        try {
            filter.setAuditLog(auditLog);
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filter, xAccessToken, "",
                    getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isImportKey());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SOSHibernateSession hibernateSession = null;
            stream = body.getEntityAs(InputStream.class);
            ImportAudit importAudit = new ImportAudit(filter);
            logAuditMessage(importAudit);
            JocKeyPair keyPair = new JocKeyPair();
            String keyFromFile = readFileContent(stream, filter);
            keyPair.setPrivateKey(null);
            if (keyFromFile != null) {
                if (keyFromFile.startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER) 
                        || keyFromFile.startsWith(SOSPGPConstants.PRIVATE_RSA_KEY_HEADER)) {
                    throw new JocUnsupportedKeyTypeException("Wrong key type. expected: public | received: private");
                } else if (keyFromFile.startsWith(SOSPGPConstants.PUBLIC_PGP_KEY_HEADER)) {
                    keyPair.setPublicKey(keyFromFile);
                } else if (keyFromFile.startsWith(SOSPGPConstants.PUBLIC_RSA_KEY_HEADER)) {
                    keyPair.setPublicKey(new String(KeyUtil.getSubjectPublicKeyInfo(keyFromFile).getEncoded(), StandardCharsets.UTF_8));
                } else if (keyFromFile.startsWith(SOSPGPConstants.CERTIFICATE_HEADER)) {
                    String pubKey = KeyUtil.getPublicKeyAsString(KeyUtil.getSubjectPublicKeyInfoFromCertificate(keyFromFile));
                        keyPair.setPublicKey(pubKey);
                    keyPair.setCertificate(keyFromFile);
                } else {
                    throw new JocKeyNotValidException("The provided file does not contain a valid PGP key!");
                }
            }
            
            if (KeyUtil.isKeyPairValid(keyPair)) {
                hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
                String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
                PublishUtils.storeKey(keyPair, hibernateSession, account, JocSecurityLevel.HIGH);
                storeAuditLogEntry(importAudit);
                return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
            } else {
                throw new JocKeyNotValidException("The provided file does not contain a valid PGP key!");
            }
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
            } catch (Exception e) {}
        }
    }

    private String readFileContent(InputStream inputStream, ImportFilter filter) throws DBConnectionRefusedException, DBInvalidDataException,
            SOSHibernateException, IOException, JocUnsupportedFileTypeException, JocConfigurationException, DBOpenSessionException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        try {
            PublishUtils.inputStream2OutputStream(inputStream, outStream);
        } finally {
            if (inputStream != null) {
                try { inputStream.close(); } catch (IOException e) {}
            }
        }
        return outStream.toString();
    }

}
