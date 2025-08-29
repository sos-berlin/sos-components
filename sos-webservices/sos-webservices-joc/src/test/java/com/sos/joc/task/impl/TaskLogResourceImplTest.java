package com.sos.joc.task.impl;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.model.job.RunningTaskLog;
import com.sos.joc.model.job.TaskFilter;

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
            filter.setTaskId(166197041L);
            filter.setEventId(1756305290154L);

            h.post("postRollingTaskLog", filter);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testRunningTaskUnsubscribe() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new TaskLogResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();

            // example with accessToken
            String accessToken = h.mockJOCLoginAsRoot();
            Long historyId = 166197043L;

            RunningTaskLog f = new RunningTaskLog();
            f.setControllerId("js7.x");
            f.setTaskId(historyId);
            f.setEventId(1756307864091L);

            h.post("postRollingTaskLog", f, accessToken).thenCompose(resp1 -> {
                try {
                    TaskFilter tf = new TaskFilter();
                    tf.setControllerId("js7.x");
                    tf.setTaskId(historyId);
                    return h.post("unsubscribeTaskLog", tf, accessToken);
                } catch (Exception e) {
                    LOGGER.error("[unsubscribeTaskLog]" + e.toString(), e);
                    return null;
                }
            });
            TimeUnit.SECONDS.sleep(5);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }
}
