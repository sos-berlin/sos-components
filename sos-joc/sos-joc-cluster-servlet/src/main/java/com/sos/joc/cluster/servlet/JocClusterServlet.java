package com.sos.joc.cluster.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.cluster.ThreadHelper;
import com.sos.joc.cluster.api.JocClusterServiceHelper;
import com.sos.js7.history.controller.HistoryMain;

public class JocClusterServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterServlet.class);

    private final JocClusterServiceHelper jocClusterServiceHelper;

    public JocClusterServlet() {
        super();
        jocClusterServiceHelper = JocClusterServiceHelper.getInstance();
        jocClusterServiceHelper.addHandler(HistoryMain.class);
    }

    public void init() throws ServletException {
        jocClusterServiceHelper.doStartCluster();
    }

    public void destroy() {
        LOGGER.info("[servlet][destroy]");
        jocClusterServiceHelper.doStopCluster();

        ThreadHelper.showGroupInfo(ThreadHelper.getThreadGroup(), "after stop");
    }
}
