package com.sos.joc.event;

import java.io.IOException;
import java.time.Instant;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.JOCEvent;
import com.sos.joc.event.bean.history.HistoryEvent;
import com.sos.joc.event.bean.history.HistoryOrderStarted;
import com.sos.joc.event.bean.history.HistoryOrderTaskTerminated;
import com.sos.joc.event.bean.history.HistoryOrderTerminated;

public class BusTest {

    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public class Listener1 {

        public Listener1() {
            EventBus.getInstance().register(this);
        }

        @Subscribe({ HistoryOrderTerminated.class, HistoryOrderTaskTerminated.class })
        public void onEvent(JOCEvent evt) throws JsonProcessingException {
            String json = objectMapper.writeValueAsString(evt);
            json = String.format("%s %s%n%s%n", Instant.now().toString(), this.getClass().getSimpleName(), json);
            System.out.print(json);
        }
    }

    public class Listener2 {

        public Listener2() {
            EventBus.getInstance().register(this);
        }

        @Subscribe
        public void doSomethingWithEvent(HistoryOrderStarted evt) throws JsonProcessingException {
            String json = objectMapper.writeValueAsString(evt);
            json = String.format("%s %s%n%s%n", Instant.now().toString(), this.getClass().getSimpleName(), json);
            System.out.print(json);
        }
    }

    public class Listener3 {

        public Listener3() {
            EventBus.getInstance().register(this);
        }

        @Subscribe
        public void doSomethingWithEvent(HistoryOrderTerminated evt) throws JsonProcessingException {
            String json = objectMapper.writeValueAsString(evt);
            json = String.format("%s %s%n%s%n", Instant.now().toString(), this.getClass().getSimpleName(), json);
            System.out.print(json);
        }

        @Subscribe
        public void doSomethingMoreWithEvent(HistoryOrderStarted evt) throws JsonProcessingException {
            String json = objectMapper.writeValueAsString(evt);
            json = String.format("%s %s%n%s%n", Instant.now().toString(), this.getClass().getSimpleName(), json);
            System.out.print(json);
        }
    }

    public class Listener4 {

        public Listener4() {
            EventBus.getInstance().register(this);
        }

        @Subscribe
        public void doSomethingWithEvent(JOCEvent evt) throws JsonProcessingException {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            String json = objectMapper.writeValueAsString(evt);
            json = String.format("%s %s%n%s%n", Instant.now().toString(), this.getClass().getSimpleName(), json);
            System.out.print(json);
        }
    }

    public class Listener5 {

        public Listener5() {
            EventBus.getInstance().register(this);
        }

        @Subscribe
        public void doSomethingWithEvent(String evt) throws JsonProcessingException {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            String json = evt;
            json = String.format("%s %s%n%s%n", Instant.now().toString(), this.getClass().getSimpleName(), json);
            System.out.print(json);
        }
    }

    @SuppressWarnings("unused")
    @Test
    public void testBus() throws IOException {
        Listener1 l1 = new Listener1();
        Listener2 l2 = new Listener2();
        Listener3 l3 = new Listener3();
        Listener4 l4 = new Listener4();
        Listener5 l5 = new Listener5();
        Listener2 l6 = new Listener2();

        System.out.println(Instant.now());
        HistoryEvent evt = new HistoryOrderStarted("controllerId", "orderId", 1L, 0L);
        EventBus eventBus = EventBus.getInstance();
        eventBus.post(evt);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        eventBus.post(evt);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
    }
}
