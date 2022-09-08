package com.sos.joc.jobtemplates.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.controller.model.jobtemplate.JobTemplate;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobtemplates.resource.IJobTemplatesResource;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.jobtemplate.JobTemplates;
import com.sos.joc.model.jobtemplate.JobTemplatesFilter;
import com.sos.schema.JsonValidator;

@Path("job_templates")
public class JobTemplatesResourceImpl extends JOCResourceImpl implements IJobTemplatesResource {

    private static final String API_CALL = "./job_templates";
    private static final Logger LOGGER = LoggerFactory.getLogger(JobTemplatesResourceImpl.class);

    @Override
    public JOCDefaultResponse postJobTemplates(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, JobTemplatesFilter.class);
            JobTemplatesFilter jobTemplatesFilter = Globals.objectMapper.readValue(filterBytes, JobTemplatesFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getInventory().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            final Set<Folder> folders = folderPermissions.getPermittedFolders(jobTemplatesFilter.getFolders());
            
            List<DBItemInventoryReleasedConfiguration> dbJobTemplates = getDbJobTemplates(jobTemplatesFilter, folders, dbLayer);
            JobTemplates entity = new JobTemplates();

            if (dbJobTemplates != null && !dbJobTemplates.isEmpty()) {
                JocError jocError = getJocError();

                entity.setJobTemplates(dbJobTemplates.stream().filter(item -> folderIsPermitted(item.getFolder(), folders)).map(
                        item -> getJobTemplate(item, jobTemplatesFilter.getCompact(), jocError)).filter(Objects::nonNull).collect(Collectors
                                .toList()));
            }

            entity.setDeliveryDate(Date.from(Instant.now()));
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
    
    public static JobTemplate getJobTemplate(DBItemInventoryReleasedConfiguration item, Boolean compact, JocError jocError) {
        try {
            JobTemplate jt = new JobTemplate();
            if (compact != Boolean.TRUE) {
                jt = Globals.objectMapper.readValue(item.getContent(), JobTemplate.class);
            } else {
                JobTemplate jt2 = Globals.objectMapper.readValue(item.getContent(), JobTemplate.class);
                jt.setDocumentationName(jt2.getDocumentationName());
                jt.setDescription(jt2.getDescription());
                jt.setTitle(jt2.getTitle());
                jt.setHash(jt2.getHash());
                // following fields have default values in JSON schema
                jt.setVersion(null);
                jt.setFailOnErrWritten(null);
                jt.setSkipIfNoAdmissionForOrderDay(null);
                jt.setGraceTimeout(null);
                jt.setParallelism(null);
            }
            jt.setPath(item.getPath());
            jt.setName(item.getName());
            return jt;
        } catch (Exception e) {
            if (jocError != null && !jocError.getMetaInfo().isEmpty()) {
                LOGGER.info(jocError.printMetaInfo());
                jocError.clearMetaInfo();
            }
            LOGGER.error(String.format("[%s] %s", item.getPath(), e.toString()));
            return null;
        }
    }
    
    public static List<DBItemInventoryReleasedConfiguration> getDbJobTemplates(JobTemplatesFilter jobTemplatesFilter, Set<Folder> folders,
            InventoryDBLayer dbLayer) throws SOSHibernateException {

        List<DBItemInventoryReleasedConfiguration> dbJobTemplates = null;
        boolean withFolderFilter = jobTemplatesFilter.getFolders() != null && !jobTemplatesFilter.getFolders().isEmpty();

        if (jobTemplatesFilter.getJobTemplatePaths() != null && !jobTemplatesFilter.getJobTemplatePaths().isEmpty()) {
            dbJobTemplates = dbLayer.getReleasedJobTemplatesByNames(jobTemplatesFilter.getJobTemplatePaths().stream().map(p -> JocInventory
                    .pathToName(p)).distinct().collect(Collectors.toList()));

        } else if (withFolderFilter && (folders == null || folders.isEmpty())) {
            // no folder permission
        } else {
            dbJobTemplates = dbLayer.getConfigurationsByType(Collections.singletonList(ConfigurationType.JOBTEMPLATE.intValue()));
        }
        return dbJobTemplates;
    }

}