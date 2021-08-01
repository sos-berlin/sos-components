package com.sos.joc.classes.documentation;

import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.db.documentation.DocumentationDBLayer;

public class JitlDocumentation {

    private static final String DOCUS = "/sos-jitl-jobdoc.zip";
    private static final Logger LOGGER = LoggerFactory.getLogger(JitlDocumentation.class);

    public static void saveOrUpdate() {
        InputStream stream = null;
        try {
            SOSHibernateSession connection = null;
            stream = JitlDocumentation.class.getClassLoader().getResourceAsStream(DOCUS);
            if (stream != null) {
                try {
                    connection = Globals.createSosHibernateStatelessConnection(JitlDocumentation.class.getSimpleName());
                    DocumentationHelper.readZipFileContent(stream, "/sos", new DocumentationDBLayer(connection));
                    LOGGER.warn("JITL-Job documentations are inserted/updated.");
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
}
