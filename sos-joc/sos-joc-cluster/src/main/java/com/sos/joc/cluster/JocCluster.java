package com.sos.joc.cluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.hibernate.query.Query;
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
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.db.DBLayerJocCluster;
import com.sos.joc.cluster.instances.JocInstance;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
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
    private final Object lockObject = new Object();
    private final String currentMemberId;
    private final Date jocStartTime;

    private volatile boolean closed;

    private List<ControllerConfiguration> controllers;
    private String activeMemberId;
    private String lastActiveMemberId;
    private boolean skipPerform;
    private boolean instanceProcessed;

    public JocCluster(final SOSHibernateFactory factory, final JocClusterConfiguration jocClusterConfiguration,
            final JocConfiguration jocConfiguration, final Date jocStartTime) {
        this.dbFactory = factory;
        this.config = jocClusterConfiguration;
        this.jocConfig = jocConfiguration;
        this.handler = new JocClusterHandler(this);
        this.httpClient = new HttpClient();
        this.currentMemberId = jocConfig.getMemberId();
        this.jocStartTime = jocStartTime;
    }

    public void doProcessing(StartupMode mode) {
        LOGGER.info(String.format("[inactive][current memberId]%s", currentMemberId));

        getInstance();

        while (!closed) {
            try {
                process(mode);

                wait(config.getPollingInterval());
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                wait(config.getPollingWaitIntervalOnError());
            }
        }
    }

    private void getInstance() {
        JocInstance instance = new JocInstance(dbFactory, jocConfig);

        instanceProcessed = false;
        while (!instanceProcessed) {
            try {
                if (closed) {
                    LOGGER.info("[getInstance][skip]because closed");
                    return;
                }
                instance.getInstance(jocStartTime);
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
                    LOGGER.info(String.format("[%s]no controllers found. sleep 1m and try again ...", jocConfig.getSecurityLevel().name()));
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
                StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_JS_INSTANCES).append(" ");
                hql.append("where securityLevel=:securityLevel");
                Query<DBItemInventoryJSInstance> query = session.createQuery(hql.toString());
                query.setParameter("securityLevel", jocConfig.getSecurityLevel().intValue());
                List<DBItemInventoryJSInstance> result = session.getResultList(query);
                session.commit();
                session.close();
                session = null;

                if (result != null && result.size() > 0) {
                    controllers = new ArrayList<ControllerConfiguration>();
                    Map<String, Properties> map = new HashMap<String, Properties>();
                    for (int i = 0; i < result.size(); i++) {
                        DBItemInventoryJSInstance item = result.get(i);

                        Properties p = null;
                        if (map.containsKey(item.getControllerId())) {
                            p = map.get(item.getControllerId());
                        } else {
                            p = new Properties();
                        }
                        p.setProperty("controller_id", item.getControllerId());
                        if (item.getIsPrimary()) {
                            p.setProperty("primary_controller_uri", item.getUri());
                            if (item.getClusterUri() != null) {
                                p.setProperty("primary_controller_cluster_uri", item.getClusterUri());
                            }
                        } else {
                            p.setProperty("secondary_controller_uri", item.getUri());
                            if (item.getClusterUri() != null) {
                                p.setProperty("secondary_controller_cluster_uri", item.getClusterUri());
                            }
                        }
                        map.put(item.getControllerId(), p);
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

    private synchronized void process(StartupMode mode) throws Exception {
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
                LOGGER.debug(String.format("[%s][start][current=%s][active=%s][lastActive=%s]%s", mode, currentMemberId, activeMemberId,
                        lastActiveMemberId, SOSHibernate.toString(item)));
            }

            skipPerform = false;
            synchronized (lockObject) {
                item = handleCurrentMemberOnProcess(mode, dbLayer, item);
                if (item != null) {
                    activeMemberId = item.getMemberId();
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][end][current=%s][active=%s][lastActive=%s]%s", mode, currentMemberId, activeMemberId,
                                lastActiveMemberId, SOSHibernate.toString(item)));
                    }
                    if (item.getStartupMode() != null) {
                        try {
                            mode = StartupMode.valueOf(item.getStartupMode());
                        } catch (Throwable e) {
                            LOGGER.warn(e.toString(), e);
                        }
                        item.setStartupMode(null);
                    }
                }
            }

        } catch (SOSHibernateObjectOperationStaleStateException e) {// @Version
            // locked
            if (dbLayer != null) {
                dbLayer.getSession().rollback();
            }
            LOGGER.error(e.toString(), e);
            LOGGER.error(String.format("[%s][exception][current=%s][active=%s][lastActive=%s][locked]%s", mode, currentMemberId, activeMemberId,
                    lastActiveMemberId, SOSHibernate.toString(item)));

        } catch (SOSHibernateObjectOperationException e) {
            if (dbLayer != null) {
                dbLayer.getSession().rollback();
            }
            LOGGER.error(e.toString(), e);
            LOGGER.error(String.format("[%s][exception][current=%s][active=%s][lastActive=%s]%s", mode, currentMemberId, activeMemberId,
                    lastActiveMemberId, SOSHibernate.toString(item)));
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
                if (item != null) {
                    JocClusterAnswer answer = performServices(mode, item.getMemberId());
                    if (answer.getError() != null) {
                        LOGGER.error(SOSString.toString(answer));
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

    private DBItemJocCluster handleCurrentMemberOnProcess(StartupMode mode, DBLayerJocCluster dbLayer, DBItemJocCluster item) throws Exception {
        if (item == null) {
            item = new DBItemJocCluster();
            item.setId(JocClusterConfiguration.IDENTIFIER);
            item.setMemberId(currentMemberId);

            dbLayer.getSession().beginTransaction();
            dbLayer.getSession().save(item);
            dbLayer.getSession().commit();

            if (!isAutomaticStartup(item.getHeartBeat())) {
                mode = StartupMode.automatic_switchover;
                item.setStartupMode(mode.name());
            }
        } else {
            if (item.getMemberId().equals(currentMemberId)) {
                item = trySwitchCurrentMemberOnProcess(mode, dbLayer, item);
            } else {
                if (isHeartBeatExceeded(item.getHeartBeat())) {
                    LOGGER.info(String.format("[%s][heartBeat exceeded][%s]%s", mode, item.getHeartBeat(), item.getMemberId()));

                    boolean update = true;
                    // to avoid start of the current instance if a switchMember defined
                    if (item.getSwitchMemberId() != null && !item.getSwitchMemberId().equals(currentMemberId)) {
                        DBItemJocInstance switchInstance = getInstance(item.getSwitchMemberId());
                        if (switchInstance != null) {
                            if (!isHeartBeatExceeded(switchInstance.getHeartBeat())) {
                                LOGGER.info(String.format("[%s][wait for switchMember]%s", mode, switchInstance.getMemberId()));
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

                        mode = StartupMode.automatic_switchover;
                        item.setStartupMode(mode.name());
                        LOGGER.info(String.format("[%s][active changed]%s", mode, SOSHibernate.toString(item)));
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug("[" + mode + "]not active");
                    }
                }
            }
        }
        return item;
    }

    public JocClusterAnswer switchMember(StartupMode mode, String newMemberId) {
        LOGGER.info(String.format("[%s][switch][start][new]%s", mode, newMemberId));

        JocClusterAnswer answer = checkSwitchMember(newMemberId);
        if (answer != null) {
            LOGGER.info(String.format("[%s][switch][end]%s", mode, SOSString.toString(answer)));
            return answer;
        }

        try {
            DBLayerJocCluster dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession());
            synchronized (lockObject) {
                boolean run = true;
                int errorCounter = 0;
                while (run) {
                    if (closed) {
                        LOGGER.info("[" + mode + "][switch][end][skip]because closed");
                        return getOKAnswer(JocClusterAnswerState.STOPPED);
                    }

                    try {
                        answer = setSwitchMember(mode, dbLayer, newMemberId);
                        run = false;
                    } catch (Exception e) {
                        LOGGER.warn(String.format("[%s][%s]%s", mode, errorCounter, e.toString()), e);
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

    private JocClusterAnswer setSwitchMember(StartupMode mode, DBLayerJocCluster dbLayer, String newMemberId) throws Exception {
        mode = StartupMode.manual_switchover;

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
                    LOGGER.info("[" + mode + "][switch][end][skip][already active]currentMemberId=switch memberId");
                    answer = getOKAnswer(JocClusterAnswerState.ALREADY_STARTED);
                } else {
                    if (handler.isActive()) {
                        LOGGER.info("[" + mode + "][switch][start][stop current]" + currentMemberId);
                        handler.perform(mode, PerformType.STOP);
                    }
                    item.setMemberId(newMemberId);
                    // item.setSwitchMemberId(null);
                    // item.setSwitchHeartBeat(null);

                    item.setSwitchMemberId(newMemberId);
                    item.setSwitchHeartBeat(new Date());

                    dbLayer.getSession().beginTransaction();
                    dbLayer.getSession().update(item);
                    dbLayer.getSession().commit();

                    LOGGER.info("[" + mode + "][switch][end][new]" + newMemberId);
                    activeMemberId = newMemberId;
                    postEvents();
                    lastActiveMemberId = activeMemberId;
                }
            } else {
                if (item.getMemberId().equals(newMemberId)) {
                    LOGGER.info("[" + mode + "][switch][end][skip][already active]activeMemberId=switch memberId");
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
                        LOGGER.info("[" + mode + "][switch]" + SOSHibernate.toString(item));
                        // watchSwitch(newMemberId);
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

    private DBItemJocCluster trySwitchCurrentMemberOnProcess(StartupMode mode, DBLayerJocCluster dbLayer, DBItemJocCluster item) throws Exception {
        skipPerform = false;
        item.setMemberId(currentMemberId);

        if (item.getSwitchMemberId() != null) {
            mode = StartupMode.manual_switchover;
            item.setStartupMode(mode.name());

            if (item.getSwitchMemberId().equals(currentMemberId)) {
                item.setSwitchMemberId(null);
                item.setSwitchHeartBeat(null);

                dbLayer.getSession().beginTransaction();
                dbLayer.getSession().update(item);
                dbLayer.getSession().commit();
            } else {
                boolean run = true;
                int errorCounter = 0;
                while (run) {
                    if (closed) {
                        LOGGER.info("[" + mode + "][trySwitchCurrentMemberOnProcess][skip]because closed");
                        return item;
                    }
                    try {
                        if (isHeartBeatExceeded(item.getSwitchHeartBeat())) {
                            LOGGER.info(String.format("[%s][switch][skip][newMemberId=%s]switchHeartBeat=%s exceeded", mode, item.getSwitchMemberId(),
                                    item.getSwitchHeartBeat()));
                        } else {
                            LOGGER.info("[" + mode + "][switch][stop current]newMemberId=" + item.getSwitchMemberId());
                            if (handler.isActive()) {
                                handler.perform(mode, PerformType.STOP);
                            }
                            item.setMemberId(item.getSwitchMemberId());
                            skipPerform = true;
                        }
                        // item.setSwitchMemberId(null);
                        // item.setSwitchHeartBeat(null);

                        dbLayer.getSession().beginTransaction();
                        dbLayer.getSession().update(item);
                        dbLayer.getSession().commit();
                        run = false;
                    } catch (Exception e) {
                        LOGGER.warn(String.format("[%s][%s]%s", mode, errorCounter, e.toString()), e);
                        errorCounter += 1;
                        if (errorCounter >= config.getSwitchMemberWaitCounterOnError()) {
                            throw e;
                        }
                        wait(config.getSwitchMemberWaitIntervalOnError());
                    }
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

    private boolean isAutomaticStartup(Date heartBeat) {
        if (((heartBeat.getTime() / 1_000) - (jocStartTime.getTime() / 1_000)) < config.getPollingInterval()) {
            return true;
        }
        return false;
    }

    private JocClusterAnswer performServices(StartupMode mode, String memberId) {
        if (memberId.equals(currentMemberId)) {
            if (handler.isActive()) {
                return getOKAnswer(JocClusterAnswerState.ALREADY_STARTED);
            } else {
                return handler.perform(mode, PerformType.START);
            }
        } else {
            if (handler.isActive()) {
                return handler.perform(mode, PerformType.STOP);
            } else {
                return getOKAnswer(JocClusterAnswerState.ALREADY_STOPPED);
            }
        }
    }

    public void close(StartupMode mode, boolean deleteActiveCurrentMember) {
        LOGGER.info("[" + mode + "][cluster][close]start ...----------------------------------------------");
        closed = true;
        httpClient.close();
        synchronized (httpClient) {
            httpClient.notifyAll();
        }
        closeServices(mode);
        if (deleteActiveCurrentMember) {
            tryDeleteActiveCurrentMember();
        }
        LOGGER.info("[" + mode + "][cluster][close]closed----------------------------------------------");
    }

    private JocClusterAnswer closeServices(StartupMode mode) {
        LOGGER.info("[" + mode + "][cluster][closeServices][isActive=" + handler.isActive() + "]start ...");
        JocClusterAnswer answer = null;
        if (handler.isActive()) {
            answer = handler.perform(mode, PerformType.STOP);
        } else {
            answer = getOKAnswer(JocClusterAnswerState.ALREADY_STOPPED);
        }
        LOGGER.info("[" + mode + "][cluster][closeServices][isActive=" + handler.isActive() + "]closed");
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

    public static void shutdownThreadPool(StartupMode mode, ExecutorService threadPool, long awaitTerminationTimeout) {
        String caller = SOSClassUtil.getMethodName(2);
        String logMode = mode == null ? "" : "[" + mode + "]";
        try {
            if (threadPool == null) {
                return;
            }
            threadPool.shutdown();// Disable new tasks from being submitted
            // Wait a while for existing tasks to terminate
            if (threadPool.awaitTermination(awaitTerminationTimeout, TimeUnit.SECONDS)) {
                LOGGER.info(String.format("%s[shutdown][%s]thread pool has been shut down correctly", logMode, caller));
            } else {
                threadPool.shutdownNow();// Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
                    LOGGER.info(String.format("%s[shutdown][%s]thread pool has ended due to timeout of %ss on shutdown", logMode, caller,
                            awaitTerminationTimeout));
                } else {
                    LOGGER.info(String.format("%s[shutdown][%s]thread pool did not terminate due to timeout of %ss on shutdown", logMode, caller,
                            awaitTerminationTimeout));
                }
            }
        } catch (InterruptedException e) {
            // (Re-)Cancel if current thread also interrupted
            LOGGER.error(String.format("%s[shutdown][%s][exception]%s", logMode, caller, e.toString()), e);
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
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
