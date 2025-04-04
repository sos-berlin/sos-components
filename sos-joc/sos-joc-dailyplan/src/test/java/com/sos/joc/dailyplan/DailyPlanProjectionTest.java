package com.sos.joc.dailyplan;

import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.commons.util.SOSDate;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.dailyplan.common.DailyPlanSettings;

public class DailyPlanProjectionTest {

    @BeforeClass
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone(SOSDate.TIMEZONE_UTC));
    }

    @Ignore
    @Test
    public void test() throws Exception {
        Globals.sosCockpitProperties = new JocCockpitProperties();
        Globals.sosCockpitProperties.getProperties().put("hibernate_configuration_file", "../../../src/test/resources/hibernate.cfg.xml");
        try {
            DailyPlanSettings s = new DailyPlanSettings();
            s.setTimeZone("Europe/Berlin");
            // currently supported (case insensitive):
            // 1) in months: <n> <- months, <n> months, <n> month, <n> m
            // or
            // 2) in years: <n> years, <n> year, <n> y

            // years will be converted to months...
            s.setProjectionsMonthAhead(6);

            DailyPlanProjections p = new DailyPlanProjections();
            p.process(s);

        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.closeFactory();
        }
    }

}
