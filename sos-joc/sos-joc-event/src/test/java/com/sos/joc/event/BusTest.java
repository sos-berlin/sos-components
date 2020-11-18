package com.sos.joc.event;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.joc.event.EventBus;
import com.sos.joc.event.annotation.Subscribe;
import com.sos.joc.event.bean.JOCEvent;
import com.sos.joc.event.bean.history.HistoryEvent;
import com.sos.joc.event.bean.history.OrderStepFinished;
import com.sos.joc.event.bean.history.OrderStepStarted;

public class BusTest {

    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static final Map<String, String> testMap = Collections.unmodifiableMap(new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;

        {
            put("key1", "var1");
            put("key2", "var2");
        }
    });


    public class Listener1 {

        public Listener1() {
            EventBus.getInstance().register(this);
        }

        @Subscribe({ OrderStepFinished.class, OrderStepStarted.class })
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
        public void doSomethingWithEvent(OrderStepStarted evt) throws JsonProcessingException {
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
        public void doSomethingWithEvent(OrderStepFinished evt) throws JsonProcessingException {
            String json = objectMapper.writeValueAsString(evt);
            json = String.format("%s %s%n%s%n", Instant.now().toString(), this.getClass().getSimpleName(), json);
            System.out.print(json);
        }
        
        @Subscribe
        public void doSomethingMoreWithEvent(OrderStepStarted evt) throws JsonProcessingException {
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
        HistoryEvent evt = new OrderStepStarted("myJob", "myScheduler", testMap);
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
