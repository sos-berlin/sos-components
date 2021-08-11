package com.sos.joc.classes.documentation;

import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Date;
import java.time.Instant;

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
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.db.xmleditor.DBItemXmlEditorConfiguration;

public class JitlDocumentation {

    private static final String DOCUS = "/sos-jitl-jobdoc.zip";
    private static final String NOTIFICATION = "/notification.xml";
    private static final String FOLDER = "/sos";
    private static final String XSLT = FOLDER + "/jitl-jobs/js7_job_documentation_v1.1.xsl";
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
                        DocumentationHelper.readZipFileContent(stream, FOLDER, dbLayer);
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
            Query<Long> query = session.createQuery("select count(*) from " + DBLayer.DBITEM_XML_EDITOR_CONFIGURATIONS + " where type=:type");
            query.setParameter("type", "NOTIFICATION");
            if (query.getSingleResult() == 0L) {
                URL notificationUrl = JitlDocumentation.class.getClassLoader().getResource(NOTIFICATION);
                if (notificationUrl != null) {
                    Path notificationPath = Paths.get(notificationUrl.toURI());
                    if (notificationPath != null && Files.exists(notificationPath)) {
                        String notification = new String(Files.readAllBytes(Paths.get(notificationUrl.toURI())), StandardCharsets.UTF_8);
                        DBItemXmlEditorConfiguration item = new DBItemXmlEditorConfiguration();
                        item.setId(null);
                        item.setType("NOTIFICATION");
                        item.setName("notification.xml");
                        item.setSchemaLocation("Notification_configuration_v1.0.xsd");
                        item.setConfigurationDraft(null);
                        item.setConfigurationDraftJson(null);
                        item.setConfigurationReleased(notification);
                        item.setConfigurationReleasedJson(null);
                        item.setAuditLogId(0L);
                        item.setAccount("root");
                        item.setReleased(Date.from(Instant.now()));
                        item.setModified(item.getReleased());
                        item.setCreated(item.getReleased());
                        
                        //session.beginTransaction();
                        session.save(item);
                        //session.commit();
                    }
                }
            }
        } catch (Exception e) {
            Globals.rollback(session);
            LOGGER.warn("Problem inserting notification.xml in database", e);
        }
    }
    
    private static void updateVersion(SOSHibernateSession session) {
        try {
            session.beginTransaction();
            DBItemJocVariable item = session.get(DBItemJocVariable.class, "version");
            if (item == null) {
                item = new DBItemJocVariable();
                item.setName("version");
                item.setTextValue(Globals.curVersion);
                session.save(item);
            } else {
                item.setTextValue(Globals.curVersion);
                session.update(item);
            }
            //session.commit();
        } catch (Exception e) {
            Globals.rollback(session);
            LOGGER.warn("Problem updating version in database", e);
        }
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
