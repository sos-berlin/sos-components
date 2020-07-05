package com.sos.joc.classes.proxy;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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

import js7.proxy.javaapi.JMasterProxy;
import js7.proxy.javaapi.data.JHttpsConfig;

public class Proxies {

    private static final Logger LOGGER = LoggerFactory.getLogger(Proxies.class);
    private static Proxies proxies;
    private volatile Map<ProxyCredentials, CompletableFuture<JMasterProxy>> controllerFutures = new ConcurrentHashMap<>();

    private Proxies() {
        //
    }

    public static Proxies getInstance() {
        if (proxies == null) {
            proxies = new Proxies();
        }
        return proxies;
    }

    protected JMasterProxy of(ProxyCredentials credentials, int connectionTimeout) throws JobSchedulerConnectionResetException, ExecutionException,
            JobSchedulerConnectionRefusedException {
        try {
            connectionTimeout = Math.max(20 * 1000, connectionTimeout);
            return start(credentials).get(connectionTimeout, TimeUnit.MILLISECONDS);
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

    public void startAll(JocCockpitProperties properties) {
        // for servlet init method
        SOSHibernateSession sosHibernateSession = null;
        try {
            JHttpsConfig httpsConfig = ProxyCredentialsBuilder.getHttpsConfig(properties);
            sosHibernateSession = Globals.createSosHibernateStatelessConnection("Proxies");
            new InventoryInstancesDBLayer(sosHibernateSession).getInventoryInstances().stream().map(dbItem -> ProxyCredentialsBuilder.withUrl(dbItem
                    .getUri()).withHttpsConfig(httpsConfig).build()).forEach(credential -> start(credential));
        } catch (JocException e) {
            LOGGER.error("Start all proxies fails", e);
        } finally {
            Globals.disconnect(sosHibernateSession);
        }
    }

    public void startAll(ProxyCredentials ...credentials) {
        // for testing only
        Arrays.asList(credentials).stream().forEach(credential -> start(credential));
    }

    public void closeAll() {
        // for servlet destroy method
        try {
            CompletableFuture.allOf(controllerFutures.values().stream().map(future -> CompletableFuture.runAsync(() -> disconnect(future))).toArray(
                    CompletableFuture[]::new)).get();
            controllerFutures.clear();
        } catch (Exception e) {
        }
    }

    private CompletableFuture<JMasterProxy> start(ProxyCredentials credentials) {
        if (!controllerFutures.containsKey(credentials)) {
            CompletableFuture<JMasterProxy> future = JMasterProxy.start(credentials.getUrl(), credentials.getAccount(), credentials.getHttpsConfig());
            // future.whenComplete((proxy, ex) -> {
            // jMasterProxies.put(credentials, proxy);
            // });
            controllerFutures.put(credentials, future);
        }
        return controllerFutures.get(credentials);
    }

    private static void disconnect(CompletableFuture<JMasterProxy> future) {
        try {
            JMasterProxy proxy = future.getNow(null);
            if (proxy == null) {
                LOGGER.info(future.toString() + " will be cancelled");
                future.cancel(true);
            } else {
                LOGGER.info(proxy.toString() + " will be closed");
                proxy.close();
            }
        } catch (Exception e) {
        }
    }

}
