package com.sos.joc.cluster.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Date;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TimeZone;
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

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.db.cluster.DBItemJocCluster;
import com.sos.jobscheduler.db.cluster.DBItemJocInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.instances.JocInstance;

public class JocClusterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterServlet.class);

    private static final String IDENTIFIER = "cluster";
    private static final String PROPERTIES_FILE_JOC = "joc/joc.properties";
    private static final String LOG4J_FILE = "joc/cluster.log4j2.xml";
    private static final String HIBERNATE_CONFIGURATION = "joc/hibernate.cfg.xml";

    private ExecutorService threadPool;
    private SOSHibernateFactory factory;
    private JocCluster cluster;
    private Path resourceDir;
    private final String appData;
    private final String timezone;
    private final Date startTime;

    public JocClusterServlet() {
        super();

        timezone = TimeZone.getDefault().getID();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));// TODO
        startTime = new Date();
        appData = System.getProperty("user.dir");
    }

    public void init() throws ServletException {
        try {
            // TODO
            resourceDir = Paths.get(appData, "resources").normalize();// getResourceDir(PROPERTIES_FILE_JOC);
        } catch (Exception e) {
            throw new ServletException(e);
        }
        setLogger();

        LOGGER.info(String.format("[init][resourceDir]%s", resourceDir));

        doStart();
    }

    private void setLogger() {
        Path p = resourceDir.resolve(LOG4J_FILE).normalize();
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

    public void destroy() {
        LOGGER.info("[servlet][destroy]");
        doTerminate();
    }

    private void doStart() {
        if (cluster == null) {
            threadPool = Executors.newFixedThreadPool(1);
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    LOGGER.info("[start][run]...");
                    try {
                        SOSShell.printSystemInfos();
                        SOSShell.printJVMInfos();

                        createFactory(getHibernateConfiguration());
                        JocInstance instance = new JocInstance(factory, appData, timezone, startTime);
                        instance.onStart();

                        cluster = new JocCluster(factory, instance.getMemberId());
                        cluster.doProcessing();

                    } catch (Throwable e) {
                        LOGGER.error(e.toString(), e);
                    }
                    LOGGER.info("[start][end]");
                }

            };
            threadPool.submit(task);
        } else {
            LOGGER.info("[start][skip]already started");
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

    private void doTerminate() {
        if (cluster != null) {
            cluster.close();
            cluster = null;
        }
        closeFactory();
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

    private Path getHibernateConfiguration() throws Exception {
        String method = "setHibernateConfiguration";
        Path confFile = resourceDir.resolve(HIBERNATE_CONFIGURATION).normalize();
        if (Files.exists(confFile)) {
            LOGGER.info(String.format("[%s]found hibernate configuration file %s", method, confFile));
        } else {
            Path jocConfFile = resourceDir.resolve(PROPERTIES_FILE_JOC).normalize();
            if (Files.exists(jocConfFile)) {
                LOGGER.info(String.format("[%s]found joc configuration file %s", method, jocConfFile));
                try {
                    Properties jocConf = readConfiguration(jocConfFile);
                    confFile = resourceDir.resolve(jocConf.getProperty("hibernate_configuration_file").trim()).normalize();
                    LOGGER.info(String.format("[%s]use hibernate configuration file %s from joc configuration", method, confFile));
                } catch (Exception e) {
                    LOGGER.info(e.toString(), e);
                }
            }
        }
        return confFile;
    }

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

    private void createFactory(Path configFile) throws Exception {
        factory = new SOSHibernateFactory(configFile);
        factory.setIdentifier(IDENTIFIER);
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBItemOperatingSystem.class);
        factory.addClassMapping(DBItemJocInstance.class);
        factory.addClassMapping(DBItemJocCluster.class);
        factory.build();
    }

    private void closeFactory() {
        if (factory != null) {
            factory.close();
            factory = null;
        }
        LOGGER.info(String.format("database factory closed"));
    }
}
