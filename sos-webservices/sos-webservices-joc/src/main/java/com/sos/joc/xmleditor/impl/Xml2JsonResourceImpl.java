package com.sos.joc.xmleditor.impl;

import javax.ws.rs.Path;

import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.security.permissions.ControllerPermissions;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.xml2json.Xml2JsonConfiguration;
import com.sos.joc.model.xmleditor.xml2json.Xml2JsonConfigurationAnswer;
import com.sos.joc.xmleditor.common.Xml2JsonConverter;
import com.sos.joc.xmleditor.resource.IXml2JsonResource;
import com.sos.schema.JsonValidator;

@Path(JocXmlEditor.APPLICATION_PATH)
public class Xml2JsonResourceImpl extends JOCResourceImpl implements IXml2JsonResource {

    @Override
    public JOCDefaultResponse process(final String accessToken, final byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, Xml2JsonConfiguration.class);
            Xml2JsonConfiguration in = Globals.objectMapper.readValue(filterBytes, Xml2JsonConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = checkPermissions(accessToken, in);
            if (response == null) {
                JocXmlEditor.parseXml(in.getConfiguration());

                java.nio.file.Path schema = null;
                switch (in.getObjectType()) {
                case YADE:
                case OTHER:
                    schema = JocXmlEditor.getSchema(in.getObjectType(), in.getSchemaIdentifier(), false);
                    break;
                default:
                    schema = JocXmlEditor.getStandardAbsoluteSchemaLocation(in.getObjectType());
                    break;
                }
                Xml2JsonConverter converter = new Xml2JsonConverter();
                response = JOCDefaultResponse.responseStatus200(getSuccess(converter.convert(in.getObjectType(), schema, in.getConfiguration())));
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void checkRequiredParameters(final Xml2JsonConfiguration in) throws Exception {
        checkRequiredParameter("controllerId", in.getControllerId());
        JocXmlEditor.checkRequiredParameter("objectType", in.getObjectType());
        checkRequiredParameter("configuration", in.getConfiguration());
        if (!in.getObjectType().equals(ObjectType.NOTIFICATION)) {
            checkRequiredParameter("schemaIdentifier", in.getSchemaIdentifier());
        }
    }

    private JOCDefaultResponse checkPermissions(final String accessToken, final Xml2JsonConfiguration in) throws Exception {
        // TODO permissions ggf. getJocPermissions
        ControllerPermissions permissions = getControllerPermissions(in.getControllerId(), accessToken);
        boolean permission = true; //permissions.getJS7Controller().getAdministration().isEditPermissions();
        return initPermissions(in.getControllerId(), permission);
    }

    private Xml2JsonConfigurationAnswer getSuccess(String json) {
        Xml2JsonConfigurationAnswer answer = new Xml2JsonConfigurationAnswer();
        answer.setConfigurationJson(json);
        return answer;
    }

}
