package com.sos.joc.classes.proxy;

import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JobSchedulerSSLCertificateException;

import js7.base.problem.Problem;
import js7.proxy.ProxyEvent;
import js7.proxy.ProxyEvent.ProxyCoupled;
import js7.proxy.ProxyEvent.ProxyCouplingError;
import js7.proxy.ProxyEvent.ProxyDecoupled$;
import js7.proxy.javaapi.JAdmission;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.JProxyContext;
import js7.proxy.javaapi.data.JHttpsConfig;
import js7.proxy.javaapi.eventbus.JStandardEventBus;

public class ProxyContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(Proxies.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private final CompletableFuture<JControllerProxy> proxyFuture;
    private final String url;
    private Optional<Problem> lastProblem = Optional.empty();
    private CompletableFuture<Void> coupledFuture = new CompletableFuture<>();

    protected ProxyContext(JProxyContext proxyContext, ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException {
        checkCredentials(credentials);
        this.url = credentials.getUrl();
        LOGGER.info("start Proxy of " + credentials.getUrl());
        JControllerApi controllerApi = proxyContext.newControllerApi(Arrays.asList(JAdmission.of(credentials.getUrl(), credentials.getAccount())),
                credentials.getHttpsConfig());
        this.proxyFuture = controllerApi.startProxy(getEventBus());
//        this.proxyFuture = proxyContext.startControllerProxy(credentials.getUrl(), credentials.getAccount(), credentials.getHttpsConfig(),
//                getEventBus());
//        JControllerProxy proxy = proxyContext.newControllerProxy(credentials.getUrl(), credentials.getAccount(), credentials.getHttpsConfig(), getEventBus(), new JControllerEventBus());
//        this.proxyFuture = proxy.startObserving().thenApply(u -> proxy);
    }

    protected JControllerProxy getProxy(long connectionTimeout) throws ExecutionException, JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException {
        try {
            long timeout = Math.max(0L, connectionTimeout);
            if (!coupledFuture.isDone()) {
                coupledFuture.get(timeout, TimeUnit.MILLISECONDS);
            } else if (coupledFuture.isCompletedExceptionally()) {
                coupledFuture.join();
            }
            return proxyFuture.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            if (e.getCause() != null) {
                throw new JobSchedulerConnectionResetException(e.getCause());
            }
            throw new JobSchedulerConnectionResetException(e);
        } catch (ExecutionException | CompletionException e) {
            if (e.getCause() != null && JobSchedulerSSLCertificateException.class.isInstance(e.getCause())) {
                throw (JobSchedulerSSLCertificateException) e.getCause();
            }
            throw e;
        } catch (TimeoutException e) {
            throw new JobSchedulerConnectionRefusedException(getLastErrorMessage(url));
        }
    }

    protected CompletableFuture<Void> stop() {
        JControllerProxy proxy = proxyFuture.getNow(null);
        if (proxy == null) {
            CompletableFuture<Void> stopfuture = new CompletableFuture<>();
            LOGGER.info(proxyFuture.toString() + " will be cancelled");
            proxyFuture.cancel(false);
            stopfuture.complete(null);
            return stopfuture;
        } else {
            LOGGER.info(proxy.toString() + " will be stopped");
            return proxy.stop();
            // proxy = null;
        }
    }

    private void onProxyCoupled(ProxyCoupled proxyCoupled) {
        LOGGER.info(proxyCoupled.toString());
        lastProblem = Optional.empty();
        if (!coupledFuture.isDone()) {
            coupledFuture.complete(null);
        }
    }

    private void onProxyDecoupled(ProxyDecoupled$ proxyDecoupled) {
        LOGGER.info(proxyDecoupled.toString());
        if (coupledFuture.isDone()) {
            coupledFuture = new CompletableFuture<>();
        }
    }

    private void onProxyCouplingError(ProxyCouplingError proxyCouplingError) {
        if (isDebugEnabled) {
            LOGGER.debug(proxyCouplingError.toString());
        }
        lastProblem = Optional.of(proxyCouplingError.problem());
        if (lastProblem.isPresent()) {
            String msg = lastProblem.get().messageWithCause();
            if (msg != null && (msg.contains("javax.net.ssl.SSLHandshakeException") || msg.contains("java.security.cert.CertificateException"))) {
                if (!coupledFuture.isDone()) {
                    coupledFuture.completeExceptionally(new JobSchedulerSSLCertificateException(msg));
                }
            }
        }
    }

    private String getLastErrorMessage(String defaultMessage) {
        if (lastProblem.isPresent()) {
            return lastProblem.get().messageWithCause();
        }
        return defaultMessage;
    }

    private void checkCredentials(ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException {
        if (credentials.getUrl() == null) {
            throw new JobSchedulerConnectionRefusedException("URL is undefined");
        } else if (credentials.getUrl().startsWith("https://")) {
            JHttpsConfig httpsConfig = credentials.getHttpsConfig();
            if (httpsConfig.trustStoreRefs() == null || httpsConfig.trustStoreRefs().isEmpty()) {
                throw new JobSchedulerConnectionRefusedException("Required truststore not found");
            } else if (credentials.getAccount().toUnderlying().isEmpty() && !httpsConfig.keyStoreFile().isPresent()) {
                throw new JobSchedulerConnectionRefusedException("Neither account is specified nor client certificate was found");
            }
        }
    }

    private JStandardEventBus<ProxyEvent> getEventBus() {
        JStandardEventBus<ProxyEvent> proxyEventBus = new JStandardEventBus<>(ProxyEvent.class);
        proxyEventBus.subscribe(Arrays.asList(ProxyCoupled.class), this::onProxyCoupled);
        proxyEventBus.subscribe(Arrays.asList(ProxyDecoupled$.class), this::onProxyDecoupled);
        proxyEventBus.subscribe(Arrays.asList(ProxyCouplingError.class), this::onProxyCouplingError);
        return proxyEventBus;
    }
}
