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
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationException;
import com.sos.commons.hibernate.exception.SOSHibernateObjectOperationStaleStateException;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSString;
import com.sos.joc.cluster.JocClusterHandler.PerformType;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.db.DBLayerJocCluster;
import com.sos.joc.cluster.instances.JocInstance;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryInstance;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.js7.event.controller.configuration.controller.ControllerConfiguration;
import com.sos.js7.event.http.HttpClient;

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

    private List<ControllerConfiguration> controllers;
    private String activeMemberId;
    private String lastActiveMemberId;
    private boolean closed;
    private boolean skipPerform;
    private boolean instanceProcessed;

    public JocCluster(SOSHibernateFactory factory, JocClusterConfiguration jocClusterConfiguration, JocConfiguration jocConfiguration) {
        dbFactory = factory;
        config = jocClusterConfiguration;
        jocConfig = jocConfiguration;
        handler = new JocClusterHandler(this);
        httpClient = new HttpClient();
        currentMemberId = jocConfig.getMemberId();
    }

    public void doProcessing(Date startTime) {
        LOGGER.info(String.format("[inactive][current memberId]%s", currentMemberId));

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
                    LOGGER.info("[getInstance][skip]because closed");
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

    public List<ControllerConfiguration> getControllers() {
        boolean run = true;
        while (run) {
            try {
                if (closed) {
                    return controllers;
                }
                getControllersFromDb();
                if (controllers != null && controllers.size() > 0) {
                    run = false;
                } else {
                    LOGGER.info("no controllers found. sleep 1m and try again ...");
                    wait(60);
                }
            } catch (Exception e) {
                LOGGER.error(String.format("[error occured][sleep 1m and try again ...]%s", e.toString()));
                wait(60);
            }
        }
        return controllers;
    }

    private List<ControllerConfiguration> getControllersFromDb() throws Exception {
        if (controllers == null) {
            SOSHibernateSession session = null;
            try {
                session = dbFactory.openStatelessSession("history");
                session.beginTransaction();
                List<DBItemInventoryInstance> result = session.getResultList("from " + DBLayer.DBITEM_INV_JS_INSTANCES);
                session.commit();
                session.close();
                session = null;

                if (result != null && result.size() > 0) {
                    controllers = new ArrayList<ControllerConfiguration>();
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
                        LOGGER.info(String.format("[add][controllerConfiguration]%s", entry));
                        ControllerConfiguration mc = new ControllerConfiguration();
                        mc.load(entry.getValue());
                        controllers.add(mc);
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
        return controllers;
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

            activeMemberId = item == null ? null : item.getMemberId();
            if (lastActiveMemberId == null) {
                lastActiveMemberId = activeMemberId;
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[start][current=%s][active=%s][lastActive=%s]%s", currentMemberId, activeMemberId, lastActiveMemberId,
                        SOSHibernate.toString(item)));
            }

            skipPerform = false;
            synchronized (lockObject) {
                if (config.currentIsClusterMember()) {
                    item = handleCurrentMemberOnProcess(dbLayer, item);
                    if (item != null) {
                        activeMemberId = item.getMemberId();

                        if (isDebugEnabled) {
                            LOGGER.debug(String.format("[end][current=%s][active=%s][lastActive=%s]%s", currentMemberId, activeMemberId,
                                    lastActiveMemberId, SOSHibernate.toString(item)));
                        }
                    }
                }
            }

        } catch (SOSHibernateObjectOperationStaleStateException e) {// @Version
            // locked
            if (dbLayer != null) {
                dbLayer.getSession().rollback();
            }
            LOGGER.error(e.toString(), e);
            LOGGER.error(String.format("[exception][current=%s][active=%s][lastActive=%s][locked]%s", currentMemberId, activeMemberId,
                    lastActiveMemberId, SOSHibernate.toString(item)));

        } catch (SOSHibernateObjectOperationException e) {
            if (dbLayer != null) {
                dbLayer.getSession().rollback();
            }
            LOGGER.error(e.toString(), e);
            LOGGER.error(String.format("[exception][current=%s][active=%s][lastActive=%s]%s", currentMemberId, activeMemberId, lastActiveMemberId,
                    SOSHibernate.toString(item)));
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
            if (!skipPerform) {
                if (config.currentIsClusterMember()) {
                    if (item != null) {
                        JocClusterAnswer answer = performHandlers(item.getMemberId());
                        if (answer.getError() != null) {
                            LOGGER.error(SOSString.toString(answer));
                        }
                    }
                }
            }
            postEvents();
            lastActiveMemberId = activeMemberId;
        }
    }

    private void postEvents() {
        if (activeMemberId != null) {
            if (lastActiveMemberId == null || !lastActiveMemberId.equals(activeMemberId)) {
                try {
                    ActiveClusterChangedEvent event = new ActiveClusterChangedEvent();
                    event.setOldClusterMemberId(lastActiveMemberId);
                    event.setNewClusterMemberId(activeMemberId);
                    LOGGER.info(String.format("[post]%s", SOSString.toString(event)));

                    EventBus.getInstance().post(event);
                } catch (Throwable e) {
                    LOGGER.error(e.toString(), e);
                }
            }
        }
    }

    private DBItemJocCluster handleCurrentMemberOnProcess(DBLayerJocCluster dbLayer, DBItemJocCluster item) throws Exception {
        if (item == null) {
            item = new DBItemJocCluster();
            item.setId(JocClusterConfiguration.IDENTIFIER);
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
                    LOGGER.info(String.format("[heartBeat exceeded][%s]%s", item.getHeartBeat(), item.getMemberId()));

                    boolean update = true;
                    // to avoid start of the current instance if a switchMember defined
                    if (item.getSwitchMemberId() != null && !item.getSwitchMemberId().equals(currentMemberId)) {
                        DBItemJocInstance switchInstance = getInstance(item.getSwitchMemberId());
                        if (switchInstance != null) {
                            if (!isHeartBeatExceeded(switchInstance.getHeartBeat())) {
                                LOGGER.info(String.format("[wait for switchMember]%s", switchInstance.getMemberId()));
                                update = false;
                            }
                        }
                    }

                    if (update) {
                        item.setMemberId(currentMemberId);
                        item.setSwitchMemberId(null);
                        item.setSwitchHeartBeat(null);

                        dbLayer.getSession().beginTransaction();
                        dbLayer.getSession().update(item);
                        dbLayer.getSession().commit();
                        LOGGER.info(String.format("[active changed]%s", SOSHibernate.toString(item)));
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug("not active");
                    }
                }
            }
        }
        return item;
    }

    public JocClusterAnswer switchMember(String newMemberId) {
        LOGGER.info("[switch][start]" + newMemberId);

        JocClusterAnswer answer = checkSwitchMember(newMemberId);
        if (answer != null) {
            LOGGER.info(String.format("[switch][end]%s", SOSString.toString(answer)));
            return answer;
        }

        try {
            DBLayerJocCluster dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession());
            synchronized (lockObject) {
                boolean run = true;
                int errorCounter = 0;
                while (run) {
                    if (closed) {
                        LOGGER.info("[switch][end][skip]because closed");
                        return getOKAnswer(JocClusterAnswerState.STOPPED);// TODO OK?
                    }

                    try {
                        answer = setSwitchMember(dbLayer, newMemberId);
                        run = false;
                    } catch (Exception e) {
                        LOGGER.warn(String.format("[%s]%s", errorCounter, e.toString()), e);
                        errorCounter += 1;
                        if (errorCounter >= config.getSwitchMemberWaitCounterOnError()) {
                            throw e;
                        }
                        wait(config.getSwitchMemberWaitIntervalOnError());
                    }
                }
            }
            return answer;
        } catch (Exception e) {
            return getErrorAnswer(e);
        }

    }

    private JocClusterAnswer checkSwitchMember(String newMemberId) {
        if (SOSString.isEmpty(newMemberId)) {
            return getErrorAnswer(new Exception("missing newMemberId"));
        }
        if (newMemberId.equals(currentMemberId)) {
            if (handler.isActive()) {
                return getOKAnswer(JocClusterAnswerState.ALREADY_STARTED);
            }
        } else {// check if exists
            try {
                if (activeMemberId != null && newMemberId.equals(activeMemberId)) {

                } else {
                    DBItemJocInstance switchInstance = getInstance(newMemberId);
                    if (switchInstance == null) {
                        return getErrorAnswer(new Exception(String.format("memberId=%s not found", newMemberId)));
                    }
                    if (isHeartBeatExceeded(switchInstance.getHeartBeat())) {
                        return getErrorAnswer(new Exception(String.format("[memberId=%s][last heart beat too old]%s", newMemberId, SOSDate
                                .getDateTimeAsString(switchInstance.getHeartBeat(), SOSDate.getDateTimeFormat()))));
                    }
                }
            } catch (Exception e) {
                return getErrorAnswer(e);
            }
        }
        return null;
    }

    private JocClusterAnswer setSwitchMember(DBLayerJocCluster dbLayer, String newMemberId) throws Exception {

        JocClusterAnswer answer = getOKAnswer(JocClusterAnswerState.SWITCH);
        try {
            dbLayer.getSession().beginTransaction();
            DBItemJocCluster item = dbLayer.getCluster();
            dbLayer.getSession().commit();
            if (item == null) {
                return getErrorAnswer(new Exception("db cluster not found"));
            }

            if (item.getMemberId().equals(currentMemberId)) {
                if (newMemberId.equals(currentMemberId)) {
                    // current is active - handled by checkSwitchMember
                    // current is not active - not possible (item.getMember() is an active instance)
                    LOGGER.info("[switch][end][skip][already active]currentMemberId=switch memberId");
                    answer = getOKAnswer(JocClusterAnswerState.ALREADY_STARTED);
                } else {
                    LOGGER.info("[switch][stop current]newMemberId=" + newMemberId);
                    if (handler.isActive()) {
                        handler.perform(PerformType.STOP);
                    }
                    item.setMemberId(newMemberId);
                    item.setSwitchMemberId(null);
                    item.setSwitchHeartBeat(null);

                    dbLayer.getSession().beginTransaction();
                    dbLayer.getSession().update(item);
                    dbLayer.getSession().commit();
                }
            } else {
                if (item.getMemberId().equals(newMemberId)) {
                    LOGGER.info("[switch][end][skip][already active]activeMemberId=switch memberId");
                    answer = getOKAnswer(JocClusterAnswerState.ALREADY_STARTED);
                } else {
                    if (item.getSwitchMemberId() == null || !item.getSwitchMemberId().equals(newMemberId)) {
                        // set switchMember because before "switch" the active cluster instance must be stopped
                        // and the current instance is not an active instance
                        item.setSwitchMemberId(newMemberId);
                        item.setSwitchHeartBeat(new Date());

                        dbLayer.getSession().beginTransaction();
                        dbLayer.getSession().update(item);
                        dbLayer.getSession().commit();
                        LOGGER.info("[switch]" + SOSHibernate.toString(item));
                    }
                }
            }

        } catch (Exception e) {
            if (dbLayer != null) {
                dbLayer.getSession().rollback();
            }
            throw e;
        }
        return answer;
    }

    private DBItemJocCluster trySwitchCurrentMemberOnProcess(DBLayerJocCluster dbLayer, DBItemJocCluster item) throws Exception {
        skipPerform = false;
        item.setMemberId(currentMemberId);

        if (item.getSwitchMemberId() != null) {
            boolean run = true;
            int errorCounter = 0;
            while (run) {
                if (closed) {
                    LOGGER.info("[trySwitchCurrentMemberOnProcess][skip]because closed");
                    return item;
                }
                try {
                    if (isHeartBeatExceeded(item.getSwitchHeartBeat())) {
                        LOGGER.info(String.format("[switch][skip][newMemberId=%s]switchHeartBeat=%s exceeded", item.getSwitchMemberId(), item
                                .getSwitchHeartBeat()));
                    } else {
                        LOGGER.info("[switch][stop current]newMemberId=" + item.getSwitchMemberId());
                        if (handler.isActive()) {
                            handler.perform(PerformType.STOP);
                        }
                        item.setMemberId(item.getSwitchMemberId());
                        skipPerform = true;
                    }
                    item.setSwitchMemberId(null);
                    item.setSwitchHeartBeat(null);

                    dbLayer.getSession().beginTransaction();
                    dbLayer.getSession().update(item);
                    dbLayer.getSession().commit();
                    run = false;
                } catch (Exception e) {
                    LOGGER.warn(String.format("[%s]%s", errorCounter, e.toString()), e);
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

    private JocClusterAnswer performHandlers(String memberId) {// TODO
        if (memberId.equals(currentMemberId)) {
            if (handler.isActive()) {
                return getOKAnswer(JocClusterAnswerState.STARTED);
            } else {
                return handler.perform(PerformType.START);
            }
        } else {
            if (handler.isActive()) {
                return handler.perform(PerformType.STOP);
            } else {
                return getOKAnswer(JocClusterAnswerState.STOPPED);
            }
        }
    }

    public void close(boolean deleteActiveCurrentMember) {
        LOGGER.info("[cluster][close]start ...----------------------------------------------");
        closed = true;
        httpClient.close();
        synchronized (httpClient) {
            httpClient.notifyAll();
        }
        closeHandlers();
        if (deleteActiveCurrentMember) {
            tryDeleteActiveCurrentMember();
        }
        LOGGER.info("[cluster][close]closed----------------------------------------------");
    }

    private JocClusterAnswer closeHandlers() {
        LOGGER.info("[cluster][closeHandlers][isActive=" + handler.isActive() + "]start ...");
        JocClusterAnswer answer = null;
        if (handler.isActive()) {
            answer = handler.perform(PerformType.STOP);
        } else {
            answer = getOKAnswer(JocClusterAnswerState.ALREADY_STOPPED);
        }
        LOGGER.info("[cluster][closeHandlers][isActive=" + handler.isActive() + "]closed");
        return answer;
    }

    private int tryDeleteActiveCurrentMember() {
        if (!instanceProcessed) {
            return 0;
        }
        DBLayerJocCluster dbLayer = null;
        int result = 0;
        try {
            dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession());
            dbLayer.getSession().beginTransaction();
            result = dbLayer.deleteCluster(currentMemberId, true);
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

    public static JocClusterAnswer getOKAnswer(JocClusterAnswerState state) {
        return new JocClusterAnswer(state);
    }

    public static JocClusterAnswer getErrorAnswer(JocClusterAnswerState state) {
        return getErrorAnswer(state, new Exception(state.toString()));
    }

    public static JocClusterAnswer getErrorAnswer(Exception e) {
        return getErrorAnswer(JocClusterAnswerState.ERROR, e);
    }

    public static JocClusterAnswer getErrorAnswer(JocClusterAnswerState state, Exception e) {
        JocClusterAnswer answer = new JocClusterAnswer(state);
        answer.setError(e);
        return answer;
    }

    public static void shutdownThreadPool(ExecutorService threadPool, long awaitTerminationTimeout) {
        String caller = SOSClassUtil.getMethodName(2);
        try {
            if (threadPool == null) {
                return;
            }
            threadPool.shutdown();
            // threadPool.shutdownNow();

            boolean shutdown = threadPool.awaitTermination(awaitTerminationTimeout, TimeUnit.SECONDS);
            if (shutdown) {
                LOGGER.info(String.format("[shutdown][%s]thread has been shut down correctly", caller));
            } else {
                LOGGER.info(String.format("[shutdown][%s]thread has ended due to timeout of %ss on shutdown", caller, awaitTerminationTimeout));
            }
        } catch (InterruptedException e) {
            LOGGER.error(String.format("[shutdown][%s][exception]%s", caller, e.toString()), e);
        }
    }

    public void wait(int interval) {
        if (!closed && interval > 0) {
            String method = "wait";
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s(%s) %ss ...", method, SOSClassUtil.getMethodName(2), interval));
            }
            try {
                synchronized (httpClient) {
                    httpClient.wait(interval * 1_000);
                }
            } catch (InterruptedException e) {
                if (closed) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("%ssleep interrupted due to handler close", method));
                    }
                } else {
                    LOGGER.warn(String.format("%s%s", method, e.toString()), e);
                }
            }
        }
    }

    public JocClusterConfiguration getConfig() {
        return config;
    }

    public JocConfiguration getJocConfig() {
        return jocConfig;
    }

    public JocClusterHandler getHandler() {
        return handler;
    }

}
