package com.sos.joc.notification.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.notification.ReadNotificationFilter;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.read.ReadConfiguration;
import com.sos.joc.notification.resource.IReadNotification;
import com.sos.joc.xmleditor.impl.ReadResourceImpl;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("notification")
public class ReadNotificationImpl extends JOCResourceImpl implements IReadNotification {

    private static final String API_CALL = "./notification";

    @Override
    public JOCDefaultResponse postReadNotification(String xAccessToken, byte[] readNotificationFilter) {
        try {
            readNotificationFilter = initLogging(API_CALL, readNotificationFilter, xAccessToken, CategoryType.MONITORING);
            JsonValidator.validateFailFast(readNotificationFilter, ReadNotificationFilter.class);
            ReadConfiguration in = Globals.objectMapper.readValue(readNotificationFilter, ReadConfiguration.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getBasicJocPermissions(xAccessToken).getNotification().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            in.setObjectType(ObjectType.NOTIFICATION);
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(ReadResourceImpl.getNotificationConfiguration(in)));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
