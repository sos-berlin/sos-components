package com.sos.joc.cluster.servlet;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

public class JocClusterServlet extends JocClusterBaseServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger LOGGER = LoggerFactory.getLogger(JocClusterServlet.class);

    private static final String IDENTIFIER = "cluster";
    private static final String LOG4J_FILE = "joc/cluster.log4j2.xml";

    private ExecutorService threadPool;
    private SOSHibernateFactory factory;
    private JocCluster cluster;
    private final Date startTime;

    public JocClusterServlet() {
        super();
        startTime = new Date();
    }

    public void init() throws ServletException {
        super.init(LOG4J_FILE);

        doStart();
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
        } catch (Throwable e) {
            sendErrorResponse(request, response, e);
        }
    }

    public void destroy() {
        LOGGER.info("[servlet][destroy]");
        doTerminate();
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

                        createFactory(getHibernateConfiguration());
                        JocInstance instance = new JocInstance(factory, getConfig(), getDataDirectory(), getTimezone(), startTime);
                        instance.onStart();
                        cluster = new JocCluster(factory, getConfig());
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
        if (cluster != null) {
            cluster.close();
            cluster = null;
        }
        closeFactory();
        shutdownThreadPool("[doTerminate]", threadPool, 3);
    }

    private void doSwitch(String memberId) {
        if (cluster != null) {
            cluster.switchMember(memberId);
        }
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
