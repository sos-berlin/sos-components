package com.sos.joc.xmleditor.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;

import javax.json.Json;
import javax.json.JsonObjectBuilder;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.xmleditor.schema.SchemaDownloadConfiguration;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.other.OtherSchemaHandler;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;
import com.sos.joc.xmleditor.resource.ISchemaDownloadResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.StreamingOutput;

@Path(JocXmlEditor.APPLICATION_PATH)
public class SchemaDownloadResourceImpl extends ACommonResourceImpl implements ISchemaDownloadResource {

    @Override
    public JOCDefaultResponse process(final String xAccessToken, String accessToken, String objectType, String show, String schemaIdentifier) {
        try {
            accessToken = getAccessToken(xAccessToken, accessToken);

            JsonObjectBuilder builder = Json.createObjectBuilder();
            if (objectType != null) {
                builder.add("objectType", objectType);
            }
            builder.add("show", show == null ? false : Boolean.parseBoolean(show));

            if (schemaIdentifier != null) {
                builder.add("schemaIdentifier", URLDecoder.decode(schemaIdentifier, JocXmlEditor.CHARSET));
            }
            String json = builder.build().toString();
            initLogging(IMPL_PATH, json.getBytes(), accessToken, CategoryType.SETTINGS);
            JsonValidator.validateFailFast(json.getBytes(), SchemaDownloadConfiguration.class);
            SchemaDownloadConfiguration in = Globals.objectMapper.readValue(json.getBytes(), SchemaDownloadConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(accessToken, in.getObjectType(), Role.VIEW);
            if (response == null) {
                return download(in);
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void checkRequiredParameters(final SchemaDownloadConfiguration in) throws Exception {
        if (JocXmlEditor.isOther(in.getObjectType())) {
            checkRequiredParameter("schemaIdentifier", in.getSchemaIdentifier());
        }
    }

    private JOCDefaultResponse download(SchemaDownloadConfiguration in) throws Exception {

        final InputStream inputStream;
        String downloadFileName = null;
        switch (in.getObjectType()) {
        case YADE:
            inputStream = StandardSchemaHandler.getYADESchemaAsInputStream();
            downloadFileName = JocXmlEditor.YADE_SCHEMA_FILENAME;
            break;
        case NOTIFICATION:
            inputStream = StandardSchemaHandler.getNotificationSchemaAsInputStream();
            downloadFileName = JocXmlEditor.NOTIFICATION_SCHEMA_FILENAME;
            break;
        case OTHER:
            inputStream = OtherSchemaHandler.getSchemaAsInputStream(in.getSchemaIdentifier(), true);
            downloadFileName = in.getSchemaIdentifier();
            break;
        default:
            inputStream = null;
        }
        if (inputStream == null) {
            throw new Exception(String.format("[%s]inputStream missing", in.getSchemaIdentifier()));
        }
        if (downloadFileName == null) {
            downloadFileName = in.getObjectType() + "_schema.xsd";
        }

        StreamingOutput fileStream = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException {
                try {
                    // in = Files.newInputStream(downPath);
                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = inputStream.read(buffer)) > 0) {
                        output.write(buffer, 0, length);
                    }
                    output.flush();
                } finally {
                    try {
                        output.close();
                    } catch (Exception e) {
                    }
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        };
        if (in.getShow()) {
            return JOCDefaultResponse.responsePlainStatus200(fileStream);
        } else {
            return JOCDefaultResponse.responseOctetStreamDownloadStatus200(fileStream, downloadFileName);
        }
    }
}
