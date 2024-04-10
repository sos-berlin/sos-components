package com.sos.joc.joc.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.cluster.JocInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.joc.resource.IUriResource;
import com.sos.joc.model.joc.CockpitURI;
import com.sos.schema.JsonValidator;

@jakarta.ws.rs.Path("joc")
public class UriImpl extends JOCResourceImpl implements IUriResource {
    
    private final static String API_CALL = "./joc/url";
    
    @Override
    public JOCDefaultResponse setUrl(String accessToken, byte[] filterBytes) {
        return setUri(accessToken, filterBytes);
    }
    
    @Override
    public JOCDefaultResponse setUri(String accessToken, byte[] filterBytes) {
        SOSHibernateSession connection = null;
        try {
            initLogging(API_CALL, filterBytes, accessToken);
            JsonValidator.validate(filterBytes, CockpitURI.class);
            CockpitURI in = Globals.objectMapper.readValue(filterBytes, CockpitURI.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).getAdministration().getSettings().getManage());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            
            String instanceId = in.getInstanceId();
            if (instanceId == null || instanceId.isBlank()) {
                instanceId = Globals.getJocId();
            }
            
            if (!instanceId.matches("[^#]+#\\d+")) {
                throw new JocBadRequestException("Invalid JOC instance ID. It has to be the form <clusterId>#<ordering>, e.g. joc#0");
            }
            
            String[] instanceIdParts = instanceId.split("#");
            
            connection = Globals.createSosHibernateStatelessConnection(API_CALL);
            JocInstancesDBLayer dbLayer = new JocInstancesDBLayer(connection);
            DBItemJocInstance dbInstance = dbLayer.getInstance(instanceIdParts[0], Integer.valueOf(instanceIdParts[1]));
            
            if (dbInstance == null) {
                throw new DBMissingDataException("Couldn't find JOC instance with ID '" + instanceId + "'");
            }
            
            if (dbInstance.getUri() == null || !dbInstance.getUri().equals(in.getUrl())) {
                dbInstance.setUri(in.getUrl());
                connection.update(dbInstance);
                EventBus.getInstance().post(new ActiveClusterChangedEvent());
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
