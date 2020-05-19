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
import com.sos.jobscheduler.db.cluster.DBItemJocInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.jobscheduler.history.master.HistoryMain;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.api.bean.ClusterAnswer;
import com.sos.joc.cluster.api.bean.ClusterAnswer.ClusterAnswerType;
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
    private List<IClusterHandler> handlers = new ArrayList<>();

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
        try {
            if (isAllowed(request)) {
                if (!SOSString.isEmpty(request.getParameter("start"))) {
                    doStart();
                } else if (!SOSString.isEmpty(request.getParameter("stop"))) {
                    doTerminate();
                } else if (!SOSString.isEmpty(request.getParameter("switch")) && !SOSString.isEmpty(request.getParameter("memberId"))) {
                    doSwitch(request.getParameter("memberId"));
                } else if (!SOSString.isEmpty(request.getParameter("restart"))) {
                    doTerminate();
                    doStart();
                } else {
                    throw new Exception(String.format("[%s]unknown parameters", method));
                }
            }
            sendOKResponse(response);
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
            handlers = new ArrayList<>();
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
                        cluster = new JocCluster(factory, config);
                        cluster.doProcessing(handlers);

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
        closeHandlers();
        closeCluster();
        closeFactory();
        JocCluster.shutdownThreadPool("doTerminate", threadPool, 3);
    }

    private void closeHandlers() {
        String method = "closeHandlers";

        int size = handlers.size();
        if (size > 0) {
            ExecutorService threadPool = Executors.newFixedThreadPool(size);
            for (int i = 0; i < size; i++) {
                IClusterHandler h = handlers.get(i);
                Runnable thread = new Runnable() {

                    @Override
                    public void run() {
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(String.format("[%s][%s][start]...", method, h.getIdentifier()));
                        }
                        h.stop();
                        if (LOGGER.isDebugEnabled()) {
                            LOGGER.debug(String.format("[%s][%s][end]", method, h.getIdentifier()));
                        }
                    }
                };
                threadPool.submit(thread);
            }
            JocCluster.shutdownThreadPool(method, threadPool, 3);
            handlers = new ArrayList<>();
        } else {
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][skip]already closed", method));
            }
        }
    }

    private void closeCluster() {
        if (cluster != null) {
            cluster.close();
            cluster = null;
        }
    }

    private void doSwitch(String memberId) {
        if (cluster != null) {
            cluster.switchMember(memberId);
        }
    }

    private void sendOKResponse(HttpServletResponse response) {
        ClusterAnswer answer = new ClusterAnswer();
        answer.setType(ClusterAnswerType.SUCCESS);

        sendResponse(response, answer);
    }

    private void sendErrorResponse(HttpServletRequest request, HttpServletResponse response, Exception e) {
        LOGGER.error(String.format("[%s]%s", e.toString(), getRequestInfo(request)), e);
        Enumeration<String> paramaterNames = request.getParameterNames();
        while (paramaterNames.hasMoreElements()) {
            String name = paramaterNames.nextElement();
            LOGGER.error(name + "=" + request.getParameter(name));
        }

        ClusterAnswer answer = new ClusterAnswer();
        answer.createError(e);
        sendResponse(response, answer);
    }

    private void sendResponse(HttpServletResponse response, ClusterAnswer answer) {
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
