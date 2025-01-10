package com.sos.joc.inventory.impl;

import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;

public class SearchResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchResourceImplTest.class);

    @Ignore
    @Test
    public void testPostSearch() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new SearchResourceImpl());
        try {
            h.init();

            h.post("postSearch", Paths.get("src/test/resources/ws/inventory/impl/request-SearchResourceImpl-postSearch.json"));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
