package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Path;

import com.sos.auth.rest.SOSShiroCurrentUser;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCPreferences;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceIds;
import com.sos.joc.model.jobscheduler.ControllerIds;

@Path("controller")
public class JobSchedulerResourceIdsImpl extends JOCResourceImpl implements IJobSchedulerResourceIds {

    private static final String API_CALL = "./controller/ids";

    @Override
    public JOCDefaultResponse postJobschedulerIds(String accessToken) {
        SOSHibernateSession connection = null;

        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, null, accessToken, "", getPermissonsJocCockpit("", accessToken)
                    .getJS7Controller().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            SOSShiroCurrentUser shiroUser = jobschedulerUser.getSosShiroCurrentUser();
            JOCPreferences jocPreferences = new JOCPreferences(shiroUser.getUsername());

            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            InventoryInstancesDBLayer dbLayer = new InventoryInstancesDBLayer(connection);
            List<String> schedulerIds = dbLayer.getControllerIds();
            Set<String> jobSchedulerIds = new HashSet<>();
            String first = null;
            if (schedulerIds != null && !schedulerIds.isEmpty()) {
                for (String schedulerId : schedulerIds) {
                    if (schedulerId == null || schedulerId.isEmpty()) {
                        continue;
                    }
                    if (!getPermissonsJocCockpit(schedulerId, accessToken).getJS7ControllerCluster().getView().isStatus()
                            && !getPermissonsJocCockpit(schedulerId, accessToken).getJS7Controller().getView().isStatus()) {
                        continue;
                    }
                    jobSchedulerIds.add(schedulerId);
                    if (first == null) {
                        first = schedulerId;
                    }
                }
                if (jobSchedulerIds.isEmpty()) {
                    return accessDeniedResponse();
                }
            } else {
                // throw new DBMissingDataException("No JobSchedulers found in DB!");
            }
            String selectedInstanceSchedulerId = jocPreferences.get(WebserviceConstants.SELECTED_INSTANCE, first);

            if (!jobSchedulerIds.contains(selectedInstanceSchedulerId)) {
                if (first != null) {
                    selectedInstanceSchedulerId = first;
                    jocPreferences.put(WebserviceConstants.SELECTED_INSTANCE, first);
                }
            }

            ControllerIds entity = new ControllerIds();
            entity.getControllerIds().addAll(jobSchedulerIds);
            entity.setSelected(selectedInstanceSchedulerId);
            entity.setDeliveryDate(Date.from(Instant.now()));

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

}
