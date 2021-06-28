package com.sos.joc.xmleditor.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.exception.SOSXMLXSDValidatorException;
import com.sos.commons.xml.validator.SOSXMLXSDValidator;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.xmleditor.JocXmlEditor;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.DbLayerXmlEditor;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.read.ReadConfiguration;
import com.sos.joc.model.xmleditor.read.other.AnswerConfiguration;
import com.sos.joc.model.xmleditor.read.other.ReadOtherConfigurationAnswer;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.model.xmleditor.validate.ValidateConfigurationAnswer;
import com.sos.joc.xmleditor.common.Utils;
import com.sos.joc.xmleditor.common.Xml2JsonConverter;
import com.sos.joc.xmleditor.common.standard.ReadConfigurationHandler;
import com.sos.joc.xmleditor.resource.IReadResource;
import com.sos.schema.JsonValidator;

@Path(JocXmlEditor.APPLICATION_PATH)
public class ReadResourceImpl extends ACommonResourceImpl implements IReadResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReadResourceImpl.class);
    private static final boolean isTraceEnabled = LOGGER.isTraceEnabled();

    @Override
    public JOCDefaultResponse process(final String accessToken, final byte[] filterBytes) {
        try {
            initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ReadConfiguration.class);
            ReadConfiguration in = Globals.objectMapper.readValue(filterBytes, ReadConfiguration.class);

            checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(in.getControllerId(), accessToken, in.getObjectType(), Role.VIEW);
            if (response == null) {
                JocXmlEditor.setRealPath();
                switch (in.getObjectType()) {
                case YADE:
                case OTHER:
                    response = JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(handleConfigurations(in)));
                    break;
                default:
                    response = JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(handleStandardConfiguration(in)));
                    break;
                }
            }
            return response;
        } catch (JocException e) {
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }
    }

    private void checkRequiredParameters(final ReadConfiguration in) throws Exception {
        checkRequiredParameter("controllerId", in.getControllerId());
        JocXmlEditor.checkRequiredParameter("objectType", in.getObjectType());
    }

    private ReadStandardConfigurationAnswer handleStandardConfiguration(ReadConfiguration in) throws Exception {
        DBItemXmlEditorConfiguration item = getItem(in.getControllerId(), in.getObjectType().name(), JocXmlEditor.getConfigurationName(in
                .getObjectType()));

        ReadConfigurationHandler handler = new ReadConfigurationHandler(in.getObjectType());
        handler.readCurrent(item, in.getControllerId(), (in.getForceRelease() != null && in.getForceRelease()));
        ReadStandardConfigurationAnswer answer = handler.getAnswer();
        if (answer.getConfiguration() != null) {
            answer.setValidation(getValidation(JocXmlEditor.getStandardAbsoluteSchemaLocation(in.getObjectType()), answer.getConfiguration()));
        }
        return answer;
    }

    private ReadOtherConfigurationAnswer handleConfigurations(ReadConfiguration in) throws Exception {

        ReadOtherConfigurationAnswer answer = new ReadOtherConfigurationAnswer();

        if (in.getId() == null || in.getId() <= 0) {
            ArrayList<String> schemas = new ArrayList<String>();
            List<Map<String, Object>> items = getConfigurationProperties(in, "id,name,schemaLocation", "order by created");
            if (items != null && items.size() > 0) {
                ArrayList<AnswerConfiguration> configurations = new ArrayList<AnswerConfiguration>();
                for (int i = 0; i < items.size(); i++) {
                    Map<String, Object> item = items.get(i);
                    AnswerConfiguration configuration = new AnswerConfiguration();
                    configuration.setId(Integer.parseInt(item.get("0").toString()));
                    configuration.setName(item.get("1").toString());
                    configuration.setSchemaIdentifier(item.get("2").toString());// fileName or http(s) location

                    // only http(s), files will be checked later - maybe no longer exists (were removed from the resources directory)
                    if (JocXmlEditor.isHttp(configuration.getSchemaIdentifier()) && !schemas.contains(configuration.getSchemaIdentifier())) {
                        schemas.add(configuration.getSchemaIdentifier());
                    }

                    configurations.add(configuration);
                }
                answer.setConfigurations(configurations);
            }
            List<java.nio.file.Path> files = JocXmlEditor.getSchemaFiles(in.getObjectType());
            if (files != null && files.size() > 0) {
                for (int i = 0; i < files.size(); i++) {
                    // fileName
                    String schema = JocXmlEditor.getSchemaIdentifier(files.get(i));
                    if (!schemas.contains(schema)) {
                        schemas.add(schema);
                    }
                }
            }
            if (schemas.size() > 0) {
                Collections.sort(schemas);
                answer.setSchemas(schemas);
            }

        } else {
            DBItemXmlEditorConfiguration item = getItem(in.getId());
            answer.setConfiguration(new AnswerConfiguration());
            if (item == null) {
                throw new JocException(new JocError(JocXmlEditor.CODE_NO_CONFIGURATION_EXIST, String.format("[%s][%s][%s]no configuration found", in
                        .getControllerId(), in.getObjectType().name(), in.getId())));
            }
            answer.getConfiguration().setId(item.getId().intValue());
            answer.getConfiguration().setName(item.getName());
            answer.getConfiguration().setSchema(JocXmlEditor.readSchema(in.getObjectType(), item.getSchemaLocation()));
            answer.getConfiguration().setSchemaIdentifier(JocXmlEditor.getHttpOrFileSchemaIdentifier(item.getSchemaLocation()));
            answer.getConfiguration().setConfiguration(item.getConfigurationDraft());
            if (item.getConfigurationDraftJson() == null) {
                if (item.getConfigurationDraft() == null) {
                    answer.getConfiguration().setRecreateJson(false);
                } else {
                    answer.getConfiguration().setRecreateJson(true);
                    Xml2JsonConverter converter = new Xml2JsonConverter();
                    answer.getConfiguration().setConfigurationJson(converter.convert(in.getObjectType(), JocXmlEditor.getSchema(in.getObjectType(),
                            item.getSchemaLocation(), false), item.getConfigurationDraft()));
                }
            } else {
                answer.getConfiguration().setRecreateJson(false);
                answer.getConfiguration().setConfigurationJson(Utils.deserializeJson(item.getConfigurationDraftJson()));
            }
            answer.getConfiguration().setValidation(getValidation(JocXmlEditor.getSchema(in.getObjectType(), answer.getConfiguration()
                    .getSchemaIdentifier(), false), answer.getConfiguration().getConfiguration()));

            answer.getConfiguration().setModified(item.getModified());

        }
        return answer;
    }

    private ValidateConfigurationAnswer getValidation(java.nio.file.Path schema, String content) throws Exception {
        ValidateConfigurationAnswer validation = null;
        try {
            SOSXMLXSDValidator.validate(schema,content);
            validation = ValidateResourceImpl.getSuccess();
        } catch (SOSXMLXSDValidatorException e) {
            validation = ValidateResourceImpl.getError(e);
        }
        return validation;
    }

    private DBItemXmlEditorConfiguration getItem(Integer id) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DbLayerXmlEditor dbLayer = new DbLayerXmlEditor(session);

            session.beginTransaction();
            DBItemXmlEditorConfiguration item = dbLayer.getObject(id.longValue());
            session.commit();

            if (isTraceEnabled) {
                LOGGER.trace(String.format("[%s]%s", id, SOSString.toString(item, Arrays.asList("configurationDraft", "configurationDraftJson",
                        "configurationDeployed", "configurationDeployedJson"))));
            }
            return item;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private DBItemXmlEditorConfiguration getItem(String controllerId, String objectType, String name) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DbLayerXmlEditor dbLayer = new DbLayerXmlEditor(session);

            session.beginTransaction();
            DBItemXmlEditorConfiguration item = dbLayer.getObject(controllerId, objectType, name);
            session.commit();
            if (isTraceEnabled) {
                LOGGER.trace(String.format("[%s][%s][%s]%s", controllerId, objectType, name, SOSString.toString(item, Arrays.asList(
                        "configuration"))));
            }
            return item;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private List<Map<String, Object>> getConfigurationProperties(ReadConfiguration in, String properties, String orderBy) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            DbLayerXmlEditor dbLayer = new DbLayerXmlEditor(session);

            session.beginTransaction();
            List<Map<String, Object>> items = dbLayer.getObjectProperties(in.getControllerId(), in.getObjectType().name(), properties, orderBy);
            session.commit();
            return items;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }
}
