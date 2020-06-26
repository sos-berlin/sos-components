package com.sos.joc.cluster.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.ThreadHelper;
import com.sos.joc.cluster.api.JocClusterMeta;
import com.sos.joc.cluster.api.JocClusterMeta.HandlerIdentifier;
import com.sos.joc.cluster.api.JocClusterServiceHelper;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.api.bean.request.restart.JocClusterRestartRequest;
import com.sos.joc.cluster.api.bean.request.switchmember.JocClusterSwitchMemberRequest;
import com.sos.js7.history.controller.HistoryMain;

public class JocClusterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterServlet.class);

    private final ObjectMapper jsonObjectMapper;
    private final JocClusterServiceHelper jocClusterServiceHelper;

    public JocClusterServlet() {
        super();
        jocClusterServiceHelper = JocClusterServiceHelper.getInstance();
        jocClusterServiceHelper.addHandler(HistoryMain.class);

        jsonObjectMapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
    }

    public void init() throws ServletException {
        jocClusterServiceHelper.doStartCluster();
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
        jocClusterServiceHelper.doStopCluster();

        ThreadHelper.showGroupInfo(ThreadHelper.getThreadGroup(), "after stop");
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
        JocCluster cluster = jocClusterServiceHelper.getCluster();
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
            answer = jocClusterServiceHelper.restartCluster();
        } else {
            answer = jocClusterServiceHelper.restartHandler(r);
        }

        LOGGER.info(String.format("[restart][%s]end", r.getType().name()));

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
}
