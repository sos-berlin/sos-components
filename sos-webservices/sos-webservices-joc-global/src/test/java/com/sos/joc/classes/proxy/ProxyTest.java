package com.sos.joc.classes.proxy;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.model.command.Terminate;
import com.sos.joc.Globals;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerSSLCertificateException;

import io.vavr.control.Either;
import js7.base.generic.SecretString;
import js7.base.problem.Problem;
import js7.common.akkahttp.https.KeyStoreRef;
import js7.common.akkahttp.https.TrustStoreRef;
import js7.controller.data.events.ControllerEvent;
import js7.controller.data.events.ControllerEvent.ControllerReady;
import js7.data.cluster.ClusterEvent;
import js7.data.event.Event;
import js7.data.event.KeyedEvent;
import js7.data.event.Stamped;
import js7.data.order.Order;
import js7.data.order.OrderId;
import js7.proxy.javaapi.JControllerApi;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.data.cluster.JClusterState;
import js7.proxy.javaapi.data.controller.JControllerState;
import js7.proxy.javaapi.data.order.JOrder;
import js7.proxy.javaapi.data.order.JOrderPredicates;
import js7.proxy.javaapi.eventbus.JControllerEventBus;

public class ProxyTest {

    /*
     * see Test in GitHub https://github.com/sos-berlin/js7/blob/main/js7-tests/src/test/java/js7/tests/controller/proxy/JControllerProxyTester.java etc
     * https://github.com/sos-berlin/js7/blob/main/js7-tests/src/test/java/js7/tests/controller/proxy/TestJControllerProxy.java
     * https://github.com/sos-berlin/js7/blob/main/js7-proxy/jvm/src/test/java/js7/proxy/javaapi/data/JControllerStateTester.java
     * https://github.com/sos-berlin/js7/blob/main/js7-proxy/jvm/src/test/java/js7/proxy/javaapi/data/JClusterStateTester.java
     * https://github.com/sos-berlin/js7/blob/main/js7-proxy/jvm/src/main/scala/js7/proxy/javaapi/data/JClusterState.scala
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyTest.class);
    private final CompletableFuture<Boolean> finished = new CompletableFuture<>();
    private static ProxyCredentials credential = null;
    private static final int connectionTimeOut = Globals.httpConnectionTimeout;
    private static final Map<Class<? extends Order.State>, String> groupStatesMap = Collections.unmodifiableMap(
            new HashMap<Class<? extends Order.State>, String>() {

                /*
                 * +PENDING: Fresh +WAITING: Forked, Offering, Awaiting, DelayedAfterError -BLOCKED: Fresh late +RUNNING: Ready, Processing, Processed
                 * ---FAILED: Failed, FailedWhileFresh, FailedInFork, Broken --SUSPENDED any state+Suspended Annotation
                 */
                private static final long serialVersionUID = 1L;

                {
                    put(Order.Fresh.class, "pending");
                    put(Order.Awaiting.class, "waiting");
                    put(Order.DelayedAfterError.class, "waiting");
                    put(Order.Forked.class, "waiting");
                    put(Order.Offering.class, "waiting");
                    put(Order.Broken.class, "failed");
                    put(Order.Failed.class, "failed");
                    put(Order.FailedInFork.class, "failed");
                    put(Order.FailedWhileFresh$.class, "failed");
                    put(Order.Ready$.class, "running");
                    put(Order.Processed$.class, "running");
                    put(Order.Processing$.class, "running");
                    put(Order.Finished$.class, "finished");
                    put(Order.Cancelled$.class, "finished");
                    put(Order.ProcessingKilled$.class, "finished");
                }
            });

    @BeforeClass
    public static void setUp() {
        Proxies.closeAll();
        Globals.httpConnectionTimeout = Math.max(20000, Globals.httpConnectionTimeout);
        credential = ProxyCredentialsBuilder.withJobSchedulerIdAndUrl("testsuite", "http://centosdev_secondary:5444")
                .withBackupUrl("http://centosdev_secondary:5544").withAccount(ProxyUser.JOC).build();
        // ProxyCredentials credential2 = ProxyCredentialsBuilder.withUrl("http://centostest_secondary:5344").build();
        // ProxyCredentials credential3 = ProxyCredentialsBuilder.withUrl("http://centostest_secondary:5544").build();
        // Proxies.getInstance().startAll(credential, credential2, credential3);
        //Proxies.getInstance().startAll(credential);
    }

    @AfterClass
    public static void tearDown() {
        Globals.httpConnectionTimeout = connectionTimeOut;
        Proxies.closeAll();
    }

    @Test
    public void testBadUri() {
        String uri = "http://localhost:4711";
        LOGGER.info("try to connect with " + uri);
        boolean connectionRefused = false;
        try {
            Proxy.of(ProxyCredentialsBuilder.withJobSchedulerIdAndUrl("test", uri).build());
        } catch (Exception e) {
            LOGGER.error(e.toString());
            connectionRefused = true;
        }
        Assert.assertTrue("Connection to " + uri + " refused", connectionRefused);
    }

    @Test
    public void testHttpsWithoutTruststore() {
        String uri = "https://centosdev_secondary:5343";
        LOGGER.info("try to connect with " + uri);
        boolean connectionRefused = false;
        try {
            Proxy.of(ProxyCredentialsBuilder.withJobSchedulerIdAndUrl("standalone", uri).withAccount(ProxyUser.HISTORY).build());
        } catch (Exception e) {
            LOGGER.error(e.toString());
            connectionRefused = true;
        }
        Assert.assertTrue("Connection to " + uri + " refused", connectionRefused);
    }

    @Test
    public void testBadHostname() {
        Path keyStoreFile = Paths.get("src/test/resources/https-keystore.p12");
        KeyStoreRef keyStoreRef = KeyStoreRef.apply(keyStoreFile, SecretString.apply("jobscheduler"), SecretString.apply("jobscheduler"));
        Path trustStoreFile = Paths.get("src/test/resources/https-truststore.p12");
        TrustStoreRef trustStoreRef = TrustStoreRef.apply(trustStoreFile, SecretString.apply("jobscheduler"));
        String uri = "https://centosdev_secondary:5343";
        LOGGER.info("try to connect with " + uri);
        boolean handshake = true;
        try {
            Proxy.of(ProxyCredentialsBuilder.withJobSchedulerIdAndUrl("standalone", uri).withAccount(ProxyUser.JOC).withHttpsConfig(keyStoreRef,
                    trustStoreRef).build());
        } catch (JobSchedulerSSLCertificateException e) {
            LOGGER.error(e.toString());
            handshake = false;
        } catch (Exception e) {
            LOGGER.error(e.toString());
        }
//        try {
//            TimeUnit.SECONDS.sleep(10);
//        } catch (InterruptedException e) {
//        }
        Assert.assertFalse("Connection to " + uri + " has handshake exception", handshake);
    }
    
    @Test
    public void testConnection() {
        String uri = "http://localhost:4711";
        LOGGER.info("try to connect with " + uri);
        boolean connectionRefused = false;
        CompletableFuture<Either<Problem, Void>> future = null;
        try {
            JControllerApi controllerApi = Proxies.getInstance().loadApi(ProxyCredentialsBuilder.withJobSchedulerIdAndUrl("test", uri).build());
            OrderId o = OrderId.of("test");
            future = controllerApi.cancelOrders(Arrays.asList(o));
            Either<Problem, Void> either = future.get(20, TimeUnit.SECONDS);
            if (either.isLeft()) {
                LOGGER.error(either.getLeft().messageWithCause());
                connectionRefused = true;
                future.cancel(true);
            }
        } catch (Exception e) {
            LOGGER.error(e.toString());
            connectionRefused = true;
            if (future != null) {
                future.cancel(true);
            }
        }
        Assert.assertTrue("Connection to " + uri + " refused", connectionRefused);
    }

    @Test
    public void testAggregatedOrders() {
        try {
            JControllerProxy controllerProxy = Proxy.of(credential);
            LOGGER.info(Instant.now().toString());
            
            JControllerState controllerState = controllerProxy.currentState();
            LOGGER.info(Instant.now().toString());
            LOGGER.info(controllerState.eventId() + "");

            // Variante 1
            Map<String, Long> map1 = controllerState.orderIds().stream().map(o -> controllerState.idToOrder(o).get()).collect(Collectors.groupingBy(
                    jOrder -> groupStatesMap.get(jOrder.asScala().state().getClass()), Collectors.counting()));
            LOGGER.info(map1.toString());

            // Variante 2 (preferred if you need predicates, e.g 'o -> true')
            Map<String, Long> map2 = controllerState.ordersBy(o -> true).collect(Collectors.groupingBy(jOrder -> groupStatesMap.get(jOrder
                    .asScala().state().getClass()), Collectors.counting()));
            LOGGER.info(map2.toString());

            // Variante 3 (new method)
            Map<String, Integer> map3 = controllerState.orderStateToCount().entrySet().stream().collect(Collectors.groupingBy(entry -> groupStatesMap
                    .get(entry.getKey()), Collectors.summingInt(entry -> entry.getValue())));
            LOGGER.info(map3.toString());
            
            //orderStates = controllerState.orderStateToCount(o -> workflowPaths.contains(o.workflowId().path().string()));
            
            Optional<JOrder> order = controllerState.ordersBy(JOrderPredicates.any()).findAny();
            if (order.isPresent()) {
                LOGGER.info(order.get().toJson());
            }
            final Instant now = Instant.ofEpochMilli(controllerState.eventId() / 1000);
            Integer i = controllerState.ordersBy(JOrderPredicates.byOrderState(Order.Fresh.class))
                .map(o -> {
                    System.out.println(o.asScala().state().maybeDelayedUntil());
                    return o.asScala().state().maybeDelayedUntil();
                })
                .filter(o -> !o.isEmpty())
                .filter(t -> t.get().toInstant().isBefore(now))
                .mapToInt(e -> 1).sum();
            LOGGER.info("+++++++++++++++++++++++" + i + "++++++++++++++++++++++++++++");

            Assert.assertEquals("", map2.size(), map3.size());
        } catch (JobSchedulerConnectionRefusedException e) {
            LOGGER.warn(e.toString());
            Assert.assertTrue("Controller is unfortunately not available at the time of testing", true);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testControllerEvents() {
        try {
            JControllerProxy controllerProxy = Proxy.of(credential);
            LOGGER.info(Instant.now().toString());
            boolean controllerReady = false;
            
            BiConsumer<Stamped<KeyedEvent<Event>>, JControllerState> callbackOfCurrentController = (stampedEvt, state) -> LOGGER.info(
                    "###########1: " + orderEventToString(stampedEvt));
            
            BiConsumer<Stamped<KeyedEvent<Event>>, JControllerState> callbackOfCurrentController2 = (stampedEvt, state) -> LOGGER.info(
                    "+++++++++++2: " + orderEventToString(stampedEvt));

            JControllerEventBus evtBus = controllerProxy.controllerEventBus();
            evtBus.subscribe(Arrays.asList(ControllerEvent.class, ClusterEvent.class), callbackOfCurrentController);
            
            controllerProxy.controllerEventBus().subscribe(Arrays.asList(ControllerEvent.class, ClusterEvent.class), callbackOfCurrentController2);;

            final String restartJson = Globals.objectMapper.writeValueAsString(new Terminate(true, null));
            LOGGER.info(restartJson);
            try {
                //evtBus.close();
                TimeUnit.SECONDS.sleep(5);
                //evtBus.subscribe(Arrays.asList(ControllerEvent.class, ClusterEvent.class), callbackOfCurrentController);
                controllerProxy.api().executeCommandJson(restartJson).get();
                controllerReady = finished.get(40, TimeUnit.SECONDS);
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            LOGGER.info(Instant.now().toString());
            Assert.assertTrue("Proxy is alive after restart", controllerReady);
        } catch (JobSchedulerConnectionRefusedException e) {
            LOGGER.warn(e.toString());
            Assert.assertTrue("Controller is unfortunately not available at the time of testing", true);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    @Test
    public void testControllerState() {
        try {
            JControllerProxy controllerProxy = Proxy.of(credential);
            LOGGER.info(Instant.now().toString());
            JControllerState state = controllerProxy.currentState();
            //LOGGER.info("++++++"+state.idToWorkflow(JWorkflowId.of("/workflow2", "05294efb-9f3e-45ac-881f-0e2e027ed712")).get().toJson());
            //LOGGER.info("++++++"+state.underlying().repo().currentItems().size());
            //LOGGER.info("++++++"+state.pathToWorkflow(WorkflowPath.of("/workflow2")).get().toJson());
            JClusterState clusterState = state.clusterState();
            LOGGER.info(clusterState.toJson());

//            ControllerMetaState metaState = state.controllerMetaState();
//            System.out.println(metaState);
//            System.out.println(metaState.startedAt().toInstant());
//            System.out.println(metaState.timezone());

            Assert.assertTrue("", true);
        } catch (JobSchedulerConnectionRefusedException e) {
            LOGGER.warn(e.toString());
            Assert.assertTrue("Controller is unfortunately not available at the time of testing", true);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }

    private String orderEventToString(Stamped<KeyedEvent<Event>> stamped) {
        Instant timestamp = stamped.timestamp().toInstant();
        KeyedEvent<Event> event = stamped.value();
        Event evt = event.event();
        if (evt instanceof ControllerReady) {
            finished.complete(true);
        }
        return timestamp.toString() + " " + event.toString();
    }

}
