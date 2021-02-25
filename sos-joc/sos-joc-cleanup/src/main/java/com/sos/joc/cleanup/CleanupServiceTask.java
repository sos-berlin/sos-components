package com.sos.joc.cleanup;

import java.time.ZonedDateTime;
import java.util.ArrayList;
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
import com.sos.joc.cleanup.model.CleanupTaskDailyplan;
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

        LOGGER.info(String.format("[%s][run]start ...", logIdentifier));
        JocCluster cluster = JocClusterService.getInstance().getCluster();
        if (cluster.getHandler().isActive()) {
            CleanupServiceSchedule cleanupSchedule = this.schedule;
            List<IJocClusterService> services = cluster.getHandler().getServices();
            LOGGER.info(String.format("[%s][run]found %s running services", logIdentifier, services.size()));

            int batchSize = cleanupSchedule.getService().getConfig().getBatchSize();
            ZonedDateTime date = CleanupService.getZonedDateTimeUTCMinusMinutes(cleanupSchedule.getFirstStart(), cleanupSchedule.getService()
                    .getConfig().getAge().getMinutes());
            String dateInfo = cleanupSchedule.getService().getConfig().getAge().getConfigured() + "=" + date;

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
                        if (service.getIdentifier().equals(ClusterServices.history.name())) {
                            task = new CleanupTaskHistory(cleanupSchedule.getFactory(), service, batchSize);
                        } else if (service.getIdentifier().equals(ClusterServices.dailyplan.name())) {
                            task = new CleanupTaskDailyplan(cleanupSchedule.getFactory(), service, batchSize);
                        }

                        if (task == null) {
                            LOGGER.info(String.format("[%s][%s][skip]not implemented yet", logIdentifier, service.getIdentifier()));
                            LOGGER.info(String.format("[%s][%s]completed", logIdentifier, service.getIdentifier()));
                        } else {
                            executeTask(task, date, dateInfo, cleanupSchedule.getUncompleted());
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

                        executeTask(manualTask, date, dateInfo, cleanupSchedule.getUncompleted());

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

        return getAnswer();
    }

    private void executeTask(ICleanupTask task, ZonedDateTime date, String dateInfo, List<String> uncompleted) {
        boolean run = true;
        if (uncompleted != null) {
            if (uncompleted.contains(task.getIdentifier())) {
                run = false;
                LOGGER.info(String.format("[%s][%s][%s][skip]is already completed", logIdentifier, task.getTypeName(), task.getIdentifier()));
            }
        }
        if (run) {
            LOGGER.info(String.format("[%s][%s][%s][%s]start...", logIdentifier, task.getTypeName(), task.getIdentifier(), dateInfo));
            cleanupTasks.add(task);
            task.start(CleanupService.toDate(date));
            LOGGER.info(String.format("[%s][%s][%s][%s]%s", logIdentifier, task.getTypeName(), task.getIdentifier(), dateInfo, SOSString.toString(task
                    .getState())));
            task.stop();
            if (LOGGER.isDebugEnabled()) {
                LOGGER.debug(String.format("[%s][%s][%s][%s]completed", logIdentifier, task.getTypeName(), task.getIdentifier(), dateInfo));
            }
        }
    }

    private List<ICleanupTask> getManualCleanupTasks(JocClusterHibernateFactory factory, int batchSize) {
        List<ICleanupTask> tasks = new ArrayList<ICleanupTask>();
        tasks.add(new CleanupTaskDeployment(factory, batchSize, "deployment"));
        return tasks;
    }

    public JocClusterAnswer stop() {
        return stopCleanupTasks();
    }

    private JocClusterAnswer stopCleanupTasks() {
        if (cleanupTasks == null || cleanupTasks.size() == 0) {
            return JocCluster.getOKAnswer(JocClusterAnswerState.STOPPED);
        }
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
}
