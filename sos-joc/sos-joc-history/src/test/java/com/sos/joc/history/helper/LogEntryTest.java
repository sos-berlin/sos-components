package com.sos.joc.history.helper;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;

import com.sos.controller.model.event.EventType;

public class LogEntryTest {

    @Ignore
    public void testEventType() throws Exception {
        EventType t = EventType.fromValue("OrderAdded");
        assertTrue(t.equals(EventType.OrderAdded));
    }

    @Ignore
    @Test
    public void testReadFile() throws Exception {
        Path file = Paths.get("pom.xml");
        byte[] b = new StringBuilder("[").append(new String(Files.readAllBytes(file))).append("]").toString().getBytes();
        System.out.println(new String(b));
    }

    @Ignore
    @Test
    public void testReadFirstLastBytes() throws Exception {
        Path file = Paths.get("pom.xml");
        int firstBytesSize = 10;
        int lastBytesSize = 10;
        String msgBetweenFirstLast = "\n-----------BetweenFirstLast-----------\n";

        StringBuilder sb = HistoryUtil.readFirstLastBytes(file, firstBytesSize, lastBytesSize, msgBetweenFirstLast);
        System.out.println(sb);
    }

}
