package com.sos.joc.xmleditor.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.xml.exception.SOSXMLXSDValidatorException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.exceptions.JocError;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.read.ReadConfiguration;
import com.sos.joc.model.xmleditor.read.other.AnswerConfiguration;
import com.sos.joc.model.xmleditor.read.other.ReadOtherConfigurationAnswer;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.model.xmleditor.validate.ValidateConfigurationAnswer;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.Utils;
import com.sos.joc.xmleditor.commons.Xml2JsonConverter;
import com.sos.joc.xmleditor.commons.other.OtherSchemaHandler;
import com.sos.joc.xmleditor.commons.standard.StandardSchemaHandler;
import com.sos.joc.xmleditor.resource.IReadResource;
import com.sos.schema.JsonValidator;

import jakarta.ws.rs.Path;

@Path(JocXmlEditor.APPLICATION_PATH)
public class ReadResourceImpl extends ACommonResourceImpl implements IReadResource {

    @Override
    public JOCDefaultResponse process(final String accessToken, byte[] filterBytes) {
        try {
            filterBytes = initLogging(IMPL_PATH, filterBytes, accessToken);
            JsonValidator.validateFailFast(filterBytes, ReadConfiguration.class);
            ReadConfiguration in = Globals.objectMapper.readValue(filterBytes, ReadConfiguration.class);

            // checkRequiredParameters(in);

            JOCDefaultResponse response = initPermissions(accessToken, in.getObjectType(), Role.VIEW);
            if (response == null) {
                switch (in.getObjectType()) {
                case YADE:
                    if (isReadAllMultipleConfigurations(in)) {
                        response = JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(getMultipleConfigurations(in)));
                    } else {
                        response = JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(getSingleYADEConfiguration(in)));
                    }
                    break;
                case NOTIFICATION:
                    response = JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(getNotificationConfiguration(in)));
                    break;
                case OTHER:
                    if (isReadAllMultipleConfigurations(in)) {
                        response = JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(getMultipleConfigurations(in)));
                    } else {
                        OtherSchemaHandler.setRealPath();
                        response = JOCDefaultResponse.responseStatus200(Globals.objectMapper.writeValueAsBytes(getSingleOTHERConfiguration(in)));
                    }
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

    public static ReadStandardConfigurationAnswer getNotificationConfiguration(ReadConfiguration in) throws Exception {
        DBItemXmlEditorConfiguration item = getItem(in.getObjectType().name(), StandardSchemaHandler.getDefaultConfigurationName(in.getObjectType()));

        StandardSchemaHandler handler = new StandardSchemaHandler(ObjectType.NOTIFICATION);
        handler.readCurrent(item, (in.getForceRelease() != null && in.getForceRelease()));
        ReadStandardConfigurationAnswer answer = handler.getAnswer();
        if (answer.getConfiguration() != null) {
            answer.setValidation(getValidationAnswer(ObjectType.NOTIFICATION, StandardSchemaHandler.getNotificationSchema(), answer
                    .getConfiguration()));
        }
        return answer;
    }

    private static boolean isReadAllMultipleConfigurations(ReadConfiguration in) {
        return in.getId() == null || in.getId() <= 0;
    }

    public static ReadStandardConfigurationAnswer getSingleYADEConfiguration(ReadConfiguration in) throws Exception {
        DBItemXmlEditorConfiguration item = getItem(in.getId());
        if (item == null) {
            throw new JocException(new JocError(JocXmlEditor.CODE_NO_CONFIGURATION_EXIST, String.format("[%s][%s]no configuration found", in
                    .getObjectType().name(), in.getId())));
        }

        StandardSchemaHandler handler = new StandardSchemaHandler(ObjectType.YADE);
        handler.readCurrent(item, (in.getForceRelease() != null && in.getForceRelease()));
        ReadStandardConfigurationAnswer answer = handler.getAnswer();
        if (answer.getConfiguration() != null) {
            answer.setValidation(getValidationAnswer(ObjectType.YADE, answer.getSchema(), answer.getConfiguration()));
        }
        return answer;
    }

    public static ReadOtherConfigurationAnswer getSingleOTHERConfiguration(ReadConfiguration in) throws Exception {
        DBItemXmlEditorConfiguration item = getItem(in.getId());
        if (item == null) {
            throw new JocException(new JocError(JocXmlEditor.CODE_NO_CONFIGURATION_EXIST, String.format("[%s][%s]no configuration found", in
                    .getObjectType().name(), in.getId())));
        }

        ReadOtherConfigurationAnswer answer = new ReadOtherConfigurationAnswer();
        answer.setConfiguration(new AnswerConfiguration());
        String schema = OtherSchemaHandler.getSchema(item.getSchemaLocation(), false);
        answer.getConfiguration().setSchema(schema);
        answer.getConfiguration().setSchemaIdentifier(OtherSchemaHandler.getHttpOrFileSchemaIdentifier(item.getSchemaLocation()));
        answer.getConfiguration().setConfiguration(item.getConfigurationDraft());

        ValidateConfigurationAnswer validation = getValidationAnswer(ObjectType.OTHER, schema, answer.getConfiguration().getConfiguration());

        if (item.getConfigurationDraftJson() == null) {
            if (item.getConfigurationDraft() == null) {
                answer.getConfiguration().setRecreateJson(false);
            } else {
                answer.getConfiguration().setRecreateJson(true);
                Xml2JsonConverter converter = new Xml2JsonConverter();
                answer.getConfiguration().setConfigurationJson(converter.convert(in.getObjectType(), schema, item.getConfigurationDraft()));
            }
        } else {
            answer.getConfiguration().setRecreateJson(false);
            answer.getConfiguration().setConfigurationJson(Utils.deserializeJson(item.getConfigurationDraftJson()));
        }
        answer.getConfiguration().setValidation(validation);
        answer.getConfiguration().setModified(item.getModified());

        return answer;
    }

    private static ReadOtherConfigurationAnswer getMultipleConfigurations(ReadConfiguration in) throws Exception {
        // YADE and OTHER
        ReadOtherConfigurationAnswer answer = new ReadOtherConfigurationAnswer();

        ArrayList<String> schemas = new ArrayList<String>();
        List<Map<String, Object>> items = getConfigurationProperties(in, "id as id,name as name,schemaLocation as schemaLocation",
                "order by created");
        boolean isYADE = JocXmlEditor.isYADE(in.getObjectType());
        boolean isOther = JocXmlEditor.isOther(in.getObjectType());
        if (items != null && items.size() > 0) {
            ArrayList<AnswerConfiguration> configurations = new ArrayList<AnswerConfiguration>();
            for (int i = 0; i < items.size(); i++) {
                Map<String, Object> item = items.get(i);
                AnswerConfiguration configuration = new AnswerConfiguration();
                configuration.setId(Long.parseLong(item.get("id").toString()));
                configuration.setName(item.get("name").toString());
                if (isYADE) {
                    configuration.setSchemaIdentifier(JocXmlEditor.YADE_SCHEMA_FILENAME);
                } else {
                    configuration.setSchemaIdentifier(item.get("schemaLocation").toString());// fileName or http(s) location
                }
                if (isOther) {
                    // only http(s), files will be checked later - maybe no longer exists (were removed from the resources directory)
                    if (OtherSchemaHandler.isHttp(configuration.getSchemaIdentifier()) && !schemas.contains(configuration.getSchemaIdentifier())) {
                        schemas.add(configuration.getSchemaIdentifier());
                    }
                }

                configurations.add(configuration);
            }
            answer.setConfigurations(configurations);
        }
        if (isOther) {
            List<java.nio.file.Path> files = OtherSchemaHandler.getSchemaFiles();
            if (files != null && files.size() > 0) {
                for (int i = 0; i < files.size(); i++) {
                    // fileName
                    String schema = OtherSchemaHandler.getSchemaIdentifier(files.get(i));
                    if (!schemas.contains(schema)) {
                        schemas.add(schema);
                    }
                }
            }
        }
        if (schemas.size() > 0) {
            Collections.sort(schemas);
            answer.setSchemas(schemas);
        }
        return answer;
    }

    private static ValidateConfigurationAnswer getValidationAnswer(ObjectType type, String schema, String xml) throws Exception {
        ValidateConfigurationAnswer answer = null;
        try {
            JocXmlEditor.validate(type, schema, xml);
            answer = ValidateResourceImpl.getSuccess();
        } catch (SOSXMLXSDValidatorException e) {
            answer = ValidateResourceImpl.getError(e);
        }
        return answer;
    }

    private static DBItemXmlEditorConfiguration getItem(Long id) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);

            session.beginTransaction();
            DBItemXmlEditorConfiguration item = dbLayer.getObject(id);
            session.commit();

            return item;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    public static DBItemXmlEditorConfiguration getItem(String objectType, String name) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);

            session.beginTransaction();
            DBItemXmlEditorConfiguration item = dbLayer.getObject(objectType, name);
            session.commit();

            return item;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private static List<Map<String, Object>> getConfigurationProperties(ReadConfiguration in, String properties, String orderBy) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(IMPL_PATH);
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);

            session.beginTransaction();
            List<Map<String, Object>> items = dbLayer.getObjectProperties(in.getObjectType().name(), properties, orderBy);
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
