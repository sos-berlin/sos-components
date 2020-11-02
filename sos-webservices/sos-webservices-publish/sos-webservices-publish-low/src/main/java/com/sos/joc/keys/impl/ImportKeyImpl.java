package com.sos.joc.keys.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
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
            String xAccessToken, FormDataBodyPart body, String timeSpent, String ticketLink, String comment, String importKeyFilter)
            throws Exception {
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
        InputStream stream = null;
        try {
            initLogging(API_CALL, importKeyFilter.getBytes(), xAccessToken);
            JsonValidator.validateFailFast(importKeyFilter.getBytes(StandardCharsets.UTF_8), ImportFilter.class);
            ImportFilter filter = Globals.objectMapper.readValue(importKeyFilter, ImportFilter.class);
            filter.setAuditLog(auditLog);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", 
                    getPermissonsJocCockpit("", xAccessToken).getInventory().getConfigurations().getPublish().isImportKey());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SOSHibernateSession hibernateSession = null;
            stream = body.getEntityAs(InputStream.class);
            ImportAudit importAudit = new ImportAudit(filter);
            JocKeyPair keyPair = new JocKeyPair();
            String keyFromFile = readFileContent(stream, filter);
            keyPair.setKeyAlgorithm(filter.getKeyAlgorithm());
            String account = Globals.defaultProfileAccount;
            String publicKey = null;
            String comment = null;
            if (keyFromFile != null) {
                if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(filter.getKeyAlgorithm())) {
                    try {
                        publicKey = KeyUtil.extractPublicKey(keyFromFile);
                        if (publicKey != null) {
                            keyPair.setPrivateKey(keyFromFile);
                            comment = String.format("autom. comment: new Private Key imported for profile - %1$s -", account);
                        }
                    } catch (Exception e) {
                        throw new JocKeyNotValidException("The provided file does not contain a valid private PGP key!");
                    }
                }  else if (SOSKeyConstants.RSA_ALGORITHM_NAME.equals(filter.getKeyAlgorithm())
                        && !keyFromFile.startsWith(SOSKeyConstants.CERTIFICATE_HEADER)) {
                    try {
                        KeyPair kp = KeyUtil.getKeyPairFromPrivatKeyString(keyFromFile);
                        if (kp != null) {
                            keyPair.setPrivateKey(keyFromFile);
                            comment = String.format("autom. comment: new Private Key imported for profile - %1$s -", account);
                        }
                    } catch (ClassCastException e) {
                        try {
                            KeyPair kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(keyFromFile);
                            if (kp != null) {
                                keyPair.setPrivateKey(keyFromFile);
                                comment = String.format("autom. comment: new Private Key imported for profile - %1$s -", account);
                            }
                        } catch (Exception e1) {
                            throw new JocKeyNotValidException("The provided file does not contain a valid private RSA key!");
                        }
                    }
                } else if (SOSKeyConstants.ECDSA_ALGORITHM_NAME.equals(filter.getKeyAlgorithm())
                        && !keyFromFile.startsWith(SOSKeyConstants.CERTIFICATE_HEADER)) {
                    try {
                        KeyPair kp = KeyUtil.getKeyPairFromECDSAPrivatKeyString(keyFromFile);
                        if (kp != null) {
                            keyPair.setPrivateKey(keyFromFile);
                            comment = String.format("autom. comment: new Private Key imported for profile - %1$s -", account);
                        }
                    } catch (Exception e) {
                        throw new JocKeyNotValidException("The provided file does not contain a valid private ECDSA key!");
                    }
                } else if (keyFromFile.startsWith(SOSKeyConstants.CERTIFICATE_HEADER)) {
                    try {
                        X509Certificate cert = KeyUtil.getX509Certificate(keyFromFile);
                        if (cert != null) {
                            keyPair.setCertificate(keyFromFile);
                            comment = String.format("autom. comment: new X.509 Certificate imported for profile - %1$s -", account);
                        }
                    } catch (Exception e) {
                        throw new JocKeyNotValidException("The provided file does not contain a valid X.509 certificate!");
                    }
                } else if (keyFromFile.startsWith(SOSKeyConstants.PUBLIC_PGP_KEY_HEADER) 
                        || keyFromFile.startsWith(SOSKeyConstants.PUBLIC_RSA_KEY_HEADER)
                        || keyFromFile.startsWith(SOSKeyConstants.PUBLIC_KEY_HEADER)
                        || keyFromFile.startsWith(SOSKeyConstants.PUBLIC_EC_KEY_HEADER)
                        || keyFromFile.startsWith(SOSKeyConstants.PUBLIC_ECDSA_KEY_HEADER)) {
                    throw new JocUnsupportedKeyTypeException("Wrong key type. expected: private or certificate | received: public");
                } else {
                    throw new JocKeyNotValidException(
                            "The provided file does not contain a valid PGP, RSA or ECDSA Private Key nor a X.509 certificate!");
                }
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            PublishUtils.storeKey(keyPair, hibernateSession, account, JocSecurityLevel.LOW);
            if(importAudit.getAuditLog() == null) {
                importAudit.setAuditLog(new AuditParams());
            }
            if (importAudit.getAuditLog().getComment() == null || importAudit.getAuditLog().getComment().isEmpty()) {
                importAudit.getAuditLog().setComment(comment);
            }
            logAuditMessage(importAudit);
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
