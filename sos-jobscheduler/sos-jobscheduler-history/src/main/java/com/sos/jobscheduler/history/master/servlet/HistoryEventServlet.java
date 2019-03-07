package com.sos.jobscheduler.history.master.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import com.sos.jobscheduler.history.helper.HistoryUtil;
import com.sos.jobscheduler.history.master.HistoryEventHandler;

public class HistoryEventServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventServlet.class);
    private HistoryEventHandler eventHandler;

    public HistoryEventServlet() {
        super();
    }

    public void init() throws ServletException {
        String method = "init";

        HistoryUtil.printSystemInfos();
        HistoryUtil.printJVMInfos();

        try {
            eventHandler = new HistoryEventHandler(getSettings());
            LOGGER.info(String.format("[%s]timezone=%s", method, eventHandler.getTimezone()));
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

    // TODO read from ConfigurationService
    private Properties readConfiguration(String baseDir) throws Exception {
        String method = "readConfiguration";

        String param = getInitParameter("history_configuration");
        Path path = param.contains("..") ? Paths.get(baseDir, param) : Paths.get(param);
        File file = path.toFile();
        LOGGER.info(String.format("[%s][history_configuration][%s]%s", method, path.toFile(), file.getCanonicalPath()));

        Properties conf = new Properties();
        try (FileInputStream in = new FileInputStream(file)) {
            conf.load(in);
        } catch (Exception ex) {
            throw new Exception(String.format("[%s][%s]error on read the history configuration: %s", method, file.getCanonicalPath(), ex.toString()),
                    ex);
        }
        LOGGER.info(String.format("[%s]%s", method, conf));
        return conf;
    }

    private Path getHibernateConfigurationFile(String baseDir, Properties conf) throws Exception {
        String param = conf.getProperty("hibernate_configuration").trim();
        return param.contains("..") ? Paths.get(baseDir, param) : Paths.get(param);
    }

    private EventHandlerSettings getSettings() throws Exception {
        String method = "getSettings";

        String baseDir = System.getProperty("jetty.base");
        LOGGER.info(String.format("[%s][jetty_base]%s", method, baseDir));

        Properties conf = readConfiguration(baseDir);

        EventHandlerSettings s = new EventHandlerSettings();
        s.setHibernateConfiguration(getHibernateConfigurationFile(baseDir, conf));
        s.setMailSmtpHost(conf.getProperty("mail_smtp_host").trim());
        s.setMailSmtpPort(conf.getProperty("mail_smtp_port").trim());
        s.setMailSmtpUser(conf.getProperty("mail_smtp_user").trim());
        s.setMailSmtpPassword(conf.getProperty("mail_smtp_password").trim());
        s.setMailFrom(conf.getProperty("mail_from").trim());
        s.setMailTo(conf.getProperty("mail_to").trim());

        EventHandlerMasterSettings ms = new EventHandlerMasterSettings(conf);

        LOGGER.info(String.format("[%s]%s", method, SOSString.toString(s)));
        LOGGER.info(String.format("[%s]%s", method, SOSString.toString(ms)));

        s.addMaster(ms);

        return s;
    }

}
