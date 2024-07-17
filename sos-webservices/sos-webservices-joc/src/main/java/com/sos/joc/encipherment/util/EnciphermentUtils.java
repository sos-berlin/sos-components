package com.sos.joc.encipherment.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;
import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.JsonConverter;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.model.audit.AuditParams;
import com.sos.joc.model.encipherment.ImportCertificateRequestFilter;
import com.sos.joc.model.encipherment.StoreCertificateRequestFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.publish.Config;
import com.sos.joc.model.publish.Configuration;
import com.sos.joc.model.publish.DeployFilter;
import com.sos.joc.model.publish.DeployablesValidFilter;
import com.sos.joc.publish.db.DBLayerDeploy;

public class EnciphermentUtils {

    public static final String ARG_NAME_ENCIPHERMENT_CERTIFICATE = "encipherment_certificate";
    public static final String ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH = "encipherment_private_key_path";

    public static DBItemInventoryConfiguration createRelatedJobResource(SOSHibernateSession hibernateSession, ImportCertificateRequestFilter filter,
            String certificate, Long auditLogId)
                    throws SOSHibernateException, IOException {
        return createRelatedJobResource(hibernateSession, filter.getCertAlias(), certificate, filter.getPrivateKeyPath(),
                filter.getJobResourceFolder(), auditLogId);
    }
    
    public static DBItemInventoryConfiguration createRelatedJobResource(SOSHibernateSession hibernateSession, StoreCertificateRequestFilter filter,
            Long auditLogId) throws SOSHibernateException, IOException {
        return createRelatedJobResource(hibernateSession, filter.getCertAlias(), filter.getCertificate(), 
                filter.getPrivateKeyPath(), filter.getJobResourceFolder(), auditLogId);
    }

    public static DBItemInventoryConfiguration createRelatedJobResource(SOSHibernateSession hibernateSession, String certAlias, String certificate,
            String privateKeyPath, String jobResourceFolder, Long auditLogId)
                    throws SOSHibernateException, IOException {
        DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
        DBItemInventoryConfiguration dbExistingJobResource = dbLayer.getInventoryConfigurationByNameAndType(certAlias,
                ConfigurationType.JOBRESOURCE.intValue());
        Path path = null;
        DBItemInventoryConfiguration jr = null;
        if (dbExistingJobResource != null) {
            JobResource existingJobResource = (JobResource) JocInventory.content2IJSObject(dbExistingJobResource.getContent(),
                    ConfigurationType.JOBRESOURCE);
            Environment args = existingJobResource.getArguments();
            if (args == null) {
                args = new Environment();
            }
            args.setAdditionalProperty(ARG_NAME_ENCIPHERMENT_CERTIFICATE, JsonConverter.quoteString(certificate));
            if(privateKeyPath != null && !privateKeyPath.isEmpty()) {
              args.setAdditionalProperty(ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH, JsonConverter.quoteString(privateKeyPath));
            }
            existingJobResource.setArguments(args);
            Environment env = existingJobResource.getEnv();
            if(env == null) {
                env = new Environment();
            }
            env.setAdditionalProperty(ARG_NAME_ENCIPHERMENT_CERTIFICATE.toUpperCase(), "$".concat(ARG_NAME_ENCIPHERMENT_CERTIFICATE));
            if(privateKeyPath != null && !privateKeyPath.isEmpty()) {
              env.setAdditionalProperty(ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH.toUpperCase(), "$".concat(ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH));
            }
            existingJobResource.setEnv(env);
            dbExistingJobResource.setContent(Globals.objectMapper.writeValueAsString(existingJobResource));
            dbExistingJobResource.setFolder(jobResourceFolder);
            path = Paths.get(dbExistingJobResource.getFolder()).resolve(certAlias);
            dbExistingJobResource.setPath(path.toString().replace('\\', '/'));
            dbExistingJobResource.setAuditLogId(auditLogId);
            dbExistingJobResource.setModified(Date.from(Instant.now()));
            hibernateSession.update(dbExistingJobResource);
            jr = dbExistingJobResource;
        } else {
            DBItemInventoryConfiguration newDBJobResource = new DBItemInventoryConfiguration();
            JobResource newJobResource = new JobResource();
            Environment args = new Environment();
            args.setAdditionalProperty(EnciphermentUtils.ARG_NAME_ENCIPHERMENT_CERTIFICATE, JsonConverter.quoteString(certificate));
            args.setAdditionalProperty(EnciphermentUtils.ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH, JsonConverter.quoteString(privateKeyPath));
            newJobResource.setArguments(args);
            Environment env = new Environment();
            env.setAdditionalProperty(ARG_NAME_ENCIPHERMENT_CERTIFICATE.toUpperCase(),"$".concat(ARG_NAME_ENCIPHERMENT_CERTIFICATE));
            env.setAdditionalProperty(ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH.toUpperCase(), "$".concat(ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH));
            newJobResource.setEnv(env);
            newJobResource.setVersion(Globals.getStrippedInventoryVersion());
            newDBJobResource.setName(certAlias);
            newDBJobResource.setFolder(jobResourceFolder);
            path = Paths.get(jobResourceFolder).resolve(certAlias);
            newDBJobResource.setPath(path.toString().replace('\\', '/'));
            Date now = Date.from(Instant.now());
            newDBJobResource.setType(ConfigurationType.JOBRESOURCE);
            newDBJobResource.setContent(Globals.objectMapper.writeValueAsString(newJobResource));
            newDBJobResource.setAuditLogId(auditLogId);
            newDBJobResource.setDeleted(false);
            newDBJobResource.setDeployed(false);
            newDBJobResource.setReleased(false);
            newDBJobResource.setRepoControlled(false);
            newDBJobResource.setCreated(now);
            newDBJobResource.setModified(now);
            try {
                Validator.validate(ConfigurationType.JOBRESOURCE, newJobResource);
                newDBJobResource.setValid(true);
            } catch (Exception e) {
                newDBJobResource.setValid(false);
            }
            hibernateSession.save(newDBJobResource);
            jr = newDBJobResource;
        }
        JocInventory.makeParentDirs(new InventoryDBLayer(hibernateSession), path.getParent(), ConfigurationType.FOLDER);
        JocInventory.postEvent(jobResourceFolder);
        JocInventory.postFolderEvent(jobResourceFolder);
        return jr;
    }
    
    public static byte[] createDeployFilter(List<String> controllerIds, String jobResourcePath, AuditParams audit) throws JsonProcessingException {
        DeployFilter deployFilter = new DeployFilter();
        deployFilter.setControllerIds(controllerIds);
        deployFilter.setAuditLog(audit);
        DeployablesValidFilter toStore = new DeployablesValidFilter();
        Config jobResourceConfig = new Config();
        Configuration jobResourceDraft = new Configuration();
        jobResourceDraft.setPath(jobResourcePath);
        jobResourceDraft.setObjectType(ConfigurationType.JOBRESOURCE);
        jobResourceConfig.setConfiguration(jobResourceDraft);
        toStore.getDraftConfigurations().add(jobResourceConfig);
        deployFilter.setStore(toStore);
        return Globals.objectMapper.writeValueAsBytes(deployFilter);
    }
    
}
