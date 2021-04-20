package com.sos.joc.inventory.impl;

import java.time.Instant;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.inventory.JocInventory;
import com.sos.joc.db.deploy.DeployedConfigurationDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.inventory.resource.IStatisticsResource;
import com.sos.joc.model.controller.ControllerIdReq;
import com.sos.joc.model.inventory.Statistics;
import com.sos.joc.model.inventory.common.ConfigurationType;
import com.sos.schema.JsonValidator;

@Path(JocInventory.APPLICATION_PATH)
public class StatisticsResourceImpl extends JOCResourceImpl implements IStatisticsResource {

    @Override
    public JOCDefaultResponse getStatistics(final String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ControllerIdReq.class);
            ControllerIdReq controller = Globals.objectMapper.readValue(filterBytes, ControllerIdReq.class);

            JOCDefaultResponse response = initPermissions(null, getJocPermissions(accessToken).getInventory().getView());
            if (response != null) {
                return response;
            }
            
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DeployedConfigurationDBLayer dbLayer = new DeployedConfigurationDBLayer(session);
            Statistics entity = new Statistics();
            entity.setSurveyDate(Date.from(Instant.now()));
            Map<ConfigurationType, Long> numOfDeployed = dbLayer.getNumOfDeployedObjects(controller.getControllerId());
            Map<ConfigurationType, Long> numOfReleased = dbLayer.getNumOfReleasedObjects();
            entity.setNumOfWorkflows(numOfDeployed.getOrDefault(ConfigurationType.WORKFLOW, 0L));
            entity.setNumOfJobs(dbLayer.getNumOfDeployedJobs(controller.getControllerId()));
            entity.setNumOfLocks(numOfDeployed.getOrDefault(ConfigurationType.LOCK, 0L));
            entity.setNumOfJunctions(numOfDeployed.getOrDefault(ConfigurationType.JUNCTION, 0L));
            entity.setNumOfFileOrderSources(numOfDeployed.getOrDefault(ConfigurationType.FILEORDERSOURCE, 0L));
            entity.setNumOfSchedules(numOfReleased.getOrDefault(ConfigurationType.SCHEDULE, 0L));
            entity.setNumOfCalendars(numOfReleased.getOrDefault(ConfigurationType.WORKINGDAYSCALENDAR, 0L) + numOfReleased.getOrDefault(
                    ConfigurationType.NONWORKINGDAYSCALENDAR, 0L));
            entity.setDeliveryDate(Date.from(Instant.now()));
            
            return JOCDefaultResponse.responseStatus200(entity);
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }
}
