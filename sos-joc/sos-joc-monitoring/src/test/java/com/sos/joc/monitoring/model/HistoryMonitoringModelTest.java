package com.sos.joc.monitoring.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HistoryMonitoringModelTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMonitoringModelTest.class);

    

    @Ignore
    @Test
    public void testRound() throws Exception {
        BigDecimal bg = new BigDecimal(1.1);
        LOGGER.info("long=" + bg.longValue() + ", bg=" + bg);
        bg = bg.setScale(0, RoundingMode.HALF_UP);
        LOGGER.info("long=" + bg.longValue() + ", bg=" + bg);

        LOGGER.info("----------------");

        bg = new BigDecimal(1.8);
        LOGGER.info("long=" + bg.longValue() + ", bg=" + bg);
        bg = bg.setScale(0, RoundingMode.HALF_UP);
        LOGGER.info("long=" + bg.longValue() + ", bg=" + bg);

    }

}
