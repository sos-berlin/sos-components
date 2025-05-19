package com.sos.joc.xmleditor.commons.standard;

import java.io.InputStream;
import java.util.Date;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSString;
import com.sos.commons.xml.exception.SOSXMLNotMatchSchemaException;
import com.sos.joc.Globals;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.db.xmleditor.XmlEditorDbLayer;
import com.sos.joc.model.inventory.common.ItemStateEnum;
import com.sos.joc.model.xmleditor.common.ObjectType;
import com.sos.joc.model.xmleditor.read.standard.ReadStandardConfigurationAnswer;
import com.sos.joc.xmleditor.commons.JocXmlEditor;
import com.sos.joc.xmleditor.commons.Utils;
import com.sos.joc.xmleditor.commons.Xml2JsonConverter;

public class StandardSchemaHandler {

    private ObjectType type;
    private ReadStandardConfigurationAnswer answer;

    public static String getSchema(ObjectType type) throws Exception {
        if (JocXmlEditor.isYADE(type)) {
            return getYADESchema();
        }
        if (JocXmlEditor.isNotification(type)) {
            return getNotificationSchema();
        }
        return null;
    }

    public static String getYADESchema() throws Exception {
        return SOSClassUtil.readResourceFile(new JocXmlEditor().getClass(), JocXmlEditor.YADE_SCHEMA_RESOURCE_PATH);
    }

    public static InputStream getYADESchemaAsInputStream() throws Exception {
        return SOSClassUtil.openResourceStream(new JocXmlEditor().getClass(), JocXmlEditor.YADE_SCHEMA_RESOURCE_PATH);
    }

    public static String getNotificationSchema() throws Exception {
        return SOSClassUtil.readResourceFile(new JocXmlEditor().getClass(), JocXmlEditor.NOTIFICATION_SCHEMA_RESOURCE_PATH);
    }

    public static InputStream getNotificationSchemaAsInputStream() throws Exception {
        return SOSClassUtil.openResourceStream(new JocXmlEditor().getClass(), JocXmlEditor.NOTIFICATION_SCHEMA_RESOURCE_PATH);
    }

    public static String getDefaultConfigurationName(ObjectType type) {
        if (JocXmlEditor.isYADE(type)) {
            return JocXmlEditor.CONFIGURATION_BASENAME_YADE + ".xml";
        } else if (JocXmlEditor.isNotification(type)) {
            return JocXmlEditor.CONFIGURATION_BASENAME_NOTIFICATION + ".xml";
        }
        return null;
    }

    public static String getYADESchemaIdentifier() throws Exception {
        return JocXmlEditor.YADE_SCHEMA_FILENAME;
    }

    public static String getSchemaIdentifier(ObjectType type) throws Exception {
        if (JocXmlEditor.isYADE(type)) {
            return getYADESchemaIdentifier();
        } else if (JocXmlEditor.isNotification(type)) {
            return getNotificationSchemaIdentifier();
        }
        return null;
    }

    public StandardSchemaHandler(ObjectType objectType) {
        type = objectType;
        answer = new ReadStandardConfigurationAnswer();
    }

    public void readCurrent(DBItemXmlEditorConfiguration item, boolean forceRelease) throws Exception {
        if (item == null || (item.getConfigurationDraft() == null && item.getConfigurationReleased() == null)) {
            return;
        }

        boolean isYADE = JocXmlEditor.isYADE(type);
        if (isYADE) {
            answer.setSchema(getYADESchema());
            answer.setSchemaIdentifier(getYADESchemaIdentifier());
        } else if (JocXmlEditor.isNotification(type)) {
            answer.setSchema(getNotificationSchema());
            answer.setSchemaIdentifier(getNotificationSchemaIdentifier());
        } else {
            throw new Exception("Unsupported type=" + type);
        }

        answer.setId(item.getId());
        answer.setName(item.getName());
        answer.setReleased(false);
        answer.setHasReleases(false);
        answer.setState(ItemStateEnum.NO_CONFIGURATION_EXIST);
        answer.setConfiguration(null);
        answer.setConfigurationJson(null);
        answer.setRecreateJson(false);

        if (item.getConfigurationReleased() != null) {
            answer.setHasReleases(true);

            if (forceRelease || item.getConfigurationDraft() == null) {
                String xml = getXML(item.getConfigurationReleased(), isYADE);
                String json = item.getConfigurationReleasedJson();

                answer.setConfigurationDate(item.getReleased());
                answer.setConfiguration(xml);
                if (SOSString.isEmpty(json)) {
                    recreateJson(xml);
                } else {
                    deserializeJson(json, xml);
                }

                if (item.getConfigurationDraft() == null) {
                    answer.setReleased(true);
                    answer.setState(ItemStateEnum.DRAFT_NOT_EXIST);
                } else {
                    if (item.getReleased() != null && item.getReleased().after(item.getModified())) {
                        answer.setState(ItemStateEnum.RELEASE_IS_NEWER);
                    } else {
                        answer.setState(ItemStateEnum.DRAFT_IS_NEWER);
                    }
                }
                return;
            }
        }

        if (item.getConfigurationDraft() != null) {
            String xml = getXML(item.getConfigurationDraft(), isYADE);
            String json = item.getConfigurationDraftJson();

            answer.setConfigurationDate(item.getModified());
            answer.setConfiguration(xml);
            if (SOSString.isEmpty(json)) {
                recreateJson(xml);
            } else {
                deserializeJson(json, xml);
            }

            if (answer.getHasReleases()) {
                if (item.getReleased() != null && item.getReleased().after(item.getModified())) {
                    answer.setState(ItemStateEnum.RELEASE_IS_NEWER);
                } else {
                    answer.setState(ItemStateEnum.DRAFT_IS_NEWER);
                }
            } else {
                answer.setState(ItemStateEnum.RELEASE_NOT_EXIST);
            }
        }
    }

    public static DBItemXmlEditorConfiguration createOrUpdateConfigurationIfReleaseOrDeploy(String implPath, ObjectType type, Long id,
            String configuration, String configurationJson, String account, Long auditLogId) throws Exception {
        SOSHibernateSession session = null;
        try {
            session = Globals.createSosHibernateStatelessConnection(implPath);
            XmlEditorDbLayer dbLayer = new XmlEditorDbLayer(session);
            session.beginTransaction();

            DBItemXmlEditorConfiguration item = null;
            switch (type) {
            case NOTIFICATION:
                String name = StandardSchemaHandler.getDefaultConfigurationName(type);
                item = dbLayer.getObject(type.name(), name);
                if (item == null) {
                    session.save(createReleasedNotificationConfiguration(name, configuration, configurationJson, account, auditLogId));
                } else {
                    session.update(updateReleasedOrDeployedConfiguration(item, configuration, configurationJson, account, auditLogId));
                }
                break;
            case YADE:
                item = dbLayer.getObject(id);
                if (item == null) {
                    throw new Exception("[" + type + "][id=" + id + "]not found");
                }
                session.update(updateReleasedOrDeployedConfiguration(item, configuration, configurationJson, account, auditLogId));
                break;
            default:
                throw new Exception("Unsupported type=" + type);
            }

            Globals.commit(session);
            return item;
        } catch (Throwable e) {
            Globals.rollback(session);
            throw e;
        } finally {
            Globals.disconnect(session);
        }
    }

    private static DBItemXmlEditorConfiguration createReleasedNotificationConfiguration(String name, String configuration, String configurationJson,
            String account, Long auditLogId) throws Exception {
        DBItemXmlEditorConfiguration item = new DBItemXmlEditorConfiguration();
        item.setType(ObjectType.NOTIFICATION.name());
        item.setName(name);
        item.setConfigurationDraft(null);
        item.setConfigurationDraftJson(null);
        item.setConfigurationReleased(configuration);
        item.setConfigurationReleasedJson(Utils.serialize(configurationJson));
        item.setSchemaLocation(JocXmlEditor.getSchemaLocation4Db(ObjectType.NOTIFICATION, null));
        item.setAuditLogId(auditLogId);
        item.setAccount(account);
        item.setCreated(new Date());
        item.setModified(item.getCreated());
        item.setReleased(item.getCreated());
        return item;
    }

    private static DBItemXmlEditorConfiguration updateReleasedOrDeployedConfiguration(DBItemXmlEditorConfiguration item, String configuration,
            String configurationJson, String account, Long auditLogId) throws Exception {
        item.setConfigurationDraft(null);
        item.setConfigurationDraftJson(null);
        item.setConfigurationReleased(configuration);
        item.setConfigurationReleasedJson(Utils.serialize(configurationJson));
        item.setAuditLogId(auditLogId);
        item.setAccount(account);
        item.setModified(new Date());
        item.setReleased(item.getModified());
        return item;
    }

    public ReadStandardConfigurationAnswer getAnswer() {
        return answer;
    }

    private static String getNotificationSchemaIdentifier() throws Exception {
        return JocXmlEditor.NOTIFICATION_SCHEMA_FILENAME;
    }

    private String getXML(String xml, boolean isYADE) throws Exception {
        if (!isYADE) {
            return xml;
        }
        // TODO activate SOSXML.transformXMLWithXSL
        return xml;// SOSXML.transformXMLWithXSL(xml, getYADEXSLAllVersionsToCurrent());
    }

    public static String getYADEXMLForDeployment(String xml) throws Exception {
        // TODO activate
        return xml;// SOSXML.transformXMLWithXSL(xml, cccc());
    }

    private static String getYADEXSLAllVersionsToCurrent() throws Exception {
        return SOSClassUtil.readResourceFile(new JocXmlEditor().getClass(), JocXmlEditor.YADE_XSL_ALL_VERSIONS_TO_CURRENT_VERSION_RESOURCE_PATH);
    }

    private void recreateJson(String xml) throws Exception {
        answer.setRecreateJson(true);
        try {
            answer.setConfigurationJson(convert(type, xml));
        } catch (SOSXMLNotMatchSchemaException e) {
            answer.setRecreateJson(false);
        }
    }

    private void deserializeJson(String json, String xml) throws Exception {
        try {
            answer.setConfigurationJson(Utils.deserializeJson(json));
        } catch (Throwable e) {
            recreateJson(xml);
        }
    }

    private String convert(ObjectType type, String xml) throws Exception {
        return new Xml2JsonConverter().convert(type, answer.getSchema(), xml);
    }

}
