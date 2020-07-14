package com.sos.joc.classes.proxy;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.db.inventory.instance.InventoryInstancesDBLayer;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JocException;

import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.JProxyContext;
import js7.proxy.javaapi.data.JHttpsConfig;

public class Proxies {

    private static final Logger LOGGER = LoggerFactory.getLogger(Proxies.class);
    private static Proxies proxies;
    private static JProxyContext proxyContext = new JProxyContext();
    private volatile Map<ProxyCredentials, ProxyContext> controllerFutures = new ConcurrentHashMap<>();

    private Proxies() {
        //
    }

    public static Proxies getInstance() {
        if (proxies == null) {
            proxies = new Proxies();
            if (proxyContext == null) {
                proxyContext = new JProxyContext();
            }
        }
        return proxies;
    }

    protected ProxyContext of(ProxyCredentials credentials, long connectionTimeout) throws JobSchedulerConnectionResetException, ExecutionException,
            JobSchedulerConnectionRefusedException {
        try {
            //TODO consider ProxyEvents
            return start(credentials).getProxy(connectionTimeout);
        } catch (InterruptedException e) {
            if (e.getCause() != null) {
                throw new JobSchedulerConnectionResetException(e.getCause());
            }
            throw new JobSchedulerConnectionResetException(e);
        } catch (CancellationException e) {
            proxies.controllerFutures.remove(credentials);
            throw new JobSchedulerConnectionResetException(credentials.getUrl());
        } catch (TimeoutException e) {
            throw new JobSchedulerConnectionRefusedException(credentials.getUrl());
        }
    }

    protected void close(ProxyCredentials credentials) {
        if (controllerFutures.containsKey(credentials)) {
            disconnect(controllerFutures.get(credentials));
            controllerFutures.remove(credentials);
        }
    }

    protected ProxyContext start(ProxyCredentials credentials) {    
        if (!controllerFutures.containsKey(credentials)) {
            controllerFutures.put(credentials, new ProxyContext(proxyContext, credentials));
        }
        return controllerFutures.get(credentials);
    }

    public void startAll(JocCockpitProperties properties) {
        // for servlet init method
        LOGGER.info("starting all proxies");
        SOSHibernateSession sosHibernateSession = null;
        try {
            JHttpsConfig httpsConfig = ProxyCredentialsBuilder.getHttpsConfig(properties);
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("Proxies");
            new InventoryInstancesDBLayer(sosHibernateSession).getInventoryInstances().stream().map(dbItem -> ProxyCredentialsBuilder.withUrl(dbItem
                    .getUri()).withHttpsConfig(httpsConfig).build()).forEach(credential -> start(credential));
        } catch (JocException e) {
            LOGGER.error("starting all proxies failed", e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public void startAll(ProxyCredentials ...credentials) {
        // for testing only
        LOGGER.info("starting all proxies");
        Arrays.asList(credentials).stream().forEach(credential -> start(credential));
    }

    public void closeAll() {
        // for servlet destroy method
        LOGGER.info("closing all proxies ...");
        try {
            CompletableFuture.allOf(controllerFutures.values().stream().map(future -> CompletableFuture.runAsync(() -> disconnect(future))).toArray(
                    CompletableFuture[]::new)).thenRun(() -> controllerFutures.clear()).get();
        } catch (Exception e) {
        } finally {
            try {
                proxyContext.close();
            } catch (Exception e) {
            } finally {
                proxyContext = null;
            }
        }
    }

    private static void disconnect(ProxyContext context) {
        try {
            CompletableFuture<JControllerProxy> future = context.getProxyFuture();
            JControllerProxy proxy = future.getNow(null);
            if (proxy == null) {
                LOGGER.info(future.toString() + " will be cancelled");
                future.cancel(true);
            } else {
                LOGGER.info(proxy.toString() + " will be closed");
                proxy.stop().get();
            }
        } catch (Exception e) {
        }
    }

}
