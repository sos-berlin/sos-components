package com.sos.joc.workflows.impl;

import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;

public class WorkflowsSearchImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(WorkflowsSearchImplTest.class);

    @Ignore
    @Test
    public void testPostSearch() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new WorkflowsSearchImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.oracle-12c.xml");
        try {
            h.init();

            h.post("postSearch", Paths.get("src/test/resources/ws/workflows/impl/request-WorkflowsSearchImpl-postSearch.json"));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }
}
