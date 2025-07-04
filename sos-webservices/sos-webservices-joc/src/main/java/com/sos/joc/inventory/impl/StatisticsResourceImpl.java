package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.inventory.resource.IStatisticsResource;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Folder;
import com.sos.joc.model.controller.ControllerIdReq;
import com.sos.joc.model.inventory.Statistics;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocInventory.APPLICATION_PATH)
public class StatisticsResourceImpl extends JOCResourceImpl implements IStatisticsResource {

    @Override
    public JOCDefaultResponse getStatistics(final String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.INVENTORY);
            JsonValidator.validateFailFast(filterBytes, ControllerIdReq.class);
            ControllerIdReq controller = Globals.objectMapper.readValue(filterBytes, ControllerIdReq.class);
            String controllerId = controller.getControllerId();

            JOCDefaultResponse response = initPermissions(controllerId, getBasicJocPermissions(accessToken).getInventory().getView());
            if (response != null) {
                return response;
            }

            Set<Folder> permittedFolders = folderPermissions.getListOfFolders();
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
            Statistics entity = new Statistics();
            entity.setSurveyDate(Date.from(Instant.now()));
            Map<ConfigurationType, Long> numOfDeployed = dbLayer.getNumOfDeployedObjects(controllerId, permittedFolders);
            Map<ConfigurationType, Long> numOfReleased = dbLayer.getNumOfReleasedObjects(controllerId, permittedFolders);
            entity.setNumOfWorkflows(numOfDeployed.getOrDefault(ConfigurationType.WORKFLOW, 0L));
            entity.setNumOfJobs(dbLayer.getNumOfDeployedJobs(controllerId, permittedFolders));
            entity.setNumOfLocks(numOfDeployed.getOrDefault(ConfigurationType.LOCK, 0L));
            entity.setNumOfNoticeBoards(numOfDeployed.getOrDefault(ConfigurationType.NOTICEBOARD, 0L));
            entity.setNumOfJobResources(numOfDeployed.getOrDefault(ConfigurationType.JOBRESOURCE, 0L));
            entity.setNumOfFileOrderSources(numOfDeployed.getOrDefault(ConfigurationType.FILEORDERSOURCE, 0L));
            entity.setNumOfSchedules(numOfReleased.getOrDefault(ConfigurationType.SCHEDULE, 0L));
            entity.setNumOfIncludeScripts(numOfReleased.getOrDefault(ConfigurationType.INCLUDESCRIPT, 0L));
            entity.setNumOfJobTemplates(numOfReleased.getOrDefault(ConfigurationType.JOBTEMPLATE, 0L));
            entity.setNumOfCalendars(numOfReleased.getOrDefault(ConfigurationType.WORKINGDAYSCALENDAR, 0L) + numOfReleased.getOrDefault(
                    ConfigurationType.NONWORKINGDAYSCALENDAR, 0L));
            entity.setNumOfReports(numOfReleased.getOrDefault(ConfigurationType.REPORT, 0L));
            
            
            entity.setDeliveryDate(Date.from(Instant.now()));

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(session);
        }
    }
}
