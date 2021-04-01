package com.sos.joc.xmleditor.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.nio.file.Files;

import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.Path;
import javax.ws.rs.core.StreamingOutput;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.schema.SchemaDownloadConfiguration;
import com.sos.joc.xmleditor.resource.ISchemaDownloadResource;
import com.sos.schema.JsonValidator;

@Path(JocXmlEditor.APPLICATION_PATH)
public class SchemaDownloadResourceImpl extends ACommonResourceImpl implements ISchemaDownloadResource {

    @Override
    public JOCDefaultResponse process(final String xAccessToken, String accessToken, String controllerId, String objectType, String show,
            String schemaIdentifier) {
        try {
            accessToken = getAccessToken(xAccessToken, accessToken);

            JsonObjectBuilder builder = Json.createObjectBuilder();
            if (controllerId != null) {
                builder.add("controllerId", controllerId);
            }
            if (objectType != null) {
                builder.add("objectType", objectType);
            }
            builder.add("show", show == null ? false : Boolean.parseBoolean(show));

            if (schemaIdentifier != null) {
                builder.add("schemaIdentifier", URLDecoder.decode(schemaIdentifier, JocXmlEditor.CHARSET));
            }
            String json = builder.build().toString();

            initLogging(IMPL_PATH, json.getBytes(), accessToken);
            JsonValidator.validateFailFast(json.getBytes(), SchemaDownloadConfiguration.class);
            SchemaDownloadConfiguration in = Globals.objectMapper.readValue(json.getBytes(), SchemaDownloadConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), accessToken, in.getObjectType(), Role.VIEW);
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
        checkRequiredParameter("controllerId", in.getControllerId());
        JocXmlEditor.checkRequiredParameter("objectType", in.getObjectType());
        if (!in.getObjectType().equals(ObjectType.NOTIFICATION)) {
            checkRequiredParameter("schemaIdentifier", in.getSchemaIdentifier());
        }
    }

    private JOCDefaultResponse download(SchemaDownloadConfiguration in) throws Exception {

        java.nio.file.Path path = null;
        switch (in.getObjectType()) {
        case YADE:
        case OTHER:
            path = JocXmlEditor.getSchema(in.getObjectType(), in.getSchemaIdentifier(), true);
            break;
        default:
            path = JocXmlEditor.getStandardAbsoluteSchemaLocation(in.getObjectType());
            break;

        }
        if (!Files.exists(path)) {
            throw new Exception(String.format("[%s][%s]schema file not found", in.getSchemaIdentifier(), path.toString()));
        }

        final java.nio.file.Path downPath = path;
        StreamingOutput fileStream = new StreamingOutput() {

            @Override
            public void write(OutputStream output) throws IOException {
                InputStream in = null;
                try {
                    in = Files.newInputStream(downPath);
                    byte[] buffer = new byte[4096];
                    int length;
                    while ((length = in.read(buffer)) > 0) {
                        output.write(buffer, 0, length);
                    }
                    output.flush();
                } finally {
                    try {
                        output.close();
                    } catch (Exception e) {
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (Exception e) {
                        }
                    }
                }
            }
        };
        if (in.getShow()) {
            return JOCDefaultResponse.responsePlainStatus200(fileStream);
        } else {
            return JOCDefaultResponse.responseOctetStreamDownloadStatus200(fileStream, path.getFileName().toString());
        }
    }
}
