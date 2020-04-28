package com.sos.jobscheduler.history.helper;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.sos.jobscheduler.model.event.EventType;

public class LogEntryTest {

    @Test
    public void testEventType() throws Exception {
        EventType t = EventType.fromValue("OrderAdded");
        assertTrue(t.equals(EventType.ORDER_ADDED));
    }

}
