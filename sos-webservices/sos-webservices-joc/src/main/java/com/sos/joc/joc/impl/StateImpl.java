package com.sos.joc.joc.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.cluster.JocInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.joc.resource.IStateResource;
import com.sos.joc.model.common.Ok;

@javax.ws.rs.Path("joc")
public class StateImpl extends JOCResourceImpl implements IStateResource {
    
    private static String API_CALL = "./joc/state";
    
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
            String currentTitle = Globals.sosCockpitProperties.getProperty("title", "");
            Ok entity = new Ok();
            if (activeInstance == null) {
                entity.setOk(false);
            } else {
                entity.setSurveyDate(activeInstance.getHeartBeat());
                entity.setOk(currentTitle.isEmpty() || currentTitle.equals(activeInstance.getTitle()));
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

}
