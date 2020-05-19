package com.sos.joc.cluster;

import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationStaleStateException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.db.cluster.DBItemJocCluster;
import com.sos.jobscheduler.db.cluster.DBItemJocInstance;
import com.sos.jobscheduler.event.http.HttpClient;
import com.sos.joc.cluster.JocClusterHandler.PerformType;
import com.sos.joc.cluster.api.bean.ClusterAnswer;
import com.sos.joc.cluster.api.bean.ClusterAnswer.ClusterAnswerType;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.db.DBLayerCluster;
import com.sos.joc.cluster.handler.IClusterHandler;

public class JocCluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocCluster.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    private static final String CLUSTER_ID = "cluster";
    private final SOSHibernateFactory dbFactory;
    private final JocClusterHandler handler;
    private final HttpClient httpClient;
    private final String currentMemberId;
    private final Object lockObject = new Object();

    private String lastActiveMemberId;
    private boolean closed;
    private boolean skipNotify;

    public JocCluster(SOSHibernateFactory factory, JocConfiguration config, List<IClusterHandler> clusterHandlers) {
        dbFactory = factory;
        handler = new JocClusterHandler(clusterHandlers);
        httpClient = new HttpClient();
        currentMemberId = config.getMemberId();
    }

    public void doProcessing() {
        LOGGER.info(String.format("[unactive][current memberId]%s", currentMemberId));

        while (!closed) {
            try {
                process();

                wait(30);
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                wait(10);
            }
        }
    }

    private synchronized void process() throws Exception {
        DBLayerCluster dbLayer = null;
        DBItemJocCluster item = null;
        try {
            dbLayer = new DBLayerCluster(dbFactory.openStatelessSession());
            dbLayer.getSession().beginTransaction();
            dbLayer.updateInstanceHeartBeat(currentMemberId);
            item = dbLayer.getCluster();
            dbLayer.getSession().commit();

            lastActiveMemberId = item == null ? null : item.getMemberId();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[start][current=%s][last=%s]%s", currentMemberId, lastActiveMemberId, SOSHibernate.toString(item)));
            }

            skipNotify = false;
            synchronized (lockObject) {
                item = handleCurrentMember(dbLayer, item);
            }

        } catch (SOSHibernateObjectOperationStaleStateException e) {// @Version
            // locked
            if (dbLayer != null) {
                dbLayer.getSession().rollback();
            }
            LOGGER.error(e.toString(), e);
            LOGGER.error(String.format("[exception][current=%s][last=%s]%s", currentMemberId, lastActiveMemberId, SOSHibernate.toString(item)));

        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            if (dbLayer != null) {
                dbLayer.getSession().rollback();
            }
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.getSession().close();
            }
            if (!skipNotify) {
                notifyHandlers(item.getMemberId());// TODO
            }
        }
    }

    private DBItemJocCluster handleCurrentMember(DBLayerCluster dbLayer, DBItemJocCluster item) throws Exception {
        dbLayer.getSession().beginTransaction();
        if (item == null) {
            item = new DBItemJocCluster();
            item.setId(CLUSTER_ID);
            item.setMemberId(currentMemberId);
            dbLayer.getSession().save(item);

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[save]%s", item));
            }
        } else {
            if (item.getMemberId().equals(currentMemberId)) {
                item.setMemberId(currentMemberId);

                if (item.getSwitchMemberId() != null) {
                    if (!isHeartBeatTimeExceeded(item.getSwitchHeartBeat())) {
                        if (handler.isActive()) {
                            handler.perform(PerformType.STOP);
                        }
                        item.setMemberId(item.getSwitchMemberId()); // todo
                        skipNotify = true;
                    }
                    item.setSwitchMemberId(null);
                    item.setSwitchHeartBeat(null);
                }

                dbLayer.getSession().update(item);

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[currentMember][update]%s", SOSHibernate.toString(item)));
                }
            } else {
                if (isHeartBeatTimeExceeded(item.getHeartBeat())) {
                    item.setMemberId(currentMemberId);
                    dbLayer.getSession().update(item);

                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[heartBeat exceeded][update]%s", SOSHibernate.toString(item)));
                    }
                } else {

                    if (isDebugEnabled) {
                        LOGGER.debug("not active");
                    }
                }
            }
        }
        if (closed) {
            dbLayer.getSession().rollback();
            LOGGER.info(String.format("[end][skip due closed=true][current=%s][last=%s]%s", currentMemberId, lastActiveMemberId, SOSHibernate
                    .toString(item)));
        } else {
            dbLayer.getSession().commit();
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[end][current=%s][last=%s]%s", currentMemberId, lastActiveMemberId, SOSHibernate.toString(item)));
            }
        }
        return item;
    }

    public ClusterAnswer switchMember(String memberId) {
        LOGGER.info("[switch][start]" + memberId);

        if (SOSString.isEmpty(memberId)) {
            return getErrorAnswer(new Exception("missing memberId"));
        }
        if (memberId.equals(currentMemberId)) {
            if (handler.isActive()) {
                return getOKAnswer();
            }
        } else {// check if exists
            try {
                if (lastActiveMemberId != null && memberId.equals(lastActiveMemberId)) {

                } else {
                    DBItemJocInstance item = getInstance(memberId);
                    if (item == null) {
                        throw new Exception(String.format("memberId=%s not found", memberId));
                    }
                    if (isHeartBeatTimeExceeded(item.getHeartBeat())) {
                        throw new Exception(String.format("[memberId=%s][last heart beat too old]%s", memberId, SOSDate.getDateTimeAsString(item
                                .getHeartBeat(), SOSDate.getDateTimeFormat())));
                    }
                }
            } catch (Exception e) {
                return getErrorAnswer(e);
            }
        }

        try {
            DBLayerCluster dbLayer = new DBLayerCluster(dbFactory.openStatelessSession());
            synchronized (lockObject) {
                boolean run = true;
                int errorCounter = 0;
                int waitCounter = 0;
                while (run) {
                    try {
                        DBItemJocCluster item = setSwitchMember(dbLayer, memberId);

                        LOGGER.info("[switch]" + SOSString.toString(item));

                        if (item.getSwitchMemberId() == null) {
                            run = false;
                        } else {
                            waitCounter += 1;
                            if (waitCounter >= 10) {
                                run = false;
                            } else {
                                wait(5);
                            }
                        }

                    } catch (Exception e) {
                        LOGGER.warn(String.format("[%s]%s", errorCounter, e.toString()));
                        errorCounter += 1;
                        if (errorCounter >= 10) {
                            throw e;
                        }
                        wait(2);
                    }
                }
            }
            return getOKAnswer();
        } catch (Exception e) {
            return getErrorAnswer(e);
        }

    }

    private DBItemJocCluster setSwitchMember(DBLayerCluster dbLayer, String memberId) throws Exception {

        DBItemJocCluster item = null;
        try {
            dbLayer.getSession().beginTransaction();
            item = dbLayer.getCluster();
            if (item == null) {
                return item;
            }
            if (item.getMemberId().equals(currentMemberId)) {
                if (memberId.equals(currentMemberId)) {
                    // start
                } else {
                    if (handler.isActive()) {
                        handler.perform(PerformType.STOP);
                    }
                    item.setMemberId(memberId);
                    item.setSwitchMemberId(null);
                    item.setSwitchHeartBeat(null);
                    dbLayer.getSession().update(item);
                }
            } else {
                if (!item.getMemberId().equals(memberId)) {
                    item.setSwitchMemberId(memberId);
                    item.setSwitchHeartBeat(new Date());
                    dbLayer.getSession().update(item);
                }
            }
            dbLayer.getSession().commit();
        } catch (SOSHibernateObjectOperationStaleStateException e) {// @Version
            LOGGER.error(e.toString(), e);
            if (dbLayer != null) {
                dbLayer.getSession().rollback();
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);

            if (dbLayer != null) {
                dbLayer.getSession().rollback();
            }
            throw e;
        }
        return item;
    }

    private DBItemJocInstance getInstance(String memberId) throws Exception {
        DBLayerCluster dbLayer = null;
        try {
            dbLayer = new DBLayerCluster(dbFactory.openStatelessSession());
            dbLayer.getSession().beginTransaction();
            DBItemJocInstance item = dbLayer.getInstance(memberId);
            dbLayer.getSession().commit();
            return item;
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            if (dbLayer != null) {
                dbLayer.getSession().rollback();
            }
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.getSession().close();
            }
        }
    }

    private boolean isHeartBeatTimeExceeded(Date heartBeat) {
        Date now = new Date();
        if (((now.getTime() / 1_000) - (heartBeat.getTime() / 1_000)) >= 60) {
            return true;
        }
        return false;
    }

    private ClusterAnswer notifyHandlers(String memberId) {// TODO
        if (memberId.equals(currentMemberId)) {
            if (!handler.isActive()) {
                return handler.perform(PerformType.START);
            }
        } else {
            if (handler.isActive()) {
                return handler.perform(PerformType.STOP);
            }
        }
        return getOKAnswer();
    }

    public void close() {
        closed = true;
        httpClient.close();
        synchronized (httpClient) {
            httpClient.notifyAll();
        }
        closeHandlers();
        tryDeleteClusterActiveMember();
    }

    private ClusterAnswer closeHandlers() {
        ClusterAnswer answer = handler.perform(PerformType.STOP);
        return answer;
    }

    private int tryDeleteClusterActiveMember() {
        DBLayerCluster dbLayer = null;
        int result = 0;
        try {
            dbLayer = new DBLayerCluster(dbFactory.openStatelessSession());
            dbLayer.getSession().beginTransaction();
            result = dbLayer.deleteCluster(currentMemberId);
            dbLayer.getSession().commit();

            if (Math.abs(result) > 0) {
                LOGGER.info(String.format("[active current memberId deleted]%s", currentMemberId));
            }
        } catch (SOSHibernateObjectOperationStaleStateException e) {// @Version
            // ignore exceptions - not me
            LOGGER.error(e.toString(), e);
            if (dbLayer != null) {
                try {
                    dbLayer.getSession().rollback();
                } catch (SOSHibernateException e1) {
                }
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);

            if (dbLayer != null) {
                try {
                    dbLayer.getSession().rollback();
                } catch (SOSHibernateException e1) {
                }
            }
        }
        return result;
    }

    public static ClusterAnswer getOKAnswer() {
        ClusterAnswer answer = new ClusterAnswer();
        answer.setType(ClusterAnswerType.SUCCESS);
        return answer;
    }

    public static ClusterAnswer getErrorAnswer(Exception e) {
        ClusterAnswer answer = new ClusterAnswer();
        answer.createError(e);
        return answer;
    }

    public static void shutdownThreadPool(String callerMethod, ExecutorService threadPool, long awaitTerminationTimeout) {
        try {
            if (threadPool == null) {
                return;
            }
            threadPool.shutdown();
            // threadPool.shutdownNow();
            boolean shutdown = threadPool.awaitTermination(awaitTerminationTimeout, TimeUnit.SECONDS);
            if (shutdown) {
                LOGGER.info(String.format("[%s]thread has been shut down correctly", callerMethod));
            } else {
                LOGGER.info(String.format("[%s]thread has ended due to timeout of %ss on shutdown", callerMethod, awaitTerminationTimeout));
            }
        } catch (InterruptedException e) {
            LOGGER.error(String.format("[%s][exception]%s", callerMethod, e.toString()), e);
        }
    }

    public void wait(int interval) {
        if (!closed && interval > 0) {
            String method = "wait";
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s%ss ...", method, interval));
            }
            try {
                synchronized (httpClient) {
                    httpClient.wait(interval * 1_000);
                }
            } catch (InterruptedException e) {
                if (closed) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%ssleep interrupted due handler close", method));
                    }
                } else {
                    LOGGER.warn(String.format("%s%s", method, e.toString()), e);
                }
            }
        }
    }

}
