package com.sos.commons.hibernate;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.hibernate.dialect.MySQLDialect;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.common.SOSBatchObject;
import com.sos.commons.util.SOSString;
import com.sos.joc.db.DBItem;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.DBSQLBatchPreparator;
import com.sos.joc.db.DBSQLBatchPreparator.BatchPreparator;
import com.sos.joc.db.history.DBItemHistoryOrderStep;

public class DBSQLBatchPreparatorTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBSQLBatchPreparatorTest.class);

    @Ignore
    @Test
    public void testBatchPreparatorInsertResult() throws IOException {

        BatchPreparator result = DBSQLBatchPreparator.prepareForSQLBatchInsert(new MySQLDialect(), getTestItems());
        if (result != null) {
            LOGGER.info(result.getSQL());
            int i = 0;
            for (Collection<SOSBatchObject> row : result.getRows()) {
                i++;
                LOGGER.info(i + ") row =");
                for (SOSBatchObject o : row) {
                    LOGGER.info("         " + SOSString.toString(o));
                }
            }
        }
    }

    @Ignore
    @Test
    public void testBatchInsert() throws Exception {
        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            // create connection
            factory = createFactory();

            // prepare items
            BatchPreparator result = DBSQLBatchPreparator.prepareForSQLBatchInsert(factory.getDialect(), getTestItems());

            // execute
            session = factory.openStatelessSession();
            session.beginTransaction();
            session.getSQLExecutor().executeBatch(result.getTableName(), result.getSQL(), result.getRows());
            session.commit();
        } catch (Exception e) {
            throw e;
        } finally {
            if (session != null) {
                session.close();
            }
            if (factory != null) {
                factory.close();
            }
        }
    }

    private List<DBItem> getTestItems() {
        DBItemHistoryOrderStep item1 = new DBItemHistoryOrderStep();
        item1.setControllerId("controllerId_1");
        item1.setCreated(new Date());
        DBItemHistoryOrderStep item2 = new DBItemHistoryOrderStep();
        item2.setControllerId("controllerId_2");
        item1.setCreated(new Date());

        List<DBItem> items = new ArrayList<>();
        items.add(item1);
        items.add(item2);
        return items;
    }

    @SuppressWarnings("unused")
    private SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.xml"));
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.build();
        return factory;
    }

}
