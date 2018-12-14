package com.sos.jobscheduler.history.master.servlet;

import java.io.FileInputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.handler.EventHandlerMasterSettings;
import com.sos.jobscheduler.event.master.handler.EventHandlerSettings;
import com.sos.jobscheduler.history.master.HistoryEventHandler;

public class HistoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryServlet.class);
    private HistoryEventHandler eventHandler;

    public HistoryServlet() {
        super();
    }

    public void init() throws ServletException {
        String method = "init";
        LOGGER.info(method);
        logThreadInfo();
        try {
            eventHandler = new HistoryEventHandler(getSettings());
        } catch (Exception ex) {
            LOGGER.error(String.format("[%s]%s", method, ex.toString()), ex);
            throw new ServletException(String.format("[%s]%s", method, ex.toString()), ex);
        }
        try {
            eventHandler.start();
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", e.toString()), e);
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
        eventHandler.exit();
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

    private EventHandlerSettings getSettings() throws Exception {
        // TODO read from ConfigurationService
        String method = "getSettings";

        String jettyBase = System.getProperty("jetty.base");

        LOGGER.info(String.format("[%s]START...", method));
        LOGGER.info(String.format("[%s][jetty_base]%s", method, jettyBase));

        String historyConfiguration = getInitParameter("history_configuration");
        Path hc = historyConfiguration.contains("..") ? Paths.get(jettyBase, historyConfiguration) : Paths.get(historyConfiguration);
        String cp = hc.toFile().getCanonicalPath();
        LOGGER.info(String.format("[%s][history_configuration][%s]%s", method, hc, cp));

        Properties conf = new Properties();
        try (FileInputStream in = new FileInputStream(cp)) {
            conf.load(in);
        } catch (Exception ex) {
            throw new Exception(String.format("[%s][%s]error on read the history configuration: %s", method, cp, ex.toString()), ex);
        }
        LOGGER.info(String.format("[%s]%s", method, conf));

        EventHandlerSettings s = null;
        try {
            EventHandlerMasterSettings ms = new EventHandlerMasterSettings(conf);

            String hibernateConfiguration = conf.getProperty("hibernate_configuration").trim();
            hc = hibernateConfiguration.contains("..") ? Paths.get(jettyBase, hibernateConfiguration) : Paths.get(hibernateConfiguration);
            s = new EventHandlerSettings();
            s.setHibernateConfiguration(hc);
            s.setMailSmtpHost(conf.getProperty("mail_smtp_host").trim());
            s.setMailSmtpPort(conf.getProperty("mail_smtp_port").trim());
            s.setMailSmtpUser(conf.getProperty("mail_smtp_user").trim());
            s.setMailSmtpPassword(conf.getProperty("mail_smtp_password").trim());
            s.setMailFrom(conf.getProperty("mail_from").trim());
            s.setMailTo(conf.getProperty("mail_to").trim());

            LOGGER.info(SOSString.toString(s));
            LOGGER.info(SOSString.toString(ms));

            s.addMaster(ms);
            LOGGER.info(String.format("[%s]END", method));
        } catch (Exception e) {
            LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
            throw new ServletException(String.format("[%s]%s", method, e.toString()), e);
        }

        return s;
    }

}
