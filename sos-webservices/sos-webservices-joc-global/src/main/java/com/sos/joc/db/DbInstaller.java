package com.sos.joc.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;
import com.sos.commons.hibernate.SOSHibernateFileProcessor;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;

public class DbInstaller {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DbInstaller.class);
    private static final List<String> sqlFileSpecs = Arrays.asList("(?<!_insert|_rename|_alter|_procedure|_trigger|_manually)\\.sql$",
            "_alter\\.sql$", "_procedure\\.sql$");
    private static final List<String> sqlFileSpecsInsert = Arrays.asList("_insert\\.sql$", "_trigger\\.sql$");
    private static final EnumSet<Dbms> supportedDbms = EnumSet.of(Dbms.ORACLE, Dbms.MSSQL, Dbms.MYSQL, Dbms.PGSQL, Dbms.H2);
    
    
    public static void createTables() {
        
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            if (Globals.sosCockpitProperties == null) {
                Globals.sosCockpitProperties = new JocCockpitProperties();
            }
            if (Globals.sosCockpitProperties.getProperty("create_db_tables", false)) {
                
                Path sqlsFolderParent = Paths.get(System.getProperty("user.dir"), "db");
                if (!Files.isDirectory(sqlsFolderParent)) {
                    throw new IOException("Folder '" + sqlsFolderParent.toString() + "' doesn't exist.");
                }

                factory = new SOSHibernateFactory(Globals.getHibernateConfFile());
//                SOSClassList sosClassList = DBLayer.getJocClassMapping();
//                factory.addClassMapping(sosClassList);
                factory.setAutoCommit(false);
                factory.build();

                Enum<Dbms> dbms = factory.getDbms();
                if (!supportedDbms.contains(dbms)) {
                    throw new SOSHibernateConfigurationException("Unsupported dbms: " + dbms.name());
                }
                session = factory.openStatelessSession();

//                if (missingAnyTable(sosClassList, session)) {
                create(session, dbms.name(), sqlsFolderParent);
//                }
            }
            
        } catch (Exception e) {
            LOGGER.error("Error during database table creation: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }
    
    private static void create(SOSHibernateSession session, String dbms, Path sqlsFolderParent) throws IOException {
        Path inputDir = sqlsFolderParent.resolve(dbms.toLowerCase());
        if (Files.isDirectory(inputDir)) {
            SOSHibernateFileProcessor processor = new SOSHibernateFileProcessor();
            LOGGER.info("...installing tables in SQL database which not exist");
            for (String sqlFileSpec : sqlFileSpecs) {
                processor.clearResult();
                processor.setFileSpec(sqlFileSpec);
                processor.process(session, inputDir);
            }
            LOGGER.info("...insert initial rows into tables in SQL database");
            for (String sqlFileSpec : sqlFileSpecsInsert) {
                processor.clearResult();
                processor.setFileSpec(sqlFileSpec);
                processor.process(session, inputDir);
            }
        } else {
            throw new IOException("Folder with SQL scripts not found: " + inputDir.toString());
        }
    }


//    private boolean missingAnyTable(SOSClassList sosClassList, SOSHibernateSession session) throws SOSHibernateException {
//        for (Class<?> clazz : sosClassList.getClasses()) {
//            if (missingTable(clazz, session)) {
//                return true;
//            }
//        }
//        return false;
//    }
//    
//    private boolean missingTable(Class<?> clazz, SOSHibernateSession session) throws SOSHibernateException {
//        try {
//            Query<Long> query = session.createQuery("select count(*) from " + clazz.getSimpleName());
//            session.getResultList(query);
//        } catch (SOSHibernateQueryException e) {
//            if (e.getCause() != null && e.getCause() instanceof SQLGrammarException) {
//                Table table = clazz.getAnnotation(Table.class);
//                String tableName = table.name();
//                LOGGER.info("database table '" + tableName + "' is missing.");
//                return true;
//            }
//            throw e;
//        } catch (SOSHibernateException e) {
//            throw e;
//        }
//        return false;
//    }

}
