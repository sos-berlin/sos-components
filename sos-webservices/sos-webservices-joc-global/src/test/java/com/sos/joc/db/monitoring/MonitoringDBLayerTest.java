package com.sos.joc.db.monitoring;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.ScrollableResults;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernate;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryAgent;
import com.sos.joc.db.history.DBItemHistoryController;

public class MonitoringDBLayerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MonitoringDBLayerTest.class);

    @Ignore
    @Test
    public void test() throws Exception {

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        ScrollableResults sr = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            Date dateFrom = null;
            String controllerId = "js7.x";
            Integer limit = null;

            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);
            sr = dbLayer.getNotifications(dateFrom, controllerId, null, limit);
            int size = 0;
            while (sr.next()) {
                NotificationDBItemEntity item = (NotificationDBItemEntity) sr.get(0);
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

    @Ignore
    @Test
    public void testControllers() throws Exception {

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        ScrollableResults sr = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            String searchControllerId = null;
            Date searchDateFrom = null;
            Date searchDateTo = null;
            MonitoringDBLayer dbLayer = new MonitoringDBLayer(session);
            sr = dbLayer.getControllers(searchControllerId, searchDateFrom, searchDateTo);
            int size = 0;

            Map<String, List<DBItemHistoryController>> map = new HashMap<>();
            while (sr.next()) {
                DBItemHistoryController item = (DBItemHistoryController) sr.get(0);
                List<DBItemHistoryController> l = map.containsKey(item.getControllerId()) ? map.get(item.getControllerId()) : new ArrayList<>();

                l.add(item);
                map.put(item.getControllerId(), l);
                size++;
            }

            long totalRunningTime = 0;
            for (String controllerId : map.keySet()) {
                LOGGER.info("controllerId=" + controllerId);
                List<DBItemHistoryController> l = map.get(controllerId);
                int lSize = l.size();
                for (int i = 0; i < lSize; i++) {
                    boolean isLast = i == lSize - 1;
                    DBItemHistoryController item = l.get(i);
                    if (item.getShutdownTime() == null) {
                        if (!isLast) {
                            DBItemHistoryController nextItem = l.get(i + 1);
                            // Long trt = nextItem.getTotalRunningTime() - item.getTotalRunningTime();
                            // item.setTotalRunningTime(trt);
                            // item.setShutdownTime(SOSDate.add(item.getReadyTime(), trt, ChronoUnit.MILLIS));
                            long diff = nextItem.getReadyEventId() - item.getReadyEventId();
                            totalRunningTime += diff;
                            item.setTotalRunningTime(totalRunningTime);
                            item.setShutdownTime(nextItem.getReadyTime());
                        }

                    } else {
                        long diff = item.getShutdownTime().getTime() - item.getReadyTime().getTime();
                        totalRunningTime += diff;
                        // item.setTotalRunningTime(item.getTotalRunningTime() + diff);
                        item.setTotalRunningTime(totalRunningTime);
                    }
                    LOGGER.info("   " + SOSHibernate.toString(item));
                    if (isLast) {
                        LOGGER.info("LAST------------");
                    }
                }
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
        factory.addClassMapping(DBLayer.getMonitoringClassMapping());
        factory.addClassMapping(DBItemHistoryController.class);
        factory.addClassMapping(DBItemHistoryAgent.class);
        factory.build();
        return factory;
    }

}
