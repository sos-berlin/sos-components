package com.sos.joc.dailyplan;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.TimeZone;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.dailyplan.common.DailyPlanSettings;

// Test fails in nightly build

public class TestDailyPlanRunner {

    @BeforeClass
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));
    }

    @Test
    @Ignore
    public void testOrderInitatorGo() throws Exception {
        if (Globals.sosCockpitProperties == null) {
            Globals.sosCockpitProperties = new JocCockpitProperties("/dailyplan.properties");
        }

        DailyPlanRunner runner = new DailyPlanRunner(getSettings(), true);
        runner.run();
    }

    private DailyPlanSettings getSettings() throws Exception {
        DailyPlanSettings settings = new DailyPlanSettings();

        String jettyBase = System.getProperty("jetty.base");
        String orderConfiguration = "src/test/resources/dailyplan.properties";
        settings.setPropertiesFile("/dailyplan.properties");
        Path hc = null;
        if (orderConfiguration.contains("..")) {
            hc = Paths.get(jettyBase, orderConfiguration);
        } else {
            hc = Paths.get(orderConfiguration);
        }
        String cp = hc.toFile().getCanonicalPath();

        Properties conf = new Properties();

        try (FileInputStream in = new FileInputStream(cp)) {
            conf.load(in);
        } catch (Exception ex) {
            throw new Exception(String.format("[%s]error on read the history configuration: %s", cp, ex.toString()), ex);
        }

        settings.setDayAheadPlan(conf.getProperty("daily_plan_days_ahead_plan"));
        settings.setDayAheadSubmit(conf.getProperty("daily_plan_days_ahead_submit"));
        String hibernateConfiguration = conf.getProperty("hibernate_configuration_file");
        if (hibernateConfiguration != null) {
            hibernateConfiguration = hibernateConfiguration.trim();
            if (hibernateConfiguration.contains("..")) {
                hc = Paths.get(jettyBase, hibernateConfiguration);
            } else {
                hc = Paths.get(hibernateConfiguration);
            }

            settings.setHibernateConfigurationFile(hc);
            settings.setOrderTemplatesDirectory(conf.getProperty("order_templates_directory"));
        }

        return settings;
    }

}
