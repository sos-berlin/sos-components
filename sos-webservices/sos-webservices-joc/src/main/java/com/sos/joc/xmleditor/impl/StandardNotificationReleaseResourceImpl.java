package com.sos.joc.xmleditor.impl;

import com.sos.commons.xml.exception.SOSXMLXSDValidatorException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.monitoring.NotificationConfigurationReleased;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.model.xmleditor.release.ReleaseConfiguration;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;
import com.sos.joc.xmleditor.resource.IStandardNotificationReleaseResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocXmlEditor.APPLICATION_PATH)
public class StandardNotificationReleaseResourceImpl extends ACommonResourceImpl implements IStandardNotificationReleaseResource {

    @Override
    public JOCDefaultResponse release(final String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken);
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
                return JOCDefaultResponse.responseStatus200(ValidateResourceImpl.getError(e));
            }

            // step 2 - update db and reread
            ReadStandardConfigurationAnswer answer = handleStandardConfiguration(in, getAccount(), 0L);

            // step 3 - post events
            EventBus.getInstance().post(new NotificationConfigurationReleased());

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    public static ReadStandardConfigurationAnswer handleStandardConfiguration(ReleaseConfiguration in, String account, Long auditLogId)
            throws Exception {
        DBItemXmlEditorConfiguration item = StandardSchemaHandler.createOrUpdateConfigurationIfReleaseOrDeploy(IMPL_PATH, in.getObjectType(), null, in
                .getConfiguration(), in.getConfigurationJson(), account, auditLogId);
        StandardSchemaHandler handler = new StandardSchemaHandler(in.getObjectType());
        handler.readCurrent(item, true);
        return handler.getAnswer();
    }

    private void checkRequiredParameters(final ReleaseConfiguration in) throws Exception {
        if (!JocXmlEditor.isNotification(in.getObjectType())) {
            throw new JocException(new JocError(JocXmlEditor.ERROR_CODE_UNSUPPORTED_OBJECT_TYPE, String.format(
                    "[%s]unsupported object type for release", in.getObjectType().name())));
        }
    }

}
