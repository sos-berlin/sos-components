package com.sos.joc.dailyplan;

import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.dailyplan.common.DailyPlanSettings;

public class DailyPlanProjectionTest {

    @BeforeClass
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
    }

    @Ignore
    @Test
    public void test() throws Exception {
        Globals.sosCockpitProperties = new JocCockpitProperties();
        Globals.sosCockpitProperties.getProperties().put("hibernate_configuration_file", "../../../src/test/resources/hibernate.cfg.xml");
        try {
            DailyPlanSettings s = new DailyPlanSettings();
            s.setTimeZone("Europa/Berlin");

            DailyPlanProjection p = new DailyPlanProjection();
            p.process(s);

        } catch (Throwable e) {
            throw e;
        } finally {
            Globals.closeFactory();
        }
    }

}
