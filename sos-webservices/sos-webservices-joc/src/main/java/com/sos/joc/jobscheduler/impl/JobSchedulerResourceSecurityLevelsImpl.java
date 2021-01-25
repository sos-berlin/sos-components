package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Path;

import com.sos.auth.rest.permission.model.SOSPermissionJocCockpit.JS7Controller;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceSecurityLevels;
import com.sos.joc.model.common.ControllerId;
import com.sos.joc.model.controller.Controller;
import com.sos.joc.model.controller.Controllers;
import com.sos.schema.JsonValidator;

@Path("controllers")
public class JobSchedulerResourceSecurityLevelsImpl extends JOCResourceImpl implements IJobSchedulerResourceSecurityLevels {

    private static final String API_CALL_LEVELS = "./controllers/security_level";
    private static final String API_CALL_TAKEOVER = API_CALL_LEVELS + "/take_over";

    @Override
    public JOCDefaultResponse postControllerIdsWithSecurityLevel(String accessToken) {
        SOSHibernateSession connection = null;

        try {
            initLogging(API_CALL_LEVELS, null, accessToken);
            JS7Controller controllerPermissions = getPermissonsJocCockpit("", accessToken).getJS7Controller();
            // TODO admin permissions to take over security level
            boolean adminPermission = controllerPermissions.getAdministration().isEditPermissions();
            boolean showPermission = controllerPermissions.getView().isStatus();
            
            JOCDefaultResponse jocDefaultResponse = initPermissions("", adminPermission || showPermission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_LEVELS);
            InventoryInstancesDBLayer dbLayer = new InventoryInstancesDBLayer(connection);
            List<Controller> controllers = dbLayer.getControllerIdsWithSecurityLevel(!adminPermission);
            
            Controllers entity = new Controllers();
            entity.setDeliveryDate(Date.from(Instant.now()));
            entity.setControllers(controllers);
            entity.setCurrentSecurityLevel(Globals.getJocSecurityLevel());

            return JOCDefaultResponse.responseStatus200(entity);

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

    @Override
    public JOCDefaultResponse takeOverSecurityLevel(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;

        try {
            initLogging(API_CALL_TAKEOVER, null, accessToken);
            JsonValidator.validateFailFast(filterBytes, ControllerId.class);
            ControllerId controllerId = Globals.objectMapper.readValue(filterBytes, ControllerId.class);
            
            // TODO admin permissions to take over security level
            boolean permission = getPermissonsJocCockpit(controllerId.getControllerId(), accessToken).getJS7Controller().getAdministration()
                    .isEditPermissions();

            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId.getControllerId(), permission);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            connection = Globals.createSosHibernateStatelessConnection(API_CALL_TAKEOVER);
            InventoryInstancesDBLayer dbLayer = new InventoryInstancesDBLayer(connection);
            List<DBItemInventoryJSInstance> controllers = dbLayer.getInventoryInstancesOfAllSecurityLevels(controllerId.getControllerId());
            if (controllers != null) {
                Integer securityLevel = Globals.getJocSecurityLevel().intValue();
                for (DBItemInventoryJSInstance controller : controllers) {
                    if (securityLevel != controller.getSecurityLevel()) {
                        controller.setSecurityLevel(securityLevel);
                        dbLayer.saveInstance(controller);
                    }
                }
            }
            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(connection);
        }
    }

}
