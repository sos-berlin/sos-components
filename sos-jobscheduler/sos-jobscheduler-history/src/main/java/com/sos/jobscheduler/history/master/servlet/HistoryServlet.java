package com.sos.jobscheduler.history.master.servlet;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.servlet.ServletContext;
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
        LOGGER.info("JobSchedulerHistoryServlet");
    }

    public void init() throws ServletException {
        LOGGER.info("init");
        ServletContext context = getServletContext();
        eventHandler = new HistoryEventHandler(getSettings(context));
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

    private EventHandlerSettings getSettings(ServletContext context) {
        String schedulerId = context.getInitParameter("scheduler_id");

        Path hibernate = Paths.get(context.getInitParameter("base_dir") + schedulerId + "/config").resolve("reporting.hibernate.cfg.xml");

        EventHandlerMasterSettings ms = new EventHandlerMasterSettings();
        ms.setSchedulerId(schedulerId);
        ms.setHttpHost(context.getInitParameter("host"));
        ms.setHttpPort(context.getInitParameter("port"));
        ms.setUser("test");
        ms.setPassword("12345");

        EventHandlerSettings s = new EventHandlerSettings();
        s.setHibernateConfiguration(hibernate);
        s.addMaster(ms);

        return s;
    }

}
