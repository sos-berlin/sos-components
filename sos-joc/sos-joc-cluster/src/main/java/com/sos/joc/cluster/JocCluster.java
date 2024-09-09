package com.sos.joc.cluster;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
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
import com.sos.joc.cluster.JocClusterActiveMemberHandler.PerformType;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.configuration.globals.ConfigurationGlobals;
import com.sos.joc.cluster.db.DBLayerJocCluster;
import com.sos.joc.cluster.instances.JocInstance;
import com.sos.joc.cluster.service.JocClusterServiceLogger;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.joc.DBItemJocCluster;
import com.sos.joc.db.joc.DBItemJocConfiguration;
import com.sos.joc.db.joc.DBItemJocInstance;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.joc.event.bean.configuration.ConfigurationGlobalsChanged;
import com.sos.joc.event.bean.dailyplan.DailyPlanCalendarEvent;
import com.sos.joc.model.cluster.common.state.JocClusterState;
import com.sos.joc.model.configuration.ConfigurationType;
import com.sos.joc.model.configuration.globals.GlobalSettings;

public class JocCluster {

    private static final Logger LOGGER = LoggerFactory.getLogger(JocCluster.class);

    public static final int MAX_AWAIT_TERMINATION_TIMEOUT = 30;

    private static final String MSG_SWITCH_TO_API_SERVER = "The action to switch to an API server is not permitted";
    private static JocClusterConfiguration config;
    private static Date jocStartTime = null;

    private final SOSHibernateFactory dbFactory;
    private final JocConfiguration jocConfig;
    private final JocClusterActiveMemberHandler activeMemberHandler;
    private final JocClusterEmbeddedServicesHandler embeddedServicesHandler;
    private final Object lock = new Object();
    private final Object lockMember = new Object();
    private final String currentMemberId;

    private volatile boolean closed;

    private CopyOnWriteArrayList<ControllerConfiguration> controllers;
    private AtomicReference<List<String>> notification;
    private static String jocTimeZone = null;
    private String activeMemberId;
    private String lastActiveMemberId;
    private boolean skipPerform;
    private boolean instanceProcessed;
    private boolean firstStep = true;

    public JocCluster(final SOSHibernateFactory factory, final JocClusterConfiguration jocClusterConfiguration,
            final JocConfiguration jocConfiguration, final Date jocStartDateTime) {
        this.dbFactory = factory;
        this.jocConfig = jocConfiguration;
        this.activeMemberHandler = new JocClusterActiveMemberHandler(this);
        this.embeddedServicesHandler = new JocClusterEmbeddedServicesHandler(this);
        this.currentMemberId = jocConfig.getMemberId();

        config = jocClusterConfiguration;
        jocStartTime = jocStartDateTime;
    }

    public ConfigurationGlobals getConfigurationGlobals(StartupMode mode) {
        return getInstance(mode);
    }

    public void doProcessing(StartupMode mode, ConfigurationGlobals configurations, boolean onJocStart) {
        JocClusterServiceLogger.setLogger();
        String ap = jocConfig.isApiServer() ? "[ApiServer]" : "";
        LOGGER.info(String.format("[inactive]%s[current memberId]%s", ap, currentMemberId));

        boolean isFirstRun = onJocStart;
        while (!closed) {
            try {
                process(mode, configurations, isFirstRun);
                waitFor(config.getPollingInterval());
            } catch (Throwable e) {
                LOGGER.error(e.toString(), e);
                waitFor(config.getPollingWaitIntervalOnError());
            } finally {
                isFirstRun = false;
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
                LOGGER.error(String.format("[getInstance]%s", e.toString()), e);
                LOGGER.info("[getInstance]wait 30s and try again ...");
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

                    JocClusterServiceLogger.setLogger();
                    LOGGER.info(String.format("[%s]no controllers found. sleep 1m and try again ...", jocConfig.getSecurityLevel().name()));
                    waitFor(60);
                }
            } catch (Exception e) {
                if (dbLayer != null) {
                    dbLayer.rollback();
                    dbLayer.close();
                    dbLayer = null;
                }
                JocClusterServiceLogger.setLogger();
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
                controllers = new CopyOnWriteArrayList<ControllerConfiguration>();
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
                JocClusterServiceLogger.setLogger();
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
                DeserializationFeature.FAIL_ON_READING_DUP_TREE_KEY, true).configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);

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
                    LOGGER.warn(String.format("[getStoredSettings][store][new]%s", e.toString()), e);
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
            LOGGER.warn(String.format("[getStoredSettings]%s", e.toString()), e);
            dbLayer.rollback();
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
        configurations.setConfigurationValues(settings);
        return configurations;
    }

    private synchronized void process(StartupMode mode, ConfigurationGlobals configurations, boolean isFirstRun) throws Exception {
        DBLayerJocCluster dbLayer = null;
        DBItemJocCluster item = null;
        boolean isDebugEnabled = LOGGER.isDebugEnabled();
        try {
            if (isDebugEnabled) {
                LOGGER.debug(String.format("[%s][current=%s]process..", mode, currentMemberId));
            }
            dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession());
            dbLayer.beginTransaction();
            dbLayer.updateInstanceHeartBeat(currentMemberId, dbLayer.getNowUTC());
            dbLayer.commit();

            if (jocConfig.isApiServer()) {
                skipPerform = true;
            } else {
                skipPerform = false;
                synchronized (lockMember) {
                    item = dbLayer.getCluster();

                    activeMemberId = item == null ? null : item.getMemberId();
                    if (lastActiveMemberId == null) {
                        lastActiveMemberId = activeMemberId;
                    }
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format("[%s][start][current=%s][active=%s][lastActive=%s]%s", mode, currentMemberId, activeMemberId,
                                lastActiveMemberId, SOSHibernate.toString(item)));
                    }

                    item = handleCurrentMemberOnProcess(mode, dbLayer, item, configurations, isFirstRun, isDebugEnabled);
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
            }
        } catch (SOSHibernateObjectOperationStaleStateException e) {// @Version
            // locked
            if (dbLayer != null) {
                dbLayer.rollback();
            }
            LOGGER.info(String.format("[%s][locked][current=%s][active=%s][lastActive=%s][%s]%s", mode, currentMemberId, activeMemberId,
                    lastActiveMemberId, SOSHibernate.toString(item), e.toString()), e);

        } catch (SOSHibernateObjectOperationException e) {
            if (dbLayer != null) {
                dbLayer.rollback();
            }
            LOGGER.info(String.format("[%s][locked][current=%s][active=%s][lastActive=%s][%s]%s", mode, currentMemberId, activeMemberId,
                    lastActiveMemberId, SOSHibernate.toString(item), e.toString()), e);
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
                    JocClusterAnswer answer = performActiveMemberServices(mode, configurations, item.getMemberId());
                    if (answer.getError() != null) {
                        LOGGER.error(SOSString.toString(answer));
                    }
                }
            }
            postActiveClusterChangedEvent();
            postDailyPlanCalendarEvent(mode);
            lastActiveMemberId = activeMemberId;
        }
    }

    private void postActiveClusterChangedEvent() {
        if (activeMemberId != null) {
            if (lastActiveMemberId == null || !lastActiveMemberId.equals(activeMemberId)) {
                try {
                    JocClusterServiceLogger.setLogger();
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

    private void postDailyPlanCalendarEvent(StartupMode mode) {
        if (StartupMode.automatic.equals(mode) && activeMemberHandler.isActive() && firstStep) {
            firstStep = false;
            try {
                JocClusterServiceLogger.setLogger();
                LOGGER.info("[post]DailyPlanCalendarEvent");
                EventBus.getInstance().post(new DailyPlanCalendarEvent());
            } catch (Exception e) {
                LOGGER.error(e.toString(), e);
            }
        }
    }

    private DBItemJocCluster handleCurrentMemberOnProcess(StartupMode mode, DBLayerJocCluster dbLayer, DBItemJocCluster item,
            ConfigurationGlobals configurations, boolean isFirstRun, boolean isDebugEnabled) throws Exception {
        if (item == null) {
            if (!config.getClusterModeResult().getUse() && !isFirstRun && !isRestart(mode)) {
                if (isDebugEnabled) {
                    LOGGER.debug(String.format(
                            "[%s][current=%s][handleCurrentMemberOnProcess][item=null,config.getClusterModeResult.getUse=false,isFirstRun=false,isRestart=false]inactiveMemberTryStopServices...",
                            mode, currentMemberId));
                }
                inactiveMemberTryStopServices(configurations, isDebugEnabled);
                return null;
            }
            item = new DBItemJocCluster();
            item.setId(JocClusterConfiguration.IDENTIFIER);
            item.setHeartBeat(dbLayer.getNowUTC());
            item.setMemberId(currentMemberId);

            try {
                dbLayer.beginTransaction();
                dbLayer.getSession().save(item);
                dbLayer.commit();

                if (!isFirstRun) {
                    mode = StartupMode.automatic_switchover;
                    item.setStartupMode(mode.name());
                }
            } catch (SOSHibernateObjectOperationException e) {
                dbLayer.rollback();
                item = dbLayer.getCluster();
                LOGGER.info(String.format("[%s][saveCluster][locked][current=%s][active=%s][lastActive=%s][%s]%s", mode, currentMemberId,
                        activeMemberId, lastActiveMemberId, SOSHibernate.toString(item), e.toString()), e);
            }
        }
        if (item != null) {
            if (item.getMemberId().equals(currentMemberId)) {
                item = trySwitchActiveMemberOnProcess(mode, dbLayer, item, configurations, isFirstRun, isDebugEnabled);
            } else {
                if (isHeartBeatExceeded(dbLayer.getNowUTC(), item.getHeartBeat())) {
                    if (!config.getClusterModeResult().getUse()) {
                        if (!isFirstRun) {// extra check when active JOC was killed/not removed from database
                            if (isDebugEnabled) {
                                LOGGER.debug(String.format(
                                        "[%s][current=%s][handleCurrentMemberOnProcess][config.getClusterModeResult.getUse=false,isFirstRun=false][return null on isHeartBeatExceeded=true]%s",
                                        mode, currentMemberId, SOSHibernate.toString(item)));
                            }
                            return null;
                        }
                    }
                    LOGGER.info(String.format("[%s][heartBeat exceeded][db][UTC][%s]%s", mode, SOSDate.getDateTimeAsString(item.getHeartBeat()), item
                            .getMemberId()));

                    boolean update = true;
                    // to avoid start of the current instance if a switchMember defined
                    if (config.getClusterModeResult().getUse() && item.getSwitchMemberId() != null && !item.getSwitchMemberId().equals(
                            currentMemberId)) {
                        DBItemJocInstance switchInstance = dbLayer.getInstance(item.getSwitchMemberId());
                        if (switchInstance != null) {
                            if (!isHeartBeatExceeded(dbLayer.getNowUTC(), switchInstance.getHeartBeat())) {
                                LOGGER.info(String.format("[%s][wait for switchMember]%s", mode, switchInstance.getMemberId()));
                                update = false;
                            }
                        }
                    }

                    if (update) {
                        item.setMemberId(currentMemberId);
                        item.setHeartBeat(dbLayer.getNowUTC());
                        item.setSwitchMemberId(null);
                        item.setSwitchHeartBeat(null);

                        dbLayer.beginTransaction();
                        dbLayer.getSession().update(activeMemberHandleConfigurationGlobalsChanged(item));
                        dbLayer.commit();

                        mode = config.getClusterModeResult().getUse() ? StartupMode.automatic_switchover : StartupMode.automatic;
                        item.setStartupMode(mode.name());
                        LOGGER.info(String.format("[%s][active changed]%s", mode, SOSHibernate.toString(item)));
                    }
                } else {
                    if (isDebugEnabled) {
                        LOGGER.debug("[" + mode + "]inactive");
                    }
                    inactiveMemberTryStopServices(configurations, isDebugEnabled);
                    inactiveMemberHandleConfigurationGlobalsChanged(item);
                }
            }
        }
        return item;
    }

    private void inactiveMemberTryStopServices(ConfigurationGlobals configurations, boolean isDebugEnabled) {
        if (activeMemberHandler.isActive()) {
            StartupMode mode = StartupMode.automatic;
            LOGGER.info("[" + mode + "][start][stop services because current is inactive]" + currentMemberId);
            activeMemberHandler.perform(mode, PerformType.STOP, configurations);
        } else {
            if (isDebugEnabled) {
                LOGGER.debug("[inactiveMemberTryStopServices][skip]activeMemberHandler.isActive=false");
            }
        }
    }

    // GUI - separate thread
    public JocClusterAnswer switchMember(StartupMode mode, ConfigurationGlobals configurations, String newMemberId) {
        if (SOSString.isEmpty(newMemberId)) {
            return getErrorAnswer(new Exception("missing newMemberId"));
        }
        if (newMemberId.equals(currentMemberId)) {
            if (jocConfig.isApiServer()) {
                String msg = getMsgSwitchover2ApiServer(mode, jocConfig.getTitle(), jocConfig.getClusterId(), jocConfig.getOrdering());
                LOGGER.info(msg);
                return getErrorAnswer(msg);
            } else {
                if (activeMemberHandler.isActive()) {
                    return getOKAnswer(JocClusterState.ALREADY_STARTED);
                }
            }
        }

        config.rereadClusterMode();
        if (!config.getClusterModeResult().getUse()) {
            return JocCluster.getErrorAnswer(JocClusterState.MISSING_LICENSE);
        }

        try {
            JocClusterAnswer answer = null;
            synchronized (lockMember) {
                boolean run = true;
                int errorCounter = 0;
                while (run) {
                    if (closed) {
                        LOGGER.info(String.format("[%s][switch][skip]because closed", mode));
                        return getOKAnswer(JocClusterState.STOPPED);
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
                        LOGGER.warn(String.format("[%s][switch][%s]%s", mode, errorCounter, e.toString()), e);
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

    private String getMsgSwitchover2ApiServer(StartupMode mode, DBItemJocInstance ji) {
        return getMsgSwitchover2ApiServer(mode, ji.getTitle(), ji.getClusterId(), ji.getOrdering());
    }

    private String getMsgSwitchover2ApiServer(StartupMode mode, String title, String clusterId, Integer ordering) {
        return String.format("[%s][%s]%s", mode, title == null ? getJocId(clusterId, ordering) : title, MSG_SWITCH_TO_API_SERVER);
    }

    private String getJocId(String clusterId, Integer ordering) {
        return clusterId + "#" + ordering;
    }

    // GUI - separate thread
    private JocClusterAnswer setSwitchMember(StartupMode mode, DBLayerJocCluster dbLayer, ConfigurationGlobals configurations, String newMemberId)
            throws Exception {
        mode = StartupMode.manual_switchover;

        JocClusterAnswer answer = getOKAnswer(JocClusterState.SWITCH);
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
                    LOGGER.info(String.format("[%s][switch][skip][newMemberId=%s]because newMemberId=currentMemberId", mode, newMemberId));
                    answer = getOKAnswer(JocClusterState.ALREADY_STARTED);
                } else {
                    DBItemJocInstance ni = dbLayer.getInstance(newMemberId);
                    Date now = dbLayer.getNowUTC();
                    if (ni == null) {
                        String msg = String.format("[%s][switch][skip][newMemberId=%s]JocInstance not found", mode, newMemberId);
                        LOGGER.info(msg);
                        // answer = getErrorAnswer(msg);
                    } else if (ni.getApiServer()) {
                        String msg = getMsgSwitchover2ApiServer(mode, ni);
                        LOGGER.info(msg);
                        answer = getErrorAnswer(msg);
                    } else if (isHeartBeatExceeded(now, ni.getHeartBeat())) {
                        String msg = String.format("[%s][switch][skip][newMemberId=%s][db][UTC]heartBeat=%s exceeded", mode, newMemberId, SOSDate
                                .getDateTimeAsString(ni.getHeartBeat()));
                        LOGGER.info(msg);
                        // answer = getErrorAnswer(msg);
                    } else {
                        LOGGER.info(String.format("[%s][switch][start][newMemberId=%s][db][UTC]heartBeat=%s ...", mode, newMemberId, SOSDate
                                .getDateTimeAsString(ni.getHeartBeat())));
                        if (activeMemberHandler.isActive()) {
                            LOGGER.info(String.format("[%s][switch][start][stop][current]%s", mode, currentMemberId));
                            activeMemberHandler.perform(mode, PerformType.STOP, configurations);

                            now = dbLayer.getNowUTC();
                        }
                        item.setMemberId(newMemberId);
                        item.setHeartBeat(now);
                        // item.setSwitchMemberId(null);
                        // item.setSwitchHeartBeat(null);

                        item.setSwitchMemberId(newMemberId);
                        item.setSwitchHeartBeat(item.getHeartBeat());

                        dbLayer.beginTransaction();
                        dbLayer.getSession().update(item);
                        dbLayer.commit();

                        LOGGER.info(String.format("[%s][switch][end]newMemberId=%s", mode, newMemberId));
                        activeMemberId = newMemberId;
                        postActiveClusterChangedEvent();
                        lastActiveMemberId = activeMemberId;
                    }
                }
            } else {
                if (item.getMemberId().equals(newMemberId)) {
                    LOGGER.info("[" + mode + "][switch][end][skip][already active]activeMemberId=switch memberId");
                    answer = getOKAnswer(JocClusterState.ALREADY_STARTED);
                } else {
                    if (item.getSwitchMemberId() == null || !item.getSwitchMemberId().equals(newMemberId)) {
                        Date lastHeartBeat = null;
                        Date now = null;

                        boolean skip = false;
                        boolean isCurrentMember = newMemberId.equals(currentMemberId);
                        if (isCurrentMember) {
                            if (jocConfig.isApiServer()) {
                                skip = true;
                            }
                        } else {
                            DBItemJocInstance ni = dbLayer.getInstance(newMemberId);
                            if (ni == null) {
                                skip = true;
                                LOGGER.info(String.format("[%s][switch][skip][setSwitchMember][newMemberId=%s]not found", mode, newMemberId));
                            } else if (ni.getApiServer()) {
                                skip = true;

                                String msg = getMsgSwitchover2ApiServer(mode, ni);
                                LOGGER.info(msg);
                                answer = getErrorAnswer(msg);
                            } else {
                                lastHeartBeat = ni.getHeartBeat();
                                now = dbLayer.getNowUTC();
                                if (isHeartBeatExceeded(now, ni.getHeartBeat())) {
                                    skip = true;
                                    LOGGER.info(String.format("[%s][switch][skip][setSwitchMember][newMemberId=%s][db][UTC]heartBeat=%s exceeded",
                                            mode, newMemberId, SOSDate.getDateTimeAsString(ni.getHeartBeat())));
                                }
                            }
                        }
                        if (!skip) {
                            if (isCurrentMember) {
                                LOGGER.info(String.format("[%s][switch][start][setSwitchMember]newMemberId=currentMemberId=%s ...", mode,
                                        newMemberId));
                            } else {
                                LOGGER.info(String.format("[%s][switch][start][setSwitchMember][newMemberId=%s]last db UTC heartBeat=%s ...", mode,
                                        newMemberId, SOSDate.getDateTimeAsString(lastHeartBeat)));
                            }
                            // set switchMember because before "switch" the active cluster instance must be stopped
                            // and the current instance is not an active instance
                            // TODO concurrency error handling
                            item.setHeartBeat(now == null ? dbLayer.getNowUTC() : now);
                            item.setSwitchMemberId(newMemberId);
                            item.setSwitchHeartBeat(item.getHeartBeat());

                            dbLayer.beginTransaction();
                            dbLayer.getSession().update(item);
                            dbLayer.commit();
                            LOGGER.info(String.format("[%s][switch][end][setSwitchMember]newMemberId=%s", mode, newMemberId));
                            // watchSwitch(newMemberId);
                        }
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

    /** called if item.getMemberId().equals(currentMemberId) */
    private DBItemJocCluster trySwitchActiveMemberOnProcess(StartupMode mode, DBLayerJocCluster dbLayer, DBItemJocCluster item,
            ConfigurationGlobals configurations, boolean isFirstRun, boolean isDebugEnabled) throws Exception {
        skipPerform = false;
        item.setMemberId(currentMemberId);

        if (item.getSwitchMemberId() != null) {// && config.getClusterModeResult().getUse()
            mode = StartupMode.manual_switchover;
            item.setStartupMode(mode.name());

            if (item.getSwitchMemberId().equals(currentMemberId)) {
                item.setHeartBeat(dbLayer.getNowUTC());
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
                        Date now = dbLayer.getNowUTC();
                        if (isHeartBeatExceeded(now, item.getSwitchHeartBeat())) {
                            LOGGER.info(String.format("[%s][switch][skip][newMemberId=%s][db][UTC]switchHeartBeat=%s exceeded", mode, item
                                    .getSwitchMemberId(), SOSDate.getDateTimeAsString(item.getSwitchHeartBeat())));
                        } else {
                            LOGGER.info("[" + mode + "][switch][stop current]newMemberId=" + item.getSwitchMemberId());
                            if (activeMemberHandler.isActive()) {
                                // perform STOP can take a time ...
                                // the stops of the individual services are executed in parallel, but are joined at the end
                                activeMemberHandler.perform(mode, PerformType.STOP, configurations);

                                now = dbLayer.getNowUTC();
                            }
                            item.setMemberId(item.getSwitchMemberId());
                            skipPerform = true;
                        }
                        // item.setSwitchMemberId(null);
                        // item.setSwitchHeartBeat(null);
                        item.setHeartBeat(now);
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
            if (!config.getClusterModeResult().getUse()) {
                if (!activeMemberHandler.isActive() && !isFirstRun && !isRestart(mode)) {
                    if (isDebugEnabled) {
                        LOGGER.debug(String.format(
                                "[%s][current=%s][trySwitchActiveMemberOnProcess][config.getClusterModeResult.getUse=false,activeMemberHandler.isActive=false,isFirstRun=false,isRestart=false]skip",
                                mode, currentMemberId));
                    }
                    return null;
                }
            }

            if (isDebugEnabled) {
                LOGGER.debug(String.format(
                        "[%s][current=%s][trySwitchActiveMemberOnProcess][activeMemberHandler.isActive=%s,isFirstRun=%s]update cluster..", mode,
                        currentMemberId, activeMemberHandler.isActive(), isFirstRun));
            }

            dbLayer.beginTransaction();
            item.setHeartBeat(dbLayer.getNowUTC());
            dbLayer.getSession().update(activeMemberHandleConfigurationGlobalsChanged(item));
            dbLayer.commit();
        }
        return item;
    }

    private boolean isRestart(StartupMode mode) {
        if (mode == null) {
            return false;
        }
        return StartupMode.manual_restart.equals(mode);
    }

    private void inactiveMemberHandleConfigurationGlobalsChanged(DBItemJocCluster item) {
        if (!SOSString.isEmpty(item.getNotification()) && !currentMemberId.equals(item.getNotificationMemberId())) {
            JocClusterNotification n = new JocClusterNotification(config.getPollingInterval());
            n.parse(item.getNotification());
            if (!n.isExpired()) {
                EventBus.getInstance().post(new ConfigurationGlobalsChanged("", ConfigurationType.GLOBALS.name(), n.getSections()));
            }
        }
    }

    private DBItemJocCluster activeMemberHandleConfigurationGlobalsChanged(DBItemJocCluster item) {
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

    public static boolean isHeartBeatExceeded(Date now, Date heartBeat) {
        if (now == null || heartBeat == null) {
            return true;
        }
        JocClusterConfiguration c = config != null ? config : JocClusterConfiguration.defaultConfiguration();
        if (((now.getTime() / 1_000) - (heartBeat.getTime() / 1_000)) > c.getHeartBeatExceededInterval()) {
            return true;
        }
        return false;
    }

    private JocClusterAnswer performActiveMemberServices(StartupMode mode, ConfigurationGlobals configurations, String memberId) {
        if (memberId.equals(currentMemberId)) {
            if (activeMemberHandler.isActive()) {
                return getOKAnswer(JocClusterState.ALREADY_STARTED);
            } else {
                return activeMemberHandler.perform(mode, PerformType.START, configurations);
            }
        } else {
            if (activeMemberHandler.isActive()) {
                return activeMemberHandler.perform(mode, PerformType.STOP, configurations);
            } else {
                return getOKAnswer(JocClusterState.ALREADY_STOPPED);
            }
        }
    }

    public void close(StartupMode mode, ConfigurationGlobals configurations, boolean deleteActiveCurrentMember) {
        LOGGER.info("[" + mode + "][cluster][close]start...----------------------------------------------");
        closed = true;
        synchronized (lock) {
            lock.notifyAll();
        }
        synchronized (lockMember) {
            lockMember.notifyAll();
        }
        closeEmbeddedServices(mode);
        closeActiveMemberServices(mode, configurations);
        if (deleteActiveCurrentMember) {
            tryDeleteActiveCurrentMember();
        }
        LOGGER.info("[" + mode + "][cluster][close]end----------------------------------------------");
    }

    private JocClusterAnswer closeActiveMemberServices(StartupMode mode, ConfigurationGlobals configurations) {
        LOGGER.info("[" + mode + "][cluster][closeActiveMemberServices][isActive=" + activeMemberHandler.isActive() + "]start...");
        JocClusterAnswer answer = null;
        if (activeMemberHandler.isActive()) {
            answer = activeMemberHandler.perform(mode, PerformType.STOP, configurations);
        } else {
            answer = getOKAnswer(JocClusterState.ALREADY_STOPPED);
        }
        LOGGER.info("[" + mode + "][cluster][closeActiveMemberServices][isActive=" + activeMemberHandler.isActive() + "]end");
        return answer;
    }

    private JocClusterAnswer closeEmbeddedServices(StartupMode mode) {
        LOGGER.info("[" + mode + "][cluster][closeEmbeddedServices]start...");
        JocClusterAnswer answer = embeddedServicesHandler.perform(mode, JocClusterEmbeddedServicesHandler.PerformType.STOP);
        LOGGER.info("[" + mode + "][cluster][closeEmbeddedServices]end");
        return answer;
    }

    public JocClusterAnswer startEmbeddedServices(StartupMode mode) {
        LOGGER.info("[" + mode + "][cluster][startEmbeddedServices]start...");
        JocClusterAnswer answer = embeddedServicesHandler.perform(mode, JocClusterEmbeddedServicesHandler.PerformType.START);
        LOGGER.info("[" + mode + "][cluster][startEmbeddedServices]end");
        return answer;
    }

    public int tryDeleteActiveCurrentMember() {
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
            dbLayer.close();
            dbLayer = null;
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
        } finally {
            if (dbLayer != null) {
                dbLayer.close();
            }
        }
        return result;
    }

    // separate thread
    protected void updateHeartBeat(StartupMode mode, String method, int retryCount, boolean log) {
        boolean run = true;
        int errorCount = 0;
        while (run) {
            try {
                if (log) {
                    LOGGER.info(String.format("[%s][%s]update heart beat on long running service %s...", mode, method, method));
                }
                updateHeartBeat();
                run = false;
            } catch (Throwable e) {
                errorCount += 1;
                if (errorCount > retryCount) {
                    run = false;
                } else {
                    waitFor(1);
                }
            }
        }
    }

    // separate thread
    private void updateHeartBeat() throws Exception {
        DBLayerJocCluster dbLayer = null;
        try {
            dbLayer = new DBLayerJocCluster(dbFactory.openStatelessSession());
            dbLayer.beginTransaction();
            Date now = dbLayer.getNowUTC();
            dbLayer.updateClusterHeartBeat(now);
            dbLayer.updateInstanceHeartBeat(currentMemberId, now);
            dbLayer.commit();

            dbLayer.close();
            dbLayer = null;
        } catch (Throwable e) {
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

    public static JocClusterAnswer getOKAnswer(JocClusterState state) {
        return new JocClusterAnswer(state);
    }

    public static JocClusterAnswer getOKAnswer(JocClusterState state, String message) {
        return new JocClusterAnswer(state, message);
    }

    public static JocClusterAnswer getErrorAnswer(String msg) {
        return getErrorAnswer(JocClusterState.ERROR, new Exception(msg));
    }

    public static JocClusterAnswer getErrorAnswer(JocClusterState state) {
        return getErrorAnswer(state, new Exception(state.toString()));
    }

    public static JocClusterAnswer getErrorAnswer(Exception e) {
        return getErrorAnswer(JocClusterState.ERROR, e);
    }

    public static JocClusterAnswer getErrorAnswer(JocClusterState state, Exception e) {
        JocClusterAnswer answer = new JocClusterAnswer(state);
        answer.setError(e);
        return answer;
    }

    public static void shutdownThreadPool(String logPrefix, ExecutorService threadPool, long awaitTerminationTimeout) {
        shutdownThreadPool(3, logPrefix, threadPool, awaitTerminationTimeout, true);
    }

    public static void shutdownThreadPool(String logPrefix, ExecutorService threadPool, long awaitTerminationTimeout, boolean logLevelInfo) {
        shutdownThreadPool(3, logPrefix, threadPool, awaitTerminationTimeout, logLevelInfo);
    }

    private static void shutdownThreadPool(int methodNameIndex, String logPrefix, ExecutorService threadPool, long awaitTerminationTimeout,
            boolean logLevelInfo) {
        String caller = SOSClassUtil.getMethodName(methodNameIndex > 0 ? methodNameIndex : 2);

        String logp = logPrefix == null ? "" : logPrefix;
        try {
            if (threadPool == null) {
                return;
            }
            caller += "-" + threadPool.getClass().getSimpleName();
            threadPool.shutdown();// Disable new tasks from being submitted
            // Wait a while for existing tasks to terminate
            if (threadPool.awaitTermination(awaitTerminationTimeout, TimeUnit.SECONDS)) {
                String msg = String.format("%s[shutdown][%s]thread pool has been shut down correctly", logp, caller);
                if (logLevelInfo) {
                    LOGGER.info(msg);
                } else {
                    LOGGER.debug(msg);
                }
            } else {
                threadPool.shutdownNow();// Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (threadPool.awaitTermination(3, TimeUnit.SECONDS)) {
                    String msg = String.format("%s[shutdown][%s]thread pool has ended due to timeout of %ss on shutdown", logp, caller,
                            awaitTerminationTimeout);
                    if (logLevelInfo) {
                        LOGGER.info(msg);
                    } else {
                        LOGGER.debug(msg);
                    }
                } else {
                    LOGGER.info(String.format("%s[shutdown][%s]thread pool did not terminate due to timeout of %ss on shutdown", logp, caller,
                            awaitTerminationTimeout));
                }
            }
        } catch (InterruptedException e) {
            // (Re-)Cancel if current thread also interrupted
            LOGGER.info(String.format("%s[shutdown][%s][exception]%s", logp, caller, e.toString()), e);
            threadPool.shutdownNow();
            // Thread.currentThread().interrupt();
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

    public JocClusterActiveMemberHandler getActiveMemberHandler() {
        return activeMemberHandler;
    }

    public List<ControllerConfiguration> getControllers() {
        return controllers;
    }

    public void setConfigurationGlobalsChanged(AtomicReference<List<String>> val) {
        notification = val;
    }

    public static Date getJocStartTime() {
        return jocStartTime;
    }

}
