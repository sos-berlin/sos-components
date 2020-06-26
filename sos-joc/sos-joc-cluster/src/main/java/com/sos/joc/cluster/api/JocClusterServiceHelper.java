package com.sos.joc.cluster.api;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.exception.SOSHibernateConfigurationException;
import com.sos.commons.hibernate.exception.SOSHibernateFactoryBuildException;
import com.sos.commons.util.SOSShell;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.api.JocClusterMeta.HandlerIdentifier;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.api.bean.request.restart.JocClusterRestartRequest;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryInstance;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.db.os.DBItemOperatingSystem;

public class JocClusterServiceHelper {

    private static JocClusterServiceHelper jocClusterServiceHelper;
    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterServiceHelper.class);

    private static final String IDENTIFIER = "cluster";

    private final JocConfiguration config;
    private final List<Class<?>> handlers;
    private final Date startTime;

    private ExecutorService threadPool;
    private JocClusterHibernateFactory factory;
    private JocCluster cluster;
    
    private JocClusterServiceHelper() {
        config = new JocConfiguration(System.getProperty("user.dir"), TimeZone.getDefault().getID());
        startTime = new Date();
        handlers = new ArrayList<>();
    }
    
    public static synchronized JocClusterServiceHelper getInstance() {
        if (jocClusterServiceHelper == null) {
            jocClusterServiceHelper = new JocClusterServiceHelper(); 
        }
        return jocClusterServiceHelper;
    }
    
    public synchronized void addHandler(Class<?> clazz) {
        handlers.add(clazz);
    }
    
    public JocCluster getCluster() {
        return cluster;
    }


    public JocClusterAnswer doStartCluster() {
        JocClusterAnswer answer = JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
        if (cluster == null) {
            threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(IDENTIFIER));
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    LOGGER.info("[start][run]...");
                    try {
                        SOSShell.printSystemInfos();
                        SOSShell.printJVMInfos();

                        createFactory(config.getHibernateConfiguration());

                        cluster = new JocCluster(factory, new JocClusterConfiguration(config.getResourceDirectory()), config, handlers);
                        cluster.doProcessing(startTime);

                    } catch (Throwable e) {
                        LOGGER.error(e.toString(), e);
                    }
                    LOGGER.info("[start][end]");
                }

            };
            threadPool.submit(task);
        } else {
            LOGGER.info("[start][skip]already started");
            answer.setState(JocClusterAnswerState.ALREADY_STARTED);
        }
        return answer;
    }

    public JocClusterAnswer doStopCluster() {
        JocClusterAnswer answer = JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
        if (cluster != null) {
            closeCluster();
            closeFactory();
            JocCluster.shutdownThreadPool(threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
        } else {
            answer.setState(JocClusterAnswerState.ALREADY_STOPPED);
        }
        return answer;
    }

    public void closeCluster() {
        if (cluster != null) {
            cluster.close();
            cluster = null;
        }
    }

    public JocClusterAnswer restartCluster() {
        doStopCluster();
        JocClusterAnswer answer = doStartCluster();
        if (answer.getState().equals(JocClusterAnswerState.STARTED)) {
            answer.setState(JocClusterAnswerState.RESTARTED);
        }
        return answer;
    }

    public JocClusterAnswer restartHandler(JocClusterRestartRequest r) {
        if (cluster == null) {
            return JocCluster.getErrorAnswer(new Exception(String.format("cluster not started. restart %s can't be performed.", r.getType())));
        }
        if (!cluster.getHandler().isActive()) {
            return JocCluster.getErrorAnswer(new Exception(String.format("cluster inactiv. restart %s can't be performed.", r.getType())));
        }

        JocClusterAnswer answer = null;
        if (r.getType().equals(HandlerIdentifier.history)) {
            answer = cluster.getHandler().restartHandler(HandlerIdentifier.history.name());
        } else {
            answer = JocCluster.getErrorAnswer(new Exception(String.format("restart not yet supported for %s", r.getType())));
        }
        return answer;
    }

    public void createFactory(Path configFile) throws SOSHibernateConfigurationException, SOSHibernateFactoryBuildException {
        factory = new JocClusterHibernateFactory(configFile, 1, 1);
        factory.setIdentifier(IDENTIFIER);
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBItemOperatingSystem.class);
        factory.addClassMapping(DBItemJocInstance.class);
        factory.addClassMapping(DBItemJocCluster.class);
        factory.addClassMapping(DBItemInventoryInstance.class);
        factory.build();
    }

    public void closeFactory() {
        if (factory != null) {
            factory.close();
            factory = null;
            LOGGER.info(String.format("database factory closed"));
        } else {
            LOGGER.info(String.format("database factory already closed"));
        }

    }
}
