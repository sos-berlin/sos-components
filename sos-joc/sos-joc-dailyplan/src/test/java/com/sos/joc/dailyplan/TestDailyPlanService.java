package com.sos.joc.dailyplan;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

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
import com.sos.joc.classes.order.OrdersHelper;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals.DefaultSections;
import com.sos.joc.cluster.configuration.globals.common.AConfigurationSection;
import com.sos.joc.cluster.service.active.IJocActiveMemberService;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.model.common.JocSecurityLevel;

public class TestDailyPlanService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestDailyPlanService.class);

    @BeforeClass
    public static void setup() {
        TimeZone.setDefault(TimeZone.getTimeZone(SOSDate.TIMEZONE_UTC));
    }

    @Ignore
    @Test
    public void testService() throws Exception {
        Path resDir = Paths.get("src/test/resources").toAbsolutePath();
        Path hibernateFile = resDir.resolve("hibernate").resolve("hibernate.cfg.pgsql.xml");

        Globals.sosCockpitProperties = new JocCockpitProperties();
        Globals.sosCockpitProperties.getProperties().put("hibernate_configuration_file", hibernateFile.toString());

        JocConfiguration jocConfig = new JocConfiguration(resDir.toString(), "UTC", hibernateFile, resDir, JocSecurityLevel.LOW, false, "title",
                "joc", 0, "joc#0", "2.x");

        DailyPlanService service = new DailyPlanService(jocConfig, new ThreadGroup(JocClusterConfiguration.IDENTIFIER));
        ConfigurationGlobals configurations = Globals.getConfigurationGlobals();
        AConfigurationSection configuration = configurations.getConfigurationSection(DefaultSections.dailyplan);

        service.start(StartupMode.manual_restart, getControllers(), configuration);
        TestDailyPlanService.stopAfter(service, StartupMode.manual_restart, 2 * 60);
    }

    @Ignore
    @Test
    public void testGenerateNewFromOldOrderId() {
        LOGGER.info(OrdersHelper.generateNewFromOldOrderId("#2021-10-12#C4038226057-00012-12-dailyplan_shedule_cyclic", ZoneId.systemDefault()));
        LOGGER.info(OrdersHelper.generateNewFromOldOrderId("#2021-10-12#C4038226057-00012-12-dailyplan_shedule_cyclic", "2021-10-25", ZoneId
                .systemDefault()));

        LOGGER.info(OrdersHelper.getNewFromOldOrderId("#2021-10-12#C4038226057-00001-12-dailyplan_shedule_cyclic", "XXX"));
        LOGGER.info(OrdersHelper.getNewFromOldOrderId("#2021-10-12#C4038226057-00002-12-dailyplan_shedule_cyclic", "XXX"));
    }

    private static void stopAfter(IJocActiveMemberService service, StartupMode mode, int seconds) {
        LOGGER.info(String.format("[start][stopAfter][%ss]...", seconds));

        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {

        } finally {
            service.stop(mode);
        }
        LOGGER.info(String.format("[end][stopAfter][%ss]", seconds));
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

}
