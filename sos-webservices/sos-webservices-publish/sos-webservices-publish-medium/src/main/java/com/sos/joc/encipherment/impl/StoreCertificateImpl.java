package com.sos.joc.encipherment.impl;

import java.time.Instant;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.encipherment.resource.IStoreCertificate;
import com.sos.joc.encipherment.util.EnciphermentUtils;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.encipherment.StoreCertificateRequestFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.joc.publish.impl.DeployImpl;
import com.sos.joc.publish.resource.IDeploy;
import com.sos.joc.publish.util.StoreDeployments;
import com.sos.schema.JsonValidator;


@jakarta.ws.rs.Path("encipherment/certificate")
public class StoreCertificateImpl extends JOCResourceImpl implements IStoreCertificate {

    private static final String API_CALL = "./encipherment/certificate/store";

    @Override
    public JOCDefaultResponse postStoreCertificate(String xAccessToken, byte[] storeCertificateFilter) throws Exception {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, storeCertificateFilter, xAccessToken);
            JsonValidator.validateFailFast(storeCertificateFilter, StoreCertificateRequestFilter.class);
            StoreCertificateRequestFilter filter = Globals.objectMapper.readValue(storeCertificateFilter, StoreCertificateRequestFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).getAdministration().getCertificates().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            DBItemJocAuditLog auditLog = storeAuditLog(filter.getAuditLog(), CategoryType.CERTIFICATES);

            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            dbLayer.storeEnciphermentCertificate(filter.getCertAlias(), filter.getCertificate(), filter.getPrivateKeyPath());
            // create or Update JobResource 
            EnciphermentUtils.createRelatedJobResource(hibernateSession, filter, auditLog.getId());
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
            // TODO Deploy the JobResource to all controllers
            IDeploy deploy = new DeployImpl();
            deploy.postDeploy(xAccessToken, storeCertificateFilter);
        }
        
    }
}
