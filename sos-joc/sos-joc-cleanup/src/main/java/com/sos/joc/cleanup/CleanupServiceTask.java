package com.sos.joc.cleanup;

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
import com.sos.joc.cleanup.model.CleanupTaskHistory;
import com.sos.joc.cleanup.model.ICleanupTask;
import com.sos.joc.cluster.AJocClusterService;
import com.sos.joc.cluster.IJocClusterService;
import com.sos.joc.cluster.JocCluster;
import com.sos.joc.cluster.JocClusterThreadFactory;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer;
import com.sos.joc.cluster.bean.answer.JocClusterAnswer.JocClusterAnswerState;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer;
import com.sos.joc.cluster.bean.answer.JocServiceTaskAnswer.JocServiceTaskAnswerState;
import com.sos.joc.model.cluster.common.ClusterServices;

public class CleanupServiceTask implements Callable<JocClusterAnswer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanupServiceTask.class);

    private final CleanupService service;
    private final String identifier;
    private final String logIdentifier;
    private List<ICleanupTask> cleanupTasks = null;

    public CleanupServiceTask(CleanupService service) {
        this.service = service;
        this.identifier = service.getIdentifier();
        this.logIdentifier = identifier + "_task";
    }

    @Override
    public JocClusterAnswer call() {
        cleanupTasks = new ArrayList<>();
        AJocClusterService.setLogger(identifier);

        LOGGER.info(String.format("[%s][run]start ...", logIdentifier));
        JocCluster cluster = JocClusterService.getInstance().getCluster();
        if (cluster.getHandler().isActive()) {
            List<IJocClusterService> services = cluster.getHandler().getServices();
            LOGGER.info(String.format("[%s][run]found %s running services", logIdentifier, services.size()));

            List<Supplier<JocClusterAnswer>> tasks = new ArrayList<Supplier<JocClusterAnswer>>();
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
                            task = new CleanupTaskHistory(service);
                        } else if (service.getIdentifier().equals(ClusterServices.dailyplan.name())) {
                            task = new CleanupTaskDailyplan(service);
                        }

                        if (task == null) {
                            LOGGER.info(String.format("[%s][%s][skip]not implemented yet", logIdentifier, service.getIdentifier()));
                            LOGGER.info(String.format("[%s][%s]completed", logIdentifier, service.getIdentifier()));
                        } else {
                            LOGGER.info(String.format("[%s][%s]start...", logIdentifier, service.getIdentifier()));
                            cleanupTasks.add(task);
                            task.start();
                            LOGGER.info(String.format("[%s][%s]%s", logIdentifier, service.getIdentifier(), SOSString.toString(task.getState())));
                            task.stop();
                            LOGGER.info(String.format("[%s][%s]completed", logIdentifier, service.getIdentifier()));
                        }

                        AJocClusterService.clearLogger();

                        return JocCluster.getOKAnswer(JocClusterAnswerState.COMPLETED);
                    }
                };
                tasks.add(task);
            }

            if (tasks.size() > 0) {
                ExecutorService es = Executors.newFixedThreadPool(tasks.size(), new JocClusterThreadFactory(service.getThreadGroup(), identifier
                        + "-t-h-start"));
                List<CompletableFuture<JocClusterAnswer>> futuresList = tasks.stream().map(task -> CompletableFuture.supplyAsync(task, es)).collect(
                        Collectors.toList());
                CompletableFuture.allOf(futuresList.toArray(new CompletableFuture[futuresList.size()])).join();
                JocCluster.shutdownThreadPool(null, es, 3);

            }

        } else {
            LOGGER.info(String.format("[%s][run][skip]cluster not active", logIdentifier));
        }
        LOGGER.info(String.format("[%s][run]end", logIdentifier));

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

    public void close() {
        stopCleanupTasks();
    }

    private void stopCleanupTasks() {
        if (cleanupTasks == null) {
            return;
        }
        int size = cleanupTasks.size();
        if (size > 0) {
            // close all cleanups
            ExecutorService threadPool = Executors.newFixedThreadPool(size, new JocClusterThreadFactory(service.getThreadGroup(), identifier
                    + "-t-h-stop"));
            for (int i = 0; i < size; i++) {
                ICleanupTask task = cleanupTasks.get(i);
                Runnable thread = new Runnable() {

                    @Override
                    public void run() {
                        if (task.isStopped()) {
                            LOGGER.info(String.format("[%s][%s][stop]already stopped", logIdentifier, task.getIdentifier()));
                        } else {
                            AJocClusterService.setLogger(identifier);
                            LOGGER.info(String.format("[%s][%s][stop]start...", logIdentifier, task.getIdentifier()));
                            JocServiceTaskAnswer answer = task.stop();
                            LOGGER.info(String.format("[%s][%s][stop][end]%s", logIdentifier, task.getIdentifier(), SOSString.toString(answer)));
                            AJocClusterService.clearLogger();
                        }
                    }
                };
                threadPool.submit(thread);
            }
            AJocClusterService.setLogger(identifier);
            JocCluster.shutdownThreadPool(null, threadPool, JocCluster.MAX_AWAIT_TERMINATION_TIMEOUT);
            AJocClusterService.clearLogger();
            cleanupTasks = new ArrayList<>();
        }
    }

}
