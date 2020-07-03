package com.sos.joc.proxy;

import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sos.jobscheduler.model.command.Terminate;
import com.sos.joc.Globals;
import com.sos.joc.classes.proxy.Proxies;
import com.sos.joc.classes.proxy.ProxyCredentialsBuilder;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;

import js7.data.cluster.ClusterEvent;
import js7.data.event.Event;
import js7.data.event.KeyedEvent;
import js7.data.event.Stamped;
import js7.master.data.events.MasterEvent;
import js7.master.data.events.MasterEvent.MasterReady;
import js7.proxy.javaapi.JMasterProxy;
import js7.proxy.javaapi.data.JMasterState;
import js7.proxy.javaapi.data.JOrder;

public class ProxyTest {
    
    /*
     * see Test in GitHub
     * js7/js7-proxy/jvm/src/test/java/js7/proxy/javaapi/data/JMasterStateTester.java etc
     * js7/js7-tests/src/test/java/js7/tests/master/proxy/JMasterProxyTester.java etc
     */
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ProxyTest.class);
    private final CompletableFuture<Boolean> finished = new CompletableFuture<>();
    
    @Test
    public void testBadUri() throws ExecutionException, InterruptedException {
        String uri = "http://localhost:5499";
        LOGGER.info("try to connect with " + uri);
        boolean connectionRefused = false;
        try {
            JMasterProxy masterProxy = Proxies.connect(ProxyCredentialsBuilder.withUrl(uri));
            LOGGER.info(masterProxy.currentState().eventId() + "");
            masterProxy.close();
        } catch (Exception e) {
            connectionRefused = true;
        }
        Assert.assertTrue("Connection to " + uri + " refused", connectionRefused);
    }
    
    @Test
    public void testAggregatedOrders() throws JobSchedulerConnectionRefusedException, JobSchedulerConnectionResetException {
        String uri = "http://centostest_secondary:5444";
        JMasterProxy masterProxy = Proxies.connect(ProxyCredentialsBuilder.withUrl(uri));
        LOGGER.info(Instant.now().toString());
        
        JMasterState masterState = masterProxy.currentState();
        LOGGER.info(Instant.now().toString());
        LOGGER.info(masterState.eventId() + "");
        
        // Variante 1 (quicker)
        Map<Class<? extends JOrder>, Long> map1 = masterState.orderIds().stream().map(o -> masterState.idToOrder(o).get()).collect(Collectors.groupingBy(
                JOrder::getClass, Collectors.counting()));
        LOGGER.info(map1.toString());

        // Variante 2 (preferred if you need predicates)
        Map<Class<? extends JOrder>, Long> map2 = masterState.ordersBy(o -> true).collect(Collectors.groupingBy(JOrder::getClass, Collectors
                .counting()));
        LOGGER.info(map2.toString());
        Proxies.close(masterProxy);
        Assert.assertEquals("", map1.size(), map2.size());
    }
    
    @Test
    public void testControllerEvents() throws JsonProcessingException, JobSchedulerConnectionRefusedException, JobSchedulerConnectionResetException {
        String uri = "http://centosdev_secondary:5444";  //5544 for backup
        JMasterProxy masterProxy = Proxies.connect(ProxyCredentialsBuilder.withUrl(uri));
        LOGGER.info(Instant.now().toString());
        boolean masterReady = false;
        
        masterProxy.eventBus().subscribe(Arrays.asList(MasterEvent.class, ClusterEvent.class), 
                (stampedEvent, state) -> LOGGER.info(orderEventToString(stampedEvent))
        );
        
        final String restartJson = Globals.objectMapper.writeValueAsString(new Terminate(true, null));
        LOGGER.info(restartJson);
        try {
            Thread.sleep(5*1000);
            masterProxy.executeCommandJson(restartJson).get();
            masterReady = finished.get(40, TimeUnit.SECONDS);
        } catch (Exception e) {
            System.err.println(e.toString());
        }
        LOGGER.info(Instant.now().toString());
        Proxies.close(masterProxy);
        Assert.assertTrue("Proxy is alive after restart", masterReady);
    }
    
    @Test
    public void testProxies() throws JobSchedulerConnectionRefusedException, JobSchedulerConnectionResetException {
        String uri = "http://centosdev_secondary:5444";
        String uriBackup = "http://centosdev_secondary:5544";
        JMasterProxy masterProxy = Proxies.connect(ProxyCredentialsBuilder.withUrl(uri).build());
        JMasterProxy masterProxyBackup = Proxies.connect(ProxyCredentialsBuilder.withUrl(uriBackup).build());
        LOGGER.info(Instant.now().toString());
        LOGGER.info("Num of Proxies: " + Proxies.getInstance().getProxies().size());
        Proxies.getInstance().getProxies().entrySet().stream().forEach(entry -> {
            LOGGER.info(entry.getKey().toString());
            LOGGER.info(entry.getValue().toString());
        });
        Proxies.close(masterProxy);
        LOGGER.info("Num of Proxies: " + Proxies.getInstance().getProxies().size());
        Proxies.getInstance().getProxies().entrySet().stream().forEach(entry -> {
            LOGGER.info(entry.getKey().toString());
            LOGGER.info(entry.getValue().toString());
        });
        Proxies.close(masterProxyBackup);
        Assert.assertEquals("Num of Proxies", 0, Proxies.getInstance().getProxies().size());
    }
    
    private String orderEventToString(Stamped<KeyedEvent<Event>> stamped) {
        Instant timestamp = stamped.timestamp().toInstant();
        KeyedEvent<Event> event = stamped.value();
        Event evt = event.event();
        if (evt instanceof MasterReady) {
            finished.complete(true);
        }
        return timestamp.toString() + " " + event.toString();
    }

}
