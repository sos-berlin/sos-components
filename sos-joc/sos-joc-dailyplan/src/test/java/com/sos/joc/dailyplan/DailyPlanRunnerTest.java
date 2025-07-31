package com.sos.joc.dailyplan;

import java.util.Collection;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.dailyplan.common.AbsoluteMainPeriod;
import com.sos.joc.dailyplan.common.DailyPlanSchedule;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;

public class DailyPlanRunnerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanRunnerTest.class);

    @BeforeClass
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone(SOSDate.TIMEZONE_UTC));
        Globals.sosCockpitProperties = new JocCockpitProperties();
    }

    @Ignore
    @Test
    public void testCalculateStartTimes() {

        String dailyPlanDate = "2025-06-16";

        try {
            DailyPlanSettings dps = new DailyPlanSettings();
            dps.setCaller(DailyPlanRunnerTest.class.getSimpleName());
            dps.setStartMode(StartupMode.webservice);// projection=automatic, schedule preview=webservice?
            dps.setTimeZone("Europe/Berlin");
            dps.setDailyPlanDate(SOSDate.getDate(dailyPlanDate));
            dps.setPeriodBegin("00:00:00");

            DailyPlanRunner r = new DailyPlanRunner(dps);
            Collection<DailyPlanSchedule> dailyPlanSchedules = r.getDailyPlanSchedules(null, false);

            DBItemDailyPlanSubmission submission = new DBItemDailyPlanSubmission();
            submission.setId(-1L);
            submission.setSubmissionForDate(dps.getDailyPlanDate());

            OrderListSynchronizer ols = r.calculateAbsoluteMainPeriods(dps.getStartMode(), "controllerId", dailyPlanSchedules, dailyPlanDate, submission);
            LOGGER.info("[OrderListSynchronizer]size=" + ols.getAbsoluteMainPeriods().size());
            for (AbsoluteMainPeriod p : ols.getAbsoluteMainPeriods()) {
                LOGGER.info("   " + p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
