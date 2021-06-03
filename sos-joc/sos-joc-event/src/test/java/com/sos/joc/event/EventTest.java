package com.sos.joc.event;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sos.joc.event.bean.cluster.ActiveClusterChangedEvent;
import com.sos.joc.event.bean.history.HistoryEvent;
import com.sos.joc.event.bean.history.HistoryOrderStarted;

public class EventTest {

    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static String jsonTestTemplate =
            "{\"TYPE\":\"HistoryOrderStarted\",\"controllerId\":\"controllerId\",\"key\":\"HistoryOrderStarted\",\"orderId\":\"orderId\",\"workflowName\":\"myWorkflow\",\"WorkflowVersionId\":\"4711\"}";

    @Test
    public void testObjectToJSON() throws JsonProcessingException {
        HistoryOrderStarted evt = new HistoryOrderStarted("controllerId", "orderId", "myWorkflow", "4711",null);
        System.out.print(objectMapper.writeValueAsString(evt));
        assertEquals("testObjectToJSON", jsonTestTemplate, objectMapper.writeValueAsString(evt));
    }

    @Ignore
    @Test
    public void testJSONToObject() throws IOException {
        HistoryEvent evt = objectMapper.readValue(jsonTestTemplate, HistoryEvent.class);
        System.out.println(evt.toString());
        HistoryOrderStarted expectEvt = new HistoryOrderStarted("controllerId", "orderId", "myWorkflow", "4711",null);
        expectEvt.setTYPE(HistoryOrderStarted.class.getSimpleName());  // only for Test necessary
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
