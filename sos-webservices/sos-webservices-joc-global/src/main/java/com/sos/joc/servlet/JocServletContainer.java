package com.sos.joc.servlet;

import java.io.IOException;
import java.nio.file.DirectoryNotEmptyException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;

import org.glassfish.jersey.servlet.ServletContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSShell;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.classes.workflow.WorkflowPaths;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;

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
        cleanupOldLogFiles(0);

        Globals.sosCockpitProperties = new JocCockpitProperties();
        Proxies.startAll(Globals.sosCockpitProperties, ProxyUser.JOC);
        Globals.readUnmodifiables();
        Globals.setProperties();
        WorkflowPaths.init();
        CompletableFuture.runAsync(() -> {
            SOSShell.printSystemInfos();
            SOSShell.printJVMInfos();
        });

        JocClusterService.getInstance().start(StartupMode.automatic);

        try {
            cleanupOldDeployedFolders(false);
        } catch (Exception e) {
            LOGGER.warn("cleanup deployed files: ", e);
        }
    }

    @Override
    public void destroy() {
        LOGGER.debug("----> destroy on close JOC");
        super.destroy();

        // 1 - stop cluster
        JocClusterService.getInstance().stop(StartupMode.automatic, true);
        // 2 - close proxies
        Proxies.closeAll();

        if (Globals.sosHibernateFactory != null) {
            LOGGER.info("----> closing DB Connections");
            Globals.sosHibernateFactory.close();
        }

        if (Globals.sosSchedulerHibernateFactories != null) {

            for (SOSHibernateFactory factory : Globals.sosSchedulerHibernateFactories.values()) {
                if (factory != null) {
                    LOGGER.info("----> closing DB Connections");
                    factory.close();
                }
            }
        }

        try {
            cleanupOldDeployedFolders(true);
        } catch (Exception e) {
            LOGGER.warn("cleanup deployed files: " + e.toString());
        }
    }

    private Set<Path> getDeployedFolders() throws IOException {
        final Path deployParentDir = Paths.get(System.getProperty("java.io.tmpdir").toString());
        final Predicate<String> pattern = Pattern.compile("^jetty-\\d{1,3}(\\.\\d{1,3}){3}-\\d{1,5}-joc.war-_joc-.+\\.dir$").asPredicate();
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
            Path logDir = Paths.get(System.getProperty("jetty.base"), "logs");
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
                LOGGER.warn("cleanup log files: " + logDir.toString() + " not found");
            }
        } catch (Exception e) {
            LOGGER.warn("cleanup log files: " + e.toString());
        }
    }

}
