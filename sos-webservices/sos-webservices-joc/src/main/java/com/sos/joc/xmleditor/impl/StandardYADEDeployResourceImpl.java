package com.sos.joc.xmleditor.impl;

import org.w3c.dom.Document;

import com.sos.commons.util.SOSCollection;
import com.sos.commons.xml.exception.SOSXMLXSDValidatorException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.yade.YADEConfigurationDeployed;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.deploy.DeployConfiguration;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.publish.impl.ADeploy;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;
import com.sos.joc.xmleditor.commons.standard.StandardYADEJobResourceHandler;
import com.sos.joc.xmleditor.resource.IStandardYADEDeployResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocXmlEditor.APPLICATION_PATH)
public class StandardYADEDeployResourceImpl extends ADeploy implements IStandardYADEDeployResource {

    @Override
    public JOCDefaultResponse deploy(final String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, DeployConfiguration.class);
            DeployConfiguration in = Globals.objectMapper.readValue(filterBytes, DeployConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions("", getJocPermissions(accessToken).map(p -> p.getInventory().getDeploy()));
            if (response != null) {
                return response;
            }
            // step 1 - check for vulnerabilities and validate
            Document doc = null;
            try {
                doc = JocXmlEditor.validate(in.getObjectType(), StandardSchemaHandler.getYADESchema(), in.getConfiguration());
            } catch (SOSXMLXSDValidatorException e) {
                return JOCDefaultResponse.responseStatus200(ValidateResourceImpl.getError(e));
            }

            // step 2 - store and deploy JobResource
            StandardYADEJobResourceHandler.storeAndDeploy(this, accessToken, in, doc);

            // step 3 - update db and reread
            ReadStandardConfigurationAnswer answer = handleStandardConfiguration(in, getAccount(), 0L);

            // step 4 - post events
            EventBus.getInstance().post(new YADEConfigurationDeployed("", ""));

            return JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(answer));
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    public static ReadStandardConfigurationAnswer handleStandardConfiguration(DeployConfiguration in, String account, Long auditLogId)
            throws Exception {
        DBItemXmlEditorConfiguration item = StandardSchemaHandler.createOrUpdateConfigurationIfReleaseOrDeploy(IMPL_PATH, in.getObjectType(), in
                .getId(), in.getConfiguration(), in.getConfigurationJson(), account, auditLogId);
        StandardSchemaHandler handler = new StandardSchemaHandler(in.getObjectType());
        handler.readCurrent(item, true);
        return handler.getAnswer();
    }

    private void checkRequiredParameters(final DeployConfiguration in) throws Exception {
        if (!JocXmlEditor.isYADE(in.getObjectType())) {
            throw new JocException(new JocError(JocXmlEditor.ERROR_CODE_UNSUPPORTED_OBJECT_TYPE, String.format(
                    "[%s]unsupported object type for release", in.getObjectType().name())));
        }
        if (SOSCollection.isEmpty(in.getControllerIds())) {
            throw new JocException(new JocError(JocXmlEditor.ERROR_CODE_MISSING_ARGUMENT, String.format("[%s]missing controllerIds", in
                    .getObjectType().name())));
        }
        if (in.getId() == null) {
            throw new JocException(new JocError(JocXmlEditor.ERROR_CODE_MISSING_ARGUMENT, String.format("[%s]missing id", in.getObjectType()
                    .name())));
        }
    }

}
