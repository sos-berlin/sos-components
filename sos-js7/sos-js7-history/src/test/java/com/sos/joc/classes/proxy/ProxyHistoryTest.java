package com.sos.joc.classes.proxy;

import java.time.Instant;
import java.util.OptionalLong;
import java.util.concurrent.ExecutionException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.sos.joc.Globals;
import com.sos.joc.exceptions.DBMissingDataException;
import com.sos.joc.exceptions.JobSchedulerConnectionRefusedException;
import com.sos.joc.exceptions.JobSchedulerConnectionResetException;

import js7.data.event.Event;
import js7.data.event.EventId;
import js7.data.event.KeyedEvent;
import js7.data.event.Stamped;
import js7.data.order.OrderEvent;
import js7.data.order.OrderEvent.OrderAdded;
import js7.data.order.OrderEvent.OrderMoved;
import js7.proxy.data.ProxyEvent;
import js7.proxy.javaapi.data.controller.JEventAndControllerState;
import js7.proxy.javaapi.eventbus.JStandardEventBus;
import reactor.core.publisher.Flux;


public class ProxyHistoryTest {
    
    private static ProxyCredentials credential = null;
    private static final int connectionTimeOut = Globals.httpConnectionTimeout;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        Proxies.closeAll();
        Globals.httpConnectionTimeout = Math.max(20000, Globals.httpConnectionTimeout);
        credential = ProxyCredentialsBuilder.withJobSchedulerIdAndUrl("testsuite", "http://centosdev_secondary:5444")
                .withBackupUrl("http://centosdev_secondary:5544").withAccount(ProxyUser.HISTORY).build();
        Proxies.getInstance().startAll(credential);
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
        Globals.httpConnectionTimeout = connectionTimeOut;
        Proxies.closeAll();
    }

//    @Before
//    public void setUp() throws Exception {
//    }
//
//    @After
//    public void tearDown() throws Exception {
//    }

    @Test
    public void test1() throws JobSchedulerConnectionResetException, JobSchedulerConnectionRefusedException, DBMissingDataException,
            ExecutionException {
        JStandardEventBus<ProxyEvent> proxyEventBus = new JStandardEventBus<>(ProxyEvent.class);
        Flux<JEventAndControllerState<Event>> eventsWithStates = Proxy.of(credential).api().eventFlux(proxyEventBus, OptionalLong.of(EventId.BeforeFirst()));
        // see https://github.com/sos-berlin/js7/blob/main/js7-tests/src/test/java/js7/tests/controller/proxy/history/JControllerApiHistoryTester.java
        // see https://github.com/sos-berlin/js7/blob/main/js7-tests/src/test/scala/js7/tests/history/InMemoryHistory.scala
        eventsWithStates.filter(e -> e.stampedEvent().value().event() instanceof OrderEvent).subscribe(this::handleFatEvent);
    }
    
    public void handleFatEvent(JEventAndControllerState<Event> eventAndState) {
        //JControllerState state = eventAndState.state();
        Stamped<KeyedEvent<Event>> stampedEvent = eventAndState.stampedEvent();
        Instant timestamp = stampedEvent.timestamp().toInstant();
        System.out.println(timestamp.toString() + " " + stampedEvent.value().toString());
    }

}
