package com.sos.jobscheduler.history.helper;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.jobscheduler.model.event.EventType;

public class LogEntryTest {

    @Ignore
    public void testEventType() throws Exception {
        EventType t = EventType.fromValue("OrderAdded");
        assertTrue(t.equals(EventType.ORDER_ADDED));
    }

    @Ignore
    @Test
    public void testReadFile() throws Exception {
        Path file = Paths.get("pom.xml");
        byte[] b = new StringBuilder("[").append(new String(Files.readAllBytes(file))).append("]").toString().getBytes();
        System.out.println(new String(b));
    }
}
