package com.sos.joc.monitoring.db;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Paths;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArraySet;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSSerializer;
import com.sos.commons.util.SOSString;
import com.sos.controller.model.event.EventType;
import com.sos.joc.cluster.bean.history.AHistoryBean;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.monitoring.model.SerializedHistoryResult;
import com.sos.monitoring.notification.NotificationRange;
import com.sos.monitoring.notification.NotificationType;

public class DBLayerMonitoringTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(DBLayerMonitoringTest.class);

    @Ignore
    @Test
    public void test() throws Exception {

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            factory = createFactory();
            session = factory.openStatelessSession();

            DBLayerMonitoring dbLayer = new DBLayerMonitoring("test", null);
            dbLayer.setSession(session);

            LOGGER.info(SOSString.toString(dbLayer.getLastNotification("1", NotificationRange.WORKFLOW, 663L)));

            List<String> result = dbLayer.getNotificationNotificationIds(NotificationType.SUCCESS, NotificationRange.WORKFLOW, 711L, 1014L);
            LOGGER.info("RESULT SIZE= " + result.size());
            for (String n : result) {
                LOGGER.info(" " + n);
            }

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

    @Ignore
    @Test
    public void testPercentage() {
        int percentage = 5;
        Long avg = 9L;
        Double seconds = Double.valueOf(percentage) / 100 * Double.valueOf(avg);
        long result = new BigDecimal(seconds).setScale(0, RoundingMode.HALF_UP).longValue();
        LOGGER.info("RESULT: double=" + seconds + "(long=" + seconds.longValue() + "), result=" + result);
    }

    @Ignore
    @Test
    public void testSerialize() throws Exception {

        SOSHibernateFactory factory = null;
        try {
            factory = createFactory();

            CopyOnWriteArraySet<AHistoryBean> payloads = new CopyOnWriteArraySet<>();
            HashMap<Long, HistoryOrderStepBean> longerThan = new HashMap<>();

            DBItemHistoryOrderStep i1 = new DBItemHistoryOrderStep();
            i1.setId(1287L);
            i1.setHistoryOrderId(1125L);
            i1.setControllerId("js7.x");
            i1.setAgentId("agent");
            i1.setAgentUri("http://localhost:4445");
            i1.setWorkflowFolder("/");
            i1.setWorkflowPath("/shell");
            i1.setWorkflowName("shell");
            i1.setOrderId("#2022-03-18#T61988048100-root");
            i1.setJobName("job");
            i1.setCreated(new Date());
            i1.setModified(i1.getCreated());

            HistoryOrderStepBean b1 = new HistoryOrderStepBean(EventType.OrderProcessingStarted, 1647619880839002L, i1, "200%", null, null);
            longerThan.put(b1.getHistoryId(), b1);

            saveJocVariable(factory, new SOSSerializer<SerializedHistoryResult>().serializeCompressed2bytes(new SerializedHistoryResult(payloads,
                    longerThan)));
        } catch (Exception e) {
            throw e;
        } finally {
            if (factory != null) {
                factory.close();
            }
        }

    }

    private void saveJocVariable(SOSHibernateFactory factory, byte[] val) throws Exception {
        DBLayerMonitoring dbLayer = new DBLayerMonitoring("test", "monitor");
        try {
            dbLayer.setSession(factory.openStatelessSession(dbLayer.getIdentifier()));
            dbLayer.getSession().beginTransaction();
            dbLayer.saveVariable(val);
            dbLayer.getSession().commit();
        } catch (Exception e) {
            dbLayer.rollback();
            throw e;
        } finally {
            dbLayer.close();
        }
    }

    private SOSHibernateFactory createFactory() throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(Paths.get("src/test/resources/hibernate.cfg.xml"));
        factory.addClassMapping(DBLayer.getMonitoringClassMapping());
        factory.build();
        return factory;
    }

}
