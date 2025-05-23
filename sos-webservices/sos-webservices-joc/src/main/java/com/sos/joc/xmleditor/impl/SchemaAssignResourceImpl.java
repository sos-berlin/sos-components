package com.sos.joc.xmleditor.impl;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.sos.commons.util.SOSPath;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.xmleditor.schema.SchemaAssignConfiguration;
import com.sos.joc.model.xmleditor.schema.SchemaAssignConfigurationAnswer;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.other.OtherSchemaHandler;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;
import com.sos.joc.xmleditor.resource.ISchemaAssignResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocXmlEditor.APPLICATION_PATH)
public class SchemaAssignResourceImpl extends ACommonResourceImpl implements ISchemaAssignResource {

    @Override
    public JOCDefaultResponse process(final String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.SETTINGS);
            JsonValidator.validateFailFast(filterBytes, SchemaAssignConfiguration.class);
            SchemaAssignConfiguration in = Globals.objectMapper.readValue(filterBytes, SchemaAssignConfiguration.class);

            boolean isYADE = JocXmlEditor.isYADE(in.getObjectType());
            checkRequiredParameters(in, isYADE);

            JOCDefaultResponse response = initPermissions(accessToken, in.getObjectType(), Role.MANAGE);
            if (response == null) {
                response = JOCDefaultResponse.responseStatus200(getSuccess(in, isYADE));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void checkRequiredParameters(final SchemaAssignConfiguration in, boolean isYADE) throws Exception {
        if (!isYADE && !JocXmlEditor.isOther(in.getObjectType())) {
            throw new Exception("Unsupported type=" + in.getObjectType());
        }
        if (!isYADE && in.getUri() == null) {
            if (in.getFileName() == null || in.getFileContent() == null) {
                throw new JocMissingRequiredParameterException("uri param is null. missing fileName or fileContent parameters.");
            }
        }
    }

    private SchemaAssignConfigurationAnswer getSuccess(final SchemaAssignConfiguration in, boolean isYADE) throws Exception {
        if (isYADE) {
            SchemaAssignConfigurationAnswer answer = new SchemaAssignConfigurationAnswer();
            answer.setSchema(StandardSchemaHandler.getYADESchema());
            answer.setSchemaIdentifier(StandardSchemaHandler.getYADESchemaIdentifier());
            return answer;
        }
        OtherSchemaHandler h = new OtherSchemaHandler();
        h.assign(in.getObjectType(), in.getUri(), in.getFileName(), in.getFileContent());
        if (Files.exists(h.getTargetTemp())) {
            boolean equals = h.getTargetTemp().equals(h.getTarget());
            try {
                String schema = SOSPath.readFile(h.getTargetTemp());
                JocXmlEditor.parseXml(schema);

                if (!equals) {
                    Files.move(h.getTargetTemp(), h.getTarget(), StandardCopyOption.REPLACE_EXISTING);
                }

                SchemaAssignConfigurationAnswer answer = new SchemaAssignConfigurationAnswer();
                answer.setSchema(schema);
                answer.setSchemaIdentifier(OtherSchemaHandler.getHttpOrFileSchemaIdentifier(h.getSource()));
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
