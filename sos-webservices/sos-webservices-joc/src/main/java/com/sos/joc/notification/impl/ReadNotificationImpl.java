package com.sos.joc.notification.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.exceptions.JocException;
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
            initLogging(API_CALL, readNotificationFilter, xAccessToken);
            JsonValidator.validateFailFast(readNotificationFilter, ReadNotificationFilter.class);
            ReadConfiguration in = Globals.objectMapper.readValue(readNotificationFilter, ReadConfiguration.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getBasicJocPermissions(xAccessToken).getNotification().getView());
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }
            in.setObjectType(ObjectType.NOTIFICATION);
            JocXmlEditor.setRealPath();
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(ReadResourceImpl.handleStandardConfiguration(in)));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }
    
}
