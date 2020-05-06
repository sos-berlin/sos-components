package com.sos.webservices.order.initiator;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.TimeZone;
import java.util.Timer;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OrderInitiatorServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(OrderInitiatorRunner.class);
    private Timer orderInitiateTimer;
    private OrderInitiatorSettings orderInitiatorSettings;

    public OrderInitiatorServlet() {
        super();
    }

    public void init() throws ServletException {
        final String METHOD = "init";
        LOGGER.info(METHOD);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        logThreadInfo();
        try {
            orderInitiatorSettings = getSettings();
            if (orderInitiatorSettings.isRunOnStart()) {
                OrderInitiatorRunner o = new OrderInitiatorRunner(orderInitiatorSettings);
                o.run();
            }

            resetStartPlannedOrderTimer();

        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", METHOD, e.toString()), e);
            throw new ServletException(String.format("[%s]%s", METHOD, e.toString()), e);
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("REQUEST....");
    }

    public void destroy() {
        LOGGER.info("destroy");
    }

    private void waitUntilFirstRun(){
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");
        Date firstRun = new Date();
        try {
            firstRun = formatter.parse(orderInitiatorSettings.getFirstRunAt());
        } catch (ParseException e1) {
            LOGGER.warn("Wrong format for start time " + orderInitiatorSettings.getFirstRunAt() + " Using default 00:00:00") ;
            try {
                firstRun = formatter.parse("00:00:00");
            } catch (ParseException e) {
                LOGGER.error("Could not parse date. Using now");
            }
        }
        Calendar first = Calendar.getInstance();
        first.setTime(firstRun);
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, first.get(Calendar.HOUR));
        cal.set(Calendar.MINUTE, first.get(Calendar.MINUTE));
        cal.set(Calendar.SECOND, first.get(Calendar.SECOND));
        firstRun = cal.getTime();
        Date now = new Date();
        Long wait = 0L;
        Long diff = now.getTime() - firstRun.getTime();
        if (now.before(firstRun)) {
            wait = diff;
        } else {
            wait = 24 * 60 * 1000 - diff;
        }
        LOGGER.debug("Waiting for " + wait / 1000 + " seconds until " + orderInitiatorSettings.getFirstRunAt() + "(UTC)");
        try {
            java.lang.Thread.sleep(diff);
        } catch (InterruptedException e) {
            LOGGER.warn("Wait time has been interrupted " + e.getCause());
        }
            
    }

    private void resetStartPlannedOrderTimer() {
        if (orderInitiateTimer != null) {
            orderInitiateTimer.cancel();
            orderInitiateTimer.purge();
        }
        waitUntilFirstRun();
        orderInitiateTimer = new Timer();
        LOGGER.debug("Plans will be created every " + orderInitiatorSettings.getRunInterval() + " s");
        orderInitiateTimer.schedule(new OrderInitiatorRunner(orderInitiatorSettings), 0, orderInitiatorSettings.getRunInterval()*1000);
    }

    private void logThreadInfo() {
        RuntimeMXBean runtimeBean = ManagementFactory.getRuntimeMXBean();
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();

        String jvmName = runtimeBean.getName();
        long pid = Long.valueOf(jvmName.split("@")[0]);
        int peakThreadCount = bean.getPeakThreadCount();

        LOGGER.info("JVM Name = " + jvmName);
        LOGGER.info("JVM PID  = " + pid);
        LOGGER.info("Peak Thread Count = " + peakThreadCount);
    }

    private OrderInitiatorSettings getSettings() throws Exception {
        String method = "getSettings";

        OrderInitiatorSettings orderInitiatorSettings = new OrderInitiatorSettings();

        String jettyBase = System.getProperty("jetty.base");
        String orderConfiguration = getInitParameter("order_configuration");

        Path pathToConfigurationFile;
        if (orderConfiguration.contains("..")) {
            pathToConfigurationFile = Paths.get(jettyBase, orderConfiguration);
        } else {
            pathToConfigurationFile = Paths.get(orderConfiguration);
        }

        String canonicalPathToConfigurationFile = pathToConfigurationFile.toFile().getCanonicalPath();
        LOGGER.info(String.format("[%s][order_configuration][%s]%s", method, pathToConfigurationFile, canonicalPathToConfigurationFile));

        Properties conf = new Properties();
        try (FileInputStream in = new FileInputStream(canonicalPathToConfigurationFile)) {
            conf.load(in);
        } catch (Exception ex) {
            throw new Exception(String.format("[%s][%s]error on read the order configuration: %s", method, canonicalPathToConfigurationFile, ex
                    .toString()), ex);
        }
        LOGGER.info(String.format("[%s]%s", method, conf));

        orderInitiatorSettings.setDayOffset(conf.getProperty("day_offset"));
        orderInitiatorSettings.setJobschedulerUrl(conf.getProperty("jobscheduler_url"));
        orderInitiatorSettings.setRunOnStart("true".equalsIgnoreCase(conf.getProperty("run_on_start", "true")));
        orderInitiatorSettings.setRunInterval(conf.getProperty("run_interval", "1440"));
        orderInitiatorSettings.setFirstRunAt(conf.getProperty("first_run_at", "00:00:00"));
        String hibernateConfiguration = conf.getProperty("hibernate_configuration").trim();
        Path hibernateConfigurationFileName;
        if (hibernateConfiguration.contains("..")) {
            hibernateConfigurationFileName = Paths.get(jettyBase, hibernateConfiguration);
        } else {
            hibernateConfigurationFileName = Paths.get(hibernateConfiguration);
        }

        orderInitiatorSettings.setHibernateConfigurationFile(hibernateConfigurationFileName);
        orderInitiatorSettings.setOrderTemplatesDirectory(conf.getProperty("order_templates_directory"));

        return orderInitiatorSettings;

    }

}
