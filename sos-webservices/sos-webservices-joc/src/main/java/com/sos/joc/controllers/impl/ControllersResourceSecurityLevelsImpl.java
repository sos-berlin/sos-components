package com.sos.joc.controllers.impl;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import jakarta.ws.rs.Path;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.controllers.resource.IControllersResourceSecurityLevels;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.controller.Controller;
import com.sos.joc.model.controller.ControllerId;
import com.sos.joc.model.controller.Controllers;
import com.sos.schema.JsonValidator;

@Path("controllers")
public class ControllersResourceSecurityLevelsImpl extends JOCResourceImpl implements IControllersResourceSecurityLevels {

    private static final String API_CALL_LEVELS = "./controllers/security_level";
    private static final String API_CALL_TAKEOVER = API_CALL_LEVELS + "/take_over";

    @Override
    public JOCDefaultResponse postControllerIdsWithSecurityLevel(String accessToken) {
        SOSHibernateSession connection = null;

        try {
            initLogging(API_CALL_LEVELS, null, accessToken);
            com.sos.joc.model.security.configuration.permissions.joc.admin.Controllers controllerPermissions = getJocPermissions(accessToken).getAdministration()
                    .getControllers();
            // TODO admin permissions to take over security level
            boolean adminPermission = controllerPermissions.getManage();
            boolean showPermission = controllerPermissions.getView();
            
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
            boolean permission = getJocPermissions(accessToken).getAdministration().getControllers().getManage();

            JOCDefaultResponse jocDefaultResponse = initPermissions("", permission);
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
