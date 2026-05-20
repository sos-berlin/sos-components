package com.sos.joc.inventory.impl;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.joc.UnitTestSimpleWSImplHelper;
import com.sos.joc.model.inventory.search.RequestQuickSearchFilter;

public class QuickSearchResourceImplTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuickSearchResourceImplTest.class);

    @Ignore
    @Test
    public void testPostSearch() throws Exception {
        UnitTestSimpleWSImplHelper h = new UnitTestSimpleWSImplHelper(new QuickSearchResourceImpl());
        h.setHibernateConfigurationFileFromWebservicesGlobal("hibernate.cfg.oracle-21c.xml");
        
        RequestQuickSearchFilter in = new RequestQuickSearchFilter();
        in.setSearch("te?*t");
        in.setSearch("al?");
        in.setSearch("test_");
        in.setToken("ae53f3be24838ff1b5cc95c1789462719094210c3a3290092874f88f0c786c21");
        
        try {
            h.init();

            h.post("postSearch", in);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            h.destroy();
        }
    }

}
