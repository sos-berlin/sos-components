package com.sos.joc.classes.proxy;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.jobscheduler.model.cluster.ClusterWatcher;
import com.sos.jobscheduler.model.cluster.IdToUri;
import com.sos.jobscheduler.model.command.ClusterAppointNodes;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.exceptions.JobSchedulerAuthorizationException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;
import com.sos.joc.exceptions.JobSchedulerSSLCertificateException;
import com.sos.joc.exceptions.ProxyNotCoupledException;

import io.vavr.control.Either;
import js7.base.problem.Problem;
import js7.data.cluster.ClusterSetting.Watch;
import js7.proxy.data.ProxyEvent;
import js7.proxy.data.ProxyEvent.ProxyCoupled;
import js7.proxy.data.ProxyEvent.ProxyCouplingError;
import js7.proxy.data.ProxyEvent.ProxyDecoupled$;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.JProxyContext;
import js7.proxy.javaapi.data.cluster.JClusterState;
import js7.proxy.javaapi.eventbus.JStandardEventBus;

public class ProxyContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyContext.class);
    private static final boolean isDebugEnabled = LOGGER.isDebugEnabled();
    private CompletableFuture<JControllerProxy> proxyFuture;
    private Optional<Problem> lastProblem = Optional.empty();
    private CompletableFuture<Void> coupledFuture;
    private boolean coupled;
    private ProxyCredentials credentials;

    protected ProxyContext(JProxyContext proxyContext, ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException {
        this.credentials = credentials;
        start(ControllerApiContext.newControllerApi(proxyContext, credentials));
    }
    
    protected ProxyContext(JControllerApi controllerApi, ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException {
        this.credentials = credentials;
        start(controllerApi);
    }
    
    public CompletableFuture<JControllerProxy> getProxyFuture() {
        return proxyFuture;
    }
    
    public boolean isCoupled() {
        return coupled;
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
    
    protected void restart(JControllerApi controllerApi, ProxyCredentials credentials) throws JobSchedulerConnectionRefusedException {
        stop();
        this.credentials = credentials;
        start(controllerApi);
    }
    
    protected void start(JControllerApi controllerApi) throws JobSchedulerConnectionRefusedException {
        LOGGER.info(String.format("start Proxy of %s", toString()));
        this.proxyFuture = controllerApi.startProxy(getProxyEventBus());
        this.coupledFuture = startMonitorFuture(120);
    }

    protected CompletableFuture<Void> stop() {
        JControllerProxy proxy = proxyFuture.getNow(null);
        if (proxy == null) {
            LOGGER.info(String.format("%s of %s will be cancelled", proxyFuture.toString(), toString()));
            return CompletableFuture.runAsync(() -> proxyFuture.cancel(false));
        } else {
            LOGGER.info(String.format("%s of %s will be stopped", proxy.toString(), toString()));
            return proxy.stop();
        }
    }
    
    @Override
    public String toString() {
        if (credentials.getBackupUrl() != null) {
            return String.format("'%s' cluster (%s, %s)", credentials.getControllerId(), credentials.getUrl(), credentials.getBackupUrl());
        } else {
            return String.format("'%s' (%s)", credentials.getControllerId(), credentials.getUrl());
        }
    }
    
    private void checkCluster() {
        if (credentials.getBackupUrl() != null) { // is Cluster
            LOGGER.info(toString() + ": check cluster appointment");
            proxyFuture.thenApply(p -> {
                Either<Problem, Void> either = null;
                JClusterState clusterState = p.currentState().clusterState();
                LOGGER.info(clusterState.toJson());
                if (clusterState.toJson().replaceAll("\\s", "").contains("\"TYPE\":\"Empty\"")) { // not appointed
                    Either<Problem, List<Watch>> clusterWatchers = Proxies.getClusterWatchers(credentials.getControllerId());
                    if (clusterWatchers.isRight()) {
                        LOGGER.info("clusterWatchers: " + clusterWatchers.get().stream().map(w -> w.uri().string()).collect(Collectors.joining(", ")));
//                        NodeId activeId = NodeId.unchecked("Primary");
//                        Map<NodeId, Uri> idToUri = new HashMap<>();
//                        idToUri.put(activeId, Uri.of(credentials.getUrl()));
//                        idToUri.put(NodeId.unchecked("Backup"), Uri.of(credentials.getBackupUrl()));
//                        either = p.api().clusterAppointNodes(idToUri, activeId, clusterWatchers.get()).join();
                        
                        ClusterAppointNodes command = new ClusterAppointNodes();
                        command.setActiveId("Primary");
                        IdToUri idToUri = new IdToUri();
                        idToUri.getAdditionalProperties().put("Primary", credentials.getUrl());
                        idToUri.getAdditionalProperties().put("Backup", credentials.getBackupUrl());
                        command.setIdToUri(idToUri);
                        List<ClusterWatcher> cWatchers = clusterWatchers.get().stream().map(w -> {
                            ClusterWatcher watcher = new ClusterWatcher();
                            watcher.setUri(URI.create(w.uri().string()));
                            return watcher;
                        }).distinct().collect(Collectors.toList());
                        command.setClusterWatches(cWatchers);
                        try {
                            String json = Globals.objectMapper.writeValueAsString(command);
                            LOGGER.info(json);
                            Either<Problem, String> e = p.api().executeCommandJson(json).join();
                            if (e.isRight()) {
                                either = Either.right(null);
                            } else {
                                either = Either.left(e.getLeft());
                            }
                        } catch (JsonProcessingException e1) {
                            either = Either.left(Problem.pure(e1.toString()));
                        }
                    } else {
                        either = Either.left(clusterWatchers.getLeft());
                    }
                } else {
                    either = Either.right(null);
                }
                return either;
            }).thenAccept(e -> {
                if (e.isLeft()) {
                    LOGGER.info(ProblemHelper.getErrorMessage(e.getLeft()));
                }
            });
        }
    }

    private void onProxyCoupled(ProxyCoupled proxyCoupled) {
        LOGGER.info(toString() + ": " + proxyCoupled.toString());
        lastProblem = Optional.empty();
        coupled = true;
        checkCluster();
        if (!coupledFuture.isDone()) {
            coupledFuture.complete(null);
        }
    }

    private void onProxyDecoupled(ProxyDecoupled$ proxyDecoupled) {
        LOGGER.info(toString() + ": " + proxyDecoupled.toString());
        coupled = false;
    }

    private void onProxyCouplingError(ProxyCouplingError proxyCouplingError) {
        if (isDebugEnabled) {
            LOGGER.debug(this.credentials.getControllerId() + ": " + proxyCouplingError.toString());
        }
        lastProblem = Optional.of(proxyCouplingError.problem());
        coupled = false;
        if (lastProblem.isPresent()) {
            String msg = lastProblem.get().messageWithCause();
            if (msg != null) {
                if (msg.matches(".*javax\\.net\\.ssl\\.SSL[a-zA-Z]*Exception.*") || msg.matches(
                        ".*java\\.security\\.cert\\.Certificate[a-zA-Z]*Exception.*")) {
                    if (coupledFuture.isDone()) {
                        coupledFuture = new CompletableFuture<>();
                    }
                    coupledFuture.completeExceptionally(new JobSchedulerSSLCertificateException(toString() + ": " + msg));
                } else if (msg.contains("HTTP 401")) {
                    if (coupledFuture.isDone()) {
                        coupledFuture = new CompletableFuture<>();
                    }
                    coupledFuture.completeExceptionally(new JobSchedulerAuthorizationException(toString() + ": " + msg));
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

    private JStandardEventBus<ProxyEvent> getProxyEventBus() {
        JStandardEventBus<ProxyEvent> proxyEventBus = new JStandardEventBus<>(ProxyEvent.class);
        proxyEventBus.subscribe(Arrays.asList(ProxyCoupled.class), this::onProxyCoupled);
        proxyEventBus.subscribe(Arrays.asList(ProxyDecoupled$.class), this::onProxyDecoupled);
        proxyEventBus.subscribe(Arrays.asList(ProxyCouplingError.class), this::onProxyCouplingError);
        return proxyEventBus;
    }
    
    private CompletableFuture<Void> startMonitorFuture(int seconds) {
        return CompletableFuture.runAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(seconds);
                throw new ProxyNotCoupledException(String.format("Even after %ds the proxy %s couldn't (re)connect.", seconds, toString()));
            } catch (InterruptedException e) {
                //
            }
        });
    }
}
