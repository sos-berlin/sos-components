package com.sos.joc.cleanup.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;

public class CleanupTaskDailyplan extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskDailyplan.class);

    public CleanupTaskDailyplan(IJocClusterService service) {
        super(service);
    }

    @Override
    public void start() {
        super.start();

        LOGGER.info(String.format("[%s][mock-mode] start ...", getLogIdentifier()));
        waitFor(10);

        LOGGER.info(String.format("[%s][mock-mode] finished", getLogIdentifier()));

        setState(JocServiceTaskAnswerState.COMPLETED);
    }
}
