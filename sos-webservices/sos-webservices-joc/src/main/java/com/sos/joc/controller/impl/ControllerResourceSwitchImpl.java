package com.sos.joc.controller.impl;

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
import com.sos.joc.controller.resource.IControllerResourceSwitch;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.common.ControllerId;
import com.sos.schema.JsonValidator;

@Path("controller")
public class ControllerResourceSwitchImpl extends JOCResourceImpl implements IControllerResourceSwitch {

    private static final String API_CALL = "./controller/switch";
    private static final String SESSION_KEY = "selectedInstance";

    @Override
    public JOCDefaultResponse postJobschedulerSwitch(String accessToken, byte[] filterBytes) {

        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ControllerId.class);
            ControllerId controller = Globals.objectMapper.readValue(filterBytes, ControllerId.class);
            String controllerId = controller.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions(controllerId, getControllerPermissions(controllerId, accessToken).getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SOSShiroCurrentUser shiroUser = jobschedulerUser.getSosShiroCurrentUser();
            JOCPreferences jocPreferences = new JOCPreferences(shiroUser.getUsername());
            String selectedInstance = controllerId;
            jocPreferences.put(WebserviceConstants.SELECTED_INSTANCE, selectedInstance);
            SOSShiroSession sosShiroSession = new SOSShiroSession(shiroUser);
            sosShiroSession.setAttribute(SESSION_KEY, selectedInstance);

            return JOCDefaultResponse.responseStatusJSOk(Date.from(Instant.now()));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
}
