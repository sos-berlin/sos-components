package com.sos.jobscheduler.history.master.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
import com.sos.jobscheduler.event.master.handler.configuration.HandlerConfiguration;
import com.sos.jobscheduler.history.helper.HistoryUtil;
import com.sos.jobscheduler.history.master.HistoryMain;
import com.sos.jobscheduler.history.master.configuration.HistoryMasterConfiguration;

public class HistoryEventServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventServlet.class);
    private HistoryMain history;

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

        if (history == null) {
            HistoryUtil.printSystemInfos();
            HistoryUtil.printJVMInfos();

            try {
                history = new HistoryMain(getConfiguration());
                LOGGER.info(String.format("[%s]timezone=%s", method, history.getTimezone()));
            } catch (Exception ex) {
                LOGGER.error(String.format("[%s]%s", method, ex.toString()), ex);
                throw new ServletException(String.format("[%s]%s", method, ex.toString()), ex);
            }
            try {
                history.start();
            } catch (Exception e) {
                LOGGER.error(String.format("[%s]%s", method, e.toString()), e);
            }
        } else {
            LOGGER.info(String.format("[%s]already started", method));
        }
    }

    private void doTerminate() {
        if (history == null) {
            LOGGER.info("[doTerminate]already terminated");
        } else {
            history.exit();
            history = null;
        }
    }

    // TODO read from ConfigurationService
    private Properties readConfiguration(Path baseDir) throws Exception {
        String method = "readConfiguration";

        String param = getInitParameter("history_configuration");
        Path path = baseDir.resolve(param);
        LOGGER.info(String.format("[%s][%s]%s", method, param, path));

        Properties conf = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            conf.load(in);
        } catch (Exception ex) {
            String addition = "";
            if (ex instanceof FileNotFoundException) {
                if (Files.exists(path) && !Files.isReadable(path)) {
                    addition = " (exists but not readable)";
                }
            }
            throw new Exception(String.format("[%s][%s]error on read the history configuration%s: %s", method, path, addition, ex.toString()), ex);
        }
        LOGGER.info(String.format("[%s]%s", method, conf));
        return conf;
    }

    private HandlerConfiguration getConfiguration() throws Exception {
        String method = "getConfiguration";

        Path baseDir = Paths.get(System.getProperty("jetty.base"));
        LOGGER.info(String.format("[%s][jetty_base]%s", method, baseDir));

        Properties conf = readConfiguration(baseDir);

        HandlerConfiguration hc = new HandlerConfiguration();

        hc.setHibernateConfiguration(baseDir.resolve(("hibernate_configuration").trim()));
        if (!SOSString.isEmpty(conf.getProperty("mail_smtp_host"))) {
            hc.setMailSmtpHost(conf.getProperty("mail_smtp_host").trim());
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_smtp_port"))) {
            hc.setMailSmtpPort(conf.getProperty("mail_smtp_port").trim());
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_smtp_user"))) {
            hc.setMailSmtpUser(conf.getProperty("mail_smtp_user").trim());
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_smtp_password"))) {
            hc.setMailSmtpPassword(conf.getProperty("mail_smtp_password").trim());
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_from"))) {
            hc.setMailFrom(conf.getProperty("mail_from").trim());
        }
        if (!SOSString.isEmpty(conf.getProperty("mail_to"))) {
            hc.setMailTo(conf.getProperty("mail_to").trim());
        }

        HistoryMasterConfiguration mc = new HistoryMasterConfiguration(conf);

        LOGGER.info(String.format("[%s]%s", method, SOSString.toString(hc)));
        LOGGER.info(String.format("[%s]%s", method, SOSString.toString(mc)));

        hc.addMaster(mc);

        return hc;
    }

}
