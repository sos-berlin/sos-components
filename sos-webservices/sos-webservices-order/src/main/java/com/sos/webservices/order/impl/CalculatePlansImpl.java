package com.sos.webservices.order.impl;

import java.io.FileInputStream;
import java.nio.file.Paths;
import java.time.Year;
import java.util.Date;
import java.util.Properties;
import javax.ws.rs.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.sos.joc.Globals;
import com.sos.joc.classes.JOCDefaultResponse;
import com.sos.joc.classes.JOCResourceImpl;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.model.plan.PlannedOrdersFilter;
import com.sos.webservices.order.initiator.OrderInitiatorRunner;
import com.sos.webservices.order.initiator.OrderInitiatorSettings;
import com.sos.webservices.order.resource.ICalculatePlansResource;

@Path("orders")
public class CalculatePlansImpl extends JOCResourceImpl implements ICalculatePlansResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(CalculatePlansImpl.class);
    private static final String API_CALL = "./orders/calculatePlans";

    private int getDayFrom(int year, int fromYear, int fromDayOfYear, int toYear) {
        int day = fromDayOfYear;
        if (year == toYear || (year > fromYear && year < toYear)) {
            day = 1;
        }
        return day;
    }

    private int getDayTo(int year, int fromYear, int toDayOfYear, int toYear) {
        Year thisYear = Year.of(fromYear);
        int countDaysInYear = thisYear.length();
        int day = toDayOfYear;
        if (year < toYear) {
            day = countDaysInYear;
        }
        return day;
    }

    @Override
    public JOCDefaultResponse postCalculatePlans(String xAccessToken, PlannedOrdersFilter plannedOrdersFilter) throws JocException {
        LOGGER.debug("Calculate the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, plannedOrdersFilter, xAccessToken, plannedOrdersFilter.getJobschedulerId(),
                    getPermissonsJocCockpit(plannedOrdersFilter.getJobschedulerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(getSettings(plannedOrdersFilter.getJobschedulerId()));
            orderInitiatorRunner.run();

            return JOCDefaultResponse.responseStatusJSOk(new Date());

        } catch (JocException e) {
            LOGGER.error(getJocError().getMessage(), e);
            e.addErrorMetaInfo(getJocError());
            return JOCDefaultResponse.responseStatusJSError(e);
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(getJocError().getMessage(), e);
            return JOCDefaultResponse.responseStatusJSError(e, getJocError());
        }

    }

    private OrderInitiatorSettings getSettings(String masterId) throws Exception {

        LOGGER.info("Test hallo welt2");
        OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();

        if (Globals.sosCockpitProperties == null) {
            throw new Exception("JOC configuration file is not set");
        }

        String s = Globals.sosCockpitProperties.getProperty("order_initiator_configuration_file");
        if (s == null) {
            throw new Exception("Cannot find: order_initiator_configuration_file in " + Globals.sosCockpitProperties.getPropertiesFile());
        }

        java.nio.file.Path orderConfigurationPath = Globals.sosCockpitProperties.resolvePath(s);

        orderInitiatorSettings.setPropertiesFile(orderConfigurationPath.toString());

        Properties conf = new Properties();

        try (FileInputStream in = new FileInputStream(orderConfigurationPath.toString())) {
            conf.load(in);
        } catch (Exception ex) {
            throw new Exception(String.format("[%s][%s]error on read the order initiator configuration", "getSettings", ex.toString()), ex);
        }

        if (conf.getProperty("order_templates_directory") == null) {
            throw new Exception("Cannot find: order_templates_directory in " + orderConfigurationPath.toString());
        }
        if (conf.getProperty("order_templates_directory") == null) {
            throw new Exception("Cannot find: order_templates_directory in " + orderConfigurationPath.toString());
        }
        if (conf.getProperty("jobscheduler_url_" + masterId) == null) {
            throw new Exception("Cannot find: jobscheduler_url_" + masterId + " in " + orderConfigurationPath.toString());
        }

        if (conf.getProperty("day_offset") == null) {
            orderInitiatorSettings.setDayOffset(1);
        } else {
            orderInitiatorSettings.setDayOffset(conf.getProperty("day_offset"));
        }
        orderInitiatorSettings.setJobschedulerUrl(conf.getProperty("jobscheduler_url" + "_" + masterId));
        String hibernateConfiguration = Globals.sosCockpitProperties.getProperty("hibernate_configuration_file");
        if (hibernateConfiguration != null) {
            hibernateConfiguration = hibernateConfiguration.trim();
        }

        orderInitiatorSettings.setHibernateConfigurationFile(Paths.get(hibernateConfiguration));
        orderInitiatorSettings.setOrderTemplatesDirectory(Globals.sosCockpitProperties.resolvePath(conf.getProperty("order_templates_directory"))
                .toString());

        return orderInitiatorSettings;
    }

    public int testGetDayFrom(int year, int fromYear, int fromDayOfYear, int toYear) {
        return getDayFrom(year, fromYear, fromDayOfYear, toYear);
    }

    public int testGetDayTo(int year, int fromYear, int toDayOfYear, int toYear) {
        return getDayTo(year, fromYear, toDayOfYear, toYear);
    }

}
