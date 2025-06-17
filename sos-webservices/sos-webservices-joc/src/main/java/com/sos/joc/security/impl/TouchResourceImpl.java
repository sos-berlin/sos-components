package com.sos.joc.security.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.SessionNotExistException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.security.resource.ITouchResource;

import jakarta.ws.rs.Path;

@Path("touch")
public class TouchResourceImpl extends JOCResourceImpl implements ITouchResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(TouchResourceImpl.class);
    private static final String API_CALL = "./touch";

    @Override
    public JOCDefaultResponse postTouch(String xAccessToken, String accessToken) {
        return postTouch(getAccessToken(xAccessToken, accessToken));
    }

    public JOCDefaultResponse postTouch(String accessToken) {
        try {
            initLogging(API_CALL, null, accessToken, CategoryType.OTHERS);
            if (!jobschedulerUser.isAuthenticated()) {
                return responseStatus401(JOCDefaultResponse.getError401Schema(jobschedulerUser, getJocError()));
            }
            try {
                 jobschedulerUser.resetTimeOut();
            } catch (SessionNotExistException e) {
                LOGGER.info(e.getMessage());
            }
            return responseStatusJSOk(null);
        } catch (DBConnectionRefusedException e) {
        	LOGGER.info(e.getMessage());
        	return responseStatusJSOk(null);
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
    
    @Override
    public JOCDefaultResponse postTouchLog4j(String accessToken) {
        try {
            initLogging(API_CALL, null, accessToken, CategoryType.OTHERS);
            if (Globals.sosCockpitProperties == null) {
                Globals.sosCockpitProperties = new JocCockpitProperties();
            } else {
                Globals.sosCockpitProperties.touchLog4JConfiguration();
            }
            return responseStatusJSOk(null);
        } catch (DBConnectionRefusedException e) {
            LOGGER.info(e.getMessage());
            return responseStatusJSOk(null);
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }
}
