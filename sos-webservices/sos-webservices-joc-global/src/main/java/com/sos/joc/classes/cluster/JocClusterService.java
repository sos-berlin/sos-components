package com.sos.joc.classes.cluster;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventoryOperatingSystem;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.model.cluster.ClusterRestart;
import com.sos.joc.model.cluster.common.ClusterServices;

public class JocClusterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterService.class);

    private static final String THREAD_NAME_PREFIX = JocClusterConfiguration.IDENTIFIER;

    private static JocClusterService INSTANCE;

    private final JocConfiguration config;
    private final Date startTime;

    private ExecutorService threadPool;
    private JocClusterHibernateFactory factory;
    private JocCluster cluster;

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
                .getResourceDir(), Globals.getJocSecurityLevel().value(), Globals.sosCockpitProperties.getProperty("title"),
                Globals.sosCockpitProperties.getProperty("ordering", 0));
        startTime = new Date();
        AJocClusterService.clearLogger();
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

    public JocClusterAnswer start() {
        JocClusterAnswer answer = JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
        if (cluster == null) {
            JocClusterConfiguration clusterConfig = new JocClusterConfiguration(Globals.sosCockpitProperties.getProperties());
            threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(clusterConfig.getThreadGroup(), THREAD_NAME_PREFIX));
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    AJocClusterService.setLogger();
                    LOGGER.info("[start][run]...");
                    try {
                        createFactory(config.getHibernateConfiguration());

                        cluster = new JocCluster(factory, clusterConfig, config);
                        cluster.doProcessing(startTime);

                    } catch (Throwable e) {
                        LOGGER.error(e.toString(), e);
                    }
                    LOGGER.info("[start][end]");
                    AJocClusterService.clearLogger();
                }

            };
            threadPool.submit(task);
        } else {
            AJocClusterService.setLogger();
            LOGGER.info("[start][skip]already started");
            answer.setState(JocClusterAnswerState.ALREADY_STARTED);
            AJocClusterService.clearLogger();
        }
        return answer;
    }

    public JocClusterAnswer stop(boolean deleteActiveCurrentMember) {
        AJocClusterService.setLogger();
        JocClusterAnswer answer = JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
        if (cluster == null) {
            answer.setState(JocClusterAnswerState.ALREADY_STOPPED);
        } else {
            ThreadGroup tg = cluster.getConfig().getThreadGroup();
            closeCluster(deleteActiveCurrentMember);
            closeFactory();
            JocCluster.shutdownThreadPool(threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);

            ThreadHelper.tryStop(tg);
        }
        ThreadHelper.print("after stop " + JocClusterConfiguration.IDENTIFIER);
        AJocClusterService.clearLogger();
        return answer;
    }

    public JocClusterAnswer restart() {
        stop(false);
        JocClusterAnswer answer = start();
        if (answer.getState().equals(JocClusterAnswerState.STARTED)) {
            answer.setState(JocClusterAnswerState.RESTARTED);
        }
        return answer;
    }

    private void closeCluster(boolean deleteActiveCurrentMember) {
        if (cluster != null) {
            cluster.close(deleteActiveCurrentMember);
            cluster = null;
        }
    }

    public JocClusterAnswer restartService(ClusterRestart r) {
        if (cluster == null) {
            return JocCluster.getErrorAnswer(new Exception(String.format("cluster not started. restart %s can't be performed.", r.getType())));
        }
        if (!cluster.getHandler().isActive()) {
            return JocCluster.getErrorAnswer(new Exception(String.format("cluster inactiv. restart %s can't be performed.", r.getType())));
        }

        AJocClusterService.setLogger();
        JocClusterAnswer answer = null;
        switch (r.getType()) {
        case history:
            answer = cluster.getHandler().restartService(ClusterServices.history.name());
            break;
        case dailyplan:
            answer = cluster.getHandler().restartService(ClusterServices.dailyplan.name());
            break;
        default:
            answer = JocCluster.getErrorAnswer(new Exception(String.format("restart not yet supported for %s", r.getType())));
        }
        AJocClusterService.clearLogger();
        return answer;
    }

    public JocClusterAnswer switchMember(String memberId) {
        AJocClusterService.setLogger();
        if (cluster == null) {
            AJocClusterService.clearLogger();
            return JocCluster.getErrorAnswer(new Exception("cluster not running"));
        }
        JocClusterAnswer answer = cluster.switchMember(memberId);
        AJocClusterService.clearLogger();
        return answer;
    }

    private void createFactory(Path configFile) throws SOSHibernateConfigurationException, SOSHibernateFactoryBuildException {
        try {
            factory = new JocClusterHibernateFactory(configFile, 1, 1);
            factory.setIdentifier(JocClusterConfiguration.IDENTIFIER);
            factory.setAutoCommit(false);
            factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
            factory.addClassMapping(DBItemInventoryOperatingSystem.class);
            factory.addClassMapping(DBItemJocInstance.class);
            factory.addClassMapping(DBItemJocCluster.class);
            factory.addClassMapping(DBItemInventoryJSInstance.class);
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
