package com.sos.joc.monitoring.impl;

import java.nio.file.Paths;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;

public class AgentsImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AgentsImplTest.class);

    // @Ignore
    @Test
    public void testPost() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new AgentsImpl());
        try {
            h.init();

            h.post("post", Paths.get("src/test/resources/ws/monitoring/impl/request-AgentsImpl-post.json"));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
