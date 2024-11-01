package com.sos.joc.servlet;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSShell;
import com.sos.joc.Globals;
import com.sos.joc.classes.DBMoveIamConfiguration;
import com.sos.joc.classes.DependencyUpdate;
import com.sos.joc.classes.JocCertificate;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.agent.AgentClusterWatch;
import com.sos.joc.classes.agent.AgentStoreUtils;
import com.sos.joc.classes.calendar.DailyPlanCalendar;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.classes.documentation.JitlDocumentation;
import com.sos.joc.classes.order.OrderTags;
import com.sos.joc.classes.proxy.ClusterWatch;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.classes.quicksearch.QuickSearchStore;
import com.sos.joc.classes.reporting.AReporting;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.classes.workflow.WorkflowRefs;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.db.DbInstaller;
import com.sos.joc.db.cluster.CheckInstance;
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
        LOGGER.debug("----> init on starting JOC");
        super.init();
        
        Globals.setSystemProperties();
        Globals.sosCockpitProperties = new JocCockpitProperties();
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
            CheckInstance.stopJOC();
            throw new ServletException(e);
        }
        
        JocCertificate.updateCertificate();
      //  DBMoveIamConfiguration.execute();

        ClusterWatch.getInstance();
        DailyPlanCalendar.getInstance();
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
                WorkflowPaths.init();
                WorkflowRefs.init();
                JitlDocumentation.saveOrUpdate();
                SOSShell.printSystemInfos();
                SOSShell.printJVMInfos();
                AReporting.deleteTmpFolder();
            }, "servlet-init2").start();
            
            JocClusterService.getInstance().start(StartupMode.automatic, true);
            DependencyUpdate.getInstance().updateThreaded();
            try {
                cleanupOldDeployedFolders(false);
            } catch (Exception e) {
                LOGGER.warn("cleanup deployed files: ", e.toString());
            }
        }, "servlet-init").start();
        
        
    }

    @Override
    public void destroy() {
        LOGGER.debug("----> destroy on close JOC");
        super.destroy();

        NotificationAppender.doNotify = false;
        QuickSearchStore.close();
        AgentClusterWatch.close();

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

        try {
            cleanupOldDeployedFolders(true);
        } catch (Exception e) {
            LOGGER.warn("cleanup deployed files: " + e.toString());
        }
    }

    private Set<Path> getDeployedFolders() throws IOException {
        final Path deployParentDir = Paths.get(System.getProperty("java.io.tmpdir").toString());
        final Predicate<String> pattern = Pattern.compile("^jetty-\\d{1,3}([._]\\d{1,3}){3}-\\d{1,5}-joc([._]war)?-.+-any-\\d+$").asPredicate();
        return Files.list(deployParentDir).filter(p -> pattern.test(p.getFileName().toString())).collect(Collectors.toSet());
    }

    private Optional<Path> getCurrentDeployedFolder(Set<Path> deployedFolders) throws IOException {
        if (deployedFolders != null && deployedFolders.size() > 1) {
            return deployedFolders.stream().max((i, j) -> {
                try {
                    return Files.getLastModifiedTime(i).compareTo(Files.getLastModifiedTime(j));
                } catch (IOException e) {
                    return 0;
                }
            });
        } else if (deployedFolders.size() == 1) {
            return Optional.of(deployedFolders.iterator().next());
        }
        throw new IOException("cleanup deployed files: couldn't determine current deploy folder");
    }

    private void cleanupOldDeployedFolders(final Set<Path> oldDeployedFolders) {
        oldDeployedFolders.stream().forEach(folder -> {
            try {
                Files.walk(folder).sorted(Comparator.reverseOrder()).forEach(f -> {
                    try {
                        Files.deleteIfExists(f);
                    } catch (DirectoryNotEmptyException e) {
                        //
                    } catch (IOException e) {
                        LOGGER.warn("cleanup deployed files: " + e.toString());
                    }
                });
            } catch (IOException e) {
                LOGGER.warn("cleanup deployed files: " + e.toString());
            }
        });
    }

    private void cleanupOldDeployedFolders(boolean withCurrentFolder) throws IOException {
        if (System.getProperty("os.name").toString().startsWith("Windows")) {
            Set<Path> deployedFolders = getDeployedFolders();
            final Optional<Path> currentDeployedFolder = getCurrentDeployedFolder(deployedFolders);
            if (currentDeployedFolder.isPresent() && deployedFolders.remove(currentDeployedFolder.get())) {
                cleanupOldDeployedFolders(deployedFolders);
            }
        }
    }

    private void cleanupOldLogFiles(int retainDays) {
        // TODO retainDays???
        try {
            String jettyBase = System.getProperty("jetty.base");
            if (jettyBase != null) {
                Path logDir = Paths.get(jettyBase, "logs");
                LOGGER.info("cleanup log files: " + logDir.toString());
                Predicate<Path> jettyLogFilter = p -> Pattern.compile("jetty\\.log\\.[0-9]+").asPredicate().test(p.getFileName().toString());
                if (Files.exists(logDir)) {
                    Files.list(logDir).filter(jettyLogFilter).forEach(p -> {
                        try {
                            Files.deleteIfExists(p);
                        } catch (IOException e) {
                            LOGGER.warn("cleanup log files: " + e.toString());
                        }
                    });
                } else {
                    LOGGER.warn("Couldn't find the cleanup log files: " + logDir.toString());
                }
            }
        } catch (Exception e) {
            LOGGER.warn("cleanup log files: " + e.toString());
        }
    }

}
