package com.sos.joc.classes.proxy;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.exceptions.JobSchedulerAuthorizationException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JobSchedulerSSLCertificateException;
import com.sos.joc.exceptions.ProxyNotCoupledException;

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
    private CompletableFuture<JControllerProxy> proxyFuture;
    private Optional<Problem> lastProblem = Optional.empty();
    private CompletableFuture<Void> coupledFuture;
    private ProxyCredentials credentials;

    protected ProxyContext(JProxyContext proxyContext, ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException {
        this.credentials = credentials;
        start(proxyContext, credentials);
    }

    protected JControllerProxy getProxy(long connectionTimeout) throws ExecutionException, JobSchedulerConnectionResetException,
            JobSchedulerConnectionRefusedException {
        try {
            long timeout = Math.max(0L, connectionTimeout);
            coupledFuture.get(timeout, TimeUnit.MILLISECONDS);
            return proxyFuture.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            if (e.getCause() != null) {
                throw new JobSchedulerConnectionResetException(e.getCause());
            }
            throw new JobSchedulerConnectionResetException(e);
        } catch (ExecutionException e) {
            if (e.getCause() != null && ProxyNotCoupledException.class.isInstance(e.getCause())) {
                throw (ProxyNotCoupledException) e.getCause();
            }
            throw e;
        } catch (TimeoutException e) {
            throw new JobSchedulerConnectionRefusedException(getLastErrorMessage(toString()));
        }
    }
    
    protected boolean restart(JProxyContext proxyContext, ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException {
        if (!this.credentials.identical(credentials)) {
            stop();
            start(proxyContext, credentials);
            return true;
        }
        return false;
    }
    
    protected void start(JProxyContext proxyContext, ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException {
        LOGGER.info(String.format("start Proxy of %s", toString()));
        checkCredentials(credentials);
        List<JAdmission> admissions = null;
        if (credentials.getBackupUrl() != null) {
            admissions = Arrays.asList(JAdmission.of(credentials.getUrl(), credentials.getAccount()), JAdmission.of(credentials.getBackupUrl(),
                    credentials.getAccount()));
        } else {
            admissions = Arrays.asList(JAdmission.of(credentials.getUrl(), credentials.getAccount()));
        }
        JControllerApi controllerApi = proxyContext.newControllerApi(admissions, credentials.getHttpsConfig());
        this.proxyFuture = controllerApi.startProxy(getProxyEventBus());
        this.coupledFuture = startMonitorFuture(60);
    }

    protected CompletableFuture<Void> stop() {
        JControllerProxy proxy = proxyFuture.getNow(null);
        if (proxy == null) {
            LOGGER.info(String.format("%s of '%s' will be cancelled", proxyFuture.toString(), toString()));
            return CompletableFuture.runAsync(() -> proxyFuture.cancel(false));
        } else {
            LOGGER.info(String.format("%s of '%s' will be stopped", proxy.toString(), toString()));
            return proxy.stop();
        }
    }
    
    @Override
    public String toString() {
        if (credentials.getBackupUrl() != null) {
            return String.format("'%s' cluster (%s, %s)", credentials.getJobSchedulerId(), credentials.getUrl(), credentials.getBackupUrl());
        } else {
            return String.format("'%s' (%s)", credentials.getJobSchedulerId(), credentials.getUrl());
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
            coupledFuture = startMonitorFuture(60);
        }
    }

    private void onProxyCouplingError(ProxyCouplingError proxyCouplingError) {
        if (isDebugEnabled) {
            LOGGER.debug(proxyCouplingError.toString());
        }
        lastProblem = Optional.of(proxyCouplingError.problem());
        if (lastProblem.isPresent()) {
            String msg = lastProblem.get().messageWithCause();
            if (msg != null) {
                if (msg.matches(".*javax\\.net\\.ssl\\.SSL[a-zA-Z]*Exception.*") || msg.matches(".*java\\.security\\.cert\\.Certificate[a-zA-Z]*Exception.*")) {
                    if (!coupledFuture.isDone()) {
                        coupledFuture.completeExceptionally(new JobSchedulerSSLCertificateException(msg));
                    }
                } else if (msg.contains("HTTP 401")) {
                    if (!coupledFuture.isDone()) {
                        coupledFuture.completeExceptionally(new JobSchedulerAuthorizationException(msg));
                    }
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
        } else if (credentials.getUrl().startsWith("https://") || (credentials.getBackupUrl() != null && credentials.getBackupUrl().startsWith(
                "https://"))) {
            JHttpsConfig httpsConfig = credentials.getHttpsConfig();
            if (httpsConfig.trustStoreRefs() == null || httpsConfig.trustStoreRefs().isEmpty()) {
                throw new JobSchedulerConnectionRefusedException("Required truststore not found");
            } else if (credentials.getAccount().toUnderlying().isEmpty() && !httpsConfig.keyStoreFile().isPresent()) {
                throw new JobSchedulerConnectionRefusedException("Neither account is specified nor client certificate was found");
            }
        }
    }

    private JStandardEventBus<ProxyEvent> getProxyEventBus() {
        JStandardEventBus<ProxyEvent> proxyEventBus = new JStandardEventBus<>(ProxyEvent.class);
        proxyEventBus.subscribe(Arrays.asList(ProxyCoupled.class), this::onProxyCoupled);
        proxyEventBus.subscribe(Arrays.asList(ProxyDecoupled$.class), this::onProxyDecoupled);
        proxyEventBus.subscribe(Arrays.asList(ProxyCouplingError.class), this::onProxyCouplingError);
        return proxyEventBus;
    }
    
    private static CompletableFuture<Void> startMonitorFuture(int seconds) {
        return CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(seconds);
                throw new ProxyNotCoupledException("Even after one minute the proxy could not reconnect to the controller.");
            } catch (InterruptedException e) {
                //
            }
        });
    }
}
