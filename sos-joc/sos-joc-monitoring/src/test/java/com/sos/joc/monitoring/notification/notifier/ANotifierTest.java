package com.sos.joc.monitoring.notification.notifier;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;

public class ANotifierTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(ANotifierTest.class);

    @Ignore
    @Test
    public void testParseJSON() throws Exception {
        String vars = "{\"file\":\"/tmp/file.txt\",\"counter\":123}";

        JsonNode root = ANotifier.JSON_OM.readTree(vars);
        if (root != null) {
            Iterator<Entry<String, JsonNode>> nodes = root.fields();
            while (nodes.hasNext()) {
                Map.Entry<String, JsonNode> entry = (Map.Entry<String, JsonNode>) nodes.next();
                LOGGER.info(entry.getKey().toUpperCase() + "=" + entry.getValue().asText());
            }
        }
    }

}
