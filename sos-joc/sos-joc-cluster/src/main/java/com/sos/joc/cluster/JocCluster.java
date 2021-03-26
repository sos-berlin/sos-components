package com.sos.joc.cluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
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
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.db.DBLayerJocCluster;
import com.sos.joc.cluster.instances.JocInstance;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.joc.event.bean.configuration.ConfigurationGlobalsChanged;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.configuration.globals.GlobalSettings;

public class JocCluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocCluster.class);

    public static final int MAX_AWAIT_TERMINATION_TIMEOUT = 30;
    private static Date jocStartTime = null;

    private final SOSHibernateFactory dbFactory;
    private final JocClusterConfiguration config;
    private final JocConfiguration jocConfig;
    private final JocClusterHandler handler;
    private final Object lock = new Object();
    private final Object lockMember = new Object();
    private final String currentMemberId;

    private volatile boolean closed;

    private List<ControllerConfiguration> controllers;
    private AtomicReference<List<String>> notification;
    private static String jocTimeZone = null;
    private String activeMemberId;
    private String lastActiveMemberId;
    private boolean skipPerform;
    private boolean instanceProcessed;

    public JocCluster(final SOSHibernateFactory factory, final JocClusterConfiguration jocClusterConfiguration,
            final JocConfiguration jocConfiguration, final Date jocStartDateTime) {
        this.dbFactory = factory;
        this.config = jocClusterConfiguration;
        this.jocConfig = jocConfiguration;
        this.handler = new JocClusterHandler(this);
        this.currentMemberId = jocConfig.getMemberId();
        jocStartTime = jocStartDateTime;
    }

    public ConfigurationGlobals getConfigurationGlobals(StartupMode mode) {
        return getInstance(mode);
    }

    public void doProcessing(StartupMode mode, ConfigurationGlobals configurations) {
        LOGGER.info(String.format("[inactive][current memberId]%s", currentMemberId));

        while (!closed) {
            try {
                process(mode, configurations);

                waitFor(config.getPollingInterval());
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                waitFor(config.getPollingWaitIntervalOnError());
            }
        }
    }

    private ConfigurationGlobals getInstance(StartupMode mode) {
        JocInstance instance = new JocInstance(dbFactory, jocConfig);

        instanceProcessed = false;
        ConfigurationGlobals configurations = null;
        while (!instanceProcessed) {
            try {
                if (closed) {
                    LOGGER.info("[getInstance][skip]because closed");
                    return configurations;
                }
                instance.getInstance(mode, jocStartTime);
                jocTimeZone = jocConfig.getTimeZone();
                configurations = getStoredSettings();
                instanceProcessed = true;
            } catch (Throwable e) {
                LOGGER.error(e.toString());
                LOGGER.info("wait 30s and try again ...");
                waitFor(30);
            }
        }
        return configurations;
    }

    public void readCurrentDbInfos() {
        boolean run = true;
        controllers = null;
        while (run) {
            DBLayerJocCluster dbLayer = null;
            try {
                if (closed) {
                    return;
                }
                dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession("currentDbInfos"));
                readCurrentDbInfoControllers(dbLayer);
                if (controllers != null && controllers.size() > 0) {
                    run = false;
                    dbLayer.close();
                    dbLayer = null;
                } else {
                    dbLayer.close();
                    dbLayer = null;
                    LOGGER.info(String.format("[%s]no controllers found. sleep 1m and try again ...", jocConfig.getSecurityLevel().name()));
                    waitFor(60);
                }
            } catch (Exception e) {
                if (dbLayer != null) {
                    dbLayer.rollback();
                    dbLayer.close();
                    dbLayer = null;
                }
                LOGGER.error(String.format("[error occured][sleep 1m and try again ...]%s", e.toString()));
                waitFor(60);
            } finally {
                if (dbLayer != null) {
                    dbLayer.close();
                    dbLayer = null;
                }
            }
        }
    }

    private void readCurrentDbInfoControllers(DBLayerJocCluster dbLayer) throws Exception {
        if (controllers == null) {
            dbLayer.beginTransaction();
            StringBuilder hql = new StringBuilder("from ").append(DBLayer.DBITEM_INV_JS_INSTANCES).append(" ");
            hql.append("where securityLevel=:securityLevel");
            Query<DBItemInventoryJSInstance> query = dbLayer.getSession().createQuery(hql.toString());
            query.setParameter("securityLevel", jocConfig.getSecurityLevel().intValue());
            List<DBItemInventoryJSInstance> result = dbLayer.getSession().getResultList(query);
            dbLayer.commit();

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
        }
    }

    private ConfigurationGlobals getStoredSettings() throws Exception {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        ConfigurationGlobals configurations = new ConfigurationGlobals();
        GlobalSettings settings = null;

        DBLayerJocCluster dbLayer = null;
        try {
            dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession("stored_settings"));
            dbLayer.beginTransaction();
            DBItemJocConfiguration item = dbLayer.getGlobalsSettings();
            if (item == null) {
                settings = configurations.getInitialSettings(jocTimeZone);
                try {
                    item = new DBItemJocConfiguration();
                    item.setControllerId(ConfigurationGlobals.CONTROLLER_ID);
                    item.setInstanceId(ConfigurationGlobals.INSTANCE_ID);
                    item.setAccount(ConfigurationGlobals.ACCOUNT);
                    item.setShared(ConfigurationGlobals.SHARED);
                    item.setObjectType(ConfigurationGlobals.OBJECT_TYPE == null ? null : ConfigurationGlobals.OBJECT_TYPE.name());
                    item.setConfigurationType(ConfigurationType.GLOBALS.name());
                    item.setConfigurationItem(mapper.writeValueAsString(settings));
                    item.setModified(new Date());

                    dbLayer.getSession().save(item);
                } catch (Throwable e) {
                    dbLayer.rollback();
                    waitFor(5);

                    dbLayer.beginTransaction();
                    item = dbLayer.getGlobalsSettings();
                }
            }
            if (item != null) {
                if (!SOSString.isEmpty(item.getConfigurationItem())) {
                    if (!item.getConfigurationItem().equals(ConfigurationGlobals.DEFAULT_CONFIGURATION_ITEM)) {
                        try {
                            settings = mapper.readValue(item.getConfigurationItem(), GlobalSettings.class);
                        } catch (Throwable e) {
                            LOGGER.error(String.format("[can't map stored settings][%s]%s", item.getConfigurationItem(), e.toString()), e);
                            LOGGER.info("store and use default settings ...");
                            settings = configurations.getInitialSettings(jocTimeZone);

                            item.setConfigurationItem(mapper.writeValueAsString(settings));
                            item.setModified(new Date());
                            dbLayer.getSession().update(item);
                        }
                    }
                }
            }
            dbLayer.commit();

            if (settings == null) {
                settings = configurations.getInitialSettings(jocTimeZone);
            }

        } catch (SOSHibernateException e) {
            dbLayer.rollback();
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
        configurations.setConfigurationValues(settings);
        return configurations;
    }

    private synchronized void process(StartupMode mode, ConfigurationGlobals configurations) throws Exception {
        DBLayerJocCluster dbLayer = null;
        DBItemJocCluster item = null;
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        try {
            dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession());
            dbLayer.beginTransaction();
            dbLayer.updateInstanceHeartBeat(currentMemberId);
            item = dbLayer.getCluster();
            dbLayer.commit();

            activeMemberId = item == null ? null : item.getMemberId();
            if (lastActiveMemberId == null) {
                lastActiveMemberId = activeMemberId;
            }
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][start][current=%s][active=%s][lastActive=%s]%s", mode, currentMemberId, activeMemberId,
                        lastActiveMemberId, SOSHibernate.toString(item)));
            }

            skipPerform = false;
            synchronized (lockMember) {
                item = handleCurrentMemberOnProcess(mode, dbLayer, item, configurations);
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
                dbLayer.rollback();
            }
            LOGGER.error(e.toString(), e);
            LOGGER.error(String.format("[%s][exception][current=%s][active=%s][lastActive=%s][locked]%s", mode, currentMemberId, activeMemberId,
                    lastActiveMemberId, SOSHibernate.toString(item)));

        } catch (SOSHibernateObjectOperationException e) {
            if (dbLayer != null) {
                dbLayer.rollback();
            }
            LOGGER.error(e.toString(), e);
            LOGGER.error(String.format("[%s][exception][current=%s][active=%s][lastActive=%s]%s", mode, currentMemberId, activeMemberId,
                    lastActiveMemberId, SOSHibernate.toString(item)));
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            if (dbLayer != null) {
                dbLayer.rollback();
            }
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
            if (!skipPerform) {
                if (item != null) {
                    JocClusterAnswer answer = performServices(mode, configurations, item.getMemberId());
                    if (answer.getError() != null) {
                        LOGGER.error(SOSString.toString(answer));
                    }
                }
            }
            postActiveClusterChangedEvent();
            lastActiveMemberId = activeMemberId;
        }
    }

    private void postActiveClusterChangedEvent() {
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

    private DBItemJocCluster handleCurrentMemberOnProcess(StartupMode mode, DBLayerJocCluster dbLayer, DBItemJocCluster item,
            ConfigurationGlobals configurations) throws Exception {
        if (item == null) {
            item = new DBItemJocCluster();
            item.setId(JocClusterConfiguration.IDENTIFIER);
            item.setMemberId(currentMemberId);

            dbLayer.beginTransaction();
            dbLayer.getSession().save(item);
            dbLayer.commit();

            if (!isAutomaticStartup(item.getHeartBeat())) {
                mode = StartupMode.automatic_switchover;
                item.setStartupMode(mode.name());
            }
        } else {
            if (item.getMemberId().equals(currentMemberId)) {
                item = trySwitchActiveMemberOnProcess(mode, dbLayer, item, configurations);
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

                        dbLayer.beginTransaction();
                        dbLayer.getSession().update(activeMemberHandleNotification(item));
                        dbLayer.commit();

                        mode = StartupMode.automatic_switchover;
                        item.setStartupMode(mode.name());
                        LOGGER.info(String.format("[%s][active changed]%s", mode, SOSHibernate.toString(item)));
                    }
                } else {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("[" + mode + "]not active");
                    }
                    nonActiveMemberHandleNotification(item);
                }
            }
        }
        return item;
    }

    public JocClusterAnswer switchMember(StartupMode mode, ConfigurationGlobals configurations, String newMemberId) {
        LOGGER.info(String.format("[%s][switch][start][new]%s", mode, newMemberId));

        JocClusterAnswer answer = checkSwitchMember(newMemberId);
        if (answer != null) {
            LOGGER.info(String.format("[%s][switch][end]%s", mode, SOSString.toString(answer)));
            return answer;
        }

        try {
            synchronized (lockMember) {
                boolean run = true;
                int errorCounter = 0;
                while (run) {
                    if (closed) {
                        LOGGER.info("[" + mode + "][switch][end][skip]because closed");
                        return getOKAnswer(JocClusterAnswerState.STOPPED);
                    }

                    DBLayerJocCluster dbLayer = null;
                    try {
                        dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession());
                        answer = setSwitchMember(mode, dbLayer, configurations, newMemberId);
                        run = false;

                        dbLayer.close();
                        dbLayer = null;
                    } catch (Exception e) {
                        if (dbLayer != null) {
                            dbLayer.close();
                            dbLayer = null;
                        }
                        LOGGER.warn(String.format("[%s][%s]%s", mode, errorCounter, e.toString()), e);
                        errorCounter += 1;
                        if (errorCounter >= config.getSwitchMemberWaitCounterOnError()) {
                            throw e;
                        }
                        waitFor(config.getSwitchMemberWaitIntervalOnError());
                    } finally {
                        if (dbLayer != null) {
                            dbLayer.close();
                            dbLayer = null;
                        }
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

    private JocClusterAnswer setSwitchMember(StartupMode mode, DBLayerJocCluster dbLayer, ConfigurationGlobals configurations, String newMemberId)
            throws Exception {
        mode = StartupMode.manual_switchover;

        JocClusterAnswer answer = getOKAnswer(JocClusterAnswerState.SWITCH);
        try {
            dbLayer.beginTransaction();
            DBItemJocCluster item = dbLayer.getCluster();
            dbLayer.commit();
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
                        handler.perform(mode, PerformType.STOP, configurations);
                    }
                    item.setMemberId(newMemberId);
                    // item.setSwitchMemberId(null);
                    // item.setSwitchHeartBeat(null);

                    item.setSwitchMemberId(newMemberId);
                    item.setSwitchHeartBeat(new Date());

                    dbLayer.beginTransaction();
                    dbLayer.getSession().update(item);
                    dbLayer.commit();

                    LOGGER.info("[" + mode + "][switch][end][new]" + newMemberId);
                    activeMemberId = newMemberId;
                    postActiveClusterChangedEvent();
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

                        dbLayer.beginTransaction();
                        dbLayer.getSession().update(item);
                        dbLayer.commit();
                        LOGGER.info("[" + mode + "][switch]" + SOSHibernate.toString(item));
                        // watchSwitch(newMemberId);
                    }
                }
            }

        } catch (Exception e) {
            if (dbLayer != null) {
                dbLayer.rollback();
            }
            throw e;
        }
        return answer;
    }

    private DBItemJocCluster trySwitchActiveMemberOnProcess(StartupMode mode, DBLayerJocCluster dbLayer, DBItemJocCluster item,
            ConfigurationGlobals configurations) throws Exception {
        skipPerform = false;
        item.setMemberId(currentMemberId);

        if (item.getSwitchMemberId() != null) {
            mode = StartupMode.manual_switchover;
            item.setStartupMode(mode.name());

            if (item.getSwitchMemberId().equals(currentMemberId)) {
                item.setSwitchMemberId(null);
                item.setSwitchHeartBeat(null);

                dbLayer.beginTransaction();
                dbLayer.getSession().update(item);
                dbLayer.commit();
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
                                handler.perform(mode, PerformType.STOP, configurations);
                            }
                            item.setMemberId(item.getSwitchMemberId());
                            skipPerform = true;
                        }
                        // item.setSwitchMemberId(null);
                        // item.setSwitchHeartBeat(null);

                        dbLayer.beginTransaction();
                        dbLayer.getSession().update(item);
                        dbLayer.commit();
                        run = false;
                    } catch (Exception e) {
                        LOGGER.warn(String.format("[%s][%s]%s", mode, errorCounter, e.toString()), e);
                        errorCounter += 1;
                        if (errorCounter >= config.getSwitchMemberWaitCounterOnError()) {
                            throw e;
                        }
                        waitFor(config.getSwitchMemberWaitIntervalOnError());
                    }
                }
            }
        } else {
            dbLayer.beginTransaction();
            dbLayer.getSession().update(activeMemberHandleNotification(item));
            dbLayer.commit();
        }
        return item;
    }

    private void nonActiveMemberHandleNotification(DBItemJocCluster item) {
        if (!SOSString.isEmpty(item.getNotification()) && !currentMemberId.equals(item.getNotificationMemberId())) {
            JocClusterNotification n = new JocClusterNotification(config.getPollingInterval());
            n.parse(item.getNotification());
            if (!n.isExpired()) {
                EventBus.getInstance().post(new ConfigurationGlobalsChanged("", ConfigurationType.GLOBALS.name(), n.getSections()));
            }
        }
    }

    private DBItemJocCluster activeMemberHandleNotification(DBItemJocCluster item) {
        // current is an active handler, write/read notification if exists
        JocClusterNotification n = new JocClusterNotification(config.getPollingInterval());
        if (item.getNotification() != null) {
            n.parse(item.getNotification());
            if (n.isExpired()) {
                item.setNotificationMemberId(null);
                item.setNotification(null);
            }
        }
        if (notification != null) {
            item.setNotificationMemberId(item.getMemberId());
            item.setNotification(n.toString(notification));
        }
        notification = null;
        return item;
    }

    private DBItemJocInstance getInstance(String memberId) throws Exception {
        DBLayerJocCluster dbLayer = null;
        try {
            dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession());
            dbLayer.beginTransaction();
            DBItemJocInstance item = dbLayer.getInstance(memberId);
            dbLayer.commit();
            return item;
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
            if (dbLayer != null) {
                dbLayer.rollback();
            }
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
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

    private JocClusterAnswer performServices(StartupMode mode, ConfigurationGlobals configurations, String memberId) {
        if (memberId.equals(currentMemberId)) {
            if (handler.isActive()) {
                return getOKAnswer(JocClusterAnswerState.ALREADY_STARTED);
            } else {
                return handler.perform(mode, PerformType.START, configurations);
            }
        } else {
            if (handler.isActive()) {
                return handler.perform(mode, PerformType.STOP, configurations);
            } else {
                return getOKAnswer(JocClusterAnswerState.ALREADY_STOPPED);
            }
        }
    }

    public void close(StartupMode mode, ConfigurationGlobals configurations, boolean deleteActiveCurrentMember) {
        LOGGER.info("[" + mode + "][cluster][close]start ...----------------------------------------------");
        closed = true;
        synchronized (lock) {
            lock.notifyAll();
        }
        synchronized (lockMember) {
            lockMember.notifyAll();
        }
        closeServices(mode, configurations);
        if (deleteActiveCurrentMember) {
            tryDeleteActiveCurrentMember();
        }
        LOGGER.info("[" + mode + "][cluster][close]closed----------------------------------------------");
    }

    private JocClusterAnswer closeServices(StartupMode mode, ConfigurationGlobals configurations) {
        LOGGER.info("[" + mode + "][cluster][closeServices][isActive=" + handler.isActive() + "]start ...");
        JocClusterAnswer answer = null;
        if (handler.isActive()) {
            answer = handler.perform(mode, PerformType.STOP, configurations);
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
            dbLayer.beginTransaction();
            result = dbLayer.deleteCluster(currentMemberId, true);
            dbLayer.commit();

            if (Math.abs(result) > 0) {
                LOGGER.info(String.format("[active current memberId deleted]%s", currentMemberId));
            }
        } catch (SOSHibernateObjectOperationStaleStateException e) {// @Version
            // ignore exceptions - not me
            LOGGER.error(e.toString(), e);
            if (dbLayer != null) {
                dbLayer.rollback();
            }
        } catch (Exception e) {
            LOGGER.error(e.toString(), e);

            if (dbLayer != null) {
                dbLayer.rollback();
            }
        }
        return result;
    }

    public static JocClusterAnswer getOKAnswer(JocClusterAnswerState state) {
        return new JocClusterAnswer(state);
    }

    public static JocClusterAnswer getOKAnswer(JocClusterAnswerState state, String message) {
        return new JocClusterAnswer(state, message);
    }

    public static JocClusterAnswer getErrorAnswer(String msg) {
        return getErrorAnswer(JocClusterAnswerState.ERROR, new Exception(msg));
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

    public void waitFor(int interval) {
        if (!closed && interval > 0) {
            boolean isDebugEnabled = LOGGER.isDebugEnabled();
            String method = "wait";
            if (isDebugEnabled) {
                LOGGER.debug(String.format("%s(%s) %ss ...", method, SOSClassUtil.getMethodName(2), interval));
            }
            try {
                synchronized (lock) {
                    lock.wait(interval * 1_000);
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

    public List<ControllerConfiguration> getControllers() {
        return controllers;
    }

    public void setNotification(AtomicReference<List<String>> val) {
        notification = val;
    }

    public static Date getJocStartTime() {
        return jocStartTime;
    }

}
