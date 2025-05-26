package com.sos.joc.xmleditor.commons;

import java.io.InputStream;
import java.util.List;

import org.w3c.dom.Document;

import com.sos.commons.util.SOSString;
import com.sos.commons.xml.SOSXML;
import com.sos.commons.xml.SOSXmlHashComparator;
import com.sos.commons.xml.SOSXmlXsdValidator;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;
import com.sos.joc.exceptions.JocMissingRequiredParameterException;
import com.sos.joc.model.xmleditor.common.ObjectType;

public class JocXmlEditor {

    public static final String NOTIFICATION_SCHEMA_FILENAME = "Notification_configuration_v1.0.xsd";
    public static final String NOTIFICATION_SCHEMA_URI = "https://www.sos-berlin.com/schema/jobscheduler/" + NOTIFICATION_SCHEMA_FILENAME;
    public static final String NOTIFICATION_SCHEMA_RESOURCE_PATH = "xmleditor/notification/xsd/" + NOTIFICATION_SCHEMA_FILENAME;

    // TODO set YADE_SCHEMA_FILENAME and activate StandardSchemaHandler XSL transformation
    public static final String YADE_SCHEMA_FILENAME = "YADE_configuration_v2.0.0.xsd";
    public static final String YADE_SCHEMA_URI = "https://www.sos-berlin.com/schema/yade/" + YADE_SCHEMA_FILENAME;
    public static final String YADE_SCHEMA_RESOURCE_PATH = "xmleditor/yade/xsd/" + YADE_SCHEMA_FILENAME;
    // XMLEDITOR API - read/store etc(transform v1,v2 to v2)
    public static final String YADE_TRANSFORM_SCHEMA_ANY_TO_CURRENT_RESOURCE_PATH = "xmleditor/yade/xsl/YADE-transform-schema-any-to-v2.0.0.xsl";
    // INVENTORY IP - store to inventory/deploy to agent (transform v2 to v1+v2)
    public static final String YADE_TRANSFORM_SCHEMA_CURRENT_TO_MERGED_LEGACY_RESOURCE_PATH =
            "xmleditor/yade/xsl/YADE-transform-schema-v2.0.0-merged-legacy.xsl";

    public static final String SCHEMA_ROOT_ELEMENT_NAME_YADE = "Configurations";
    public static final String SCHEMA_ROOT_ELEMENT_NAME__NOTIFICATION = "Configurations";

    public static final String CONFIGURATION_BASENAME_YADE = "yade";
    public static final String CONFIGURATION_BASENAME_NOTIFICATION = "notification";

    public static final String APPLICATION_PATH = "xmleditor";
    public static final String CHARSET = "UTF-8";
    public static final String JOC_SCHEMA_LOCATION = "xsd";
    public static final String NEW_LINE = "\r\n";

    public static final String CODE_NO_CONFIGURATION_EXIST = "XMLEDITOR-101";
    public static final String ERROR_CODE_VALIDATION_ERROR = "XMLEDITOR-401";
    public static final String ERROR_CODE_UNSUPPORTED_OBJECT_TYPE = "XMLEDITOR-402";
    public static final String ERROR_CODE_MISSING_ARGUMENT = "XMLEDITOR-403";

    public static Document validate(ObjectType type, String schema, String xml) throws Exception {
        return SOSXmlXsdValidator.validate(schema, xml, ObjectType.YADE.equals(type) ? true : false);
    }

    public static boolean isYADE(ObjectType type) {
        return ObjectType.YADE.equals(type);
    }

    public static boolean isNotification(ObjectType type) {
        return ObjectType.NOTIFICATION.equals(type);
    }

    public static boolean isOther(ObjectType type) {
        return ObjectType.OTHER.equals(type);
    }

    public static boolean isStandardType(ObjectType type) {
        return isYADE(type) || isNotification(type);
    }

    public static String getRootElementName(ObjectType type) {
        if (isYADE(type)) {
            return SCHEMA_ROOT_ELEMENT_NAME_YADE;
        } else if (isNotification(type)) {
            return SCHEMA_ROOT_ELEMENT_NAME__NOTIFICATION;
        }
        return null;
    }

    public static Document parseXml(String xml) throws Exception {
        if (SOSString.isEmpty(xml)) {
            return null;
        }
        return SOSXML.parse(xml);
    }

    public static Document parseXml(InputStream is) throws Exception {
        if (is == null) {
            return null;
        }
        return SOSXML.parse(is);
    }

    public static String getResourceImplPath(final String path) {
        return String.format("./%s/%s", APPLICATION_PATH, path);
    }

    public static String getSchemaLocation4Db(ObjectType type, String schemaIdentifier) {
        switch (type) {
        case YADE:
            return YADE_SCHEMA_FILENAME;
        case NOTIFICATION:
            return NOTIFICATION_SCHEMA_FILENAME;
        case OTHER:
            return schemaIdentifier;
        }
        return null;
    }

    public static boolean isChanged(DBItemXmlEditorConfiguration item, String currentConfiguration) throws Exception {
        if (currentConfiguration == null) {
            return true;
        }

        if (item.getConfigurationDraft() != null) {
            return !SOSXmlHashComparator.equals(currentConfiguration, item.getConfigurationDraft());
        } else if (item.getConfigurationReleased() != null) {
            return !SOSXmlHashComparator.equals(currentConfiguration, item.getConfigurationReleased());
        }
        return true;
    }

    public static boolean checkRequiredParameter(final String paramKey, final ObjectType paramVal) throws JocMissingRequiredParameterException {
        if (paramVal == null || paramVal.toString().isEmpty()) {
            throw new JocMissingRequiredParameterException(String.format("undefined '%1$s'", paramKey));
        }
        return true;
    }

    public static boolean checkRequiredParameter(final String paramKey, final List<?> paramVal) throws JocMissingRequiredParameterException {
        if (paramVal == null || paramVal.size() == 0) {
            throw new JocMissingRequiredParameterException(String.format("undefined '%1$s'", paramKey));
        }
        return true;
    }

}
