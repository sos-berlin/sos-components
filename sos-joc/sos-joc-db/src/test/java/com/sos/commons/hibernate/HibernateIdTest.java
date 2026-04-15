package com.sos.commons.hibernate;

import java.time.Instant;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.helpers.dbitems.DBItemATest;
import com.sos.commons.util.SOSClassList;

public class HibernateIdTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HibernateIdTest.class);

    @Ignore
    @Test
    public void testInsert() throws Exception {

        SOSClassList mapping = new SOSClassList();
        mapping.add(DBItemATest.class);

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory(mapping);
            session = factory.openStatelessSession();

            DBItemATest item = new DBItemATest();
            item.setName("xxxx");
            item.setCreated(Instant.now());
            item.setModified(item.getCreated());

            session.beginTransaction();
            session.save(item);
            session.commit();

        } catch (Throwable e) {
            if (session != null) {
                session.rollback();
            }
            LOGGER.error(e.toString(), e);
        } finally {
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }

}
