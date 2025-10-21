package com.sos.js7.converter.commons.workflow;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.js7.converter.commons.wokflow.JS7WorkflowTimesCalculator;

public class JS7WorkflowTimesCalculatorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(JS7WorkflowTimesCalculatorTest.class);

    @Ignore
    @Test
    public void test() {
        // First Monday of Month 18:00: 64.800
        // Second Monday of Month 18:00: 669.600
        LOGGER.info("First Monday of Month=" + JS7WorkflowTimesCalculator.getSecondOfWeeks(1, 1, "18:00"));
        LOGGER.info("Second Monday of Month=" + JS7WorkflowTimesCalculator.getSecondOfWeeks(2, 1, "18:00"));

        LOGGER.info("Seconds of specific date/time=" + JS7WorkflowTimesCalculator.getSecondsSinceLocalEpoch("2025-01-09", "18:00"));
        LOGGER.info("Seconds of specific date/time=" + JS7WorkflowTimesCalculator.getSecondsSinceLocalEpoch("2025-02-28", "18:00"));

        // Last Day of a Month 18:00: - 21.600
        // Last 1st Day of Month 18:00: - 108.000
        // Last 2st Day of Month 18:00: - 194.400
        LOGGER.info("Last Day of a Month=" + JS7WorkflowTimesCalculator.getLastSecondOfMonth(0, SOSDate.getTimeAsSeconds("18:00")));
        LOGGER.info("Last 1st Day of a Month=" + JS7WorkflowTimesCalculator.getLastSecondOfMonth(1, SOSDate.getTimeAsSeconds("18:00")));
        LOGGER.info("Last 2st Day of a Month=" + JS7WorkflowTimesCalculator.getLastSecondOfMonth(2, SOSDate.getTimeAsSeconds("18:00")));
    }

}
