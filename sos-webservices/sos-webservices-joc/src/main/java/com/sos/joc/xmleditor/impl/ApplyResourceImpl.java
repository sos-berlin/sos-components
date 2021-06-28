package com.sos.joc.xmleditor.impl;

import java.util.Date;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.classes.xmleditor.exceptions.SOSXsdValidatorException;
import com.sos.joc.classes.xmleditor.validator.XsdValidator;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.DbLayerXmlEditor;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.xmleditor.apply.ApplyConfiguration;
import com.sos.joc.model.xmleditor.apply.ApplyConfigurationAnswer;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.xmleditor.common.Xml2JsonConverter;
import com.sos.joc.xmleditor.resource.IApplyResource;
import com.sos.schema.JsonValidator;

@Path(JocXmlEditor.APPLICATION_PATH)
public class ApplyResourceImpl extends ACommonResourceImpl implements IApplyResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplyResourceImpl.class);

    @Override
    public JOCDefaultResponse process(final String accessToken, final byte[] filterBytes) {
        SOSHibernateSession session = null;
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ApplyConfiguration.class);
            ApplyConfiguration in = Globals.objectMapper.readValue(filterBytes, ApplyConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), accessToken, in.getObjectType(), Role.MANAGE);
            if (response == null) {
                // step 1 - check for vulnerabilities and validate
                response = check(in, false);
                if (response != null) {
                    return response;
                }

                // step 2 - xml2json
                String json = convertXml2Json(in);

                // step 3 - store in the database TODO same code as in store
                session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
                DbLayerXmlEditor dbLayer = new DbLayerXmlEditor(session);

                DBItemXmlEditorConfiguration item = null;
                String name = null;
                switch (in.getObjectType()) {
                case YADE:
                case OTHER:
                    name = in.getName();
                    item = getObject(dbLayer, in, name);
                    break;
                default:
                    name = JocXmlEditor.getConfigurationName(in.getObjectType());
                    item = getStandardObject(dbLayer, in);
                    break;
                }
                if (item == null) {
                    item = create(session, in, name);

                } else {
                    item = update(session, in, item, name);
                }

                session.commit();
                response = JOCDefaultResponse.responseStatus200(getSuccess(in, item, json));
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

    private JOCDefaultResponse check(ApplyConfiguration in, boolean validate) throws Exception {
        if (validate) {
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
            // check for vulnerabilities and validate
            XsdValidator validator = new XsdValidator(schema);
            try {
                validator.validate(in.getConfiguration());
            } catch (SOSXsdValidatorException e) {
                LOGGER.error(String.format("[%s]%s", validator.getSchema(), e.toString()), e);
                return JOCDefaultResponse.responseStatus200(getError(e));
            }
        } else {
            // check for vulnerabilities
            JocXmlEditor.parseXml(in.getConfiguration());
        }
        return null;
    }

    private ApplyConfigurationAnswer getSuccess(ApplyConfiguration in, DBItemXmlEditorConfiguration item, String json) throws Exception {
        ApplyConfigurationAnswer answer = new ApplyConfigurationAnswer();
        answer.setId(item.getId().intValue());
        answer.setName(item.getName());
        switch (in.getObjectType()) {
        case YADE:
        case OTHER:
            answer.setSchemaIdentifier(JocXmlEditor.getHttpOrFileSchemaIdentifier(item.getSchemaLocation()));
            break;
        default:
            answer.setSchemaIdentifier(JocXmlEditor.getStandardSchemaIdentifier(in.getObjectType()));
            break;
        }
        answer.setConfiguration(item.getConfigurationDraft());
        answer.setConfigurationJson(json);
        answer.setRecreateJson(true);
        answer.setModified(item.getModified());
        if (in.getObjectType().equals(ObjectType.NOTIFICATION)) {
            answer.setReleased(false);
            if (item.getDeployed() == null) {
                answer.setHasReleases(true);
                answer.setState(ItemStateEnum.RELEASE_NOT_EXIST);
            } else {
                answer.setHasReleases(false);
                answer.setState(ItemStateEnum.DRAFT_IS_NEWER);
            }
        }
        return answer;
    }

    private String convertXml2Json(ApplyConfiguration in) throws Exception {
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
        return converter.convert(in.getObjectType(), schema, in.getConfiguration());
    }

    public static ApplyConfigurationAnswer getError(SOSXsdValidatorException e) {
        ApplyConfigurationAnswer answer = new ApplyConfigurationAnswer();
        answer.setValidationError(ValidateResourceImpl.getErrorMessage(e));
        return answer;
    }

    private void checkRequiredParameters(final ApplyConfiguration in) throws Exception {
        checkRequiredParameter("controllerId", in.getControllerId());
        JocXmlEditor.checkRequiredParameter("objectType", in.getObjectType());
        switch (in.getObjectType()) {
        case YADE:
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

    private DBItemXmlEditorConfiguration getObject(DbLayerXmlEditor dbLayer, ApplyConfiguration in, String name) throws Exception {
        DBItemXmlEditorConfiguration item = null;
        if (in.getId() != null && in.getId() > 0) {
            item = dbLayer.getObject(in.getId().longValue());
        }
        return item;
    }

    private DBItemXmlEditorConfiguration getStandardObject(DbLayerXmlEditor dbLayer, ApplyConfiguration in) throws Exception {
        return dbLayer.getObject(in.getControllerId(), in.getObjectType().name(), JocXmlEditor.getConfigurationName(in.getObjectType(), in
                .getName()));
    }

    private DBItemXmlEditorConfiguration create(SOSHibernateSession session, ApplyConfiguration in, String name) throws Exception {
        DBItemXmlEditorConfiguration item = new DBItemXmlEditorConfiguration();
        item.setControllerId(in.getControllerId());
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
