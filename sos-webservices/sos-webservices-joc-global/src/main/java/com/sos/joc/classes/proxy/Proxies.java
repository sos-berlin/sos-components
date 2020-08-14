package com.sos.joc.classes.proxy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.bean.proxy.ProxyRemoved;
import com.sos.joc.event.bean.proxy.ProxyRestarted;
import com.sos.joc.event.bean.proxy.ProxyStarted;
import com.sos.joc.exceptions.DBConnectionRefusedException;
import com.sos.joc.exceptions.DBInvalidDataException;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.DBOpenSessionException;
import com.sos.joc.exceptions.JobSchedulerAuthorizationException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocConfigurationException;
import com.sos.joc.exceptions.JocException;

import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.JProxyContext;
import js7.proxy.javaapi.data.JHttpsConfig;

public class Proxies {

    private static final Logger LOGGER = LoggerFactory.getLogger(Proxies.class);
    private static Proxies proxies;
    private static JProxyContext proxyContext = new JProxyContext();
    private volatile Map<ProxyCredentials, ProxyContext> controllerFutures = new ConcurrentHashMap<>();
    private volatile Map<String, List<DBItemInventoryJSInstance>> controllerDbInstances = new ConcurrentHashMap<>();
    
    private Proxies() {
    }

    public static Proxies getInstance() {
        if (proxies == null) {
            proxies = new Proxies();
        }
        if (proxyContext == null) {
            proxyContext = new JProxyContext();
        }
        return proxies;
    }
    
    /**
     * Returns a JControllerProxy according the credentials specified by jobschedulerId and account (and starts it if necessary)
     * @param jobschedulerId
     * @param account
     * @param connectionTimeout
     * @return JControllerProxy
     * @throws JobSchedulerConnectionResetException
     * @throws JobSchedulerConnectionRefusedException 
     *      if the asynchronous started Proxy not available within the specified connectionTimeout
     *      or a an SSL handshake error occurs* @throws DBMissingDataException
     * @throws ExecutionException
     */
    protected JControllerProxy of(String jobschedulerId, ProxyUser account, long connectionTimeout) throws JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException, DBMissingDataException, ExecutionException {
        return of(ProxyCredentialsBuilder.withDbInstancesOfCluster(controllerDbInstances.get(jobschedulerId)).withAccount(account)
                .build(), connectionTimeout);
    }
    
    /**
     * Closes a started Proxy according the credentials specified by jobschedulerId and account
     * @param jobschedulerId
     * @param account
     * @return CompletableFuture&lt;Void&gt;
     * @throws DBMissingDataException
     */
    protected CompletableFuture<Void> close(String jobschedulerId, ProxyUser account) throws DBMissingDataException {
        return close(ProxyCredentialsBuilder.withDbInstancesOfCluster(controllerDbInstances.get(jobschedulerId)).withAccount(account).build());
    }
    
    /**
     * Starts Proxy with according credentials specified by jobschedulerId and account
     * @param jobschedulerId
     * @param account
     * @return ProxyContext
     * @throws JobSchedulerConnectionRefusedException
     * @throws DBMissingDataException
     */
    protected ProxyContext start(String jobschedulerId, ProxyUser account) throws JobSchedulerConnectionRefusedException, DBMissingDataException {
        return start(ProxyCredentialsBuilder.withDbInstancesOfCluster(controllerDbInstances.get(jobschedulerId)).withAccount(account).build());
    }
    
    /**
     * Closes all Proxies with specified 'jobschedulerId' inside its credentials
     * Should only called from provisioning dialogue 
     * @param jobschedulerId
     */
    protected void removeProxies(final String jobschedulerId) {
        proxiesOfJobSchedulerId(jobschedulerId).forEach((key, proxy) -> {
            proxy.stop();
            controllerFutures.remove(key);
            EventBus.getInstance().post(new ProxyRemoved(key.getUser().name(), jobschedulerId, null));
        });
        controllerDbInstances.remove(jobschedulerId);
    }
    
    /**
     * Restarts Proxies with credentials from specified database instances  
     * Should only called from provisioning dialogue
     * @param controllerDbInstances
     * @throws DBMissingDataException 
     * @throws JobSchedulerConnectionRefusedException 
     */
    protected void updateProxies(final List<DBItemInventoryJSInstance> controllerDbInstances) throws DBMissingDataException,
            JobSchedulerConnectionRefusedException {
        if (controllerDbInstances != null && !controllerDbInstances.isEmpty()) {
            String jobschedulerId = controllerDbInstances.get(0).getSchedulerId();
            boolean isNew = !this.controllerDbInstances.containsKey(jobschedulerId);
            this.controllerDbInstances.put(jobschedulerId, controllerDbInstances);
            for (ProxyUser account : ProxyUser.values()) {
                // Proxies of history in an inactive joc cluster member is possibly not started
                if (account == ProxyUser.JOC || proxiesOfUserAreAlreadyStarted(account)) {
                    if (isNew) {
                        start(ProxyCredentialsBuilder.withDbInstancesOfCluster(controllerDbInstances).withAccount(account).build());
                        EventBus.getInstance().post(new ProxyStarted(account.name(), jobschedulerId, null));
                    } else {
                        if (restart(ProxyCredentialsBuilder.withDbInstancesOfCluster(controllerDbInstances).withAccount(account).build())) {
                            EventBus.getInstance().post(new ProxyRestarted(account.name(), jobschedulerId, null));
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Returns map of Controller database instances where the key is the JobSchedulerId and the
     * value is always a list. The list contains one or two members depending on stand-alone or cluster. 
     * @return Map&lt;String, List&lt;DBItemInventoryJSInstance&gt;&gt;
     */
    public static Map<String, List<DBItemInventoryJSInstance>> getControllerDbInstances() {
        return Proxies.getInstance().controllerDbInstances;
    }
    
    /**
     * Starts all Proxies from db 'instances' table for specified user. Should be called in servlet 'init' method 
     * @param properties (from ./resources/joc/joc.properties to get keystore and truststore information)
     * @param account
     */
    public void startAll(final JocCockpitProperties properties, ProxyUser account) {
        LOGGER.info(String.format("starting all proxies for user %s ...", account));
        SOSHibernateSession sosHibernateSession = null;
        try {
            JHttpsConfig httpsConfig = ProxyCredentialsBuilder.getHttpsConfig(properties);
            initControllerDbInstances();
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
                    start(credential);
                } catch (JobSchedulerConnectionRefusedException e) {
                    LOGGER.error("", e);
                }
            });
        } catch (JocException e) {
            LOGGER.error("starting all proxies failed", e);
        } finally {
            Globals.disconnect(sosHibernateSession);
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
            } catch (JobSchedulerConnectionRefusedException e) {
                LOGGER.error("", e);
            }
        });
    }

    /**
     * Closes all started Proxies. Should be called in servlet 'destroy' method
     */
    public void closeAll() {
        LOGGER.info("closing all proxies ...");
        try {
            CompletableFuture.allOf(controllerFutures.values().stream()
                .map(future -> CompletableFuture.runAsync(() -> future.stop())).toArray(CompletableFuture[]::new))
                .thenRun(() -> {
                    controllerFutures.clear();
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
    
    private void initControllerDbInstances() throws JocConfigurationException, DBOpenSessionException, DBInvalidDataException,
            DBConnectionRefusedException {
        SOSHibernateSession sosHibernateSession = null;
        try {
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("Proxies");
            controllerDbInstances = new InventoryInstancesDBLayer(sosHibernateSession).getInventoryInstances().stream().collect(Collectors.groupingBy(
                    DBItemInventoryJSInstance::getSchedulerId));
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    private Map<ProxyCredentials, ProxyContext> proxiesOfJobSchedulerId(final String jobschedulerId) {
        return controllerFutures.entrySet().stream().filter(entry -> entry.getKey().getJobSchedulerId().equals(jobschedulerId)).collect(Collectors
                .toMap(Entry::getKey, Entry::getValue));
    }
    
    private boolean proxiesOfUserAreAlreadyStarted(final ProxyUser acount) {
        return controllerFutures.keySet().stream().anyMatch(key -> key.getUser().equals(acount));
    }
    
    /**
     * only protected for Unit tests
     * @param credentials
     * @param connectionTimeout
     * @return
     * @throws JobSchedulerConnectionResetException
     * @throws ExecutionException
     * @throws JobSchedulerConnectionRefusedException
     */
    protected JControllerProxy of(ProxyCredentials credentials, long connectionTimeout) throws JobSchedulerConnectionResetException, ExecutionException,
            JobSchedulerConnectionRefusedException {
        ProxyContext context = start(credentials);
        try {
            return context.getProxy(connectionTimeout);
        } catch (JobSchedulerAuthorizationException e) {
            close(credentials);
            throw e;
        } catch (CancellationException e) {
            close(credentials);
            throw new JobSchedulerConnectionResetException(credentials.getJobSchedulerId());
        }
    }

    private CompletableFuture<Void> close(ProxyCredentials credentials) {
        if (controllerFutures.containsKey(credentials)) {
            return controllerFutures.get(credentials).stop().thenRun(() -> controllerFutures.remove(credentials));
        } else {
            CompletableFuture<Void> closeFuture = new CompletableFuture<>();
            closeFuture.complete(null);
            return closeFuture;
        }
    }

    private ProxyContext start(ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException {
        if (!controllerFutures.containsKey(credentials)) {
            controllerFutures.put(credentials, new ProxyContext(proxyContext, credentials));
        }
        return controllerFutures.get(credentials);
    }
    
    private boolean restart(ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException {
        if (controllerFutures.containsKey(credentials)) {
            return controllerFutures.get(credentials).restart(proxyContext, credentials);
        }
        return false;
    }

}
