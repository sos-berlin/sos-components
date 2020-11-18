package com.sos.joc.jobscheduler.impl;

import java.time.Instant;
import java.util.Date;

import javax.ws.rs.Path;

import com.sos.auth.rest.SOSShiroCurrentUser;
import com.sos.auth.rest.SOSShiroSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCPreferences;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.jobscheduler.resource.IJobSchedulerResourceSwitch;
import com.sos.joc.model.common.ControllerId;
import com.sos.schema.JsonValidator;

@Path("controller")
public class JobSchedulerResourceSwitchImpl extends JOCResourceImpl implements IJobSchedulerResourceSwitch {

    private static final String API_CALL = "./controller/switch";
    private static final String SESSION_KEY = "selectedInstance";

    @Override
    public JOCDefaultResponse postJobschedulerSwitch(String accessToken, byte[] filterBytes) {

        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ControllerId.class);
            ControllerId controllerId = Globals.objectMapper.readValue(filterBytes, ControllerId.class);

            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId.getControllerId(), getPermissonsJocCockpit(controllerId
                    .getControllerId(), accessToken).getJS7Controller().getView().isStatus());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SOSShiroCurrentUser shiroUser = jobschedulerUser.getSosShiroCurrentUser();
            JOCPreferences jocPreferences = new JOCPreferences(shiroUser.getUsername());
            String selectedInstance = controllerId.getControllerId();
            jocPreferences.put(WebserviceConstants.SELECTED_INSTANCE, selectedInstance);
            SOSShiroSession sosShiroSession = new SOSShiroSession(shiroUser);
            sosShiroSession.setAttribute(SESSION_KEY, selectedInstance);

            shiroUser.removeSchedulerInstanceDBItem(controllerId.getControllerId());

            try {
                Globals.forceClosingHttpClients(shiroUser, accessToken);
            } catch (Exception e) {
            }

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
