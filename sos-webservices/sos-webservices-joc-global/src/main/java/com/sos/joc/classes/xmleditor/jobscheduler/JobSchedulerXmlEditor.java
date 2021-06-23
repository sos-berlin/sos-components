package com.sos.joc.classes.xmleditor.jobscheduler;

import com.sos.joc.model.xmleditor.common.ObjectType;

public class JobSchedulerXmlEditor {

    public static final String CONFIGURATION_LIVE_FOLDER = "sos/.configuration";
    public static final String CONFIGURATION_BASENAME_YADE = "yade";
    public static final String CONFIGURATION_BASENAME_NOTIFICATION = "notification";

    public static final String SCHEMA_URI_YADE = "https://www.sos-berlin.com/schema/yade/YADE_configuration_v1.12.xsd";
    public static final String SCHEMA_URI_NOTIFICATION = "https://www.sos-berlin.com/schema/jobscheduler/Notification_configuration_v1.0.xsd";

    public static final String SCHEMA_FILENAME_YADE = "YADE_configuration_v1.12.xsd";
    public static final String SCHEMA_FILENAME_NOTIFICATION = "Notification_configuration_v1.0.xsd";

    public static final String SCHEMA_ROOT_ELEMENT_NAME_YADE = "Configurations";
    public static final String SCHEMA_ROOT_ELEMENT_NAME__NOTIFICATION = "Configurations";

    public static String getLivePathYadeXml() {
        return String.format("%s/%s/%s.xml", CONFIGURATION_LIVE_FOLDER, CONFIGURATION_BASENAME_YADE, CONFIGURATION_BASENAME_YADE);
    }

    public static String getLivePathYadeIni() {
        return String.format("%s/%s/%s.ini", CONFIGURATION_LIVE_FOLDER, CONFIGURATION_BASENAME_YADE, CONFIGURATION_BASENAME_YADE);
    }

    public static String getLivePathYadeXsd() {
        return String.format("%s/%s/%s", CONFIGURATION_LIVE_FOLDER, CONFIGURATION_BASENAME_YADE, SCHEMA_FILENAME_YADE);
    }

    public static String getLivePathNotificationXml() {
        return String.format("%s/%s/%s.xml", CONFIGURATION_LIVE_FOLDER, CONFIGURATION_BASENAME_NOTIFICATION, CONFIGURATION_BASENAME_NOTIFICATION);
    }

    public static String getLivePathNotificationXsd() {
        return String.format("%s/%s/%s", CONFIGURATION_LIVE_FOLDER, CONFIGURATION_BASENAME_NOTIFICATION, SCHEMA_FILENAME_NOTIFICATION);
    }

    public static String getNormalizedLiveFolder(ObjectType type) {
        if (type.equals(ObjectType.YADE)) {
            return String.format("/%s/%s", CONFIGURATION_LIVE_FOLDER, CONFIGURATION_BASENAME_YADE);
        } else if (type.equals(ObjectType.NOTIFICATION)) {
            return String.format("/%s/%s", CONFIGURATION_LIVE_FOLDER, CONFIGURATION_BASENAME_NOTIFICATION);
        }
        return null;
    }

    public static String getRootElementName(ObjectType type) {
        if (type.equals(ObjectType.YADE)) {
            return SCHEMA_ROOT_ELEMENT_NAME_YADE;
        } else if (type.equals(ObjectType.NOTIFICATION)) {
            return SCHEMA_ROOT_ELEMENT_NAME__NOTIFICATION;
        }
        return null;
    }
}
