package com.sos.joc.history.helper;

import static org.junit.Assert.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSPath;
import com.sos.controller.model.event.EventType;

public class LogEntryTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(LogEntryTest.class);

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
        LOGGER.info(new String(b));
    }

    @Ignore
    @Test
    public void testReadFirstLastBytes() throws Exception {
        Instant start = Instant.now();
        Path file = Paths.get("pom.xml");
        int firstBytes2read = 100 * 1_024;
        int lastBytes2read = 100 * 1_024;
        StringBuilder msgBetweenFirstLast = new StringBuilder("\n-----------BetweenFirstLast-----------\n");

        StringBuilder sb = SOSPath.readFirstLastBytes(file, firstBytes2read, lastBytes2read, msgBetweenFirstLast);
        LOGGER.info("duration=" + SOSDate.getDuration(start, Instant.now()));

        Files.write(file.getParent().resolve(file.getFileName() + "-firstlast.txt"), sb.toString().getBytes(), StandardOpenOption.CREATE,
                StandardOpenOption.TRUNCATE_EXISTING);
    }

}
