package com.sos.joc.cleanup;

import java.nio.file.Path;
import java.sql.Connection;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sos.commons.hibernate.SOSHibernateFactory.Dbms;
import com.sos.commons.util.SOSString;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.cleanup.CleanupServiceConfiguration.Age;
import com.sos.joc.cleanup.model.CleanupTaskAuditLog;
import com.sos.joc.cleanup.model.CleanupTaskDailyPlan;
import com.sos.joc.cleanup.model.CleanupTaskDeployment;
import com.sos.joc.cleanup.model.CleanupTaskHistory;
import com.sos.joc.cleanup.model.CleanupTaskMonitoring;
import com.sos.joc.cleanup.model.CleanupTaskUserProfiles;
import com.sos.joc.cleanup.model.CleanupTaskYade;
import com.sos.joc.cleanup.model.ICleanupTask;
import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.cluster.configuration.JocClusterConfiguration.StartupMode;
import com.sos.joc.db.DBLayer;
import com.sos.joc.model.cluster.common.ClusterServices;

public class CleanupServiceTask implements Callable<JocClusterAnswer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupServiceTask.class);

    private static final String MANUAL_TASK_IDENTIFIER_DEPLOYMENT = "deployment";
    private static final String MANUAL_TASK_IDENTIFIER_AUDITLOG = "auditlog";
    private static final String MANUAL_TASK_IDENTIFIER_YADE = "file_transfer";
    private static final String MANUAL_TASK_IDENTIFIER_USER_PROFILES = "user_profiles";
    private static final String MANUAL_TASK_IDENTIFIER_GIT = "git";

    /** seconds */
    private static final int MAX_AWAIT_TERMINATION_TIMEOUT = 3 * 60;
    private static final int MAX_AWAIT_TERMINATION_TIMEOUT_ON_START_MODE_AUTOMATIC = 60;
    private static final int MAX_BATCH_SIZE_ORACLE = 1_000;

    private final CleanupServiceSchedule schedule;
    private final String identifier;
    private final String logIdentifier;

    private JocClusterHibernateFactory factory = null;
    private List<ICleanupTask> cleanupTasks = null;
    private StartupMode startMode = StartupMode.unknown;
    private int batchSize;

    public CleanupServiceTask(CleanupServiceSchedule schedule) {
        this.schedule = schedule;
        this.identifier = schedule.getService().getIdentifier();
        this.logIdentifier = identifier + "_task";
    }

    @Override
    public JocClusterAnswer call() {
        cleanupTasks = new ArrayList<>();
        CleanupService.setServiceLogger();

        schedule.getService().setLastActivityStart(new Date().getTime());
        LOGGER.info(String.format("[%s][run]start ...", logIdentifier));
        JocCluster cluster = JocClusterService.getInstance().getCluster();
        if (cluster.getHandler().isActive()) {
            CleanupServiceSchedule cleanupSchedule = this.schedule;
            List<IJocClusterService> services = cluster.getHandler().getServices();
            LOGGER.info(String.format("[%s][run]found %s running services", logIdentifier, services.size()));

            try {
                createFactory(cleanupSchedule.getService().getConfig().getHibernateConfiguration(), 5);
            } catch (Exception e) {
                LOGGER.error(String.format("[%s][createFactory]%s", logIdentifier, e.toString()), e);
                return JocCluster.getErrorAnswer(e);
            }
            batchSize = cleanupSchedule.getService().getConfig().getBatchSize();
            try {
                if (batchSize > MAX_BATCH_SIZE_ORACLE && Dbms.ORACLE.equals(factory.getDbms())) {
                    LOGGER.info(String.format("[%s][run][configured batch_size=%s][skip]use max batch_size=%s for oracle", logIdentifier, batchSize,
                            MAX_BATCH_SIZE_ORACLE));

                    batchSize = MAX_BATCH_SIZE_ORACLE;
                }
            } catch (Throwable e) {
                LOGGER.warn(e.toString(), e);
            }

            List<Supplier<JocClusterAnswer>> tasks = new ArrayList<Supplier<JocClusterAnswer>>();
            // 1) service tasks
            for (IJocClusterService service : services) {
                if (service.getIdentifier().equals(ClusterServices.cleanup.name())) {
                    LOGGER.info("  [service][skip]" + service.getIdentifier());
                    continue;
                }
                LOGGER.info("  [service]" + service.getIdentifier());
                Supplier<JocClusterAnswer> task = new Supplier<JocClusterAnswer>() {

                    @Override
                    public JocClusterAnswer get() {
                        CleanupService.setServiceLogger();

                        ICleanupTask task = null;
                        boolean disabled = false;
                        List<TaskDateTime> datetimes = new ArrayList<TaskDateTime>();
                        if (service.getIdentifier().equals(ClusterServices.history.name())) {
                            TaskDateTime orderDatetime = new TaskDateTime(cleanupSchedule.getService().getConfig().getOrderHistoryAge(),
                                    cleanupSchedule.getFirstStart());

                            TaskDateTime orderLogsDatetime = new TaskDateTime(cleanupSchedule.getService().getConfig().getOrderHistoryLogsAge(),
                                    cleanupSchedule.getFirstStart());

                            if (orderDatetime.getDatetime() == null && orderLogsDatetime.getDatetime() == null) {
                                disabled = true;
                            } else {
                                task = new CleanupTaskHistory(factory, service, batchSize);
                                datetimes.add(orderDatetime);
                                datetimes.add(orderLogsDatetime);

                            }
                        } else if (service.getIdentifier().equals(ClusterServices.dailyplan.name())) {
                            TaskDateTime datetime = new TaskDateTime(cleanupSchedule.getService().getConfig().getDailyPlanHistoryAge(),
                                    cleanupSchedule.getFirstStart());
                            if (datetime.getDatetime() == null) {
                                disabled = true;
                            } else {
                                task = new CleanupTaskDailyPlan(factory, service, batchSize);
                                datetimes.add(datetime);
                            }
                        } else if (service.getIdentifier().equals(ClusterServices.monitor.name())) {
                            TaskDateTime monitoringDatetime = new TaskDateTime(cleanupSchedule.getService().getConfig().getMonitoringHistoryAge(),
                                    cleanupSchedule.getFirstStart());
                            TaskDateTime notificationDatetime = new TaskDateTime(cleanupSchedule.getService().getConfig().getNotificationHistoryAge(),
                                    cleanupSchedule.getFirstStart());
                            if (monitoringDatetime.getDatetime() == null && notificationDatetime.getDatetime() == null) {
                                disabled = true;
                            } else {
                                task = new CleanupTaskMonitoring(factory, service, batchSize);
                                datetimes.add(monitoringDatetime);
                                datetimes.add(notificationDatetime);
                            }
                        }

                        if (disabled) {
                            LOGGER.info(String.format("[%s][%s][skip]age=0", logIdentifier, service.getIdentifier()));
                            LOGGER.info(String.format("[%s][%s]completed", logIdentifier, service.getIdentifier()));
                        } else {
                            if (task == null) {
                                LOGGER.info(String.format("[%s][%s][skip]not implemented yet", logIdentifier, service.getIdentifier()));
                                LOGGER.info(String.format("[%s][%s]completed", logIdentifier, service.getIdentifier()));
                            } else {
                                executeTask(task, datetimes, cleanupSchedule.getUncompleted());
                            }
                        }

                        return JocCluster.getOKAnswer(JocClusterAnswerState.COMPLETED);
                    }
                };
                tasks.add(task);
            }

            // 2) manual tasks
            List<ICleanupTask> manualTasks = getManualCleanupTasks(factory, batchSize);
            LOGGER.info(String.format("[%s][run]found %s manual tasks", logIdentifier, manualTasks.size()));
            for (ICleanupTask manualTask : manualTasks) {
                LOGGER.info("  [manual]" + manualTask.getIdentifier());
                Supplier<JocClusterAnswer> task = new Supplier<JocClusterAnswer>() {

                    @Override
                    public JocClusterAnswer get() {
                        CleanupService.setServiceLogger();

                        if (manualTask.getIdentifier().equals(MANUAL_TASK_IDENTIFIER_DEPLOYMENT)) {
                            int versions = cleanupSchedule.getService().getConfig().getDeploymentHistoryVersions();
                            if (versions == 0) {
                                LOGGER.info(String.format("[%s][%s][skip]versions=0", manualTask.getTypeName(), manualTask.getIdentifier()));
                            } else {
                                executeTask(manualTask, versions, cleanupSchedule.getUncompleted());
                            }
                        } else if (manualTask.getIdentifier().equals(MANUAL_TASK_IDENTIFIER_AUDITLOG)) {
                            List<TaskDateTime> datetimes = new ArrayList<TaskDateTime>();
                            TaskDateTime datetime = new TaskDateTime(cleanupSchedule.getService().getConfig().getAuditLogAge(), cleanupSchedule
                                    .getFirstStart());
                            if (datetime.getDatetime() == null) {
                                LOGGER.info(String.format("[%s][%s][skip]age=0", logIdentifier, manualTask.getIdentifier()));
                                LOGGER.info(String.format("[%s][%s]completed", logIdentifier, manualTask.getIdentifier()));
                            } else {
                                datetimes.add(datetime);
                                executeTask(manualTask, datetimes, cleanupSchedule.getUncompleted());
                            }

                        } else if (manualTask.getIdentifier().equals(MANUAL_TASK_IDENTIFIER_YADE)) {
                            List<TaskDateTime> datetimes = new ArrayList<TaskDateTime>();
                            TaskDateTime datetime = new TaskDateTime(cleanupSchedule.getService().getConfig().getFileTransferHistoryAge(),
                                    cleanupSchedule.getFirstStart());
                            if (datetime.getDatetime() == null) {
                                LOGGER.info(String.format("[%s][%s][skip]age=0", logIdentifier, manualTask.getIdentifier()));
                                LOGGER.info(String.format("[%s][%s]completed", logIdentifier, manualTask.getIdentifier()));
                            } else {
                                datetimes.add(datetime);
                                executeTask(manualTask, datetimes, cleanupSchedule.getUncompleted());
                            }

                        } else if (manualTask.getIdentifier().equals(MANUAL_TASK_IDENTIFIER_USER_PROFILES)) {
                            List<TaskDateTime> datetimes = new ArrayList<TaskDateTime>();
                            TaskDateTime profileDateTime = new TaskDateTime(cleanupSchedule.getService().getConfig().getProfileAge(), cleanupSchedule
                                    .getFirstStart());
                            TaskDateTime failedLoginHistoryDateTime = new TaskDateTime(cleanupSchedule.getService().getConfig()
                                    .getFailedLoginHistoryAge(), cleanupSchedule.getFirstStart());
                            if (profileDateTime.getDatetime() == null && failedLoginHistoryDateTime.getDatetime() == null) {
                                LOGGER.info(String.format("[%s][%s][skip]age=0", logIdentifier, manualTask.getIdentifier()));
                                LOGGER.info(String.format("[%s][%s]completed", logIdentifier, manualTask.getIdentifier()));
                            } else {
                                datetimes.add(profileDateTime);
                                datetimes.add(failedLoginHistoryDateTime);
                                executeTask(manualTask, datetimes, cleanupSchedule.getUncompleted());
                            }

                        } else if (manualTask.getIdentifier().equals(MANUAL_TASK_IDENTIFIER_GIT)) {
                            List<TaskDateTime> datetimes = new ArrayList<TaskDateTime>();
                            executeTask(manualTask, datetimes, cleanupSchedule.getUncompleted());
                        } else {
                            LOGGER.info(String.format("  [%s][skip][%s]not implemented yet", manualTask.getTypeName(), manualTask.getIdentifier()));
                        }
                        return JocCluster.getOKAnswer(JocClusterAnswerState.COMPLETED);
                    }
                };
                tasks.add(task);
            }

            if (tasks.size() > 0) {
                ExecutorService es = Executors.newFixedThreadPool(tasks.size(), new JocClusterThreadFactory(cleanupSchedule.getService()
                        .getThreadGroup(), identifier + "-t-s-start"));
                List<CompletableFuture<JocClusterAnswer>> futuresList = tasks.stream().map(task -> CompletableFuture.supplyAsync(task, es)).collect(
                        Collectors.toList());
                CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()])).join();
                JocCluster.shutdownThreadPool(null, es, 3);
            }

        } else {
            LOGGER.info(String.format("[%s][run][skip]cluster not active", logIdentifier));
        }
        LOGGER.info(String.format("[%s][run]end", logIdentifier));
        schedule.getService().setLastActivityEnd(new Date().getTime());

        return getAnswer();
    }

    private void executeTask(ICleanupTask task, List<TaskDateTime> datetimes, List<String> uncompleted) {
        boolean run = true;
        if (uncompleted != null) {
            if (!uncompleted.contains(task.getIdentifier())) {
                run = false;
                LOGGER.info(String.format("[%s][%s][%s][skip]is already completed", logIdentifier, task.getTypeName(), task.getIdentifier()));
            }
        }
        if (run) {
            LOGGER.info(String.format("[%s][%s][%s]start...", logIdentifier, task.getTypeName(), task.getIdentifier()));
            cleanupTasks.add(task);
            task.start(datetimes);
            LOGGER.info(String.format("[%s][%s][%s][completed=%s]%s", logIdentifier, task.getTypeName(), task.getIdentifier(), task.isCompleted(),
                    SOSString.toString(task.getState())));
            task.stop(getMaxAwaitTimeout());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][%s][%s]completed=%s", logIdentifier, task.getTypeName(), task.getIdentifier(), task.isCompleted()));
            }
        }
    }

    private void executeTask(ICleanupTask task, int counter, List<String> uncompleted) {
        boolean run = true;
        if (uncompleted != null) {
            if (!uncompleted.contains(task.getIdentifier())) {
                run = false;
                LOGGER.info(String.format("[%s][%s][%s][skip]is already completed", logIdentifier, task.getTypeName(), task.getIdentifier()));
            }
        }
        if (run) {
            LOGGER.info(String.format("[%s][%s][%s][%s]start...", logIdentifier, task.getTypeName(), task.getIdentifier(), counter));
            cleanupTasks.add(task);
            task.start(counter);
            LOGGER.info(String.format("[%s][%s][%s][%s][completed=%s]%s", logIdentifier, task.getTypeName(), task.getIdentifier(), counter, task
                    .isCompleted(), SOSString.toString(task.getState())));
            task.stop(getMaxAwaitTimeout());
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][%s][%s][%s]completed=%s", logIdentifier, task.getTypeName(), task.getIdentifier(), counter, task
                        .isCompleted()));
            }
        }
    }

    private List<ICleanupTask> getManualCleanupTasks(JocClusterHibernateFactory factory, int batchSize) {
        List<ICleanupTask> tasks = new ArrayList<ICleanupTask>();
        tasks.add(new CleanupTaskDeployment(factory, batchSize, MANUAL_TASK_IDENTIFIER_DEPLOYMENT));
        tasks.add(new CleanupTaskAuditLog(factory, batchSize, MANUAL_TASK_IDENTIFIER_AUDITLOG));
        tasks.add(new CleanupTaskYade(factory, batchSize, MANUAL_TASK_IDENTIFIER_YADE));
        // tasks.add(new CleanupTaskGit(factory, batchSize, MANUAL_TASK_IDENTIFIER_GIT));
        tasks.add(new CleanupTaskUserProfiles(factory, batchSize, MANUAL_TASK_IDENTIFIER_USER_PROFILES));
        return tasks;
    }

    public synchronized JocClusterAnswer stop(int timeout) {
        if (cleanupTasks == null || cleanupTasks.size() == 0) {
            return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
        }
        schedule.getService().setLastActivityStart(new Date().getTime());

        List<Supplier<JocServiceTaskAnswer>> tasks = new ArrayList<Supplier<JocServiceTaskAnswer>>();
        for (int i = 0; i < cleanupTasks.size(); i++) {
            ICleanupTask cleanupTask = cleanupTasks.get(i);

            Supplier<JocServiceTaskAnswer> task = new Supplier<JocServiceTaskAnswer>() {

                @Override
                public JocServiceTaskAnswer get() {
                    CleanupService.setServiceLogger();
                    JocServiceTaskAnswer answer = null;
                    if (cleanupTask.isStopped()) {
                        answer = new JocServiceTaskAnswer(cleanupTask.getState());
                        LOGGER.info(String.format("[%s][%s][%s][stop][completed=%s]already stopped", logIdentifier, cleanupTask.getTypeName(),
                                cleanupTask.getIdentifier(), cleanupTask.isCompleted()));
                    } else {
                        LOGGER.info(String.format("[%s][%s][%s][stop][completed=%s]start...", logIdentifier, cleanupTask.getTypeName(), cleanupTask
                                .getIdentifier(), cleanupTask.isCompleted()));
                        answer = cleanupTask.stop(timeout > 0 ? timeout : getMaxAwaitTimeout());
                        LOGGER.info(String.format("[%s][%s][%s][stop][completed=%s][end]%s", logIdentifier, cleanupTask.getTypeName(), cleanupTask
                                .getIdentifier(), cleanupTask.isCompleted(), SOSString.toString(answer)));
                    }
                    return answer;
                }
            };
            tasks.add(task);
        }

        // close all cleanups
        if (tasks.size() > 0) {
            ExecutorService es = Executors.newFixedThreadPool(tasks.size(), new JocClusterThreadFactory(schedule.getService().getThreadGroup(),
                    identifier + "-t-s-stop"));
            List<CompletableFuture<JocServiceTaskAnswer>> futuresList = tasks.stream().map(task -> CompletableFuture.supplyAsync(task, es)).collect(
                    Collectors.toList());
            CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()])).join();
            JocCluster.shutdownThreadPool(null, es, 3);
        }
        JocClusterAnswer answer = getAnswer();

        closeFactory();
        cleanupTasks = new ArrayList<>();
        schedule.getService().setLastActivityEnd(new Date().getTime());
        return answer;
    }

    private JocClusterAnswer getAnswer() {
        if (cleanupTasks.size() == 0) {
            return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
        }
        List<String> nonCompleted = cleanupTasks.stream().filter(t -> t.getState() == null || !t.getState().equals(
                JocServiceTaskAnswerState.COMPLETED)).map(t -> {
                    return t.getIdentifier();
                }).collect(Collectors.toList());
        if (nonCompleted.size() == 0) {
            return JocCluster.getOKAnswer(JocClusterAnswerState.COMPLETED);
        } else {
            return JocCluster.getOKAnswer(JocClusterAnswerState.UNCOMPLETED, String.join(",", nonCompleted));
        }
    }

    protected void setStartMode(StartupMode val) {
        startMode = val;
    }

    protected StartupMode getStartMode() {
        return startMode;
    }

    private int getMaxAwaitTimeout() {
        return startMode.equals(StartupMode.automatic) ? MAX_AWAIT_TERMINATION_TIMEOUT_ON_START_MODE_AUTOMATIC : MAX_AWAIT_TERMINATION_TIMEOUT;
    }

    private void createFactory(Path configFile, int maxPoolSize) throws Exception {
        if (factory != null) {
            factory.close();
        }

        factory = new JocClusterHibernateFactory(configFile, 1, maxPoolSize);
        factory.setIdentifier(logIdentifier);
        factory.setAutoCommit(false);
        factory.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
        factory.addClassMapping(DBLayer.getJocClassMapping());

        factory.build();
    }

    private void closeFactory() {
        CleanupService.setServiceLogger();
        if (factory != null) {
            factory.close();
            factory = null;
        }
        LOGGER.info(String.format("[%s]database factory closed", logIdentifier));
    }

    public class TaskDateTime {

        private final Age age;
        private ZonedDateTime zonedDatetime = null;
        private Date datetime;

        public TaskDateTime(Age age, ZonedDateTime start) {
            this.age = age;
            if (this.age.getMinutes() > 0) {
                this.zonedDatetime = CleanupService.getZonedDateTimeUTCMinusMinutes(start, this.age.getMinutes());
                this.datetime = CleanupService.toDate(zonedDatetime);
            }
        }

        public Age getAge() {
            return age;
        }

        public ZonedDateTime getZonedDatetime() {
            return zonedDatetime;
        }

        public Date getDatetime() {
            return datetime;
        }

    }
}
