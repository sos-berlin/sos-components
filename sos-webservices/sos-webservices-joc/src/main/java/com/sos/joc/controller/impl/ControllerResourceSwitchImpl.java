package com.sos.joc.controller.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.auth.classes.SOSAuthCurrentAccount;
import com.sos.auth.classes.SOSSessionHandler;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCPreferences;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.WebserviceConstants;
import com.sos.joc.controller.resource.IControllerResourceSwitch;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.controller.ControllerIdReq;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("controller")
public class ControllerResourceSwitchImpl extends JOCResourceImpl implements IControllerResourceSwitch {

    private static final String API_CALL = "./controller/switch";
    private static final String SESSION_KEY = "selectedInstance";

    @Override
    public JOCDefaultResponse postJobschedulerSwitch(String accessToken, byte[] filterBytes) {

        try {
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.CONTROLLER);
            JsonValidator.validateFailFast(filterBytes, ControllerIdReq.class);
            ControllerIdReq controller = Globals.objectMapper.readValue(filterBytes, ControllerIdReq.class);
            String controllerId = controller.getControllerId();
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getBasicControllerPermissions(controllerId, accessToken).getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            SOSAuthCurrentAccount sosAuthCurrentAccount = jobschedulerUser.getSOSAuthCurrentAccount();
            JOCPreferences jocPreferences = new JOCPreferences(sosAuthCurrentAccount.getAccountname());
            String selectedInstance = controllerId;
            jocPreferences.put(WebserviceConstants.SELECTED_INSTANCE, selectedInstance);
            SOSSessionHandler sosSessionHandler = new SOSSessionHandler(sosAuthCurrentAccount);
            sosSessionHandler.setAttribute(SESSION_KEY, selectedInstance);

            return responseStatusJSOk(Date.from(Instant.now()));

        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
}
