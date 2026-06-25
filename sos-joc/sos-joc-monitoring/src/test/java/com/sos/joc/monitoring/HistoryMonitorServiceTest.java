package com.sos.joc.monitoring;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.hibernate.exception.SOSHibernateException;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSReflection;
import com.sos.controller.model.event.EventType;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.Globals;
import com.sos.joc.classes.JocCockpitProperties;
import com.sos.joc.cluster.bean.history.HistoryOrderStepBean;
import com.sos.joc.cluster.common.JocClusterUtil;
import com.sos.joc.cluster.configuration.JocClusterConfiguration;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.cluster.configuration.JocConfiguration;
import com.sos.joc.cluster.configuration.controller.ControllerConfiguration;
import com.sos.joc.cluster.service.active.IJocActiveMemberService;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.event.bean.history.HistoryOrderTaskStarted;
import com.sos.joc.event.bean.history.HistoryOrderTaskTerminated;
import com.sos.joc.event.bean.history.HistoryTaskEvent;
import com.sos.joc.model.common.JocSecurityLevel;
import com.sos.joc.model.order.OrderStateText;
import com.sos.joc.monitoring.model.HistoryMonitoringModel;

public class HistoryMonitorServiceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(HistoryMonitorServiceTest.class);

    private static final int NUMBER_OF_GENERATED_PAYLOADS = 10;

    @Ignore
    @Test
    public void test() {
        try {
            TimeZone.setDefault(TimeZone.getTimeZone("UTC"));

            HistoryMonitorService service = new HistoryMonitorService(getJocConfig(), new ThreadGroup(JocClusterConfiguration.IDENTIFIER));
            service.start(StartupMode.manual_restart, getControllers(), null);
            setPayload(service);
            stopAfter(service, StartupMode.manual_restart, 2);
        } catch (Throwable e) {
            LOGGER.error(e.toString(), e);
        } finally {
            if (Globals.getHibernateFactory() != null) {
                SOSHibernateSession session = null;
                try {
                    session = Globals.getHibernateFactory().openStatelessSession();
                    session.setAutoCommit(false);
                    session.beginTransaction();
                    session.executeUpdate("delete from " + DBLayer.DBITEM_MON_ORDER_STEPS + " where jobName='junit'");
                    session.executeUpdate("delete from " + DBLayer.DBITEM_JOC_VARIABLES + " where name='monitor'");
                    session.commit();
                    session = null;
                } catch (Exception e) {
                    if (session != null) {
                        try {
                            session.rollback();
                        } catch (SOSHibernateException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                    }
                    e.printStackTrace();
                }
                if (session != null) {
                    session.close();
                }
                Globals.closeFactory();
            }
        }
    }

    private void setPayload(HistoryMonitorService service) throws Exception {
        HistoryMonitoringModel m = SOSReflection.getDeclaredField(service, HistoryMonitoringModel.class, "history");

        String controllerId = "junit";
        String orderId = "junit";
        String workflowName = "junit";
        String workflowVersion = "123";

        String warnIfLonger = "1";
        String warnIfShorter = null;
        List<SortedSet<Integer>> warnReturnCodes = null;
        String notification = null;

        new Thread(() -> {

            for (int i = 0; i < NUMBER_OF_GENERATED_PAYLOADS; i++) {
                int counter = i + 1;
                LOGGER.info(counter + ")---------handleHistoryEvents-----------------");

                Date now = SOSDate.addToDate(new Date(), -10, ChronoUnit.SECONDS);
                DBItemHistoryOrderStep item = createStepStartDbItem(counter, now, controllerId, orderId, workflowName, workflowVersion);

                HistoryTaskEvent startEvt = createStepStartEvent(counter, now, item, warnIfLonger, warnIfShorter, warnReturnCodes, notification);
                @SuppressWarnings("unused")
                HistoryTaskEvent endEvt = createStepEndEvent(counter, now, item, warnIfLonger, warnIfShorter, warnReturnCodes, notification);

                m.handleHistoryEvents(startEvt);
                // m.handleHistoryEvents(endEvt);

                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private static DBItemHistoryOrderStep createStepStartDbItem(int counter, Date now, String controllerId, String orderId, String workflowName,
            String workflowVersion) {
        Date startTime = SOSDate.addToDate(now, -60, ChronoUnit.SECONDS);
        Long startEventId = JocClusterUtil.getDateAsEventId(startTime);

        DBItemHistoryOrderStep item = new DBItemHistoryOrderStep();
        item.setAgentId("primaryAgent");
        item.setAgentName(item.getAgentId());
        item.setAgentUri("http://localhost:4555");
        item.setConstraintHash(null);
        item.setControllerId(controllerId);
        // item.setCreated(now);
        item.setCriticality(JobCriticality.NORMAL);
        item.setCriticality(item.getCriticalityAsEnum().intValue());
        item.setEndEventId(null);
        item.setEndTime(null);
        item.setHistoryOrderId(1L);
        item.setHistoryOrderMainParentId(1L);
        item.setId(Long.valueOf(counter));
        item.setJobLabel("junit");
        item.setJobName("junit");
        item.setLogId(0l);
        // item.setModified(now);
        item.setOrderId(orderId);
        item.setPosition(Integer.valueOf(0));
        item.setRetryCounter(Integer.valueOf(0));
        item.setReturnCode(Integer.valueOf(0));
        item.setSeverity(OrderStateText.RUNNING);
        item.setSeverity(OrderStateText.RUNNING.intValue());
        item.setStartCause("order");
        item.setStartTime(startTime);
        item.setStartEventId(startEventId);
        item.setWorkflowFolder("junit");
        item.setWorkflowName(workflowName);
        item.setWorkflowPath("");
        item.setWorkflowPosition("0");
        item.setWorkflowVersionId(workflowVersion);
        return item;
    }

    private static HistoryTaskEvent createStepStartEvent(int counter, Date now, DBItemHistoryOrderStep item, String warnIfLonger,
            String warnIfShorter, List<SortedSet<Integer>> warnReturnCodes, String notification) {
        EventType eventType = EventType.OrderProcessingStarted;
        Long eventId = JocClusterUtil.getDateAsEventId(now) + counter;

        HistoryOrderStepBean payload = new HistoryOrderStepBean(eventType, eventId, item, warnIfLonger, warnIfShorter, warnReturnCodes, notification);
        return new HistoryOrderTaskStarted(item.getControllerId(), item.getOrderId(), item.getWorkflowName(), item.getWorkflowVersionId(), payload);
    }

    private static HistoryTaskEvent createStepEndEvent(int counter, Date now, DBItemHistoryOrderStep item, String warnIfLonger, String warnIfShorter,
            List<SortedSet<Integer>> warnReturnCodes, String notification) {
        EventType eventType = EventType.OrderProcessed;
        Long eventId = JocClusterUtil.getDateAsEventId(now) + counter;

        Date endTime = SOSDate.addToDate(now, -10, ChronoUnit.SECONDS);
        Long endEventId = JocClusterUtil.getDateAsEventId(endTime);

        item.setEndEventId(endEventId);
        item.setEndTime(endTime);
        item.setReturnCode(Integer.valueOf(0));
        item.setSeverity(OrderStateText.FINISHED);
        item.setSeverity(OrderStateText.FINISHED.intValue());

        HistoryOrderStepBean payload = new HistoryOrderStepBean(eventType, eventId, item, warnIfLonger, warnIfShorter, warnReturnCodes, notification);
        return new HistoryOrderTaskTerminated(item.getControllerId(), item.getOrderId(), item.getWorkflowName(), item.getWorkflowVersionId(),
                payload);
    }

    private JocConfiguration getJocConfig() {
        Path resDir = Paths.get("src/test/resources").toAbsolutePath();
        Path hibernate = resDir.resolve("hibernate").resolve("hibernate.cfg.mysql.xml");

        Globals.sosCockpitProperties = new JocCockpitProperties();
        Globals.sosCockpitProperties.getProperties().setProperty("hibernate_configuration_file", hibernate.toString());
        Globals.sosCockpitProperties.getProperties().setProperty("history_log_dir", resDir.resolve("logs").toString());

        return new JocConfiguration(resDir.toString(), "UTC", hibernate, resDir, JocSecurityLevel.LOW, false, "title", "joc", 0, "joc#0", "2.8.4");

    }

    private List<ControllerConfiguration> getControllers() {
        Properties p = new Properties();
        p.setProperty("controller_id", "js7.x");
        p.setProperty("primary_controller_uri", "http://localhost:5444");

        List<ControllerConfiguration> list = new ArrayList<ControllerConfiguration>();
        ControllerConfiguration c = new ControllerConfiguration();
        try {
            c.load(p);
            list.add(c);
        } catch (Exception e) {
        }

        return list;
    }

    private static void stopAfter(IJocActiveMemberService service, StartupMode mode, int seconds) {
        LOGGER.info(String.format("[start][stopAfter][%ss]...", seconds));

        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e) {

        } finally {
            service.stop(mode);
        }
        LOGGER.info(String.format("[end][stopAfter][%ss]", seconds));
    }
}
