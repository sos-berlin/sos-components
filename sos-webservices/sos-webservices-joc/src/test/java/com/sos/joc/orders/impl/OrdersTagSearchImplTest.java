package com.sos.joc.orders.impl;

import java.nio.file.Paths;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;

public class OrdersTagSearchImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(OrdersTagSearchImplTest.class);

    @Ignore
    @Test
    public void testPostTagSearch() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new OrdersTagSearchImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");
        try {
            h.init();

            h.post("postTagSearch", Paths.get("src/test/resources/ws/orders/impl/request-OrdersTagSearchImpl-postTagSearch.json"));
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }
}
