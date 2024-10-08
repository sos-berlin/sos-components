package com.sos.joc.classes.cluster;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.hibernate.exception.SOSHibernateFactoryBuildException;
import com.sos.commons.util.SOSString;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.proxy.ProxyUser;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.ThreadHelper;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration.Action;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobalsJoc;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.cluster.service.active.IJocActiveMemberService;
import com.sos.joc.cluster.service.embedded.IJocEmbeddedService;
import com.sos.joc.db.DBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.configuration.ConfigurationGlobalsChanged;
import com.sos.joc.event.bean.proxy.ProxyClosed;
import com.sos.joc.event.bean.proxy.ProxyEvent;
import com.sos.joc.event.bean.proxy.ProxyRestarted;
import com.sos.joc.event.bean.proxy.ProxyStarted;
import com.sos.joc.model.cluster.ClusterRestart;
import com.sos.joc.model.cluster.ClusterServiceRun;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.cluster.common.state.JocClusterState;

public class JocClusterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterService.class);

    private final static String THREAD_NAME_PREFIX = JocClusterConfiguration.IDENTIFIER;
    private final static long SUBMIT_SETTING_CHANGES = TimeUnit.MINUTES.toMillis(1);
    private static JocClusterService INSTANCE;

    private final JocConfiguration config;
    private final Date startTime;

    private ExecutorService threadPool;
    private JocClusterHibernateFactory factory;
    private JocCluster cluster;

    private Timer settingsChangedTimer;
    private AtomicReference<List<String>> settingsChanged = new AtomicReference<List<String>>();
    private final Object lockSettings = new Object();

    private JocClusterService() {
        JocClusterServiceLogger.setLogger();
        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties();
        }

        Path hibernateConfig = null;
        try {
            hibernateConfig = Globals.getHibernateConfFile();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }

        config = new JocConfiguration(System.getProperty("user.dir"), TimeZone.getDefault().getID(), hibernateConfig, Globals.sosCockpitProperties
                .getResourceDir(), Globals.getJocSecurityLevel(), Globals.isApiServer, Globals.sosCockpitProperties.getProperty("title"), Globals
                        .getClusterId(), Globals.getOrdering(), Globals.getJocId(), Globals.curVersion.getVersion());
        startTime = new Date();
        JocClusterServiceLogger.removeLogger();

        EventBus.getInstance().register(this);
    }

    public static synchronized JocClusterService getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JocClusterService();
        }
        return INSTANCE;
    }

    public JocCluster getCluster() {
        return cluster;
    }

    public JocClusterAnswer start(StartupMode mode, boolean onJocStart) {
        JocClusterAnswer answer = JocCluster.getOKAnswer(JocClusterState.STARTED);
        if (cluster == null) {
            JocClusterConfiguration clusterConfig = new JocClusterConfiguration(Globals.sosCockpitProperties.getProperties());
            config.setClusterMode(clusterConfig.getClusterModeResult().getUse());
            threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(clusterConfig.getThreadGroup(), THREAD_NAME_PREFIX));
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    JocClusterServiceLogger.setLogger();
                    LOGGER.info(String.format("[%s][start][run]...", mode));
                    try {
                        createFactory(config.getHibernateConfiguration());

                        cluster = new JocCluster(factory, clusterConfig, config, startTime);
                        Globals.setConfigurationGlobals(cluster.getConfigurationGlobals(mode));
                        cluster.startEmbeddedServices(mode);

                        if (Globals.getConfigurationGlobals() != null) {// null when closed during cluster.getConfigurationGlobals (empty database or db errors)
                            if (onJocStart) {
                                cluster.tryDeleteActiveCurrentMember();
                            }
                            cluster.doProcessing(mode, Globals.getConfigurationGlobals(), onJocStart);
                        }
                        LOGGER.info(String.format("[%s][start][end]", mode));
                    } catch (Throwable e) {
                        LOGGER.error(String.format("[%s][start][end]%s", mode, e.toString()), e);
                    }
                    JocClusterServiceLogger.removeLogger();
                }

            };
            threadPool.submit(task);
        } else {
            JocClusterServiceLogger.setLogger();
            LOGGER.info("[" + mode + "][start][skip]already started");
            answer.setState(JocClusterState.ALREADY_STARTED);
            JocClusterServiceLogger.removeLogger();
        }
        return answer;
    }

    public JocClusterAnswer stop(StartupMode mode, boolean deleteActiveCurrentMember, boolean resetCurrentInstanceHeartBeat) {
        JocClusterServiceLogger.setLogger();
        JocClusterAnswer answer = JocCluster.getOKAnswer(JocClusterState.STOPPED);
        if (cluster == null) {
            answer.setState(JocClusterState.ALREADY_STOPPED);
        } else {
            ThreadGroup tg = cluster.getConfig().getThreadGroup();
            stopSettingsChangedTimer();
            closeCluster(mode, deleteActiveCurrentMember, resetCurrentInstanceHeartBeat);
            closeFactory();
            JocCluster.shutdownThreadPool("[" + mode + "]", threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);

            ThreadHelper.tryStop(mode, tg);
        }
        ThreadHelper.print(mode, String.format("after stop %s", JocClusterConfiguration.IDENTIFIER));
        JocClusterServiceLogger.removeLogger();
        return answer;
    }

    public JocClusterAnswer restart(StartupMode mode) {
        stop(mode, false, false);
        JocClusterAnswer answer = start(mode, false);
        if (answer.getState().equals(JocClusterState.STARTED)) {
            answer.setState(JocClusterState.RESTARTED);
        }
        return answer;
    }

    public boolean isRunning() {
        if (cluster == null) {
            return false;
        }
        return cluster.getActiveMemberHandler().isActive();
    }

    /** not occur during JOC start/stop */
    /** TODO: will be sent for each user - JOC, HISTORY */
    @Subscribe({ ProxyStarted.class })
    public void handleControllerAdded(ProxyStarted evt) {
        if (!checkControllerEvent(evt)) {
            return;
        }
        JocClusterServiceLogger.setLogger();
        LOGGER.info(String.format("[ControllerAdded]%s", evt.getControllerId()));
        JocClusterServiceLogger.removeLogger();
        updateControllerInfos(StartupMode.controller_added, evt.getControllerId(), Action.ADDED);
    }

    /** not occur during JOC start/stop */
    @Subscribe({ ProxyRestarted.class })
    public void handleControllerUpdated(ProxyRestarted evt) {
        if (!checkControllerEvent(evt)) {
            return;
        }
        JocClusterServiceLogger.setLogger();
        LOGGER.info(String.format("[ControllerUpdated]%s", evt.getControllerId()));
        JocClusterServiceLogger.removeLogger();
        updateControllerInfos(StartupMode.controller_updated, evt.getControllerId(), Action.UPDATED);
    }

    /** not occur during JOC start/stop */
    @Subscribe({ ProxyClosed.class })
    public void handleControllerRemoved(ProxyClosed evt) {
        if (!checkControllerEvent(evt)) {
            return;
        }
        JocClusterServiceLogger.setLogger();
        LOGGER.info(String.format("[ControllerRemoved]%s", evt.getControllerId()));
        JocClusterServiceLogger.removeLogger();
        updateControllerInfos(StartupMode.controller_removed, evt.getControllerId(), Action.REMOVED);
    }

    private boolean checkControllerEvent(ProxyEvent evt) {
        return evt.getKey().equals(ProxyUser.HISTORY.name());
    }

    @Subscribe({ ConfigurationGlobalsChanged.class })
    public void respondConfigurationChanges(ConfigurationGlobalsChanged evt) {
        if (cluster == null) {
            return;
        }
        if (cluster.getActiveMemberHandler().isActive()) {
            handleGlobalsOnActiveMember(evt);
        } else {
            handleGlobalsOnNonActiveMember(evt);
        }
    }

    private void handleGlobalsOnActiveMember(ConfigurationGlobalsChanged evt) {
        JocClusterServiceLogger.setLogger();
        stopSettingsChangedTimer();

        // LOGGER.info("COUNT=" + evt.getSections() + "=" + evt.getSections().size());

        List<String> sections = settingsChanged.get();
        if (sections == null) {
            sections = new ArrayList<String>(evt.getSections());
        } else {
            sections.addAll(evt.getSections());
        }
        settingsChanged.set(sections);

        synchronized (lockSettings) {
            settingsChangedTimer = new Timer();
            settingsChangedTimer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    JocClusterServiceLogger.setLogger();
                    List<String> sections = settingsChanged.get();
                    if (sections != null && sections.size() > 0) {
                        sections = sections.stream().distinct().collect(Collectors.toList());

                        cluster.setConfigurationGlobalsChanged(new AtomicReference<List<String>>(sections));

                        // TODO restart asynchronous
                        // Restart Active JOC Cluster services
                        for (String identifier : sections) {
                            AConfigurationSection section = null;
                            try {
                                ClusterServices.valueOf(identifier);
                                section = Globals.getConfigurationGlobals().getConfigurationSection(DefaultSections.valueOf(identifier));
                            } catch (Throwable e) {
                                if (LOGGER.isDebugEnabled()) {
                                    JocClusterServiceLogger.setLogger();
                                    LOGGER.debug(String.format("[%s][%s][restartService][skip]is not a service", StartupMode.settings_changed.name(),
                                            identifier));
                                    JocClusterServiceLogger.removeLogger();
                                }
                            }
                            if (section != null) {
                                JocClusterServiceLogger.setLogger();
                                LOGGER.info(String.format("[%s][%s]restartService", StartupMode.settings_changed.name(), identifier));
                                JocClusterServiceLogger.removeLogger();
                                cluster.getActiveMemberHandler().restartService(StartupMode.settings_changed, identifier, section);
                            }
                        }

                        // Update embedded and active JOC Cluster services
                        if (sections.contains(DefaultSections.joc.name())) {
                            // 1) Embedded services
                            List<String> embeddedServices = new ArrayList<>();
                            if (cluster.getEmbeddedServicesHandler() != null) {
                                for (IJocEmbeddedService s : cluster.getEmbeddedServicesHandler().getServices()) {
                                    embeddedServices.add(s.getIdentifier());
                                }
                            }

                            // 2) Active JOC cluster services
                            List<ClusterServices> activeClusterServices = new ArrayList<>();
                            activeClusterServices.add(ClusterServices.history);
                            // activeClusterServices.add(ClusterServices.monitor); // instead, an embedded service is used
                            // updateServices.add(ClusterServices.lognotification);

                            // 3) 1) and 2)
                            List<String> allServicesToUpdate = new ArrayList<>();
                            allServicesToUpdate.addAll(embeddedServices);
                            allServicesToUpdate.addAll(activeClusterServices.stream().map(e -> e.name()).collect(Collectors.toList()));

                            String updateServicesNames = String.join(",", allServicesToUpdate);
                            AConfigurationSection joc = Globals.getConfigurationGlobals().getConfigurationSection(DefaultSections.joc);

                            StartupMode mode = StartupMode.settings_changed;
                            if (joc == null) {
                                LOGGER.info(String.format("[%s][joc][updateService][%s][skip]joc section is null", mode.name(), updateServicesNames));
                            } else {
                                JocClusterServiceLogger.setLogger();
                                LOGGER.info(String.format("[%s][joc][updateService][%s]forwarding of entries from the joc section", mode.name(),
                                        updateServicesNames));
                                JocClusterServiceLogger.removeLogger();

                                // Embedded services
                                if (embeddedServices.size() > 0) {
                                    for (IJocEmbeddedService s : cluster.getEmbeddedServicesHandler().getServices()) {
                                        s.update(mode, joc);
                                    }
                                }

                                // Active JOC cluster services
                                for (ClusterServices s : activeClusterServices) {
                                    cluster.getActiveMemberHandler().updateService(StartupMode.settings_changed, s, joc);
                                }
                            }
                        }
                    }
                    settingsChanged = new AtomicReference<List<String>>();
                }

            }, SUBMIT_SETTING_CHANGES, SUBMIT_SETTING_CHANGES);
        }
    }

    private void handleGlobalsOnNonActiveMember(ConfigurationGlobalsChanged evt) {
        StartupMode mode = StartupMode.settings_changed;
        Globals.setConfigurationGlobals(cluster.getConfigurationGlobals(mode));

        List<String> embeddedServices = new ArrayList<>();
        if (cluster.getEmbeddedServicesHandler() != null) {
            for (IJocEmbeddedService s : cluster.getEmbeddedServicesHandler().getServices()) {
                embeddedServices.add(s.getIdentifier());
            }
        }
        String updateServicesNames = String.join(",", embeddedServices);
        ConfigurationGlobalsJoc joc = Globals.getConfigurationGlobalsJoc();
        if (joc == null) {
            JocClusterServiceLogger.setLogger();
            LOGGER.info(String.format("[%s][joc][updateService][%s][skip]joc section is null", mode.name(), updateServicesNames));
            JocClusterServiceLogger.removeLogger();
        } else {
            // Embedded services
            if (embeddedServices.size() > 0) {
                JocClusterServiceLogger.setLogger();
                LOGGER.info(String.format("[%s][joc][updateService][%s]forwarding of entries from the joc section", mode.name(),
                        updateServicesNames));
                JocClusterServiceLogger.removeLogger();

                for (IJocEmbeddedService s : cluster.getEmbeddedServicesHandler().getServices()) {
                    s.update(mode, joc);
                }
            }
        }
    }

    private void stopSettingsChangedTimer() {
        if (settingsChangedTimer != null) {
            settingsChangedTimer.cancel();
            settingsChangedTimer.purge();
        }
    }

    private void closeCluster(StartupMode mode, boolean deleteActiveCurrentMember, boolean resetCurrentInstanceHeartBeat) {
        if (cluster != null) {
            cluster.close(mode, Globals.getConfigurationGlobals(), deleteActiveCurrentMember, resetCurrentInstanceHeartBeat);
            cluster = null;
        }
    }

    public void updateControllerInfos(StartupMode mode, String controllerId, Action action) {
        if (cluster == null || !cluster.getActiveMemberHandler().isActive()) {
            return;
        }
        cluster.getActiveMemberHandler().updateControllerInfos();
        cluster.getActiveMemberHandler().updateService(mode, ClusterServices.history.name(), controllerId, action);
    }

    public JocClusterAnswer runServiceNow(ClusterServiceRun r, StartupMode mode) {
        if (cluster == null) {
            return JocCluster.getErrorAnswer(new Exception(String.format("cluster not started. %s run %s can't be performed.", mode, r.getType())));
        }
        if (!cluster.getActiveMemberHandler().isActive()) {
            return JocCluster.getErrorAnswer(new Exception(String.format("cluster inactiv. %s run %s can't be performed.", mode, r.getType())));
        }

        JocClusterServiceLogger.setLogger();
        JocClusterAnswer answer = null;
        switch (r.getType()) {
        case cleanup:
            answer = cluster.getActiveMemberHandler().runServiceNow(mode, ClusterServices.cleanup.name(), Globals.getConfigurationGlobals()
                    .getConfigurationSection(DefaultSections.cleanup));
            break;
        case dailyplan:
            answer = cluster.getActiveMemberHandler().runServiceNow(mode, ClusterServices.dailyplan.name(), Globals.getConfigurationGlobals()
                    .getConfigurationSection(DefaultSections.dailyplan));
            break;
        default:
            answer = JocCluster.getErrorAnswer(new Exception(String.format("%s run not yet supported for %s", mode, r.getType())));
        }

        JocClusterServiceLogger.removeLogger();
        return answer;
    }

    public JocClusterAnswer restartService(ClusterRestart r, StartupMode mode) {
        if (cluster == null) {
            return JocCluster.getErrorAnswer(new Exception(String.format("cluster not started. %s restart %s can't be performed.", mode, r
                    .getType())));
        }
        if (!cluster.getActiveMemberHandler().isActive()) {
            return JocCluster.getErrorAnswer(new Exception(String.format("cluster inactiv. %s restart %s can't be performed.", mode, r.getType())));
        }

        JocClusterServiceLogger.setLogger();
        JocClusterAnswer answer = null;
        switch (r.getType()) {
        case history:
            answer = cluster.getActiveMemberHandler().restartService(mode, ClusterServices.history.name(), null);
            break;
        case dailyplan:
            answer = cluster.getActiveMemberHandler().restartService(mode, ClusterServices.dailyplan.name(), Globals.getConfigurationGlobals()
                    .getConfigurationSection(DefaultSections.dailyplan));
            break;
        case cleanup:
            answer = cluster.getActiveMemberHandler().restartService(mode, ClusterServices.cleanup.name(), Globals.getConfigurationGlobals()
                    .getConfigurationSection(DefaultSections.cleanup));
            break;
        case monitor:
            answer = cluster.getActiveMemberHandler().restartService(mode, ClusterServices.monitor.name(), null);
            break;
        case lognotification:
            answer = cluster.getActiveMemberHandler().restartService(mode, ClusterServices.lognotification.name(), Globals.getConfigurationGlobals()
                    .getConfigurationSection(DefaultSections.lognotification));
            break;
        default:
            answer = JocCluster.getErrorAnswer(new Exception(String.format("%s restart not yet supported for %s", mode, r.getType())));
        }
        JocClusterServiceLogger.removeLogger();
        return answer;
    }

    public synchronized void updateJocUri(StartupMode mode, String memberId, String uri) {
        if (cluster == null) {
            return;
        }
        if (SOSString.equals(config.getMemberId(), memberId)) {
            config.setUri(uri);
            updateServicesJocConfiguration(mode);
        }
    }

    private void updateServicesJocConfiguration(StartupMode mode) {
        if (cluster.getEmbeddedServicesHandler() != null) {
            for (IJocEmbeddedService s : cluster.getEmbeddedServicesHandler().getServices()) {
                s.update(mode, config);
            }
        }
        if (cluster.getActiveMemberHandler() != null && cluster.getActiveMemberHandler().isActive()) {
            for (IJocActiveMemberService s : cluster.getActiveMemberHandler().getServices()) {
                s.update(mode, config);
            }
        }
    }

    public JocClusterAnswer switchMember(StartupMode mode, String memberId) {
        JocClusterServiceLogger.setLogger();
        if (cluster == null) {
            JocClusterServiceLogger.removeLogger();
            return JocCluster.getErrorAnswer(new Exception("cluster not running"));
        }

        JocClusterAnswer answer = cluster.switchMember(mode, Globals.getConfigurationGlobals(), memberId);
        JocClusterServiceLogger.removeLogger();
        return answer;
    }

    private void createFactory(Path configFile) throws SOSHibernateConfigurationException, SOSHibernateFactoryBuildException {
        try {
            factory = new JocClusterHibernateFactory(configFile, 1, 1);
            factory.setIdentifier(JocClusterConfiguration.IDENTIFIER);
            factory.setAutoCommit(false);
            factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            factory.addClassMapping(DBLayer.getJocClusterClassMapping());
            factory.build();
        } catch (Throwable e) {
            throw e;
        }
    }

    private void closeFactory() {
        if (factory != null) {
            factory.close();
            factory = null;
            LOGGER.info(String.format("[%s]database factory closed", JocClusterConfiguration.IDENTIFIER));
        } else {
            LOGGER.info(String.format("[%s]database factory already closed", JocClusterConfiguration.IDENTIFIER));
        }
    }
}
