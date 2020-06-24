package com.sos.joc.event;

import static org.junit.Assert.*;

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.joc.event.bean.history.HistoryEvent;
import com.sos.joc.event.bean.history.OrderStepStarted;


public class EventTest {
    
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static String jsonTestTemplate = "{\"TYPE\":\"OrderStepStarted\",\"eventId\":[evtId],\"jobschedulerId\":\"myScheduler\",\"key\":\"job\",\"key1\":\"var1\",\"key2\":\"var2\"}";
    private static final Map<String, String> testMap = Collections.unmodifiableMap(new HashMap<String, String>() {

        private static final long serialVersionUID = 1L;

        {
            put("key1", "var1");
            put("key2", "var2");
        }
    });

    @Test
    public void testObjectToJSON() throws JsonProcessingException {
        long now = Instant.now().getEpochSecond();
        OrderStepStarted evt = new OrderStepStarted("job", now, "myScheduler", testMap);
        System.out.print(objectMapper.writeValueAsString(evt));
        assertEquals("testObjectToJSON", jsonTestTemplate.replaceFirst("\\[evtId\\]", now + ""), objectMapper.writeValueAsString(evt));
    }
    
    @Test
    public void testJSONToObject() throws IOException {
        long now = Instant.now().getEpochSecond();
        HistoryEvent evt = objectMapper.readValue(jsonTestTemplate.replaceFirst("\\[evtId\\]", now + ""), HistoryEvent.class);
        OrderStepStarted expectEvt = new OrderStepStarted("job", now, "myScheduler", testMap);
        expectEvt.setTYPE("OrderStepStarted");  //only for Test necessary
        assertEquals("testJSONToObject", expectEvt, evt);
    }

}
