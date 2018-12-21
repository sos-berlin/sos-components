package com.sos.webservices.order.initiator;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
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

    public OrderInitiatorServlet() {
        super();
    }

    public void init() throws ServletException {
        final String METHOD = "init";
        LOGGER.info(METHOD);
        logThreadInfo();
        orderInitiateTimer = new Timer();
        try {
            orderInitiateTimer.schedule(new OrderInitiatorRunner(getSettings()), 1000, 3000000);
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

        Path pathToConfigurationFile ;
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
            throw new Exception(String.format("[%s][%s]error on read the order configuration: %s", method, canonicalPathToConfigurationFile, ex.toString()), ex);
        }
        LOGGER.info(String.format("[%s]%s", method, conf));

        orderInitiatorSettings.setDayOffset(conf.getProperty("day_offset"));
        orderInitiatorSettings.setJocUrl(conf.getProperty("joc_url"));
        orderInitiatorSettings.setRunOnStart("true".equalsIgnoreCase(conf.getProperty("run_on_start", "true")));
        orderInitiatorSettings.setRunInterval(conf.getProperty("run_interval", "1440"));
        orderInitiatorSettings.setFirstRunAt(conf.getProperty("first_run_at","00:00:00"));
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
