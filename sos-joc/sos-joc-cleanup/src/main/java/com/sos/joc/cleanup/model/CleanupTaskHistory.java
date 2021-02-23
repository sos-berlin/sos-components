package com.sos.joc.cleanup.model;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.bean.answer.JocServiceAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;

public class CleanupTaskHistory extends CleanupTaskModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupTaskHistory.class);

    public CleanupTaskHistory(IJocClusterService service) {
        super(service);
    }

    @Override
    public void start() {
        super.start();

        LOGGER.info(String.format("[%s][mock-mode]ask history...", getLogIdentifier()));
        if (!isStopped()) {
            askHistory();
        }
        LOGGER.info(String.format("[%s][mock-mode]do something 2 minutes and ask history again...", getLogIdentifier()));
        try {
            waitFor(2 * 60);
        } catch (Throwable e) {
            LOGGER.warn(e.toString(), e);
        }
        if (!isStopped()) {
            askHistory();
            setState(JocServiceTaskAnswerState.COMPLETED);
        }
    }

    private void askHistory() {
        JocServiceAnswer info = getService().getInfo();
        LOGGER.info(String.format("[%s][mock-mode][history answer]%s", getLogIdentifier(), SOSString.toString(info)));
        if (info.getLastActivityStart() == 0) {
            LOGGER.info(String.format("[%s][mock-mode][history answer][lastActivityStart]not set", getLogIdentifier()));
        } else {
            try {
                LOGGER.info(String.format("[%s][mock-mode][history answer][lastActivityStart]%s", getLogIdentifier(), SOSDate.getDateTimeAsString(
                        new Date(info.getLastActivityStart()), SOSDate.dateTimeFormat)));
            } catch (Exception e) {
            }
        }
        if (info.getLastActivityEnd() == 0) {
            LOGGER.info(String.format("[%s][mock-mode][history answer][lastActivityEnd]not set", getLogIdentifier()));
        } else {
            try {
                LOGGER.info(String.format("[%s][mock-mode][history answer][lastActivityEnd]%s", getLogIdentifier(), SOSDate.getDateTimeAsString(
                        new Date(info.getLastActivityEnd()), SOSDate.dateTimeFormat)));
            } catch (Exception e) {
            }
        }
    }
}
