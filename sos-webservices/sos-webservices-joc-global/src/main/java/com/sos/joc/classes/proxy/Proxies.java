package com.sos.joc.classes.proxy;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.db.inventory.DBItemInventoryAgentInstance;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.DBItemInventorySubAgentInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.proxy.ProxyClosed;
import com.sos.joc.event.bean.proxy.ProxyCoupled;
import com.sos.joc.event.bean.proxy.ProxyRemoved;
import com.sos.joc.event.bean.proxy.ProxyRestarted;
import com.sos.joc.event.bean.proxy.ProxyStarted;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;
import com.sos.joc.exceptions.ProxyNotCoupledException;
import com.sos.joc.model.agent.SubagentDirectorType;

import js7.base.web.Uri;
import js7.data.agent.AgentPath;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.auth.JHttpsConfig;
import js7.data_for_java.subagent.JSubagentItem;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.JProxyContext;

public class Proxies {

    private static final Logger LOGGER = LoggerFactory.getLogger(Proxies.class);
    private static Proxies proxies;
    private static JProxyContext proxyContext = new JProxyContext();
    private volatile ConcurrentMap<ProxyCredentials, ProxyContext> controllerFutures = new ConcurrentHashMap<>();
    private volatile ConcurrentMap<ProxyCredentials, JControllerApi> controllerApis = new ConcurrentHashMap<>();
    private volatile ConcurrentMap<String, List<DBItemInventoryJSInstance>> controllerDbInstances = new ConcurrentHashMap<>();
    private volatile ConcurrentMap<String, Boolean> coupledStates = new ConcurrentHashMap<>();
    private volatile ConcurrentMap<String, ProxyCoupled> proxyCredentials = new ConcurrentHashMap<>();

    private Proxies() {
        EventBus.getInstance().register(this);
    }

    protected static Proxies getInstance() {
        if (proxies == null) {
            proxies = new Proxies();
        }
        if (proxyContext == null) {
            proxyContext = new JProxyContext();
        }
        return proxies;
    }
    
    /**
     * Returns a JControllerProxy according the credentials specified by controllerId and account (and starts it if necessary)
     * @param controllerId
     * @param account
     * @param connectionTimeout
     * @return JControllerProxy
     * @throws ControllerConnectionResetException
     * @throws ControllerConnectionRefusedException 
     *      if the asynchronous started Proxy not available within the specified connectionTimeout
     *      or a an SSL handshake error occurs 
     * @throws DBMissingDataException
     * @throws ExecutionException
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    protected JControllerProxy of(String controllerId, ProxyUser account, long connectionTimeout) throws ControllerConnectionResetException,
            ControllerConnectionRefusedException, DBMissingDataException, ExecutionException, JocConfigurationException, DBOpenSessionException,
            DBInvalidDataException, DBConnectionRefusedException {
        initControllerDbInstances(controllerId);
        return of(ProxyCredentialsBuilder.withDbInstancesOfCluster(controllerDbInstances.get(controllerId)).withAccount(account).build(),
                connectionTimeout);
    }
    
    /**
     * Returns a ControllerApi according the credentials specified by controllerId and account
     * @param controllerId
     * @param account
     * @param connectionTimeout
     * @return JControllerApi
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBMissingDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     * @throws ControllerConnectionResetException 
     */
    protected JControllerApi loadApi(String controllerId, ProxyUser account, long connectionTimeout) throws DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException,
            ControllerConnectionRefusedException {
        initControllerDbInstances(controllerId);
        return loadApi(ProxyCredentialsBuilder.withDbInstancesOfCluster(controllerDbInstances.get(controllerId)).withAccount(account).build());
    }
    
    /**
     * Closes a started Proxy according the credentials specified by controllerId and account
     * @param controllerId
     * @param account
     * @return CompletableFuture&lt;Void&gt;
     * @throws DBMissingDataException
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     */
    protected CompletableFuture<Void> close(String controllerId, ProxyUser account) throws DBMissingDataException, JocConfigurationException,
            DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException {
        initControllerDbInstances(controllerId);
        return close(ProxyCredentialsBuilder.withDbInstancesOfCluster(controllerDbInstances.get(controllerId)).withAccount(account).build());
    }
    
    /**
     * Starts Proxy with according credentials specified by controllerId and account
     * @param controllerId
     * @param account
     * @return ProxyContext
     * @throws ControllerConnectionRefusedException
     * @throws DBMissingDataException
     * @throws DBOpenSessionException 
     * @throws JocConfigurationException 
     * @throws DBConnectionRefusedException 
     * @throws DBInvalidDataException 
     */
    protected ProxyContext start(String controllerId, ProxyUser account) throws ControllerConnectionRefusedException, DBMissingDataException,
            JocConfigurationException, DBOpenSessionException, DBInvalidDataException, DBConnectionRefusedException {
        initControllerDbInstances(controllerId);
        return start(ProxyCredentialsBuilder.withDbInstancesOfCluster(controllerDbInstances.get(controllerId)).withAccount(account).build());
    }
    
    /**
     * Closes all Proxies with specified 'controllerId' inside its credentials
     * Should only called from provisioning dialogue 
     * @param controllerId
     */
    protected void removeProxies(final String controllerId) {
        LOGGER.info("Remove ControllerApi (Proxy/Cluster Watch) for '" + controllerId + "'");
        proxiesOfControllerId(controllerId).forEach((key, proxy) -> {
            proxy.stop();
            controllerFutures.remove(key);
            JControllerApi controllerApi = controllerApis.remove(key);
            if (controllerApi != null) {
                controllerApi.stop();
            }
            EventBus.getInstance().post(new ProxyRemoved(key.getUser().name(), controllerId));
        });
        controllerDbInstances.remove(controllerId);
    }
    
    /**
     * Restarts Proxies with credentials from specified database instances  
     * Should only called from provisioning dialogue
     * @param controllerDbInstances
     * @throws DBMissingDataException 
     * @throws ControllerConnectionRefusedException 
     */
    protected void updateProxies(final List<DBItemInventoryJSInstance> controllerDbInstances) throws DBMissingDataException,
            ControllerConnectionRefusedException {
        if (controllerDbInstances != null && !controllerDbInstances.isEmpty()) {
            String controllerId = controllerDbInstances.get(0).getControllerId();
            boolean isNew = !this.controllerDbInstances.containsKey(controllerId);
            this.controllerDbInstances.put(controllerId, controllerDbInstances);
            for (ProxyUser account : ProxyUser.values()) {
                ProxyCredentials newCredentials = ProxyCredentialsBuilder.withDbInstancesOfCluster(controllerDbInstances).withAccount(account)
                        .build();
                // History needs only ControllerAPI (not Proxy)
                // in case of changes - see eg JocClusterService.java->handleControllerAdded
                if (isNew) {
                    if (account == ProxyUser.JOC) {
                        start(newCredentials);
                        //startWatch(newCredentials);
                    } else if (account == ProxyUser.HISTORY) {
                        loadApi(newCredentials);
                    }
                    EventBus.getInstance().post(new ProxyStarted(account.name(), controllerId));
                } else {
                    if ((account == ProxyUser.JOC && restart(newCredentials, false)) || (account == ProxyUser.HISTORY && reloadApi(newCredentials))) {
                        EventBus.getInstance().post(new ProxyRestarted(account.name(), controllerId));
                    }
                }
            }
        }
    }
    
    protected void updateProxies(final String controllerId) throws DBMissingDataException, ControllerConnectionRefusedException {
        if (controllerDbInstances != null && !controllerDbInstances.isEmpty()) {
            List<DBItemInventoryJSInstance> dbInstances = controllerDbInstances.get(controllerId);
            for (ProxyUser account : ProxyUser.values()) {
                ProxyCredentials credentials = ProxyCredentialsBuilder.withDbInstancesOfCluster(dbInstances).withAccount(account)
                        .build();
                // History needs only ControllerAPI (not Proxy)
                // in case of changes - see eg JocClusterService.java->handleControllerAdded
                if ((account == ProxyUser.JOC && restart(credentials, true)) || (account == ProxyUser.HISTORY && reloadApi(credentials))) {
                    EventBus.getInstance().post(new ProxyRestarted(account.name(), controllerId));
                }
            }
        }
    }
    
    /**
     * Returns map of Controller database instances where the key is the controllerId and the
     * value is always a list. The list contains one or two members depending on stand-alone or cluster. 
     * @return Map&lt;String, List&lt;DBItemInventoryJSInstance&gt;&gt;
     */
    public static Map<String, List<DBItemInventoryJSInstance>> getControllerDbInstances() {
        return Proxies.getInstance().controllerDbInstances;
    }
    
//    public static Optional<JCredentials> getJOCCredentials() {
//        return Proxies.getInstance().controllerApis.keySet().stream().filter(p -> ProxyUser.JOC.getUser().equals(p.getUser().getUser())).map(
//                ProxyCredentials::getAccount).findAny();
//    }
    
    public static ProxyCoupled getJOCCredentials(String url) {
        return Proxies.getInstance().proxyCredentials.get(url);
    }
    
    /**
     * Starts all Proxies from db 'instances' table for specified user. Should be called in servlet 'init' method 
     * @param properties (from ./resources/joc/joc.properties to get keystore and truststore information)
     * @param delay 
     *      A started Proxy future needs around 10 seconds until the connection is successful.
     *      The method sleeps according specified 'delay' (in seconds) 
     * @param account
     */
    public static void startAll(final JocCockpitProperties properties, final long delay, final ProxyUser account) {
        Proxies.getInstance()._startAll(properties, delay, account, null);
    }
    
    /**
     * Starts all Proxies from db 'instances' table for specified user. 
     * A url mapping (key=url from database, value=new url) can be used for test environment. 
     * Should be called in servlet 'init' method 
     * @param properties
     * @param delay
     *      A started Proxy future needs around 10 seconds until the connection is successfully.
     *      The method sleeps according specified 'delay' (in seconds) 
     * @param account
     * @param urlMapper
     */
    public static void startAll(final JocCockpitProperties properties, final long delay, final ProxyUser account, Map<String, String> urlMapper) {
        Proxies.getInstance()._startAll(properties, delay, account, urlMapper);
    }
    
    /**
     * Starts all Proxies from db 'instances' table for specified user. Should be called in servlet 'init' method 
     * @param properties (from ./resources/joc/joc.properties to get keystore and truststore information)
     * @param account
     */
    public static void startAll(final JocCockpitProperties properties, final ProxyUser account) {
        Proxies.getInstance()._startAll(properties, 10, account, null);
    }
    
    /**
     * 
     * @param controllerId
     * @return
     */
    public static boolean isCoupled(String controllerId) {
        return Proxies.getInstance().coupledStates.getOrDefault(controllerId, true);
    }
    
    @Subscribe({ ProxyCoupled.class })
    public void setCoupled(ProxyCoupled evt) {
        if (evt.getControllerId() != null && !evt.getControllerId().isEmpty() && evt.isCoupled() != null) {
            coupledStates.put(evt.getControllerId(), evt.isCoupled());
        }
        if (evt.getUser() != null && ProxyUser.JOC.getUser().equals(evt.getUser())) {
            if (evt.getUrl() != null) {
                proxyCredentials.put(evt.getUrl(), evt);
            }
            if (evt.getBackupUrl() != null) {
                proxyCredentials.put(evt.getBackupUrl(), evt);
            }
        }
    }
    
    public static void setCoupled(String controllerId, boolean isCoupled) {
        Proxies.getInstance()._setCoupled(controllerId, isCoupled);
    }
    
    private void _setCoupled(String controllerId, boolean isCoupled) {
        coupledStates.put(controllerId, isCoupled);
    }
    
    private void _startAll(final JocCockpitProperties properties, final long delay, final ProxyUser account, Map<String, String> urlMapper) {
        LOGGER.info(String.format("starting all proxies for user %s ...", account));
        try {
            JHttpsConfig httpsConfig = ProxyCredentialsBuilder.getHttpsConfig(properties);
            initControllerDbInstances(urlMapper);
            if (controllerDbInstances == null) {
                controllerDbInstances = new ConcurrentHashMap<String, List<DBItemInventoryJSInstance>>();
            }
            controllerDbInstances.values().stream().map(dbItems -> {
                try {
                    return ProxyCredentialsBuilder.withDbInstancesOfCluster(dbItems).withAccount(account).withHttpsConfig(httpsConfig).build();
                } catch (DBMissingDataException e) {
                    LOGGER.error("", e);
                    return null;
                }
            }).filter(Objects::nonNull).filter(credentials -> credentials.getUrl() != null).forEach(credentials -> {
                try {
                    start(credentials);
                    //startWatch(credentials);
                } catch (ControllerConnectionRefusedException e) {
                    LOGGER.error(e.toString());
                }
            });
            if (!controllerDbInstances.isEmpty()) {
                try {
                    long timeout = Math.max(0, delay);
                    if (timeout > 0) {
                        TimeUnit.SECONDS.sleep(timeout);
                    }
                } catch (InterruptedException e) {
                }
            }
        } catch (JocException e) {
            LOGGER.error("starting all proxies failed", e);
        }
    }

    /**
     * Starts all Proxies from specified credentials. Only used for unit test
     * @param credentials
     */
    protected void startAll(final ProxyCredentials ...credentials) {
        LOGGER.info("starting all proxies ...");
        Arrays.asList(credentials).stream().forEach(credential -> {
            try {
                start(credential);
            } catch (ControllerConnectionRefusedException e) {
                LOGGER.error("", e);
            }
        });
    }
    
    /**
     * Loads all ControllerApis from db 'instances' table for specified user.
     * @param properties (from ./resources/joc/joc.properties to get keystore and truststore information)
     * @param account
     */
    public static void loadAllApis(final JocCockpitProperties properties, final ProxyUser account) {
        Proxies.getInstance()._loadAllApis(properties, account, null);
    }
    
    public static void loadAllApis(final JocCockpitProperties properties, final ProxyUser account, Map<String, String> urlMapper) {
        Proxies.getInstance()._loadAllApis(properties, account, urlMapper);
    }
    
    private void _loadAllApis(final JocCockpitProperties properties, final ProxyUser account, Map<String, String> urlMapper) {
        LOGGER.info(String.format("load all controller apis for user %s ...", account));
        try {
            JHttpsConfig httpsConfig = ProxyCredentialsBuilder.getHttpsConfig(properties);
            initControllerDbInstances(urlMapper);
            if (controllerDbInstances == null) {
                controllerDbInstances = new ConcurrentHashMap<String, List<DBItemInventoryJSInstance>>();
            }
            controllerDbInstances.values().stream().map(dbItems -> {
                try {
                    return ProxyCredentialsBuilder.withDbInstancesOfCluster(dbItems).withAccount(account).withHttpsConfig(httpsConfig).build();
                } catch (DBMissingDataException e) {
                    LOGGER.error("", e);
                    return null;
                }
            }).filter(credential -> credential != null && credential.getUrl() != null).forEach(credential -> {
                try {
                    loadApi(credential);
                } catch (ControllerConnectionRefusedException e) {
                    LOGGER.error(e.toString());
                }
            });
        } catch (JocException e) {
            LOGGER.error("loading all controller apis failed", e);
        }
    }

    /**
     * Closes all started Proxies. Should be called in servlet 'destroy' method
     */
    public static void closeAll() {
        Proxies.getInstance()._closeAll();
    }
    
    private void _closeAll() {
        LOGGER.info("closing all proxies ...");
        try {
            CompletableFuture.allOf(controllerApis.values().stream()
                .map(controllerApi -> CompletableFuture.runAsync(() -> controllerApi.stop())).toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    controllerFutures.clear();
                    controllerApis.clear();
                    proxyContext.close();
                    proxyContext = null;
                }).get(30, TimeUnit.SECONDS);
        } catch (Exception e) {
        } finally {
            try {
                if(proxyContext != null) {
                    proxyContext.close();
                }
            } catch (Exception e) {
            } finally {
                proxyContext = null;
            }
        }
    }
    
    private void initControllerDbInstances(Map<String, String> urlMapper) throws JocConfigurationException, DBOpenSessionException,
            DBInvalidDataException, DBConnectionRefusedException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("Proxies");
            if (urlMapper == null) {
                controllerDbInstances = new InventoryInstancesDBLayer(sosHibernateSession).getInventoryInstances().stream().collect(Collectors
                        .groupingByConcurrent(DBItemInventoryJSInstance::getControllerId));
            } else {
                controllerDbInstances = new InventoryInstancesDBLayer(sosHibernateSession).getInventoryInstances().stream().peek(i -> i.setUri(
                        urlMapper.getOrDefault(i.getUri(), i.getUri()))).collect(Collectors.groupingByConcurrent(
                                DBItemInventoryJSInstance::getControllerId));
            }
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }
    
    private void initControllerDbInstances(String controllerId) throws JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException, DBMissingDataException {
        if (!controllerDbInstances.containsKey(controllerId)) {
            SOSHibernateSession sosHibernateSession = null;
            try {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection("Proxies");
                List<DBItemInventoryJSInstance> instances = new InventoryInstancesDBLayer(sosHibernateSession).getInventoryInstancesByControllerId(
                        controllerId);
                if (instances != null && !instances.isEmpty()) {
                    controllerDbInstances.put(controllerId, instances);
                } else {
                    throw new DBMissingDataException(String.format("unknown controller '%s'", controllerId));
                }
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
        }
    }
    
    public static Map<JAgentRef, List<JSubagentItem>> getUnknownAgents(String controllerId, InventoryAgentInstancesDBLayer dbLayer,
            Map<AgentPath, JAgentRef> controllerKnownAgents, Map<SubagentId, JSubagentItem> controllerKnownSubagents, boolean onlyReDeployed)
            throws JocException, DBConnectionRefusedException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            if (dbLayer == null) {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection("GetUnknownAgents");
                dbLayer = new InventoryAgentInstancesDBLayer(sosHibernateSession);
            }
            List<DBItemInventoryAgentInstance> dbAgents = dbLayer.getAgentsByControllerIds(Collections.singleton(controllerId), false, false);
            if (dbAgents != null) {
                Map<JAgentRef, List<JSubagentItem>> result = new LinkedHashMap<>(dbAgents.size());
                Map<String, List<DBItemInventorySubAgentInstance>> subAgents = dbLayer.getSubAgentInstancesByControllerIds(Collections.singleton(
                        controllerId), false, true);
                for (DBItemInventoryAgentInstance dbAgent : dbAgents) {
                    AgentPath agentPath = AgentPath.of(dbAgent.getAgentId());
                    JAgentRef jAgentRef = controllerKnownAgents.get(agentPath);
                    List<DBItemInventorySubAgentInstance> subs = subAgents.get(dbAgent.getAgentId());
                    if (subs == null || subs.isEmpty()) { // single agent
                        if (jAgentRef != null) {
                            // update deployed flag in DB if necessary
                            String url = jAgentRef.director().map(controllerKnownSubagents::get).map(JSubagentItem::uri).map(Uri::string).orElse(
                                    dbAgent.getUri());
                            if (!dbAgent.getDeployed() && dbAgent.getUri().equals(url)) {
                                dbAgent.setDeployed(true);
                                dbLayer.updateAgent(dbAgent);
                            }
                            continue;
                        }
                        subs = Collections.singletonList(dbLayer.solveAgentWithoutSubAgent(dbAgent)); // create director with agentId as subagentId
                    } else {
                        if (jAgentRef != null) {
                            continue;
                        }
                    }
                    if (dbAgent.getHidden()) {
                        continue;
                    }
                    if (onlyReDeployed && !dbAgent.getDeployed()) {
                        continue;
                    }
                    List<JSubagentItem> subRefs = subs.stream().map(s -> JSubagentItem.of(SubagentId.of(s.getSubAgentId()), AgentPath.of(s
                            .getAgentId()), Uri.of(s.getUri()), s.getDisabled())).collect(Collectors.toList());
                    Set<SubagentId> directors = subs.stream().filter(s -> s.getIsDirector() > SubagentDirectorType.NO_DIRECTOR.intValue()).sorted()
                            .map(s -> SubagentId.of(s.getSubAgentId())).collect(Collectors.toSet());
                    result.put(JAgentRef.of(agentPath, directors), subRefs);
                }
                return result;
            }
            return Collections.emptyMap();
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private Map<ProxyCredentials, ProxyContext> proxiesOfControllerId(final String controllerId) {
        return controllerFutures.entrySet().stream().filter(entry -> entry.getKey().getControllerId().equals(controllerId)).collect(Collectors
                .toMap(Entry::getKey, Entry::getValue));
    }
    
    /**
     * only protected for Unit tests
     * @param credentials
     * @param connectionTimeout
     * @return
     * @throws ControllerConnectionResetException
     * @throws ExecutionException
     * @throws ControllerConnectionRefusedException
     */
    protected JControllerProxy of(ProxyCredentials credentials, long connectionTimeout) throws ControllerConnectionResetException,
            ExecutionException, ControllerConnectionRefusedException, ControllerConnectionRefusedException {
        ProxyContext context = start(credentials);
        try {
            return context.getProxy(connectionTimeout);
        } catch (ProxyNotCoupledException e) {
            close(credentials);
            throw e;
        } catch (CancellationException e) {
            close(credentials);
            throw new ControllerConnectionResetException(credentials.getControllerId());
        }
    }
    
    private CompletableFuture<Void> close(ProxyCredentials credentials) {
        if (controllerFutures.containsKey(credentials)) {
            return controllerFutures.get(credentials).stop().thenRun(() -> {
                controllerFutures.remove(credentials);
                JControllerApi controllerApi = controllerApis.remove(credentials);
                if (controllerApi != null) {
                    controllerApi.stop();
                }
                EventBus.getInstance().post(new ProxyClosed(credentials.getUser().name(), credentials.getControllerId()));
            });
        } else if (controllerApis.containsKey(credentials)) {
            return CompletableFuture.runAsync(() -> {
                controllerApis.remove(credentials);
                EventBus.getInstance().post(new ProxyClosed(credentials.getUser().name(), credentials.getControllerId()));
            });
        } else {
            return CompletableFuture.completedFuture(null);
        }
    }

    private ProxyContext start(ProxyCredentials credentials) throws ControllerConnectionRefusedException {
        if (!controllerFutures.containsKey(credentials)) {
            controllerFutures.put(credentials, new ProxyContext(loadApi(credentials), credentials));
        }
        return controllerFutures.get(credentials);
    }
    
    private boolean restart(ProxyCredentials credentials, boolean force) throws ControllerConnectionRefusedException {
        // contains if controller id and account is equal (see ProxyCredentials.equals)
        if (controllerFutures.containsKey(credentials)) {
            if (!force) {
                // "identical" checks equality inclusive the urls
                // restart not necessary if a proxy with identically credentials already started
                if (controllerFutures.keySet().stream().anyMatch(key -> key.identical(credentials))) {
                    appointNodes(credentials, controllerApis.get(credentials));
                    return false;
                }
            }
            reloadApi(credentials);
            controllerFutures.get(credentials).restart(loadApi(credentials), credentials);
            return true;
        }
        return false;
    }
    
    protected JControllerApi loadApi(ProxyCredentials credentials) throws ControllerConnectionRefusedException {
        if (!controllerApis.containsKey(credentials)) {
            controllerApis.put(credentials, newControllerApi(credentials));
        }
        return controllerApis.get(credentials);
    }
    
    private boolean reloadApi(ProxyCredentials credentials) throws ControllerConnectionRefusedException {
        if (controllerApis.containsKey(credentials)) {
            // "identical" checks equality inclusive the urls
            // restart not necessary if a proxy with identically credentials already started
            if (controllerApis.keySet().stream().anyMatch(key -> key.identical(credentials))) {
                appointNodes(credentials, controllerApis.get(credentials));
                return false;
            }
            controllerApis.get(credentials).stop();
            controllerApis.put(credentials, newControllerApi(credentials));
            return true;
        }
        return false;
    }
    
    private JControllerApi newControllerApi(ProxyCredentials credentials) {
        return appointNodes(credentials, ControllerApiContext.newControllerApi(proxyContext, credentials));
    }
    
    private JControllerApi appointNodes(ProxyCredentials credentials, JControllerApi api) {
        if (api != null && credentials.getBackupUrl() != null && ProxyUser.JOC.equals(credentials.getUser())) {
            ClusterWatch.getInstance().appointNodes(credentials.getControllerId(), api, null, null, null);
        }
        return api;
    }

}
