package com.sos.joc.joc.impl;

import java.time.Instant;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.db.cluster.JocInstancesDBLayer;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.joc.resource.IUriResource;
import com.sos.joc.model.audit.CategoryType;
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
            filterBytes = initLogging(API_CALL, filterBytes, accessToken, CategoryType.OTHERS);
            JsonValidator.validate(filterBytes, CockpitURI.class);
            CockpitURI in = Globals.objectMapper.readValue(filterBytes, CockpitURI.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions("", getJocPermissions(accessToken).map(p -> p.getAdministration().getSettings()
                    .getManage()));
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
                JocClusterService.getInstance().updateJocUri(StartupMode.settings_changed, dbInstance.getMemberId(), dbInstance.getUri());
            }

            return responseStatusJSOk(Date.from(Instant.now()));
        } catch (Exception e) {
            return responseStatusJSError(e);
        } finally {
            Globals.disconnect(connection);
        }
    }

}
