package com.sos.joc.xmleditor.impl;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.schema.SchemaAssignConfiguration;
import com.sos.joc.model.xmleditor.schema.SchemaAssignConfigurationAnswer;
import com.sos.joc.xmleditor.common.schema.SchemaHandler;
import com.sos.joc.xmleditor.resource.ISchemaAssignResource;
import com.sos.schema.JsonValidator;

@Path(JocXmlEditor.APPLICATION_PATH)
public class SchemaAssignResourceImpl extends ACommonResourceImpl implements ISchemaAssignResource {

    @Override
    public JOCDefaultResponse process(final String accessToken, final byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, SchemaAssignConfiguration.class);
            SchemaAssignConfiguration in = Globals.objectMapper.readValue(filterBytes, SchemaAssignConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), accessToken, in.getObjectType(), Role.MANAGE);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(getSuccess(in));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void checkRequiredParameters(final SchemaAssignConfiguration in) throws Exception {
        checkRequiredParameter("controllerId", in.getControllerId());
        JocXmlEditor.checkRequiredParameter("objectType", in.getObjectType());
        if (in.getUri() == null) {
            if (in.getFileName() == null || in.getFileContent() == null) {
                throw new JocMissingRequiredParameterException("uri param is null. missing fileName or fileContent parameters.");
            }
        }
    }

    private SchemaAssignConfigurationAnswer getSuccess(final SchemaAssignConfiguration in) throws Exception {
        if (in.getObjectType().equals(ObjectType.NOTIFICATION)) {
            throw new Exception(String.format("[%s]not supported", in.getObjectType().name()));
        }

        SchemaHandler h = new SchemaHandler();
        h.process(in.getObjectType(), in.getUri(), in.getFileName(), in.getFileContent());
        if (Files.exists(h.getTargetTemp())) {
            boolean equals = h.getTargetTemp().equals(h.getTarget());
            try {
                String schema = JocXmlEditor.getFileContent(h.getTargetTemp());
                JocXmlEditor.parseXml(schema);

                if (!equals) {
                    Files.move(h.getTargetTemp(), h.getTarget(), StandardCopyOption.REPLACE_EXISTING);
                }

                SchemaAssignConfigurationAnswer answer = new SchemaAssignConfigurationAnswer();
                answer.setSchema(schema);
                answer.setSchemaIdentifier(JocXmlEditor.getHttpOrFileSchemaIdentifier(h.getSource()));
                return answer;
            } catch (Exception e) {
                h.onError(!equals);
                throw e;
            }
        } else {
            throw new Exception(String.format("[%s][targetTemp=%s][target=%s]target file not found", h.getSource(), h.getTargetTemp(), h
                    .getTarget()));
        }
    }
}
