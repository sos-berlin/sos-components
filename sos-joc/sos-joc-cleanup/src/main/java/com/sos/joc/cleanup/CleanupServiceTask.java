package com.sos.joc.cleanup;

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

import com.sos.commons.util.SOSString;
import com.sos.joc.classes.cluster.JocClusterService;
import com.sos.joc.cleanup.CleanupServiceConfiguration.Age;
import com.sos.joc.cleanup.model.CleanupTaskDailyPlan;
import com.sos.joc.cleanup.model.CleanupTaskDeployment;
import com.sos.joc.cleanup.model.CleanupTaskHistory;
import com.sos.joc.cleanup.model.ICleanupTask;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterHibernateFactory;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.model.cluster.common.ClusterServices;

public class CleanupServiceTask implements Callable<JocClusterAnswer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupServiceTask.class);

    private final String MANUAL_TASK_IDENTIFIER_DEPLOYMENT = "deployment";
    private final CleanupServiceSchedule schedule;
    private final String identifier;
    private final String logIdentifier;
    private List<ICleanupTask> cleanupTasks = null;

    public CleanupServiceTask(CleanupServiceSchedule schedule) {
        this.schedule = schedule;
        this.identifier = schedule.getService().getIdentifier();
        this.logIdentifier = identifier + "_task";
    }

    @Override
    public JocClusterAnswer call() {
        cleanupTasks = new ArrayList<>();
        AJocClusterService.setLogger(identifier);

        schedule.getService().setLastActivityStart(new Date().getTime());
        LOGGER.info(String.format("[%s][run]start ...", logIdentifier));
        JocCluster cluster = JocClusterService.getInstance().getCluster();
        if (cluster.getHandler().isActive()) {
            CleanupServiceSchedule cleanupSchedule = this.schedule;
            List<IJocClusterService> services = cluster.getHandler().getServices();
            LOGGER.info(String.format("[%s][run]found %s running services", logIdentifier, services.size()));

            int batchSize = cleanupSchedule.getService().getConfig().getBatchSize();
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
                        AJocClusterService.setLogger(identifier);

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
                                task = new CleanupTaskHistory(cleanupSchedule.getFactory(), service, batchSize);
                                datetimes.add(orderDatetime);
                                datetimes.add(orderLogsDatetime);

                            }
                        } else if (service.getIdentifier().equals(ClusterServices.dailyplan.name())) {
                            TaskDateTime datetime = new TaskDateTime(cleanupSchedule.getService().getConfig().getDailyPlanHistoryAge(),
                                    cleanupSchedule.getFirstStart());
                            if (datetime.getDatetime() == null) {
                                disabled = true;
                            } else {
                                task = new CleanupTaskDailyPlan(cleanupSchedule.getFactory(), service, batchSize);
                                datetimes.add(datetime);
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

                        AJocClusterService.clearLogger();
                        return JocCluster.getOKAnswer(JocClusterAnswerState.COMPLETED);
                    }
                };
                tasks.add(task);
            }

            // 2) manual tasks
            List<ICleanupTask> manualTasks = getManualCleanupTasks(cleanupSchedule.getFactory(), batchSize);
            LOGGER.info(String.format("[%s][run]found %s manual tasks", logIdentifier, manualTasks.size()));
            for (ICleanupTask manualTask : manualTasks) {
                LOGGER.info("  [manual]" + manualTask.getIdentifier());
                Supplier<JocClusterAnswer> task = new Supplier<JocClusterAnswer>() {

                    @Override
                    public JocClusterAnswer get() {
                        AJocClusterService.setLogger(identifier);

                        if (manualTask.getIdentifier().equals(MANUAL_TASK_IDENTIFIER_DEPLOYMENT)) {
                            int versions = cleanupSchedule.getService().getConfig().getDeploymentHistoryVersions();
                            if (versions == 0) {
                                LOGGER.info(String.format("[%s][%s][skip]versions=0", manualTask.getTypeName(), manualTask.getIdentifier()));
                            } else {
                                executeTask(manualTask, versions, cleanupSchedule.getUncompleted());
                            }
                        } else {
                            LOGGER.info(String.format("  [%s][skip][%s]not implemented yet", manualTask.getTypeName(), manualTask.getIdentifier()));
                        }
                        AJocClusterService.clearLogger();
                        return JocCluster.getOKAnswer(JocClusterAnswerState.COMPLETED);
                    }
                };
                tasks.add(task);
            }

            if (tasks.size() > 0) {
                ExecutorService es = Executors.newFixedThreadPool(tasks.size(), new JocClusterThreadFactory(cleanupSchedule.getService()
                        .getThreadGroup(), identifier + "-t-h-start"));
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
            LOGGER.info(String.format("[%s][%s][%s]%s", logIdentifier, task.getTypeName(), task.getIdentifier(), SOSString.toString(task
                    .getState())));
            task.stop();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][%s][%s]completed", logIdentifier, task.getTypeName(), task.getIdentifier()));
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
            LOGGER.info(String.format("[%s][%s][%s][%s]%s", logIdentifier, task.getTypeName(), task.getIdentifier(), counter, SOSString.toString(task
                    .getState())));
            task.stop();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][%s][%s][%s]completed", logIdentifier, task.getTypeName(), task.getIdentifier(), counter));
            }
        }
    }

    private List<ICleanupTask> getManualCleanupTasks(JocClusterHibernateFactory factory, int batchSize) {
        List<ICleanupTask> tasks = new ArrayList<ICleanupTask>();
        tasks.add(new CleanupTaskDeployment(factory, batchSize, MANUAL_TASK_IDENTIFIER_DEPLOYMENT));
        return tasks;
    }

    public JocClusterAnswer stop() {
        return stopCleanupTasks();
    }

    private JocClusterAnswer stopCleanupTasks() {
        if (cleanupTasks == null || cleanupTasks.size() == 0) {
            return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
        }
        schedule.getService().setLastActivityStart(new Date().getTime());

        // close all cleanups
        ExecutorService threadPool = Executors.newFixedThreadPool(cleanupTasks.size(), new JocClusterThreadFactory(schedule.getService()
                .getThreadGroup(), identifier + "-t-h-stop"));
        for (int i = 0; i < cleanupTasks.size(); i++) {
            ICleanupTask task = cleanupTasks.get(i);
            Runnable thread = new Runnable() {

                @Override
                public void run() {
                    AJocClusterService.setLogger(identifier);
                    if (task.isStopped()) {
                        LOGGER.info(String.format("[%s][%s][%s][stop]already stopped", logIdentifier, task.getTypeName(), task.getIdentifier()));
                    } else {
                        LOGGER.info(String.format("[%s][%s][%s][stop]start...", logIdentifier, task.getTypeName(), task.getIdentifier()));
                        JocServiceTaskAnswer answer = task.stop();
                        LOGGER.info(String.format("[%s][%s][%s][stop][end]%s", logIdentifier, task.getTypeName(), task.getIdentifier(), SOSString
                                .toString(answer)));
                    }
                    AJocClusterService.clearLogger();
                }
            };
            threadPool.submit(thread);
        }
        JocClusterAnswer answer = getAnswer();

        AJocClusterService.setLogger(identifier);
        JocCluster.shutdownThreadPool(null, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
        AJocClusterService.clearLogger();

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
