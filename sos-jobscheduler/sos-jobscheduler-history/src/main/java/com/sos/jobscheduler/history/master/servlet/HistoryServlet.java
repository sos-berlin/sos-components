package com.sos.jobscheduler.history.master.servlet;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        LOGGER.info("init");
        eventHandler = new HistoryEventHandler(getSettings());
        try {
            eventHandler.start();
        } catch (Exception e) {
            LOGGER.error("init:" + e.toString(), e);
            System.out.println("init error" + e.toString());
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

    private EventHandlerSettings getSettings() {
        // TODO read from ConfigurationService
        EventHandlerMasterSettings ms = new EventHandlerMasterSettings();
        ms.setSchedulerId(getInitParameter("scheduler_id"));
        ms.setHttpHost(getInitParameter("scheduler_host"));
        ms.setHttpPort(getInitParameter("scheduler_port"));
        ms.useLogin(Boolean.parseBoolean(getInitParameter("use_master_login")));
        ms.setUser(getInitParameter("scheduler_master_user"));
        ms.setPassword(getInitParameter("scheduler_master_user_password"));

        String jettyBase = System.getProperty("jetty.base");
        String hibernateConfiguration = getInitParameter("hibernate_configuration");
        Path hc = hibernateConfiguration.contains("..") ? Paths.get(jettyBase, hibernateConfiguration) : Paths.get(hibernateConfiguration);
        LOGGER.info("schedulerId=" + ms.getSchedulerId());
        LOGGER.info("schedulerHost=" + ms.getHttpHost());
        LOGGER.info("schedulerPort=" + ms.getHttpPort());
        LOGGER.info("useMasterLogin=" + ms.useLogin());
        LOGGER.info("schedulerMasterUser=" + ms.getUser());
        LOGGER.info("schedulerMasterUserPassword=" + ms.getPassword());
        LOGGER.info("hibernateConfiguration=" + hibernateConfiguration + "[" + hc.toAbsolutePath() + "]");

        EventHandlerSettings s = new EventHandlerSettings();
        s.setHibernateConfiguration(hc);
        s.addMaster(ms);
        return s;
    }

}
