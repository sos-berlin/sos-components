package com.sos.webservices.order.initiator;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
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
        LOGGER.info("init");
        logThreadInfo();
        orderInitiateTimer = new Timer();
        orderInitiateTimer.schedule(new OrderInitiatorRunner(getSettings()), 1000, 30000);
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

    private OrderInitiatorSettings getSettings() {
        OrderInitiatorSettings ms = new OrderInitiatorSettings();

        String jettyBase = System.getProperty("jetty.base");
        String hibernateConfiguration = getInitParameter("hibernate_configuration");
        Path hc = null;
        if (hibernateConfiguration.contains("..")) {
            hc = Paths.get(jettyBase, hibernateConfiguration);
        } else {
            hc = Paths.get(hibernateConfiguration);
        }
        LOGGER.info("hibernate_configuration=" + hibernateConfiguration + "[" + hc.toAbsolutePath().normalize() + "]");
        return ms;
    }

}
