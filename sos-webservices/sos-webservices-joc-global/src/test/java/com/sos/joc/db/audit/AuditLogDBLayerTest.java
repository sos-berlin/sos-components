package com.sos.joc.db.audit;

import java.nio.file.Paths;

import org.hibernate.ScrollableResults;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.audit.AuditLogFilter;

public class AuditLogDBLayerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuditLogDBLayerTest.class);

    @Ignore
    @Test
    public void test() throws Exception {

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        ScrollableResults sr = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            String controllerId = "js7.x";
            Integer limit = null;

            AuditLogFilter aFilter = new AuditLogFilter();
            aFilter.setControllerId(controllerId);
            aFilter.setDateFrom(null);

            // Collection<String> controllerIds, Collection<CategoryType> categories,
            // Collection<Long> auditLogIds

            AuditLogDBLayer dbLayer = new AuditLogDBLayer(session);
            sr = dbLayer.getAuditLogs(new AuditLogDBFilter(aFilter, null, null, null), limit);
            int size = 0;
            while (sr.next()) {
                AuditLogDBItem item = (AuditLogDBItem) sr.get(0);
                LOGGER.info(SOSHibernate.toString(item));
                size++;
            }
            LOGGER.info("SIZE=" + size);
        } catch (Exception e) {
            throw e;
        } finally {
            if (sr != null) {
                sr.close();
            }
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }

    }

    private SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.xml"));
        factory.addClassMapping(DBLayer.getJocClassMapping());
        factory.build();
        return factory;
    }

}
