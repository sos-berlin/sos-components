package com.sos.joc.classes.documentation;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.xml.SOSXML;
import com.sos.joc.Globals;
import com.sos.joc.db.documentation.DBItemDocumentation;
import com.sos.joc.db.documentation.DocumentationDBLayer;

public class JitlDocumentation {

    private static final String DOCUS = "/sos-jitl-jobdoc.zip";
    private static final String FOLDER = "/sos";
    private static final String XSLT = FOLDER + "/jitl-jobs/js7_job_documentation_v1.1.xsl";
    private static final Logger LOGGER = LoggerFactory.getLogger(JitlDocumentation.class);

    public static void saveOrUpdate() {
        InputStream stream = null;
        try {
            SOSHibernateSession connection = null;
            stream = JitlDocumentation.class.getClassLoader().getResourceAsStream(DOCUS);
            if (stream != null) {
                try {
                    connection = Globals.createSosHibernateStatelessConnection(JitlDocumentation.class.getSimpleName());
                    DocumentationDBLayer dbLayer = new DocumentationDBLayer(connection);
                    if (updateIsNecessary(dbLayer)) {
                        DocumentationHelper.readZipFileContent(stream, FOLDER, dbLayer);
                        LOGGER.info("JITL-Job documentations are inserted/updated.");
                    } else {
                        LOGGER.info("JITL-Job documentations are already up to date.");
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error while insert/update JITL-Job documentations: ", e);
                } finally {
                    Globals.disconnect(connection);
                }
            } else {
                LOGGER.warn(String.format("Error while reading resource %1$s from classpath: not found", DOCUS));
            }
        } catch (Exception e) {
            LOGGER.warn(String.format("Error while reading resource %1$s from classpath: ", DOCUS), e);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (Exception e) {
            }
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
