package com.sos.jobscheduler.history.master.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.List;
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

import com.sos.commons.util.SOSPath;
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
        Path userDir = Paths.get(System.getProperty("user.dir"));
        try {
            // TODO
            resourceDir = userDir.resolve("resources").normalize();
        } catch (Exception e) {
            throw new ServletException(e);
        }
        setLogger();

        LOGGER.info(String.format("[init][resourceDir]%s", resourceDir));
        // TMP
        if (Files.exists(userDir.resolve("webapps").resolve("cluster.war").normalize()) || Files.exists(userDir.resolve("webapps").resolve("cluster")
                .normalize())) {
            LOGGER.info("[init]waiting for cluster answer ...");
        } else {
            doStart();
        }
    }

    private void setLogger() {
        Path p = resourceDir.resolve(LOG4J_FILE_HISTORY).normalize();
        if (Files.exists(p)) {
            try {
                LoggerContext context = (LoggerContext) LogManager.getContext(false);
                context.setConfigLocation(p.toUri());
                context.updateLoggers();
                LOGGER.info(String.format("[setLogger]%s", p));
            } catch (Exception e) {
                LOGGER.warn(e.toString(), e);
            }
        } else {
            LOGGER.info("[setLogger]use default logger configuration");
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        LOGGER.info("[servlet][doPost]");
        // doGet(request, response);
        sendResponse(response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = "servlet][doGet";
        LOGGER.info(String.format("[%s]%s", method, request.getRequestURL().append('?').append(request.getQueryString())));
        try {
            if (!SOSString.isEmpty(request.getParameter("start"))) {
                doStart();
            } else if (!SOSString.isEmpty(request.getParameter("stop"))) {
                doTerminate();
            } else if (!SOSString.isEmpty(request.getParameter("restart"))) {
                doTerminate();
                doStart();
            } else {
                LOGGER.warn(String.format("[%s]unknown parameters=%s", method, request.getRequestURL().append('?').append(request.getQueryString())));
                Enumeration<String> paramaterNames = request.getParameterNames();
                while (paramaterNames.hasMoreElements()) {
                    LOGGER.warn(paramaterNames.nextElement());
                }
            }
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            sendResponse(response);
        }
    }

    private void sendResponse(HttpServletResponse response) {
        response.setContentType("text/plain; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out;
        try {
            out = response.getWriter();
            out.print("OK");
            out.flush();
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
    }

    public void destroy() {
        LOGGER.info("[servlet][destroy]");
        doTerminate();
    }

    private void doStart() throws ServletException {
        if (history == null) {
            threadPool = Executors.newFixedThreadPool(1);
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    LOGGER.info("[start history][run]...");
                    try {
                        tmpMoveLogFiles(getConfiguration());

                        history = new HistoryMain(getConfiguration());
                        history.start();

                    } catch (Throwable e) {
                        LOGGER.error(e.toString(), e);
                    }
                    LOGGER.info("[start history][end]");
                }

            };
            threadPool.submit(task);
        } else {
            LOGGER.info("[start history][skip]already started");
        }
    }

    private void tmpMoveLogFiles(Configuration conf) {// to be delete
        try {
            Path logDir = Paths.get(((HistoryConfiguration) conf.getApp()).getLogDir());
            List<Path> l = SOSPath.getFileList(logDir, "^[1-9]*[_]?[1-9]*\\.log$", 0);
            l.stream().forEach(p -> {
                Path dir = logDir.resolve(p.getFileName().toString().replace(".log", "").split("_")[0]);
                try {
                    if (!Files.exists(dir)) {
                        Files.createDirectory(dir);
                    }
                    Files.move(p, dir.resolve(p.getFileName()), StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    LOGGER.error(e.toString(), e);
                }
            });
            LOGGER.info(String.format("[tmpMoveLogFiles][moved]%s", l.size()));
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        }
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
        String method = "setHibernateConfiguration";
        Path hibernateConfFile = resourceDir.resolve(HIBERNATE_CONFIGURATION).normalize();
        if (Files.exists(hibernateConfFile)) {
            LOGGER.info(String.format("[%s]found hibernate configuration file %s", method, hibernateConfFile));
            config.setHibernateConfiguration(hibernateConfFile);
        } else {
            Path jocConfFile = resourceDir.resolve(PROPERTIES_FILE_JOC).normalize();
            if (Files.exists(jocConfFile)) {
                LOGGER.info(String.format("[%s]found joc configuration file %s", method, jocConfFile));
                try {
                    Properties jocConf = readConfiguration(jocConfFile);
                    config.setHibernateConfiguration(resourceDir.resolve(jocConf.getProperty("hibernate_configuration_file").trim()).normalize());
                    LOGGER.info(String.format("[%s]use hibernate configuration file %s from joc configuration", method, config
                            .getHibernateConfiguration()));
                } catch (Exception e) {
                    LOGGER.info(e.toString(), e);
                }
            }
        }
        if (config.getHibernateConfiguration() == null) {
            config.setHibernateConfiguration(resourceDir.resolve(historyConf.getProperty("hibernate_configuration_file").trim()).normalize());
            LOGGER.info(String.format("[%s]use hibernate configuration file %s from history configuration", method, config
                    .getHibernateConfiguration()));
        }
    }

    private Configuration getConfiguration() throws Exception {
        String method = "getConfiguration";

        Configuration config = new Configuration();
        Properties historyConf = readConfiguration(resourceDir.resolve(PROPERTIES_FILE_HISTORY).normalize());

        setHibernateConfiguration(config, historyConf);
        config.isPublic(historyConf.getProperty("is_public") == null ? false : Boolean.parseBoolean(historyConf.getProperty("is_public")));

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
