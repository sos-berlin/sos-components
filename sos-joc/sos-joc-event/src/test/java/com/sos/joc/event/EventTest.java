package com.sos.joc.event;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.joc.event.bean.history.HistoryEvent;
import com.sos.joc.event.bean.history.OrderStepStarted;


public class EventTest {
    
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static String jsonTestTemplate = "{\"TYPE\":\"OrderStepStarted\",\"controllerId\":\"myScheduler\",\"key\":\"job\",\"key1\":\"var1\",\"key2\":\"var2\"}";
    private static final Map<String, String> testMap = Collections.unmodifiableMap(new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;

        {
            put("key1", "var1");
            put("key2", "var2");
        }
    });

    @Test
    public void testObjectToJSON() throws JsonProcessingException {
        OrderStepStarted evt = new OrderStepStarted("job", "myScheduler", testMap);
        System.out.print(objectMapper.writeValueAsString(evt));
        assertEquals("testObjectToJSON", jsonTestTemplate, objectMapper.writeValueAsString(evt));
    }
    
    @Test
    public void testJSONToObject() throws IOException {
        HistoryEvent evt = objectMapper.readValue(jsonTestTemplate, HistoryEvent.class);
        System.out.println(evt.toString());
        OrderStepStarted expectEvt = new OrderStepStarted("job", "myScheduler", testMap);
        expectEvt.setTYPE("OrderStepStarted");  //only for Test necessary
        assertEquals("testJSONToObject", expectEvt, evt);
    }
    
    @Test 
    public void t() throws IOException {
        ActiveClusterChangedEvent evt = new ActiveClusterChangedEvent("hallo", "welt");
        System.out.println(objectMapper.writeValueAsString(evt));
        String json = "{\"TYPE\":\"ActiveClusterChangedEvent\",\"newClusterMemberId\":\"hallo\",\"oldClusterMemberId\":\"welt\"}";
        ActiveClusterChangedEvent obj = objectMapper.readValue(json, ActiveClusterChangedEvent.class);
        System.out.println(obj.toString());
    }

}
