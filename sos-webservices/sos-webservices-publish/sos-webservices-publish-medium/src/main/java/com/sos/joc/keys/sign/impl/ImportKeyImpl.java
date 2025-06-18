package com.sos.joc.keys.sign.impl;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.exceptions.JocKeyNotValidException;
import com.sos.joc.exceptions.JocUnsupportedKeyTypeException;
import com.sos.joc.keys.sign.resource.IImportKey;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.publish.ImportKeyFilter;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("profile/key")
public class ImportKeyImpl extends JOCResourceImpl implements IImportKey {

    private static final String API_CALL = "./profile/key/import";

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

    private JOCDefaultResponse postImportKey(String xAccessToken, FormDataBodyPart body, AuditParams auditLog, String importKeyFilter)
            throws Exception {
        InputStream stream = null;
        SOSHibernateSession hibernateSession = null;
        try {
            checkRequiredParameter("importKeyFilter", importKeyFilter);
            importKeyFilter = importKeyFilter.trim().replaceFirst("}$", "\"filename\":\"" + PublishUtils.getImportFilename(body) + "\"}");
            initLogging(API_CALL, importKeyFilter.getBytes(), xAccessToken, CategoryType.CERTIFICATES);
            JsonValidator.validateFailFast(importKeyFilter.getBytes(StandardCharsets.UTF_8), ImportKeyFilter.class);
            ImportKeyFilter filter = Globals.objectMapper.readValue(importKeyFilter, ImportKeyFilter.class);
            filter.setAuditLog(auditLog);
            //4-eyes principle cannot support uploads
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(xAccessToken).getAdministration().getCertificates()
                    .getManage(), false);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(filter.getAuditLog());
            
            stream = body.getEntityAs(InputStream.class);
            String account = jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
            JocKeyPair keyPair = null;
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayerKeys = new DBLayerKeys(hibernateSession);
            keyPair = dbLayerKeys.getKeyPair(account, JocSecurityLevel.MEDIUM);
            if(keyPair == null) {
                keyPair = new JocKeyPair();
            }
            String keyFromFile = PublishUtils.readFileContent(stream);
            keyPair.setKeyAlgorithm(filter.getKeyAlgorithm());
            String publicKey = null;
            String reason = null;
            if (keyFromFile != null) {
                if (SOSKeyConstants.PGP_ALGORITHM_NAME.equals(filter.getKeyAlgorithm())) {
                    try {
                        publicKey = KeyUtil.extractPublicKey(keyFromFile);
                        if (publicKey != null) {
                            keyPair.setPrivateKey(keyFromFile);
                            reason = String.format("new Private Key imported for profile - %1$s -", account);
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
                            reason = String.format("new Private Key imported for profile - %1$s -", account);
                        }
                    } catch (ClassCastException e) {
                        try {
                            KeyPair kp = KeyUtil.getKeyPairFromRSAPrivatKeyString(keyFromFile);
                            if (kp != null) {
                                keyPair.setPrivateKey(keyFromFile);
                                reason = String.format("new Private Key imported for profile - %1$s -", account);
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
                            reason = String.format("new Private Key imported for profile - %1$s -", account);
                        }
                    } catch (Exception e) {
                        throw new JocKeyNotValidException("The provided file does not contain a valid private ECDSA key!");
                    }
                } else if (keyFromFile.startsWith(SOSKeyConstants.CERTIFICATE_HEADER)) {
                    try {
                        X509Certificate cert = KeyUtil.getX509Certificate(keyFromFile);
                        if (cert != null) {
                            keyPair.setCertificate(keyFromFile);
                            reason = String.format("new X.509 Certificate imported for profile - %1$s -", account);
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
            PublishUtils.storeKey(keyPair, hibernateSession, account, JocSecurityLevel.MEDIUM);
//            DeployAudit importAudit = new DeployAudit(filter.getAuditLog(), reason);
//            logAuditMessage(importAudit);
//            storeAuditLogEntry(importAudit);
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {}
        }
    }

}
