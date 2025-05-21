package com.sos.joc.xmleditor.impl;

import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.sos.commons.util.SOSPath;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.xmleditor.schema.SchemaReassignConfiguration;
import com.sos.joc.model.xmleditor.schema.SchemaReassignConfigurationAnswer;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.Xml2JsonConverter;
import com.sos.joc.xmleditor.commons.other.OtherSchemaHandler;
import com.sos.joc.xmleditor.resource.IOtherSchemaReassignResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocXmlEditor.APPLICATION_PATH)
public class OtherSchemaReassignResourceImpl extends ACommonResourceImpl implements IOtherSchemaReassignResource {

    @Override
    public JOCDefaultResponse process(final String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.SETTINGS);
            JsonValidator.validateFailFast(filterBytes, SchemaReassignConfiguration.class);
            SchemaReassignConfiguration in = Globals.objectMapper.readValue(filterBytes, SchemaReassignConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(accessToken, in.getObjectType(), Role.MANAGE);
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

    private void checkRequiredParameters(final SchemaReassignConfiguration in) throws Exception {
        if (in.getUri() == null) {
            if (in.getFileName() == null || in.getFileContent() == null) {
                throw new JocMissingRequiredParameterException("uri param is null. missing fileName or fileContent parameters.");
            }
        }
    }

    private SchemaReassignConfigurationAnswer getSuccess(final SchemaReassignConfiguration in) throws Exception {
        if (JocXmlEditor.isStandardType(in.getObjectType())) {
            throw new Exception(String.format("[%s]not supported", in.getObjectType().name()));
        }

        OtherSchemaHandler h = new OtherSchemaHandler();
        h.assign(in.getObjectType(), in.getUri(), in.getFileName(), in.getFileContent());
        if (Files.exists(h.getTargetTemp())) {
            boolean equals = h.getTargetTemp().equals(h.getTarget());
            try {
                JocXmlEditor.parseXml(in.getConfiguration());
                String schema = SOSPath.readFile(h.getTargetTemp());
                JocXmlEditor.parseXml(schema);

                if (!equals) {
                    Files.move(h.getTargetTemp(), h.getTarget(), StandardCopyOption.REPLACE_EXISTING);
                }

                Xml2JsonConverter converter = new Xml2JsonConverter();
                String configurationJson = converter.convert(in.getObjectType(), schema, in.getConfiguration());

                SchemaReassignConfigurationAnswer answer = new SchemaReassignConfigurationAnswer();
                answer.setSchema(schema);
                answer.setSchemaIdentifier(OtherSchemaHandler.getHttpOrFileSchemaIdentifier(h.getSource()));
                answer.setConfigurationJson(configurationJson);
                answer.setRecreateJson(true);
                return answer;
            } catch (Exception e) {
                h.onError(!equals);
                throw e;
            }
        } else {
            throw new Exception(String.format("[%s][target=%s]target file not found", h.getSource(), h.getTarget()));
        }
    }
}
