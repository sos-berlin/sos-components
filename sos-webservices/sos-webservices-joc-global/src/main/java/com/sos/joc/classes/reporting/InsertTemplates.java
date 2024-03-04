package com.sos.joc.classes.reporting;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSClassList;
import com.sos.joc.db.reporting.DBItemReportTemplate;

public class InsertTemplates {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(InsertTemplates.class);
    
    public InsertTemplates() {
        
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            LOGGER.info(String.format("Usage: %s hibernate_config_file   input_dir", InsertTemplates.class.getSimpleName()));
            LOGGER.info("            hibernate_config_file : required");
            LOGGER.info("                                    path to the hibernate configuration file");
            LOGGER.info("            input_dir             : required");
            LOGGER.info("                                    directory");
            return;
        }
        
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        int exitCode = 0;
        try {
            
            Path templatesDir = Paths.get(args[1].trim());
            if (Files.isDirectory(templatesDir)) {
                
                factory = new SOSHibernateFactory(args[0]);
                
                SOSClassList cl = new SOSClassList();
                cl.add(DBItemReportTemplate.class);
                factory.addClassMapping(cl);
                factory.setAutoCommit(true);
                factory.build();

                session = factory.openStatelessSession("UpdateTemplates");
                
                Templates.updateTemplates(templatesDir, session);
                
            } else {
                throw new FileNotFoundException(templatesDir.toString() + " is not a directory.");
            }

        } catch (Exception e) {
            exitCode = 1;
            e.printStackTrace(System.err);
        } finally {
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
        System.exit(exitCode);

    }

}
