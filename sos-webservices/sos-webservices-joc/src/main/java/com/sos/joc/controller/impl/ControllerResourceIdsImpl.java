package com.sos.joc.controller.impl;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCPreferences;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.controller.resource.IControllerResourceIds;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.controller.ControllerIds;

import jakarta.ws.rs.Path;

@Path("controller")
public class ControllerResourceIdsImpl extends JOCResourceImpl implements IControllerResourceIds {

    private static final String API_CALL = "./controller/ids";

    @Override
    public JOCDefaultResponse postJobschedulerIds(String accessToken) {
        SOSHibernateSession connection = null;

        try {
            initLogging(API_CALL, null, accessToken, CategoryType.CONTROLLER);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            JOCPreferences jocPreferences = new JOCPreferences(jobschedulerUser.getSOSAuthCurrentAccount().getAccountname());

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryInstancesDBLayer dbLayer = new InventoryInstancesDBLayer(connection);
            List<String> dbControllerIds = dbLayer.getControllerIds();
            Set<String> controllerIds = new HashSet<>();
            String first = null;
            
            if (dbControllerIds != null && !dbControllerIds.isEmpty()) {
                for (String dbControllerId : dbControllerIds) {
                    if (dbControllerId == null || dbControllerId.isEmpty()) {
                        continue;
                    }
                    if (!getBasicControllerPermissions(dbControllerId, accessToken).getView()) {
                        continue;
                    }
                    controllerIds.add(dbControllerId);
                    if (first == null) {
                        first = dbControllerId;
                    }
                }
                if (controllerIds.isEmpty()) {
                    return accessDeniedResponse();
                }
            } else {
                // throw new DBMissingDataException("No Controllers found!");
            }
            String selectedInstanceSchedulerId = jocPreferences.get(WebserviceConstants.SELECTED_INSTANCE, first);

            if (!controllerIds.contains(selectedInstanceSchedulerId)) {
                if (first != null) {
                    selectedInstanceSchedulerId = first;
                    jocPreferences.put(WebserviceConstants.SELECTED_INSTANCE, first);
                }
            }

            ControllerIds entity = new ControllerIds();
            entity.setControllerIds(controllerIds);
            entity.setSelected(selectedInstanceSchedulerId);
            entity.setDeliveryDate(Date.from(Instant.now()));

            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));

        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }

    }

}
