package com.sos.joc.cluster.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.joc.cluster.api.bean.ClusterAnswer;
import com.sos.joc.cluster.api.bean.ClusterAnswer.ClusterAnswerType;
import com.sos.joc.cluster.configuration.JocConfiguration;

public abstract class JocClusterBaseServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterBaseServlet.class);

    private static final String PROPERTIES_FILE = "joc/joc.properties";
    private static final String HIBERNATE_CONFIGURATION = "joc/hibernate.cfg.xml";

    private JocConfiguration config;
    private Path dataDirectory;
    private Path resourceDirectory;
    private Path hibernateConfiguration;
    private final String timezone;

    public JocClusterBaseServlet() {
        super();
        timezone = TimeZone.getDefault().getID();
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));// TODO
    }

    public void init(String logConfigurationFile) throws ServletException {
        dataDirectory = Paths.get(System.getProperty("user.dir"));
        try {
            // TODO
            resourceDirectory = dataDirectory.resolve("resources").normalize();
        } catch (Exception e) {
            throw new ServletException(e);
        }
        setLogger(logConfigurationFile);
        LOGGER.info(String.format("[init][resourceDir]%s", resourceDirectory));

        try {
            config = new JocConfiguration(readConfiguration(resourceDirectory.resolve(PROPERTIES_FILE).normalize()));
        } catch (Exception e) {
            throw new ServletException(e);
        }

        try {
            hibernateConfiguration = getHibernateConfiguration();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void setLogger(String logConfigurationFile) {
        Path p = resourceDirectory.resolve(logConfigurationFile).normalize();
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
        sendErrorResponse(request, response, new Exception("POST method not allowed"));
    }

    public void sendOKResponse(HttpServletResponse response) {
        ClusterAnswer answer = new ClusterAnswer();
        answer.setType(ClusterAnswerType.SUCCESS);

        sendResponse(response, answer);
    }

    public void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, Throwable e) {
        LOGGER.error(String.format("[%s]%s", e.toString(), getRequestInfo(request)), e);
        Enumeration<String> paramaterNames = request.getParameterNames();
        while (paramaterNames.hasMoreElements()) {
            LOGGER.error(paramaterNames.nextElement());
        }

        ClusterAnswer answer = new ClusterAnswer();
        answer.setType(ClusterAnswerType.ERROR);
        answer.setMessage(e.toString());

        sendResponse(response, answer);
    }

    public void sendResponse(HttpServletResponse response, ClusterAnswer answer) {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out;
        try {
            out = response.getWriter();
            out.print((new ObjectMapper()).writeValueAsString(answer));
            out.flush();
        } catch (Exception ex) {
            LOGGER.error(ex.toString(), ex);
        }
    }

    public String getRequestInfo(HttpServletRequest request) {
        return request.getRequestURL().append("?").append(request.getQueryString()).toString();
    }

    public boolean isAllowed(HttpServletRequest request) {// TODO
        if ("localhost".equals(request.getServerName()) && config.getPort() == request.getServerPort()) { // request.getLocalPort()
            return true;
        }
        return false;
    }

    // TODO read from ConfigurationService
    public Properties readConfiguration(Path path) throws Exception {
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

    public Path getHibernateConfiguration() throws Exception {
        if (hibernateConfiguration == null) {
            hibernateConfiguration = resourceDirectory.resolve(HIBERNATE_CONFIGURATION).normalize();
            if (Files.exists(hibernateConfiguration)) {
                LOGGER.info(String.format("found hibernate configuration file %s", hibernateConfiguration));
            } else {
                hibernateConfiguration = resourceDirectory.resolve(config.getHibernateConfigurationFile()).normalize();
            }
        }
        return hibernateConfiguration;
    }

    public void shutdownThreadPool(String callerMethod, ExecutorService threadPool, long awaitTerminationTimeout) {
        try {
            if (threadPool == null) {
                return;
            }
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

    public JocConfiguration getConfig() {
        return config;
    }

    public String getTimezone() {
        return timezone;
    }

    public Path getDataDirectory() {
        return dataDirectory;
    }

    public Path getResourceDirectory() {
        return resourceDirectory;
    }
}
