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
import com.sos.jobscheduler.db.joc.DBItemJocCluster;
import com.sos.jobscheduler.db.joc.DBItemJocInstance;
import com.sos.jobscheduler.db.os.DBItemOperatingSystem;
import com.sos.jobscheduler.history.master.HistoryMain;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.api.JocClusterMeta;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.api.bean.request.switchmember.JocClusterSwitchMemberRequest;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocConfiguration;

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
        try {
            String pathInfo = checkAccess(request);

            JocClusterAnswer answer = null;
            if (pathInfo.equals("/" + JocClusterMeta.RequestPath.switchMember.name())) {
                answer = doSwitch(request);
            } else {
                throw new Exception(String.format("unknown path=%s", pathInfo));
            }

            sendResponse(response, answer);
        } catch (Exception e) {
            sendErrorResponse(request, response, e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String pathInfo = checkAccess(request);

            // TODO answer
            JocClusterAnswer answer = null;
            if (pathInfo.equals("/" + JocClusterMeta.RequestPath.start.name())) {
                doStart();
            } else if (pathInfo.equals("/" + JocClusterMeta.RequestPath.stop.name())) {
                doStop();
            } else if (pathInfo.equals("/" + JocClusterMeta.RequestPath.restart.name())) {
                doStop();
                doStart();
            } else {
                throw new Exception(String.format("unknown path=%s", pathInfo));
            }

            sendResponse(response, answer);
        } catch (Exception e) {
            sendErrorResponse(request, response, e);
        }
    }

    public void destroy() {
        LOGGER.info("[servlet][destroy]");
        doStop();
    }

    private void doStart() throws ServletException {

        if (cluster == null) {
            threadPool = Executors.newFixedThreadPool(1);
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    LOGGER.info("[start][run]...");
                    try {
                        SOSShell.printSystemInfos();
                        SOSShell.printJVMInfos();

                        createFactory(config.getHibernateConfiguration());

                        List<Class<?>> handlers = new ArrayList<>();
                        handlers.add(HistoryMain.class);

                        cluster = new JocCluster(factory, new JocClusterConfiguration(config.getResourceDirectory()), config, handlers);
                        cluster.doProcessing(startTime);

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

    private void doStop() {
        if (cluster != null) {
            closeCluster();
            closeFactory();
            JocCluster.shutdownThreadPool("doStop", threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
        }
    }

    private void closeCluster() {
        if (cluster != null) {
            cluster.close();
            cluster = null;
        }
    }

    private JocClusterAnswer doSwitch(HttpServletRequest request) {

        JocClusterSwitchMemberRequest r = null;
        try {
            r = jsonObjectMapper.readValue(request.getInputStream(), JocClusterSwitchMemberRequest.class);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            return JocCluster.getErrorAnswer(e);
        }
        if (r == null || SOSString.isEmpty(r.getMemberId())) {
            return JocCluster.getErrorAnswer(new Exception("missing memberId"));
        }

        JocClusterAnswer answer = null;
        if (cluster != null) {
            answer = cluster.switchMember(r.getMemberId());
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

    private void sendResponse(HttpServletResponse response, JocClusterAnswer answer) {
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

    private String checkAccess(HttpServletRequest request) throws Exception {// TODO
        if (SOSString.isEmpty(request.getHeader(JocClusterMeta.HEADER_NAME_ACCESS_TOKEN))) {
            throw new Exception("invalid session");
        }
        String pathInfo = request.getPathInfo();
        if (SOSString.isEmpty(pathInfo)) {
            throw new Exception("unknown path");
        }
        // if ("localhost".equals(request.getServerName())) { // request.getLocalPort()
        // }
        return pathInfo;
    }

    private void printRequestInfo(HttpServletRequest request) {
        LOGGER.info(String.format("[uri]%s", request.getRequestURL().append('?').append(request.getQueryString())));

        LOGGER.info(String.format("[getContextPath]%s", request.getContextPath()));
        LOGGER.info(String.format("[getQueryString]%s", request.getQueryString()));
        LOGGER.info(String.format("[getPathInfo]%s", request.getPathInfo()));
        LOGGER.info(String.format("[getPathTranslated]%s", request.getPathTranslated()));
        LOGGER.info(String.format("[getRequestURI]%s", request.getRequestURI()));
        LOGGER.info(String.format("[getRequestURL]%s", request.getRequestURL()));
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
            LOGGER.info(String.format("database factory closed"));
        } else {
            LOGGER.info(String.format("database factory already closed"));
        }

    }
}
