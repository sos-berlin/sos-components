package com.sos.joc.xmleditor.impl;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.xmleditor.xml2json.Xml2JsonConfiguration;
import com.sos.joc.model.xmleditor.xml2json.Xml2JsonConfigurationAnswer;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.Xml2JsonConverter;
import com.sos.joc.xmleditor.commons.other.OtherSchemaHandler;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;
import com.sos.joc.xmleditor.resource.IXml2JsonResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocXmlEditor.APPLICATION_PATH)
public class Xml2JsonResourceImpl extends ACommonResourceImpl implements IXml2JsonResource {

    @Override
    public JOCDefaultResponse process(final String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.SETTINGS);
            JsonValidator.validateFailFast(filterBytes, Xml2JsonConfiguration.class);
            Xml2JsonConfiguration in = Globals.objectMapper.readValue(filterBytes, Xml2JsonConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(accessToken, in.getObjectType(), Role.VIEW);
            if (response != null) {
                return response;
            }
            JocXmlEditor.parseXml(in.getConfiguration());

            String schema = null;
            switch (in.getObjectType()) {
            case NOTIFICATION:
                schema = StandardSchemaHandler.getNotificationSchema();
                break;
            case YADE:
                schema = StandardSchemaHandler.getYADESchema();
                break;
            case OTHER:
                schema = OtherSchemaHandler.getSchema(in.getSchemaIdentifier(), false);
                break;
            }
            
            Xml2JsonConverter converter = new Xml2JsonConverter();
            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(getSuccess(converter.convert(in.getObjectType(),
                    schema, in.getConfiguration()))));

        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void checkRequiredParameters(final Xml2JsonConfiguration in) throws Exception {
        if (JocXmlEditor.isOther(in.getObjectType())) {
            checkRequiredParameter("schemaIdentifier", in.getSchemaIdentifier());
        }
    }

    private Xml2JsonConfigurationAnswer getSuccess(String json) {
        Xml2JsonConfigurationAnswer answer = new Xml2JsonConfigurationAnswer();
        answer.setConfigurationJson(json);
        return answer;
    }

}
