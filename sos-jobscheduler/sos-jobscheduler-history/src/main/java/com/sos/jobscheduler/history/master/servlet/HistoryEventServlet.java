package com.sos.jobscheduler.history.master.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.configuration.Configuration;
import com.sos.jobscheduler.history.master.HistoryMain;
import com.sos.jobscheduler.history.master.configuration.HistoryConfiguration;

public class HistoryEventServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventServlet.class);

    private static final String PROPERTIES_FILE_JOC = "joc/joc.properties";
    private static final String PROPERTIES_FILE_HISTORY = "joc/history.properties";
    private static final String LOG4J_FILE_HISTORY = "joc/history.log4j2.xml";
    private static final String HIBERNATE_CONFIGURATION = "joc/hibernate.cfg.xml";

    private HistoryMain history;
    private Path resourceDir;

    private ExecutorService threadPool;

    public HistoryEventServlet() {
        super();
    }

    public void init() throws ServletException {
        try {
            // TODO
            resourceDir = Paths.get(System.getProperty("user.dir"), "resources").normalize();// getResourceDir(PROPERTIES_FILE_JOC);
        } catch (Exception e) {
            throw new ServletException(e);
        }
        setLogger();

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
                LOGGER.info("use logger configuration " + p);
            } catch (Exception e) {
                LOGGER.warn(e.toString(), e);
            }
        } else {
            LOGGER.info("use default logger configuration");
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

        threadPool = Executors.newFixedThreadPool(1);
        Runnable task = new Runnable() {

            @Override
            public void run() {
                String name = Thread.currentThread().getName();
                LOGGER.info(String.format("[start][run][thread]%s", name));
                try {
                    if (history == null) {
                        SOSShell.printSystemInfos();
                        SOSShell.printJVMInfos();

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

                } catch (Throwable e) {
                    LOGGER.error(String.format("[run][thread][%s]%s", name, e.toString()), e);
                }
                LOGGER.info(String.format("[start][end][thread]%s", name));
            }

        };
        threadPool.submit(task);
    }

    private void doTerminate() {
        if (history == null) {
            LOGGER.info("[doTerminate]already terminated");
        } else {
            history.exit();
            history = null;
        }
        shutdownThreadPool("[doTerminate]", threadPool, 3);
    }

    private void shutdownThreadPool(String callerMethod, ExecutorService threadPool, long awaitTerminationTimeout) {
        try {
            threadPool.shutdown();
            // threadPool.shutdownNow();
            boolean shutdown = threadPool.awaitTermination(awaitTerminationTimeout, TimeUnit.SECONDS);
            if (shutdown) {
                LOGGER.info(String.format("%sthread has been shut down correctly", callerMethod));
            } else {
                LOGGER.info(String.format("%sthread has ended due to timeout of %ss on shutdown", callerMethod, awaitTerminationTimeout));
            }
        } catch (InterruptedException e) {
            LOGGER.error(String.format("%s[exception]%s", callerMethod, e.toString()), e);
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

    private void setHibernateConfiguration(Configuration config, Properties historyConf) throws Exception {
        Path hibernateConfFile = resourceDir.resolve(HIBERNATE_CONFIGURATION).normalize();
        if (Files.exists(hibernateConfFile)) {
            LOGGER.info(String.format("found hibernate configuration file %s", hibernateConfFile));
            config.setHibernateConfiguration(hibernateConfFile);
        } else {
            Path jocConfFile = resourceDir.resolve(PROPERTIES_FILE_JOC).normalize();
            if (Files.exists(jocConfFile)) {
                LOGGER.info(String.format("found joc configuration file %s", jocConfFile));
                try {
                    Properties jocConf = readConfiguration(jocConfFile);
                    config.setHibernateConfiguration(resourceDir.resolve(jocConf.getProperty("hibernate_configuration_file").trim()).normalize());
                    LOGGER.info(String.format("use hibernate configuration file %s from joc configuration", config.getHibernateConfiguration()));
                } catch (Exception e) {
                    LOGGER.info(e.toString(), e);
                }
            }
        }
        if (config.getHibernateConfiguration() == null) {
            config.setHibernateConfiguration(resourceDir.resolve(historyConf.getProperty("hibernate_configuration_file").trim()).normalize());
            LOGGER.info(String.format("use hibernate configuration file %s from history configuration", config.getHibernateConfiguration()));
        }
    }

    private Configuration getConfiguration() throws Exception {
        String method = "getConfiguration";

        Configuration config = new Configuration();
        Properties historyConf = readConfiguration(resourceDir.resolve(PROPERTIES_FILE_HISTORY).normalize());

        setHibernateConfiguration(config, historyConf);

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
