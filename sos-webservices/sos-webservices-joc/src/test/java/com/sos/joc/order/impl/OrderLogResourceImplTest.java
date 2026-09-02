package com.sos.joc.order.impl;

import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.model.job.TaskFilter;
import com.sos.joc.model.order.OrderHistoryFilter;
import com.sos.joc.model.order.RunningOrderLogEvents;

public class OrderLogResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrderLogResourceImplTest.class);

    @Ignore
    @Test
    public void testPostRollingOrderLog() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new OrderLogResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();

            RunningOrderLogEvents filter = new RunningOrderLogEvents();
            filter.setControllerId("js7.x");
            filter.setHistoryId(166197041L);
            filter.setEventId(1756305290154L);
            filter.setLogEvents(null);
            filter.setComplete(null);

            h.post("postRollingOrderLog", filter);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testUnsubscribeOrderLog() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new OrderLogResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();

            // example with accessToken
            String accessToken = h.mockJOCLoginAsRoot();
            Long historyId = 166197043L;
            String orderId = null;

            OrderHistoryFilter f = new OrderHistoryFilter();
            f.setControllerId("js7.x");
            f.setHistoryId(historyId);
            f.setOrderId(orderId);

            h.post("unsubscribeOrderLog", f, accessToken).thenCompose(resp1 -> {
                try {
                    TaskFilter tf = new TaskFilter();
                    tf.setControllerId("js7.x");
                    tf.setTaskId(historyId);
                    return h.post("unsubscribeOrderLog", tf, accessToken);
                } catch (Exception e) {
                    LOGGER.error("[unsubscribeOrderLog]" + e.toString(), e);
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
