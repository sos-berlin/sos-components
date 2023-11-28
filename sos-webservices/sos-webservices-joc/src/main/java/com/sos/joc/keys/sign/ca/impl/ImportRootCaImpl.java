package com.sos.joc.keys.sign.ca.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.cert.X509Certificate;
import java.time.Instant;
import java.util.Date;

import jakarta.ws.rs.Path;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.sign.keys.SOSKeyConstants;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.settings.ClusterSettings;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocKeyNotValidException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.keys.ca.resource.IImportRootCa;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.sign.JocKeyPair;
import com.sos.joc.model.sign.JocKeyType;
import com.sos.joc.publish.util.PublishUtils;

@Path("profile/key/ca")
public class ImportRootCaImpl extends JOCResourceImpl implements IImportRootCa {

    private static final String API_CALL = "./profile/key/ca/import";

    @Override
    public JOCDefaultResponse postImportRootCa(
            String xAccessToken, FormDataBodyPart body, String timeSpent, String ticketLink, String comment)
            throws Exception {
        AuditParams auditLog = new AuditParams();
        auditLog.setComment(comment);
        auditLog.setTicketLink(ticketLink);
        try {
            auditLog.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {
        }
        return postImportRootCa(xAccessToken, body, auditLog);
    }

    private JOCDefaultResponse postImportRootCa(String xAccessToken, FormDataBodyPart body, AuditParams auditLog) throws Exception {
        InputStream stream = null;
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, null, xAccessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getCertificates().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            storeAuditLog(auditLog, CategoryType.CERTIFICATES);
            
            stream = body.getEntityAs(InputStream.class);
            JocKeyPair keyPair = new JocKeyPair();
            String certificateFromFile = readFileContent(stream);
            keyPair.setKeyAlgorithm(SOSKeyConstants.ECDSA_ALGORITHM_NAME);
            keyPair.setKeyType(JocKeyType.CA.name());
            String accountName = "";
            if (JocSecurityLevel.LOW.equals(Globals.getJocSecurityLevel())) {
                accountName = ClusterSettings.getDefaultProfileAccount(Globals.getConfigurationGlobalsJoc());
            } else {
                accountName =  jobschedulerUser.getSOSAuthCurrentAccount().getAccountname();
            }
            String publicKey = null;
            if (certificateFromFile != null) {
                try {
                    X509Certificate cert = KeyUtil.getX509Certificate(certificateFromFile);
                    if (cert != null) {
                        keyPair.setCertificate(certificateFromFile);
                    }
                } catch (Exception e) {
                    throw new JocKeyNotValidException("The provided file does not contain a valid X.509 certificate!");
                }
            }
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            dbLayer.saveOrUpdateSigningRootCaCertificate(keyPair, accountName, Globals.getJocSecurityLevel().intValue());
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {}
        }
    }

    private String readFileContent(InputStream inputStream) throws DBConnectionRefusedException, DBInvalidDataException,
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
