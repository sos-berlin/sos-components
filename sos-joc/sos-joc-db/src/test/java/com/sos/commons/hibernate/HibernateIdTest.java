package com.sos.commons.hibernate;

import java.time.Instant;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.helpers.dbitems.DBItemATest;
import com.sos.commons.util.SOSClassList;
import com.sos.commons.util.SOSString;

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
            item.setJavaDateManual(Instant.now());

            session.beginTransaction();
            session.save(item);

            item.setName("xxxx-updated");

            // set DateNullable with current UTC timestamp
            // see explanation below : ... item.setDateNullable(item.getDbCurrentTimestampUtcAuto());
            item.setDateNullable(session.getCurrentTimestampAsInstant());

            session.update(item);

            LOGGER.info("[AFTER_UPDATE]" + SOSString.toString(item));
            // Does NOT work with StatelessSession - getDbCurrentTimestampUtcAuto remains null after save because StatelessSession has no persistence context
            // and does not update the
            // entity.
            // item.setDateNullable(item.getDbCurrentTimestampUtcAuto());
            // session.update(item);

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
