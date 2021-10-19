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
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSClassList;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.db.joc.DBItemJocVariable;
import com.sos.joc.exceptions.JocConfigurationException;

public class DbInstaller {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(DbInstaller.class);
    private static final List<String> sqlFileSpecs = Arrays.asList("(?<!_insert|_rename|_alter|_procedure|_trigger|_manually)\\.sql$",
            "_alter\\.sql$", "_procedure\\.sql$");
    private static final List<String> sqlFileSpecsInsert = Arrays.asList("_insert\\.sql$", "_trigger\\.sql$");
    private static final EnumSet<Dbms> supportedDbms = EnumSet.of(Dbms.ORACLE, Dbms.MSSQL, Dbms.MYSQL, Dbms.PGSQL, Dbms.H2);
    
    
    public static void createTables() throws JocConfigurationException, SOSHibernateException {
        
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        
        try {
            Path createTableSignalFile = Paths.get(System.getProperty("user.dir"), "etc", "createTables");
            boolean createTables = Files.exists(createTableSignalFile);
            try {
                Files.deleteIfExists(createTableSignalFile);
            } catch (IOException e) {
                LOGGER.warn("Problem deleting signal file " + createTableSignalFile.toString(), e);
            }
            
            if (Globals.sosCockpitProperties == null) {
                Globals.sosCockpitProperties = new JocCockpitProperties();
            }
            if (createTables || Globals.sosCockpitProperties.getProperty("create_db_tables", false)) {
                
                String jettyHome = System.getProperty("jetty.home");
                if (jettyHome == null || jettyHome.isEmpty()) {
                    LOGGER.warn("Creating database table are only supported in Jetty");
                } else {
                    Path sqlsFolderParent = Paths.get(jettyHome).getParent().resolve("db");
                    if (!Files.isDirectory(sqlsFolderParent)) {
                        throw new SOSHibernateConfigurationException("Folder '" + sqlsFolderParent.toString() + "' doesn't exist.");
                    }

                    factory = new SOSHibernateFactory(Globals.getHibernateConfFile());
                    // SOSClassList sosClassList = DBLayer.getJocClassMapping();
                    // factory.addClassMapping(sosClassList);
                    SOSClassList cl = new SOSClassList();
                    cl.add(DBItemJocVariable.class);
                    factory.addClassMapping(cl);
                    factory.setAutoCommit(false);
                    factory.build();

                    Enum<Dbms> dbms = factory.getDbms();
                    if (!supportedDbms.contains(dbms)) {
                        throw new SOSHibernateConfigurationException("Unsupported dbms: " + dbms.name());
                    }
                    session = factory.openStatelessSession();
                    
                    // if (missingAnyTable(sosClassList, session)) {
                    // create(session, dbms.name(), sqlsFolderParent);
                    // }
                    
                    if (updateIsNecessary(session) || Globals.sosCockpitProperties.getProperty("create_db_tables", false)) {
                        create(session, dbms.name(), sqlsFolderParent);
                    }

                }
                try {
                    Globals.sosCockpitProperties.updateProperty("create_db_tables", "false");
                } catch (IOException e) {
                    LOGGER.warn("Problem updating the joc.properties file", e);
                }
            }
            
//        } catch (Exception e) {
//            LOGGER.error("Error during database table creation: ", e);
        } finally {
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }
    
    private static void create(SOSHibernateSession session, String dbms, Path sqlsFolderParent) throws SOSHibernateException {
        Path inputDir = sqlsFolderParent.resolve(dbms.toLowerCase());
        if (Files.isDirectory(inputDir)) {
            boolean hasError = false;
            SOSHibernateFileProcessor processor = new SOSHibernateFileProcessor();
            LOGGER.info("...installing tables in " + dbms + " database which not exist");
            for (String sqlFileSpec : sqlFileSpecs) {
                processor.clearResult();
                processor.setFileSpec(sqlFileSpec);
                processor.process(session, inputDir);
                if (!hasError) {
                    hasError = processor.hasError(); 
                }
            }
            if (!hasError) {
                LOGGER.info("...insert initial rows into tables in " + dbms + " database");
                for (String sqlFileSpec : sqlFileSpecsInsert) {
                    processor.clearResult();
                    processor.setFileSpec(sqlFileSpec);
                    processor.process(session, inputDir);
                    if (!hasError) {
                        hasError = processor.hasError();
                    }
                }
            } else {
                LOGGER.info("...insert initial rows into tables in SQL database is skipped because of previous error");
            }
            if (hasError) {
                throw new SOSHibernateException("Error occurred while creating the database tables."); 
            }
        } else {
            throw new SOSHibernateConfigurationException("Folder with SQL scripts not found: " + inputDir.toString());
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
    
//    private static boolean missingTable(Class<?> clazz, SOSHibernateSession session) throws SOSHibernateException {
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
    
    private static boolean updateIsNecessary(SOSHibernateSession session) {
        boolean update = true;
        try {
            DBItemJocVariable item = session.get(DBItemJocVariable.class, "version");
            String version = item.getTextValue();
            if (version != null) {
                LOGGER.info("Version in database: " + version);
                int compare = Globals.curVersionCompareWith(version);
                if (compare < 0) {
                    update = false; 
                } else if (compare == 0 && !version.contains("-SNAPSHOT")) {
                    update = false;
                }
            }
        } catch (Exception e) {
            //
        }
        return update;
    }

}
