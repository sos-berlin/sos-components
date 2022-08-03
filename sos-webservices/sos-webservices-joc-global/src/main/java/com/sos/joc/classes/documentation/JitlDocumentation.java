package com.sos.joc.classes.documentation;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.xml.SOSXML;
import com.sos.joc.Globals;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DocumentationDBLayer;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;

public class JitlDocumentation {

    private static final String DOCUS = "/sos-jitl-jobdoc.zip";
    private static final String NOTIFICATION = "/notification.xml";
    private static final String NOTIFICATION_XSD = "Notification_configuration_v1.0.xsd";
    private static final String MAINFOLDER = "/sos";
    public static final String FOLDER = MAINFOLDER + "/jitl-jobs";
    public static final String XSLT = FOLDER + "/js7_job_documentation_v1.1.xsl";
    private static final Logger LOGGER = LoggerFactory.getLogger(JitlDocumentation.class);

    public static void saveOrUpdate() {
        InputStream stream = null;
        SOSHibernateSession connection = null;
        try {
            connection = Globals.createSosHibernateStatelessConnection(JitlDocumentation.class.getSimpleName());
            //updateVersion(connection);
            updateNotification(connection);
            stream = JitlDocumentation.class.getClassLoader().getResourceAsStream(DOCUS);
            if (stream != null) {
                try {
                    DocumentationDBLayer dbLayer = new DocumentationDBLayer(connection);
                    if (updateIsNecessary(dbLayer)) {
                        DocumentationHelper.readZipFileContent(stream, MAINFOLDER, dbLayer);
                        LOGGER.info("JITL-Job documentations are inserted/updated.");
                    } else {
                        LOGGER.info("JITL-Job documentations are already up to date.");
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error while insert/update JITL-Job documentations: ", e);
                }
            } else {
                LOGGER.warn(String.format("Error while reading resource %1$s from classpath: not found", DOCUS));
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Error while reading resource %1$s from classpath: ", DOCUS), e);
        } finally {
            Globals.disconnect(connection);
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
            }
        }
    }
    
    private static void updateNotification(SOSHibernateSession session) {
        try {

            URL notificationUrl = JitlDocumentation.class.getClassLoader().getResource(NOTIFICATION);
            if (notificationUrl != null) {
                Path notificationPath = Paths.get(notificationUrl.toURI());
                if (notificationPath != null && Files.exists(notificationPath)) {
                    String notification = new String(Files.readAllBytes(notificationPath), StandardCharsets.UTF_8);
                    String notificationFilename = notificationPath.getFileName().toString();

                    Query<DBItemXmlEditorConfiguration> query = session.createQuery("from " + DBLayer.DBITEM_XML_EDITOR_CONFIGURATIONS
                            + " where type in (:types)");
                    query.setParameterList("types", Arrays.asList("NOTIFICATION", "NOTIFICATION_DEFAULT"));
                    List<DBItemXmlEditorConfiguration> result = query.getResultList();
                    Map<String, List<DBItemXmlEditorConfiguration>> resultMapByType = Collections.emptyMap();
                    if (result != null) {
                        resultMapByType = result.stream().collect(Collectors.groupingBy(DBItemXmlEditorConfiguration::getType));
                    }

                    if (!resultMapByType.containsKey("NOTIFICATION")) {
                        DBItemXmlEditorConfiguration item = getNotificationDbItem(null, "NOTIFICATION", notification, notificationFilename);

                        session.save(item);
                        LOGGER.info(notificationPath.getFileName().toString() + " is inserted in database");
                    }
                    if (!resultMapByType.containsKey("NOTIFICATION_DEFAULT")) {
                        DBItemXmlEditorConfiguration item = getNotificationDbItem(null, "NOTIFICATION_DEFAULT", notification, notificationFilename);

                        session.save(item);
                        LOGGER.info(notificationPath.getFileName().toString() + " as default is inserted in database");
                    } else {
                        DBItemXmlEditorConfiguration item = resultMapByType.get("NOTIFICATION_DEFAULT").get(0);
                        if (!notification.equals(item.getConfigurationReleased())) {
                            item = getNotificationDbItem(item, "NOTIFICATION_DEFAULT", notification, notificationFilename);
                            session.update(item);
                            LOGGER.info(notificationPath.getFileName().toString() + " as default is update in database");
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Problem inserting notification.xml in database", e);
        }
    }
    
    private static DBItemXmlEditorConfiguration getNotificationDbItem(DBItemXmlEditorConfiguration item, String type, String notification,
            String notificationFilename) {
        if (item == null) {
            item = new DBItemXmlEditorConfiguration();
            item.setId(null);
            item.setCreated(Date.from(Instant.now()));
        }
        item.setType(type);
        item.setName(notificationFilename);
        item.setSchemaLocation(NOTIFICATION_XSD);
        item.setConfigurationDraft(null);
        item.setConfigurationDraftJson(null);
        item.setConfigurationReleased(notification);
        item.setConfigurationReleasedJson(null);
        item.setAuditLogId(0L);
        item.setAccount("root");
        item.setReleased(Date.from(Instant.now()));
        item.setModified(item.getReleased());
        return item;
    }
    
    private static boolean updateIsNecessary(DocumentationDBLayer dbLayer) {
        boolean update = true;
        try {
            DBItemDocumentation xsltItem = dbLayer.getDocumentation(XSLT);
            if (xsltItem != null) {
                String xsltContent = xsltItem.getContent();
                if (xsltContent != null) {
                    Node versionNode = SOSXML.newXPath().selectNode(SOSXML.parse(xsltContent), "//variable[@name='version']/@select");
                    if (versionNode != null && versionNode.getNodeValue() != null) {
                        String version = versionNode.getNodeValue();
                        if (version != null) {
                            version = version.replaceAll("'", "");
                            LOGGER.info("JITL-Job documentation version in database: " + version);
                            int compare = Globals.curVersionCompareWith(version);
                            if (compare < 0) {
                                update = false; 
                            } else if (compare == 0 && !version.contains("-SNAPSHOT")) {
                                update = false;
                            }
                        }
                    } else {
                        LOGGER.info("Couldn't determine JITL-Job documentation version.");
                    }
                }
            }
        } catch (Exception e) {
            //
        }
        return update;
    }
}
