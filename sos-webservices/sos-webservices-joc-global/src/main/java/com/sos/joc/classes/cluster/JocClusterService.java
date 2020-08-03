package com.sos.joc.classes.cluster;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.hibernate.exception.SOSHibernateFactoryBuildException;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
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
        MDC.put("clusterService", ClusterServices.cluster.name());
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
        //MDC.remove("clusterService");
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
        MDC.put("clusterService", ClusterServices.cluster.name());
        JocClusterAnswer answer = JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
        if (cluster == null) {
            JocClusterConfiguration clusterConfig = new JocClusterConfiguration(config.getResourceDirectory());
            threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(clusterConfig.getThreadGroup(), THREAD_NAME_PREFIX));
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    MDC.put("clusterService", ClusterServices.cluster.name());
                    LOGGER.info("[start][run]...");
                    try {
                        createFactory(config.getHibernateConfiguration());

                        cluster = new JocCluster(factory, clusterConfig, config);
                        cluster.doProcessing(startTime);

                    } catch (Throwable e) {
                        LOGGER.error(e.toString(), e);
                    }
                    LOGGER.info("[start][end]");
                    MDC.remove("clusterService");
                }

            };
            threadPool.submit(task);
        } else {
            LOGGER.info("[start][skip]already started");
            answer.setState(JocClusterAnswerState.ALREADY_STARTED);
        }
        return answer;
    }

    public JocClusterAnswer stop(boolean deleteActiveCurrentMember) {
        MDC.put("clusterService", ClusterServices.cluster.name());
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
        return answer;
    }

    public JocClusterAnswer restart() {
        MDC.put("clusterService", ClusterServices.cluster.name());
        stop(false);
        JocClusterAnswer answer = start();
        if (answer.getState().equals(JocClusterAnswerState.STARTED)) {
            answer.setState(JocClusterAnswerState.RESTARTED);
        }
        return answer;
    }

    public void closeCluster(boolean deleteActiveCurrentMember) {
        MDC.put("clusterService", ClusterServices.cluster.name());
        if (cluster != null) {
            cluster.close(deleteActiveCurrentMember);
            cluster = null;
        }
    }

    public JocClusterAnswer restartService(ClusterRestart r) {
        MDC.put("clusterService", ClusterServices.cluster.name());
        if (cluster == null) {
            return JocCluster.getErrorAnswer(new Exception(String.format("cluster not started. restart %s can't be performed.", r.getType())));
        }
        if (!cluster.getHandler().isActive()) {
            return JocCluster.getErrorAnswer(new Exception(String.format("cluster inactiv. restart %s can't be performed.", r.getType())));
        }

        JocClusterAnswer answer = null;
        if (r.getType().equals(ClusterServices.history)) {
            answer = cluster.getHandler().restartService(ClusterServices.history.name());
        } else {
            answer = JocCluster.getErrorAnswer(new Exception(String.format("restart not yet supported for %s", r.getType())));
        }
        return answer;
    }

    public JocClusterAnswer switchMember(String memberId) {
        MDC.put("clusterService", ClusterServices.cluster.name());
        if (cluster == null) {
            return JocCluster.getErrorAnswer(new Exception("cluster not running"));
        }
        return cluster.switchMember(memberId);
    }

    public void createFactory(Path configFile) throws SOSHibernateConfigurationException, SOSHibernateFactoryBuildException {
        factory = new JocClusterHibernateFactory(configFile, 1, 1);
        factory.setIdentifier(JocClusterConfiguration.IDENTIFIER);
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBItemInventoryOperatingSystem.class);
        factory.addClassMapping(DBItemJocInstance.class);
        factory.addClassMapping(DBItemJocCluster.class);
        factory.addClassMapping(DBItemInventoryJSInstance.class);
        factory.build();
    }

    public void closeFactory() {
        if (factory != null) {
            factory.close();
            factory = null;
            LOGGER.info(String.format("[%s]database factory closed", JocClusterConfiguration.IDENTIFIER));
        } else {
            LOGGER.info(String.format("[%s]database factory already closed", JocClusterConfiguration.IDENTIFIER));
        }

    }
}
