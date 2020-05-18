package com.sos.jobscheduler.history.master.servlet;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.event.master.configuration.Configuration;
import com.sos.jobscheduler.history.master.HistoryMain;
import com.sos.jobscheduler.history.master.configuration.HistoryConfiguration;
import com.sos.joc.cluster.servlet.JocClusterBaseServlet;

public class HistoryEventServlet extends JocClusterBaseServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryEventServlet.class);

    private static final String PROPERTIES_FILE = "joc/history.properties";
    private static final String LOG4J_FILE = "joc/history.log4j2.xml";

    private ExecutorService threadPool;
    private HistoryMain history;
    private Configuration conf;

    public HistoryEventServlet() {
        super();
    }

    public void init() throws ServletException {

        super.init(LOG4J_FILE);
        setConfiguration();

        // TMP
        if (Files.exists(getDataDirectory().resolve("webapps").resolve("cluster.war").normalize()) || Files.exists(getDataDirectory().resolve(
                "webapps").resolve("cluster").normalize())) {
            LOGGER.info("[init][not active]waiting for cluster activation ...");
        } else {
            doStart();
        }
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doPost(request, response);
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = "servlet][doGet";
        LOGGER.info(String.format("[%s]%s", method, request.getRequestURL().append('?').append(request.getQueryString())));
        try {
            if (isAllowed(request)) {
                if (!SOSString.isEmpty(request.getParameter("start"))) {
                    doStart();
                } else if (!SOSString.isEmpty(request.getParameter("stop"))) {
                    doTerminate();
                } else if (!SOSString.isEmpty(request.getParameter("restart"))) {
                    doTerminate();
                    doStart();
                } else {
                    throw new Exception(String.format("[%s]unknown parameters", method));
                }
            }
            sendOKResponse(response);
        } catch (Throwable e) {
            sendErrorResponse(request, response, e);
        }
    }

    public void destroy() {
        LOGGER.info("[servlet][destroy]");
        doTerminate();
    }

    private void doStart() throws ServletException {
        if (history == null) {
            try {
                threadPool = Executors.newFixedThreadPool(1);
                Runnable task = new Runnable() {

                    @Override
                    public void run() {
                        LOGGER.info("[start history][run]...");
                        try {
                            tmpMoveLogFiles(conf);

                            history = new HistoryMain(conf, getConfig());
                            history.start();

                        } catch (Throwable e) {
                            LOGGER.error(e.toString(), e);
                        }
                        LOGGER.info("[start history][end]");
                    }

                };
                threadPool.submit(task);
            } catch (Exception e) {
                throw new ServletException(e);
            }
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

    private void setConfiguration() throws ServletException {
        String method = "getConfiguration";

        Configuration config = new Configuration();
        try {
            Properties historyProperties = readConfiguration(getResourceDirectory().resolve(PROPERTIES_FILE).normalize());

            config.setHibernateConfiguration(getHibernateConfiguration());
            config.isPublic(historyProperties.getProperty("is_public") == null ? false : Boolean.parseBoolean(historyProperties.getProperty(
                    "is_public")));

            config.getMailer().load(historyProperties);
            config.getHandler().load(historyProperties);
            config.getHttpClient().load(historyProperties);
            config.getWebservice().load(historyProperties);

            HistoryConfiguration h = new HistoryConfiguration();
            h.load(historyProperties);
            config.setApp(h);

            conf = config;
            LOGGER.info(String.format("[%s]%s", method, SOSString.toString(conf)));
        } catch (Exception ex) {
            throw new ServletException();
        }
    }

}
