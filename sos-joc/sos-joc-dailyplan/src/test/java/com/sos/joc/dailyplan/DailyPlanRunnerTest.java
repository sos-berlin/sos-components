package com.sos.joc.dailyplan;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

import org.hibernate.query.Query;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSDate;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.dailyplan.common.AbsoluteMainPeriod;
import com.sos.joc.dailyplan.common.DailyPlanSchedule;
import com.sos.joc.dailyplan.common.DailyPlanSettings;
import com.sos.joc.dailyplan.db.DBLayerDailyPlanSubmissions;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.dailyplan.DBItemDailyPlanSubmission;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;

public class DailyPlanRunnerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DailyPlanRunnerTest.class);

    @BeforeClass
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone(SOSDate.TIMEZONE_UTC));

        Path resDir = Paths.get("src/test/resources").toAbsolutePath();
        Path hibernateFile = resDir.resolve("hibernate").resolve("hibernate.cfg.pgsql.xml");

        Globals.sosCockpitProperties = new JocCockpitProperties();
        Globals.sosCockpitProperties.getProperties().put("hibernate_configuration_file", hibernateFile.toString());
        Globals.sosCockpitProperties.getProperties().put("ordering", "0");
    }

    @Ignore
    @Test
    public void testCalculateStartTimes() {

        String dailyPlanDate = "2025-06-16";

        try {
            DailyPlanSettings dps = new DailyPlanSettings();
            dps.setCaller(DailyPlanRunnerTest.class.getSimpleName());
            dps.setStartMode(StartupMode.webservice);// projection=automatic, schedule preview=webservice
            dps.setTimeZone("Europe/Berlin");
            dps.setDailyPlanDate(SOSDate.getDate(dailyPlanDate));
            dps.setPeriodBegin("00:00:00");

            DailyPlanRunner r = new DailyPlanRunner(dps);
            Collection<DailyPlanSchedule> dailyPlanSchedules = DailyPlanRunner.getDailyPlanSchedules(null, false);

            DBItemDailyPlanSubmission submission = new DBItemDailyPlanSubmission();
            submission.setId(-1L);
            submission.setSubmissionForDate(dps.getDailyPlanDate());

            OrderListSynchronizer ols = r.calculateAbsoluteMainPeriodsOnlyWithoutIncludeLate(dps.getStartMode(), "controllerId", dailyPlanSchedules,
                    dailyPlanDate, submission);
            LOGGER.info("[OrderListSynchronizer]size=" + ols.getAbsoluteMainPeriods().size());
            for (AbsoluteMainPeriod p : ols.getAbsoluteMainPeriods()) {
                LOGGER.info("   " + p);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void testRunNow() {
        try {
            DailyPlanSettings dps = new DailyPlanSettings();
            dps.setCaller(DailyPlanRunnerTest.class.getSimpleName());
            dps.setControllers(getControllers());
            dps.setStartMode(StartupMode.run_now);
            dps.setTimeZone("Europe/Berlin");
            dps.setPeriodBegin("00:00:00");
            dps.setDaysAheadPlan(3);
            dps.setDaysAheadSubmit(0);

            DailyPlanRunner r = new DailyPlanRunner(dps);
            r.run();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            Proxies.closeAll();
        }
    }

    /** Sets a fixed time for the test environment using the TimeTransformer Java agent.
     * <p>
     * The agent manipulates the time returned by {@link System#currentTimeMillis()} and {@link System#nanoTime()}, effectively "freezing" the system time to
     * the specified date and time for the duration of the test.
     * </p>
     * <p>
     * <b>Important:</b> Most java.time APIs (e.g., {@link LocalDateTime#now()}, {@link ZonedDateTime#now()}, {@link LocalDate#now()}) ARE affected because they
     * internally use {@code System.currentTimeMillis()}.
     * </p>
     * <p>
     * <b>The ONLY exception:</b> {@link Instant#now()} is NOT affected because it uses a separate internal clock ({@link Clock#systemUTC()}).
     * </p>
     * 
     * <b>Setup:</b>
     * <ol>
     * <li>Download the JAR from: <a href="https://mvnrepository.com/artifact/com.topdesk/time-transformer-agent">Maven Repository</a></li>
     * <li>Add the JAR to the project dependencies</li>
     * <li>Add the following VM argument to the test run configuration:
     * 
     * <pre>
     * -javaagent:&lt;path-to-time-transformer-agent.jar&gt;
     * </pre>
     * 
     * </li>
     * <li>Use {@link #setTimeTransformer(String)} to set a fixed time</li>
     * <li>Use {@link #resetTimeTransformer()} to reset to real time</li>
     * </ol>
     * 
     * @see System#currentTimeMillis()
     * @see System#nanoTime()
     * @see Instant#now() */
    @Ignore
    @Test
    public void testRunNowDST() {
        // 2026 - winter time - 2026-10-25
        // setTimeTransformer("2026-10-23 05:30:00");

        // 2027 summer time - 2027-03-28
        String currentSystemDatetime = "2027-03-25 05:30:00";
        try {
            setTimeTransformer(currentSystemDatetime);

            DailyPlanSettings dps = new DailyPlanSettings();
            dps.setCaller(DailyPlanRunnerTest.class.getSimpleName());
            dps.setControllers(getControllers());
            dps.setStartMode(StartupMode.run_now);
            dps.setTimeZone("Europe/Berlin");
            dps.setPeriodBegin("06:00:00");
            // dps.setDaysAheadPlan(3);
            dps.setDaysAheadPlan(4);
            dps.setDaysAheadSubmit(0);

            deleteSubmissions(dps, currentSystemDatetime);

            DailyPlanRunner r = new DailyPlanRunner(dps);
            r.run();

            // cleanup
            // deleteSubmissions(dps, currentSystemDatetime);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            resetTimeTransformer();
            Proxies.closeAll();
        }
    }

    private List<ControllerConfiguration> getControllers() {
        List<ControllerConfiguration> list = new ArrayList<ControllerConfiguration>();
        SOSHibernateSession session = null;
        try {
            session = Globals.getHibernateFactory().openStatelessSession("getControllersId");
            Query<DBItemInventoryJSInstance> query = session.createQuery("from " + DBLayer.DBITEM_INV_JS_INSTANCES);

            List<DBItemInventoryJSInstance> result = session.getResultList(query);
            for (DBItemInventoryJSInstance item : result) {
                Properties p = new Properties();
                p.setProperty("controller_id", item.getControllerId());
                p.setProperty("primary_controller_uri", item.getUri());

                ControllerConfiguration c = new ControllerConfiguration();
                c.load(p);
                list.add(c);
            }
        } catch (Throwable e) {
            LOGGER.warn(String.format("[getControllersId]%s", e.toString()), e);
        } finally {
            Globals.getHibernateFactory().close(session);
        }
        return list;
    }

    private void deleteSubmissions(DailyPlanSettings dps, String currentSystemDatetime) throws Exception {
        Date dailyPlanDate = dps.getDailyPlanDate();
        if (dailyPlanDate == null) {
            // next day
            dailyPlanDate = SOSDate.add(SOSDate.getDate(currentSystemDatetime), 1, ChronoUnit.DAYS);
        }

        String dateFrom = SOSDate.tryGetDateTimeAsString(dailyPlanDate);
        String dateTo = SOSDate.tryGetDateTimeAsString(SOSDate.add(dailyPlanDate, dps.getDaysAheadPlan(), ChronoUnit.DAYS));

        DBLayerDailyPlanSubmissions dbLayer = null;
        try {
            SOSHibernateSession session = Globals.createSosHibernateStatelessConnection("deleteSubmissions");
            dbLayer = new DBLayerDailyPlanSubmissions(session);
            session.setAutoCommit(false);
            dbLayer.beginTransaction();
            for (ControllerConfiguration c : dps.getControllers()) {
                dbLayer.delete(dps.getStartMode(), "deleteSubmissions", c.getCurrent().getId(), null, dateFrom, dateTo);
            }
            dbLayer.commit();
            dbLayer.close();
            dbLayer = null;
        } catch (Exception e) {
            if (dbLayer != null) {
                dbLayer.rollback();
            }
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
    }

    /** see {@link #testRunNowDST()} */
    public static void setTimeTransformer(String isoDateTime) {
        try {
            // long time = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(isoDateTime).getTime();

            // com.topdesk.timetransformer.TimeTransformer.setTime(com.topdesk.timetransformer.TransformingTime.INSTANCE);
            // com.topdesk.timetransformer.TransformingTime.INSTANCE.apply(com.topdesk.timetransformer.TransformingTime.change().at(time).start());
        } catch (Exception e) {

        }
    }

    public static void resetTimeTransformer() {
        // com.topdesk.timetransformer.TimeTransformer.setTime(com.topdesk.timetransformer.DefaultTime.INSTANCE);
    }

}
