package com.sos.joc.cluster.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.db.cluster.DBItemJocCluster;
import com.sos.jobscheduler.db.joc.DBItemJocInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.jobscheduler.history.master.HistoryMain;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.api.bean.ClusterAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.handler.IClusterHandler;
import com.sos.joc.cluster.instances.JocInstance;

public class JocClusterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterServlet.class);

    private static final String IDENTIFIER = "cluster";
    private static final String LOG4J_FILE = "joc/cluster.log4j2.xml";

    private final JocConfiguration config;
    private final ObjectMapper jsonObjectMapper;
    private final Date startTime;

    private ExecutorService threadPool;
    private SOSHibernateFactory factory;
    private JocCluster cluster;

    public JocClusterServlet() {
        super();
        config = new JocConfiguration(System.getProperty("user.dir"), TimeZone.getDefault().getID());
        setLogger(LOG4J_FILE);
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));// TODO

        startTime = new Date();
        jsonObjectMapper = new ObjectMapper();
        jsonObjectMapper.setSerializationInclusion(Include.NON_NULL);
    }

    private void setLogger(String logConfigurationFile) {
        Path p = config.getResourceDirectory().resolve(logConfigurationFile).normalize();
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

    public void init() throws ServletException {
        doStart();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        sendErrorResponse(request, response, new Exception("POST method not allowed"));
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String method = "servlet][doGet";
        LOGGER.info(String.format("[%s]%s", method, request.getRequestURL().append('?').append(request.getQueryString())));
        ClusterAnswer answer = null;
        try {
            if (isAllowed(request)) {
                if (!SOSString.isEmpty(request.getParameter("start"))) {
                    doStart();
                } else if (!SOSString.isEmpty(request.getParameter("stop"))) {
                    doTerminate();
                } else if (!SOSString.isEmpty(request.getParameter("switch")) && !SOSString.isEmpty(request.getParameter("memberId"))) {
                    answer = doSwitch(request.getParameter("memberId"));
                } else if (!SOSString.isEmpty(request.getParameter("restart"))) {
                    doTerminate();
                    doStart();
                } else {
                    throw new Exception(String.format("[%s]unknown parameters", method));
                }
            }
            sendResponse(response, answer);
        } catch (Exception e) {
            sendErrorResponse(request, response, e);
        }
    }

    public void destroy() {
        LOGGER.info("[servlet][destroy]");
        doTerminate();
    }

    private void doStart() throws ServletException {

        if (cluster == null) {
            List<IClusterHandler> handlers = new ArrayList<>();
            handlers.add(new HistoryMain(config));

            threadPool = Executors.newFixedThreadPool(1);
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    LOGGER.info("[start][run]...");
                    try {
                        SOSShell.printSystemInfos();
                        SOSShell.printJVMInfos();

                        createFactory(config.getHibernateConfiguration());
                        JocInstance instance = new JocInstance(factory, config, startTime);
                        instance.onStart();
                        cluster = new JocCluster(factory, new JocClusterConfiguration(config.getResourceDirectory()), config, handlers);
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

    private void doTerminate() {
        closeCluster();
        closeFactory();
        JocCluster.shutdownThreadPool("doTerminate", threadPool, 3);
    }

    private void closeCluster() {
        if (cluster != null) {
            cluster.close();
            cluster = null;
        }
    }

    private ClusterAnswer doSwitch(String memberId) {
        ClusterAnswer answer = null;
        if (cluster != null) {
            answer = cluster.switchMember(memberId);
        } else {
            answer = JocCluster.getErrorAnswer(new Exception("cluster not running"));
        }
        return answer;
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception e) {
        LOGGER.error(String.format("[%s]%s", e.toString(), getRequestInfo(request)), e);
        Enumeration<String> paramaterNames = request.getParameterNames();
        while (paramaterNames.hasMoreElements()) {
            String name = paramaterNames.nextElement();
            LOGGER.error(name + "=" + request.getParameter(name));
        }
        sendResponse(response, JocCluster.getErrorAnswer(e));
    }

    private void sendResponse(HttpServletResponse response, ClusterAnswer answer) {
        if (answer == null) {
            answer = JocCluster.getOKAnswer();
        }

        LOGGER.info(SOSString.toString(answer));

        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out;
        try {
            out = response.getWriter();
            out.print(jsonObjectMapper.writeValueAsString(answer));
            out.flush();
        } catch (Exception ex) {
            LOGGER.error(ex.toString(), ex);
        }
    }

    private String getRequestInfo(HttpServletRequest request) {
        return request.getRequestURL().append("?").append(request.getQueryString()).toString();
    }

    private boolean isAllowed(HttpServletRequest request) {// TODO
        // if ("localhost".equals(request.getServerName())) { // request.getLocalPort()
        // return true;
        // }
        return true;
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
