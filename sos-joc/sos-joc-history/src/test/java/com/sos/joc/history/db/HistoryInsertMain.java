package com.sos.joc.history.db;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.sos.commons.exception.SOSInvalidDataException;
import com.sos.commons.hibernate.SOSHibernateFactory;
import com.sos.commons.hibernate.SOSHibernateSession;
import com.sos.commons.util.SOSDate;
import com.sos.commons.util.SOSGzip;
import com.sos.commons.util.SOSPath;
import com.sos.commons.util.SOSString;
import com.sos.inventory.model.job.JobCriticality;
import com.sos.joc.db.DBLayer;
import com.sos.joc.db.history.DBItemHistoryLog;
import com.sos.joc.db.history.DBItemHistoryOrder;
import com.sos.joc.db.history.DBItemHistoryOrderStep;
import com.sos.joc.db.monitoring.DBItemMonitoringOrder;
import com.sos.joc.db.monitoring.DBItemMonitoringOrderStep;
import com.sos.joc.model.order.OrderStateText;

public class HistoryInsertMain {

    private static final String FILE_NAME_TEMPLATE_ORDER_LOG = "template_order_log.log";
    private static final String FILE_NAME_TEMPLATE_ORDER_STEP_LOG = "template_order_step_log.log";

    private static final int GENERATE_ENTRIES_FOR_DAYS_IN_THE_PAST = 180;

    public static void main(String[] args) {
        int orders = 100;
        int stepsPerOrder = 3;
        boolean insertMonitoring = true;

        if (args.length == 0) {
            printUsage(orders, stepsPerOrder, insertMonitoring);
            return;
        }
        Path hibernateConfigurationFile = null;
        String controllerId = "java_generator_controller";
        // orders = 100000;
        // stepsPerOrder = 1;

        for (String arg : args) {
            if (arg.startsWith("-hibernate_configuration_file=")) {
                hibernateConfigurationFile = Paths.get(arg.replace("-hibernate_configuration_file=", ""));
            } else if (arg.startsWith("-controller_id=")) {
                controllerId = arg.replace("-controller_id=", "");
            } else if (arg.startsWith("-orders")) {
                orders = Integer.parseInt(arg.replace("-orders=", ""));
            } else if (arg.startsWith("-steps_per_order")) {
                stepsPerOrder = Integer.parseInt(arg.replace("-steps_per_order=", ""));
            } else if (arg.startsWith("-insert_monitoring")) {
                insertMonitoring = Boolean.parseBoolean(arg.replace("-insert_monitoring=", ""));
            }
        }
        System.out.println("Arguments:");
        System.out.println("     -hibernate_configuration_file=" + hibernateConfigurationFile.toAbsolutePath());
        System.out.println("     -controller_id=" + controllerId);
        System.out.println("     -orders=" + orders);
        System.out.println("     -steps_per_order=" + stepsPerOrder);
        System.out.println("     -insert_monitoring=" + insertMonitoring);
        System.out.println("     Insert Logs:");
        Path orderLog = Paths.get(FILE_NAME_TEMPLATE_ORDER_LOG);
        if (Files.exists(orderLog)) {
            System.out.println("         [Order Logs]from \"" + FILE_NAME_TEMPLATE_ORDER_LOG + "\"");
        } else {
            System.out.println("         [skip][template file not found][Order Logs]" + orderLog.toAbsolutePath());
            orderLog = null;
        }
        Path orderStepLog = Paths.get(FILE_NAME_TEMPLATE_ORDER_STEP_LOG);
        if (Files.exists(orderStepLog)) {
            System.out.println("         [Order Step Logs]from \"" + FILE_NAME_TEMPLATE_ORDER_STEP_LOG + "\"");
        } else {
            System.out.println("         [skip][template file not found][Order Step Logs]" + orderStepLog.toAbsolutePath());
            orderStepLog = null;
        }
        execute(hibernateConfigurationFile, controllerId, orders, stepsPerOrder, insertMonitoring, orderLog, orderStepLog);
    }

    private static void execute(Path hibernateConfigurationFile, String controllerId, int orders, int stepsPerOrder, boolean insertMonitoring,
            Path orderLogFile, Path orderStepLogFile) {
        Instant start = Instant.now();
        try {
            System.out.println("");
            System.out.println("START----- [Entries are generated for " + GENERATE_ENTRIES_FOR_DAYS_IN_THE_PAST + " days in the past: " + SOSDate
                    .getDateAsString(getDate()) + "]");
        } catch (SOSInvalidDataException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        int insertedOrders = 0;
        int insertedOrderSteps = 0;

        int insertedOrderLogs = 0;
        int insertedOrderStepLogs = 0;

        int insertedMonitoringOrders = 0;
        int insertedMonitoringOrderSteps = 0;

        SOSHibernateFactory factory = null;
        SOSHibernateSession session = null;
        try {
            TimeZone.setDefault(TimeZone.getTimeZone(SOSDate.TIMEZONE_UTC));

            factory = createFactory(hibernateConfigurationFile);
            session = factory.openStatelessSession("history");
            session.beginTransaction();

            Long orderEventIdCounter = Long.valueOf(0);
            Long orderEvenIdLastTimeMillis = Long.valueOf(0);

            DBItemHistoryLog orderLogTemplate = null;
            DBItemHistoryLog orderStepLogTemplate = null;
            if (orderLogFile != null) {
                orderLogTemplate = logTemplateOrder(orderLogFile);
            }
            if (orderStepLogFile != null) {
                orderStepLogTemplate = logTemplateStep(orderStepLogFile);
            }
            for (int i = 0; i < orders; i++) {
                DBItemHistoryOrder order = newOrder(controllerId, stepsPerOrder, i, orderEventIdCounter, orderEvenIdLastTimeMillis);
                session.save(order);

                // Not set MainParentId - because cleanup will delete all entries before this insert (delete <= max MainParentId ....)
                // order.setMainParentId(order.getId());
                // session.update(order);
                insertedOrders++;

                if (orderLogTemplate != null) {
                    DBItemHistoryLog orderLog = newOrderLog(order, orderLogTemplate);
                    session.save(orderLog);

                    order.setLogId(orderLog.getId());
                    session.update(order);

                    insertedOrderLogs++;
                }

                DBItemMonitoringOrder monitorOrder = null;
                if (insertMonitoring) {
                    monitorOrder = newMonitoringOrder(order);
                    session.save(monitorOrder);
                    insertedMonitoringOrders++;
                }

                Long stepEventIdCounter = Long.valueOf(0);
                Long stepEvenIdLastTimeMillis = Long.valueOf(0);
                for (int j = 0; j < stepsPerOrder; j++) {
                    DBItemHistoryOrderStep step = newOrderStep(order, j, stepEventIdCounter, stepEvenIdLastTimeMillis);
                    session.save(step);
                    insertedOrderSteps++;

                    if (orderStepLogTemplate != null) {
                        DBItemHistoryLog orderStepLog = newOrderStepLog(step, orderStepLogTemplate);
                        session.save(orderStepLog);

                        step.setLogId(orderStepLog.getId());
                        session.update(step);

                        insertedOrderStepLogs++;
                    }

                    DBItemMonitoringOrderStep monitorOrderStep = null;
                    if (insertMonitoring) {
                        monitorOrderStep = newMonitoringOrderStep(step);
                        session.save(monitorOrderStep);
                        insertedMonitoringOrderSteps++;
                    }

                    if (insertedOrderSteps % 1_000 == 0) {
                        session.commit();
                        session.beginTransaction();

                        System.out.println("    [Inserted][History]orders=" + insertedOrders + ", orderSteps=" + insertedOrderSteps + " ...");
                        if (insertedOrderLogs > 0 || insertedOrderStepLogs > 0) {
                            System.out.println("              orderLogs=" + insertedOrderLogs + ", orderStepLogs=" + insertedOrderStepLogs + " ...");
                        }
                        if (insertMonitoring) {
                            System.out.println("              [Monitoring]orders=" + insertedMonitoringOrders + ", orderSteps="
                                    + insertedMonitoringOrderSteps);
                        }
                        System.out.println("              [Duration]" + SOSDate.getDuration(start, Instant.now()));
                    }
                }
            }
            session.commit();

        } catch (Throwable e) {
            System.err.println(e);
        } finally {
            close(factory, session);

            System.out.println("END----- [Duration=" + SOSDate.getDuration(start, Instant.now()) + "]");
            System.out.println("[Inserted][History]orders=" + insertedOrders + ", orderSteps=" + insertedOrderSteps);
            if (insertedOrderLogs > 0 || insertedOrderStepLogs > 0) {
                System.out.println("              orderLogs=" + insertedOrderLogs + ", orderStepLogs=" + insertedOrderStepLogs);
            }
            if (insertMonitoring) {
                System.out.println("              [Monitoring]orders=" + insertedMonitoringOrders + ", orderSteps=" + insertedMonitoringOrderSteps);
            }
        }
    }

    private static Date getDate() {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DAY_OF_YEAR, -1 * GENERATE_ENTRIES_FOR_DAYS_IN_THE_PAST);
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

        item.setMainParentId(Long.valueOf(1));
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

    private static DBItemMonitoringOrder newMonitoringOrder(DBItemHistoryOrder history) {

        DBItemMonitoringOrder item = new DBItemMonitoringOrder();
        item.setHistoryId(history.getId());
        item.setControllerId(history.getControllerId());
        item.setOrderId(history.getOrderId());

        item.setWorkflowPath(history.getWorkflowPath());
        item.setWorkflowVersionId(history.getWorkflowVersionId());
        item.setWorkflowPosition(history.getWorkflowPosition());
        item.setWorkflowFolder(history.getWorkflowFolder());
        item.setWorkflowName(history.getWorkflowName());
        item.setWorkflowTitle(history.getWorkflowTitle());

        item.setMainParentId(history.getMainParentId());
        item.setParentId(history.getParentId());
        item.setParentOrderId(null);
        item.setHasChildren(history.getHasChildren());

        item.setName(history.getName());

        item.setStartCause(history.getStartCause());
        item.setStartTimeScheduled(history.getStartTimeScheduled());
        item.setStartTime(history.getStartTime());
        item.setStartWorkflowPosition(history.getStartWorkflowPosition());
        item.setStartVariables(history.getStartVariables());

        item.setCurrentHistoryOrderStepId(history.getCurrentHistoryOrderStepId());

        item.setEndTime(history.getStartTime());
        item.setEndWorkflowPosition(history.getEndWorkflowPosition());
        item.setEndHistoryOrderStepId(history.getEndHistoryOrderStepId());
        item.setEndReturnCode(history.getEndReturnCode());
        item.setEndMessage(history.getEndMessage());

        item.setSeverity(history.getSeverity());
        item.setState(history.getState());
        item.setStateTime(history.getStateTime());

        item.setError(history.getError());
        item.setErrorState(history.getErrorState());
        item.setErrorReason(history.getErrorReason());
        item.setErrorReturnCode(history.getErrorReturnCode());
        item.setErrorCode(history.getErrorCode());
        item.setErrorText(history.getErrorText());

        item.setLogId(history.getLogId());

        item.setCreated(history.getCreated());
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

    private static DBItemMonitoringOrderStep newMonitoringOrderStep(DBItemHistoryOrderStep history) {

        DBItemMonitoringOrderStep item = new DBItemMonitoringOrderStep();
        item.setHistoryId(history.getId());
        item.setWorkflowPosition(history.getWorkflowPosition());
        item.setHistoryOrderMainParentId(history.getHistoryOrderMainParentId());
        item.setHistoryOrderId(history.getHistoryOrderId());
        item.setPosition(history.getPosition());

        item.setJobName(history.getJobName());
        item.setJobLabel(history.getJobLabel());
        item.setJobTitle(history.getJobTitle());
        item.setJobCriticality(history.getCriticality());
        item.setJobNotification(history.getJobNotification());

        item.setAgentId(history.getAgentId());
        item.setAgentName(history.getAgentName());
        item.setAgentUri(history.getAgentUri());
        item.setSubagentClusterId(history.getSubagentClusterId());

        item.setStartCause(history.getStartCause());
        item.setStartTime(history.getStartTime());
        item.setStartVariables(history.getStartVariables());

        item.setEndTime(history.getEndTime());
        item.setEndVariables(history.getEndVariables());

        item.setReturnCode(history.getReturnCode());
        item.setSeverity(history.getSeverity());

        item.setError(history.getError());
        item.setErrorCode(history.getErrorCode());
        item.setErrorText(history.getErrorText());

        item.setLogId(history.getLogId());

        item.setCreated(history.getCreated());
        item.setModified(item.getCreated());
        return item;
    }

    private static DBItemHistoryLog logTemplateOrder(Path file) throws Exception {
        DBItemHistoryLog item = new DBItemHistoryLog();
        item.setFileBasename(SOSPath.getBasename(file));
        item.setFileSizeUncomressed(Files.size(file));
        item.setFileLinesUncomressed(SOSPath.getLineCount(file));
        item.setFileContent(SOSPath.readFile(file).getBytes(StandardCharsets.UTF_8));
        return item;
    }

    private static DBItemHistoryLog logTemplateStep(Path file) throws Exception {
        DBItemHistoryLog item = new DBItemHistoryLog();
        item.setFileBasename(SOSPath.getBasename(file));
        item.setFileSizeUncomressed(Files.size(file));
        item.setFileLinesUncomressed(SOSPath.getLineCount(file));
        item.setFileContent(SOSGzip.compress(file, false).getCompressed());
        return item;
    }

    private static DBItemHistoryLog newOrderLog(DBItemHistoryOrder order, DBItemHistoryLog template) throws Exception {
        DBItemHistoryLog item = new DBItemHistoryLog();
        item.setControllerId(order.getControllerId());
        item.setHistoryOrderId(order.getId());
        item.setHistoryOrderMainParentId(order.getMainParentId());
        item.setHistoryOrderStepId(Long.valueOf(0));

        item.setCompressed(false);
        item.setFileBasename(template.getFileBasename());
        item.setFileSizeUncomressed(template.getFileSizeUncomressed());
        item.setFileLinesUncomressed(template.getFileLinesUncomressed());
        item.setFileContent(template.getFileContent());

        item.setCreated(order.getCreated());
        return item;
    }

    private static DBItemHistoryLog newOrderStepLog(DBItemHistoryOrderStep orderStep, DBItemHistoryLog template) throws Exception {
        DBItemHistoryLog item = new DBItemHistoryLog();
        item.setControllerId(orderStep.getControllerId());
        item.setHistoryOrderId(orderStep.getHistoryOrderId());
        item.setHistoryOrderMainParentId(orderStep.getHistoryOrderMainParentId());
        item.setHistoryOrderStepId(orderStep.getId());

        item.setCompressed(true);
        item.setFileBasename(template.getFileBasename());
        item.setFileSizeUncomressed(template.getFileSizeUncomressed());
        item.setFileLinesUncomressed(template.getFileLinesUncomressed());
        item.setFileContent(template.getFileContent());

        item.setCreated(orderStep.getCreated());
        return item;
    }

    private static String hashOrderConstraint(String controllerId, Long eventId, String orderId) {
        return SOSString.hash256(new StringBuilder(controllerId).append(eventId).append(orderId).toString());
    }

    private static String hashOrderStepConstraint(String controllerId, Long eventId, String orderId, String workflowPosition) {
        return SOSString.hash256(new StringBuilder(controllerId).append(eventId).append(orderId).append(workflowPosition).toString());
    }

    private static void printUsage(int defaultOrders, int defaultStepsPerOrder, boolean defaultInsertMonitoring) {
        System.out.println("Usage:--------------------------------------");
        System.out.println("     -hibernate_configuration_file=<path>");
        System.out.println("     -controller_id=<controllerId>");
        System.out.println("     -orders=<number>               default=" + defaultOrders);
        System.out.println("     -steps_per_order=<number>      default=" + defaultStepsPerOrder);
        System.out.println("     -insert_monitoring=<boolean>   default=" + defaultInsertMonitoring);
        System.out.println("     Insert Logs:");
        System.out.println("         Order Logs: create a template file \"" + FILE_NAME_TEMPLATE_ORDER_LOG + "\" in the script folder");
        System.out.println("         Order Step Logs: create a template file \"" + FILE_NAME_TEMPLATE_ORDER_STEP_LOG + "\" in the script folder");
        System.out.println("--------------------------------------------");
    }

    private static SOSHibernateFactory createFactory(Path configFile) throws Exception {
        SOSHibernateFactory factory = new SOSHibernateFactory(configFile);
        factory.setIdentifier("history");
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getHistoryClassMapping());
        factory.addClassMapping(DBLayer.getMonitoringClassMapping());
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
