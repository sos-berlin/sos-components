package com.sos.joc.encipherment.impl;

import java.io.InputStream;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.stream.Collectors;

import org.glassfish.jersey.media.multipart.FormDataBodyPart;
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
import com.sos.joc.encipherment.resource.IImportCertificate;
import com.sos.joc.encipherment.util.EnciphermentUtils;
import com.sos.joc.exceptions.JocConcurrentAccessException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.encipherment.ImportCertificateRequestFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.DeployablesValidFilter;
import com.sos.joc.publish.impl.DeployImpl;
import com.sos.joc.publish.util.PublishUtils;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("encipherment/certificate")
public class ImportCertificateImpl extends JOCResourceImpl implements IImportCertificate {

    private static final String API_CALL = "./encipherment/certificate/import";
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportCertificateImpl.class);
    
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
        return postImportCertificate(xAccessToken, body, filter);
    }
    
    private JOCDefaultResponse postImportCertificate(String xAccessToken, FormDataBodyPart body, ImportCertificateRequestFilter filter) {
        SOSHibernateSession hibernateSession = null;
        InputStream stream = null;
        try {
            initLogging(API_CALL, filter.toString().getBytes(), xAccessToken);
            JsonValidator.validateFailFast(Globals.objectMapper.writeValueAsBytes(filter), ImportCertificateRequestFilter.class);
            //4-eyes principle cannot support uploads
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicJocPermissions(xAccessToken).getAdministration().getCertificates()
                    .getManage(), false);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            DBItemJocAuditLog auditLog = storeAuditLog(filter.getAuditLog(), CategoryType.CERTIFICATES);
            
            stream = body.getEntityAs(InputStream.class);
            String certificateFromFile = PublishUtils.readFileContent(stream);
            // simple check if filter.getCertificate() really is a certificate or public key
            KeyUtil.isInputCertOrPublicKey(certificateFromFile);
            hibernateSession = Globals.createSosHibernateStatelessConnection(API_CALL);
            DBLayerKeys dbLayer = new DBLayerKeys(hibernateSession);
            dbLayer.storeEnciphermentCertificate(filter.getCertAlias(), certificateFromFile, filter.getPrivateKeyPath());
            // create or Update JobResource 
            final DBItemInventoryConfiguration generatedJobResource = EnciphermentUtils
                    .createRelatedJobResource(hibernateSession, filter, certificateFromFile, auditLog.getId());
            
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

    private byte[] createDeployFilter (String xAccessToken, DBItemInventoryConfiguration jobResource, AuditParams audit) {
        try {
            DeployFilter deployFilter = new DeployFilter();
            Set<String> allowedControllerIds = Collections.emptySet();
            allowedControllerIds = Proxies.getControllerDbInstances().keySet().stream()
                    .filter(availableController -> getBasicControllerPermissions(availableController, xAccessToken)
                            .getDeployments().getDeploy()).collect(Collectors.toSet());
            deployFilter.setControllerIds(new ArrayList<String>(allowedControllerIds));
            deployFilter.setAuditLog(audit);
            DeployablesValidFilter toStore = new DeployablesValidFilter();
            deployFilter.setStore(toStore);
            Config jobResourceConfig = new Config();
            Configuration jobResourceDraft = new Configuration();
            jobResourceDraft.setPath(jobResource.getPath());
            jobResourceDraft.setObjectType(ConfigurationType.JOBRESOURCE);
            jobResourceConfig.setConfiguration(jobResourceDraft);
            toStore.getDraftConfigurations().add(jobResourceConfig);
            return Globals.objectMapper.writeValueAsBytes(deployFilter);
        } catch (JsonProcessingException e) {
            LOGGER.warn("error creating DeployFilter to deploy newly generated JobResource.");
            return null;
        }
    }
    
}
