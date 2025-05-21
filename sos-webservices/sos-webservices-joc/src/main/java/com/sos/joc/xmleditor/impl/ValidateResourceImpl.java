package com.sos.joc.xmleditor.impl;

import java.util.Date;

import com.sos.commons.xml.exception.SOSXMLXSDValidatorException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.xmleditor.validate.ErrorMessage;
import com.sos.joc.model.xmleditor.validate.ValidateConfiguration;
import com.sos.joc.model.xmleditor.validate.ValidateConfigurationAnswer;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.other.OtherSchemaHandler;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;
import com.sos.joc.xmleditor.resource.IValidateResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocXmlEditor.APPLICATION_PATH)
public class ValidateResourceImpl extends ACommonResourceImpl implements IValidateResource {

    @Override
    public JOCDefaultResponse process(final String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.SETTINGS);
            JsonValidator.validateFailFast(filterBytes, ValidateConfiguration.class);
            ValidateConfiguration in = Globals.objectMapper.readValue(filterBytes, ValidateConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(accessToken, in.getObjectType(), Role.VIEW);
            if (response == null) {
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

                // check for vulnerabilities and validate
                try {
                    JocXmlEditor.validate(in.getObjectType(), schema, in.getConfiguration());
                } catch (SOSXMLXSDValidatorException e) {
                    return JOCDefaultResponse.responseStatus200(getError(e));
                }
                response = JOCDefaultResponse.responseStatus200(getSuccess());
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void checkRequiredParameters(final ValidateConfiguration in) throws Exception {
        if (JocXmlEditor.isOther(in.getObjectType())) {
            checkRequiredParameter("schemaIdentifier", in.getSchemaIdentifier());
        }
    }

    public static ValidateConfigurationAnswer getError(SOSXMLXSDValidatorException e) {
        ValidateConfigurationAnswer answer = new ValidateConfigurationAnswer();
        answer.setValidated(null);
        answer.setValidationError(getErrorMessage(e));
        return answer;
    }

    public static ErrorMessage getErrorMessage(SOSXMLXSDValidatorException e) {
        ErrorMessage m = new ErrorMessage();
        m.setCode(JocXmlEditor.ERROR_CODE_VALIDATION_ERROR);
        try {
            m.setMessage(String.format("'%s', line=%s, column=%s, %s", e.getElementName(), e.getLineNumber(), e.getColumnNumber(), e.getCause()
                    .getMessage()));
        } catch (Throwable ex) {
            m.setMessage(ex.toString());
        }
        m.setLine(e.getLineNumber());
        m.setColumn(e.getColumnNumber());
        m.setElementName(e.getElementName());
        m.setElementPosition(e.getElementPosition());
        m.setFatal(e.getFatal());
        return m;
    }

    public static ValidateConfigurationAnswer getSuccess() {
        ValidateConfigurationAnswer answer = new ValidateConfigurationAnswer();
        answer.setValidated(new Date());
        return answer;
    }
}
