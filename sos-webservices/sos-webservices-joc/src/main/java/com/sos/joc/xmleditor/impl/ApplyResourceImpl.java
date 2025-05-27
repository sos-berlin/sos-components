package com.sos.joc.xmleditor.impl;

import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.exception.SOSXMLXSDValidatorException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.audit.CategoryType;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.xmleditor.apply.ApplyConfiguration;
import com.sos.joc.model.xmleditor.apply.ApplyConfigurationAnswer;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.Xml2JsonConverter;
import com.sos.joc.xmleditor.commons.other.OtherSchemaHandler;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;
import com.sos.joc.xmleditor.resource.IApplyResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocXmlEditor.APPLICATION_PATH)
public class ApplyResourceImpl extends ACommonResourceImpl implements IApplyResource {

    @Override
    public JOCDefaultResponse process(final String accessToken, byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken, CategoryType.SETTINGS);
            JsonValidator.validateFailFast(filterBytes, ApplyConfiguration.class);
            ApplyConfiguration in = Globals.objectMapper.readValue(filterBytes, ApplyConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(accessToken, in.getObjectType(), Role.MANAGE);
            Exception transformException = null;
            if (response == null) {
                String schema = null;
                switch (in.getObjectType()) {
                case YADE:
                    schema = StandardSchemaHandler.getYADESchema();
                    try {
                        // If XML is invalid (missing closing tag, etc.) -
                        // - getXml() - The XSLT transformation cannot be performed.
                        // - The XSLT transformation exception does not provide details about the validation line, etc. - Therefore, save this exception for
                        // later use if validation was successful.
                        in.setConfiguration(StandardSchemaHandler.getXml(in.getConfiguration(), true));
                    } catch (Exception e) {
                        transformException = e;
                    }
                    break;
                case NOTIFICATION:
                    schema = StandardSchemaHandler.getNotificationSchema();
                    break;
                case OTHER:
                    schema = OtherSchemaHandler.getSchema(in.getSchemaIdentifier(), false);
                    break;
                }
                // step 1 - check for vulnerabilities and validate
                try {
                    JocXmlEditor.validate(in.getObjectType(), schema, in.getConfiguration());
                } catch (SOSXMLXSDValidatorException e) {
                    return JOCDefaultResponse.responseStatus200(getError(e));
                }

                // step 1.1 - If validation is successful, a transformException is thrown – can this happen(validation is successful)?
                if (transformException != null) {
                    throw transformException;
                }

                // step 2 - xml2json
                String json = convertXml2Json(in, schema);

                // step 3 - store in the database TODO same code as in store
                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);

                DBItemXmlEditorConfiguration item = null;
                String name = null;
                switch (in.getObjectType()) {
                case YADE:
                case OTHER:
                    name = in.getName();
                    if (in.getId() != null && in.getId() > 0) {
                        item = dbLayer.getObject(in.getId());
                    }
                    break;
                case NOTIFICATION:
                    name = StandardSchemaHandler.getDefaultConfigurationName(in.getObjectType());
                    item = dbLayer.getObject(in.getObjectType().name(), name);
                    break;
                }
                boolean isChanged = true;
                if (item == null) {
                    item = create(session, in, name);

                } else {
                    String currentConfiguration = SOSString.isEmpty(in.getConfiguration()) ? null : in.getConfiguration();
                    isChanged = JocXmlEditor.isChanged(item, currentConfiguration);
                    if (isChanged) {
                        item = update(session, in, item, name);
                    }
                }
                session.commit();

                response = JOCDefaultResponse.responseStatus200(getSuccess(in, item, json, isChanged));
            }
            return response;
        } catch (JocException e) {
            Globals.rollback(session);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            Globals.rollback(session);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        } finally {
            Globals.disconnect(session);
        }
    }

    private ApplyConfigurationAnswer getSuccess(ApplyConfiguration in, DBItemXmlEditorConfiguration item, String json, boolean isChanged)
            throws Exception {
        ApplyConfigurationAnswer answer = new ApplyConfigurationAnswer();
        answer.setId(item.getId());
        answer.setName(item.getName());
        answer.setModified(item.getModified());

        if (JocXmlEditor.isStandardType(in.getObjectType())) {
            answer.setSchemaIdentifier(StandardSchemaHandler.getSchemaIdentifier(in.getObjectType()));
            if (isChanged) {
                answer.setReleased(false);
                if (item.getReleased() == null) {
                    answer.setHasReleases(false);
                    answer.setState(ItemStateEnum.RELEASE_NOT_EXIST);
                } else {
                    answer.setHasReleases(true);
                    answer.setState(ItemStateEnum.DRAFT_IS_NEWER);
                }
            } else {
                StandardSchemaHandler handler = new StandardSchemaHandler(in.getObjectType());
                handler.readCurrent(item, false);
                ReadStandardConfigurationAnswer readAnswer = handler.getAnswer();

                answer.setReleased(readAnswer.getReleased());
                answer.setHasReleases(readAnswer.getHasReleases());
                answer.setState(readAnswer.getState());

                answer.setModified(readAnswer.getConfigurationDate());

                answer.setConfiguration(readAnswer.getConfiguration());
                answer.setConfigurationJson(readAnswer.getConfigurationJson());
                answer.setRecreateJson(false);
            }
        } else {
            answer.setSchemaIdentifier(OtherSchemaHandler.getHttpOrFileSchemaIdentifier(item.getSchemaLocation()));
        }

        if (isChanged) {
            answer.setConfiguration(item.getConfigurationDraft());
            answer.setConfigurationJson(json);
            answer.setRecreateJson(true);
        }

        return answer;
    }

    private String convertXml2Json(ApplyConfiguration in, String schema) throws Exception {
        return new Xml2JsonConverter().convert(in.getObjectType(), schema, in.getConfiguration());
    }

    public static ApplyConfigurationAnswer getError(SOSXMLXSDValidatorException e) {
        ApplyConfigurationAnswer answer = new ApplyConfigurationAnswer();
        answer.setValidationError(ValidateResourceImpl.getErrorMessage(e));
        return answer;
    }

    private void checkRequiredParameters(final ApplyConfiguration in) throws Exception {
        switch (in.getObjectType()) {
        case YADE:
            checkRequiredParameter("id", in.getId());
            checkRequiredParameter("name", in.getName());
            break;
        case OTHER:
            checkRequiredParameter("id", in.getId());
            checkRequiredParameter("name", in.getName());
            checkRequiredParameter("schemaIdentifier", in.getSchemaIdentifier());
            break;
        default:
            checkRequiredParameter("configuration", in.getConfiguration());
            break;
        }
    }

    private DBItemXmlEditorConfiguration create(SOSHibernateSession session, ApplyConfiguration in, String name) throws Exception {
        DBItemXmlEditorConfiguration item = new DBItemXmlEditorConfiguration();
        item.setType(in.getObjectType().name());
        item.setName(name.trim());
        item.setConfigurationDraft(in.getConfiguration());
        item.setConfigurationDraftJson(null);
        item.setSchemaLocation(JocXmlEditor.getSchemaLocation4Db(in.getObjectType(), in.getSchemaIdentifier()));
        item.setAuditLogId(Long.valueOf(0));// TODO
        item.setAccount(getAccount());
        item.setCreated(new Date());
        item.setModified(item.getCreated());
        session.save(item);
        return item;
    }

    private DBItemXmlEditorConfiguration update(SOSHibernateSession session, ApplyConfiguration in, DBItemXmlEditorConfiguration item, String name)
            throws Exception {
        item.setName(name.trim());
        item.setConfigurationDraft(SOSString.isEmpty(in.getConfiguration()) ? null : in.getConfiguration());
        item.setConfigurationDraftJson(null);
        item.setSchemaLocation(JocXmlEditor.getSchemaLocation4Db(in.getObjectType(), in.getSchemaIdentifier()));
        // item.setAuditLogId(new Long(0));// TODO
        item.setAccount(getAccount());
        item.setModified(new Date());
        session.update(item);
        return item;
    }

}
