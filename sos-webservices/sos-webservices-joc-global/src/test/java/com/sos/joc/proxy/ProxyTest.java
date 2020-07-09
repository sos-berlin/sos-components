package com.sos.joc.proxy;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.jobscheduler.model.command.Terminate;
import com.sos.joc.Globals;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.Proxy;
import com.sos.joc.classes.proxy.ProxyCredentials;
import com.sos.joc.classes.proxy.ProxyCredentialsBuilder;

import js7.controller.data.ControllerSnapshots.ControllerMetaState;
import js7.controller.data.ControllerState;
import js7.controller.data.agent.AgentSnapshot;
import js7.controller.data.events.ControllerEvent;
import js7.controller.data.events.ControllerEvent.ControllerReady;
import js7.data.agent.AgentRefPath;
import js7.data.cluster.ClusterEvent;
import js7.data.cluster.ClusterState;
import js7.data.event.Event;
import js7.data.event.KeyedEvent;
import js7.data.event.Stamped;
import js7.data.order.Order;
import js7.proxy.javaapi.JControllerProxy;
import js7.proxy.javaapi.data.JControllerState;

public class ProxyTest {

    /*
     * see Test in GitHub js7/js7-proxy/jvm/src/test/java/js7/proxy/javaapi/data/JMasterStateTester.java etc
     * js7/js7-tests/src/test/java/js7/tests/master/proxy/JMasterProxyTester.java etc
     * https://github.com/sos-berlin/js7/blob/main/js7-tests/src/test/java/js7/tests/controller/proxy/TestJControllerProxy.java
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyTest.class);
    private final CompletableFuture<Boolean> finished = new CompletableFuture<>();
    private ProxyCredentials credential = null;
    private static final int connectionTimeOut = Globals.httpConnectionTimeout;
    private static final Map<Class<? extends Order.State>, String> groupStatesMap = Collections.unmodifiableMap(
            new HashMap<Class<? extends Order.State>, String>() {

                /*
                 * +PENDING: Fresh 
                 * +WAITING: Forked, Offering, Awaiting, DelayedAfterError 
                 * -BLOCKED: Fresh late 
                 * +RUNNING: Ready, Processing, Processed
                 * ---FAILED: Failed, FailedWhileFresh, FailedInFork, Broken 
                 * --SUSPENDED any state+Suspended Annotation
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
                    put(Order.ProcessingCancelled$.class, "finished");
                }
            });
    
    @Before
    public void setUp() {
        Globals.httpConnectionTimeout = Math.max(20000, Globals.httpConnectionTimeout);
        credential = ProxyCredentialsBuilder.withUrl("http://centosdev_secondary:5444").build();
//        ProxyCredentials credential2 = ProxyCredentialsBuilder.withUrl("http://centostest_secondary:5344").build();
//        ProxyCredentials credential3 = ProxyCredentialsBuilder.withUrl("http://centostest_secondary:5544").build();
//        Proxies.getInstance().startAll(credential, credential2, credential3);
        Proxies.getInstance().startAll(credential);
    }

    @After
    public void tearDown() {
        Globals.httpConnectionTimeout = connectionTimeOut;
        Proxies.getInstance().closeAll();
    }
    
    @Test
    public void testBadUri() {
        String uri = "http://localhost:4711";
        LOGGER.info("try to connect with " + uri);
        boolean connectionRefused = false;
        try {
            Proxy.of(ProxyCredentialsBuilder.withUrl(uri).build());
        } catch (Exception e) {
            LOGGER.error("",e);
            connectionRefused = true;
        }
        Assert.assertTrue("Connection to " + uri + " refused", connectionRefused);
    }

    @Test
    public void testBadUri2() {
        String uri = "https://centosdev_secondary:5443";
        LOGGER.info("try to connect with " + uri);
        boolean connectionRefused = false;
        try {
            Proxy.of(ProxyCredentialsBuilder.withUrl(uri).build());
        } catch (Exception e) {
            LOGGER.error("",e);
            connectionRefused = true;
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

            // Variante 1 (quicker, why??)
            Map<String, Long> map1 = controllerState.orderIds().stream().map(o -> controllerState.idToOrder(o).get()).collect(Collectors.groupingBy(
                    jOrder -> groupStatesMap.get(jOrder.underlying().state().getClass()), Collectors.counting()));
            LOGGER.info(map1.toString());

            // Variante 2 (preferred if you need predicates)
            Map<String, Long> map2 = controllerState.ordersBy(o -> true).collect(Collectors.groupingBy(jOrder -> groupStatesMap.get(jOrder.underlying()
                    .state().getClass()), Collectors.counting()));
            LOGGER.info(map2.toString());
            Assert.assertEquals("", map1.size(), map2.size());
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

            controllerProxy.eventBus().<Event>subscribe(Arrays.asList(ControllerEvent.class, ClusterEvent.class), (stampedEvent, state) -> LOGGER.info(
                    orderEventToString(stampedEvent)));

            final String restartJson = Globals.objectMapper.writeValueAsString(new Terminate(true, null));
            LOGGER.info(restartJson);
            try {
                Thread.sleep(5 * 1000);
                controllerProxy.executeCommandJson(restartJson).get();
                controllerReady = finished.get(40, TimeUnit.SECONDS);
            } catch (Exception e) {
                LOGGER.error("", e);
            }
            LOGGER.info(Instant.now().toString());
            Assert.assertTrue("Proxy is alive after restart", controllerReady);
        } catch (Exception e) {
            Assert.fail(e.toString());
        }
    }
    
    @Test
    public void testControllerState() {
        try {
            JControllerProxy controllerProxy = Proxy.of(credential);
            LOGGER.info(Instant.now().toString());
            JControllerState controllerState = controllerProxy.currentState();
            ControllerState state = controllerState.underlying();
            ClusterState clusterState = state.clusterState();
            System.out.println(clusterState);
            
            ControllerMetaState metaState = state.controllerMetaState();
            System.out.println(metaState);
            System.out.println(metaState.startedAt().toInstant());
            System.out.println(metaState.timezone());
            
            Assert.assertTrue("", true);
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
