package com.sos.joc.yade.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.model.yade.TransferFilter;
import com.sos.joc.model.yade.TransferId;

public class YADETransfersResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(YADETransfersResourceImplTest.class);

    @Ignore
    @Test
    public void testPostYADETransfers() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new YADETransfersResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        try {
            h.init();

            TransferFilter in = new TransferFilter();
            in.setCompact(false);

            h.post("postYADETransfers", in);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

    @Ignore
    @Test
    public void testPostYADETransfer() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new YADETransfersResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.mysql.xml");

        try {
            h.init();

            TransferId in = new TransferId();
            in.setCompact(false);
            in.setTransferId(132L);

            h.post("postYADETransfer", in);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }
}
