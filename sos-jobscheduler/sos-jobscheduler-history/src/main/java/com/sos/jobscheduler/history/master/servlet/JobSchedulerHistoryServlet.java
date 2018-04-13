package com.sos.jobscheduler.history.master.servlet;

import java.io.IOException;
import java.nio.file.Paths;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.event.master.EventHandlerMasterSettings;
import com.sos.jobscheduler.event.master.EventHandlerSettings;
import com.sos.jobscheduler.history.master.JobSchedulerHistoryEventHandler;

public class JobSchedulerHistoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(JobSchedulerHistoryServlet.class);
    private JobSchedulerHistoryEventHandler eventHandler;

    public JobSchedulerHistoryServlet() {
        super();
        LOGGER.info("JobSchedulerHistoryServlet");
    }

    public void init() throws ServletException {
        LOGGER.info("init");
        ServletContext context = getServletContext();
        eventHandler = new JobSchedulerHistoryEventHandler(getSettings(context));
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

        EventHandlerMasterSettings ms = new EventHandlerMasterSettings();
        ms.setSchedulerId(schedulerId);
        ms.setHost(context.getInitParameter("host"));
        ms.setHttpHost(context.getInitParameter("host"));
        ms.setHttpPort(context.getInitParameter("port"));
        ms.setConfigDirectory(Paths.get(context.getInitParameter("base_dir") + schedulerId + "/config"));
        ms.setLiveDirectory(ms.getConfigDirectory().resolve("live"));

        EventHandlerSettings s = new EventHandlerSettings();
        s.setHibernateConfiguration(ms.getConfigDirectory().resolve("reporting.hibernate.cfg.xml"));
        s.addMaster(ms);

        /** EventHandlerMasterSettings ms2 = new EventHandlerMasterSettings(); ms2.setSchedulerId(schedulerId + "XXXX"); ms2.setHost(host + "XXX");
         * ms2.setHttpHost(host + "XXX"); ms2.setHttpPort(port); ms2.setConfigDirectory(Paths.get(configDir));
         * ms2.setLiveDirectory(ms.getConfigDirectory().resolve("live")); s.addMaster(ms2); */
        return s;
    }

}
