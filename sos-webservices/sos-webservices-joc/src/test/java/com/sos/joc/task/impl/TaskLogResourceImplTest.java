package com.sos.joc.task.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.model.job.RunningTaskLog;

public class TaskLogResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskLogResourceImplTest.class);

    @Ignore
    @Test
    public void testPostRollingTaskLog() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new TaskLogResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();

            RunningTaskLog filter = new RunningTaskLog();
            filter.setControllerId("js7.x");
            filter.setTaskId(98L);;
            filter.setEventId(1752744724241L);

            h.post("postRollingTaskLog", filter);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
