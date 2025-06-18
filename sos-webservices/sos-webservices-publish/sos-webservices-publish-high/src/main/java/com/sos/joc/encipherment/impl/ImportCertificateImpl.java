package com.sos.joc.encipherment.impl;

import java.io.InputStream;
import java.time.Instant;
import java.util.Date;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.encipherment.resource.IImportCertificate;
import com.sos.joc.encipherment.util.EnciphermentUtils;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.encipherment.ImportCertificateRequestFilter;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("encipherment/certificate")
public class ImportCertificateImpl extends JOCResourceImpl implements IImportCertificate {

    private static final String API_CALL = "./encipherment/certificate/import";
    //private static final Logger LOGGER = LoggerFactory.getLogger(ImportCertificateImpl.class);

    @Override
    public JOCDefaultResponse postImportCertificate(String xAccessToken, FormDataBodyPart body, String certAlias, String privateKeyPath,
            String jobResourceFolder, String timeSpent, String ticketLink, String comment) {
        AuditParams auditLog = new AuditParams();
        auditLog.setComment(comment);
        auditLog.setTicketLink(ticketLink);
        try {
            auditLog.setTimeSpent(Integer.valueOf(timeSpent));
        } catch (Exception e) {
        }
        ImportCertificateRequestFilter filter = new ImportCertificateRequestFilter();
        filter.setAuditLog(auditLog);
        filter.setCertAlias(certAlias);
        filter.setPrivateKeyPath(privateKeyPath);
        filter.setJobResourceFolder(jobResourceFolder);
        filter.setFilename(PublishUtils.getImportFilename(body));
        return postImportCertificate(xAccessToken, body, filter);
    }
    
    private JOCDefaultResponse postImportCertificate(String xAccessToken, FormDataBodyPart body, ImportCertificateRequestFilter filter) {
        SOSHibernateSession hibernateSession = null;
        InputStream stream = null;
        try {
            byte[] fakeRequest = Globals.objectMapper.writeValueAsBytes(filter);
            initLogging(API_CALL, fakeRequest, xAccessToken, CategoryType.CERTIFICATES);
            JsonValidator.validateFailFast(fakeRequest, ImportCertificateRequestFilter.class);
            //4-eyes principle cannot support uploads
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(xAccessToken).getAdministration().getCertificates()
                    .getManage(), false);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            DBItemJocAuditLog auditLog = storeAuditLog(filter.getAuditLog());
            
            stream = body.getEntityAs(InputStream.class);
            String certificateFromFile = PublishUtils.readFileContent(stream);
            // simple check if filter.getCertificate() really is a certificate or public key
            KeyUtil.isInputCertOrPublicKey(certificateFromFile);
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            dbLayer.storeEnciphermentCertificate(filter.getCertAlias(), certificateFromFile, filter.getPrivateKeyPath());
            // create or Update JobResource 
            EnciphermentUtils.createRelatedJobResource(hibernateSession, filter, certificateFromFile, auditLog.getId());
            
            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocConcurrentAccessException e) {
            ProblemHelper.postMessageAsHintIfExist(e.getMessage(), xAccessToken, getJocError(), null);
            return responseStatus434JSError(e);
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
