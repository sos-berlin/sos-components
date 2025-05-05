package com.sos.joc.encipherment.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.sign.keys.key.KeyUtil;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.keys.DBLayerKeys;
import com.sos.joc.encipherment.resource.IStoreCertificate;
import com.sos.joc.encipherment.util.EnciphermentUtils;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.encipherment.StoreCertificateRequestFilter;
import com.sos.joc.publish.impl.DeployImpl;
import com.sos.joc.publish.resource.IDeploy;
import com.sos.schema.JsonValidator;


@jakarta.ws.rs.Path("encipherment/certificate")
public class StoreCertificateImpl extends JOCResourceImpl implements IStoreCertificate {

    private static final String API_CALL = "./encipherment/certificate/store";
    private static final Logger LOGGER = LoggerFactory.getLogger(StoreCertificateImpl.class);

    @Override
    public JOCDefaultResponse postStoreCertificate(String xAccessToken, byte[] storeCertificateFilter) {
        SOSHibernateSession hibernateSession = null;
        try {
            initLogging(API_CALL, storeCertificateFilter, xAccessToken);
            JsonValidator.validateFailFast(storeCertificateFilter, StoreCertificateRequestFilter.class);
            StoreCertificateRequestFilter filter = Globals.objectMapper.readValue(storeCertificateFilter, StoreCertificateRequestFilter.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(xAccessToken).map(p -> p.getAdministration()
                    .getCertificates().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            DBItemJocAuditLog auditLog = storeAuditLog(filter.getAuditLog(), CategoryType.CERTIFICATES);
            
            // simple check if filter.getCertificate() really is a certificate or public key
            KeyUtil.isInputCertOrPublicKey(filter.getCertificate());

            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            dbLayer.storeEnciphermentCertificate(filter.getCertAlias(), filter.getCertificate(), filter.getPrivateKeyPath());
            // create or Update JobResource 
            final DBItemInventoryConfiguration generatedJobResource = EnciphermentUtils.createRelatedJobResource(hibernateSession, filter, auditLog
                    .getId());
            // Deploy the JobResource to all controllers
            new Thread(() -> {
                byte[] deployFilter = createDeployFilter(xAccessToken, generatedJobResource, filter.getAuditLog());
                if (deployFilter != null) {
                    new DeployImpl().postDeploy(xAccessToken, deployFilter);
                }
            }).start();
            
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
    
    private byte[] createDeployFilter(String xAccessToken, DBItemInventoryConfiguration jobResource, AuditParams audit) {
        try {
            List<String> allowedControllerIds = Proxies.getControllerDbInstances().keySet().stream()
                    .filter(availableController -> getBasicControllerPermissions(availableController, xAccessToken)
                            .getDeployments().getDeploy()).collect(Collectors.toList());
            // TODO allowedControllerIds.isEmpty -> no permissions
            return EnciphermentUtils.createDeployFilter(allowedControllerIds, jobResource.getPath(), audit);
        } catch (JsonProcessingException e) {
            LOGGER.warn("error creating DeployFilter to deploy newly generated JobResource.", e);
            return null;
        }
    }
    
}
