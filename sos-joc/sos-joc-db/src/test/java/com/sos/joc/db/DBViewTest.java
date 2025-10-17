package com.sos.joc.db;

import java.nio.file.Paths;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSClassUtil;
import com.sos.commons.util.SOSString;

public class DBViewTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBViewTest.class);

    // If a view DBItem defines @Id on a nullable field (or one of the composite keys references a nullable field), and this field is actually NULL in a record,
    // Hibernate will return NULL instead of a DBItem for that row.
    @Ignore
    @Test
    public void testNullDBItemRows() {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            checkNullDBItemRows(session, DBLayer.DBITEM_INV_SCHEDULE2CALENDARS, "scheduleName,calendarName");
            checkNullDBItemRows(session, DBLayer.DBITEM_INV_RELEASED_SCHEDULE2CALENDARS, "scheduleName,calendarName");

        } catch (Exception e) {
            LOGGER.error(e.toString(), e);
        } finally {
            SOSClassUtil.closeQuietly(session);
            if (factory != null) {
                factory.close();
            }
        }
    }

    private void checkNullDBItemRows(SOSHibernateSession session, String dbItem, String orderByFields) throws Exception {
        LOGGER.info("[" + dbItem + "]start..");

        List<? extends DBItem> l = session.getResultList("from " + dbItem + (SOSString.isEmpty(orderByFields) ? "" : (" order by " + orderByFields)));
        long n = l.stream().filter(i -> i == null).count();
        LOGGER.info("[" + dbItem + "][total=" + l.size() + "]null=" + n);
        if (n > 0) {
            int c = 0;
            for (DBItem i : l) {
                c++;
                if (i == null) {
                    LOGGER.info("[" + c + "]null");
                } else {
                    LOGGER.info("[" + c + "]" + SOSHibernate.toString(i));
                }
            }
            throw new Exception("[" + dbItem + "]" + n + " NULL DBItem found");
        }
        LOGGER.info("[" + dbItem + "]end");
    }

    private SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.mysql.xml"));
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
        return factory;
    }

}
