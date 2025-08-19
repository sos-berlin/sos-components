package com.sos.joc.notification.impl;

import com.sos.commons.xml.exception.SOSXMLXSDValidatorException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.db.joc.DBItemJocAuditLog;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.notification.ReleaseNotificationFilter;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.model.xmleditor.release.ReleaseConfiguration;
import com.sos.joc.notification.resource.IReleaseNotification;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;
import com.sos.joc.xmleditor.impl.ReadResourceImpl;
import com.sos.joc.xmleditor.impl.StandardNotificationReleaseResourceImpl;
import com.sos.joc.xmleditor.impl.ValidateResourceImpl;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path("notification")
public class ReleaseNotificationImpl extends JOCResourceImpl implements IReleaseNotification {

    private static final String API_CALL = "./notification/release";

    @Override
    public JOCDefaultResponse postReleaseNotification(String xAccessToken, byte[] inBytes) {
        try {
            inBytes = initLogging(API_CALL, inBytes, xAccessToken, CategoryType.MONITORING);
            JsonValidator.validateFailFast(inBytes, ReleaseNotificationFilter.class);
            ReleaseConfiguration in = Globals.objectMapper.readValue(inBytes, ReleaseConfiguration.class);
            JOCDefaultResponse jocDefaultResponse = initPermissions(null, getJocPermissions(xAccessToken).map(p -> p.getNotification().getManage()));
            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            DBItemJocAuditLog dbAuditlog = storeAuditLog(in.getAuditLog());
            in.setObjectType(ObjectType.NOTIFICATION);

            // step 0 - use existing configuration if it is not set in the request
            if (in.getConfiguration() == null || in.getConfiguration().isEmpty()) {
                DBItemXmlEditorConfiguration item = ReadResourceImpl.getItem(in.getObjectType().name(), StandardSchemaHandler
                        .getDefaultConfigurationName(in.getObjectType()));
                StandardSchemaHandler handler = new StandardSchemaHandler(in.getObjectType());
                handler.readCurrent(item, false);
                ReadStandardConfigurationAnswer answer = handler.getAnswer();
                if (answer.getConfiguration() == null || answer.getConfiguration().isEmpty()) {
                    throw new DBMissingDataException("Couldn't find the configuration of the notifications.");
                }
                in.setConfiguration(answer.getConfiguration());
                in.setConfigurationJson(answer.getConfigurationJson());
            }

            // step 1 - check for vulnerabilities and validate
            try {
                JocXmlEditor.validate(in.getObjectType(), StandardSchemaHandler.getNotificationSchema(), in.getConfiguration());
            } catch (SOSXMLXSDValidatorException e) {
                // LOGGER.error(String.format("[%s]%s", schema, e.toString()), e);
                return responseStatus200(Globals.objectMapper.writeValueAsBytes(ValidateResourceImpl.getError(e)));
            }

            // step 2 - update db and post NotificationConfigurationReleased event
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(StandardNotificationReleaseResourceImpl
                    .handleStandardConfiguration(in, getAccount(), dbAuditlog.getId())));

        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

}
