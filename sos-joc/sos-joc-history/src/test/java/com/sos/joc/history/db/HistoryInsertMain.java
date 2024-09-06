package com.sos.joc.history.db;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.model.order.OrderStateText;

public class HistoryInsertMain {

    public static void main(String[] args) {
        if (args.length == 0) {
            printUsage();
            return;
        }
        Path hibernateConfigurationFile = null;
        String controllerId = "java_generator_controller";
        int orders = 100;
        int stepsPerOrder = 3;

        //orders = 100000;
        //stepsPerOrder = 1;

        for (String arg : args) {
            if (arg.startsWith("-hibernate_configuration_file=")) {
                hibernateConfigurationFile = Paths.get(arg.replace("-hibernate_configuration_file=", ""));
            } else if (arg.startsWith("-controller_id=")) {
                controllerId = arg.replace("-controller_id=", "");
            } else if (arg.startsWith("-orders")) {
                orders = Integer.parseInt(arg.replace("-orders=", ""));
            } else if (arg.startsWith("-steps_per_order")) {
                stepsPerOrder = Integer.parseInt(arg.replace("-steps_per_order=", ""));
            }
        }
        System.out.println("Arguments:");
        System.out.println("     -hibernate_configuration_file=" + hibernateConfigurationFile.toAbsolutePath());
        System.out.println("     -controller_id=" + controllerId);
        System.out.println("     -orders=" + orders);
        System.out.println("     -steps_per_order=" + stepsPerOrder);

        execute(hibernateConfigurationFile, controllerId, orders, stepsPerOrder);
    }

    private static void execute(Path hibernateConfigurationFile, String controllerId, int orders, int stepsPerOrder) {
        System.out.println("START-----");
        int ordersInserted = 0;
        int stepInserted = 0;

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {

            TimeZone.setDefault(TimeZone.getTimeZone("Etc/UTC"));

            factory = createFactory(hibernateConfigurationFile);
            session = factory.openStatelessSession("history");
            session.beginTransaction();

            Long orderEventIdCounter = Long.valueOf(0);
            Long orderEvenIdLastTimeMillis = Long.valueOf(0);

            for (int i = 0; i < orders; i++) {
                DBItemHistoryOrder order = newOrder(controllerId, stepsPerOrder, i, orderEventIdCounter, orderEvenIdLastTimeMillis);
                session.save(order);
                order.setMainParentId(order.getId());
                session.update(order);
                ordersInserted++;

                Long stepEventIdCounter = Long.valueOf(0);
                Long stepEvenIdLastTimeMillis = Long.valueOf(0);

                for (int j = 0; j < stepsPerOrder; j++) {
                    DBItemHistoryOrderStep step = newOrderStep(order, j, stepEventIdCounter, stepEvenIdLastTimeMillis);
                    session.save(step);
                    stepInserted++;

                    if (stepInserted % 1_000 == 0) {
                        session.commit();
                        session.beginTransaction();

                        System.out.println("    Inserted: orders=" + ordersInserted + ", steps=" + stepInserted + " ...");
                    }
                }
            }
            session.commit();

        } catch (Throwable e) {
            System.err.println(e);
        } finally {
            close(factory, session);

            System.out.println("END-----");
            System.out.println("Inserted: orders=" + ordersInserted + ", steps=" + stepInserted);
        }
    }

    private static Date getDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -180);
        return cal.getTime();
    }

    private static Long getEventId(Date date, Long counter, Long lastTimeMillis) {
        long baseTimeMillis = date.getTime();
        if (baseTimeMillis == lastTimeMillis.longValue()) {
            counter++;
        } else {
            counter = Long.valueOf(0);
            lastTimeMillis = Long.valueOf(baseTimeMillis);
        }
        return Long.valueOf(String.format("%013d%03d", baseTimeMillis % 10000000000000L, counter));
    }

    private static DBItemHistoryOrder newOrder(String controllerId, int stepsPerOrder, int counter, Long eventIdCounter, Long evenIdLastTimeMillis) {
        Date date = getDate();
        Long eventId = getEventId(date, eventIdCounter, evenIdLastTimeMillis);
        String orderId = "#java_generator_" + counter + "_" + eventId;

        DBItemHistoryOrder item = new DBItemHistoryOrder();
        item.setControllerId(controllerId);
        item.setOrderId(DBItemHistoryOrder.normalizeValue(orderId, 255));

        item.setWorkflowName("java_generator_workflow");
        item.setWorkflowFolder("/java_generator");
        item.setWorkflowPath(item.getWorkflowFolder() + "/" + item.getWorkflowName());
        item.setWorkflowVersionId("xyz");
        item.setWorkflowPosition("0");
        item.setWorkflowTitle(null);

        item.setMainParentId(Long.valueOf(0));
        item.setParentId(Long.valueOf(0));
        item.setParentOrderId(null);
        item.setHasChildren(false);
        item.setRetryCounter(Integer.valueOf(0));

        item.setName(item.getOrderId());
        item.setStartCause("order");// TODO

        item.setStartTimeScheduled(null);
        item.setStartTime(date);
        item.setStartWorkflowPosition(item.getWorkflowPosition());
        item.setStartEventId(eventId);

        item.setStartVariables(null);

        item.setCurrentHistoryOrderStepId(Long.valueOf(0));

        item.setEndTime(item.getStartTime());
        item.setEndWorkflowPosition((stepsPerOrder - 1) + "");
        item.setEndHistoryOrderStepId(Long.valueOf(0));
        item.setEndReturnCode(null);
        item.setEndMessage(null);
        item.setEndEventId(item.getStartEventId());

        item.setSeverity(OrderStateText.FINISHED);
        item.setState(OrderStateText.FINISHED);

        item.setStateTime(date);
        item.setStateText(null);
        item.setHasStates(false);

        item.setError(false);
        item.setErrorState(null);
        item.setErrorReason(null);
        item.setErrorReturnCode(null);
        item.setErrorCode(null);
        item.setErrorText(null);

        item.setLogId(Long.valueOf(0));

        item.setConstraintHash(hashOrderConstraint(item.getControllerId(), eventId, item.getOrderId()));
        item.setCreated(date);
        item.setModified(item.getCreated());
        return item;
    }

    private static DBItemHistoryOrderStep newOrderStep(DBItemHistoryOrder order, int counter, Long eventIdCounter, Long evenIdLastTimeMillis) {
        Date date = getDate();
        Long eventId = getEventId(date, eventIdCounter, evenIdLastTimeMillis);

        DBItemHistoryOrderStep item = new DBItemHistoryOrderStep();
        item.setControllerId(order.getControllerId());
        item.setOrderId(order.getOrderId());

        item.setWorkflowPath(order.getWorkflowPath());
        item.setWorkflowVersionId(order.getWorkflowVersionId());
        item.setWorkflowPosition(counter + "");
        item.setWorkflowFolder(order.getWorkflowFolder());
        item.setWorkflowName(order.getWorkflowName());

        item.setHistoryOrderMainParentId(order.getMainParentId());
        item.setHistoryOrderId(order.getId());
        item.setPosition(Integer.valueOf(counter));
        item.setRetryCounter(Integer.valueOf(0));

        item.setJobName("job_" + counter);
        item.setJobLabel("job_label_" + counter);
        item.setJobTitle(null);
        item.setCriticality(JobCriticality.NORMAL.intValue());
        item.setJobNotification(null);

        item.setAgentId("standaloneAgent");
        item.setAgentName(item.getAgentId());
        item.setAgentUri("http://localhost:4445");
        item.setSubagentClusterId(null);

        item.setStartCause("order");
        item.setStartTime(date);
        item.setStartEventId(eventId);

        item.setStartVariables(null);

        item.setEndTime(item.getStartTime());
        item.setEndEventId(item.getStartEventId());

        item.setReturnCode(Integer.valueOf(0));
        item.setSeverity(OrderStateText.FINISHED);

        item.setError(false);
        item.setErrorCode(null);
        item.setErrorText(null);

        item.setLogId(Long.valueOf(0));

        item.setConstraintHash(hashOrderStepConstraint(item.getControllerId(), eventId, item.getOrderId(), item.getWorkflowPosition()));
        item.setCreated(date);
        item.setModified(item.getCreated());
        return item;
    }

    private static String hashOrderConstraint(String controllerId, Long eventId, String orderId) {
        return SOSString.hash256(new StringBuilder(controllerId).append(eventId).append(orderId).toString());
    }

    private static String hashOrderStepConstraint(String controllerId, Long eventId, String orderId, String workflowPosition) {
        return SOSString.hash256(new StringBuilder(controllerId).append(eventId).append(orderId).append(workflowPosition).toString());
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("     -hibernate_configuration_file=<path>");
        System.out.println("     -controller_id=<controllerId>");
        System.out.println("     -orders=<number>");
        System.out.println("     -steps_per_order=<number>");
    }

    private static SOSHibernateFactory createFactory(Path configFile) throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(configFile);
        factory.setIdentifier("history");
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.build();
        return factory;
    }

    private static void close(SOSHibernateFactory factory, SOSHibernateSession session) {
        if (factory != null) {
            factory.close(session);
            factory = null;
        }
    }
}
