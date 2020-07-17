package com.sos.joc.keys.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
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
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.exceptions.JocPGPKeyNotValidException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.exceptions.JocUnsupportedKeyTypeException;
import com.sos.joc.keys.resource.IImportKey;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.pgp.JocKeyPair;
import com.sos.joc.model.publish.ImportFilter;
import com.sos.joc.publish.util.PublishUtils;

@Path("publish")
public class ImportKeyImpl extends JOCResourceImpl implements IImportKey {

    private static final String API_CALL = "./publish/import_key";
    private static final String PRIVATE_KEY_BLOCK_TITLESTART = "-----BEGIN PGP PRIVATE";
    private static final String PUBLIC_KEY_BLOCK_TITLESTART = "-----BEGIN PGP PUBLIC";
    private SOSHibernateSession connection = null;

    @Override
    public JOCDefaultResponse postImportKey(String xAccessToken, FormDataBodyPart body, String timeSpent, String ticketLink, String comment)
            throws Exception {
        AuditParams auditLog = new AuditParams();
        auditLog.setComment(comment);
        auditLog.setTicketLink(ticketLink);
        try {
            auditLog.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {
        }
        return postImportKey(xAccessToken, body, auditLog);
    }

    private JOCDefaultResponse postImportKey(String xAccessToken, FormDataBodyPart body, AuditParams auditLog) throws Exception {
        InputStream stream = null;
        String uploadFileName = null;
        try {
            ImportFilter filter = new ImportFilter();
            filter.setAuditLog(auditLog);
            if (body != null) {
                uploadFileName = URLDecoder.decode(body.getContentDisposition().getFileName(), "UTF-8");
            } else {
                throw new JocMissingRequiredParameterException("undefined 'file'");
            }
            // copy&paste Permission, has o be changed to the correct permission for upload
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, filter, xAccessToken, "",
                    /* getPermissonsJocCockpit(filter.getJobschedulerId(), xAccessToken).getDeploy().isImportKey() */
                    true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SOSHibernateSession hibernateSession = null;
            stream = body.getEntityAs(InputStream.class);
            String extension = PublishUtils.getExtensionFromFilename(uploadFileName);
            // final String mediaSubType = body.getMediaType().getSubtype().replaceFirst("^x-", "");
            ImportAudit importAudit = new ImportAudit(filter);
            logAuditMessage(importAudit);
            JocKeyPair keyPair = new JocKeyPair();
//            if ("asc".equalsIgnoreCase(extension)) {
                String keyFromFile = readFileContent(stream, filter);
                String privateKey = null;
                String publicKey = null;
                if (keyFromFile != null) {
                    if (keyFromFile.startsWith(SOSPGPConstants.PRIVATE_PGP_KEY_HEADER)) {
                        privateKey = keyFromFile;
                        publicKey = KeyUtil.extractPublicKey(keyFromFile);
                        keyPair.setPrivateKey(privateKey);
                        keyPair.setPublicKey(publicKey);
                    } else if (keyFromFile.startsWith(SOSPGPConstants.PRIVATE_RSA_KEY_HEADER)) {
                        KeyPair kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(keyFromFile);
                        keyPair = KeyUtil.createJOCKeyPair(kp);
                    } else if (keyFromFile.startsWith(SOSPGPConstants.PRIVATE_KEY_HEADER)) {
                        KeyPair kp = KeyUtil.getKeyPairFromPrivatKeyString(keyFromFile);
                        keyPair = KeyUtil.createJOCKeyPair(kp);
                    } else if (keyFromFile.startsWith(SOSPGPConstants.PUBLIC_PGP_KEY_HEADER) 
                            || keyFromFile.startsWith(SOSPGPConstants.PUBLIC_RSA_KEY_HEADER)
                            || keyFromFile.startsWith(SOSPGPConstants.PUBLIC_KEY_HEADER)) {
                        throw new JocUnsupportedKeyTypeException("Wrong key type. expected: private | received: public");
                    } else {
                        throw new JocPGPKeyNotValidException("The provided file does not contain a valid PGP or RSA Private Key!");
                    }
                }
//            } else {
//                throw new JocUnsupportedFileTypeException(String.format("The file %1$s to be uploaded must have the format *.asc!", uploadFileName));
//            }
            if (keyPair != null && keyPair.getPrivateKey() != null) {
                hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
                String account = jobschedulerUser.getSosShiroCurrentUser().getUsername();
                PublishUtils.storeKey(keyPair, hibernateSession, account);
                storeAuditLogEntry(importAudit);
                return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
            } else {
                throw new JocPGPKeyNotValidException("The provided file does not contain a valid PGP key!");
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
            } catch (Exception e) {
            }
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
