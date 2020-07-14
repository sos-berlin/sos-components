package com.sos.joc.classes.proxy;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import js7.base.problem.Problem;
import js7.proxy.ProxyEvent;
import js7.proxy.ProxyEvent.ProxyCoupled;
import js7.proxy.ProxyEvent.ProxyCouplingError;
import js7.proxy.ProxyEvent.ProxyDecoupled$;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.JProxyContext;
import js7.proxy.javaapi.JStandardEventBus;

public class ProxyContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(Proxies.class);
    private CompletableFuture<JControllerProxy> proxyFuture;
    private JControllerProxy proxy;
    private boolean coupled = false;
    private Optional<Problem> lastProblem = Optional.empty();
    // private final CompletableFuture<Void> coupled = new CompletableFuture<>();
    // private final CompletableFuture<Void> decoupled = new CompletableFuture<>();
    // private final CompletableFuture<Problem> firstProblem = new CompletableFuture<>();

    public ProxyContext(JProxyContext proxyContext, ProxyCredentials credentials) {
        JStandardEventBus<ProxyEvent> proxyEventBus = new JStandardEventBus<>(ProxyEvent.class);
        proxyEventBus.subscribe(Arrays.asList(ProxyCoupled.class), this::onProxyCoupled);
        proxyEventBus.subscribe(Arrays.asList(ProxyDecoupled$.class), this::onProxyDecoupled);
        proxyEventBus.subscribe(Arrays.asList(ProxyCouplingError.class), this::onProxyCouplingError);
        proxyFuture = proxyContext.startControllerProxy(credentials.getUrl(), credentials.getAccount(), credentials.getHttpsConfig(), proxyEventBus);
    }
    
    private void onProxyCoupled(ProxyCoupled proxyCoupled) {
        LOGGER.info(proxyCoupled.toString());
        // coupled.complete(null);
        coupled = true;
    }
    
    private void onProxyDecoupled(ProxyDecoupled$ proxyDecoupled) {
        LOGGER.info(proxyDecoupled.toString());
        // decoupled.complete(null);
        coupled = false;
    }

    private void onProxyCouplingError(ProxyCouplingError proxyCouplingError) {
        LOGGER.info(proxyCouplingError.toString());
        // firstProblem.complete(proxyCouplingError.problem());
        lastProblem = Optional.of(proxyCouplingError.problem());
    }

    public Optional<Problem> getLastProblem() {
        return lastProblem;
    }

    public boolean isCoupled() {
        return coupled;
    }

    protected CompletableFuture<JControllerProxy> getProxyFuture() {
        return proxyFuture;
    }

    protected ProxyContext getProxy(long connectionTimeout) throws InterruptedException, ExecutionException, TimeoutException {
        proxy = proxyFuture.get(Math.max(0L, connectionTimeout), TimeUnit.MILLISECONDS);
        return this;
    }
    
    public JControllerProxy get() {
        return proxy;
    }

}
