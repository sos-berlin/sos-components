package com.sos.joc.classes.proxy;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;

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
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private final CompletableFuture<JControllerProxy> proxyFuture;
    private final String url;
    private Optional<Problem> lastProblem = Optional.empty();
    private CompletableFuture<Void> coupledFuture = new CompletableFuture<>();

    protected ProxyContext(JProxyContext proxyContext, ProxyCredentials credentials) {
        url = credentials.getUrl();
        JStandardEventBus<ProxyEvent> proxyEventBus = new JStandardEventBus<>(ProxyEvent.class);
        proxyEventBus.subscribe(Arrays.asList(ProxyCoupled.class), this::onProxyCoupled);
        proxyEventBus.subscribe(Arrays.asList(ProxyDecoupled$.class), this::onProxyDecoupled);
        proxyEventBus.subscribe(Arrays.asList(ProxyCouplingError.class), this::onProxyCouplingError);
        proxyFuture = proxyContext.startControllerProxy(credentials.getUrl(), credentials.getAccount(), credentials.getHttpsConfig(), proxyEventBus);
    }

    protected CompletableFuture<JControllerProxy> getProxyFuture() {
        return proxyFuture;
    }

    protected JControllerProxy getProxy(long connectionTimeout) throws ExecutionException, JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException {
        try {
            long timeout = Math.max(0L, connectionTimeout);
            if (!coupledFuture.isDone()) {
                coupledFuture.get(timeout, TimeUnit.MILLISECONDS);
            }
            return proxyFuture.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            if (e.getCause() != null) {
                throw new JobSchedulerConnectionResetException(e.getCause());
            }
            throw new JobSchedulerConnectionResetException(e);
        } catch (TimeoutException e) {
            throw new JobSchedulerConnectionRefusedException(getLastErrorMessage(url));
        }
    }
    
    protected CompletableFuture<Void> stop() {
        CompletableFuture<Void> stopfuture = new CompletableFuture<>();
        JControllerProxy proxy = proxyFuture.getNow(null);
        if (proxy == null) {
            LOGGER.info(proxyFuture.toString() + " will be cancelled");
            proxyFuture.cancel(true);
            stopfuture.complete(null);
        } else {
            LOGGER.info(proxy.toString() + " will be closed");
            stopfuture = proxy.stop();
            //proxy = null;
        }
        return stopfuture;
    }

    private void onProxyCoupled(ProxyCoupled proxyCoupled) {
        if (isDebugEnabled) {
            LOGGER.debug(proxyCoupled.toString());
        }
        lastProblem = Optional.empty();
        if (!coupledFuture.isDone()) {
            coupledFuture.complete(null);
        }
    }

    private void onProxyDecoupled(ProxyDecoupled$ proxyDecoupled) {
        if (isDebugEnabled) {
            LOGGER.debug(proxyDecoupled.toString());
        }
        if (coupledFuture.isDone()) {
            coupledFuture = new CompletableFuture<>();
        }
    }

    private void onProxyCouplingError(ProxyCouplingError proxyCouplingError) {
        if (isDebugEnabled) {
            LOGGER.debug(proxyCouplingError.toString());
        }
        lastProblem = Optional.of(proxyCouplingError.problem());
    }

    private String getLastErrorMessage(String defaultMessage) {
        if (lastProblem.isPresent()) {
            return lastProblem.get().messageWithCause();
        }
        return defaultMessage;
    }
}
