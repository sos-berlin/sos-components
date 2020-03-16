package com.sos.jobscheduler.history.master.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.configuration.Configuration;
import com.sos.jobscheduler.history.helper.HistoryUtil;
import com.sos.jobscheduler.history.master.HistoryMain;
import com.sos.jobscheduler.history.master.configuration.HistoryConfiguration;

public class HistoryEventServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventServlet.class);

    private static final String PROPERTIES_FILE_JOC = "/joc/joc.properties";
    private static final String PROPERTIES_FILE_HISTORY = "/joc/history.properties";
    private static final String LOG4J_FILE_HISTORY = "/joc/history.log4j2.xml";

    private HistoryMain history;
    private Path resourceDir;

    public HistoryEventServlet() {
        super();
    }

    public void init() throws ServletException {
        try {
            resourceDir = getResourceDir(PROPERTIES_FILE_JOC);
        } catch (Exception e) {
            throw new ServletException(e);
        }
        setLogger();
        LOGGER.info("[servlet][init]");
        LOGGER.info(String.format("[init][resourceDir]%s", resourceDir));

        doStart();
    }

    private void setLogger() {
        Path p = resourceDir.resolve(LOG4J_FILE_HISTORY).normalize();
        if (Files.exists(p)) {
            try {
                LoggerContext context = (LoggerContext) LogManager.getContext(false);
                context.setConfigLocation(p.toUri());
                context.updateLoggers();
            } catch (Exception e) {
                LOGGER.warn(e.toString(), e);
            }
        }
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
    private Properties readConfiguration(Path path) throws Exception {
        String method = "readConfiguration";

        LOGGER.info(String.format("[%s]%s", method, path));

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
            throw new Exception(String.format("[%s][%s]error on read the properties file%s: %s", method, path, addition, ex.toString()), ex);

        }
        LOGGER.info(String.format("[%s]%s", method, conf));
        return conf;
    }

    private Path getResourceDir(String propertiesFile) throws Exception {
        try {
            Path parentPath = Paths.get(propertiesFile).getParent();
            String parent = "/";
            if (parentPath != null && parentPath.getNameCount() != 0) {
                parent = parentPath.toString().replace('\\', '/');
            }
            URL url = this.getClass().getResource(parent);
            if (url != null) {
                Path p = Paths.get(url.toURI());
                if (Files.exists(p)) {
                    return p;
                }
            }
            throw new Exception("directory not found " + parent);
        } catch (Exception e) {
            throw new Exception("Cannot determine resource path: " + e.toString(), e);
        }
    }

    private Configuration getConfiguration() throws Exception {
        String method = "getConfiguration";

        Configuration config = new Configuration();

        try {
            Properties jocConf = readConfiguration(resourceDir.resolve(PROPERTIES_FILE_JOC).normalize());
            config.setHibernateConfiguration(resourceDir.resolve(jocConf.getProperty("hibernate_configuration_file").trim()).normalize());
        } catch (Exception e) {
            LOGGER.info(e.toString(), e);
        }
        Properties historyConf = readConfiguration(resourceDir.resolve(PROPERTIES_FILE_HISTORY).normalize());
        if (config.getHibernateConfiguration() == null) {
            config.setHibernateConfiguration(resourceDir.resolve(historyConf.getProperty("hibernate_configuration_file").trim()).normalize());
        }

        config.getMailer().load(historyConf);
        config.getHandler().load(historyConf);
        config.getHttpClient().load(historyConf);
        config.getWebservice().load(historyConf);

        HistoryConfiguration h = new HistoryConfiguration();
        h.load(historyConf);
        config.setApp(h);

        LOGGER.info(String.format("[%s]%s", method, SOSString.toString(config)));
        return config;
    }

}
