package com.sos.joc.jobtemplate.impl;

import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.controller.model.jobtemplate.JobTemplate;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.inventory.DBItemInventoryReleasedConfiguration;
import com.sos.joc.db.inventory.InventoryDBLayer;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocFolderPermissionsException;
import com.sos.joc.jobtemplate.resource.IJobTemplateResource;
import com.sos.joc.jobtemplates.impl.AssignedWorkflowsImpl;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.joc.model.jobtemplate.JobTemplateFilter;
import com.sos.joc.model.jobtemplate.JobTemplateState;
import com.sos.joc.model.jobtemplate.JobTemplateStateFilter;
import com.sos.joc.model.jobtemplate.JobTemplateStateText;
import com.sos.schema.JsonValidator;

@Path("job_template")
public class JobTemplateResourceImpl extends JOCResourceImpl implements IJobTemplateResource {

    private static final String API_CALL = "./job_template";
    private static final String API_CALL_STATE = "./job_template/state";

    @Override
    public JOCDefaultResponse postJobTemplate(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, JobTemplateFilter.class);
            JobTemplateFilter jobTemplateFilter = Globals.objectMapper.readValue(filterBytes, JobTemplateFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getInventory().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            session = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            List<DBItemInventoryReleasedConfiguration> dbJobTemplates = dbLayer.getReleasedJobTemplatesByNames(Collections.singletonList(JocInventory
                    .pathToName(jobTemplateFilter.getJobTemplatePath())));

            com.sos.joc.model.jobtemplate.JobTemplate entity = new com.sos.joc.model.jobtemplate.JobTemplate();

            if (dbJobTemplates != null && !dbJobTemplates.isEmpty()) {
                DBItemInventoryReleasedConfiguration item = dbJobTemplates.get(0);
                if (!folderPermissions.isPermittedForFolder(item.getFolder())) {
                    throw new JocFolderPermissionsException(item.getFolder());
                }
                JobTemplate jt = Globals.objectMapper.readValue(item.getContent(), JobTemplate.class);
                jt.setPath(item.getPath());
                jt.setName(item.getName());
                entity.setJobTemplate(jt);

            } else {
                throw new DBMissingDataException(String.format("Couldn't find Job Template '%s'.", jobTemplateFilter.getJobTemplatePath()));
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
    
    @Override
    public JOCDefaultResponse postJobTemplateState(String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(API_CALL_STATE, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, JobTemplateStateFilter.class);
            JobTemplateStateFilter jobTemplateFilter = Globals.objectMapper.readValue(filterBytes, JobTemplateStateFilter.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getInventory().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            session = Globals.createSosHibernateStatelessConnection(API_CALL_STATE);
            InventoryDBLayer dbLayer = new InventoryDBLayer(session);
            DBItemInventoryReleasedConfiguration dbJobTemplate = dbLayer.getReleasedConfiguration(JocInventory.pathToName(jobTemplateFilter
                    .getJobTemplatePath()), ConfigurationType.JOBTEMPLATE.intValue());

            JobTemplateState entity = AssignedWorkflowsImpl.getState(JobTemplateStateText.UNKNOWN);
            if (dbJobTemplate == null) {
                entity = AssignedWorkflowsImpl.getState(JobTemplateStateText.DELETED);
            } else {
                com.sos.inventory.model.jobtemplate.JobTemplate jt = (com.sos.inventory.model.jobtemplate.JobTemplate) JocInventory.content2IJSObject(
                        dbJobTemplate.getContent(), ConfigurationType.JOBTEMPLATE.intValue());
                if (jt.getHash() != null && jt.getHash().equals(jobTemplateFilter.getHash())) {
                    entity = AssignedWorkflowsImpl.getState(JobTemplateStateText.IN_SYNC);
                } else {
                    entity = AssignedWorkflowsImpl.getState(JobTemplateStateText.NOT_IN_SYNC);
                }
            }
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
        SOSHibernateSession session = null;
        

}