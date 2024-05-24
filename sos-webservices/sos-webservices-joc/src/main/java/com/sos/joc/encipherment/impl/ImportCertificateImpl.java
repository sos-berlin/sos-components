package com.sos.joc.encipherment.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Date;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
import org.glassfish.jersey.media.multipart.FormDataParam;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.encipherment.resource.IImportCertificate;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocUnsupportedFileTypeException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.encipherment.DeleteCertificateRequestFilter;
import com.sos.joc.model.encipherment.ImportCertificateRequestFilter;
import com.sos.joc.model.encipherment.StoreCertificateRequestFilter;
import com.sos.joc.model.publish.ImportKeyFilter;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("encipherment/certificate")
public class ImportCertificateImpl extends JOCResourceImpl implements IImportCertificate {

    private static final String API_CALL = "./encipherment/certificate/import";

    @Override
    public JOCDefaultResponse postImportCertificate(String xAccessToken, FormDataBodyPart body, String certAlias, String privateKeyPath, String timeSpent, String ticketLink, String comment)
            throws Exception {
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
        return postImportCertificate(xAccessToken, body, filter);
    }
    
    private JOCDefaultResponse postImportCertificate(String xAccessToken, FormDataBodyPart body, ImportCertificateRequestFilter filter) {
        SOSHibernateSession hibernateSession = null;
        InputStream stream = null;
        try {
            initLogging(API_CALL, filter.toString().getBytes(), xAccessToken);
            JsonValidator.validateFailFast(Globals.objectMapper.writeValueAsBytes(filter), ImportCertificateRequestFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getCertificates().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            storeAuditLog(filter.getAuditLog(), CategoryType.CERTIFICATES);
            
            stream = body.getEntityAs(InputStream.class);
            String certificateFromFile = PublishUtils.readFileContent(stream);
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            dbLayer.storeEnciphermentCertificate(filter.getCertAlias(), certificateFromFile, filter.getPrivateKeyPath());
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));
        } catch (JocConcurrentAccessException e) {
            ProblemHelper.postMessageAsHintIfExist(e.getMessage(), xAccessToken, getJocError(), null);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatus434JSError(e);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(hibernateSession);
        }
    }

}
