package com.sos.jobscheduler.history.master.servlet;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
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
        LOGGER.info("[servlet][init]");
        doStart();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("[servlet][doPost]");
        // doGet(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = "[servlet][doGet]";
        LOGGER.info(method);
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String name = parameterNames.nextElement();
            String value = request.getParameter(name);

            LOGGER.info(String.format("%s[param]%s=%s", method, name, value));

            switch (name) {

            case "terminate":
                doTerminate();
                return;

            case "start":
                doStart();
                return;

            case "restart":
                doTerminate();
                doStart();
                return;

            default:
                break;
            }
        }
    }

    public void destroy() {
        LOGGER.info("[servlet][destroy]");
        doTerminate();
    }

    private void doStart() throws ServletException {
        String method = "doStart";

        if (eventHandler == null) {
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
        } else {
            LOGGER.info(String.format("[%s]already started", method));
        }
    }

    private void doTerminate() {
        if (eventHandler == null) {
            LOGGER.info("[doTerminate]already terminated");
        } else {
            eventHandler.exit();
            eventHandler = null;
        }
    }

    private Path getPath(String baseDir, String param) {
        if (param.startsWith("/") || param.substring(1, 2).equals(":") || param.startsWith("\\\\")) {
            return Paths.get(param);
        }
        return Paths.get(baseDir, param);
    }

    // TODO read from ConfigurationService
    private Properties readConfiguration(String baseDir) throws Exception {
        String method = "readConfiguration";

        String param = getInitParameter("history_configuration");
        Path path = getPath(baseDir, param);
        File file = path.toFile();
        LOGGER.info(String.format("[%s][%s]%s", method, param, file.getCanonicalPath()));

        Properties conf = new Properties();
        try (FileInputStream in = new FileInputStream(file.getCanonicalPath())) {
            conf.load(in);
        } catch (Exception ex) {
            String addition = "";
            if (ex instanceof FileNotFoundException) {
                if (Files.exists(path) && !Files.isReadable(path)) {
                    addition = " (exists but not readable)";
                }
            }
            throw new Exception(String.format("[%s][%s]error on read the history configuration%s: %s", method, file.getCanonicalPath(), addition, ex
                    .toString()), ex);
        }
        LOGGER.info(String.format("[%s]%s", method, conf));
        return conf;
    }

    private EventHandlerSettings getSettings() throws Exception {
        String method = "getSettings";

        String baseDir = System.getProperty("jetty.base");
        LOGGER.info(String.format("[%s][jetty_base]%s", method, baseDir));

        Properties conf = readConfiguration(baseDir);

        EventHandlerSettings s = new EventHandlerSettings();

        s.setHibernateConfiguration(getPath(baseDir, conf.getProperty("hibernate_configuration").trim()));
        if (!SOSString.isEmpty(conf.getProperty("mail_smtp_host"))) {
            s.setMailSmtpHost(conf.getProperty("mail_smtp_host").trim());
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_smtp_port"))) {
            s.setMailSmtpPort(conf.getProperty("mail_smtp_port").trim());
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_smtp_user"))) {
            s.setMailSmtpUser(conf.getProperty("mail_smtp_user").trim());
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_smtp_password"))) {
            s.setMailSmtpPassword(conf.getProperty("mail_smtp_password").trim());
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_from"))) {
            s.setMailFrom(conf.getProperty("mail_from").trim());
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_to"))) {
            s.setMailTo(conf.getProperty("mail_to").trim());
        }

        EventHandlerMasterSettings ms = new EventHandlerMasterSettings(conf);

        LOGGER.info(String.format("[%s]%s", method, SOSString.toString(s)));
        LOGGER.info(String.format("[%s]%s", method, SOSString.toString(ms)));

        s.addMaster(ms);

        return s;
    }

}
