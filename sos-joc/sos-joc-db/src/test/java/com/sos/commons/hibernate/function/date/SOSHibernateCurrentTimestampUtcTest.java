package com.sos.commons.hibernate.function.date;

import java.util.List;

import org.hibernate.query.Query;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.SOSHibernateTest;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBLayer;

public class SOSHibernateCurrentTimestampUtcTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(SOSHibernateCurrentTimestampUtcTest.class);

    @Ignore
    @Test
    public void test() {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = SOSHibernateTest.createFactory();
            session = factory.openStatelessSession();

            StringBuilder hql = new StringBuilder("select uri, ");
            hql.append(SOSHibernateCurrentTimestampUtc.getFunction()).append(" as CURRENT_UTC ");
            hql.append("from ").append(DBLayer.DBITEM_HISTORY_CONTROLLERS).append(" ");

            Query<?> query = session.createQuery(hql.toString());
            query.setMaxResults(1);
            List<?> result = session.getResultList(query);
            LOGGER.info("---- size=" + result.size());
            for (Object o : result) {
                LOGGER.info(SOSString.toString(o));
            }

        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        } finally {
            if (factory != null) {
                factory.close(session);
            }
        }
    }

}
