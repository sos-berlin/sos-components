package com.sos.joc.cluster.servlet;

import java.io.IOException;
import java.io.PrintWriter;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.util.SOSShell;
import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.ThreadHelper;
import com.sos.joc.cluster.api.JocClusterMeta;
import com.sos.joc.cluster.api.JocClusterMeta.HandlerIdentifier;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.api.bean.request.restart.JocClusterRestartRequest;
import com.sos.joc.cluster.api.bean.request.switchmember.JocClusterSwitchMemberRequest;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.db.inventory.DBItemInventoryInstance;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.db.os.DBItemOperatingSystem;
import com.sos.js7.history.controller.HistoryMain;

public class JocClusterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterServlet.class);

    private static final String IDENTIFIER = "cluster";

    private final JocConfiguration config;
    private final List<Class<?>> handlers;
    private final ObjectMapper jsonObjectMapper;
    private final Date startTime;

    private ExecutorService threadPool;
    private JocClusterHibernateFactory factory;
    private JocCluster cluster;

    public JocClusterServlet() {
        super();
        config = new JocConfiguration(System.getProperty("user.dir"), TimeZone.getDefault().getID());

        startTime = new Date();
        jsonObjectMapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);

        handlers = new ArrayList<>();
        handlers.add(HistoryMain.class);
        // handlers.add(OrderInitiatorMain.class);
    }

    public void init() throws ServletException {
        doStartCluster();
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            String pathInfo = checkAccess(request);

            JocClusterAnswer answer = null;
            if (pathInfo.equals("/" + JocClusterMeta.RequestPath.switchMember.name())) {
                answer = doSwitchCluster(request);
            } else if (pathInfo.equals("/" + JocClusterMeta.RequestPath.restart.name())) {
                answer = doRestart(request);
            } else {
                throw new Exception(String.format("unknown path=%s", pathInfo));
            }

            sendResponse(response, answer);
        } catch (Exception e) {
            sendErrorResponse(request, response, e);
        }
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

    }

    public void destroy() {
        LOGGER.info("[servlet][destroy]");
        doStopCluster();

        ThreadHelper.showGroupInfo(ThreadHelper.getThreadGroup(), "after stop");
    }

    private JocClusterAnswer doStartCluster() throws ServletException {
        JocClusterAnswer answer = JocCluster.getOKAnswer(JocClusterAnswerState.STARTED);
        if (cluster == null) {
            threadPool = Executors.newFixedThreadPool(1, new JocClusterThreadFactory(IDENTIFIER));
            Runnable task = new Runnable() {

                @Override
                public void run() {
                    LOGGER.info("[start][run]...");
                    try {
                        SOSShell.printSystemInfos();
                        SOSShell.printJVMInfos();

                        createFactory(config.getHibernateConfiguration());

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
            answer.setState(JocClusterAnswerState.ALREADY_STARTED);
        }
        return answer;
    }

    private JocClusterAnswer doStopCluster() {
        JocClusterAnswer answer = JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
        if (cluster != null) {
            closeCluster();
            closeFactory();
            JocCluster.shutdownThreadPool(threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
        } else {
            answer.setState(JocClusterAnswerState.ALREADY_STOPPED);
        }
        return answer;
    }

    private void closeCluster() {
        if (cluster != null) {
            cluster.close();
            cluster = null;
        }
    }

    private JocClusterAnswer doSwitchCluster(HttpServletRequest request) {

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

    private JocClusterAnswer doRestart(HttpServletRequest request) throws Exception {

        JocClusterRestartRequest r = null;
        try {
            r = jsonObjectMapper.readValue(request.getInputStream(), JocClusterRestartRequest.class);
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            return JocCluster.getErrorAnswer(e);
        }
        if (r == null || r.getType() == null) {
            return JocCluster.getErrorAnswer(new Exception("missing type"));
        }

        LOGGER.info(String.format("[restart][%s]start...", r.getType().name()));

        JocClusterAnswer answer = null;
        if (r.getType().equals(HandlerIdentifier.cluster)) {
            answer = restartCluster();
        } else {
            answer = restartHandler(r);
        }

        LOGGER.info(String.format("[restart][%s]end", r.getType().name()));

        return answer;
    }

    private JocClusterAnswer restartCluster() throws Exception {
        doStopCluster();
        JocClusterAnswer answer = doStartCluster();
        if (answer.getState().equals(JocClusterAnswerState.STARTED)) {
            answer.setState(JocClusterAnswerState.RESTARTED);
        }
        return answer;
    }

    private JocClusterAnswer restartHandler(JocClusterRestartRequest r) throws Exception {
        if (cluster == null) {
            return JocCluster.getErrorAnswer(new Exception(String.format("cluster not started. restart %s can't be performed.", r.getType())));
        }
        if (!cluster.getHandler().isActive()) {
            return JocCluster.getErrorAnswer(new Exception(String.format("cluster inactiv. restart %s can't be performed.", r.getType())));
        }

        JocClusterAnswer answer = null;
        if (r.getType().equals(HandlerIdentifier.history)) {
            answer = cluster.getHandler().restartHandler(HandlerIdentifier.history.name());
        } else {
            answer = JocCluster.getErrorAnswer(new Exception(String.format("restart not yet supported for %s", r.getType())));
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
        LOGGER.info(SOSString.toString(answer));

        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter out;
        try {
            out = response.getWriter();
            if (answer == null) {
                out.print(jsonObjectMapper.writeValueAsString(JocCluster.getErrorAnswer(new Exception("missing answer"))));
            } else {
                out.print(jsonObjectMapper.writeValueAsString(answer));
            }
            out.flush();
        } catch (Exception ex) {
            LOGGER.error(ex.toString(), ex);
        }
    }

    private String getRequestInfo(HttpServletRequest request) {
        return request.getRequestURL().append("?").append(request.getQueryString()).toString();
    }

    private String checkAccess(HttpServletRequest request) throws Exception {// TODO
        String pathInfo = request.getPathInfo();
        if (SOSString.isEmpty(pathInfo)) {
            throw new Exception("missing path");
        }
        return pathInfo;
    }

    @SuppressWarnings("unused")
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
        factory = new JocClusterHibernateFactory(configFile, 1, 1);
        factory.setIdentifier(IDENTIFIER);
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBItemOperatingSystem.class);
        factory.addClassMapping(DBItemJocInstance.class);
        factory.addClassMapping(DBItemJocCluster.class);
        factory.addClassMapping(DBItemInventoryInstance.class);
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
