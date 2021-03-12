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
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.ThreadHelper;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.db.DBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.configuration.ConfigurationGlobalsChanged;
import com.sos.joc.model.cluster.ClusterRestart;
import com.sos.joc.model.cluster.common.ClusterServices;

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
        AJocClusterService.setLogger();
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
                .getResourceDir(), Globals.getJocSecurityLevel(), Globals.sosCockpitProperties.getProperty("title"), Globals.sosCockpitProperties
                        .getProperty("ordering", 0));
        startTime = new Date();
        AJocClusterService.clearLogger();

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

    public JocClusterAnswer start(StartupMode mode) {
        JocClusterAnswer answer = JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
        if (cluster == null) {
            JocClusterConfiguration clusterConfig = new JocClusterConfiguration(Globals.sosCockpitProperties.getProperties());
            threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(clusterConfig.getThreadGroup(), THREAD_NAME_PREFIX));
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    AJocClusterService.setLogger();
                    LOGGER.info("[" + mode + "][start][run]...");
                    try {
                        createFactory(config.getHibernateConfiguration());

                        cluster = new JocCluster(factory, clusterConfig, config, startTime);
                        Globals.configurationGlobals = cluster.getConfigurationGlobals();
                        cluster.doProcessing(mode, Globals.configurationGlobals);

                    } catch (Throwable e) {
                        LOGGER.error(e.toString(), e);
                    }
                    LOGGER.info("[" + mode + "][start][end]");
                    AJocClusterService.clearLogger();
                }

            };
            threadPool.submit(task);
        } else {
            AJocClusterService.setLogger();
            LOGGER.info("[" + mode + "][start][skip]already started");
            answer.setState(JocClusterAnswerState.ALREADY_STARTED);
            AJocClusterService.clearLogger();
        }
        return answer;
    }

    public JocClusterAnswer stop(StartupMode mode, boolean deleteActiveCurrentMember) {
        AJocClusterService.setLogger();
        JocClusterAnswer answer = JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
        if (cluster == null) {
            answer.setState(JocClusterAnswerState.ALREADY_STOPPED);
        } else {
            ThreadGroup tg = cluster.getConfig().getThreadGroup();
            stopSettingsChangedTimer();
            closeCluster(mode, deleteActiveCurrentMember);
            closeFactory();
            JocCluster.shutdownThreadPool(mode, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);

            ThreadHelper.tryStop(mode, tg);
        }
        ThreadHelper.print(mode, String.format("after stop %s", JocClusterConfiguration.IDENTIFIER));
        AJocClusterService.clearLogger();
        return answer;
    }

    public JocClusterAnswer restart(StartupMode mode) {
        stop(mode, false);
        JocClusterAnswer answer = start(mode);
        if (answer.getState().equals(JocClusterAnswerState.STARTED)) {
            answer.setState(JocClusterAnswerState.RESTARTED);
        }
        return answer;
    }

    @Subscribe({ ConfigurationGlobalsChanged.class })
    public void respondConfigurationChanges(ConfigurationGlobalsChanged evt) {
        if (cluster == null || !cluster.getHandler().isActive()) {
            return;
        }
        AJocClusterService.setLogger(JocClusterConfiguration.IDENTIFIER);
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
                    AJocClusterService.setLogger(JocClusterConfiguration.IDENTIFIER);
                    List<String> sections = settingsChanged.get();
                    if (sections != null && sections.size() > 0) {
                        sections = sections.stream().distinct().collect(Collectors.toList());
                        AJocClusterService.setLogger(JocClusterConfiguration.IDENTIFIER);
                        LOGGER.info(String.format("[%s]restart %s services", StartupMode.settings_changed.name(), sections.size()));
                        AJocClusterService.clearLogger();
                        // TODO restart asynchronous
                        for (String identifier : sections) {
                            AConfigurationSection section = null;
                            try {
                                ClusterServices.valueOf(identifier);
                                section = Globals.configurationGlobals.getConfigurationSection(DefaultSections.valueOf(identifier));
                            } catch (Throwable e) {
                                AJocClusterService.setLogger(JocClusterConfiguration.IDENTIFIER);
                                LOGGER.info(String.format("[%s][%s][skip]is not a service", StartupMode.settings_changed.name(), identifier));
                                AJocClusterService.clearLogger();
                            }
                            if (section != null) {
                                AJocClusterService.setLogger(JocClusterConfiguration.IDENTIFIER);
                                LOGGER.info(String.format("[%s][%s]restart", StartupMode.settings_changed.name(), identifier));
                                AJocClusterService.clearLogger();
                                cluster.getHandler().restartService(identifier, StartupMode.settings_changed, section);
                            }
                        }
                    }
                    settingsChanged = new AtomicReference<List<String>>();
                }

            }, SUBMIT_SETTING_CHANGES, SUBMIT_SETTING_CHANGES);
        }
    }

    private void stopSettingsChangedTimer() {
        if (settingsChangedTimer != null) {
            settingsChangedTimer.cancel();
            settingsChangedTimer.purge();
        }
    }

    private void closeCluster(StartupMode mode, boolean deleteActiveCurrentMember) {
        if (cluster != null) {
            cluster.close(mode, Globals.configurationGlobals, deleteActiveCurrentMember);
            cluster = null;
        }
    }

    public JocClusterAnswer restartService(ClusterRestart r, StartupMode mode) {
        if (cluster == null) {
            return JocCluster.getErrorAnswer(new Exception(String.format("cluster not started. %s restart %s can't be performed.", mode, r
                    .getType())));
        }
        if (!cluster.getHandler().isActive()) {
            return JocCluster.getErrorAnswer(new Exception(String.format("cluster inactiv. %s restart %s can't be performed.", mode, r.getType())));
        }

        AJocClusterService.setLogger();
        JocClusterAnswer answer = null;
        switch (r.getType()) {
        case history:
            answer = cluster.getHandler().restartService(ClusterServices.history.name(), mode, null);
            break;
        case dailyplan:
            answer = cluster.getHandler().restartService(ClusterServices.dailyplan.name(), mode, Globals.configurationGlobals.getConfigurationSection(
                    DefaultSections.dailyplan));
            break;
        case cleanup:
            answer = cluster.getHandler().restartService(ClusterServices.cleanup.name(), mode, Globals.configurationGlobals.getConfigurationSection(
                    DefaultSections.cleanup));
            break;
        default:
            answer = JocCluster.getErrorAnswer(new Exception(String.format("%s restart not yet supported for %s", mode, r.getType())));
        }
        AJocClusterService.clearLogger();
        return answer;
    }

    public JocClusterAnswer switchMember(StartupMode mode, String memberId) {
        AJocClusterService.setLogger();
        if (cluster == null) {
            AJocClusterService.clearLogger();
            return JocCluster.getErrorAnswer(new Exception("cluster not running"));
        }
        JocClusterAnswer answer = cluster.switchMember(mode, Globals.configurationGlobals, memberId);
        AJocClusterService.clearLogger();
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
