package com.sos.joc.encipherment.util;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.inventory.model.job.Environment;
import com.sos.inventory.model.jobresource.JobResource;
import com.sos.joc.Globals;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.classes.inventory.Validator;
import com.sos.joc.db.inventory.DBItemInventoryConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.model.encipherment.ImportCertificateRequestFilter;
import com.sos.joc.model.encipherment.StoreCertificateRequestFilter;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.publish.db.DBLayerDeploy;
import com.sos.schema.exception.SOSJsonSchemaException;

public class EnciphermentUtils {

    public static final String ARG_NAME_ENCIPHERMENT_CERTIFICATE = "encipherment_certificate";
    public static final String ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH = "encipherment_private_key_path";

    public static void createRelatedJobResource(SOSHibernateSession hibernateSession, ImportCertificateRequestFilter filter,
            String certificate, Long auditLogId)
                    throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        createRelatedJobResource(hibernateSession, filter.getCertAlias(), certificate, filter.getPrivateKeyPath(),
                filter.getJobResourceFolder(), auditLogId);
    }
    
    public static void createRelatedJobResource(SOSHibernateSession hibernateSession, StoreCertificateRequestFilter filter,
            Long auditLogId) throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        createRelatedJobResource(hibernateSession, filter.getCertAlias(), filter.getCertificate(), 
                filter.getPrivateKeyPath(), filter.getJobResourceFolder(), auditLogId);
    }

    public static void createRelatedJobResource(SOSHibernateSession hibernateSession, String certAlias, String certificate,
            String privateKeyPath, String jobResourceFolder, Long auditLogId)
                    throws SOSHibernateException, JsonMappingException, JsonProcessingException {
        DBLayerDeploy dbLayer = new DBLayerDeploy(hibernateSession);
        DBItemInventoryConfiguration dbExistingJobResource = dbLayer.getInventoryConfigurationByNameAndType(certAlias,
                ConfigurationType.JOBRESOURCE.intValue());
        Path path = null;
        if (dbExistingJobResource != null) {
            JobResource existingJobResource = Globals.objectMapper.readValue(dbExistingJobResource.getContent(),
                    JobResource.class);
            Environment args = existingJobResource.getArguments();
            args.getAdditionalProperties().put(EnciphermentUtils.ARG_NAME_ENCIPHERMENT_CERTIFICATE, certificate);
            args.getAdditionalProperties().put(EnciphermentUtils.ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH, privateKeyPath);
            existingJobResource.setArguments(args);
            existingJobResource.setVersion(Globals.getStrippedInventoryVersion());
            dbExistingJobResource.setContent(Globals.objectMapper.writeValueAsString(existingJobResource));
            dbExistingJobResource.setFolder(jobResourceFolder);
            dbExistingJobResource.setPath(dbExistingJobResource.getFolder().concat("/").concat(certAlias));
            dbExistingJobResource.setAuditLogId(auditLogId);
            dbExistingJobResource.setModified(Date.from(Instant.now()));
            hibernateSession.update(dbExistingJobResource);
            path = Paths.get(dbExistingJobResource.getPath());
        } else {
            DBItemInventoryConfiguration newDBJobResource = new DBItemInventoryConfiguration();
            JobResource newJobResource = new JobResource();
            newJobResource.getArguments().getAdditionalProperties().
                put(EnciphermentUtils.ARG_NAME_ENCIPHERMENT_CERTIFICATE, certificate);
            newJobResource.getArguments().getAdditionalProperties().
                put(EnciphermentUtils.ARG_NAME_ENCIPHERMENT_PRIVATE_KEY_PATH, privateKeyPath);
            newJobResource.setVersion(Globals.getStrippedInventoryVersion());
            newDBJobResource.setName(certAlias);
            newDBJobResource.setFolder(jobResourceFolder);
            newDBJobResource.setPath(jobResourceFolder.concat("/").concat(certAlias));
            path = Paths.get(newDBJobResource.getPath());
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
                Validator.validate(ConfigurationType.JOBRESOURCE, Globals.objectMapper.writeValueAsBytes(newJobResource));
                newDBJobResource.setValid(true);
            } catch (Exception e) {
                newDBJobResource.setValid(false);
            }
            hibernateSession.save(newDBJobResource);
        }
        JocInventory.makeParentDirs(new InventoryDBLayer(hibernateSession), path.getParent(), ConfigurationType.FOLDER);
        JocInventory.postEvent(jobResourceFolder);
        JocInventory.postFolderEvent(jobResourceFolder);
    }
    
}
