package com.sos.joc.joc.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.cluster.JocInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.joc.resource.IStateResource;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.common.Ok;

@jakarta.ws.rs.Path("joc")
public class StateImpl extends JOCResourceImpl implements IStateResource {
    
    private static String API_CALL = "./joc/is_active";
    
    @Override
    public JOCDefaultResponse postIsActive(String accessToken) {
        try {
            initLogging(API_CALL, "{}".getBytes(), accessToken, CategoryType.OTHERS);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", true);
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            Ok entity = new Ok();
            DBItemJocInstance activeInstance = getActiveInstance(API_CALL, null);
            if (activeInstance == null) {
                entity.setOk(false);
            } else {
                entity.setSurveyDate(activeInstance.getHeartBeat());
                entity.setOk(Globals.getMemberId().equals(activeInstance.getMemberId()));
            }
            entity.setDeliveryDate(Date.from(Instant.now()));
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(entity));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    @Override
    public JOCDefaultResponse getIsActive(String xAccessToken, String accessToken) {
        return postIsActive(getAccessToken(xAccessToken, accessToken));
    }
    
    public static Boolean isActive(String apiCall, JocInstancesDBLayer dbLayer) {
        DBItemJocInstance activeInstance = getActiveInstance(apiCall, dbLayer);
        if (activeInstance == null) {
            return false;
        }
        return Globals.getMemberId().equals(activeInstance.getMemberId());
    }
    
    private static DBItemJocInstance getActiveInstance(String apiCall, JocInstancesDBLayer dbLayer) {
        SOSHibernateSession connection = null;
        try {
            if (dbLayer == null) {
                connection = Globals.createSosHibernateStatelessConnection(apiCall);
                dbLayer = new JocInstancesDBLayer(connection);
            }
            return dbLayer.getActiveInstance();
        } finally {
            Globals.disconnect(connection);
        }
    }

}
