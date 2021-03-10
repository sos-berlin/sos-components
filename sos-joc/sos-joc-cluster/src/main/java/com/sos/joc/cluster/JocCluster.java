package com.sos.joc.cluster;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.hibernate.query.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
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
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocClusterGlobalSettings;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.db.DBLayerJocCluster;
import com.sos.joc.cluster.instances.JocInstance;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.joc.event.bean.configuration.ConfigurationGlobalsChanged;
import com.sos.joc.model.cluster.common.ClusterServices;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.configuration.globals.GlobalSettings;
import com.sos.joc.model.configuration.globals.GlobalSettingsSection;

public class JocCluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocCluster.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private final static long SUBMIT_SETTING_CHANGES = TimeUnit.MINUTES.toMillis(1);

    public static final int MAX_AWAIT_TERMINATION_TIMEOUT = 30;

    private final SOSHibernateFactory dbFactory;
    private final JocClusterConfiguration config;
    private final JocConfiguration jocConfig;
    private final JocClusterHandler handler;
    private final Object lock = new Object();
    private final Object lockMember = new Object();
    private final String currentMemberId;
    private final Date jocStartTime;

    private volatile boolean closed;

    private List<ControllerConfiguration> controllers;
    private GlobalSettings settings;
    private static String jocTimeZone = null;
    private Timer settingsChangedTimer;
    private AtomicReference<List<String>> settingsChanged = new AtomicReference<List<String>>();
    private final Object lockSettings = new Object();
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
        this.currentMemberId = jocConfig.getMemberId();
        this.jocStartTime = jocStartTime;

        EventBus.getInstance().register(this);
    }

    public void doProcessing(StartupMode mode) {
        LOGGER.info(String.format("[inactive][current memberId]%s", currentMemberId));

        getInstance();

        while (!closed) {
            try {
                process(mode);

                waitFor(config.getPollingInterval());
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                waitFor(config.getPollingWaitIntervalOnError());
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
                jocTimeZone = jocConfig.getTimeZone();
                instanceProcessed = true;
            } catch (Throwable e) {
                LOGGER.error(e.toString());
                LOGGER.info("wait 30s and try again ...");
                waitFor(30);
            }
        }
    }

    public void readCurrentDbInfos() {
        boolean run = true;
        controllers = null;
        settings = null;
        while (run) {
            DBLayerJocCluster dbLayer = null;
            try {
                if (closed) {
                    return;
                }
                dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession("currentDbInfos"));
                readCurrentDbInfoControllers(dbLayer);
                readCurrentDbInfoStoredSettings(dbLayer);
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

    private void readCurrentDbInfoStoredSettings(DBLayerJocCluster dbLayer) throws Exception {
        settings = getStoredSettings(dbLayer, null);
    }

    @SuppressWarnings("unused")
    private static GlobalSettings getStoredSettings(SOSHibernateSession session) throws Exception {
        return getStoredSettings(null, session);
    }

    public static GlobalSettingsSection getStoredSettings(SOSHibernateSession session, ClusterServices service) throws Exception {
        GlobalSettings settings = getStoredSettings(null, session);
        return settings == null ? null : settings.getAdditionalProperties().get(service.name());
    }

    @SuppressWarnings("unused")
    private GlobalSettingsSection getStoredSettings(ClusterServices service) throws Exception {
        return getStoredSettings(service.name());
    }

    protected GlobalSettingsSection getStoredSettings(String serviceIdentifier) throws Exception {
        DBLayerJocCluster dbLayer = null;
        try {
            dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession("stored_settings_" + serviceIdentifier));
            GlobalSettings settings = getStoredSettings(dbLayer, null);
            dbLayer.close();
            dbLayer = null;
            return settings == null ? null : settings.getAdditionalProperties().get(serviceIdentifier);
        } catch (Throwable e) {
            throw e;
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
    }

    private static GlobalSettings getStoredSettings(DBLayerJocCluster dbLayer, SOSHibernateSession session) throws Exception {
        ObjectMapper mapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false).configure(
                SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

        GlobalSettings settings = null;
        if (dbLayer == null) {
            dbLayer = new DBLayerJocCluster(session);
        }

        try {
            dbLayer.beginTransaction();
            DBItemJocConfiguration item = dbLayer.getGlobalsSettings();
            if (item == null) {
                settings = getInitialSettings();

                item = new DBItemJocConfiguration();
                item.setControllerId(JocClusterGlobalSettings.CONTROLLER_ID);
                item.setInstanceId(JocClusterGlobalSettings.INSTANCE_ID);
                item.setAccount(JocClusterGlobalSettings.ACCOUNT);
                item.setShared(JocClusterGlobalSettings.SHARED);
                item.setObjectType(JocClusterGlobalSettings.OBJECT_TYPE == null ? null : JocClusterGlobalSettings.OBJECT_TYPE.name());
                item.setConfigurationType(ConfigurationType.GLOBALS.name());
                item.setConfigurationItem(mapper.writeValueAsString(settings));
                item.setModified(new Date());

                dbLayer.getSession().save(item);
            } else {
                if (!SOSString.isEmpty(item.getConfigurationItem())) {
                    if (!item.getConfigurationItem().equals(JocClusterGlobalSettings.DEFAULT_CONFIGURATION_ITEM)) {
                        try {
                            settings = mapper.readValue(item.getConfigurationItem(), GlobalSettings.class);
                        } catch (Throwable e) {
                            LOGGER.error(String.format("[can't map stored settings][%s]%s", item.getConfigurationItem(), e.toString()), e);
                            LOGGER.info("store and use default settings ...");
                            settings = getInitialSettings();
                            item.setConfigurationItem(mapper.writeValueAsString(settings));
                            item.setModified(new Date());
                            dbLayer.getSession().update(item);
                        }
                    }
                }
            }
            dbLayer.commit();
        } catch (SOSHibernateException e) {
            dbLayer.rollback();
        }
        return settings == null ? new GlobalSettings() : JocClusterGlobalSettings.addDefaultInfos(settings);
    }

    private static GlobalSettings getInitialSettings() {
        GlobalSettings settings = JocClusterGlobalSettings.getDefaultSettings();
        JocClusterGlobalSettings.useAndRemoveDefaultInfos(settings);
        JocClusterGlobalSettings.setCleanupInitialPeriod(settings);

        if (!SOSString.isEmpty(jocTimeZone)) {
            JocClusterGlobalSettings.setCleanupInitialTimeZone(settings, jocTimeZone);
            JocClusterGlobalSettings.setDailyPlanInitialTimeZone(settings, jocTimeZone);
        }
        return settings;
    }

    private synchronized void process(StartupMode mode) throws Exception {
        DBLayerJocCluster dbLayer = null;
        DBItemJocCluster item = null;
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
                    JocClusterAnswer answer = performServices(mode, item.getMemberId());
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

    private DBItemJocCluster handleCurrentMemberOnProcess(StartupMode mode, DBLayerJocCluster dbLayer, DBItemJocCluster item) throws Exception {
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

                        dbLayer.beginTransaction();
                        dbLayer.getSession().update(item);
                        dbLayer.commit();

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
                        answer = setSwitchMember(mode, dbLayer, newMemberId);
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

    private JocClusterAnswer setSwitchMember(StartupMode mode, DBLayerJocCluster dbLayer, String newMemberId) throws Exception {
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
                        handler.perform(mode, PerformType.STOP);
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

    private DBItemJocCluster trySwitchCurrentMemberOnProcess(StartupMode mode, DBLayerJocCluster dbLayer, DBItemJocCluster item) throws Exception {
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
                                handler.perform(mode, PerformType.STOP);
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
            dbLayer.getSession().update(item);
            dbLayer.commit();
        }
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
        synchronized (lock) {
            lock.notifyAll();
        }
        synchronized (lockMember) {
            lockMember.notifyAll();
        }
        stopSettingsChangedTimer();
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

    @Subscribe({ ConfigurationGlobalsChanged.class })
    public void respondConfigurationChanges(ConfigurationGlobalsChanged evt) {
        if (!handler.isActive()) {
            return;
        }

        stopSettingsChangedTimer();
        settingsChanged.compareAndSet(settingsChanged.get(), evt.getSections());

        synchronized (lockSettings) {
            settingsChangedTimer = new Timer();
            settingsChangedTimer.scheduleAtFixedRate(new TimerTask() {

                @Override
                public void run() {
                    AJocClusterService.setLogger(JocClusterConfiguration.IDENTIFIER);
                    List<String> sections = settingsChanged.get();
                    settingsChanged = new AtomicReference<List<String>>();

                    sections = sections.stream().distinct().collect(Collectors.toList());
                    LOGGER.info(String.format("[%s]restart %s services", StartupMode.settings_changed.name(), sections.size()));
                    // TODO restart asynchronous
                    for (String identifier : sections) {
                        LOGGER.info(String.format("[%s][%s]restart ...", StartupMode.settings_changed.name(), identifier));
                        handler.restartService(identifier, StartupMode.settings_changed);
                    }
                }

            }, SUBMIT_SETTING_CHANGES, SUBMIT_SETTING_CHANGES);
        }
    }

    private void stopSettingsChangedTimer() {
        if (settingsChangedTimer != null) {
            settingsChangedTimer.cancel();
            settingsChangedTimer.purge();
        }
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

    public GlobalSettings getSettings() {
        return settings;
    }
}
