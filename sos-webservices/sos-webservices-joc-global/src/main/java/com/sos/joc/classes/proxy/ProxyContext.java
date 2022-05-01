package com.sos.joc.classes.proxy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.Globals;
import com.sos.joc.classes.ProblemHelper;
import com.sos.joc.classes.calendar.DailyPlanCalendar;
import com.sos.joc.db.inventory.DBItemInventoryJSInstance;
import com.sos.joc.db.inventory.instance.InventoryAgentInstancesDBLayer;
import com.sos.joc.event.EventBus;
import com.sos.joc.exceptions.ControllerAuthorizationException;
import com.sos.joc.exceptions.JocBadRequestException;
import com.sos.joc.exceptions.ControllerConnectionRefusedException;
import com.sos.joc.exceptions.ControllerConnectionResetException;
import com.sos.joc.exceptions.ControllerSSLCertificateException;
import com.sos.joc.exceptions.ProxyNotCoupledException;

import io.vavr.control.Either;
import js7.base.auth.UserAndPassword;
import js7.base.problem.Problem;
import js7.base.web.Uri;
import js7.data.agent.AgentPath;
import js7.data.node.NodeId;
import js7.data.subagent.SubagentId;
import js7.data_for_java.agent.JAgentRef;
import js7.data_for_java.controller.JControllerState;
import js7.data_for_java.item.JUpdateItemOperation;
import js7.data_for_java.subagent.JSubagentItem;
import js7.proxy.data.event.ProxyEvent;
import js7.proxy.data.event.ProxyEvent.ProxyCoupled;
import js7.proxy.data.event.ProxyEvent.ProxyCouplingError;
import js7.proxy.data.event.ProxyEvent.ProxyDecoupled$;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.JProxyContext;
import js7.proxy.javaapi.eventbus.JStandardEventBus;
import reactor.core.publisher.Flux;
import scala.compat.java8.OptionConverters;

public class ProxyContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyContext.class);
    private CompletableFuture<JControllerProxy> proxyFuture;
    private Optional<Problem> lastProblem = Optional.empty();
    private CompletableFuture<Void> coupledFuture;
    private Boolean coupled = null;
    private ProxyCredentials credentials;

    protected ProxyContext(JProxyContext proxyContext, ProxyCredentials credentials) throws ControllerConnectionRefusedException {
        this.credentials = credentials;
        start(ControllerApiContext.newControllerApi(proxyContext, credentials));
    }
    
    protected ProxyContext(JControllerApi controllerApi, ProxyCredentials credentials) throws ControllerConnectionRefusedException {
        this.credentials = credentials;
        start(controllerApi);
    }
    
    public CompletableFuture<JControllerProxy> getProxyFuture() {
        return proxyFuture;
    }
    
    public Boolean isCoupled() {
        return coupled;
    }

    protected JControllerProxy getProxy(long connectionTimeout) throws ExecutionException, ControllerConnectionResetException,
            ControllerConnectionRefusedException {
        try {
            long timeout = Math.max(0L, connectionTimeout);
            coupledFuture.get(timeout, TimeUnit.MILLISECONDS);
            return proxyFuture.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            if (e.getCause() != null) {
                throw new ControllerConnectionResetException(e.getCause());
            }
            throw new ControllerConnectionResetException(e);
        } catch (ExecutionException e) {
            if (e.getCause() != null && ProxyNotCoupledException.class.isInstance(e.getCause())) {
                throw (ProxyNotCoupledException) e.getCause();
            }
            throw e;
        } catch (TimeoutException e) {
            throw new ControllerConnectionRefusedException(getLastErrorMessage(toString()));
        }
    }
    
    protected void restart(JControllerApi controllerApi, ProxyCredentials credentials) throws ControllerConnectionRefusedException {
        stop();
        this.credentials = credentials;
        start(controllerApi);
    }
    
    protected void start(JControllerApi controllerApi) throws ControllerConnectionRefusedException {
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
            proxyFuture.thenApplyAsync(p -> {
                Either<Problem, Void> either = null;
                if (p.currentState().clusterState().toJson().replaceAll("\\s", "").contains("\"TYPE\":\"Empty\"")) { // not appointed
                    try {
                        LOGGER.info("Cluster Nodes are not appointed");
                        List<DBItemInventoryJSInstance> controllerInstances = Proxies.getControllerDbInstances().get(credentials.getControllerId());
                        if (controllerInstances == null || controllerInstances.size() < 2) { // is not cluster
                            throw new JocBadRequestException("There is no cluster configured with the Id: " + credentials.getControllerId());
                        }
                        NodeId activeId = NodeId.of("Primary");
                        Map<NodeId, Uri> idToUri = new HashMap<>();
                        for (DBItemInventoryJSInstance inst : controllerInstances) {
                            idToUri.put(inst.getIsPrimary() ? activeId : NodeId.of("Backup"), Uri.of(inst.getClusterUri()));
                        }
                        p.api().clusterAppointNodes(idToUri, activeId, Proxies.getClusterWatchers(credentials.getControllerId(), null)).thenAccept(
                                e -> {
                                    if (e.isLeft()) {
                                        LOGGER.warn(ProblemHelper.getErrorMessage(e.getLeft()));
                                    } else {
                                        LOGGER.info("Appointing Cluster Nodes was successful");
                                    }
                                });
                        either = Either.right(null);
                    } catch (Exception e) {
                        either = Either.left(Problem.pure(e.toString()));
                    }
                } else {
                    either = Either.right(null);
                }
                return either;
            }).thenAccept(e -> {
                if (e.isLeft()) {
                    LOGGER.error(ProblemHelper.getErrorMessage(e.getLeft()));
                }
            });
        }
    }
    
    private void reDeployAgentsAndCalendar() {
        proxyFuture.thenAcceptAsync(p -> {
            SOSHibernateSession sosHibernateSession = null;
            try {
                sosHibernateSession = Globals.createSosHibernateStatelessConnection("GetAgents");
                InventoryAgentInstancesDBLayer dbLayer = new InventoryAgentInstancesDBLayer(sosHibernateSession);
                JControllerState currentState = p.currentState();
                Map<AgentPath, JAgentRef> controllerKnownAgents = currentState.pathToAgentRef();
                Map<SubagentId, JSubagentItem> controllerKnownSubagents = currentState.idToSubagentItem();
                Map<JAgentRef, List<JSubagentItem>> agents = Proxies.getUnknownAgents(credentials.getControllerId(), dbLayer, controllerKnownAgents,
                        controllerKnownSubagents, false);
                if (!agents.isEmpty()) {
                    LOGGER.info(toString() + ": Redeploy Agents");

                    Stream<JUpdateItemOperation> a = agents.keySet().stream().map(JUpdateItemOperation::addOrChangeSimple);
                    Stream<JUpdateItemOperation> s = agents.values().stream().flatMap(l -> l.stream().map(
                            JUpdateItemOperation::addOrChangeSimple));

                    p.api().updateItems(Flux.concat(Flux.fromStream(a), Flux.fromStream(s))).thenAccept(e -> {
                        if (e.isLeft()) {
                            LOGGER.error(ProblemHelper.getErrorMessage(e.getLeft()));
                        } else {
                            SOSHibernateSession connection1 = null;
                            try {
                                connection1 = Globals.createSosHibernateStatelessConnection("UpdateAgents");
                                connection1.setAutoCommit(false);
                                Globals.beginTransaction(connection1);
                                InventoryAgentInstancesDBLayer dbLayer1 = new InventoryAgentInstancesDBLayer(connection1);
                                dbLayer1.setAgentsDeployed(agents.keySet().stream().map(JAgentRef::path).map(AgentPath::string).collect(Collectors
                                        .toList()));
                                dbLayer1.setSubAgentsDeployed(agents.values().stream().flatMap(l -> l.stream()).map(JSubagentItem::path).map(
                                        SubagentId::string).collect(Collectors.toList()));
                                Globals.commit(connection1);
                            } catch (Exception e1) {
                                Globals.rollback(connection1);
                                LOGGER.error(e1.toString());
                            } finally {
                                Globals.disconnect(connection1);
                            }
                        }
                    });
                }
                DailyPlanCalendar.getInstance().updateDailyPlanCalendar(p.api(), currentState, toString());
            } catch (Exception e) {
                LOGGER.error(e.toString());
            } finally {
                Globals.disconnect(sosHibernateSession);
            }
        });
    }

    private void onProxyCoupled(ProxyCoupled proxyCoupled) {
        LOGGER.info(toString() + ": " + proxyCoupled.toString());
        lastProblem = Optional.empty();
        if (!Boolean.TRUE.equals(coupled)) {
            Optional<UserAndPassword> account = OptionConverters.toJava(this.credentials.getAccount().toScala());
            if (account.isPresent()) {
                EventBus.getInstance().post(new com.sos.joc.event.bean.proxy.ProxyCoupled(this.credentials.getControllerId(), true, this.credentials
                        .getUrl(), this.credentials.getBackupUrl(), account.get().userId().string(), account.get().password().string()));
            } else {
                EventBus.getInstance().post(new com.sos.joc.event.bean.proxy.ProxyCoupled(this.credentials.getControllerId(), true));
            }
        }
        coupled = true;
        if (!coupledFuture.isDone()) {
            coupledFuture.complete(null);
        }
        reDeployAgentsAndCalendar();
        checkCluster();
    }

    private void onProxyDecoupled(ProxyDecoupled$ proxyDecoupled) {
        LOGGER.info(toString() + ": " + proxyDecoupled.toString());
        if (!Boolean.FALSE.equals(coupled)) {
            EventBus.getInstance().post(new com.sos.joc.event.bean.proxy.ProxyCoupled(this.credentials.getControllerId(), false));
        }
        coupled = false;
    }

    private void onProxyCouplingError(ProxyCouplingError proxyCouplingError) {
        if (LOGGER.isDebugEnabled()) {
            LOGGER.debug(this.credentials.getControllerId() + ": " + proxyCouplingError.toString());
        }
        lastProblem = Optional.of(proxyCouplingError.problem());
        if (!Boolean.FALSE.equals(coupled)) {
            EventBus.getInstance().post(new com.sos.joc.event.bean.proxy.ProxyCoupled(this.credentials.getControllerId(), false));
        }
        coupled = false;
        if (lastProblem.isPresent()) {
            String msg = lastProblem.get().messageWithCause();
            if (msg != null) {
                if (msg.matches(".*javax\\.net\\.ssl\\.SSL[a-zA-Z]*Exception.*") || msg.matches(
                        ".*java\\.security\\.cert\\.Certificate[a-zA-Z]*Exception.*")) {
                    if (coupledFuture.isDone()) {
                        coupledFuture = new CompletableFuture<>();
                    }
                    coupledFuture.completeExceptionally(new ControllerSSLCertificateException(toString() + ": " + msg));
                } else if (msg.contains("HTTP 401")) {
                    if (coupledFuture.isDone()) {
                        coupledFuture = new CompletableFuture<>();
                    }
                    coupledFuture.completeExceptionally(new ControllerAuthorizationException(toString() + ": " + msg));
                }
            }
        }
    }

    private String getLastErrorMessage(String defaultMessage) {
        if (lastProblem.isPresent()) {
            return lastProblem.get().message(); //messageWithCause();
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
