package com.sos.webservices.order.rest.order.impl;

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
import com.sos.joc.model.plan.PlanFilter;
import com.sos.webservices.order.initiator.OrderInitiatorRunner;
import com.sos.webservices.order.initiator.OrderInitiatorSettings;
import com.sos.webservices.order.rest.order.resource.ICalculatePlansResource;

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
    public JOCDefaultResponse postCalculatePlans(String xAccessToken, PlanFilter planFilter) throws JocException {
        LOGGER.debug("Reading the daily plan");
        try {
            JOCDefaultResponse jocDefaultResponse = init(API_CALL, planFilter, xAccessToken, planFilter.getJobschedulerId(), getPermissonsJocCockpit(
                    planFilter.getJobschedulerId(), xAccessToken).getDailyPlan().getView().isStatus());

            if (jocDefaultResponse != null) {
                return jocDefaultResponse;
            }

            OrderInitiatorRunner orderInitiatorRunner = new OrderInitiatorRunner(getSettings(planFilter.getJobschedulerId()));
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

        OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();

        java.nio.file.Path orderConfigurationPath = Globals.sosCockpitProperties.resolvePath(Globals.sosCockpitProperties.getProperty("order_initiator_configuration_file"));
        
        orderInitiatorSettings.setPropertiesFile(orderConfigurationPath.toString());
     
        Properties conf = new Properties();

        try (FileInputStream in = new FileInputStream(orderConfigurationPath.toString())) {
            conf.load(in);
        } catch (Exception ex) {
            throw new Exception(String.format("[%s][%s]error on read the order initiator configuration: %s", "getSettings", orderConfigurationPath.toString(), ex.toString()), ex);
        }

        
        orderInitiatorSettings.setDayOffset(conf.getProperty("day_offset"));
        orderInitiatorSettings.setJobschedulerUrl(Globals.jocConfigurationProperties.getProperty("jobscheduler_url" + "_" + masterId));
        String hibernateConfiguration = Globals.sosCockpitProperties.getProperty("hibernate_configuration_file");
        if (hibernateConfiguration != null) {
            hibernateConfiguration = hibernateConfiguration.trim();
        }
 
        orderInitiatorSettings.setHibernateConfigurationFile(Paths.get(hibernateConfiguration));
        orderInitiatorSettings.setOrderTemplatesDirectory(Globals.sosCockpitProperties.resolvePath(conf.getProperty("order_templates_directory")).toString());

        return orderInitiatorSettings;
    }
 

    public int testGetDayFrom(int year, int fromYear, int fromDayOfYear, int toYear) {
        return getDayFrom(year, fromYear, fromDayOfYear, toYear);
    }

    public int testGetDayTo(int year, int fromYear, int toDayOfYear, int toYear) {
        return getDayTo(year, fromYear, toDayOfYear, toYear);
    }

}
