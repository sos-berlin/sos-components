package com.sos.joc.xmleditor.impl;

import com.sos.commons.xml.exception.SOSXMLXSDValidatorException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.monitoring.NotificationConfigurationReleased;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.model.xmleditor.release.ReleaseConfiguration;
import com.sos.joc.notification.impl.ReleaseNotificationImpl;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;
import com.sos.joc.xmleditor.resource.IStandardNotificationReleaseResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

/** Not used by JOC - see {@link ReleaseNotificationImpl#postReleaseNotification(String, byte[])} */
@Path(JocXmlEditor.APPLICATION_PATH)
public class StandardNotificationReleaseResourceImpl extends ACommonResourceImpl implements IStandardNotificationReleaseResource {

    @Override
    public JOCDefaultResponse release(final String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.SETTINGS);
            JsonValidator.validateFailFast(filterBytes, ReleaseConfiguration.class);
            ReleaseConfiguration in = Globals.objectMapper.readValue(filterBytes, ReleaseConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(accessToken, in.getObjectType(), Role.MANAGE);
            if (response != null) {
                return response;
            }
            // step 1 - check for vulnerabilities and validate
            String schema = StandardSchemaHandler.getSchema(in.getObjectType());
            try {
                JocXmlEditor.validate(in.getObjectType(), schema, in.getConfiguration());
            } catch (SOSXMLXSDValidatorException e) {
                return responseStatus200(Globals.objectMapper.writeValueAsBytes(ValidateResourceImpl.getError(e)));
            }

            // step 2 - update db/reread and post event
            return responseStatus200(Globals.objectMapper.writeValueAsBytes(handleStandardConfiguration(in, getAccount(), 0L)));
        } catch (Exception e) {
            return responseStatusJSError(e);
        }
    }

    /** update db/reread, post NotificationConfigurationReleased event */
    public static ReadStandardConfigurationAnswer handleStandardConfiguration(ReleaseConfiguration in, String account, Long auditLogId)
            throws Exception {
        // update db/reread
        DBItemXmlEditorConfiguration item = StandardSchemaHandler.createOrUpdateConfigurationIfReleaseOrDeploy(IMPL_PATH, in.getObjectType(), null, in
                .getConfiguration(), in.getConfigurationJson(), account, auditLogId);
        StandardSchemaHandler handler = new StandardSchemaHandler(in.getObjectType());
        handler.readCurrent(item, true);

        // post event
        EventBus.getInstance().post(new NotificationConfigurationReleased());

        return handler.getAnswer();
    }

    private void checkRequiredParameters(final ReleaseConfiguration in) throws Exception {
        if (!JocXmlEditor.isNotification(in.getObjectType())) {
            throw new JocException(new JocError(JocXmlEditor.ERROR_CODE_UNSUPPORTED_OBJECT_TYPE, String.format(
                    "[%s]unsupported object type for release", in.getObjectType().name())));
        }
    }

}
