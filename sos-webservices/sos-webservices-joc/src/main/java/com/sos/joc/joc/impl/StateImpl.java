package com.sos.joc.joc.impl;

import java.net.UnknownHostException;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.cluster.JocInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.joc.resource.IStateResource;
import com.sos.joc.model.common.Ok;

@jakarta.ws.rs.Path("joc")
public class StateImpl extends JOCResourceImpl implements IStateResource {
    
    private static String API_CALL = "./joc/is_active";
    
    @Override
    public JOCDefaultResponse postIsActive(String accessToken) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, null, accessToken);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            JocInstancesDBLayer dbLayer = new JocInstancesDBLayer(connection);
            DBItemJocInstance activeInstance = dbLayer.getActiveInstance();
            //String currentTitle = Globals.sosCockpitProperties.getProperty("title", "");
            Ok entity = new Ok();
            if (activeInstance == null) {
                entity.setOk(false);
            } else {
                entity.setSurveyDate(activeInstance.getHeartBeat());
                entity.setOk(activeIsCurrent(activeInstance.getMemberId()));
            }
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

    @Override
    public JOCDefaultResponse getIsActive(String xAccessToken, String accessToken) {
        return postIsActive(getAccessToken(xAccessToken, accessToken));
    }
    
    private static Boolean activeIsCurrent(String activeMemberId) throws UnknownHostException {
        // see com.sos.joc.cluster.configuration.JocConfiguration constructor
        String curMemberId = getHostname() + ":" + SOSString.hash256(Paths.get(System.getProperty("user.dir")).toString());
        return curMemberId.equals(activeMemberId);
    }
    
    private static String getHostname() {
        String hostname = "unknown";
        try {
            hostname = SOSShell.getHostname();
        } catch (UnknownHostException e) {
            //
        }
        return hostname;
    }

}
