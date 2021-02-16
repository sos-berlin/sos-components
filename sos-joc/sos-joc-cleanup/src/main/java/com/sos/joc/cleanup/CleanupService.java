package com.sos.joc.cleanup;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.JocClusterServices;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.db.DBLayer;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;

public class CleanupService extends AJocClusterService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupService.class);

    private static final String IDENTIFIER = JocClusterServices.cleanup.name();
    private static final int MAX_POOL_SIZE = 3;

    private JocClusterHibernateFactory factory;

    public CleanupService(JocConfiguration jocConf, ThreadGroup clusterThreadGroup) {
        super(jocConf, clusterThreadGroup, IDENTIFIER);
    }

    @Override
    public JocClusterAnswer start(List<ControllerConfiguration> controllers, StartupMode mode) {
        try {
            AJocClusterService.setLogger(IDENTIFIER);

            LOGGER.info(String.format("[%s][%s]start...", getIdentifier(), mode));
            createFactory(getJocConfig().getHibernateConfiguration());
            return JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
        } catch (Exception e) {
            return JocCluster.getErrorAnswer(e);
        } finally {
            AJocClusterService.clearLogger();
        }
    }

    @Override
    public JocClusterAnswer stop(StartupMode mode) {
        AJocClusterService.setLogger(IDENTIFIER);
        LOGGER.info(String.format("[%s][%s]stop...", getIdentifier(), mode));

        closeFactory();
        LOGGER.info(String.format("[%s][%s]stopped", getIdentifier(), mode));

        AJocClusterService.clearLogger();
        return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
    }

    private void createFactory(Path configFile) throws Exception {
        factory = new JocClusterHibernateFactory(configFile, 1, MAX_POOL_SIZE);
        factory.setIdentifier(IDENTIFIER);
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
    }

    private void closeFactory() {
        if (factory != null) {
            factory.close();
            factory = null;
        }
        LOGGER.info(String.format("[%s]database factory closed", getIdentifier()));
    }
}
