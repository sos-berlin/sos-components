package com.sos.joc.servlet;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSShell;
import com.sos.joc.Globals;
import com.sos.joc.bean.JOCMBeanServer;
import com.sos.joc.classes.DBMoveIamConfiguration;
import com.sos.joc.classes.DependencyUpdate;
import com.sos.joc.classes.JocCertificate;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.agent.AgentClusterWatch;
import com.sos.joc.classes.agent.AgentStoreUtils;
import com.sos.joc.classes.calendar.ControllerSettings;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.classes.documentation.JitlDocumentation;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.proxy.ClusterWatch;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.classes.publish.listener.DeploymentHistoryMoveListener;
import com.sos.joc.classes.quicksearch.QuickSearchStore;
import com.sos.joc.classes.reporting.AReporting;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowRefs;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.db.DbInstaller;
import com.sos.joc.db.cluster.CheckInstance;
import com.sos.joc.db.inventory.instance.InventorySubagentClustersDBLayer;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.log4j2.NotificationAppender;

import jakarta.servlet.ServletException;

public class JocServletContainer extends ServletContainer {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocServletContainer.class);

    private static final long serialVersionUID = 1L;

    public JocServletContainer() {
        super();
    }

    @Override
    public void init() throws ServletException {
        super.init();
        
        Globals.setSystemProperties();
        Globals.sosCockpitProperties = new JocCockpitProperties();
        Globals.StartUpLOGGER.info("START");
        cleanupOldLogFiles(0);

        Globals.readUnmodifiables();
        try {
            DbInstaller.createTables();
        } catch (Exception e) {
            throw new ServletException(e);
        }

        try {
            CheckInstance.check();
        } catch (JocConfigurationException | SOSHibernateException e) {
            if (Globals.sosHibernateFactory != null) {
                LOGGER.info("----> closing DB Connections");
                Globals.sosHibernateFactory.close();
            }
            LOGGER.error("", e);
            CheckInstance.stopJOC();
            Globals.StartUpLOGGER.info("STOP");
            throw new ServletException(e);
        }
        
        JocCertificate.updateCertificate();
        DBMoveIamConfiguration.execute();

        ClusterWatch.getInstance();
        ControllerSettings.getInstance();
        OrderTags.getInstance();
        AgentStoreUtils.getInstance();
        
        try {
            Globals.setProperties();
        } catch (Exception e1) {
            LOGGER.error(e1.toString());
        }
        
        new Thread(() -> {
            Proxies.startAll(Globals.sosCockpitProperties, ProxyUser.JOC);
            
            new Thread(() -> {
                InventorySubagentClustersDBLayer.fillEmptyControllerIds();
                WorkflowPaths.init();
                WorkflowRefs.init();
                DeploymentHistoryMoveListener.getInstance();
                JitlDocumentation.saveOrUpdate();
                SOSShell.printSystemInfos();
                SOSShell.printJVMInfos();
                AReporting.deleteTmpFolder();
            }, "servlet-init2").start();
            
            JocClusterService.getInstance().start(StartupMode.automatic, true);
            DependencyUpdate.getInstance().updateThreaded();
            JOCMBeanServer.register();
            cleanupAllTempDirSubFolders();
        }, "servlet-init").start();
        
        
    }

    @Override
    public void destroy() {
        LOGGER.debug("----> destroy on close JOC");

        NotificationAppender.doNotify = false;
        QuickSearchStore.close();
        AgentClusterWatch.close();
        JOCMBeanServer.unregister();

        // 1 - stop cluster: boolean deleteActiveCurrentMember, boolean resetCurrentInstanceHeartBeat
        JocClusterService.getInstance().stop(StartupMode.automatic, true, true);
        JocClusterServiceLogger.clearAllLoggers();
        // 2 - close proxies
        Proxies.closeAll();
        DependencyUpdate.getInstance().close();
        if (Globals.sosHibernateFactory != null) {
            // if (Globals.sosHibernateFactory.dbmsIsH2()) {
            // SOSHibernateSession connection = null;
            // try {
            // connection = Globals.createSosHibernateStatelessConnection("closeH2");
            // connection.createQuery("SHUTDOWN").executeUpdate();
            // } catch (Exception e) {
            // LOGGER.warn("shutdown H2 database: " + e.toString());
            // } finally {
            // Globals.disconnect(connection);
            // }
            // }
            LOGGER.info("----> closing DB Connections");
            Globals.sosHibernateFactory.close();
        }

        super.destroy();
        cleanupAllTempDirSubFolders();
        Globals.StartUpLOGGER.info("STOP");
    }

    private void cleanupOldLogFiles(int retainDays) {
        // TODO retainDays???
        try {
            Path logDir = Paths.get("logs");
            LOGGER.info("cleanup log directory: " + logDir.toAbsolutePath().toString());
            Predicate<String> jettyLogFilter1 = Pattern.compile("jetty\\.log\\.[0-9]+").asPredicate();
            Predicate<String> logIndexes1 = Pattern.compile("-indexed\\.tmp$").asPredicate();
            Predicate<Path> jettyLogFilter = p -> jettyLogFilter1.test(p.getFileName().toString());
            Predicate<Path> logIndexes = p -> logIndexes1.test(p.getFileName().toString());
            if (Files.exists(logDir)) {
                Files.list(logDir).filter(jettyLogFilter.or(logIndexes)).forEach(p -> {
                    try {
                        Files.deleteIfExists(p);
                    } catch (IOException e) {
                        LOGGER.warn("cleanup log files: " + e.toString());
                    }
                });
            } else {
                LOGGER.warn("Couldn't find the log directory: " + logDir.toAbsolutePath().toString());
            }
        } catch (Exception e) {
            LOGGER.warn("cleanup log files: " + e.toString());
        }
    }
    
    private void cleanupFolder(final Path folder) {
        try {
            Files.walk(folder).sorted(Comparator.reverseOrder()).forEach(f -> {
                try {
                    Files.deleteIfExists(f);
                } catch (DirectoryNotEmptyException e) {
                    //
                } catch (IOException e) {
                    LOGGER.warn("cleanup files: " + e.toString());
                }
            });
        } catch (IOException e) {
            LOGGER.warn("cleanup files: " + e.toString());
        }
    }
    
    private void cleanupAllTempDirSubFolders() {
        //if (System.getProperty("os.name").startsWith("Windows")) {
            try {
                Files.list(Paths.get(System.getProperty("java.io.tmpdir"))).filter(Files::isDirectory).forEach(this::cleanupFolder);
                LOGGER.info("cleanup temp. subfolders: " + System.getProperty("java.io.tmpdir"));
            } catch (IOException e) {
                LOGGER.warn("cleanup temp. subfolders: " + e.toString());
            }
        //}
    }

}
