package com.sos.joc.cluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationStaleStateException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.jobscheduler.db.DBLayer;
import com.sos.jobscheduler.db.inventory.DBItemInventoryInstance;
import com.sos.jobscheduler.db.joc.DBItemJocCluster;
import com.sos.jobscheduler.db.joc.DBItemJocInstance;
import com.sos.jobscheduler.event.http.HttpClient;
import com.sos.jobscheduler.event.master.configuration.master.MasterConfiguration;
import com.sos.joc.cluster.JocClusterHandler.PerformType;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.api.bean.answer.JocClusterAnswer.JocClusterAnswerType;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.db.DBLayerJocCluster;
import com.sos.joc.cluster.instances.JocInstance;

public class JocCluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocCluster.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();

    public static final int MAX_AWAIT_TERMINATION_TIMEOUT = 30;

    private final SOSHibernateFactory dbFactory;
    private final JocClusterConfiguration config;
    private final JocConfiguration jocConfig;
    private final JocClusterHandler handler;
    private final HttpClient httpClient;
    private final String currentMemberId;
    private final Object lockObject = new Object();

    private ArrayList<MasterConfiguration> masters;
    private String lastActiveMemberId;
    private boolean closed;
    private boolean skipNotify;
    private boolean instanceProcessed;

    public JocCluster(SOSHibernateFactory factory, JocClusterConfiguration jocClusterConfiguration, JocConfiguration jocConfiguration,
            List<Class<?>> clusterHandlers) {
        dbFactory = factory;
        config = jocClusterConfiguration;
        jocConfig = jocConfiguration;
        handler = new JocClusterHandler(this, clusterHandlers);
        httpClient = new HttpClient();
        currentMemberId = jocConfig.getMemberId();
    }

    public void doProcessing(Date startTime) {
        LOGGER.info(String.format("[unactive][current memberId]%s", currentMemberId));

        getInstance(startTime);

        while (!closed) {
            try {
                process();

                wait(config.getPollingInterval());
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                wait(config.getPollingWaitIntervalOnError());
            }
        }
    }

    private void getInstance(Date startTime) {
        JocInstance instance = new JocInstance(dbFactory, jocConfig);

        instanceProcessed = false;
        while (!instanceProcessed) {
            try {
                if (closed) {
                    LOGGER.info("[getInstance][skip]due closed");
                    return;
                }
                instance.getInstance(startTime);
                instanceProcessed = true;
            } catch (Throwable e) {
                LOGGER.error(e.toString());
                LOGGER.info("wait 30s and try again ...");
                wait(30);
            }
        }
    }

    public ArrayList<MasterConfiguration> getMasters() {
        boolean run = true;
        while (run) {
            try {
                if (closed) {
                    return masters;
                }
                getMastersFromDb();
                if (masters != null && masters.size() > 0) {
                    run = false;
                } else {
                    LOGGER.info("no masters found. sleep 1m and try again ...");
                    wait(60);
                }
            } catch (Exception e) {
                LOGGER.error(String.format("[error occured][sleep 1m and try again ...]%s", e.toString()));
                wait(60);
            }
        }
        return masters;
    }

    private ArrayList<MasterConfiguration> getMastersFromDb() throws Exception {
        if (masters == null) {
            SOSHibernateSession session = null;
            try {
                session = dbFactory.openStatelessSession("history");
                session.beginTransaction();
                List<DBItemInventoryInstance> result = session.getResultList("from " + DBLayer.DBITEM_INV_JS_INSTANCES);
                session.commit();
                session.close();
                session = null;

                if (result != null && result.size() > 0) {
                    masters = new ArrayList<MasterConfiguration>();
                    Map<String, Properties> map = new HashMap<String, Properties>();
                    for (int i = 0; i < result.size(); i++) {
                        DBItemInventoryInstance item = result.get(i);

                        Properties p = null;
                        if (map.containsKey(item.getSchedulerId())) {
                            p = map.get(item.getSchedulerId());
                        } else {
                            p = new Properties();
                        }
                        // TODO user, pass
                        p.setProperty("jobscheduler_id", item.getSchedulerId());
                        if (item.getIsPrimaryMaster()) {
                            p.setProperty("primary_master_uri", item.getUri());
                            if (item.getClusterUri() != null) {
                                p.setProperty("primary_cluster_uri", item.getClusterUri());
                            }
                        } else {
                            p.setProperty("backup_master_uri", item.getUri());
                            if (item.getClusterUri() != null) {
                                p.setProperty("backup_cluster_uri", item.getClusterUri());
                            }
                        }
                        map.put(item.getSchedulerId(), p);
                    }
                    for (Map.Entry<String, Properties> entry : map.entrySet()) {
                        LOGGER.info(String.format("[add][masterConfiguration]%s", entry));
                        MasterConfiguration mc = new MasterConfiguration();
                        mc.load(entry.getValue());
                        masters.add(mc);
                    }
                }
            } catch (Exception e) {
                if (session != null) {
                    try {
                        session.rollback();
                    } catch (SOSHibernateException e1) {
                    }
                }
                throw e;
            } finally {
                if (session != null) {
                    session.close();
                }
            }
        }
        return masters;
    }

    private synchronized void process() throws Exception {
        DBLayerJocCluster dbLayer = null;
        DBItemJocCluster item = null;
        try {
            dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession());
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
                if (config.currentIsClusterMember()) {
                    item = handleCurrentMemberOnProcess(dbLayer, item);
                }
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
                if (config.currentIsClusterMember()) {
                    notifyHandlers(item.getMemberId());// TODO
                }
            }
        }
    }

    private DBItemJocCluster handleCurrentMemberOnProcess(DBLayerJocCluster dbLayer, DBItemJocCluster item) throws Exception {
        if (item == null) {
            item = new DBItemJocCluster();
            item.setId(JocClusterConfiguration.CLUSTER_ID);
            item.setMemberId(currentMemberId);

            dbLayer.getSession().beginTransaction();
            dbLayer.getSession().save(item);
            dbLayer.getSession().commit();

            if (isDebugEnabled) {
                LOGGER.debug(String.format("[save]%s", item));
            }
        } else {
            if (item.getMemberId().equals(currentMemberId)) {
                item = trySwitchCurrentMemberOnProcess(dbLayer, item);

                if (isDebugEnabled) {
                    LOGGER.debug(String.format("[currentMember][update]%s", SOSHibernate.toString(item)));
                }
            } else {
                if (isHeartBeatExceeded(item.getHeartBeat())) {
                    item.setMemberId(currentMemberId);

                    dbLayer.getSession().beginTransaction();
                    dbLayer.getSession().update(item);
                    dbLayer.getSession().commit();

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
        if (isDebugEnabled) {
            LOGGER.debug(String.format("[end][current=%s][last=%s]%s", currentMemberId, lastActiveMemberId, SOSHibernate.toString(item)));
        }
        return item;
    }

    public JocClusterAnswer switchMember(String newMemberId) {
        LOGGER.info("[switch][start]" + newMemberId);

        JocClusterAnswer answer = checkSwitchMaster(newMemberId);
        if (answer != null) {
            return answer;
        }

        try {
            DBLayerJocCluster dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession());
            synchronized (lockObject) {
                boolean run = true;
                int errorCounter = 0;
                int waitCounter = 0;
                while (run) {
                    if (closed) {
                        LOGGER.info("[switchMaster][skip]due closed");
                        return getOKAnswer();// TODO OK?
                    }

                    try {
                        DBItemJocCluster item = setSwitchMember(dbLayer, newMemberId);

                        LOGGER.info("[switch]" + SOSString.toString(item));

                        if (item.getSwitchMemberId() == null) {
                            run = false;
                        } else {
                            waitCounter += 1;
                            if (waitCounter >= config.getSwitchMemberWaitCounterOnSuccess()) {
                                run = false;
                            } else {
                                wait(config.getSwitchMemberWaitIntervalOnSuccess());
                            }
                        }

                    } catch (Exception e) {
                        LOGGER.warn(String.format("[%s]%s", errorCounter, e.toString()));
                        errorCounter += 1;
                        if (errorCounter >= config.getSwitchMemberWaitCounterOnError()) {
                            throw e;
                        }
                        wait(config.getSwitchMemberWaitIntervalOnError());
                    }
                }
            }
            return getOKAnswer();
        } catch (Exception e) {
            return getErrorAnswer(e);
        }

    }

    private JocClusterAnswer checkSwitchMaster(String newMemberId) {
        if (SOSString.isEmpty(newMemberId)) {
            return getErrorAnswer(new Exception("missing memberId"));
        }
        if (newMemberId.equals(currentMemberId)) {
            if (handler.isActive()) {
                return getOKAnswer();
            }
        } else {// check if exists
            try {
                if (lastActiveMemberId != null && newMemberId.equals(lastActiveMemberId)) {

                } else {
                    DBItemJocInstance item = getInstance(newMemberId);
                    if (item == null) {
                        throw new Exception(String.format("memberId=%s not found", newMemberId));
                    }
                    if (isHeartBeatExceeded(item.getHeartBeat())) {
                        throw new Exception(String.format("[memberId=%s][last heart beat too old]%s", newMemberId, SOSDate.getDateTimeAsString(item
                                .getHeartBeat(), SOSDate.getDateTimeFormat())));
                    }
                }
            } catch (Exception e) {
                return getErrorAnswer(e);
            }
        }
        return null;
    }

    private DBItemJocCluster setSwitchMember(DBLayerJocCluster dbLayer, String newMemberId) throws Exception {

        DBItemJocCluster item = null;
        try {
            dbLayer.getSession().beginTransaction();
            item = dbLayer.getCluster();
            if (item == null) {
                return item;
            }
            if (item.getMemberId().equals(currentMemberId)) {
                if (newMemberId.equals(currentMemberId)) {
                    // current is active - handled by checkSwitchMaster
                    // current is not active - not possible (item.getMember() is an active instance)
                } else {
                    if (handler.isActive()) {
                        handler.perform(PerformType.STOP);
                    }
                    item.setMemberId(newMemberId);
                    item.setSwitchMemberId(null);
                    item.setSwitchHeartBeat(null);
                    dbLayer.getSession().update(item);
                }
            } else {
                if (!item.getMemberId().equals(newMemberId)) {
                    item.setSwitchMemberId(newMemberId);
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
            throw e;
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);

            if (dbLayer != null) {
                dbLayer.getSession().rollback();
            }
            throw e;
        }
        return item;
    }

    private DBItemJocCluster trySwitchCurrentMemberOnProcess(DBLayerJocCluster dbLayer, DBItemJocCluster item) throws Exception {
        skipNotify = false;
        item.setMemberId(currentMemberId);

        if (item.getSwitchMemberId() != null) {

            boolean run = true;
            int errorCounter = 0;
            while (run) {
                if (closed) {
                    LOGGER.info("[trySwitchCurrentMemberOnProcess][skip]due closed");
                    return item;
                }
                try {
                    if (!isHeartBeatExceeded(item.getSwitchHeartBeat())) {
                        if (handler.isActive()) {
                            handler.perform(PerformType.STOP);
                        }
                        item.setMemberId(item.getSwitchMemberId());
                        skipNotify = true;
                    }
                    item.setSwitchMemberId(null);
                    item.setSwitchHeartBeat(null);
                    dbLayer.getSession().beginTransaction();
                    dbLayer.getSession().update(item);
                    dbLayer.getSession().commit();
                    run = false;
                } catch (Exception e) {
                    LOGGER.warn(String.format("[%s]%s", errorCounter, e.toString()));
                    errorCounter += 1;
                    if (errorCounter >= config.getSwitchMemberWaitCounterOnError()) {
                        throw e;
                    }
                    wait(config.getSwitchMemberWaitIntervalOnError());
                }
            }
        } else {
            dbLayer.getSession().beginTransaction();
            dbLayer.getSession().update(item);
            dbLayer.getSession().commit();
        }

        return item;
    }

    private DBItemJocInstance getInstance(String memberId) throws Exception {
        DBLayerJocCluster dbLayer = null;
        try {
            dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession());
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

    private boolean isHeartBeatExceeded(Date heartBeat) {
        Date now = new Date();
        if (((now.getTime() / 1_000) - (heartBeat.getTime() / 1_000)) >= config.getHeartBeatExceededInterval()) {
            return true;
        }
        return false;
    }

    private JocClusterAnswer notifyHandlers(String memberId) {// TODO
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
        LOGGER.info("[cluster][close]start ...----------------------------------------------");
        closed = true;
        httpClient.close();
        synchronized (httpClient) {
            httpClient.notifyAll();
        }
        closeHandlers();
        tryDeleteClusterCurrentMember();
        LOGGER.info("[cluster][close]closed----------------------------------------------");
    }

    private JocClusterAnswer closeHandlers() {
        LOGGER.info("[cluster][closeHandlers][isActive=" + handler.isActive() + "]start ...");
        JocClusterAnswer answer = null;
        if (handler.isActive()) {
            answer = handler.perform(PerformType.STOP);
        } else {
            answer = getOKAnswer();
        }
        LOGGER.info("[cluster][closeHandlers][isActive=" + handler.isActive() + "]closed");
        return answer;
    }

    private int tryDeleteClusterCurrentMember() {
        if (!instanceProcessed) {
            return 0;
        }
        DBLayerJocCluster dbLayer = null;
        int result = 0;
        try {
            dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession());
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

    public static JocClusterAnswer getOKAnswer() {
        JocClusterAnswer answer = new JocClusterAnswer();
        answer.setType(JocClusterAnswerType.SUCCESS);
        return answer;
    }

    public static JocClusterAnswer getErrorAnswer(Exception e) {
        JocClusterAnswer answer = new JocClusterAnswer();
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

    public JocConfiguration getJocConfig() {
        return jocConfig;
    }

}
